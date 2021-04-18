package ru.tsystems.tchallenge.service.domain.workbook;

import com.google.common.collect.Sets;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import ru.tsystems.tchallenge.service.domain.event.EventStatus;
import ru.tsystems.tchallenge.service.utility.search.SearchResult;
import ru.tsystems.tchallenge.service.domain.account.AccountCategory;
import ru.tsystems.tchallenge.service.domain.account.AccountDocument;
import ru.tsystems.tchallenge.service.domain.account.AccountRepository;
import ru.tsystems.tchallenge.service.domain.account.AccountRole;
import ru.tsystems.tchallenge.service.domain.event.EventDocument;
import ru.tsystems.tchallenge.service.domain.event.EventRepository;
import ru.tsystems.tchallenge.service.domain.event.HealthStatus;
import ru.tsystems.tchallenge.service.domain.maturity.Maturity;
import ru.tsystems.tchallenge.service.domain.problem.*;
import ru.tsystems.tchallenge.service.domain.specialization.SpecializationDocument;
import ru.tsystems.tchallenge.service.domain.specialization.SpecializationRepository;
import ru.tsystems.tchallenge.service.domain.statistics.wallboard.WallboardManager;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.AssignmentDocument;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.AssignmentUpdateInvoice;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.code.CodeSolutionDocument;
import ru.tsystems.tchallenge.service.domain.workbook.scoring.WorkbookScoringService;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.security.authentication.UserAuthentication;
import ru.tsystems.tchallenge.service.utility.mail.MailData;
import ru.tsystems.tchallenge.service.utility.mail.TemplateMailInvoice;
import ru.tsystems.tchallenge.service.utility.mail.TemplateMailManager;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static ru.tsystems.tchallenge.service.domain.workbook.WorkbookStatus.*;
import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder.forbidden;
import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.*;

@Component
@RequiredArgsConstructor
@Log4j2
public class WorkbookManagerBean implements WorkbookManager {

    private final SpecializationRepository specializationRepository;
    private final EventRepository eventRepository;
    private final ProblemRepository problemRepository;
    private final TemplateMailManager templateMailManager;
    private final MongoTemplate mongoTemplate;
    private final WorkbookRepository workbookRepository;
    private final WorkbookConverter workbookConverter;
    private final WorkbookScoringService workbookScoringService;
    private final AccountRepository accountRepository;
    private final WallboardManager wallboardManager;


    @Override
    public String create(UserAuthentication authentication, WorkbookInvoice invoice) {
        invoice.validate();
        WorkbookDocument unsubmited = unsubmitedWorkbook(authentication.getAccountId(), invoice.getEventId());
        if (unsubmited == null) {
            if (!canAttempt(authentication.getAccountId(), invoice.getEventId())) {
                throw noMoreAttempts();
            }
            final WorkbookDocument workbookDocument = prepareNewWorkbook(authentication, invoice);
            workbookRepository.insert(workbookDocument);
            log.info("Created workbook " + workbookDocument.getId());
            send(workbookDocument, invoice.getBacklinkPathTemplate(), authentication);
            wallboardManager.updateStats(invoice.getEventId());
            unsubmited = workbookDocument;
        }
        return unsubmited.getId();
    }

    @Override
    public Workbook retrieveById(String id) {
        return toWorkBookByStatus(get(id));
    }

    @Override
    public Boolean isReviewed(String id) {
        WorkbookDocument workbookDocument = get(id);
        return workbookDocument.getReviewed();
    }

    @Override
    public Boolean isAssessed(String id) {
        WorkbookDocument workbookDocument = get(id);
        return ( workbookDocument.getStatus() == ASSESSED );
    }

    @Override
    public Workbook reviewWorkbook(String id) {
        WorkbookDocument workbook = workbookRepository.findById(id)
                .orElseThrow(() -> workbookNotFound(id));

        if ( workbook.getStatus() != ASSESSED ) {
            throw forbidden();
        }

        workbook.setReviewed(true);
        workbookRepository.save(workbook);
        log.info("Reviewed workbook " + id);
        wallboardManager.updateStats(workbook.getEventId());
        return workbookConverter.toDto(workbook);
    }

    private boolean canAttempt(String accountId, String eventId) {
        List<WorkbookDocument> attempted = workbookRepository
                .findByOwnerIdAndEventId(accountId, eventId);
        EventDocument eventDocument = eventRepository.findById(eventId).orElseThrow(() -> eventNotFound(eventId));
        if (attempted.stream().anyMatch(WorkbookDocument::getReviewed) && eventDocument.getReviewThreshold() != null ) {
            return false;
        }
        AccountDocument accountDocument = accountRepository.findById(accountId)
                .orElseThrow(() -> accountNotFound(accountId));
        Instant now = Instant.now();
        if ((eventDocument.getStatus() != EventStatus.APPROVED)
                || isNotWhitelisted(eventDocument, accountDocument)
                || !isActive(eventDocument, now)) {
            throw forbidden();
        }
        Integer allowed = eventDocument.getNumberOfAttempts();
        return ((allowed == null) || (attempted.size() < allowed));
    }

    private WorkbookDocument unsubmitedWorkbook(String accountId, String eventId){
        return workbookRepository
                .findByOwnerIdAndEventId(accountId, eventId)
                .stream().filter( w -> (w.getStatus().equals(WorkbookStatus.APPROVED) && w.getSubmittableUntil().isAfter(Instant.now())) )
                .findFirst()
                .orElse(null);
    }

    private boolean isNotWhitelisted(EventDocument eventDocument, AccountDocument accountDocument) {
        return eventDocument.getWhiteListOnly()
                && ((eventDocument.getEmails()== null) ||
                eventDocument.getEmails().stream().noneMatch(accountDocument.getEmail()::equalsIgnoreCase));
    }

    private boolean isActive(EventDocument eventDocument, Instant now) {
        return (eventDocument.getValidFrom().isBefore(now) || eventDocument.getValidFrom().equals(now))
                && eventDocument.getValidUntil().isAfter(now);
    }


    private Workbook toWorkBookByStatus(WorkbookDocument workbookDocument) {
        if ( workbookDocument.getStatus() == ASSESSED ) {
            return workbookConverter.toDto(workbookDocument);
        } else {
            return workbookConverter.toClassifiedDto(workbookDocument);
        }
    }

    private WorkbookDocument get(String id) {
        return workbookRepository.findById(id)
                .orElseThrow(() -> workbookNotFound(id));
    }

    @Override
    public void updateAssignment(String id, Integer index, AssignmentUpdateInvoice invoice) {
        invoice.validate();
        final WorkbookDocument workbookDocument = get(id);
        if (isWorkbookExpired(workbookDocument)) {
            throw workbookExpired(workbookDocument.getSubmittableUntil());
        }
        if ( workbookDocument.getStatus() == SUBMITTED || workbookDocument.getStatus() == ASSESSED ) {
            throw forbidden();
        }

        AssignmentDocument assignmentDocument = workbookDocument.getAssignments().get(index - 1);
        assignmentDocument.setSolution(invoice.getSolution());
        workbookRepository.save(workbookDocument);
        log.info("Updated workbook " + id + " assignment " + index);
    }


    @Override
    public void validateUpdateStatus(UserAuthentication authentication, String id,
                                     WorkbookStatusUpdateInvoice invoice) {
        invoice.validate();
        final WorkbookDocument workbookDocument = get(id);
        if (authentication.getAccountCategory() == AccountCategory.PARTICIPANT) {
            validateParticipantUpdateStatus(workbookDocument, invoice);
        }

    }

    @Override
    public WorkbookStatus updateStatus(String id, WorkbookStatusUpdateInvoice invoice) {
        final WorkbookDocument workbookDocument = get(id);
        validateParticipantUpdateStatus(workbookDocument, invoice);
        workbookDocument.setStatus(invoice.getStatus());
        workbookRepository.save(workbookDocument);
        log.info("Updated workbook " + id + " status " + invoice.getStatus());
        WorkbookDocument wbd = workbookRepository.findById(id)
                .orElseThrow(() -> workbookNotFound(id));
        return wbd.getStatus();
    }

    @Override
    public Workbook assessSubmittedWorkbook(WorkbookDocument workbookDocument) {
        if (workbookDocument.getStatus() == SUBMITTED) {
            workbookScoringService.assessWorkbook(workbookDocument);
            workbookDocument.setAssessedAt(Instant.now());
            workbookDocument.setStatus( ASSESSED );
            log.info( "Assessed workbook " + workbookDocument.getId() );
            // For event, that doesn't require workbook review, automatically review workbook after submit
            String eventId = workbookDocument.getEventId();
            EventDocument eventDocument = eventRepository.findById(eventId).orElseThrow(() -> eventNotFound(eventId));
            if (eventDocument.getReviewThreshold() == null) {
                workbookDocument.setReviewed(true);
                log.info( "Reviewed workbook " + workbookDocument.getId() );
            }
            workbookRepository.save(workbookDocument);
        }
        return toWorkBookByStatus(workbookDocument);
    }

    private void validateParticipantUpdateStatus(WorkbookDocument workbook, WorkbookStatusUpdateInvoice invoice) {
        // Only coworker or crond can asses workbook
        if (invoice.getStatus() == ASSESSED) {
            throw forbidden();
        }
        // If workbook is already submitted or assessed , participant can't change it status
        if (workbook.getStatus() == SUBMITTED || workbook.getStatus() == ASSESSED) {
            throw forbidden();
        }
        if (invoice.getStatus() == SUBMITTED) {
            // Only approved workbooks can be submitted
            if (workbook.getStatus() != APPROVED) {
                throw forbidden();
            }

            // Check if time to solve test is not expired
            if (isWorkbookExpired(workbook)) {
                throw workbookExpired(workbook.getSubmittableUntil());
            }
        }
    }

    private boolean isWorkbookExpired(WorkbookDocument workbook) {
        return workbook.getSubmittableUntil().isBefore(Instant.now());
    }


    private WorkbookDocument prepareNewWorkbook(UserAuthentication authentication, final WorkbookInvoice invoice) {
        final String accountId = authenticatedAccountId(authentication);
        invoice.validate();
        final String specializationPermalink = invoice.getSpecializationPermalink();
        final Maturity maturity = invoice.getMaturity();
        return WorkbookDocument.builder()
                .textcode(generateTextcode())
                .assignments(prepareNewAssignments(invoice))
                .ownerId(accountId)
                .eventId(invoice.getEventId())
                .createdAt(Instant.now())
                .reviewed(false)
                .specializationPermalink(specializationPermalink)
                .maturity(maturity)
                .submittableUntil(Instant.now().plus(Duration.ofHours(6)))
                .status(WorkbookStatus.APPROVED)
                .build();
    }

    @Override
    public HealthStatus getHealthStatus(Maturity maturity, String specializationPermalink, Set<String> tagIds) {
        HealthStatus status = new HealthStatus();
        status.setMaturity(maturity);
        status.setSpecializationPermalink(specializationPermalink);
        ProblemRandomInvoice problemRandomInvoice = ProblemRandomInvoice.builder()
                .tagIds(tagIds)
                .categories(categoriesBySpecializationPermalink(specializationPermalink))
                .difficulties(difficultiesByMaturity(maturity))
                .build();
        Map<ProblemDifficulty, Integer> difficultiesByMaturity = difficultiesByMaturity(maturity);
        final List<ProblemDocument> problemDocuments = problemRepository.findRandom(mongoTemplate, problemRandomInvoice);
        if (problemDocuments.size() < ProblemRandomInvoice.NUMBER) {
            Map<ProblemDifficulty, Integer> missingDifficulties = new EnumMap<>(ProblemDifficulty.class);
            for (Map.Entry<ProblemDifficulty, Integer> diff: difficultiesByMaturity.entrySet()) {
                int size = (int) problemDocuments
                        .stream()
                        .filter(document -> document.getDifficulty().equals(diff.getKey()))
                        .count();
                if (size < diff.getValue()){
                    missingDifficulties.put(diff.getKey(), diff.getValue() - size);
                }
            }
            status.setMissing(missingDifficulties);
        }
        return status;
    }

    private List<AssignmentDocument> prepareNewAssignments(final WorkbookInvoice invoice) {
        final ProblemRandomInvoice randomInvoice = problemRandomInvoice(invoice);
        final List<ProblemDocument> problemDocuments = problemRepository.findRandom(mongoTemplate, randomInvoice);

        if (problemDocuments.size() < ProblemRandomInvoice.NUMBER) {
            throw wrongProblemsNumber();
        }
        return problemDocuments
                .stream()
                .map(this::assignmentByProblem)
                .collect(Collectors.toList());
    }

    private AssignmentDocument assignmentByProblem(final ProblemDocument problemDocument) {
        return AssignmentDocument.builder()
                .problemId(problemDocument.getId())
                .scoreMax(scoreMaxByDifficulty(problemDocument.getDifficulty()))
                .solution(null)
                .codeSolution(problemDocument.getExpectation() == ProblemExpectation.CODE ? CodeSolutionDocument.builder().build() : null)
                .build();
    }

    private Integer scoreMaxByDifficulty(final ProblemDifficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return 10;
            case MODERATE:
                return 20;
            case HARD:
                return 30;
            case ULTIMATE:
                return 40;
            default:
                return 30;
        }
    }

    private OperationException wrongProblemsNumber() {
        return OperationExceptionBuilder.operationException()
                .description("Wrong problems number")
                .textcode(ERR_PROBLEMS_NUMBER)
                .build();
    }

    @Override
    public ProblemRandomInvoice problemRandomInvoice(final WorkbookInvoice invoice) {
        invoice.validate();
        return ProblemRandomInvoice.builder()
                .tagIds(tagIdsByEventId(invoice.getEventId()))
                .categories(categoriesBySpecializationPermalink(invoice.getSpecializationPermalink()))
                .difficulties(difficultiesByMaturity(invoice.getMaturity()))
                .build();
    }

    public SearchResult<WorkbookInfo> retrieveSearchResult(UserAuthentication authentication, WorkbookSearchInvoice invoice) {
        List<WorkbookDocument> workbookDocuments;
        String eventId = invoice.getFilter().getEventId();
        Sort sort = Sort.by(Sort.Direction.DESC, "assessedAt");
        Pageable pageable = PageRequest.of(invoice.getPageIndex(), invoice.getPageSize(), sort);
        long count;
        if (authentication.getAuthorities().contains(AccountRole.PARTICIPANT)) {
            if (invoice.getFilter().getEventCaption() != null) {
                workbookDocuments = workbookRepository.findByOwnerIdAndEventCaption(mongoTemplate,
                        authentication.getAccountId(),
                        invoice.getFilter().getEventCaption(),
                        pageable);
                count = workbookRepository.countByOwnerIdAndEventCaption(mongoTemplate,
                        authentication.getAccountId(),
                        invoice.getFilter().getEventCaption());
            } else {
                workbookDocuments = workbookRepository.findByOwnerId(authentication.getAccountId(), pageable);
                count = workbookRepository.countByOwnerId(authentication.getAccountId());
            }
        } else {
            if (eventId != null) {
                workbookDocuments = workbookRepository.findByEventIdAndStatus(eventId, ASSESSED, pageable);
                count = workbookRepository.countByEventIdAndStatus(eventId, ASSESSED);
            } else {
                workbookDocuments = workbookRepository.findByStatus(ASSESSED, pageable);
                count = workbookRepository.countByStatus(ASSESSED);
            }
        }
        List<WorkbookInfo> workbooks = workbookDocuments.stream()
                .map(workbookConverter::toDtoShort)
                .map(this::getMoreInfo)
                .collect(Collectors.toList());
        return SearchResult.<WorkbookInfo>builder()
                .items(workbooks)
                .total(count)
                .build();
    }

    @Override
    public Boolean areWorkbooksAvailable(UserAuthentication authentication) {
        if (authentication.getAuthorities().contains(AccountRole.PARTICIPANT)) {
            return workbookRepository.countByOwnerId(authentication.getAccountId()) > 0;
        } else {
            return workbookRepository.countByStatus(ASSESSED) > 0;
        }
    }

    private int compareWorkbooks(WorkbookDocument w1, WorkbookDocument w2) {
        if ((w1.getAssessedAt() != null) && (w2.getAssessedAt() != null)) {
            return w2.getAssessedAt().compareTo(w1.getAssessedAt());
        } else {
            return w2.getCreatedAt().compareTo(w1.getCreatedAt());
        }
    }

    private WorkbookInfo getMoreInfo(Workbook workbook) {
        AccountDocument accountDocument = accountRepository.findById(workbook.getOwnerId())
                .orElseThrow(() -> accountIsMissing(workbook.getOwnerId()));
        EventDocument eventDocument = eventRepository.findById(workbook.getEventId())
                .orElseThrow(() -> eventNotFound(workbook.getEventId()));
        return WorkbookInfo.builder()
                .workbook(workbook)
                .ownerQuickname(accountDocument.getPersonality().getQuickname())
                .eventCaption(eventDocument.getCaption())
                .reviewThreshold(eventDocument.getReviewThreshold())
                .congratulations(eventDocument.getCongratulations())
                .build();
    }

    private Set<ProblemCategory> categoriesBySpecializationPermalink(final String permalink) {
        final SpecializationDocument specializationDocument = specializationRepository.findByPermalink(permalink)
                .orElseThrow(() -> specializationNotFound(permalink));
        return Sets.newHashSet(specializationDocument.getProblemCategories());
    }

    private Set<String> tagIdsByEventId(final String eventId) {
        final EventDocument eventDocument = eventRepository.findById(eventId)
                .orElseThrow(() -> eventNotFound(eventId));
        if (eventDocument.getTagIds() != null) {
            return Sets.newHashSet(eventDocument.getTagIds());
        } else {
            return new HashSet<>();
        }
    }

    private Map<ProblemDifficulty, Integer> difficultiesByMaturity(final Maturity maturity) {
        final Map<ProblemDifficulty, Integer> difficulties = new EnumMap<>(ProblemDifficulty.class);
        switch (maturity) {
            case JUNIOR:
                difficulties.put(ProblemDifficulty.EASY, 3);
                difficulties.put(ProblemDifficulty.MODERATE, 1);
                return difficulties;
            case INTERMEDIATE:
                difficulties.put(ProblemDifficulty.EASY, 1);
                difficulties.put(ProblemDifficulty.MODERATE, 2);
                difficulties.put(ProblemDifficulty.HARD, 1);
                return difficulties;
            case SENIOR:
                difficulties.put(ProblemDifficulty.MODERATE, 1);
                difficulties.put(ProblemDifficulty.HARD, 2);
                difficulties.put(ProblemDifficulty.ULTIMATE, 1);
                return difficulties;
            case EXPERT:
                difficulties.put(ProblemDifficulty.HARD, 1);
                difficulties.put(ProblemDifficulty.ULTIMATE, 3);
                return difficulties;
            default:
                difficulties.put(ProblemDifficulty.MODERATE, 4);
                return difficulties;
        }
    }

    private String authenticatedAccountId(UserAuthentication authentication) {
        return authentication.getAccountId();
    }

    private void send(final WorkbookDocument workbook, final String pathTemplate, UserAuthentication authentication) {
        WorkbookTemplateInvoice workbookTemplateInvoice = WorkbookTemplateInvoice.builder()
                .id(workbook.getId())
                .build();
        final String backlink = templateMailManager.createBacklink(AccountCategory.PARTICIPANT,
                pathTemplate, workbookTemplateInvoice);

        String event = eventRepository.findById(workbook.getEventId())
                .map(EventDocument::getCaption)
                .orElseThrow(() -> eventNotFound(workbook.getEventId()));
        String specialization = specializationRepository.findByPermalink(workbook.getSpecializationPermalink())
                .map(SpecializationDocument::getCaption)
                .orElseThrow(() -> specializationNotFound(workbook.getSpecializationPermalink()));
        String maturityStr = workbook.getMaturity().toString();
        String maturity = maturityStr.charAt(0) + maturityStr.substring(1).toLowerCase();
        long minutes = Duration.between(workbook.getCreatedAt(), workbook.getSubmittableUntil())
                .toMinutes();


        final MailData mailData = MailData.builder()
                .backlink(backlink)
                .name(authentication.getName())
                .supportEmail(templateMailManager.getSupportEmail())
                .logoPath(templateMailManager.createLogoPath(AccountCategory.PARTICIPANT))
                .event(event)
                .solveTime((int) minutes)
                .specialization(specialization)
                .maturity(maturity)
                .build();


        final TemplateMailInvoice templateMailInvoice = TemplateMailInvoice.builder()
                .email(authentication.getAccountEmail())
                .subject("T-Challenge: Новая рабочая тетрадь")
                .templateName("workbook-created")
                .data(mailData)
                .build();
        log.info("Sending email about new workbook " + workbook.getId() + " to "+ authentication.getAccountEmail());
        templateMailManager.sendAsync(templateMailInvoice);
    }

    private String generateTextcode() {
        long serial = 1000 + this.workbookRepository.count() + 1;
        long salt = System.currentTimeMillis() % 100;
        return String.format("%s-%s", serial, salt);
    }


    private OperationException eventNotFound(String id) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_INTERNAL)
                .description("Event not found")
                .attachment(id)
                .build();
    }

    private OperationException accountNotFound(String id) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_INTERNAL)
                .description("Account not found")
                .attachment(id)
                .build();
    }


    private OperationException specializationNotFound(String specializationPermalink) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_INTERNAL)
                .description("Specialization not found")
                .attachment(specializationPermalink)
                .build();
    }

    private OperationException workbookNotFound(String workbookId) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_WORKBOOK)
                .description("Workbook not found")
                .attachment(workbookId)
                .build();
    }


    private OperationException workbookExpired(Instant submittableUntil) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_WORKBOOK_EXPIRED)
                .description("The time to solve test has expired")
                .attachment(submittableUntil)
                .build();
    }

    private OperationException noMoreAttempts() {
        return OperationExceptionBuilder.operationException()
                .description("No more attempts")
                .textcode(ERR_ATTEMPTS_LEFT)
                .build();
    }

    private OperationException accountIsMissing(String id) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_ACC)
                .description("Account is missing")
                .attachment(id)
                .build();
    }


    @Data
    @Builder
    private static final class WorkbookTemplateInvoice {
        private final String id;
    }
}

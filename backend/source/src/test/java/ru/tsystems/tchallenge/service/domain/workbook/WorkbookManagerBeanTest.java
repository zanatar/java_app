package ru.tsystems.tchallenge.service.domain.workbook;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import ru.tsystems.tchallenge.service.domain.account.AccountDocument;
import ru.tsystems.tchallenge.service.domain.account.AccountRepository;
import ru.tsystems.tchallenge.service.domain.account.AccountSystemManager;
import ru.tsystems.tchallenge.service.domain.event.EventDocument;
import ru.tsystems.tchallenge.service.domain.event.EventRepository;
import ru.tsystems.tchallenge.service.domain.event.EventStatus;
import ru.tsystems.tchallenge.service.domain.maturity.Maturity;
import ru.tsystems.tchallenge.service.domain.problem.*;
import ru.tsystems.tchallenge.service.domain.specialization.SpecializationDocument;
import ru.tsystems.tchallenge.service.domain.specialization.SpecializationRepository;
import ru.tsystems.tchallenge.service.domain.statistics.wallboard.WallboardManager;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.AssignmentUpdateInvoice;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.security.authentication.UserAuthentication;
import ru.tsystems.tchallenge.service.utility.mail.TemplateMailManager;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class WorkbookManagerBeanTest {
    private static final String ERR_FORBIDDEN_ACCESS_DENIED = "ERR_FORBIDDEN: Access denied";
    private static final String EXPIRED = "ERR_WORKBOOK_EXPIRED: The time to solve test has expired";
    @Mock
    private SpecializationRepository specializationRepository;
    @Mock
    private WorkbookDocument workbookDocument;
    @Mock
    private ProblemRepository problemRepository;
    @Mock
    private TemplateMailManager templateMailManager;
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private WorkbookRepository workbookRepository;
    @Mock
    private WorkbookConverter workbookConverter;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private WallboardManager wallboardManager;
    @Mock
    private AccountSystemManager accountManager;
    @Mock
    private SimpMessagingTemplate template;
    @Mock
    private AccountRepository accountRepository;
    @InjectMocks
    private WorkbookManagerBean workbookManager;

    private static final String EVENT_ID = UUID.randomUUID().toString();
    private static final String ACCOUNT_ID = UUID.randomUUID().toString();
    private static final String SPEC_PERMALINK = "javadev";
    private static final String WORKBOOK_ID = UUID.randomUUID().toString();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private WorkbookInvoice juniorWorkbookInvoice;
    private List<ProblemCategory> javaDevCategories;
    private ProblemDocument easy;
    private UserAuthentication authentication;
    private ProblemDocument moderate;
    private EventDocument eventDocument;

    @Before
    public void init() {
        juniorWorkbookInvoice = new WorkbookInvoice();
        juniorWorkbookInvoice.setEventId(EVENT_ID);
        juniorWorkbookInvoice.setMaturity(Maturity.JUNIOR);
        juniorWorkbookInvoice.setSpecializationPermalink(SPEC_PERMALINK);
        javaDevCategories = Arrays.asList(ProblemCategory.JAVA, ProblemCategory.OOD);
        SpecializationDocument document = SpecializationDocument.builder()
                .caption("caption")
                .permalink("link")
                .problemCategories(javaDevCategories).build();
        when(specializationRepository.findByPermalink(any())).thenReturn(Optional.of(document));
        easy = ProblemDocument.builder()
                .caption("An easy problem")
                .categories(javaDevCategories)
                .difficulty(ProblemDifficulty.EASY)
                .build();
        authentication = UserAuthentication.builder()
                .accountId(ACCOUNT_ID).build();
        moderate = ProblemDocument.builder()
                .caption("caption")
                .categories(javaDevCategories)
                .difficulty(ProblemDifficulty.MODERATE)
                .build();
        eventDocument = EventDocument.builder()
                .whiteListOnly(false)
                .validFrom(Instant.now())
                .validUntil(Instant.now().plus(2, ChronoUnit.DAYS))
                .status(EventStatus.APPROVED)
                .numberOfAttempts(5)
                .caption("My event")
                .build();
        when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(eventDocument));
        AccountDocument accountDocument = AccountDocument.builder()
                .email("example@example.com")
                .build();
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountDocument));
    }

    @Test
    public void juniorProblemRandomInvoice() {
        ProblemRandomInvoice problemRandomInvoice = workbookManager.problemRandomInvoice(juniorWorkbookInvoice);
        Map<ProblemDifficulty, Integer> expected = new HashMap<>();
        expected.put(ProblemDifficulty.EASY, 3);
        expected.put(ProblemDifficulty.MODERATE, 1);
        assertEquals(expected, problemRandomInvoice.getDifficulties());
    }

    @Test
    public void noMoreAttemptsAllUsed() {
        eventDocument.setNumberOfAttempts(1);
        when(workbookRepository.findByOwnerIdAndEventId(ACCOUNT_ID, EVENT_ID))
                .thenReturn(Collections.singletonList(workbookDocument));
        when(workbookDocument.getReviewed()).thenReturn(false);
        when(workbookDocument.getStatus()).thenReturn(WorkbookStatus.ASSESSED);
        expectedException.expect(OperationException.class);
        expectedException.expectMessage("ERR_ATTEMPTS_LEFT: No more attempts");
        workbookManager.create(authentication, juniorWorkbookInvoice);
        verifyNoMoreInteractions(workbookRepository);
    }

    @Test
    public void noMoreAttemptsAll5Used() {
        eventDocument.setNumberOfAttempts(5);
        when(workbookRepository.findByOwnerIdAndEventId(ACCOUNT_ID, EVENT_ID))
                .thenReturn(Arrays.asList(workbookDocument, workbookDocument, workbookDocument, workbookDocument, workbookDocument));
        when(workbookDocument.getReviewed()).thenReturn(false);
        when(workbookDocument.getStatus()).thenReturn(WorkbookStatus.SUBMITTED);
        when(problemRepository.findRandom(any(), any()))
                .thenReturn(Arrays.asList(easy, easy, easy, moderate));
        expectedException.expect(OperationException.class);
        expectedException.expectMessage("ERR_ATTEMPTS_LEFT: No more attempts");
        workbookManager.create(authentication, juniorWorkbookInvoice);
        verifyNoMoreInteractions(workbookRepository);
    }
/*
    @Test
    public void noMoreAttemptsAlreadyReviewed() {
        eventDocument.setNumberOfAttempts(10);
        when(workbookRepository.findByOwnerIdAndEventId(ACCOUNT_ID, EVENT_ID))
                .thenReturn(Collections.singletonList(workbookDocument));
        when(workbookDocument.getReviewed()).thenReturn(true);
        expectedException.expect(OperationException.class);
        expectedException.expectMessage("ERR_ATTEMPTS_LEFT: No more attempts");
        workbookManager.create(authentication, juniorWorkbookInvoice);
        verifyNoMoreInteractions(workbookRepository);
    }
*/
    @Test
    public void createWorkbookEvenIfReviewedExists() {
        eventDocument.setNumberOfAttempts(10);
        when(workbookRepository.findByOwnerIdAndEventId(ACCOUNT_ID, EVENT_ID))
                .thenReturn(Collections.singletonList(workbookDocument));
        when(workbookDocument.getStatus()).thenReturn(WorkbookStatus.ASSESSED);
        when(workbookDocument.getReviewed()).thenReturn(true);
        when(problemRepository.findRandom(any(), any()))
                .thenReturn(Arrays.asList(easy, easy, easy, moderate));
        workbookManager.create(authentication, juniorWorkbookInvoice);
        verify(workbookRepository).insert(any(WorkbookDocument.class));
        verify(templateMailManager).sendAsync(any());
        verify(wallboardManager).updateStats(EVENT_ID);
    }

    @Test
    public void haveAttempts() {
        eventDocument.setNumberOfAttempts(5);
        when(workbookRepository.findByOwnerIdAndEventId(ACCOUNT_ID, EVENT_ID))
                .thenReturn(Arrays.asList(workbookDocument, workbookDocument, workbookDocument));
        when(workbookDocument.getReviewed()).thenReturn(false);
        when(workbookDocument.getStatus()).thenReturn(WorkbookStatus.SUBMITTED);
        when(problemRepository.findRandom(any(), any()))
                .thenReturn(Arrays.asList(easy, easy, easy, moderate));
        workbookManager.create(authentication, juniorWorkbookInvoice);
        verify(workbookRepository).insert(any(WorkbookDocument.class));
        verify(templateMailManager).sendAsync(any());
        verify(wallboardManager).updateStats(EVENT_ID);
    }

    @Test
    public void haveAttemptsButNotSubmittedWorkbooksExist() {
        eventDocument.setNumberOfAttempts(5);
        when(workbookRepository.findByOwnerIdAndEventId(ACCOUNT_ID, EVENT_ID))
                .thenReturn(Arrays.asList(workbookDocument, workbookDocument, workbookDocument));
        when(workbookDocument.getSubmittableUntil()).thenReturn( Instant.now().plus(Duration.ofHours(2)) );
        when(workbookDocument.getReviewed()).thenReturn(false);
        when(workbookDocument.getStatus()).thenReturn(WorkbookStatus.APPROVED);
        when(problemRepository.findRandom(any(), any()))
                .thenReturn(Arrays.asList(easy, easy, easy, moderate));
        workbookManager.create(authentication, juniorWorkbookInvoice);
        verify(workbookRepository, never()).insert(any(WorkbookDocument.class));
        verify(templateMailManager, never()).sendAsync(any());
        verify(wallboardManager, never()).updateStats(EVENT_ID);
    }

    @Test
    public void haveAttemptsButNotSubmittedWorkbookExists() {
        eventDocument.setNumberOfAttempts(5);
        when(workbookRepository.findByOwnerIdAndEventId(ACCOUNT_ID, EVENT_ID))
                .thenReturn(Arrays.asList(workbookDocument));
        when(workbookDocument.getSubmittableUntil()).thenReturn( Instant.now().minus(Duration.ofHours(3)) );
        when(workbookDocument.getReviewed()).thenReturn(false);
        when(workbookDocument.getStatus()).thenReturn(WorkbookStatus.APPROVED);
        when(problemRepository.findRandom(any(), any()))
                .thenReturn(Arrays.asList(easy, easy, easy, moderate));
        workbookManager.create(authentication, juniorWorkbookInvoice);
        verify(workbookRepository).insert(any(WorkbookDocument.class));
        verify(templateMailManager).sendAsync(any());
        verify(wallboardManager).updateStats(EVENT_ID);
    }

    @Test
    public void notEnoughProblems() {
        //eventDocument.setNumberOfAttempts(1);
        when(workbookRepository.findByOwnerIdAndEventId(ACCOUNT_ID, EVENT_ID))
                .thenReturn( new ArrayList<WorkbookDocument>() );
        when(problemRepository.findRandom(any(), any()))
                .thenReturn(Arrays.asList(easy, easy, moderate));
        when(workbookDocument.getStatus()).thenReturn(WorkbookStatus.ASSESSED);
        expectedException.expect(OperationException.class);
        expectedException.expectMessage("ERR_PROBLEMS_NUMBER: Wrong problems number");
        workbookManager.create(authentication, juniorWorkbookInvoice);
    }

    @Test
    public void checkJuniorDifficulties() {
        ProblemRandomInvoice problemRandomInvoice = workbookManager.problemRandomInvoice(juniorWorkbookInvoice);
        prepareData(problemRandomInvoice);
        workbookManager.create(authentication, juniorWorkbookInvoice);
        ArgumentCaptor<ProblemDifficulty> captor = ArgumentCaptor.forClass(ProblemDifficulty.class);
        verify(problemRepository, times(2))
                .retrieveRandomByCriteria(any(), eq(problemRandomInvoice), captor.capture());
        assertTrue(captor.getAllValues().contains(ProblemDifficulty.EASY));
        assertTrue(captor.getAllValues().contains(ProblemDifficulty.MODERATE));
        assertFalse(captor.getAllValues().contains(ProblemDifficulty.HARD));
        assertFalse(captor.getAllValues().contains(ProblemDifficulty.ULTIMATE));
        List<ProblemDocument> problemDocuments = problemRepository.findRandom(mongoTemplate, problemRandomInvoice);
        assertTrue(problemDocuments.stream().allMatch(p -> p.getDifficulty().equals(ProblemDifficulty.EASY) ||
                p.getDifficulty().equals(ProblemDifficulty.MODERATE)));
    }

    private void prepareData(ProblemRandomInvoice problemRandomInvoice) {
        ProblemDocument hard = ProblemDocument.builder()
                .caption("caption")
                .categories(javaDevCategories)
                .difficulty(ProblemDifficulty.HARD)
                .build();
        ProblemDocument ultimate = ProblemDocument.builder()
                .caption("caption")
                .categories(javaDevCategories)
                .difficulty(ProblemDifficulty.ULTIMATE)
                .build();
        Map<String, ProblemDocument> easyMap = new HashMap<>();
        easyMap.put("id", easy);
        easyMap.put("id2", easy);
        easyMap.put("id3", easy);
        Map<String, ProblemDocument> moderateMap = new HashMap<>();
        moderateMap.put("idm", moderate);
        Map<String, ProblemDocument> hardMap = new HashMap<>();
        hardMap.put("idh", hard);
        Map<String, ProblemDocument> ultimateMap = new HashMap<>();
        ultimateMap.put("idu", ultimate);
        when(problemRepository.findRandom(any(), eq(problemRandomInvoice)))
                .thenCallRealMethod();
        when(problemRepository.retrieveRandomByCriteria(any(), eq(problemRandomInvoice),
                eq(ProblemDifficulty.EASY)))
                .thenReturn(easyMap);
        when(problemRepository.retrieveRandomByCriteria(any(), eq(problemRandomInvoice),
                eq(ProblemDifficulty.MODERATE)))
                .thenReturn(moderateMap);
        when(problemRepository.retrieveRandomByCriteria(any(), eq(problemRandomInvoice),
                eq(ProblemDifficulty.HARD)))
                .thenReturn(hardMap);
        when(problemRepository.retrieveRandomByCriteria(any(), eq(problemRandomInvoice),
                eq(ProblemDifficulty.ULTIMATE)))
                .thenReturn(ultimateMap);
    }

    @Test
    public void canReviewSubmittedWorkbook() {
        when(workbookDocument.getStatus()).thenReturn(WorkbookStatus.ASSESSED);
        when(workbookRepository.findById(any())).thenReturn(Optional.of(workbookDocument));
        workbookManager.reviewWorkbook(WORKBOOK_ID);
        verify(workbookRepository).save(any());
    }

    @Test
    public void cannotReviewNotSubmittedWorkbook() {
        when(workbookDocument.getStatus()).thenReturn(WorkbookStatus.APPROVED);
        when(workbookRepository.findById(any())).thenReturn(Optional.of(workbookDocument));
        expectedException.expect(OperationException.class);
        expectedException.expectMessage(ERR_FORBIDDEN_ACCESS_DENIED);
        workbookManager.reviewWorkbook(WORKBOOK_ID);
    }

    @Test
    public void cannotUpdateAfterDueDate() {
        AssignmentUpdateInvoice invoice = new AssignmentUpdateInvoice();
        invoice.setSolution("42");
        when(workbookRepository.findById(WORKBOOK_ID)).thenReturn(Optional.of(workbookDocument));
        when(workbookDocument.getOwnerId()).thenReturn(ACCOUNT_ID);
        when(workbookDocument.getSubmittableUntil())
                .thenReturn(Instant.now().minus(10, ChronoUnit.MINUTES));
        expectedException.expect(OperationException.class);
        expectedException.expectMessage(EXPIRED);
        workbookManager.updateAssignment(WORKBOOK_ID, 1, invoice);
    }

    @Test
    public void cannotSubmitAfterDueDate() {
        when(workbookRepository.findById(WORKBOOK_ID)).thenReturn(Optional.of(workbookDocument));
        when(workbookDocument.getOwnerId()).thenReturn(ACCOUNT_ID);
        when(workbookDocument.getStatus()).thenReturn(WorkbookStatus.APPROVED);
        when(workbookDocument.getSubmittableUntil())
                .thenReturn(Instant.now().minus(2, ChronoUnit.MINUTES));
        expectedException.expect(OperationException.class);
        expectedException.expectMessage(EXPIRED);
        WorkbookStatusUpdateInvoice statusUpdateInvoice = new WorkbookStatusUpdateInvoice();
        statusUpdateInvoice.setStatus(WorkbookStatus.SUBMITTED);
        workbookManager.updateStatus(WORKBOOK_ID, statusUpdateInvoice);
    }

    @Test
    public void participantCannotAssess() {
        WorkbookStatusUpdateInvoice statusUpdateInvoice = new WorkbookStatusUpdateInvoice();
        statusUpdateInvoice.setStatus(WorkbookStatus.ASSESSED);
        when(workbookRepository.findById(WORKBOOK_ID)).thenReturn(Optional.of(workbookDocument));
        when(workbookDocument.getOwnerId()).thenReturn(ACCOUNT_ID);
        when(workbookDocument.getStatus()).thenReturn(WorkbookStatus.SUBMITTED);
        expectedException.expect(OperationException.class);
        expectedException.expectMessage(ERR_FORBIDDEN_ACCESS_DENIED);
        workbookManager.updateStatus(WORKBOOK_ID, statusUpdateInvoice);
    }
}
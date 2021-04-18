package ru.tsystems.tchallenge.service.domain.problem;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.ProblemCodeExpectationItemsDocument;
import ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.ProblemCodeExpectationItemsInvoice;
import ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.contest.ProblemContestDocument;
import ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.contest.ProblemContestInvoice;
import ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.contest.test.ProblemTestDocument;
import ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.contest.test.ProblemTestInvoice;
import ru.tsystems.tchallenge.service.domain.problem.image.ProblemImage;
import ru.tsystems.tchallenge.service.domain.problem.image.ProblemImageDocument;
import ru.tsystems.tchallenge.service.domain.problem.option.ProblemOptionDocument;
import ru.tsystems.tchallenge.service.domain.problem.option.ProblemOptionInvoice;
import ru.tsystems.tchallenge.service.domain.problem.snippet.ProblemSnippetDocument;
import ru.tsystems.tchallenge.service.domain.problem.snippet.ProblemSnippetInvoice;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.utility.search.SearchResult;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder.forbidden;
import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder.internal;
import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_INTERNAL;


@Component
@RequiredArgsConstructor
@Log4j2
public class ProblemManager {
    private final ProblemConverter problemConverter;
    private final ProblemRepository problemRepository;
    private final MongoTemplate mongoTemplate;

    public Problem create(ProblemInvoice invoice) {
        final ProblemDocument problemDocument = prepareNewProblem(invoice);
        problemRepository.save(problemDocument);
        log.info("Created problem " + problemDocument);
        return problemConverter.toDto(problemDocument);
    }

    public Problem update(String id, ProblemInvoice invoice) {
        ProblemDocument problemDocument = problemRepository.findById(id)
                .orElseThrow(() -> problemNotFound(id));
        if ((problemDocument.getStatus() == ProblemStatus.SUSPENDED) && !onlyStatusChanged(invoice, problemDocument) ||
                (problemDocument.getStatus() == ProblemStatus.DELETED)){
                throw forbidden();
        }
        if ((problemDocument.getStatus() == ProblemStatus.APPROVED) && !onlyStatusChanged(invoice, problemDocument)) {
            invoice.setStatus(ProblemStatus.MODIFIED);
        }
        updateProblem(problemDocument, invoice);
        problemRepository.save(problemDocument);
        log.info("Updated problem " + problemDocument);
        return problemConverter.toDto(problemDocument);
    }

    private boolean onlyStatusChanged(ProblemInvoice invoice, ProblemDocument document) {
        return invoice.getCaption().equals(document.getCaption()) &&
                Objects.equals(invoice.getComplexity(), document.getComplexity()) &&
                (document.getCodeExpectationItems() == null ||
                document.getCodeExpectationItems().equals( prepareNewProblemCodeExpectationItems(invoice.getCodeExpectationItems()) )) &&
                invoice.getExpectation().equals(document.getExpectation()) &&
                Objects.equals(invoice.getIntroduction(), document.getIntroduction()) &&
                invoice.getDifficulty().equals(document.getDifficulty()) &&
                invoice.getQuestion().equals(document.getQuestion()) &&
                document.getCategories().equals(invoice.getCategories()) &&
                ((document.getImages() == null) && (invoice.getImages() == null) ||
                        (document.getImages() != null) && document.getImages().equals(prepareImages(invoice.getImages()))) &&
                document.getOptions().equals(prepareNewOptions(invoice)) &&
                ((document.getSnippets() == null) ||
                document.getSnippets().equals(prepareNewSnippets(invoice))) &&
                (((document.getTagIds() == null) && ((invoice.getTagIds() == null) || invoice.getTagIds().isEmpty())
                        || (document.getTagIds() != null) && document.getTagIds().equals(invoice.getTagIds())));
    }

    public List<Problem> retrieveAll() {
        return problemRepository
                .findAll()
                .stream()
                .map(problemConverter::toDto)

                .collect(Collectors.toList());
    }

    SearchResult<Problem> retrieveByFilter(ProblemSearchInvoice invoice) {
        invoice.validate();
        PageRequest pageRequest = PageRequest.of(invoice.getPageIndex(), invoice.getPageSize());
        String filter = (invoice.getFilterText() != null) ? invoice.getFilterText() : "";
        invoice.setFilterText(filter);
        if ((invoice.getDifficulties() == null) || (invoice.getDifficulties().isEmpty())) {
            invoice.setDifficulties(EnumSet.allOf(ProblemDifficulty.class));
        }
        List<Problem> problems = problemRepository
                .find(mongoTemplate, invoice, pageRequest)
                .stream()
                .map(problemConverter::toDto)
                .collect(Collectors.toList());
        Long total = problemRepository.countByFilter(mongoTemplate, invoice);
        return SearchResult.<Problem>builder()
                .items(problems)
                .total(total)
                .build();
    }

    List<Problem> retrieveRandom(ProblemRandomInvoice invoice) {
        invoice.validate();
        return problemRepository.findRandom(mongoTemplate, invoice)
                .stream()
                .map(problemConverter::toDtoClassified)
                .collect(Collectors.toList());
    }

    private ProblemDocument prepareNewProblem(final ProblemInvoice invoice) {
        invoice.setStatus(ProblemStatus.CREATED);
        return updateProblem(new ProblemDocument(), invoice);
    }

    private ProblemDocument updateProblem(final ProblemDocument problemDocument, final ProblemInvoice invoice) {
        invoice.validate();
        problemDocument.setCategories(invoice.getCategories());
        problemDocument.setComplexity(invoice.getComplexity());
        problemDocument.setDifficulty(invoice.getDifficulty());
        problemDocument.setImages(prepareImages(invoice.getImages()));
        problemDocument.setExpectation(invoice.getExpectation());
        problemDocument.setCaption(invoice.getCaption());
        problemDocument.setIntroduction(invoice.getIntroduction());
        problemDocument.setOptions(ImmutableList.copyOf(prepareNewOptions(invoice)));
        problemDocument.setQuestion(invoice.getQuestion());
        problemDocument.setSnippets(ImmutableList.copyOf(prepareNewSnippets(invoice)));
        problemDocument.setStatus(invoice.getStatus());
        problemDocument.setCodeExpectationItems(prepareNewProblemCodeExpectationItems(invoice.getCodeExpectationItems()));
        problemDocument.setTagIds(invoice.getTagIds());
        return problemDocument;
    }

    private List<ProblemImageDocument> prepareImages(List<ProblemImage> images) {
        return images == null ? new ArrayList<>() : images.stream()
                .map(problemConverter::fromProblemImage)
                .collect(Collectors.toList());
    }

    private List<ProblemOptionDocument> prepareNewOptions(final ProblemInvoice invoice) {
        return invoice.getOptions()
                .stream()
                .map(o -> prepareNewOption(o, invoice.getExpectation() == ProblemExpectation.TEXT))
                .collect(Collectors.toList());
    }

    private ProblemOptionDocument prepareNewOption(final ProblemOptionInvoice invoice, boolean trimContent) {
        return ProblemOptionDocument.builder()
                .content(trimContent ? trimContent(invoice.getContent()) : invoice.getContent())
                .correct(invoice.getCorrect())
                .build();
    }

    private String trimContent(String content) {
        return content.trim()
                .replaceAll("\\s{2,}", " ")
                .toLowerCase();
    }

    private List<ProblemSnippetDocument> prepareNewSnippets(final ProblemInvoice invoice) {
        if (invoice.getSnippets() == null) {
            return new ArrayList<>();
        }
        invoice.validate();
        return invoice.getSnippets()
                .stream()
                .map(this::prepareNewSnippet)
                .collect(Collectors.toList());
    }

    private ProblemSnippetDocument prepareNewSnippet(final ProblemSnippetInvoice invoice) {
        invoice.validate();
        return ProblemSnippetDocument.builder()
                .content(invoice.getContent())
                .style(invoice.getStyle())
                .build();
    }

    private ProblemCodeExpectationItemsDocument prepareNewProblemCodeExpectationItems(final ProblemCodeExpectationItemsInvoice invoice) {
        if(invoice==null) return null;
        return ProblemCodeExpectationItemsDocument.builder()
                .predefinedLang(invoice.getPredefinedLang())
                .predefinedCode(invoice.getPredefinedCode())
                .enableTestsRun(invoice.getEnableTestsRun())
                .contest( prepareNewContest(invoice.getContest()) )
                .build();
    }

    private ProblemContestDocument prepareNewContest( final ProblemContestInvoice invoice ) {
        invoice.validate();
        return ProblemContestDocument.builder()
                .timeLimit(invoice.getTimeLimit())
                .memoryLimit(invoice.getMemoryLimit())
                .tests( prepareNewTests(invoice) )
                .build();
    }
    private List<ProblemTestDocument> prepareNewTests(final ProblemContestInvoice invoice) {
        if (invoice.getTests() == null) {
            return new ArrayList<>();
        }
        return invoice.getTests()
                .stream()
                .map(this::prepareNewTest)
                .collect(Collectors.toList());
    }

    private ProblemTestDocument prepareNewTest( final ProblemTestInvoice invoice ) {
        invoice.validate();
        return ProblemTestDocument.builder()
                .input(invoice.getInput())
                .output(invoice.getOutput())
                .build();
    }

    private OperationException problemNotFound(String id) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_INTERNAL)
                .attachment(id)
                .description("Problem with specified id not found")
                .build();
    }

}

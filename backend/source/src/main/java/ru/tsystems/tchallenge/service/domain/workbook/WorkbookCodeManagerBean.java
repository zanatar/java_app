package ru.tsystems.tchallenge.service.domain.workbook;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ru.tsystems.tchallenge.codemaster.api.LanguagesApi;
import ru.tsystems.tchallenge.codemaster.api.SubmissionsApi;
import ru.tsystems.tchallenge.codemaster.model.*;
import ru.tsystems.tchallenge.service.domain.problem.ProblemDocument;
import ru.tsystems.tchallenge.service.domain.problem.ProblemExpectation;
import ru.tsystems.tchallenge.service.domain.problem.ProblemRepository;
import ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.contest.ProblemContestDocument;
import ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.contest.test.ProblemTestDocument;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.AssignmentDocument;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.code.CodeSolutionDocument;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.code.CodeSolutionInvoice;
import ru.tsystems.tchallenge.service.domain.workbook.scoring.WorkbookScoringService;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder.internal;

@Service
@Log4j2
@RequiredArgsConstructor
public class WorkbookCodeManagerBean implements WorkbookCodeManager {

    private final WorkbookRepository workbookRepository;
    private final WorkbookConverter workbookConverter;
    private final ProblemRepository problemRepository;
    private final SubmissionsApi submissionsApi;
    private final LanguagesApi languagesApi;
    private final WorkbookScoringService workbookScoringService;

    @Override
    public Workbook updateCodeSolution(String workbookId, Integer assignmentIndex, CodeSolutionInvoice invoice) {
        invoice.validate();

        WorkbookDocument workbook = workbookRepository.findById(workbookId)
                .orElseThrow(() -> workbookNotFound(workbookId));
        AssignmentDocument assignmentDocument = workbook.getAssignments().get(assignmentIndex - 1);

        ProblemDocument problem = problemRepository.findById(assignmentDocument.getProblemId())
                .orElseThrow(() -> problemNotFound(assignmentDocument.getProblemId()));
        if (problem.getExpectation() != ProblemExpectation.CODE) {
            throw illegalUpdate();
        }

        CodeSolutionDocument codeSolution = assignmentDocument.getCodeSolution();

        codeSolution.setCode(invoice.getCode());
        codeSolution.setLanguage(invoice.getLanguage());

        // Reset assessment
        assignmentDocument.setScore(null);
        assignmentDocument.getCodeSolution().setSubmissionId(null);

        workbookRepository.save(workbook);
        log.info("Updated workbook " + workbookId + " code assignment " + assignmentIndex);
        return workbookConverter.toClassifiedDto(workbook);
    }

    public SubmissionResult runTests(String workbookId, Integer assignmentIndex) {
        WorkbookDocument workbook = workbookRepository.findById(workbookId)
                .orElseThrow(() -> workbookNotFound(workbookId));
        AssignmentDocument assignmentDocument = workbook.getAssignments().get(assignmentIndex - 1);

        SubmissionResult submissionResult = runTests(assignmentDocument, true);
        assignmentDocument.getCodeSolution().setSubmissionId(submissionResult.getId());
        workbookRepository.save(workbook);
        log.info("Ran tests for workbook " + workbookId + " code assignment " + assignmentIndex);
        return submissionResult;
    }


    public SubmissionResult getTestsResult(String workbookId, Integer assignmentIndex, Boolean lastSubmission, Boolean withSource) {
        WorkbookDocument workbook = workbookRepository.findById(workbookId)
                .orElseThrow(() -> workbookNotFound(workbookId));
        AssignmentDocument assignmentDocument = workbook.getAssignments()
                .get(assignmentIndex - 1);

        CodeSolutionDocument codeSolution = assignmentDocument.getCodeSolution();
        String submissionId = lastSubmission? codeSolution.getSubmissionId() : codeSolution.getLastSuccessfulSubmissionId();
        OperationResultWithSubmissionResult result = submissionsApi.getSubmissionResult(submissionId, true, withSource);
        SubmissionResult submissionResult = result.getContent();
        if (submissionResult == null) {
            throw apiException();
        }

        workbookScoringService.assessCodeSolution(assignmentDocument, submissionResult);
        workbookRepository.save(workbook);
        log.info("Assessed code assignment "+ assignmentIndex + " in workbook " + workbookId);
        return submissionResult;
    }

    @Override
    public void assessAllCodeAssignments( WorkbookDocument workbookDocument ) {
        workbookDocument.getAssignments()
                .stream()
                .filter(a -> a.getScore() == null)
                .filter(a -> a.getCodeSolution() != null && a.getCodeSolution().getCode() != null)
                .filter( a -> a.getCodeSolution().getLanguage() != null )
                .parallel()
                .forEach(a -> runTests(a, false));
        workbookDocument.getAssignments()
                .stream()
                .filter(a -> a.getScore() == null)
                .filter(a -> (a.getCodeSolution() != null) && (a.getCodeSolution().getCode() == null))
                .forEach(a -> a.setScore(BigDecimal.ZERO));
        workbookRepository.save(workbookDocument);
        log.info("Assessed all code assignments in workbook " + workbookDocument.getId());
    }

    public LanguageInfoList availableLanguages() {
        return languagesApi.getAvailableLanguages().getContent();
    }


    private SubmissionResult runTests(AssignmentDocument assignmentDocument, Boolean async) {
        ProblemDocument problem = problemRepository.findById(assignmentDocument.getProblemId())
                .orElseThrow(() -> problemNotFound(assignmentDocument.getProblemId()));

        CodeSolutionDocument solution = assignmentDocument.getCodeSolution();
        ProblemContestDocument contest = problem.getCodeExpectationItems().getContest();

        CompileInvoice compileInvoice = new CompileInvoice()
                .language(solution.getLanguage())
                .sourceCode(solution.getCode());

        Contest c = new Contest()
                .memoryLimit( contest.getMemoryLimit() )
                .timeLimit( contest.getTimeLimit() )
                .tests( contest.getTests()
                        .stream()
                        .map(t-> new TestInvoice().input(t.getInput()).output(t.getOutput()))
                        .collect(Collectors.toCollection(() -> new TestInvoiceList()))
                );
        SubmissionInvoice submission = new SubmissionInvoice()
                .contest(c)
                .submission(compileInvoice);
        OperationResultWithSubmissionResult result = submissionsApi.createSubmission(submission, async);
        if (result.getContent() == null) {
            throw apiException();
        }

        workbookScoringService.assessCodeSolution(assignmentDocument, result.getContent());
        assignmentDocument.getCodeSolution().setSubmissionId(result.getContent().getId());
        return result.getContent();
    }

    private OperationException workbookNotFound(final String id) {
        return OperationExceptionBuilder.operationException()
                .textcode(OperationExceptionType.ERR_WORKBOOK)
                .description("Workbook with specified id not found")
                .attachment(id)
                .build();
    }

    private OperationException problemNotFound(final String id) {
        return OperationExceptionBuilder.operationException()
                .textcode(OperationExceptionType.ERR_INTERNAL)
                .description("Workbook with specified id not found")
                .attachment(id)
                .build();
    }

    private OperationException apiException() {
        return internal("Exception occurred in Codemaster API");
    }

    private OperationException illegalUpdate() {
        return OperationExceptionBuilder.operationException()
                .textcode(OperationExceptionType.ERR_ASSIGNMENT_UPDATE_ILLEGAL)
                .description("Illegal update of assignment")
                .build();
    }
}

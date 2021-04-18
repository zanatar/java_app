package ru.tsystems.tchallenge.service.domain.workbook.scoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import ru.tsystems.tchallenge.codemaster.model.SubmissionResult;
import ru.tsystems.tchallenge.service.domain.problem.ProblemDocument;
import ru.tsystems.tchallenge.service.domain.problem.ProblemRepository;
import ru.tsystems.tchallenge.service.domain.workbook.WorkbookDocument;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.Assignment;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.AssignmentDocument;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static java.math.BigDecimal.ROUND_HALF_EVEN;
import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_INTERNAL;

/**
 * Service that assess all tasks independent of it's difficulty
 */

@Service
@ConditionalOnProperty(prefix = "tchallenge.workbook.scoring", name = "strategy", havingValue = "default")
public class DefaultWorkbookScoringService implements WorkbookScoringService {

    private final ProblemRepository problemRepository;


    @Autowired
    public DefaultWorkbookScoringService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    @Override
    public void assessWorkbook(final WorkbookDocument workbookDocument) {
        final List<AssignmentDocument> assignmentDocuments = workbookDocument.getAssignments();
        for (final AssignmentDocument assignmentDocument : assignmentDocuments) {
            if (assignmentDocument.getScore() != null) {
                continue;
            }
            final String problemId = assignmentDocument.getProblemId();
            ProblemDocument problemDocument = problemRepository.findById(problemId).orElseThrow(
                    () -> problemNotFound(problemId)
            );
            BigDecimal scoreMax = BigDecimal.valueOf(assignmentDocument.getScoreMax());
            BigDecimal score = evalProblemScore(assignmentDocument.getSolution(), problemDocument, scoreMax);
            assignmentDocument.setScore(score);
        }
        workbookDocument.setAvgScore(calculateCorrectSolvedRate(workbookDocument));
    }

    @Override
    public BigDecimal assignmentsAvgScore(List<Assignment> assignments) {
        return assignments
                .stream()
                .filter( w -> (w.getScore() != null) )
                .map(a -> BigDecimal.valueOf(a.getScore()).divide(BigDecimal.valueOf(a.getScoreMax()), 4, ROUND_HALF_EVEN))
                .reduce(BigDecimal::add)
                .map(sum -> sum.divide(BigDecimal.valueOf(assignments.size()), 4, ROUND_HALF_EVEN))
                .orElse(BigDecimal.ZERO);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void assessCodeSolution(AssignmentDocument assignmentDocument, SubmissionResult result) {
        switch (result.getStatus()) {
            case OK:
                assignmentDocument.setScore(BigDecimal.valueOf(assignmentDocument.getScoreMax()));
                assignmentDocument.getCodeSolution().setLastSuccessfulSubmissionId(result.getId());
                break;
            case TIME_LIMIT:
            case MEMORY_LIMIT:
            case WRONG_ANSWER:
            case RUNTIME_ERROR:
            case COMPILATION_ERROR:
                assignmentDocument.setScore(BigDecimal.ZERO);
        }
    }

    @SuppressWarnings("Duplicates")
    private BigDecimal evalProblemScore(final String solution, final ProblemDocument problemDocument, BigDecimal scoreMax) {
        BigDecimal zero = BigDecimal.ZERO;

        switch (problemDocument.getExpectation()) {
            case NUMBER:
            case TEXT:
                return textSolutionMatches(solution, problemDocument.getOptions().get(0).getContent()) ?
                        scoreMax : zero;
            case STRING:
                return Objects.equals(solution, problemDocument.getOptions().get(0).getContent()) ?
                        scoreMax : zero;
            case SINGLE:
                String optionSolution = optionSolution(problemDocument);
                return Objects.equals(optionSolution, solution) ? scoreMax : zero;
            case MULTIPLE:
                optionSolution = optionSolution(problemDocument);
                return evalMultipleOptionScore(solution, optionSolution, scoreMax);
            default:
                return BigDecimal.ZERO;
        }
    }

    private boolean textSolutionMatches(String solution, String answer) {
        if (solution == null) {
            return false;
        }

        return Objects.equals(prepareSolution(solution), prepareSolution(answer));
    }

    private String prepareSolution(String solution) {
        return solution.trim()
                .replaceAll("\\s{2,}", " ")
                .toLowerCase();
    }


    private BigDecimal evalMultipleOptionScore(final String solution, final String answer, BigDecimal scoreMax) {
        return answer.equalsIgnoreCase(solution) ? scoreMax : BigDecimal.ZERO;
    }

    private String optionSolution(final ProblemDocument problemDocument) {
        final StringBuilder result = new StringBuilder();
        problemDocument.getOptions().forEach(o -> result.append(o.getCorrect() ? 1 : 0));
        return result.toString();
    }


    private BigDecimal calculateCorrectSolvedRate(WorkbookDocument workbookDocument) {
        List<AssignmentDocument> assignments = workbookDocument.getAssignments();
        return assignments
                .stream()
                .map(a -> a.getScore().divide(BigDecimal.valueOf(a.getScoreMax()), 2, ROUND_HALF_EVEN))
                .reduce(BigDecimal::add)
                .map(sum -> sum.divide(BigDecimal.valueOf(assignments.size()), 2, ROUND_HALF_EVEN))
                .orElse(BigDecimal.ZERO);

    }



    private OperationException problemNotFound(String id) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_INTERNAL)
                .description("Problem not found")
                .attachment(id)
                .build();
    }

}

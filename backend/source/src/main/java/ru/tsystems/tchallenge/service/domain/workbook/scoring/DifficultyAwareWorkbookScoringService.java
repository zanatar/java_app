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
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

import static java.math.BigDecimal.ROUND_HALF_EVEN;
import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_INTERNAL;

/**
 * Service that assess workbooks and use scoreMax of task
 * So you can solve 2 tasks of 4 and get >50% avgScore
 */

@Service
@ConditionalOnProperty(prefix = "tchallenge.workbook.scoring", name = "strategy", havingValue = "difficulty-aware")
public class DifficultyAwareWorkbookScoringService implements WorkbookScoringService {
    private final ProblemRepository problemRepository;

    @Autowired
    public DifficultyAwareWorkbookScoringService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    @Override
    public void assessWorkbook(final WorkbookDocument workbookDocument) {
        final List<AssignmentDocument> assignmentDocuments = workbookDocument.getAssignments();
        for (final AssignmentDocument assignmentDocument : assignmentDocuments) {
            final String problemId = assignmentDocument.getProblemId();
            ProblemDocument problemDocument = problemRepository.findById(problemId).orElseThrow(
                    () -> problemNotFound(problemId)
            );
            BigDecimal score = evalProblemScore(assignmentDocument.getSolution(), problemDocument,
                    assignmentDocument.getScoreMax());
            assignmentDocument.setScore(score);
        }
        workbookDocument.setAvgScore(calculateCorrectSolvedRate(workbookDocument));
    }


    public BigDecimal assignmentsAvgScore(List<Assignment> assignments) {
        int scoreSum = assignments
                .stream()
                .mapToInt(Assignment::getScore)
                .sum();
        int scoreMaxSum = assignments
                .stream()
                .mapToInt(Assignment::getScoreMax)
                .sum();
        return BigDecimal.valueOf(scoreSum)
                .divide(BigDecimal.valueOf(scoreMaxSum), 4, RoundingMode.HALF_EVEN);
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
    private BigDecimal evalProblemScore(final String solution, final ProblemDocument problemDocument,
                                        Integer assignmentScoreMax) {
        BigDecimal scoreMax = BigDecimal.valueOf(assignmentScoreMax);
        BigDecimal zero = BigDecimal.ZERO;

        switch (problemDocument.getExpectation()) {
            case NUMBER:
            case TEXT:
                return textSolutionMatches(solution, problemDocument.getOptions().get(0).getContent()) ?
                        scoreMax : zero;
            case STRING:
            case CODE:
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

        solution = solution.trim()
                .replaceAll("\\s{2,}", " ")
                .toLowerCase();

        answer = answer.trim()
                .replace("\\{2,}", "")
                .toLowerCase();

        return solution.equals(answer);
    }


    private BigDecimal evalMultipleOptionScore(final String solution, final String answer, final BigDecimal scoreMax) {
        if (solution == null) {
            return BigDecimal.ZERO;
        }

        int scoreBalance = 0;
        int checkedAnswers = 0;
        for (int i = 0; i < solution.length(); i++) {

            if (answer.charAt(i) == '1') {
                checkedAnswers++;
                // For correctly chosen options, add a score
                if (solution.charAt(i) == answer.charAt(i)) {
                    scoreBalance++;
                    // For mistakenly selected options, we reduce the score (not lower than 0)
                }
            } else if (solution.charAt(i) != answer.charAt(i)) {
                scoreBalance--;
            }

        }

        return BigDecimal.valueOf(scoreBalance)
                .max(BigDecimal.ZERO)
                .divide(BigDecimal.valueOf(checkedAnswers), 2, RoundingMode.HALF_EVEN)
                .multiply(scoreMax);
    }

    private String optionSolution(final ProblemDocument problemDocument) {
        final StringBuilder result = new StringBuilder();
        problemDocument.getOptions().forEach(o -> result.append(o.getCorrect() ? 1 : 0));
        return result.toString();
    }


    private BigDecimal calculateCorrectSolvedRate(WorkbookDocument workbookDocument) {

        BigDecimal scoreSum = workbookDocument.getAssignments()
                .stream()
                .map(AssignmentDocument::getScore)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

        int scoreMaxSum = workbookDocument.getAssignments()
                .stream()
                .mapToInt(AssignmentDocument::getScoreMax)
                .sum();

        // sum of score divided by sum of scoreMax
        return scoreSum
                .divide(BigDecimal.valueOf(scoreMaxSum), 2, ROUND_HALF_EVEN);
    }


    private OperationException problemNotFound(String id) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_INTERNAL)
                .description("Problem not found")
                .attachment(id)
                .build();
    }
}

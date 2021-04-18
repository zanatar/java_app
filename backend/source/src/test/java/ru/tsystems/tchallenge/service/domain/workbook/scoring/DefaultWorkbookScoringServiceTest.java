package ru.tsystems.tchallenge.service.domain.workbook.scoring;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import ru.tsystems.tchallenge.service.domain.problem.*;
import ru.tsystems.tchallenge.service.domain.problem.option.ProblemOptionDocument;
import ru.tsystems.tchallenge.service.domain.workbook.WorkbookDocument;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.Assignment;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.AssignmentDocument;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class DefaultWorkbookScoringServiceTest {
    @Mock
    ProblemRepository problemRepository;

    @InjectMocks
    DefaultWorkbookScoringService scoringService;

    private List<Assignment> assignments;

    private static final String EASY_PROBLEM_ID = UUID.randomUUID().toString();
    private static final String HARD_PROBLEM_ID = UUID.randomUUID().toString();
    private static final String MODERATE_PROBLEM_ID = UUID.randomUUID().toString();

    private WorkbookDocument workbookDocument;

    @Before
    public void setUp() {
        prepareAssignments();
        prepareWorkbookDocument();
    }

    private void prepareAssignments() {
        Problem easy = new Problem();
        easy.setDifficulty(ProblemDifficulty.EASY);
        Problem moderate = new Problem();
        moderate.setDifficulty(ProblemDifficulty.MODERATE);
        List<Problem> problems = Arrays.asList(easy, easy, moderate, easy);
        assignments = problems
                .stream()
                .map(problem -> {
                    Assignment assignment = new Assignment();
                    assignment.setProblem(problem);
                    assignment.setScoreMax(problem.getDifficulty() == ProblemDifficulty.EASY ? 10 : 20);
                    return assignment;
                })
                .collect(Collectors.toList());
    }

    private void prepareWorkbookDocument() {
        ProblemOptionDocument textOption = ProblemOptionDocument
                .builder()
                .content("answer")
                .correct(true)
                .build();
        ProblemDocument easy = ProblemDocument.builder()
                .difficulty(ProblemDifficulty.EASY)
                .expectation(ProblemExpectation.TEXT)
                .options(Collections.singletonList(textOption))
                .build();
        ProblemDocument moderate = ProblemDocument.builder()
                .difficulty(ProblemDifficulty.MODERATE)
                .expectation(ProblemExpectation.TEXT)
                .options(Collections.singletonList(textOption))
                .build();
        List<ProblemOptionDocument> multipleOptions = Arrays.asList(
                ProblemOptionDocument
                        .builder()
                        .content("answer")
                        .correct(true)
                        .build(),
                ProblemOptionDocument
                        .builder()
                        .content("answer 2")
                        .correct(false)
                        .build(),
                ProblemOptionDocument
                        .builder()
                        .content("answer 3")
                        .correct(true)
                        .build());
        ProblemDocument hard = ProblemDocument.builder()
                .difficulty(ProblemDifficulty.HARD)
                .expectation(ProblemExpectation.MULTIPLE)
                .options(multipleOptions)
                .build();
        when(problemRepository.findById(EASY_PROBLEM_ID)).thenReturn(Optional.of(easy));
        when(problemRepository.findById(MODERATE_PROBLEM_ID)).thenReturn(Optional.of(moderate));
        when(problemRepository.findById(HARD_PROBLEM_ID)).thenReturn(Optional.of(hard));
        List<AssignmentDocument> assignmentDocuments = Arrays.asList(
                AssignmentDocument.builder()
                        .problemId(EASY_PROBLEM_ID)
                        .solution("answer")
                        .scoreMax(10)
                        .build(),
                AssignmentDocument.builder()
                        .problemId(MODERATE_PROBLEM_ID)
                        .solution("answer")
                        .scoreMax(20)
                        .build(),
                AssignmentDocument.builder()
                        .problemId(MODERATE_PROBLEM_ID)
                        .solution("answer")
                        .scoreMax(20)
                        .build(),
                AssignmentDocument.builder()
                        .problemId(HARD_PROBLEM_ID)
                        .solution("101")
                        .scoreMax(30)
                        .build());
        workbookDocument = WorkbookDocument.builder()
                .assignments(assignmentDocuments)
                .build();
    }

    @Test
    public void assessWorkbookZeroSolved() {
        workbookDocument.getAssignments()
                .forEach(assignmentDocument -> assignmentDocument.setSolution(null));
        scoringService.assessWorkbook(workbookDocument);
        BigDecimal actualScore = workbookDocument.getAvgScore();
        BigDecimal expectedScore = BigDecimal.valueOf(0);
        assertEquals(0, actualScore.compareTo(expectedScore));
    }

    @Test
    public void assessWorkbookOneSolved() {
        workbookDocument.getAssignments()
                .stream()
                .limit(3)
                .forEach(assignmentDocument -> assignmentDocument.setSolution(null));
        scoringService.assessWorkbook(workbookDocument);
        BigDecimal actualScore = workbookDocument.getAvgScore();
        BigDecimal expectedScore = BigDecimal.valueOf(0.25);
        assertEquals(0, actualScore.compareTo(expectedScore));
    }

    @Test
    public void assessWorkbookThreeSolvedOnePartiallySolved() {
        AssignmentDocument multipleOptionsAssignment = workbookDocument.getAssignments().get(3);
        multipleOptionsAssignment.setSolution("001");
        scoringService.assessWorkbook(workbookDocument);
        BigDecimal actualScore = workbookDocument.getAvgScore();
        BigDecimal expectedScore = BigDecimal.valueOf(0.75);
        assertEquals(0, actualScore.compareTo(expectedScore));
    }

    @Test
    public void assessWorkbookAllSolved() {
        scoringService.assessWorkbook(workbookDocument);
        BigDecimal actualScore = workbookDocument.getAvgScore();
        BigDecimal expectedScore = BigDecimal.valueOf(1);
        assertEquals(0, actualScore.compareTo(expectedScore));
    }

    @Test
    public void assignmentsAvgScoreZeroSolved() {
        assignments.forEach(assignment -> assignment.setScore(0));
        BigDecimal avgScore = scoringService.assignmentsAvgScore(assignments);
        BigDecimal expectedScore = BigDecimal.valueOf(0);
        assertEquals(0, expectedScore.compareTo(avgScore));
    }

    @Test
    public void assignmentsAvgScoreThreeSolved() {
        assignments.forEach(assignment -> assignment.setScore(assignment.getScoreMax()));
        assignments.get(0).setScore(0);
        BigDecimal avgScore = scoringService.assignmentsAvgScore(assignments);
        BigDecimal expectedScore = BigDecimal.valueOf(0.75);
        assertEquals(0, expectedScore.compareTo(avgScore));
    }


    @Test
    public void assignmentsAvgScoreAllSolved() {
        assignments.forEach(assignment -> assignment.setScore(assignment.getScoreMax()));
        BigDecimal avgScore = scoringService.assignmentsAvgScore(assignments);
        BigDecimal expectedScore = BigDecimal.valueOf(1);
        assertEquals(0, expectedScore.compareTo(avgScore));
    }

}

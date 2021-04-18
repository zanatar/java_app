package ru.tsystems.tchallenge.service.domain.statistics.user;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import ru.tsystems.tchallenge.service.domain.event.EventDocument;
import ru.tsystems.tchallenge.service.domain.event.EventRepository;
import ru.tsystems.tchallenge.service.domain.problem.Problem;
import ru.tsystems.tchallenge.service.domain.problem.ProblemCategory;
import ru.tsystems.tchallenge.service.domain.problem.ProblemRepository;
import ru.tsystems.tchallenge.service.domain.specialization.SpecializationDocument;
import ru.tsystems.tchallenge.service.domain.specialization.SpecializationRepository;
import ru.tsystems.tchallenge.service.domain.workbook.Workbook;
import ru.tsystems.tchallenge.service.domain.workbook.WorkbookConverter;
import ru.tsystems.tchallenge.service.domain.workbook.WorkbookRepository;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.Assignment;
import ru.tsystems.tchallenge.service.domain.workbook.scoring.DefaultWorkbookScoringService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static ru.tsystems.tchallenge.service.domain.problem.ProblemCategory.JAVA;
import static ru.tsystems.tchallenge.service.domain.problem.ProblemCategory.OOD;
import static ru.tsystems.tchallenge.service.domain.workbook.WorkbookStatus.ASSESSED;
import static ru.tsystems.tchallenge.service.domain.workbook.WorkbookStatus.SUBMITTED;

@RunWith(SpringRunner.class)
public class UserStatisticsManagerTest {
    @InjectMocks
    private UserStatisticsManager userStatisticsManager;
    @MockBean
    private WorkbookRepository workbookRepository;
    @MockBean
    private WorkbookConverter workbookConverter;
    private List<Workbook> testData;
    @MockBean
    private SpecializationRepository specializationRepository;
    @MockBean
    private EventRepository eventRepository;
    @Mock
    private ProblemRepository problemRepository;


    @Before
    public void init() {
        userStatisticsManager = new UserStatisticsManager(workbookRepository, workbookConverter,
                specializationRepository, eventRepository, new DefaultWorkbookScoringService(problemRepository));
    }

    private GeneralUserStatistics prepareData() {
        Instant firstAssign = Instant.now().minus(3, DAYS);
        Instant lastAssign = Instant.now();

        EventDocument joker = EventDocument.builder()

                .caption("joker 2018")
                .build();

        SpecializationDocument javadev = SpecializationDocument.builder()
                .caption("java dev")
                .build();

        when(eventRepository.findById(any())).thenReturn(Optional.of(joker));
        when(specializationRepository.findByPermalink(any())).thenReturn(Optional.of(javadev));

        final String specPermalink = "javaDevId";
        final String eventId = "joker2018Id";

        Workbook workbook = new Workbook();
        workbook.setStatus(ASSESSED);
        workbook.setCreatedAt(firstAssign);
        workbook.setSpecializationPermalink(specPermalink);
        workbook.setEventId(eventId);

        Assignment assignment = new Assignment();
        Problem problem = new Problem();
        problem.setCategories(Collections.singletonList(JAVA));
        assignment.setProblem(problem);
        assignment.setScore(12);
        assignment.setScoreMax(20);

        Assignment assignment2 = new Assignment();
        Problem problem2 = new Problem();
        problem2.setCategories(Arrays.asList(JAVA, OOD));
        assignment2.setProblem(problem2);
        assignment2.setScore(5);
        assignment2.setScoreMax(15);

        workbook.setAssignments(Arrays.asList(
                assignment, assignment2
        ));

        workbook.setAvgScore(BigDecimal.valueOf(0.47));

        Workbook workbook2 = new Workbook();
        workbook2.setStatus(ASSESSED);
        workbook2.setCreatedAt(lastAssign);
        workbook2.setSpecializationPermalink(specPermalink);
        workbook2.setEventId(eventId);


        Assignment assignment3 = new Assignment();
        Problem problem3 = new Problem();
        problem3.setCategories(Collections.singletonList(JAVA));
        assignment3.setProblem(problem3);
        assignment3.setScore(6);
        assignment3.setScoreMax(20);

        workbook2.setAssignments(Collections.singletonList(assignment3));
        workbook2.setAvgScore(BigDecimal.valueOf(0.3));
        testData = Arrays.asList(workbook, workbook2);

        Map<ProblemCategory, CategoryStatistics> problemCategoryStatistics = new HashMap<>();
        problemCategoryStatistics.put(JAVA, CategoryStatistics.builder()
                .number(3)
                .avgScore(BigDecimal.valueOf(0.41))
                .build());
        problemCategoryStatistics.put(OOD, CategoryStatistics.builder()
                .number(1)
                .avgScore(BigDecimal.valueOf(0.33))
                .build());

        List<WorkbookScore> workbooksScores = new ArrayList<>();
        workbooksScores.add(new WorkbookScore(firstAssign, BigDecimal.valueOf(0.47)));
        workbooksScores.add(new WorkbookScore(lastAssign, new BigDecimal("0.30")));

        BigDecimal avgScore = BigDecimal.valueOf(0.38);

        Map<String, CategoryStatistics> specsStatistics = new HashMap<>();
        specsStatistics.put("java dev",
                CategoryStatistics.builder()
                        .number(2)
                        .avgScore(avgScore)
                        .build());
        Map<String, CategoryStatistics> eventStatistics = new HashMap<>();
        eventStatistics.put("joker 2018",
                CategoryStatistics.builder()
                        .number(2)
                        .avgScore(avgScore)
                        .build());

        return GeneralUserStatistics.builder()
                .totalProblemsSolved(3)
                .firstAssign(firstAssign)
                .lastAssign(lastAssign)
                .totalWorkbooks(2)
                .solvedWorkbooks(2)
                // W2 score 6/20
                // avg of W1 + W2
                .avgScore(avgScore)
                .workbooksScores(workbooksScores)
                .problemCategoryStatistics(problemCategoryStatistics)
                .specializationsStatistics(specsStatistics)
                .eventsStatistics(eventStatistics)
                .build();
    }

    @Test
    public void test() {
        GeneralUserStatistics expected = prepareData();
        GeneralUserStatistics actual = userStatisticsManager.retrieveGeneralStat(testData);
        Assert.assertEquals(expected, actual);
    }
}

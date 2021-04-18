package ru.tsystems.tchallenge.service.domain.statistics.user;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tsystems.tchallenge.service.domain.event.EventDocument;
import ru.tsystems.tchallenge.service.domain.event.EventRepository;
import ru.tsystems.tchallenge.service.domain.problem.ProblemCategory;
import ru.tsystems.tchallenge.service.domain.specialization.SpecializationDocument;
import ru.tsystems.tchallenge.service.domain.specialization.SpecializationRepository;
import ru.tsystems.tchallenge.service.domain.workbook.Workbook;
import ru.tsystems.tchallenge.service.domain.workbook.WorkbookConverter;
import ru.tsystems.tchallenge.service.domain.workbook.WorkbookRepository;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.Assignment;
import ru.tsystems.tchallenge.service.domain.workbook.scoring.WorkbookScoringService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.RoundingMode.HALF_EVEN;
import static ru.tsystems.tchallenge.service.domain.event.EventManager.eventNotFound;
import static ru.tsystems.tchallenge.service.domain.workbook.WorkbookStatus.ASSESSED;
import static ru.tsystems.tchallenge.service.domain.workbook.WorkbookStatus.SUBMITTED;

@Service
public class UserStatisticsManager {

    private final WorkbookRepository workbookRepository;
    private final WorkbookConverter workbookConverter;
    private final SpecializationRepository specializationRepository;
    private final EventRepository eventRepository;
    private final WorkbookScoringService scoringService;

    @Autowired
    public UserStatisticsManager(WorkbookRepository workbookRepository, WorkbookConverter workbookConverter,
                                 SpecializationRepository specializationRepository, EventRepository eventRepository,
                                 WorkbookScoringService scoringService) {
        this.workbookRepository = workbookRepository;
        this.workbookConverter = workbookConverter;
        this.specializationRepository = specializationRepository;
        this.eventRepository = eventRepository;
        this.scoringService = scoringService;
    }

    public boolean hasStatistics(String id) {
        return workbookRepository.countByOwnerIdAndStatusIn(id, EnumSet.of(SUBMITTED, ASSESSED)) > 1;
    }


    public GeneralUserStatistics retrieveGeneralStat(String id) {
        List<Workbook> workbooks = workbookRepository.findByOwnerId(id)
                .stream()
                .map(workbookConverter::toDto)
                .collect(Collectors.toList());
        return retrieveGeneralStat(workbooks);

    }

    public GeneralUserStatistics retrieveGeneralStat(List<Workbook> workbooks) {

        List<Workbook> solvedWorkbooks = workbooks.stream()
                .filter(w -> w.getStatus() == ASSESSED )
                .collect(Collectors.toList());
        long solvedWorkbooksCount = solvedWorkbooks.size();

        List<WorkbookScore> workbooksScores = solvedWorkbooks.stream()
                .map(w -> new WorkbookScore(w.getCreatedAt(), workbookAvgScore(w).setScale(2, HALF_EVEN)))
                .sorted(Comparator.comparing(WorkbookScore::getCreatedAt))
                .collect(Collectors.toList());


        BigDecimal avgScore = solvedWorkbooks.stream()
                .map(this::workbookAvgScore)
                .reduce(BigDecimal::add)
                .map(r -> r.divide(BigDecimal.valueOf(solvedWorkbooks.size()), 2, HALF_EVEN))
                .orElse(BigDecimal.ZERO);

        long totalProblemsSolved = solvedWorkbooks.stream()
                .filter(w -> w.getStatus() == ASSESSED)
                .map(Workbook::getAssignments)
                .mapToLong(List::size)
                .sum();

        Instant firstAssign = solvedWorkbooks.stream()
                .map(Workbook::getCreatedAt)
                .min(Comparator.naturalOrder())
                .orElse(null);

        Instant lastAssign = solvedWorkbooks.stream()
                .map(Workbook::getCreatedAt)
                .max(Comparator.naturalOrder())
                .orElse(null);


        Map<ProblemCategory, CategoryStatistics> problemCategoryStatistics = calcProblemCategoryStat(solvedWorkbooks);
        Map<String, CategoryStatistics> specializationCategoryStatistics = calcSpecCategoryStat(solvedWorkbooks);
        Map<String, CategoryStatistics> eventCategoryStatistics = calcEventCategoryStat(solvedWorkbooks);

        return GeneralUserStatistics.builder()
                .totalWorkbooks(workbooks.size())
                .solvedWorkbooks((int) solvedWorkbooksCount)
                .workbooksScores(workbooksScores)
                .avgScore(avgScore)
                .totalProblemsSolved((int) totalProblemsSolved)
                .firstAssign(firstAssign)
                .lastAssign(lastAssign)
                .problemCategoryStatistics(problemCategoryStatistics)
                .specializationsStatistics(specializationCategoryStatistics)
                .eventsStatistics(eventCategoryStatistics)
                .build();
    }

    private BigDecimal workbookAvgScore(Workbook workbook) {
        return workbook.getAvgScore();
    }

    private BigDecimal assignmentsAvgScore(List<Assignment> assignments) {
        return scoringService.assignmentsAvgScore(assignments);
    }

    private Map<ProblemCategory, CategoryStatistics> calcProblemCategoryStat(List<Workbook> workbooks) {
        Map<ProblemCategory, List<Assignment>> assignmentsByCategory = assignmentsByCategory(workbooks);


        Map<ProblemCategory, CategoryStatistics> problemCategoryStatistics = new HashMap<>();
        for (ProblemCategory category : assignmentsByCategory.keySet()) {
            List<Assignment> categoryAssignments = assignmentsByCategory.get(category);
            BigDecimal avgScore = assignmentsAvgScore(categoryAssignments).setScale(2, HALF_EVEN);

            CategoryStatistics categoryStatistics = CategoryStatistics.builder()
                    .avgScore(avgScore)
                    .number(categoryAssignments.size())
                    .build();
            problemCategoryStatistics.put(category, categoryStatistics);
        }
        return problemCategoryStatistics;
    }


    private Map<ProblemCategory, List<Assignment>> assignmentsByCategory(List<Workbook> workbooks) {
        List<Assignment> assignments = workbooks.stream()
                .map(Workbook::getAssignments)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        Map<ProblemCategory, List<Assignment>> assignmentByProblemCategory = new HashMap<>();
        for (Assignment assignment : assignments) {
            assignment.getProblem()
                    .getCategories()
                    .forEach(category -> {
                        assignmentByProblemCategory.computeIfPresent(category, (c, list) -> {
                            list.add(assignment);
                            return list;
                        });
                        assignmentByProblemCategory.putIfAbsent(category, Lists.newArrayList(assignment));
                    });
        }
        return assignmentByProblemCategory;
    }


    private CategoryStatistics categoryStatistics(Map<String, List<Workbook>> workbooksById, String id) {
        List<Workbook> workbookList = workbooksById.get(id);
        BigDecimal avgScore = workbookList.stream()
                .map(this::workbookAvgScore)
                .reduce(BigDecimal::add)
                .map(v -> v.divide(BigDecimal.valueOf(workbookList.size()), 2, HALF_EVEN))
                .orElse(BigDecimal.ZERO);

        return CategoryStatistics.builder()
                .avgScore(avgScore)
                .number(workbookList.size())
                .build();
    }


    private Map<String, CategoryStatistics> calcSpecCategoryStat(List<Workbook> workbooks) {
        Map<String, List<Workbook>> workbooksBySpec = workbooks.stream()
                .collect(Collectors.groupingBy(Workbook::getSpecializationPermalink));

        Map<String, CategoryStatistics> specializationCategoryStatistics = new HashMap<>();
        for (String specPermalink : workbooksBySpec.keySet()) {
            Optional<SpecializationDocument> optionalSpecializationDocument = specializationRepository.findByPermalink(specPermalink);
            if(optionalSpecializationDocument.isPresent()) {
                SpecializationDocument specializationDocument = optionalSpecializationDocument.get();
                CategoryStatistics categoryStatistics = categoryStatistics(workbooksBySpec, specPermalink);
                specializationCategoryStatistics.put(specializationDocument.getCaption(), categoryStatistics);
            }
        }
        return specializationCategoryStatistics;
    }


    private Map<String, CategoryStatistics> calcEventCategoryStat(List<Workbook> workbooks) {
        Map<String, List<Workbook>> workbooksByEventId = workbooks.stream()
                .collect(Collectors.groupingBy(Workbook::getEventId));

        Map<String, CategoryStatistics> specializationCategoryStatistics = new HashMap<>();
        for (String eventId : workbooksByEventId.keySet()) {
            EventDocument eventDocument = eventRepository.findById(eventId).orElseThrow(() -> eventNotFound(eventId));
            CategoryStatistics categoryStatistics = categoryStatistics(workbooksByEventId, eventId);
            specializationCategoryStatistics.put(eventDocument.getCaption(), categoryStatistics);
        }
        return specializationCategoryStatistics;
    }

}

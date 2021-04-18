package ru.tsystems.tchallenge.service.domain.statistics.user;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Value;
import ru.tsystems.tchallenge.service.domain.problem.ProblemCategory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Value
@Builder
@ApiModel(description = "Statistics of user for all solved workbooks. Contains average score, " +
        "history of solved workbooks, distribution of solved problem categories, etc.")
public class GeneralUserStatistics {
    int totalWorkbooks;
    int solvedWorkbooks;
    List<WorkbookScore> workbooksScores;
    BigDecimal avgScore;
    int totalProblemsSolved;
    Instant firstAssign;
    Instant lastAssign;
    Map<ProblemCategory, CategoryStatistics> problemCategoryStatistics;
    Map<String, CategoryStatistics> eventsStatistics;
    Map<String, CategoryStatistics> specializationsStatistics;
}

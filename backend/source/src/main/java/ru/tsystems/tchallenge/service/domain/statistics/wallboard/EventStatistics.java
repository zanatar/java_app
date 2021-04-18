package ru.tsystems.tchallenge.service.domain.statistics.wallboard;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Value;
import ru.tsystems.tchallenge.service.domain.statistics.UserStatistics;

import java.util.List;
import java.util.Map;

@Value
@Builder
@ApiModel(description = "Wallboard data. Contains participants amount, " +
        "participants amount grouped by the number of correct answers, number of tests in progress, etc.")
public class EventStatistics {
    int participants;
    Map<Integer, Long> answersStatistics;
    List<UserStatistics> ratingList;
    long workbooksInProgress;
}

package ru.tsystems.tchallenge.service.domain.statistics;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StatisticsResult {
    private final Long total;

    private final List<UserStatistics> statistics;
}

package ru.tsystems.tchallenge.service.domain.statistics.user;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class CategoryStatistics {
    BigDecimal avgScore;
    Integer number;
}

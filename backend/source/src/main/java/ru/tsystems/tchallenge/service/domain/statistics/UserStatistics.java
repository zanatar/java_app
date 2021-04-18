package ru.tsystems.tchallenge.service.domain.statistics;

import lombok.Builder;
import lombok.Data;
import ru.tsystems.tchallenge.service.domain.account.AccountPersonality;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class UserStatistics {

    private final AccountPersonality accountPersonality;

    private final Instant assessedAt;
    // If participant rank is between 2 to 5, minRank will be 2
    private Integer minRank;
    // If participant rank is between 2 to 5, maxRank will be 5
    private Integer maxRank;

    private final BigDecimal correctlySolvedRate;

}

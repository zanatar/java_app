package ru.tsystems.tchallenge.service.domain.statistics;

import lombok.Builder;
import lombok.Data;
import ru.tsystems.tchallenge.service.domain.account.AccountPersonality;

import java.math.BigDecimal;

@Data
@Builder
public class PrizeWinner {
    private String id;
    private String email;
    private AccountPersonality personality;
    private BigDecimal score;
}

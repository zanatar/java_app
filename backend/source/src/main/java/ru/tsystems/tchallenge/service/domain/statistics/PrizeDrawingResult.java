package ru.tsystems.tchallenge.service.domain.statistics;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PrizeDrawingResult {
    Integer number;
    List<PrizeWinner> winners;
}

package ru.tsystems.tchallenge.service.domain.statistics.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkbookScore {
    private Instant createdAt;
    private BigDecimal score;
}

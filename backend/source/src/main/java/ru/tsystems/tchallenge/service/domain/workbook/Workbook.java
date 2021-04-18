package ru.tsystems.tchallenge.service.domain.workbook;

import lombok.Data;
import ru.tsystems.tchallenge.service.domain.maturity.Maturity;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.Assignment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public final class Workbook {

    private String id;
    private String textcode;
    private List<Assignment> assignments;
    private String eventId;
    private String specializationPermalink;
    private String ownerId;
    private Maturity maturity;
    private Instant createdAt;
    private Instant submittableUntil;
    private Instant assessedAt;
    private WorkbookStatus status;
    private Boolean reviewed;
    private BigDecimal avgScore;
    private String coworkerLink;
}

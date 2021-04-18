package ru.tsystems.tchallenge.service.domain.workbook;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.tsystems.tchallenge.service.domain.maturity.Maturity;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.AssignmentDocument;
import ru.tsystems.tchallenge.service.utility.data.AbstractDocument;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Document(collection = "workbooks")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class WorkbookDocument extends AbstractDocument {
    private final String textcode;
    private final List<AssignmentDocument> assignments;
    private final String eventId;
    private final String specializationPermalink;
    private final String ownerId;
    private final Instant submittableUntil;
    private Maturity maturity;
    private WorkbookStatus status;
    private Boolean reviewed;
    private final Instant createdAt;
    private BigDecimal avgScore;
    private Instant assessedAt;
}

package ru.tsystems.tchallenge.service.domain.workbook;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class WorkbookFilter {
    private final String eventId;
    private final String eventCaption;
    private final Set<WorkbookStatus> statuses;
}

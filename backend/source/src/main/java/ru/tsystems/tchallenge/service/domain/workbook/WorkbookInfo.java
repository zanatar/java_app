package ru.tsystems.tchallenge.service.domain.workbook;

import lombok.Builder;
import lombok.Data;
import ru.tsystems.tchallenge.service.domain.event.EventCongratulationMessage;

import java.util.List;

@Data
@Builder
public class WorkbookInfo {
    private Workbook workbook;
    private String ownerQuickname;
    private String eventCaption;
    private Integer reviewThreshold;
    private List<EventCongratulationMessage> congratulations;
}

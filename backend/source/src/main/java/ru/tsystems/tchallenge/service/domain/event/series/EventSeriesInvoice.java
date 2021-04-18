package ru.tsystems.tchallenge.service.domain.event.series;

import com.google.common.base.Strings;
import lombok.Data;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

import java.util.List;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_CAPTION;
import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_EVENT_IDS_MISSING;

@Data
public class EventSeriesInvoice implements ValidationAware {
    private List<String> eventIds;
    private String caption;
    private String description;

    @Override
    public void registerViolations() {
        if (Strings.isNullOrEmpty(caption)) {
            throw missingCaption();
        }
        if ((eventIds == null) || (eventIds.isEmpty())) {
            throw missingEvents();
        }
    }

    private OperationException missingCaption() {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_CAPTION)
                .description("Caption should be specified")
                .build();
    }

    private OperationException missingEvents() {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_EVENT_IDS_MISSING)
                .description("Event ids should be specified")
                .build();
    }
}

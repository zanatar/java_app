package ru.tsystems.tchallenge.service.domain.event;

import com.google.common.base.Strings;
import lombok.Data;
import ru.tsystems.tchallenge.service.domain.maturity.Maturity;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.*;


@Data
public class EventManagementInvoice implements ValidationAware {
    private String description;
    private String greeting;
    private Instant validFrom;
    private Instant validUntil;
    private List<String> notifications;
    private List<Maturity> maturities;
    private List<EventCongratulationMessage> congratulations;
    private Integer reviewThreshold;
    private List<String> specializationPermalinks;
    private EventStatus status;
    private String caption;
    private String permalink;
    private Integer numberOfAttempts;
    private Set<String> tagIds;
    private Boolean whiteListOnly;
    private List<String> emails;
    private String seriesId;

    @Override
    public void registerViolations() {
        if (Strings.isNullOrEmpty(permalink)) {
            throw emptyPermalink();
        }

        if (Strings.isNullOrEmpty(caption)) {
            throw emptyCaption();
        }
        if ((numberOfAttempts != null) && (numberOfAttempts < 1)) {
            throw invalidAttemptsNumber();
        }
    }


    private OperationException emptyPermalink() {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_PERMALINK)
                .description("Permalink should be specified")
                .build();
    }

    private OperationException emptyCaption() {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_CAPTION)
                .description("Caption should be specified")
                .build();
    }

    private OperationException invalidAttemptsNumber() {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_ATTEMPTS_NUMBER_INVALID)
                .description("Invalid attempts number")
                .build();
    }
}

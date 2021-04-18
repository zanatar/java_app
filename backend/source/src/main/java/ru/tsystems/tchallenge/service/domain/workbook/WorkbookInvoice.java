package ru.tsystems.tchallenge.service.domain.workbook;

import com.google.common.base.Strings;
import lombok.Data;
import ru.tsystems.tchallenge.service.domain.maturity.Maturity;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_INTERNAL;

@Data
public final class WorkbookInvoice implements ValidationAware {

    private String eventId;
    private Maturity maturity;
    private String specializationPermalink;
    private String backlinkPathTemplate;

    @Override
    public void registerViolations() {
        if (Strings.isNullOrEmpty(eventId)) {
            throw eventIdIsMissing();
        }
        if (maturity == null) {
            throw maturityIsMissing();
        }
        if (Strings.isNullOrEmpty(specializationPermalink)) {
            throw specializationIsMissing();
        }
    }

    private OperationException eventIdIsMissing() {
        return OperationExceptionBuilder.operationException()
                .description("Event id is missing")
                .textcode(ERR_INTERNAL)
                .build();
    }

    private OperationException maturityIsMissing() {
        return OperationExceptionBuilder.operationException()
                .description("Maturity is missing")
                .textcode(ERR_INTERNAL)
                .build();
    }

    private OperationException specializationIsMissing() {
        return OperationExceptionBuilder.operationException()
                .description("Specialization permalink is missing")
                .textcode(ERR_INTERNAL)
                .build();
    }
}

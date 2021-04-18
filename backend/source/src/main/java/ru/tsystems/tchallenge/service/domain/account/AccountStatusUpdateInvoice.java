package ru.tsystems.tchallenge.service.domain.account;

import lombok.Builder;
import lombok.Data;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_UPD_STATUS;

@Data
@Builder
public final class AccountStatusUpdateInvoice implements ValidationAware {

    private AccountStatus newStatus;

    @Override
    public void registerViolations() {
        if (newStatus == null) {
            throw statusIsMissing();
        }
    }

    private OperationException statusIsMissing() {
        return OperationExceptionBuilder.operationException()
                .description("Status is missing")
                .textcode(ERR_UPD_STATUS)
                .build();
    }
}

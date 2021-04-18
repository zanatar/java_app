package ru.tsystems.tchallenge.service.domain.workbook;

import lombok.Data;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_ACC_STATUS;

@Data
public final class WorkbookStatusUpdateInvoice implements ValidationAware {

    private WorkbookStatus status;

    @Override
    public void registerViolations() {
        if (status == null) {
            throw statusIsMissing();
        }
    }

    private OperationException statusIsMissing() {
        return OperationExceptionBuilder.operationException()
                .description("Status is missing")
                .textcode(ERR_ACC_STATUS)
                .build();
    }
}

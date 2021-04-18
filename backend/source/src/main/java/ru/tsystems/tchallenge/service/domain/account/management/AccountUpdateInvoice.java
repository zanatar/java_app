package ru.tsystems.tchallenge.service.domain.account.management;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.tsystems.tchallenge.service.domain.account.AccountInvoice;
import ru.tsystems.tchallenge.service.domain.account.AccountStatus;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_ACC_STATUS;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "Invoice for updating accounts by admin, so it contains additional field - status")
public class AccountUpdateInvoice extends AccountInvoice {

    private AccountStatus status;

    @Override
    public void registerViolations() {
        super.registerViolations();

        if (status == null) {
            throw statusIsRequired();
        }
    }

    private OperationException statusIsRequired() {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_ACC_STATUS)
                .description("Status is required")
                .build();
    }
}

package ru.tsystems.tchallenge.service.domain.account.management;

import com.google.common.base.Strings;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.tsystems.tchallenge.service.domain.account.AccountInvoice;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_BACKLINK;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "Model for creating accounts by coworker, so need additional field")
public class AccountCreateInvoice extends AccountInvoice {
    private String backlinkTemplate;

    @Override
    public void registerViolations() {
        super.registerViolations();
        if (Strings.isNullOrEmpty(backlinkTemplate)) {
            throw backlinkIsMissing();
        }
    }

    private OperationException backlinkIsMissing() {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_BACKLINK)
                .description("Backlink is missing")
                .build();
    }
}

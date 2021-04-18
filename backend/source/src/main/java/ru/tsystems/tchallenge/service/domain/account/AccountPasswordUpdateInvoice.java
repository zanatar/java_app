package ru.tsystems.tchallenge.service.domain.account;

import com.google.common.base.Strings;
import lombok.Builder;
import lombok.Data;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_UPD_PASS;

@Data
@Builder
public class AccountPasswordUpdateInvoice implements ValidationAware {

    private String current;
    private String desired;

    @Override
    public void registerViolations() {
        if (Strings.isNullOrEmpty(current)) {
            throw passwordIsMissing("Current");
        }
        if (Strings.isNullOrEmpty(desired)) {
            throw passwordIsMissing("Desired");
        }
    }

    private OperationException passwordIsMissing(String prefix) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_UPD_PASS)
                .description(prefix + " password is missing")
                .build();
    }
}

package ru.tsystems.tchallenge.service.domain.account;

import com.google.common.base.Strings;
import lombok.Data;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_ACC_EMAIL;

@Data
public class EmailSetInvoice implements ValidationAware {

    private String email;

    @Override
    public void registerViolations() {
        if (Strings.isNullOrEmpty(email) || !validateEmail(email)) {
            throw emailIsInvalid(email);
        }
    }

    private OperationException emailIsInvalid(String email) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_ACC_EMAIL)
                .attachment(email)
                .description("Email is invalid")
                .build();
    }
}

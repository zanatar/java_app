package ru.tsystems.tchallenge.service.domain.account;

import org.springframework.stereotype.Component;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_REG_PASS;

@Component
public class AccountPasswordValidator {

    public void validate(final String password) {
        if (password == null || password.length() < 5 || password.length() > 30) {
            throw OperationExceptionBuilder.operationException()
                    .textcode(ERR_REG_PASS)
                    .description("Password doesn't meet security criteria")
                    .build();
        }
    }
}

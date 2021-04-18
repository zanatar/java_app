package ru.tsystems.tchallenge.service.security.registration;

import com.google.common.base.Strings;
import lombok.Data;
import ru.tsystems.tchallenge.service.domain.account.AccountCategory;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.*;

@Data
public final class SecurityRegistrationInvoice implements ValidationAware {

    private String email;
    private String quickname;
    private AccountCategory accountCategory;
    private String backlinkPathTemplate;

    @Override
    public void registerViolations() {
        if (Strings.isNullOrEmpty(email)) {
            throw emailIsInvalid();
        }
        if (Strings.isNullOrEmpty(backlinkPathTemplate)) {
            throw backlinkIsMissing();
        }

        if (Strings.isNullOrEmpty(quickname)) {
            throw quicknameIsMissing();
        }

        if (accountCategory == null) {
            throw accountCategoryIsMissing();
        }
    }

    private OperationException emailIsInvalid() {
        return OperationExceptionBuilder.operationException()
                .description("Email is missing")
                .textcode(ERR_ACC_EMAIL)
                .build();
    }

    private OperationException backlinkIsMissing() {
        return OperationExceptionBuilder.operationException()
                .description("Backlink is missing")
                .textcode(ERR_INTERNAL)
                .build();
    }

    private OperationException quicknameIsMissing() {
        return OperationExceptionBuilder.operationException()
                .description("Quickname is missing")
                .textcode(ERR_ACC_QUICKNAME)
                .build();
    }

    private OperationException accountCategoryIsMissing() {
        return OperationExceptionBuilder.operationException()
                .description("Account category is missing")
                .textcode(ERR_ACC_CAT)
                .build();
    }

}

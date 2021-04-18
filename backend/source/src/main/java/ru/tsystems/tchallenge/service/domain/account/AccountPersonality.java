package ru.tsystems.tchallenge.service.domain.account;

import com.google.common.base.Strings;
import lombok.Data;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_ACC_QUICKNAME;

@Data
public final class AccountPersonality implements ValidationAware {

    private String firstname;
    private String lastname;
    private String middlename;
    private String quickname;

    @Override
    public void registerViolations() {
        if (Strings.isNullOrEmpty(quickname)) {
            throw quicknameIsInvalid();
        }
    }

    private OperationException quicknameIsInvalid() {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_ACC_QUICKNAME)
                .description("Quickname is required")
                .build();
    }
}

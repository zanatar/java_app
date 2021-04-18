package ru.tsystems.tchallenge.service.security.voucher;

import com.google.common.base.Strings;
import lombok.Builder;
import lombok.Data;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_ACC_EMAIL;
import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_INTERNAL;

@Data
@Builder
public final class SecurityVoucherInvoice implements ValidationAware {

    private final String email;
    private final String backlinkTemplate;
    private final Boolean resetPassword;
    private final Boolean resend;

    @Override
    public void registerViolations() {
        if (Strings.isNullOrEmpty(email)) {
            throw emailIsInvalid();
        }
        if (Strings.isNullOrEmpty(backlinkTemplate)) {
            throw backlinkIsMissing();
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
}

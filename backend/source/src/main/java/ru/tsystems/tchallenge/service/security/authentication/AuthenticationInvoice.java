package ru.tsystems.tchallenge.service.security.authentication;

import com.google.common.base.Strings;
import lombok.Data;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_AUTH_INVOICE;
import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_INTERNAL;

@Data
public final class AuthenticationInvoice implements ValidationAware {

    private AuthenticationMethod method;
    private String email;
    private String password;
    private String passwordUpdate;
    private String tokenPayload;
    private String voucherPayload;
    private String googleIdToken;
    private VkSession vkSession;

    boolean isPasswordUpdateRequested() {
        return method == AuthenticationMethod.VOUCHER && passwordUpdate != null;
    }

    @Override
    public void registerViolations() {
        if (method == null) {
            throw authIsMissingOrUnknown();
        }
        if ((method == AuthenticationMethod.PASSWORD &&
                (Strings.isNullOrEmpty(email) || Strings.isNullOrEmpty(password)))
                || (method == AuthenticationMethod.VOUCHER && Strings.isNullOrEmpty(voucherPayload))
                || (method == AuthenticationMethod.GOOGLE && Strings.isNullOrEmpty(googleIdToken))
                || (method == AuthenticationMethod.VK && (vkSession == null))) {
            throw authInvoiceIsInvalid();
        }
    }

    private OperationException authIsMissingOrUnknown() {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_INTERNAL)
                .description("Authentication method is missing or unknown")
                .build();
    }

    private OperationException authInvoiceIsInvalid() {
        return OperationExceptionBuilder.operationException()
                .description("Authentication invoice is invalid")
                .textcode(ERR_AUTH_INVOICE)
                .build();
    }
}

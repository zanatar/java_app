package ru.tsystems.tchallenge.service.security.authentication;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.tsystems.tchallenge.service.domain.account.*;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.security.registration.SecurityRegistration;
import ru.tsystems.tchallenge.service.security.registration.SecurityRegistrationManager;
import ru.tsystems.tchallenge.service.security.token.SecurityToken;
import ru.tsystems.tchallenge.service.security.token.TokenManager;
import ru.tsystems.tchallenge.service.security.voucher.SecurityVoucher;
import ru.tsystems.tchallenge.service.security.voucher.SecurityVoucherManager;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.EnumSet;
import java.util.Set;

import static ru.tsystems.tchallenge.service.domain.account.AccountStatus.*;
import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.*;

@Component
public class AuthenticationManager {

    private AccountPasswordHashEngine accountPasswordHashEngine;
    private AccountRepository accountRepository;
    private AccountSystemManager accountSystemManager;
    private Set<AccountStatus> illegalStatuses;
    private SecurityVoucherManager securityVoucherManager;
    private TokenManager tokenManager;
    private GoogleIdTokenVerifier googleIdTokenVerifier;
    private VkVerifier vkVerifier;
    private SecurityRegistrationManager securityRegistrationManager;

    @Autowired
    public AuthenticationManager(AccountPasswordHashEngine accountPasswordHashEngine, AccountRepository accountRepository,
                                 AccountSystemManager accountSystemManager, SecurityVoucherManager securityVoucherManager, TokenManager tokenManager,
                                 GoogleIdTokenVerifier googleIdTokenVerifier, SecurityRegistrationManager securityRegistrationManager,
                                 VkVerifier vkVerifier) {
        this.accountPasswordHashEngine = accountPasswordHashEngine;
        this.accountRepository = accountRepository;
        this.accountSystemManager = accountSystemManager;
        this.securityVoucherManager = securityVoucherManager;
        this.tokenManager = tokenManager;
        this.illegalStatuses = EnumSet.of(SUSPENDED, DELETED);
        this.googleIdTokenVerifier = googleIdTokenVerifier;
        this.securityRegistrationManager = securityRegistrationManager;
        this.vkVerifier = vkVerifier;
    }

    public static UserAuthentication getAuthentication() {
        return (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
    }

    public static OperationException notAuthenticated(Object attach) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_NOT_AUTHORIZED)
                .description("Not authenticated")
                .attachment(attach)
                .build();
    }

    public UserAuthentication authenticateByPassword(final AuthenticationInvoice invoice) {
        invoice.validate();
        final String email = invoice.getEmail();
        final AccountDocument accountDocument = accountRepository.findByEmailIgnoreCase(email);
        if (accountDocument == null) {
            throw accountIsMissingOrPasswordIsIncorrect(invoice.getEmail());
        }
        final String password = invoice.getPassword();
        if (!accountPasswordHashEngine.match(password, accountDocument.getPasswordHash())) {
            throw accountIsMissingOrPasswordIsIncorrect(invoice.getEmail());
        }
        if (accountIsIllegalForAuthentication(accountDocument.getStatus())) {
            throw accountHasIllegalStatus(accountDocument.getId());
        }

        if ((accountDocument.getStatus() == CREATED) || (accountDocument.getStatus() == MODIFIED)) {
            throw accountNotVerified(accountDocument.getId());
        }

        return UserAuthentication.builder()
                .accountId(accountDocument.getId())
                .accountEmail(accountDocument.getEmail())
                .method(AuthenticationMethod.PASSWORD)
                .build();
    }

    public UserAuthentication authenticateByToken(final String tokenPayload) {
        final SecurityToken token = tokenManager.retrieveByPayload(tokenPayload);
        if (token == null) {
            throw tokenIsExpiredOrMissing(tokenPayload);
        }
        final Account account = accountSystemManager.findById(token.getAccountId());
        if (account == null) {
            throw accountIsMissing(token.getAccountId());
        }
        if (accountIsIllegalForAuthentication(account.getStatus())) {
            throw accountHasIllegalStatus(account.getId());
        }
        return UserAuthentication.builder()
                .accountId(account.getId())
                .accountEmail(account.getEmail())
                .tokenPayload(token.getPayload())
                .name(getName(account))
                .authorities(account.getRoles())
                .accountCategory(account.getCategory())
                .authenticated(true)
                .method(AuthenticationMethod.TOKEN)
                .build();
    }

    public UserAuthentication authenticateByVoucher(final AuthenticationInvoice invoice) {
        invoice.validate();
        final String payload = invoice.getVoucherPayload();
        final SecurityVoucher voucher = securityVoucherManager.utilizeByPayload(payload);
        if (voucher == null) {
            throw voucherIsExpiredOrMissing(invoice.getEmail());
        }
        final Account account = accountSystemManager.findByEmail(voucher.getAccountEmail());
        if (account == null) {
            throw accountIsMissing(voucher.getAccountEmail());
        }
        if (accountIsIllegalForAuthentication(account.getStatus())) {
            throw accountHasIllegalStatus(account.getId());
        }
        if (invoice.isPasswordUpdateRequested()) {
            accountSystemManager.updatePassword(account.getId(), invoice.getPasswordUpdate());
        }

        // When user signed up, his account isn't verified, but when user use voucher he verify account
        if (account.getStatus() != APPROVED) {
            accountSystemManager.verifyAccount(account.getId());
        }

        return UserAuthentication.builder()
                .accountId(account.getId())
                .authenticated(true)
                .authorities(account.getRoles())
                .accountCategory(account.getCategory())
                .name(getName(account))
                .accountEmail(account.getEmail())
                .method(AuthenticationMethod.VOUCHER)
                .voucherPayload(voucher.getPayload())
                .build();
    }

    public UserAuthentication authenticateByGoogleToken(final AuthenticationInvoice invoice) {
        invoice.validate();
        GoogleIdToken idToken;
        try {
            idToken = googleIdTokenVerifier.verify(invoice.getGoogleIdToken());
        } catch (GeneralSecurityException | IOException e) {
            throw tokenIsExpiredOrMissing(invoice.getGoogleIdToken());
        }
        if (idToken == null) {
            throw tokenIsExpiredOrMissing(invoice.getGoogleIdToken());
        }
        String email = idToken.getPayload().getEmail();
        Account account = accountSystemManager.findByEmail(email);
        if (account == null) {
            String firstName = (String) idToken.getPayload().get("given_name");
            String lastName = (String) idToken.getPayload().get("family_name");
            SecurityRegistration registration = securityRegistrationManager
                    .createWithGoogle(email, firstName, lastName);
            account = accountSystemManager.findById(registration.getId());
        } else {
            if (accountIsIllegalForAuthentication(account.getStatus())) {
                throw accountHasIllegalStatus(account.getId());
            }
        }
        return UserAuthentication.builder()
                .accountId(account.getId())
                .accountEmail(account.getEmail())
                .name(getName(account))
                .authorities(account.getRoles())
                .accountCategory(account.getCategory())
                .authenticated(true)
                .method(AuthenticationMethod.GOOGLE)
                .build();
    }

    private String getName(Account account) {
        AccountPersonality personality = account.getPersonality();
        return Strings.isNullOrEmpty(personality.getFirstname())
                ? personality.getQuickname() : personality.getFirstname();
    }

    private boolean accountIsIllegalForAuthentication(final AccountStatus status) {
        return illegalStatuses.contains(status);
    }

    private OperationException accountIsMissing(String accountId) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_INTERNAL)
                .description("Account is missing")
                .attachment(accountId)
                .build();
    }

    private OperationException accountIsMissingOrPasswordIsIncorrect(String attach) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_ACC_OR_PASS)
                .description("Account is missing or password is incorrect")
                .attachment(attach)
                .build();
    }

    private OperationException accountHasIllegalStatus(String id) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_ACC_ILLEGAL_STATUS)
                .description("Account cannot be accessed due to its status")
                .attachment(id)
                .build();
    }

    private OperationException accountNotVerified(String id) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_ACC_NEED_CONFIRMATION)
                .description("Need to verify account")
                .attachment(id)
                .build();
    }

    private OperationException tokenIsExpiredOrMissing(String token) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_ACC_TOKEN)
                .description("Security token is expired or does not exist")
                .attachment(token)
                .build();
    }

    private OperationException voucherIsExpiredOrMissing(String email) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_ACC_VOUCHER)
                .description("Security voucher is expired or does not exist")
                .attachment(email)
                .build();
    }

    public UserAuthentication authenticateByVK(AuthenticationInvoice invoice) {
        invoice.validate();
        vkVerifier.verifyVkAuth(invoice.getVkSession());
        String vkId = invoice.getVkSession().getUserId();
        Account account = accountSystemManager.findByVkId(vkId);
        if (account == null) {
            SecurityRegistration registration = securityRegistrationManager
                    .createWithVK(vkId, invoice.getVkSession().getFirstName(), invoice.getVkSession().getLastName());
            account = accountSystemManager.findById(registration.getId());
        } else {
            if (accountIsIllegalForAuthentication(account.getStatus())) {
                throw accountHasIllegalStatus(account.getId());
            }
        }
        return UserAuthentication.builder()
                .accountId(account.getId())
                .accountEmail(account.getEmail())
                .name(getName(account))
                .authorities(account.getRoles())
                .accountCategory(account.getCategory())
                .authenticated(true)
                .method(AuthenticationMethod.VK)
                .build();
    }

}

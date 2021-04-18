package ru.tsystems.tchallenge.service.security.authentication;

import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.Authentication;
import ru.tsystems.tchallenge.service.domain.account.AccountCategory;
import ru.tsystems.tchallenge.service.domain.account.AccountRole;

import java.util.Collection;

/**
 * Descriptor of a successful authentication.
 *
 * @author Ilia Gubarev
 */
@Data
@Builder
public final class UserAuthentication implements Authentication {

    private final AuthenticationMethod method;
    private final String accountId;
    private final String accountEmail;
    private final String generatedTokenPayload;
    private final boolean passwordUpdated;
    private final String tokenPayload;
    private final Collection<AccountRole> authorities;
    private final AccountCategory accountCategory;
    private final String voucherPayload;

    private final String name;
    private final transient Object credentials;
    private final transient Object details;
    private final transient Object principal;
    private boolean authenticated;
}

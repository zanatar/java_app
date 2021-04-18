package ru.tsystems.tchallenge.service.security.token;

import lombok.Builder;
import lombok.Data;
import ru.tsystems.tchallenge.service.domain.account.AccountCategory;
import ru.tsystems.tchallenge.service.domain.account.AccountRole;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;

@Data
@Builder
public final class SecurityToken {

    private final String id;
    private final String accountId;
    private final String payload;
    private final Instant createdAt;
    private final AccountCategory accountCategory;
    private final Collection<AccountRole> roles;
    private Instant validUntil;

    public boolean isExpired() {
        return validUntil.isBefore(Instant.now());
    }

    public void prolongate(final Duration duration) {
        validUntil = validUntil.plus(duration);
    }
}

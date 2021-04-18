package ru.tsystems.tchallenge.service.security.voucher;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public final class SecurityVoucher {

    private final String id;
    private final String accountEmail;
    private final String payload;
    private final Boolean accountVerified;
    private final Instant createdAt;
    private final Instant validUntil;

    public boolean isExpired() {
        return validUntil.isBefore(Instant.now());
    }
}

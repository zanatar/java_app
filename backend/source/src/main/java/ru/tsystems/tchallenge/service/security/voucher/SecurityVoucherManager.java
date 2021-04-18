package ru.tsystems.tchallenge.service.security.voucher;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.DAYS;

@Component
public class SecurityVoucherManager{

    private Map<String, SecurityVoucher> vouchers = new HashMap<>();

    public SecurityVoucher create(final String email, final Boolean verified) {
        final SecurityVoucher voucher = SecurityVoucher.builder()
                .id(UUID.randomUUID().toString())
                .accountEmail(email)
                .accountVerified(verified)
                .payload(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .validUntil(Instant.now().plus(1, DAYS))
                .build();
        vouchers.put(voucher.getPayload(), voucher);
        return voucher;
    }

    public SecurityVoucher getByPayload(final String payload) {
        SecurityVoucher voucher = vouchers.get(payload);
        return Optional.ofNullable(voucher)
                .flatMap(v -> voucher.isExpired()? Optional.empty() : Optional.of(v))
                .orElse(null);
    }

    public SecurityVoucher utilizeByPayload(final String payload) {
        final SecurityVoucher voucher = vouchers.get(payload);
        if (voucher == null || voucher.isExpired()) {
            return null;
        }
        vouchers.remove(payload);
        return voucher;
    }
}

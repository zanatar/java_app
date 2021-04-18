package ru.tsystems.tchallenge.service.domain.account;

import lombok.Data;
import ru.tsystems.tchallenge.service.utility.data.IdAware;

import java.time.Instant;
import java.util.Set;

@Data
public final class Account implements IdAware {

    private String id;
    private String email;
    private String passwordHash;
    private AccountCategory category;
    private Set<AccountRole> roles;
    private AccountPersonality personality;
    private ParticipantPersonality participantPersonality;
    private AccountStatus status;
    private Instant registeredAt;
    private String vkId;
}

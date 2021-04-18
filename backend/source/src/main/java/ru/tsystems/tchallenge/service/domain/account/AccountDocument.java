package ru.tsystems.tchallenge.service.domain.account;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.tsystems.tchallenge.service.utility.data.AbstractDocument;
import ru.tsystems.tchallenge.service.utility.data.IdAware;

import java.time.Instant;
import java.util.Set;

@Document(collection = "accounts")
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class AccountDocument extends AbstractDocument implements IdAware {

    private String email;
    private String passwordHash;
    private AccountCategory category;
    private Set<AccountRole> roles;
    private AccountPersonalityDocument personality;
    private ParticipantPersonalityDocument participantPersonality;
    private AccountStatus status;
    private Instant registeredAt;
    private String vkId;
}

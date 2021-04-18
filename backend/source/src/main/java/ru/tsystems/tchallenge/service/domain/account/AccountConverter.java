package ru.tsystems.tchallenge.service.domain.account;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Service;

@Mapper(componentModel = "spring")
@Service
public interface AccountConverter {
    Account toDto(AccountDocument accountDocument);

    @Mapping(target = "participantPersonality", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "registeredAt", ignore = true)
    Account toDtoShort(AccountDocument accountDocument);

    @Mapping(target = "passwordHash", ignore = true)
    Account toMgmtDto(AccountDocument accountDocument);


    AccountPersonality toPersonality(AccountPersonalityDocument accountPersonalityDocument);

    AccountPersonalityDocument fromPersonality(AccountPersonality accountPersonality);

    ParticipantPersonality toParticipantPersonality(ParticipantPersonalityDocument document);

    ParticipantPersonalityDocument fromParticipantPersonality(ParticipantPersonality invoice);
}

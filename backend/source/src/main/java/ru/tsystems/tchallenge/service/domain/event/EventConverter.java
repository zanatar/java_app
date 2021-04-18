package ru.tsystems.tchallenge.service.domain.event;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Service;

@Mapper(componentModel = "spring")
@Service
public interface EventConverter {

    Event toDto(EventDocument eventDocument);

    @Mapping(target = "description", ignore = true)
    @Mapping(target = "greeting", ignore = true)
    @Mapping(target = "validFrom", ignore = true)
    @Mapping(target = "validUntil", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "maturities", ignore = true)
    @Mapping(target = "specializationPermalinks", ignore = true)
    @Mapping(target = "numberOfAttempts", ignore = true)
    @Mapping(target = "congratulations", ignore = true)
    Event toDtoShort(EventDocument eventDocument);

    @Mapping(target = "permalink", ignore = true)
    @Mapping(target = "caption", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "greeting", ignore = true)
    @Mapping(target = "validFrom", ignore = true)
    @Mapping(target = "validUntil", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "maturities", ignore = true)
    @Mapping(target = "specializationPermalinks", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "numberOfAttempts", ignore = true)
    @Mapping(target = "tagIds", ignore = true)
    @Mapping(target = "whiteListOnly", ignore = true)
    @Mapping(target = "emails", ignore = true)
    @Mapping(target = "seriesId", ignore = true)
    Event toDtoClassified(EventDocument eventDocument);
}

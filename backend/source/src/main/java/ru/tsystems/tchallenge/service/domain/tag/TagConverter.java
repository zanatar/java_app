package ru.tsystems.tchallenge.service.domain.tag;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Service;

@Mapper(componentModel = "spring")
@Service
public interface TagConverter {
    Tag toDto(TagDocument tagDocument);
}

package ru.tsystems.tchallenge.service.domain.specialization;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Service;

@Mapper(componentModel = "spring")
@Service
public interface SpecializationConverter {
    Specialization toDto(SpecializationDocument specializationDocument);
}

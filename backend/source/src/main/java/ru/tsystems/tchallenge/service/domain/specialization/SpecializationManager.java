package ru.tsystems.tchallenge.service.domain.specialization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SpecializationManager {

    private final SpecializationRepository specializationRepository;
    private final SpecializationConverter specializationConverter;

    @Autowired
    public SpecializationManager(SpecializationRepository specializationRepository,
                                 SpecializationConverter specializationConverter) {
        this.specializationRepository = specializationRepository;
        this.specializationConverter = specializationConverter;
    }

    public List<Specialization> retrieveAll() {
        return specializationRepository
                .findAll()
                .stream()
                .map(specializationConverter::toDto)
                .collect(Collectors.toList());
    }

}

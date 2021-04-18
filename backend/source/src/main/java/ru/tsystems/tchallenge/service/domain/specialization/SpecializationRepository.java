package ru.tsystems.tchallenge.service.domain.specialization;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpecializationRepository extends MongoRepository<SpecializationDocument, String> {
    Optional<SpecializationDocument> findByPermalink(String permalink);
}

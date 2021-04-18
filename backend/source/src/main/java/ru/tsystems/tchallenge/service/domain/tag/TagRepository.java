package ru.tsystems.tchallenge.service.domain.tag;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends MongoRepository<TagDocument, String> {
}

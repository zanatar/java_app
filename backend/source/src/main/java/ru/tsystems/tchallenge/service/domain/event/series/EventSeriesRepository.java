package ru.tsystems.tchallenge.service.domain.event.series;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventSeriesRepository extends MongoRepository<EventSeriesDocument, String> {
    Page<EventSeriesDocument> findByCaptionContainingIgnoreCase(String caption, Pageable pageable);
}

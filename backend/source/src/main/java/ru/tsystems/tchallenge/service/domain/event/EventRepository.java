package ru.tsystems.tchallenge.service.domain.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.CountQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface EventRepository extends MongoRepository<EventDocument, String> {
    List<EventDocument> findByPermalinkRegexAndStatusIn(String permalinkRegex, Collection<EventStatus> statuses,
                                                        Pageable pageable);


    @Query(value =
            "{" +
                    "status: {$in: ?0}, " +
                    "permalink: {$regex: ?1}, " +
                    "validFrom: {$lte: ?2}, " +
                    "validUntil: {$gt: ?2}" +
                    "}")
    List<EventDocument> findActiveEventsByFilter(Collection<EventStatus> statuses, String permalinkRegex,
                                                 Instant now, Pageable pageable);

    @CountQuery(value =
            "{" +
                    "status: {$in: ?0}, " +
                    "permalink: {$regex: ?1}, " +
                    "validFrom: {$lte: ?2}, " +
                    "validUntil: {$gt: ?2}" +
                    "}")
    Long countActiveEventsByFilter(Collection<EventStatus> statuses, String permalinkRegex,
                                                 Instant now);


    EventDocument findByPermalinkIgnoreCase(String permalink);

    Set<EventDocument> findByIdIn(List<String> ids);

    Page<EventDocument> findByCaptionContainingIgnoreCaseAndSeriesIdIn(String caption,
                                                                       Collection<String> seriesIds,
                                                                       Pageable pageable);

    Page<EventDocument> findByCaptionContainingIgnoreCase(String caption, Pageable pageable);

}

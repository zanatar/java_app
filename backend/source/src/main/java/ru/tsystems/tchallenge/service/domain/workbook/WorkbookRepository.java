package ru.tsystems.tchallenge.service.domain.workbook;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.Document;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import ru.tsystems.tchallenge.service.domain.statistics.UserStatistics;
import ru.tsystems.tchallenge.service.utility.search.Filter;
import ru.tsystems.tchallenge.service.utility.search.SearchResult;
import ru.tsystems.tchallenge.service.utility.search.Type;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Repository
public interface WorkbookRepository extends MongoRepository<WorkbookDocument, String> {
    List<WorkbookDocument> findByOwnerId(String id);

    List<WorkbookDocument> findByOwnerId(String id, Pageable pageable);

    Long countByOwnerId(String id);

    Long countByOwnerIdAndStatusIn(String id, Set<WorkbookStatus> statuses);

    List<WorkbookDocument> findByOwnerIdAndEventId(String ownerId, String eventId);

    default List<WorkbookDocument> findByOwnerIdAndEventCaption(MongoTemplate mongoTemplate,
                                                                String ownerId,
                                                                String eventCaption,
                                                                Pageable pageable) {
        Criteria criteria = Criteria.where("ownerId").is(ownerId);
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(criteria),
                Aggregation.project("textcode", "assignments", "eventId", "specializationPermalink",
                        "ownerId", "submittableUntil", "maturity", "status", "reviewed", "createdAt",
                        "avgScore", "assessedAt")
                        .and(context -> context.getMappedObject(Document.parse("{$toObjectId: '$eventId'}")))
                        .as("convertedId"),
                Aggregation.lookup("events", "convertedId", "_id", "event"),
                Aggregation.unwind("event"),
                Aggregation.match(Criteria.where("event.caption").regex(".*" + eventCaption + ".*","i")),
                Aggregation.sort(pageable.getSort()),
                Aggregation.skip((long) pageable.getPageSize() * pageable.getPageNumber()),
                Aggregation.limit(pageable.getPageSize()));
        return mongoTemplate.aggregate(aggregation, WorkbookDocument.class, WorkbookDocument.class)
                .getMappedResults();
    }

    default Long countByOwnerIdAndEventCaption(MongoTemplate mongoTemplate, String ownerId, String eventCaption) {
        Criteria criteria = Criteria.where("ownerId").is(ownerId);
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(criteria),
                Aggregation.project()
                        .and(context -> context.getMappedObject(Document.parse("{$toObjectId: '$eventId'}")))
                        .as("convertedId"),
                Aggregation.lookup("events", "convertedId", "_id", "event"),
                Aggregation.unwind("event"),
                Aggregation.match(Criteria.where("event.caption").regex(".*" + eventCaption + ".*","i")),
                Aggregation.count().as("total"));
        List<SearchResult> results = mongoTemplate.aggregate(aggregation, WorkbookDocument.class, SearchResult.class)
                .getMappedResults();
        return (results.size() > 0) ? results.get(0).getTotal() : 0;
    }

    List<WorkbookDocument> findByEventIdAndStatusInAndReviewed(String eventId, Set<WorkbookStatus> eventStatus, Boolean reviewed);


    List<WorkbookDocument> findByEventIdAndStatusInAndReviewed(String eventId,
                                                                 Set<WorkbookStatus> eventStatus,
                                                                 Boolean reviewed,
                                                                 Pageable pageable);

    default Long countBestReviewedWorkbooks(MongoTemplate mongoTemplate,
                                            String eventId, String nameFilterText, Filter scoreFilter,
                                            Filter rankFilter, Filter timeFilter) {
        List<AggregationOperation> operations = new ArrayList<>();
        filterByEventAndScore(eventId, scoreFilter, operations);
        evalRanks(operations);
        filterByNameRankAndTime(nameFilterText, rankFilter, timeFilter, operations);
        operations.add(Aggregation.count().as("total"));
        Aggregation aggregation = Aggregation.newAggregation(operations);
        List<SearchResult> results = mongoTemplate.aggregate(aggregation, WorkbookDocument.class, SearchResult.class)
                .getMappedResults();
        return (results.size() > 0) ? results.get(0).getTotal() : 0;
    }

    default List<UserStatistics> findBestReviewedWorkbookForEachUser(MongoTemplate mongoTemplate,
                                                                     String eventId,
                                                                     String nameFilterText,
                                                                     Filter scoreFilter,
                                                                     Filter rankFilter,
                                                                     Filter timeFilter, Pageable pageable) {

        List<AggregationOperation> operations = new ArrayList<>();
        filterByEventAndScore(eventId, scoreFilter, operations);
        evalRanks(operations);
        filterByNameRankAndTime(nameFilterText, rankFilter, timeFilter, operations);
        sort(pageable, operations);
        Aggregation aggregation = Aggregation.newAggregation(operations);
        return mongoTemplate.aggregate(aggregation, WorkbookDocument.class, UserStatistics.class)
                .getMappedResults();
    }

    default void sort(Pageable pageable, List<AggregationOperation> operations) {
        operations.addAll(Arrays.asList(Aggregation.sort(pageable.getSort()),
                Aggregation.skip((long) pageable.getPageSize() * pageable.getPageNumber()),
                Aggregation.limit(pageable.getPageSize())));
    }

    default void filterByEventAndScore(String eventId, Filter scoreFilter, List<AggregationOperation> operations) {
        Criteria criteria = getCriteria(eventId, scoreFilter);
        operations.add(Aggregation.match(criteria));
        operations.add(Aggregation.sort(Sort.Direction.DESC, "avgScore"));
        operations.add( Aggregation.group("ownerId")
                .first("avgScore").as("avgScore")
                .first("assessedAt").as("assessedAt"));
    }

    default void filterByNameRankAndTime(String nameFilterText, Filter rankFilter, Filter timeFilter, List<AggregationOperation> operations) {
        operations.addAll(Arrays.asList(
                Aggregation.project("minRank", "maxRank")
                        .and("arr.assessedAt").as("assessedAt")
                        .and("_id").as("avgScore")
                        .and(context -> context.getMappedObject(Document.parse("{$toObjectId: '$arr.ownerId'}")))
                        .as("convertedId"),
                Aggregation.lookup("accounts", "convertedId", "_id", "account"),
                Aggregation.unwind("account"),
                Aggregation.project("assessedAt", "avgScore", "minRank", "maxRank")
                        .and("account.personality").as("accountPersonality")
                        .and("avgScore").as("correctlySolvedRate")
                        .and(getHour(timeFilter)).as("hour")
                        .and(getMinute(timeFilter)).as("minute"),
                Aggregation.match(getNameRankTimeCriteria(nameFilterText, rankFilter, timeFilter))
        ));
    }

    default void evalRanks(List<AggregationOperation> operations) {
        DBObject push = new BasicDBObject();
        push.put("ownerId", "$_id");
        push.put("assessedAt", "$assessedAt");
        push.put("avgScore", "$avgScore");
        DBObject push2 = new BasicDBObject();
        push2.put("ownerId", "$arr.ownerId");
        push2.put("assessedAt", "$arr.assessedAt");
        push2.put("avgScore", "$arr.avgScore");
        push2.put("globalRank", "$arr.globalRank");
        operations.addAll(Arrays.asList(Aggregation.sort(Sort.Direction.DESC, "avgScore"),
                Aggregation.group().push(push).as("arr"),
                Aggregation.unwind("arr", "globalRank"),
                Aggregation.group("arr.avgScore").push(push2).as("arr")
                        .max("globalRank").as("maxRank")
                        .min("globalRank").as("minRank"),
                Aggregation.sort(Sort.Direction.DESC, "arr.avgScore"),
                Aggregation.unwind("arr")));
    }

    default DateOperators.Hour getHour(Filter filter) {
        return DateOperators.Hour.hourOf("assessedAt").withTimezone(
                (filter != null) ?
                DateOperators.Timezone.valueOf(filter.getUtcOffset())
        : DateOperators.Timezone.none());
    }

    default DateOperators.Minute getMinute(Filter filter) {
        return DateOperators.Minute.minuteOf("assessedAt").withTimezone(
                (filter != null) ?
                        DateOperators.Timezone.valueOf(filter.getUtcOffset())
                        : DateOperators.Timezone.none());
    }

    default Criteria getCriteria(String eventId, Filter scoreFilter) {
        Criteria criteria = Criteria.where("eventId").is(eventId)
                .and("status").is(WorkbookStatus.ASSESSED)
                .and("reviewed").is(true);
        if (scoreFilter != null) {
            BigDecimal score = new BigDecimal(scoreFilter.getFilter());
            switch (scoreFilter.getType()) {
                case notEqual:
                    criteria = criteria.and("avgScore").ne(score);
                    break;
                case lessThan:
                    criteria = criteria.and("avgScore").lt(score);
                    break;
                case lessThanOrEqual:
                    criteria = criteria.and("avgScore").lte(score);
                    break;
                case greaterThan:
                    criteria = criteria.and("avgScore").gt(score);
                    break;
                case greaterThanOrEqual:
                    criteria = criteria.and("avgScore").gte(score);
                    break;
                case inRange:
                    criteria = criteria.andOperator(Criteria.where("avgScore").gte(score),
                            Criteria.where("avgScore").lte(new BigDecimal(scoreFilter.getFilterTo())));
                    break;
                default:
                    criteria = criteria.and("avgScore").is(score);
            }
        }
        return criteria;
    }

    default Criteria getNameRankTimeCriteria(String nameFilterText, Filter rankFilter, Filter timeFilter) {
        Criteria criteria = Criteria
                .where("accountPersonality.quickname")
                .regex(".*" + nameFilterText + ".*", "i");
        if (rankFilter != null) {
            Integer rank = Integer.parseInt(rankFilter.getFilter()) -1 ;
            if (rankFilter.getType() == Type.inRange) {
                criteria = criteria.andOperator(Criteria.where("minRank").gte(rank),
                        Criteria.where("maxRank").lte(Integer.parseInt(rankFilter.getFilterTo()) - 1));
            }
        }
        if (timeFilter != null) {
            if (timeFilter.getType() == Type.lessThanOrEqual) {
                criteria = criteria.orOperator(Criteria.where("hour").lt(timeFilter.getHour()),
                        Criteria.where("hour").is(timeFilter.getHour())
                        .and("minute").lte(timeFilter.getMinute()));
            } else if (timeFilter.getType() == Type.greaterThanOrEqual) {
                criteria = criteria.orOperator(Criteria.where("hour").gt(timeFilter.getHour()),
                        Criteria.where("hour").is(timeFilter.getHour())
                                .and("minute").gte(timeFilter.getMinute()));
            }
        }
        return criteria;
    }


    @Query(value = "{eventId: ?0, status: {$in: ?1}, avgScore: {$gte : ?2}, reviewed: true}")
    Set<WorkbookDocument> findPrizeApplicants(String eventId, Set<WorkbookStatus> statuses,
                                               BigDecimal avgScore);

    List<WorkbookDocument> findByEventIdAndStatus(String eventId, WorkbookStatus status, Pageable pageable);

    Long countByEventIdAndStatus(String eventId, WorkbookStatus status);

    List<WorkbookDocument> findByStatus(WorkbookStatus status, Pageable pageable);

    Long countByStatus(WorkbookStatus status);

    List<WorkbookDocument> findByEventId(String eventId);
}

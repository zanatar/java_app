package ru.tsystems.tchallenge.service.domain.account;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import ru.tsystems.tchallenge.service.utility.search.Filter;
import ru.tsystems.tchallenge.service.utility.search.SearchResult;
import ru.tsystems.tchallenge.service.utility.search.Type;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Repository
public interface AccountRepository extends MongoRepository<AccountDocument, String> {
    AccountDocument findByEmailIgnoreCase(String email);

    @Query(fields = "{ email : 1}")
    List<AccountDocument> findByCategoryAndEmailContainingIgnoreCase(AccountCategory category,
                                                                     String email,
                                                                     Pageable pageable);

    Long countByCategoryAndEmailContainingIgnoreCase(AccountCategory category, String email);

    AccountDocument findByVkId(String vkId);


    default List<AccountDocument> find(MongoTemplate template, String email, String nameFilter,
                                       Set<AccountStatus> statuses,
                                       Set<AccountRole> roles,
                                       Filter dateFilter, Pageable pageable) {
        Criteria criteria = getCriteria(email, statuses, roles, dateFilter);
        ConditionalOperators.IfNull firstName = ConditionalOperators.ifNull("personality.firstname")
                .then("");
        ConditionalOperators.IfNull lastName = ConditionalOperators.ifNull("personality.lastname")
                .then("");
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.project("email", "category", "roles", "personality",
                        "participantPersonality", "status", "registeredAt")
                        .and(firstName).as("firstname")
                        .and(lastName).as("lastname"),
                Aggregation.project("email", "category", "roles", "personality",
                        "participantPersonality", "status", "registeredAt")
                .and(getName()).as("name"),
                Aggregation.match(Criteria.where("name").regex(".*" + nameFilter + ".*", "i")),
                Aggregation.sort(pageable.getSort()),
                Aggregation.skip((long) pageable.getPageSize() * pageable.getPageNumber()),
                Aggregation.limit(pageable.getPageSize()));
        return template.aggregate(aggregation, AccountDocument.class, AccountDocument.class)
                .getMappedResults();
    }

    default ConditionalOperators.Cond getName() {

        return ConditionalOperators.Cond.when(
                BooleanOperators.Or.or(ComparisonOperators.Eq.valueOf("firstname")
                                .equalToValue(""),
                        ComparisonOperators.Eq.valueOf("lastname")
                                .equalToValue("")))
                .thenValueOf("personality.quickname")
                .otherwise(StringOperators.Concat.valueOf("firstname")
                        .concat(" ").concatValueOf("lastname"));
    }

    default Long count(MongoTemplate template, String email, String nameFilter,
                       Set<AccountStatus> statuses,
                       Set<AccountRole> roles, Filter dateFilter) {
        Criteria criteria = getCriteria(email, statuses, roles, dateFilter);
        ConditionalOperators.IfNull firstName = ConditionalOperators.ifNull("personality.firstname")
                .then("");
        ConditionalOperators.IfNull lastName = ConditionalOperators.ifNull("personality.lastname")
                .then("");
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.project("email", "category", "roles", "personality",
                        "participantPersonality", "status", "registeredAt")
                        .and(firstName).as("firstname")
                        .and(lastName).as("lastname"),
                Aggregation.project("email", "category", "roles", "personality",
                        "participantPersonality", "status", "registeredAt")
                        .and(getName()).as("name"),
                Aggregation.match(Criteria.where("name").regex(".*" + nameFilter + ".*", "i")),
                Aggregation.count().as("total"));
        List<SearchResult> mappedResults = template.aggregate(aggregation, AccountDocument.class, SearchResult.class)
                .getMappedResults();
        return (mappedResults.size() > 0) ? mappedResults.get(0).getTotal() : 0;
    }

    default Criteria getCriteria(String email, Set<AccountStatus> statuses,
                                 Set<AccountRole> roles, Filter dateFilter) {
        Criteria criteria = Criteria.where("status").in(statuses)
                .and("roles").in(roles)
                .and("email").regex(".*" + email + ".*", "i");  //case-insensitive
        if ((dateFilter != null) && (dateFilter.getType().equals(Type.inRange))) {
            Instant dateTo = dateFilter.getDateTo().plus(1, ChronoUnit.DAYS)
                    .minus(1, ChronoUnit.MILLIS);
            criteria = criteria.andOperator(
                    Criteria.where("registeredAt").gte(dateFilter.getDateFrom()),
                    Criteria.where("registeredAt").lte(dateTo));
        }
        return criteria;
    }
}

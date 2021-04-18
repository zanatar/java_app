package ru.tsystems.tchallenge.service.domain.problem;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ru.tsystems.tchallenge.service.utility.search.SearchResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.tsystems.tchallenge.service.domain.problem.ProblemStatus.APPROVED;

@Repository
public interface ProblemRepository extends MongoRepository<ProblemDocument, String> {


    List<ProblemDocument> findByIdIn(final List<String> ids);


    // Can return less then sample number, because sample can return same element twice (or more times)
    default Map<String, ProblemDocument> retrieveRandomByCriteria(MongoTemplate mongoTemplate,
                                                                  ProblemRandomInvoice invoice,
                                                                  ProblemDifficulty difficulty) {
        Criteria filter = Criteria.where("categories").in(invoice.getCategories());
        if (!invoice.getTagIds().isEmpty()) {
            filter = filter.and("tagIds").all(invoice.getTagIds());
        }
        filter = filter
                .and("difficulty").is(difficulty)
                .and("status").is(APPROVED);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(filter),
                Aggregation.sample(invoice.getDifficulties().get(difficulty))
        );
        return mongoTemplate
                .aggregate(aggregation, ProblemDocument.class, ProblemDocument.class)
                .getMappedResults()
                .stream()
                .collect(Collectors.toMap((ProblemDocument::getId), p -> p));
    }


    default List<ProblemDocument> findRandom(MongoTemplate mongoTemplate, final ProblemRandomInvoice invoice) {
        final int maxRandomIterations = 3;
        final Map<String, ProblemDocument> randomProblems = new HashMap<>();
        invoice.getDifficulties().forEach((difficulty, number) -> {
            final Map<String, ProblemDocument> problemsForDifficulty = new HashMap<>();
            int iterations = 0;
            while (problemsForDifficulty.size() < number && iterations < maxRandomIterations) {
                problemsForDifficulty.putAll(retrieveRandomByCriteria(mongoTemplate, invoice, difficulty));
                iterations++;
            }
            randomProblems.putAll(problemsForDifficulty);
        });
        return new ArrayList<>(randomProblems.values());
    }


    default List<ProblemDocument> find(MongoTemplate mongoTemplate,
                                       ProblemSearchInvoice searchInvoice,
                                       Pageable pageable) {
        Criteria filter = getCriteriaByInvoice(searchInvoice);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(filter),
                Aggregation.skip(pageable.getOffset()),
                Aggregation.limit(pageable.getPageSize())
        );
        return mongoTemplate
                .aggregate(aggregation, ProblemDocument.class, ProblemDocument.class)
                .getMappedResults();
    }

    default Long countByFilter(MongoTemplate mongoTemplate, ProblemSearchInvoice searchInvoice) {
        Criteria filter = getCriteriaByInvoice(searchInvoice);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(filter),
                Aggregation.count().as("total")
        );
        List<SearchResult> results = mongoTemplate
                .aggregate(aggregation, ProblemDocument.class, SearchResult.class)
                .getMappedResults();
        return results.isEmpty() ? 0 : results.get(0).getTotal();
    }

    default Criteria getCriteriaByInvoice(ProblemSearchInvoice searchInvoice) {
        Criteria filter = Criteria
                .where("difficulty").in(searchInvoice.getDifficulties())
                .orOperator(Criteria.where("caption").regex(searchInvoice.getFilterText(), "i"),
                        Criteria.where("question").regex(searchInvoice.getFilterText(), "i"));
        if ((searchInvoice.getTagIds() != null) && (!searchInvoice.getTagIds().isEmpty())) {
            filter = filter.and("tagIds").in(searchInvoice.getTagIds());
        }
        return filter;
    }

}

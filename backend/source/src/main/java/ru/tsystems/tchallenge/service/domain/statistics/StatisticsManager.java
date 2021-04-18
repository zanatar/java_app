package ru.tsystems.tchallenge.service.domain.statistics;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import ru.tsystems.tchallenge.service.domain.workbook.WorkbookRepository;
import ru.tsystems.tchallenge.service.utility.search.Filter;
import ru.tsystems.tchallenge.service.utility.search.SortInvoice;

import java.util.ArrayList;
import java.util.List;

@Service
public class StatisticsManager {

    private final WorkbookRepository workbookRepository;
    private final MongoTemplate mongoTemplate;

    public StatisticsManager(WorkbookRepository workbookRepository, MongoTemplate mongoTemplate) {
        this.workbookRepository = workbookRepository;
        this.mongoTemplate = mongoTemplate;
    }


    StatisticsResult retrieveStatisticsForEvent(StatisticsInvoice invoice) {
        invoice.validate();

        PageRequest pageRequest = PageRequest.of(invoice.getPageIndex(), invoice.getPageSize(),
                getSort(invoice.getSort()));
        Filter nameFilter = invoice.getFilters().get(StatFilterKey.quickName);
        String nameFilterText = (nameFilter != null) ? nameFilter.getFilter() : "";
        List<UserStatistics> statistics = workbookRepository
                .findBestReviewedWorkbookForEachUser(mongoTemplate,
                        invoice.getEventId(), nameFilterText, invoice.getFilters().get(StatFilterKey.score),
                        invoice.getFilters().get(StatFilterKey.rank),
                        invoice.getFilters().get(StatFilterKey.assessedAt),
                        pageRequest);
        for (UserStatistics stat: statistics) {
            stat.setMinRank(stat.getMinRank() + 1);
            stat.setMaxRank(stat.getMaxRank() + 1);
        }
        Long total = workbookRepository.countBestReviewedWorkbooks(mongoTemplate, invoice.getEventId(),
                nameFilterText, invoice.getFilters().get(StatFilterKey.score),
                invoice.getFilters().get(StatFilterKey.rank),
                invoice.getFilters().get(StatFilterKey.assessedAt));
        return StatisticsResult.builder()
                .total(total)
                .statistics(statistics)
                .build();
    }

    private Sort getSort(List<SortInvoice<StatFilterKey>> sortModels) {
        List<Sort.Order> orders = new ArrayList<>();
        if (sortModels != null) {
            for (SortInvoice<StatFilterKey> model : sortModels) {
                StatFilterKey sortKey = model.getKey();
                Sort.Direction direction = model.getAscending() ? Sort.Direction.ASC : Sort.Direction.DESC;
                switch (sortKey) {
                    case score:
                        orders.add(new Sort.Order(direction, "avgScore"));
                        break;
                    case quickName:
                        orders.add(new Sort.Order(direction, "accountPersonality.quickname"));
                        break;
                    case assessedAt:
                        orders.add(new Sort.Order(direction, sortKey.name()));
                        break;
                    case rank:
                        orders.add(new Sort.Order(direction, "minRank"));
                        break;
                    default:
                }
            }
        }
        return orders.isEmpty() ? Sort.by(Sort.Direction.DESC, "avgScore") : Sort.by(orders);
    }


}

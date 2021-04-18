package ru.tsystems.tchallenge.service.domain.statistics;

import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.tsystems.tchallenge.service.utility.search.SearchInvoice;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType;


@EqualsAndHashCode(callSuper = true)
@Data
public class StatisticsInvoice extends SearchInvoice<StatFilterKey> {
    private String eventId;

    @Override
    public void registerViolations() {
        super.registerViolations();
        if (Strings.isNullOrEmpty(eventId)) {
            throw eventIdIsMissing();
        }

    }

    private OperationException eventIdIsMissing() {
        return OperationExceptionBuilder.operationException()
                .textcode(OperationExceptionType.ERR_INTERNAL)
                .description("Event id is missing")
                .build();
    }

}

enum StatFilterKey {
    quickName, rank, score, assessedAt
}

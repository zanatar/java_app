package ru.tsystems.tchallenge.service.utility.search;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_PAGE_INDEX;
import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_PAGE_SIZE;

@Data
@RequiredArgsConstructor
public class SearchInvoice<T> implements ValidationAware {
    private Integer pageSize;
    private Integer pageIndex;
    private Map<T, Filter> filters = new HashMap<>();
    private List<SortInvoice<T>> sort;

    @Override
    public void registerViolations() {
        if ((pageIndex == null) || (pageIndex < 0)) {
            throw wrongPageIndex();
        }
        if ((pageSize == null) || (pageSize < 1)) {
            throw wrongPageSize();
        }
    }

    private OperationException wrongPageIndex() {
        return OperationExceptionBuilder.operationException()
                .description("Wrong page index")
                .textcode(ERR_PAGE_INDEX)
                .build();
    }

    private OperationException wrongPageSize() {
        return OperationExceptionBuilder.operationException()
                .description("Wrong page size")
                .textcode(ERR_PAGE_SIZE)
                .build();
    }
}

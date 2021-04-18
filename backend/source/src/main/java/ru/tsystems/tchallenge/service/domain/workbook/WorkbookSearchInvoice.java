package ru.tsystems.tchallenge.service.domain.workbook;

import lombok.Builder;
import lombok.Data;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_PAGE_INDEX;
import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_PAGE_SIZE;

@Data
@Builder
public class WorkbookSearchInvoice implements ValidationAware {
    private Integer pageSize;
    private Integer pageIndex;
    private WorkbookFilter filter;

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

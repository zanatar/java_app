package ru.tsystems.tchallenge.service.domain.problem;

import lombok.Builder;
import lombok.Data;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

import java.util.Map;
import java.util.Set;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.*;

@Data
@Builder
public final class ProblemRandomInvoice implements ValidationAware {

    private Set<ProblemCategory> categories;
    public static final int NUMBER = 4;
    private Map<ProblemDifficulty, Integer> difficulties;
    private Set<String> tagIds;

    @Override
    public void registerViolations() {
        if (categories == null || categories.isEmpty()) {
            throw categoriesIsMissing();
        }
        if (difficulties == null || difficulties.isEmpty()) {
            throw difficultiesAreMissing();
        }
        final int tasksNumber = difficulties.values().stream().reduce(0, Integer::sum);
        if (tasksNumber != NUMBER) {
            throw wrongDifficultiesNumber();
        }
    }

    private OperationException categoriesIsMissing() {
        return OperationExceptionBuilder.operationException()
                .description("Category is missing")
                .textcode(ERR_PROBLEM_CATEGORY)
                .build();
    }

    private OperationException difficultiesAreMissing() {
        return OperationExceptionBuilder.operationException()
                .description("Difficulties are missing")
                .textcode(ERR_PROBLEM_DIFFICULTIES)
                .build();
    }

    private OperationException wrongDifficultiesNumber() {
        return OperationExceptionBuilder.operationException()
                .description("Wrong difficulties number")
                .textcode(ERR_PROBLEM_DIFFICULTIES_NUMBER)
                .build();
    }
}

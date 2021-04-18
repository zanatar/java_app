package ru.tsystems.tchallenge.service.domain.problem.snippet;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_PROBLEM_CONTENT;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public final class ProblemSnippetInvoice implements ValidationAware {

    private String content;
    private ProblemSnippetStyle style;

    @Override
    public void registerViolations() {
        if (Strings.isNullOrEmpty(content)) {
            throw contentIsMissing();
        }
    }

    private OperationException contentIsMissing() {
        return OperationExceptionBuilder.operationException()
                .description("Content is missing")
                .textcode(ERR_PROBLEM_CONTENT)
                .build();
    }
}

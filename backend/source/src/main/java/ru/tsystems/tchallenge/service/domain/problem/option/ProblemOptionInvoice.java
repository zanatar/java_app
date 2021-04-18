package ru.tsystems.tchallenge.service.domain.problem.option;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public final class ProblemOptionInvoice implements ValidationAware {

    private String content;
    private Boolean correct;

    @Override
    public void registerViolations() {
    }

}
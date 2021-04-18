package ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.contest.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProblemTestInvoice implements ValidationAware {
    String input;
    String output;

    @Override
    public void registerViolations() {
    }
}



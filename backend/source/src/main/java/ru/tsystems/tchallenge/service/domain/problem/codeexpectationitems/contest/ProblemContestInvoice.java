package ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.contest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.contest.test.ProblemTestInvoice;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public final class ProblemContestInvoice implements ValidationAware {
    private List<ProblemTestInvoice> tests;
    private Integer timeLimit;
    private Integer memoryLimit;

    @Override
    public void registerViolations() {
    }
}

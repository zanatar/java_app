package ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems;

import ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.contest.ProblemContestInvoice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProblemCodeExpectationItemsInvoice implements ValidationAware {
    private ProblemContestInvoice contest;
    private String predefinedLang;
    private String predefinedCode;
    private Boolean enableTestsRun;

    @Override
    public void registerViolations() {
    }
}

package ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems;

import ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.contest.ProblemContestDocument;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProblemCodeExpectationItemsDocument {
    private ProblemContestDocument contest;
    private String predefinedLang;
    private String predefinedCode;
    private Boolean enableTestsRun;
}

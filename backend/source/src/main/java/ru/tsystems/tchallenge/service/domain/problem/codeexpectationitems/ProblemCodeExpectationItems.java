package ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems;

import ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.contest.ProblemContest;
import lombok.Data;

@Data
public class ProblemCodeExpectationItems {
    private ProblemContest contest;
    private String predefinedLang;
    private String predefinedCode;
    private Boolean enableTestsRun;
}
package ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.contest;

import java.util.List;
import lombok.Data;
import ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.contest.test.ProblemTest;

@Data
public class ProblemContest {
    private List<ProblemTest> tests;
    private Integer timeLimit;
    private Integer memoryLimit;
}


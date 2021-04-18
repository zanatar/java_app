package ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.contest;

import lombok.Builder;
import lombok.Data;
import ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.contest.test.ProblemTestDocument;

import java.util.List;

@Data
@Builder
public class ProblemContestDocument {
    private List<ProblemTestDocument> tests;
    private Integer timeLimit;
    private Integer memoryLimit;
}

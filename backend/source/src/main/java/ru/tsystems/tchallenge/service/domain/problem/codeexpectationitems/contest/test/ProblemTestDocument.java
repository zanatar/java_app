package ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.contest.test;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProblemTestDocument {
    String input;
    String output;
}

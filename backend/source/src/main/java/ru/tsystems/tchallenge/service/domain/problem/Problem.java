package ru.tsystems.tchallenge.service.domain.problem;

import lombok.Data;
import ru.tsystems.tchallenge.service.domain.problem.image.ProblemImage;
import ru.tsystems.tchallenge.service.domain.problem.option.ProblemOption;
import ru.tsystems.tchallenge.service.domain.problem.snippet.ProblemSnippet;
import ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.ProblemCodeExpectationItemsDocument;

import java.util.List;
import java.util.Set;

@Data
public final class Problem {
    private String id;
    private List<ProblemCategory> categories;
    private Integer complexity;
    private ProblemDifficulty difficulty;
    private ProblemExpectation expectation;
    private List<ProblemImage> images;
    private String introduction;
    private List<ProblemOption> options;
    private ProblemCodeExpectationItemsDocument codeExpectationItems;
    private String question;
    private String caption;
    private List<ProblemSnippet> snippets;
    private ProblemStatus status;
    private Set<String> tagIds;
}

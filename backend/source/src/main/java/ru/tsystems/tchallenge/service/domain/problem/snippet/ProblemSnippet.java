package ru.tsystems.tchallenge.service.domain.problem.snippet;

import lombok.Data;

@Data
public final class ProblemSnippet {
    private String content;
    private ProblemSnippetStyle style;
}

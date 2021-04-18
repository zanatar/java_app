package ru.tsystems.tchallenge.service.domain.problem.snippet;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class ProblemSnippetDocument {
    String content;
    ProblemSnippetStyle style;
}

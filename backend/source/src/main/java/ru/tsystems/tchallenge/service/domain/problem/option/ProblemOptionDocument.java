package ru.tsystems.tchallenge.service.domain.problem.option;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class ProblemOptionDocument {
    private Integer index;
    private String textcode;
    private String content;
    private Boolean correct;
}

package ru.tsystems.tchallenge.service.domain.problem.option;

import lombok.Data;

@Data
public final class ProblemOption {

    private Integer index;
    private String textcode;
    private String content;
    private Boolean correct;
}

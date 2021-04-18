package ru.tsystems.tchallenge.service.domain.workbook.assignment;

import lombok.Data;
import ru.tsystems.tchallenge.service.domain.problem.Problem;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.code.CodeSolution;

@Data
public final class Assignment {

    private Integer index;
    private Problem problem;
    private Integer score;
    private Integer scoreMax;
    private String solution;
    private CodeSolution codeSolution;
}

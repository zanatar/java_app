package ru.tsystems.tchallenge.service.domain.workbook.assignment;


import lombok.Builder;
import lombok.Data;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.code.CodeSolutionDocument;

import java.math.BigDecimal;

@Data
@Builder
public class AssignmentDocument {
    private String problemId;
    private BigDecimal score;
    private Integer scoreMax;
    private String solution;
    private CodeSolutionDocument codeSolution;
}

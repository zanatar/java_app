package ru.tsystems.tchallenge.service.domain.workbook.scoring;

import ru.tsystems.tchallenge.codemaster.model.SubmissionResult;
import ru.tsystems.tchallenge.service.domain.workbook.WorkbookDocument;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.Assignment;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.AssignmentDocument;

import java.math.BigDecimal;
import java.util.List;

public interface WorkbookScoringService {
    /**
     * Assess all assignments and set scores
     * @param workbookDocument workbook to check
     */
    void assessWorkbook(final WorkbookDocument workbookDocument);

    /**
     * Assess code solution
     * @param assignmentDocument assignment to check
     * @param result Result of tests
     */
    void assessCodeSolution(AssignmentDocument assignmentDocument, SubmissionResult result);

    /**
     * Calculate the average score of assignments.
     * @param assignments assignments
     * @return workbook average score
     */
    BigDecimal assignmentsAvgScore(List<Assignment> assignments);
}

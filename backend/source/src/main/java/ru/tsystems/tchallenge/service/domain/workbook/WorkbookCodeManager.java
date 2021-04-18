package ru.tsystems.tchallenge.service.domain.workbook;

import ru.tsystems.tchallenge.codemaster.model.LanguageInfoList;
import ru.tsystems.tchallenge.codemaster.model.SubmissionResult;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.code.CodeSolutionInvoice;

/**
 * Manager for CRUD operations with code solutions in workbook
 */
public interface WorkbookCodeManager {

    /**
     * Update current code solution for specified assignment
     * @param workbookId The ID of workbook to update
     * @param assignmentIndex The index of assignment in workbook to update
     * @param invoice Desired update data
     * @return Updated workbook
     */
    Workbook updateCodeSolution(String workbookId, Integer assignmentIndex, CodeSolutionInvoice invoice);

    /**
     * Create async task, that upload, compile code and run tests
     * @param workbookId The ID of workbook
     * @param assignmentIndex The index of assignment in workbook to run tests
     * @return Current Test run result (probably waiting or compiling)
     */
    SubmissionResult runTests(String workbookId, Integer assignmentIndex);

    /**
     * Retrieve current status of tests run
     * @param workbookId The ID of workbook
     * @param assignmentIndex The index of assignment
     * @param lastSubmission Whether to use last submission or to use last successful submission
     * @param withSource Whether to include source code in response or not
     * @return Current Test run result
     */
    SubmissionResult getTestsResult(String workbookId, Integer assignmentIndex, Boolean lastSubmission, Boolean withSource);

    /**
     * Synchronous task, that assess all code assignments
     * @param workbookDocument
     */
    void assessAllCodeAssignments( WorkbookDocument workbookDocument );

    LanguageInfoList availableLanguages();
}

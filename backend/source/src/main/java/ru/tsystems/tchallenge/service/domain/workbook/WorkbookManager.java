package ru.tsystems.tchallenge.service.domain.workbook;

import ru.tsystems.tchallenge.service.utility.search.SearchResult;
import ru.tsystems.tchallenge.service.domain.event.HealthStatus;
import ru.tsystems.tchallenge.service.domain.maturity.Maturity;
import ru.tsystems.tchallenge.service.domain.problem.ProblemRandomInvoice;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.AssignmentUpdateInvoice;
import ru.tsystems.tchallenge.service.security.authentication.UserAuthentication;

import java.util.Set;

/**
 * Manager for CRUD operations with workbook
 */
public interface WorkbookManager {
    /**
     * Create new workbook
     * @param authentication Current user authentication
     * @param invoice Invoice to create workbook
     * @return The ID of created workbook
     */
    String create(UserAuthentication authentication, WorkbookInvoice invoice);

    /**
     * Retrieve workbook by id
     * @param id The ID of workbook
     * @return workbook
     */
    Workbook retrieveById(String id);

    /**
     * Check if workbook is reviewed by coworker
     * @param id The id of workbook
     * @return true if workbooks is reviewed
     */
    Boolean isReviewed(String id);

    Boolean isAssessed(String id);

    /**
     * Review workbook
     * @param id The id of workbook
     * @return updated workbook
     */
    Workbook reviewWorkbook(String id);

    /**
     * Update solution for specified assignment
     * @param id The ID of workbook
     * @param index Index of assignment
     * @param invoice Desired update
     */
    void updateAssignment(String id, Integer index, AssignmentUpdateInvoice invoice);

    /**
     * Checks that workbook status is updated by owner of workbook and status is legal
     * @param authentication Current user authentication
     * @param id The ID of workbook
     * @param invoice Desired update
     */
    void validateUpdateStatus(UserAuthentication authentication, String id,
                              WorkbookStatusUpdateInvoice invoice);

    /**
     * Update status of workbook
     * @param id The ID of workbook
     * @param invoice Desired update
     * @return Updated workbook
     */
    WorkbookStatus updateStatus(String id, WorkbookStatusUpdateInvoice invoice);

    Workbook assessSubmittedWorkbook(WorkbookDocument workbookDocument);

    HealthStatus getHealthStatus(Maturity maturity, String specializationPermalink, Set<String> tagIds);

    ProblemRandomInvoice problemRandomInvoice(WorkbookInvoice invoice);

    SearchResult<WorkbookInfo> retrieveSearchResult(UserAuthentication authentication, WorkbookSearchInvoice invoice);

    Boolean areWorkbooksAvailable(UserAuthentication authentication);
}

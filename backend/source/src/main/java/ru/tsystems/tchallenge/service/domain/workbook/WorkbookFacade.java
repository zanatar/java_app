package ru.tsystems.tchallenge.service.domain.workbook;

import org.springframework.stereotype.Service;
import ru.tsystems.tchallenge.codemaster.model.LanguageInfoList;
import ru.tsystems.tchallenge.codemaster.model.SubmissionResult;
import ru.tsystems.tchallenge.service.utility.search.SearchResult;
import ru.tsystems.tchallenge.service.domain.account.AccountRole;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.AssignmentUpdateInvoice;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.code.CodeSolutionInvoice;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.security.authentication.UserAuthentication;

import java.util.Collection;
import java.util.Objects;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder.forbidden;
import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_WORKBOOK;

@Service
public class WorkbookFacade {

    private final WorkbookManager workbookManager;
    private final WorkbookCodeManager workbookCodeManager;
    private final WorkbookRepository workbookRepository;

    public WorkbookFacade(WorkbookManager workbookManager, WorkbookCodeManagerBean workbookCodeManager,
                          WorkbookRepository workbookRepository) {
        this.workbookManager = workbookManager;
        this.workbookCodeManager = workbookCodeManager;
        this.workbookRepository = workbookRepository;
    }

    public String create(UserAuthentication authentication, WorkbookInvoice invoice) {
        return workbookManager.create(authentication, invoice);
    }

    public Workbook retrieveById(UserAuthentication authentication, String id) {
        if (!hasAccessToWorkbook(authentication, id)) {
            throw forbidden();
        }
        return workbookManager.retrieveById(id);
    }

    public Boolean isReviewed(UserAuthentication authentication, String id) {
        if (!hasAccessToWorkbook(authentication, id)) {
            throw forbidden();
        }
        return workbookManager.isReviewed(id);
    }

    public Boolean isAssessed(UserAuthentication authentication, String id) {
        if (!hasAccessToWorkbook(authentication, id)) {
            throw forbidden();
        }
        return workbookManager.isAssessed(id);
    }

    public Workbook reviewWorkbook(String id) {
        return workbookManager.reviewWorkbook(id);
    }


    public void updateAssignment(UserAuthentication authentication, String id,
                                 Integer index, AssignmentUpdateInvoice invoice) {
        if (!hasAccessToWorkbook(authentication, id)) {
            throw forbidden();
        }

        workbookManager.updateAssignment(id, index, invoice);
    }

    public Workbook updateCodeSolution(UserAuthentication authentication, String id,
                                   Integer index, CodeSolutionInvoice invoice) {
        if (!hasAccessToWorkbook(authentication, id)) {
            throw forbidden();
        }
        return workbookCodeManager.updateCodeSolution(id, index, invoice);
    }


    public WorkbookStatus updateStatus(UserAuthentication authentication, String id, WorkbookStatusUpdateInvoice invoice) {
        if (!hasAccessToWorkbook(authentication, id)) {
            throw forbidden();
        }

        workbookManager.validateUpdateStatus(authentication, id, invoice);
       // if (invoice.getStatus() == WorkbookStatus.SUBMITTED) {
            // TODO: as run tests can take some long time, maybe need to improve update status procedure
            // Probably in this case workbook set status to assessing and immediately return
            // Also create async task that will assess workbook
         //   workbookCodeManager.assessAllCodeAssignments(id);
      //  }
        invoice.validate();
        return workbookManager.updateStatus(id, invoice);
    }

    public LanguageInfoList availableLanguages() {
        return workbookCodeManager.availableLanguages();
    }

    public SubmissionResult runTests(String workbookId, Integer assignmentIndex) {
        return workbookCodeManager.runTests(workbookId, assignmentIndex);
    }

    public SubmissionResult getTestResults(String workbookId, Integer assignmentIndex, Boolean lastSubmission, Boolean withSource) {
        return workbookCodeManager.getTestsResult(workbookId, assignmentIndex, lastSubmission, withSource);
    }


    private boolean hasAccessToWorkbook(UserAuthentication authentication, String workbookId) {
        Collection<AccountRole> authorities = authentication.getAuthorities();
        if (authorities.contains(AccountRole.REVIEWER) || authorities.contains(AccountRole.ROBOT)) {
            return true;
        }
        //  participant can access only their workbooks
        WorkbookDocument workbook = workbookRepository.findById(workbookId).orElseThrow(() -> workbookNotFound(workbookId));
        return Objects.equals(workbook.getOwnerId(), authentication.getAccountId());
    }


    private OperationException workbookNotFound(String workbookId) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_WORKBOOK)
                .description("Workbook not found")
                .attachment(workbookId)
                .build();
    }

    public SearchResult<WorkbookInfo> retrieveSearchResult(UserAuthentication authentication, WorkbookSearchInvoice filter) {
        return workbookManager.retrieveSearchResult(authentication, filter);
    }

    public Boolean areWorkbooksAvailable(UserAuthentication authentication) {
        return workbookManager.areWorkbooksAvailable(authentication);
    }
}
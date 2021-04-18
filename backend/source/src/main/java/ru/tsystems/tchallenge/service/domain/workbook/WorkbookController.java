package ru.tsystems.tchallenge.service.domain.workbook;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.tsystems.tchallenge.codemaster.model.LanguageInfoList;
import ru.tsystems.tchallenge.codemaster.model.SubmissionResult;
import ru.tsystems.tchallenge.service.utility.search.SearchResult;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.AssignmentUpdateInvoice;
import ru.tsystems.tchallenge.service.domain.workbook.assignment.code.CodeSolutionInvoice;
import ru.tsystems.tchallenge.service.security.authentication.UserAuthentication;
import ru.tsystems.tchallenge.service.utility.data.IdAware;
import ru.tsystems.tchallenge.service.utility.data.IdContainer;

import static ru.tsystems.tchallenge.service.security.authentication.AuthenticationManager.getAuthentication;

@RestController
@RequestMapping("/workbooks/")
@PreAuthorize("hasAnyAuthority('PARTICIPANT', 'REVIEWER', 'MODERATOR')")
@Api(tags = "Workbooks management")
public class WorkbookController {

    private final WorkbookFacade workbookFacade;

    public WorkbookController(WorkbookFacade workbookFacade) {
        this.workbookFacade = workbookFacade;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PARTICIPANT')")
    @ApiOperation("Create new workbook for specified event and selected maturity and specialization")
    public IdAware create(@RequestBody WorkbookInvoice invoice) {
        return IdContainer.builder()
                .id(workbookFacade.create(getAuthentication(), invoice))
                .build();
    }


    @GetMapping("{id}")
    @ApiOperation("Retrieve workbook with specified id. Participant can only access their workbooks")
    public Workbook getById(@PathVariable String id) {
        return workbookFacade.retrieveById(getAuthentication(), id);
    }

    @GetMapping("{id}/reviewed")
    @ApiOperation("Check if workbook is reviewed by coworker.")
    public Boolean isReviewed(@PathVariable String id) {
        return workbookFacade.isReviewed(getAuthentication(), id);
    }

    @GetMapping("{id}/assessed")
    @ApiOperation("Check if workbook is assessed.")
    public Boolean isAssessed(@PathVariable String id) {
        return workbookFacade.isAssessed(getAuthentication(), id);
    }

    @PutMapping("{id}/assignments/{index}")
    @PreAuthorize("hasAuthority('PARTICIPANT')")
    @ApiOperation(value = "Set solution for specified workbooks in specified index.\n " +
            "Solution can be set only by owner of workbook")
    public void setSolution(@PathVariable("id") String id,
                            @PathVariable("index") Integer index,
                            @RequestBody AssignmentUpdateInvoice invoice) {
        workbookFacade.updateAssignment(getAuthentication(), id, index, invoice);
    }

    @PutMapping("{id}/assignments/{index}/code")
    @PreAuthorize("hasAuthority('PARTICIPANT')")
    @ApiOperation(value = "Set code solution for specified workbooks in specified index.\n " +
            "Solution can be set only by owner of workbook")
    public Workbook setCodeSolution(@PathVariable("id") String id,
                                    @PathVariable("index") Integer index,
                                    @RequestBody CodeSolutionInvoice invoice) {
        return workbookFacade.updateCodeSolution(getAuthentication(), id, index, invoice);
    }

    @PutMapping("{id}/status")
    @ApiOperation(value = "Update status of specified workbook. " +
            "Participant can only set status to their workbooks",
            notes = "Sample of using - submitting workbook")
    public WorkbookStatus updateStatus(@PathVariable String id,
                                 @RequestBody WorkbookStatusUpdateInvoice invoice) {
        return workbookFacade.updateStatus(getAuthentication(), id, invoice);
    }

    @PutMapping("/{id}/review")
    @ApiOperation(value = "Set that workbook with specified id is reviewed by coworker")
    @PreAuthorize("hasAnyAuthority('REVIEWER', 'MODERATOR')")
    public Workbook reviewWorkbook(@PathVariable String id) {
        return workbookFacade.reviewWorkbook(id);
    }


    @GetMapping("/languages")
    @ApiOperation(value = "Return list of available languages (e.g. Java, C, C++)",
            notes = "Participant should use one of this elements when attaching code to workbook")
    public LanguageInfoList availableLanguages() {
        return workbookFacade.availableLanguages();
    }


    @PutMapping("/{id}/assignments/{index}/tests")
    @ApiOperation(value = "Create task to run tests on current code solution")
    public SubmissionResult runTests(@PathVariable("id") String id, @PathVariable("index") Integer index) {
        return workbookFacade.runTests(id, index);
    }

    @GetMapping("/{id}/assignments/{index}/tests")
    @ApiOperation("Retrieve current compilation and tests status and results (for code solution)")
    public SubmissionResult getTestsResult(@PathVariable("id") String id, @PathVariable("index") Integer index,
                                           @ApiParam("whether to return last submission result or return last successfull submission result")
                                           @RequestParam(value = "lastSubmission", defaultValue = "true") Boolean lastSubmission,
                                           @ApiParam("whether to include source code in response or not")
                                           @RequestParam(value = "withSource", defaultValue = "false") Boolean withSource) {
        return workbookFacade.getTestResults(id, index, lastSubmission, withSource);
    }

    @GetMapping
    @ApiOperation("Retrieve workbooks for a given event. Participant can only access their workbooks")
    public SearchResult<WorkbookInfo> retrieveSearchResult(@RequestParam(defaultValue = "0") Integer pageIndex,
                                                           @RequestParam(defaultValue = "50") Integer pageSize,
                                                           @ApiParam("Id of event")
                                                               @RequestParam(value = "eventId", required = false) String eventId,
                                                           @ApiParam("Event caption")
                                                               @RequestParam(value = "eventCaption", required = false) String eventCaption) {
        WorkbookFilter filter = WorkbookFilter.builder()
                .eventId(eventId)
                .eventCaption(eventCaption)
                .build();
        WorkbookSearchInvoice invoice = WorkbookSearchInvoice.builder()
                .pageIndex(pageIndex)
                .pageSize(pageSize)
                .filter(filter)
                .build();
        return workbookFacade.retrieveSearchResult(getAuthentication(), invoice);
    }

    @GetMapping("/presence/")
    @ApiOperation("Check if there are workbooks that can be viewed")
    public Boolean hasWorkbooks() {
        UserAuthentication authentication = getAuthentication();
        return workbookFacade.areWorkbooksAvailable(authentication);
    }
}

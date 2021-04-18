package ru.tsystems.tchallenge.service.domain.problem;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.tsystems.tchallenge.service.utility.search.SearchResult;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/problems")
@Api(tags = "Problem management")
@PreAuthorize("hasAnyAuthority('PARTICIPANT', 'MODERATOR', 'REVIEWER')")
public class ProblemController {

    private final ProblemFacade problemFacade;

    public ProblemController(ProblemFacade problemFacade) {
        this.problemFacade = problemFacade;
    }

    @GetMapping("/")
    @ApiOperation("Retrieve all problems. Only for moderators and reviewers")
    @PreAuthorize("hasAnyAuthority('MODERATOR', 'REVIEWER')")
    public List<Problem> getAll() {
        return problemFacade.retrieveAll();
    }

    @GetMapping("/filtered/")
    @ApiOperation("Retrieve problems from given page satisfying filter")
    @PreAuthorize("hasAnyAuthority('MODERATOR', 'REVIEWER')")
    public SearchResult<Problem> get(@RequestParam Integer pageIndex,
                                     @RequestParam Integer pageSize,
                                     @RequestParam(required = false) String filter,
                                     @RequestParam(required = false) Set<ProblemDifficulty> difficulties,
                                     @RequestParam(required = false) Set<String> tagIds) {
        ProblemSearchInvoice invoice = ProblemSearchInvoice.builder()
                .pageIndex(pageIndex)
                .pageSize(pageSize)
                .filterText(filter)
                .difficulties(difficulties)
                .tagIds(tagIds)
                .build();
        return problemFacade.retrieveByFilter(invoice);
    }

    @PostMapping("/")
    @ApiOperation("Create new problem. Only for moderators")
    @PreAuthorize("hasAuthority('MODERATOR')")
    public Problem create(@RequestBody ProblemInvoice invoice) {
        return problemFacade.create(invoice);
    }

    @PostMapping("/random")
    @ApiOperation(value = "Retrieve random problems, that satisfying invoice", notes = "Used for creating workbook")
    public List<Problem> getRandom(@RequestBody
                                   @ApiParam("Problem filter. Contains problem difficulty, " +
                                           "categories and number of problems to be returned")
                                           ProblemRandomInvoice invoice) {
        return problemFacade.retrieveRandom(invoice);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MODERATOR')")
    @ApiOperation("Modify problem. Only for moderators")
    public Problem updateProblem(@PathVariable
                                 @ApiParam("Problem id to be updated")
                                         String id,
                                 @ApiParam("Desired problem data")
                                 @RequestBody ProblemInvoice invoice) {
        return problemFacade.update(id, invoice);
    }


}

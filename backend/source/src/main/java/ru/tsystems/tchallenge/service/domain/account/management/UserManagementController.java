package ru.tsystems.tchallenge.service.domain.account.management;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.tsystems.tchallenge.service.utility.search.SearchInvoice;
import ru.tsystems.tchallenge.service.utility.search.SearchResult;
import ru.tsystems.tchallenge.service.domain.account.Account;
import ru.tsystems.tchallenge.service.domain.account.AccountRole;
import ru.tsystems.tchallenge.service.domain.account.AccountStatus;

import java.util.List;

@RestController
@RequestMapping("/accounts/")
@PreAuthorize("hasAuthority('ADMIN')")
@Api(tags = "Account management")
public class UserManagementController {
    private final UserManagementManager userManagementManager;

    public UserManagementController(UserManagementManager userManagementManager) {
        this.userManagementManager = userManagementManager;
    }

    @GetMapping
    @ApiOperation("Retrieve all existing accounts")
    public List<Account> getAll() {
        return userManagementManager.findAll();
    }

    @PostMapping("filtered/")
    @ApiOperation("Retrieve accounts by filter")
    public SearchResult<Account> get(@RequestBody SearchInvoice<UserFilterKey> invoice) {
        return userManagementManager.find(invoice);
    }

    @GetMapping("statuses")
    @ApiOperation("Return all account statuses")
    public AccountStatus[] getStatuses() {
        return AccountStatus.values();
    }

    @GetMapping("roles")
    @ApiOperation("Return all account security roles")
    public AccountRole[] getRoles() {
        return AccountRole.values();
    }

    @PutMapping("{id}")
    @ApiOperation("Update specified account")
    public Account update(@PathVariable
                          @ApiParam("Account id to update")
                                  String id,
                          @RequestBody
                          @ApiParam("Desired account data") AccountUpdateInvoice invoice) {
        return userManagementManager.update(id, invoice);
    }

    @PostMapping
    @ApiOperation(value = "Create new account",
            notes = "This operation allow to create any participant/coworker/bot user, immediately set personality fields " +
                    "in contrast to registration")
    public Account create(@RequestBody AccountCreateInvoice invoice) {
        return userManagementManager.create(invoice);
    }

    @PostMapping("{id}/send")
    @ApiOperation(value = "Send email")
    public void sendEmail(@PathVariable
                              @ApiParam("Account id to send Email to")
                                      String id, @RequestBody EmailInvoice invoice) {
        userManagementManager.sendEmail(id, invoice);
    }
}

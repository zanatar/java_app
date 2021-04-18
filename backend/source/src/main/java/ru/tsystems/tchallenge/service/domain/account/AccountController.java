package ru.tsystems.tchallenge.service.domain.account;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.tsystems.tchallenge.service.security.authentication.UserAuthentication;

import static ru.tsystems.tchallenge.service.security.authentication.AuthenticationManager.getAuthentication;

@RestController
@RequestMapping("/accounts/current")
@PreAuthorize("hasAnyAuthority('PARTICIPANT')")
@Api(tags = "Participant account operations")
public class AccountController {

    private final AccountManager accountManager;

    @Autowired
    public AccountController(AccountManager accountManager) {
        this.accountManager = accountManager;
    }


    @GetMapping
    @ApiOperation(value = "Retrieve current (authenticated) account")
    public Account getCurrent() {
        return accountManager.retrieveCurrent(getAuthentication());
    }

    @PutMapping("/password")
    @ApiOperation(value = "Update password of current account")
    public void updatePassword(@RequestBody
                               @ApiParam("Invoice contains old password and desired password")
                                       AccountPasswordUpdateInvoice invoice) {
        accountManager.updateCurrentPassword(getAuthentication(), invoice);
    }

    @PutMapping("/email")
    @ApiOperation(value = "Update email of current account")
    public void updateEmail(@RequestBody EmailSetInvoice invoice) {
        UserAuthentication authentication = getAuthentication();
        accountManager.setEmail(authentication, invoice);
    }

    @PutMapping("/personality")
    @ApiOperation(value = "Update account personality (first name, quick name, etc.)")
    public AccountPersonality updatePersonality(@RequestBody
                                  @ApiParam("Desired account personality")
                                          AccountPersonality personality) {
        return accountManager.updatePersonality(getAuthentication(), personality);
    }

    @PutMapping("/participantPersonality")
    @ApiOperation(value = "Update account contacts (participant personality) such as github, hh, etc")
    public ParticipantPersonality updateContacts(@RequestBody
                           @ApiParam("Desired account participant personality")
                           ParticipantPersonality participantPersonality) {
        return accountManager.updateContacts(getAuthentication(), participantPersonality);
    }

    @PutMapping("/status")
    @ApiOperation(value = "Set new account status",
            notes = "Can't set illegal statuses like suspended and banned")
    public void updateStatus(@RequestBody AccountStatusUpdateInvoice invoice) {
        accountManager.updateCurrentStatus(getAuthentication(), invoice);
    }


}

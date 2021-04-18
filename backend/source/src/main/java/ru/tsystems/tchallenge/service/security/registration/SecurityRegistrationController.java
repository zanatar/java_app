package ru.tsystems.tchallenge.service.security.registration;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/security/registrations/")
@Api(tags = "Sign up")
public class SecurityRegistrationController {

    private final SecurityRegistrationFacade securityRegistrationFacade;

    @Autowired
    public SecurityRegistrationController(SecurityRegistrationFacade securityRegistrationFacade) {
        this.securityRegistrationFacade = securityRegistrationFacade;
    }

    @PostMapping
    @ApiOperation("Create new participant account")
    public SecurityRegistration register(@RequestBody SecurityRegistrationInvoice invoice) {
        return securityRegistrationFacade.createAndSendVoucher(invoice);
    }

    @GetMapping("emailValidator")
    @ApiOperation(value = "Check if email already binded to other account",
            notes = "Needs to validate email on client side")
    public Boolean isEmailFree(@RequestParam("email") String email) {
        return securityRegistrationFacade.isEmailFree(email);
    }
}

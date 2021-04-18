package ru.tsystems.tchallenge.service.security.token;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.security.authentication.AuthenticationInvoice;
import ru.tsystems.tchallenge.service.security.authentication.AuthenticationManager;
import ru.tsystems.tchallenge.service.security.authentication.AuthenticationMethod;
import ru.tsystems.tchallenge.service.security.authentication.UserAuthentication;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_INTERNAL;
import static ru.tsystems.tchallenge.service.security.authentication.AuthenticationManager.getAuthentication;

@RestController
@RequestMapping("/security/tokens/")
@Api(tags = "Sign in")
public class TokenController {

    private final TokenFacade tokenFacade;
    private final AuthenticationManager authenticationManager;

    public TokenController(TokenFacade tokenFacade, AuthenticationManager authenticationManager) {
        this.tokenFacade = tokenFacade;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping
    @ApiOperation(value = "Create new token by checking account log/password or using voucher",
    notes = "Token need to be attached to header to every authorized requests")
    public SecurityToken createToken(@RequestBody AuthenticationInvoice invoice) {
        UserAuthentication authentication;
        if (invoice.getMethod() == AuthenticationMethod.PASSWORD) {
            authentication = authenticationManager.authenticateByPassword(invoice);
        } else if (invoice.getMethod() == AuthenticationMethod.VOUCHER) {
            authentication = authenticationManager.authenticateByVoucher(invoice);
        } else if (invoice.getMethod() == AuthenticationMethod.GOOGLE) {
            authentication = authenticationManager.authenticateByGoogleToken(invoice);
        } else if (invoice.getMethod() == AuthenticationMethod.VK) {
            authentication = authenticationManager.authenticateByVK(invoice);
        } else {
            throw unknownAuthMethod(invoice.getMethod());
        }
        return tokenFacade.createForCurrentAccount(authentication);

    }

    @GetMapping("validator")
    @ApiOperation("Check if token is valid and not expired")
    public boolean isValid() {
        return getAuthentication() != null;
    }

    @GetMapping("current")
    @ApiOperation(value = "Retrieve current token", authorizations = @Authorization(value = "bearer"))
    @PreAuthorize("authentication.authenticated")
    public SecurityToken getCurrent() {
        return tokenFacade.retrieveCurrent(getAuthentication());
    }

    @DeleteMapping("current")
    @PreAuthorize("authentication.authenticated")
    @ApiOperation(value = "Delete current token", authorizations = @Authorization(value = "bearer"))
    public void deleteCurrent() {
        tokenFacade.deleteCurrent(getAuthentication());
    }

    private OperationException unknownAuthMethod(AuthenticationMethod authMethod) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_INTERNAL)
                .description("Unknown auth method")
                .attachment(authMethod)
                .build();
    }

}

package ru.tsystems.tchallenge.service.domain.account;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.security.authentication.UserAuthentication;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.*;
import static ru.tsystems.tchallenge.service.security.authentication.AuthenticationManager.notAuthenticated;

@Service
@Log4j2
public class AccountManager {

    private AccountPasswordHashEngine accountPasswordHashEngine;
    private AccountPasswordValidator accountPasswordValidator;
    private AccountRepository accountRepository;
    private final AccountConverter accountConverter;

    @Autowired
    public AccountManager(AccountPasswordHashEngine accountPasswordHashEngine,
                          AccountPasswordValidator accountPasswordValidator, AccountRepository accountRepository,
                          AccountConverter accountConverter) {
        this.accountConverter = accountConverter;
        this.accountPasswordHashEngine = accountPasswordHashEngine;
        this.accountPasswordValidator = accountPasswordValidator;
        this.accountRepository = accountRepository;
    }

    public Account retrieveCurrent(UserAuthentication authentication) {
        final String id = authenticatedAccountId(authentication);
        final AccountDocument accountDocument = accountRepository.findById(id)
                .orElseThrow(() -> notAuthenticated(id));
        return accountConverter.toMgmtDto(accountDocument);
    }

    public void updateCurrentPassword(UserAuthentication authentication, final AccountPasswordUpdateInvoice invoice) {
        invoice.validate();
        accountPasswordValidator.validate(invoice.getDesired());
        final String id = authenticatedAccountId(authentication);
        final AccountDocument accountDocument = accountRepository.findById(id)
                .orElseThrow(() -> notAuthenticated(id));
        final String passwordHash = accountDocument.getPasswordHash();
        if (!accountPasswordHashEngine.match(invoice.getCurrent(), passwordHash)) {
            throw passwordDoesNotMatch();
        }
        accountDocument.setPasswordHash(accountPasswordHashEngine.hash(invoice.getDesired()));
        accountRepository.save(accountDocument);
        log.info("Updated account password" + accountDocument.getId());
    }
    public AccountPersonality updatePersonality(UserAuthentication authentication,
                                  final AccountPersonality personality) {
        personality.registerViolations();
        final String id = authenticatedAccountId(authentication);
        AccountDocument accountDocument = accountRepository.findById(id).orElseThrow(() -> accountIsMissing(id));

        AccountPersonalityDocument personalityDocument = accountConverter.fromPersonality(personality);
        accountDocument.setPersonality(personalityDocument);

        accountRepository.save(accountDocument);
        log.info("Updated account personality data" + accountDocument.getId());
        return accountConverter.toPersonality(accountDocument.getPersonality());
    }

    public ParticipantPersonality updateContacts(UserAuthentication authentication,
                                                 final ParticipantPersonality personality) {
        personality.registerViolations();
        final String id = authenticatedAccountId(authentication);
        AccountDocument accountDocument = accountRepository.findById(id).orElseThrow(() -> accountIsMissing(id));

        ParticipantPersonalityDocument personalityDocument = accountConverter.fromParticipantPersonality(personality);
        accountDocument.setParticipantPersonality(personalityDocument);

        accountRepository.save(accountDocument);
        log.info("Updated account contact data" + accountDocument.getId());
        return accountConverter.toParticipantPersonality(accountDocument.getParticipantPersonality());
    }



    public void updateCurrentStatus(UserAuthentication authentication, final AccountStatusUpdateInvoice invoice) {
        invoice.validate();
        final String id = authenticatedAccountId(authentication);
        final AccountDocument accountDocument = accountRepository.findById(id)
                .orElseThrow(() -> notAuthenticated(id));
        if (invoice.getNewStatus() != AccountStatus.DELETED) {
            throw statusIsNotPermitted(invoice.getNewStatus());
        }
        accountDocument.setStatus(invoice.getNewStatus());
        accountRepository.save(accountDocument);
        log.info("Updated account status " + accountDocument.getId() + " " + accountDocument.getStatus());
    }

    private String authenticatedAccountId(UserAuthentication authentication) {
        return authentication.getAccountId();
    }

    private OperationException passwordDoesNotMatch() {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_UPD_PASS)
                .description("Current password does not match")
                .build();
    }

    private OperationException statusIsNotPermitted(AccountStatus status) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_UPD_STATUS)
                .description("Status is not permitted")
                .attachment(status)
                .build();
    }

    private OperationException accountIsMissing(String id) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_ACC)
                .description("Account is missing")
                .attachment(id)
                .build();
    }

    public void setEmail(UserAuthentication authentication, EmailSetInvoice invoice) {
        invoice.validate();
        final String id = authenticatedAccountId(authentication);
        final AccountDocument accountDocument = accountRepository.findById(id)
                .orElseThrow(() -> notAuthenticated(id));
        accountDocument.setEmail(invoice.getEmail());
        accountRepository.save(accountDocument);
        log.info("Updated account email" + accountDocument.getId());
    }
}

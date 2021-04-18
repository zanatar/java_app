package ru.tsystems.tchallenge.service.domain.account;

import com.google.common.collect.Sets;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.utility.data.IdAware;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.*;

@Component
@Log4j2
public class AccountSystemManager {

    private final AccountConverter accountConverter;
    private AccountPasswordHashEngine accountPasswordHashEngine;
    private AccountPasswordValidator accountPasswordValidator;
    private AccountRepository accountRepository;

    @Autowired
    public AccountSystemManager(AccountConverter accountConverter,
                                AccountPasswordHashEngine accountPasswordHashEngine,
                                AccountPasswordValidator accountPasswordValidator,
                                AccountRepository accountRepository) {
        this.accountConverter = accountConverter;
        this.accountPasswordHashEngine = accountPasswordHashEngine;
        this.accountPasswordValidator = accountPasswordValidator;
        this.accountRepository = accountRepository;
    }

    public IdAware register(final AccountInvoice invoice) {
        invoice.setCategory(AccountCategory.PARTICIPANT);
        invoice.setRoles(EnumSet.of(AccountRole.PARTICIPANT));
        invoice.setParticipantPersonality(new ParticipantPersonality());
        return create(invoice);
    }

    public AccountDocument create(final AccountInvoice invoice) {
        invoice.validate();
        validateCategoryAndRoles(invoice);
        if ((invoice.getEmail()!= null) && (accountRepository.findByEmailIgnoreCase(invoice.getEmail()) != null)) {
            throw emailAlreadyExistsException(invoice.getEmail());
        }
        AccountDocument account = AccountDocument.builder()
                .email(invoice.getEmail())
                .status(AccountStatus.CREATED)
                .passwordHash(accountPasswordHashEngine.hash(UUID.randomUUID().toString()))
                .personality(accountConverter.fromPersonality(invoice.getPersonality()))
                .participantPersonality(
                        accountConverter.fromParticipantPersonality(invoice.getParticipantPersonality()))
                .category(invoice.getCategory())
                .roles(invoice.getRoles())
                .vkId(invoice.getVkId())
                .registeredAt(Instant.now())
                .build();
        accountRepository.save(account);
        return account;
    }

    private void validateCategoryAndRoles(AccountInvoice invoice) {
        invoice.validate();
        AccountCategory category = invoice.getCategory();
        if (category == AccountCategory.PARTICIPANT) {
            if (!Sets.difference(invoice.getRoles(), EnumSet.of(AccountRole.PARTICIPANT)).isEmpty()) {
                throw invalidRolesInCategory(invoice.getRoles());
            }
        } else if(category == AccountCategory.COWORKER) {
            if (invoice.getRoles().contains(AccountRole.PARTICIPANT) || invoice.getRoles().contains(AccountRole.ROBOT)) {
                throw invalidRolesInCategory(invoice.getRoles());
            }
        } else {
            if (!Sets.difference(invoice.getRoles(), EnumSet.of(AccountRole.ROBOT)).isEmpty()) {
                throw invalidRolesInCategory(invoice.getRoles());
            }
        }
    }


    public void updatePassword(final String id, final String password) {
        accountPasswordValidator.validate(password);
        final AccountDocument accountDocument = accountRepository.findById(id)
                .orElseThrow(() -> accountNotFound(id));
        final String passwordHash = accountPasswordHashEngine.hash(password);
        accountDocument.setPasswordHash(passwordHash);
        accountRepository.save(accountDocument);
    }

    public void verifyAccount(final String id) {
        final AccountDocument accountDocument = accountRepository.findById(id)
                .orElseThrow(() -> accountNotFound(id));
        accountDocument.setStatus(AccountStatus.APPROVED);
        accountRepository.save(accountDocument);
        log.info("Verified account " + id);
    }

    public Account findByEmail(final String email) {
        final AccountDocument document = accountRepository.findByEmailIgnoreCase(email);
        return accountConverter.toDtoShort(document);
    }

    public Account findById(final String id) {
        final AccountDocument document = accountRepository.findById(id)
                .orElse(null);
        return accountConverter.toDtoShort(document);
    }

    public Account findByVkId(final String vkId) {
        final AccountDocument document = accountRepository.findByVkId(vkId);
        return accountConverter.toDtoShort(document);
    }

    public AccountPersonality personalityById(final String id) {
        AccountDocument accountDocument = accountRepository.findById(id).orElse(null);
        return accountConverter.toPersonality(accountDocument != null ? accountDocument.getPersonality() : null);
    }

    private OperationException emailAlreadyExistsException(String email) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_REG_EMAIL)
                .description("Account with such email already exists")
                .attachment(email)
                .build();
    }

    private OperationException invalidRolesInCategory(Set<AccountRole> roles) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_ACC_CAT)
                .description("Invalid set of roles for specified category")
                .attachment(roles)
                .build();
    }

    private OperationException accountNotFound(String id) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_INTERNAL)
                .description("Account not found")
                .attachment(id)
                .build();
    }
}

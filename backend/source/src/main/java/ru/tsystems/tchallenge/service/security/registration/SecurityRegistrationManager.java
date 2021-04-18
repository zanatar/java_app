package ru.tsystems.tchallenge.service.security.registration;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.tsystems.tchallenge.service.domain.account.AccountCategory;
import ru.tsystems.tchallenge.service.domain.account.AccountInvoice;
import ru.tsystems.tchallenge.service.domain.account.AccountPersonality;
import ru.tsystems.tchallenge.service.domain.account.AccountSystemManager;
import ru.tsystems.tchallenge.service.utility.data.IdAware;

@Component
@Log4j2
public class SecurityRegistrationManager{

    private AccountSystemManager accountSystemManager;

    @Autowired
    public SecurityRegistrationManager(AccountSystemManager accountSystemManager) {
        this.accountSystemManager = accountSystemManager;
    }

    public SecurityRegistration create(final SecurityRegistrationInvoice invoice) {
        invoice.setAccountCategory(AccountCategory.PARTICIPANT);
        invoice.validate();

        AccountPersonality accountPersonality = new AccountPersonality();
        accountPersonality.setQuickname(invoice.getQuickname());
        final AccountInvoice accountInvoice = AccountInvoice.builder()
                .email(invoice.getEmail())
                .personality(accountPersonality)
                .build();
        IdAware idAware = accountSystemManager.register(accountInvoice);
        return SecurityRegistration.builder()
                .id(idAware.getId())
                .build();
    }

    public Boolean isEmailFree(String email) {
        return accountSystemManager.findByEmail(email) == null;
    }

    public SecurityRegistration createWithGoogle(String email, String firstName, String lastName) {
        AccountPersonality accountPersonality = new AccountPersonality();
        String quickName = email.substring(0, email.indexOf('@'));
        accountPersonality.setQuickname(quickName);
        accountPersonality.setFirstname(firstName);
        accountPersonality.setLastname(lastName);
        final AccountInvoice accountInvoice = AccountInvoice.builder()
                .email(email)
                .personality(accountPersonality)
                .build();
        IdAware idAware = accountSystemManager.register(accountInvoice);
        log.info("Created account with Google " + email);
        accountSystemManager.verifyAccount(idAware.getId());
        return SecurityRegistration.builder()
                .id(idAware.getId())
                .build();
    }

    public SecurityRegistration createWithVK(String vkId, String firstName, String lastName) {
        AccountPersonality accountPersonality = new AccountPersonality();
        accountPersonality.setQuickname(firstName + " " + lastName);
        accountPersonality.setFirstname(firstName);
        accountPersonality.setLastname(lastName);
        final AccountInvoice accountInvoice = AccountInvoice.builder()
                .vkId(vkId)
                .personality(accountPersonality)
                .build();
        IdAware idAware = accountSystemManager.register(accountInvoice);
        log.info("Created account with VK " + vkId);
        accountSystemManager.verifyAccount(idAware.getId());
        return SecurityRegistration.builder()
                .id(idAware.getId())
                .build();
    }
}

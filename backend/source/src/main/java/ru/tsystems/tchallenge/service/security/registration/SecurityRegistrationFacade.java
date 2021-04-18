package ru.tsystems.tchallenge.service.security.registration;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.tsystems.tchallenge.service.security.voucher.SecurityVoucher;
import ru.tsystems.tchallenge.service.security.voucher.SecurityVoucherManager;
import ru.tsystems.tchallenge.service.utility.mail.MailData;
import ru.tsystems.tchallenge.service.utility.mail.TemplateMailInvoice;
import ru.tsystems.tchallenge.service.utility.mail.TemplateMailManager;

@Component
@Log4j2
public class SecurityRegistrationFacade {

    private SecurityRegistrationManager securityRegistrationManager;
    private SecurityVoucherManager securityVoucherManager;
    private TemplateMailManager templateMailManager;

    @Autowired
    public SecurityRegistrationFacade(SecurityRegistrationManager securityRegistrationManager,
                                      SecurityVoucherManager securityVoucherManager,
                                      TemplateMailManager templateMailManager) {
        this.securityRegistrationManager = securityRegistrationManager;
        this.securityVoucherManager = securityVoucherManager;
        this.templateMailManager = templateMailManager;
    }

    public SecurityRegistration createAndSendVoucher(SecurityRegistrationInvoice invoice) {
        final SecurityRegistration result = securityRegistrationManager.create(invoice);
        createVoucherAndSend(invoice);
        log.info("Created participant account " + invoice.getEmail());
        return result;
    }

    public Boolean isEmailFree(String email) {
        return securityRegistrationManager.isEmailFree(email);
    }

    private SecurityVoucher createVoucherAndSend(final SecurityRegistrationInvoice invoice) {
        final SecurityVoucher voucher = securityVoucherManager.create(invoice.getEmail(), false);

        final String backlink = templateMailManager.createBacklink(invoice.getAccountCategory(),
                invoice.getBacklinkPathTemplate(), voucher);
        MailData mail = MailData.builder()
                .backlink(backlink)
                .email(invoice.getEmail())
                .supportEmail(templateMailManager.getSupportEmail())
                .logoPath(templateMailManager.createLogoPath(invoice.getAccountCategory()))
                .build();

        final TemplateMailInvoice templateMailInvoice = TemplateMailInvoice.builder()
                .email(invoice.getEmail())
                .subject("T-Challenge: Регистрация")
                .templateName("account-created")
                .data(mail)
                .build();
        templateMailManager.sendAsync(templateMailInvoice);
        return voucher;
    }

}

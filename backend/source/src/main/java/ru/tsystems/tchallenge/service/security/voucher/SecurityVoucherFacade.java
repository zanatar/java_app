package ru.tsystems.tchallenge.service.security.voucher;

import com.google.common.base.Strings;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import ru.tsystems.tchallenge.service.domain.account.Account;
import ru.tsystems.tchallenge.service.domain.account.AccountPersonality;
import ru.tsystems.tchallenge.service.domain.account.AccountSystemManager;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.utility.mail.MailData;
import ru.tsystems.tchallenge.service.utility.mail.TemplateMailInvoice;
import ru.tsystems.tchallenge.service.utility.mail.TemplateMailManager;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_ACC;

@Component
@Log4j2
public class SecurityVoucherFacade {

    private SecurityVoucherManager securityVoucherManager;
    private TemplateMailManager templateMailManager;
    private AccountSystemManager accountSystemManager;


    public SecurityVoucherFacade(SecurityVoucherManager securityVoucherManager,
                                 TemplateMailManager templateMailManager, AccountSystemManager accountSystemManager) {
        this.securityVoucherManager = securityVoucherManager;
        this.templateMailManager = templateMailManager;
        this.accountSystemManager = accountSystemManager;
    }

    public SecurityVoucher createAndSend(SecurityVoucherInvoice invoice) {
        Account account = accountSystemManager.findByEmail(invoice.getEmail());
        if (account == null) {
            throw accountNotFound(invoice.getEmail());
        }
        final SecurityVoucher voucher = securityVoucherManager.create(invoice.getEmail(), invoice.getResetPassword());
        String backlink = templateMailManager.createBacklink(account.getCategory(), invoice.getBacklinkTemplate(), voucher);
        final MailData mailData = MailData.builder()
                .backlink(backlink)
                .logoPath(templateMailManager.createLogoPath(account.getCategory()))
                .name(getName(account))
                .email(invoice.getEmail())
                .supportEmail(templateMailManager.getSupportEmail())
                .build();

        final TemplateMailInvoice templateMailInvoice = TemplateMailInvoice.builder()
                .email(invoice.getEmail())
                .subject(subjectByInvoice(invoice))
                .templateName(templateNameByInvoice(invoice))
                .data(mailData)
                .build();

        log.info("Sending voucher to " + invoice.getEmail());
        templateMailManager.sendAsync(templateMailInvoice);
        return voucher;
    }


    private String getName(Account account) {
        AccountPersonality personality = account.getPersonality();
        return Strings.isNullOrEmpty(personality.getFirstname()) ?
                personality.getQuickname() : personality.getFirstname();
    }

    private String templateNameByInvoice(SecurityVoucherInvoice invoice) {
        if ((invoice.getResend() != null) && invoice.getResend()) {
            return "account-created";
        }
        return invoice.getResetPassword() == null || invoice.getResetPassword() ? "account-reset" : "security-voucher-created";
    }

    private String subjectByInvoice(SecurityVoucherInvoice invoice) {
        if ((invoice.getResend() != null) && invoice.getResend()) {
            return "T-Challenge: Регистрация";
        }
        return invoice.getResetPassword() == null || invoice.getResetPassword() ? "T-Challenge: Сброс пароля" :
                "T-Challenge: Подтверждение аккаунта";
    }

    public SecurityVoucher getByPayload(String payload) {
        return securityVoucherManager.getByPayload(payload);
    }


    private OperationException accountNotFound(String email) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_ACC)
                .description("Account with specified email not found")
                .attachment(email)
                .build();
    }
}

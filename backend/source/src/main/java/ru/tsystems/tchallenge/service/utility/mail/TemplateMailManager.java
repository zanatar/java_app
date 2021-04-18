package ru.tsystems.tchallenge.service.utility.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.tsystems.tchallenge.service.domain.account.AccountCategory;
import ru.tsystems.tchallenge.service.utility.batch.BatchManager;
import ru.tsystems.tchallenge.service.utility.template.TemplateManager;

@Component
public class TemplateMailManager {

    private BatchManager batchManager;
    private MailManager mailManager;
    private TemplateManager templateManager;

    @Value("${tchallenge.participant.url}")
    private String participantUrl;
    @Value("${tchallenge.coworker.url}")
    private String coworkerUrl;


    @Autowired
    public TemplateMailManager(BatchManager batchManager, MailManager mailManager, TemplateManager templateManager) {
        this.batchManager = batchManager;
        this.mailManager = mailManager;
        this.templateManager = templateManager;
    }

    public String createBacklink(AccountCategory accountCategory, String pathTemplate, Object model) {
        String baseUrl = getBaseUrl(accountCategory);
        String template = baseUrl + pathTemplate;
        return templateManager.renderInline(template, model);
    }

    private String getBaseUrl(AccountCategory accountCategory) {
        if (accountCategory == AccountCategory.PARTICIPANT) {
            return participantUrl;
        } else if (accountCategory == AccountCategory.COWORKER) {
            return coworkerUrl;
        } else {
            throw new UnsupportedOperationException("Voucher supports only for coworker and participant clients");
        }
    }

    public String createLogoPath(AccountCategory accountCategory) {
        String baseUrl = getBaseUrl(accountCategory);
        return baseUrl + "/assets/images/t-systems.png";
    }

    public String getSupportEmail() {
        return mailManager.getOrigin();
    }

    public void sendAsync(TemplateMailInvoice invoice) {
        this.batchManager.submit(() -> send(invoice));
    }

    public void send(TemplateMailInvoice invoice) {
        String templatePath = templatePath(invoice.getTemplateName());
        String content = this.templateManager.render(templatePath, invoice.getData());
        MailInvoice mailInvoice = MailInvoice.builder()
                .email(invoice.getEmail())
                .subject(invoice.getSubject())
                .content(content)
                .build();
        this.mailManager.send(mailInvoice);
    }

    private String templatePath(final String templateName) {
        return "mail/templates/" + templateName;
    }
}

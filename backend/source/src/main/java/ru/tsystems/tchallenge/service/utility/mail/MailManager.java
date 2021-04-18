package ru.tsystems.tchallenge.service.utility.mail;

import com.sendgrid.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;

import javax.mail.internet.MimeMessage;
import java.io.IOException;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_INTERNAL;


@Log4j2
@Component
public class MailManager {


    private final JavaMailSender javaMailSender;
    private final SendGrid sendgrid;
    private String mime;


    @Value("${spring.mail.username}")
    private String origin;
    @Value("${tchallenge.mail.sendgrid-enabled}")
    private String sendgridEnabled;
    @Value("${spring.sendgrid.api-key}")
    private String sendgridApiKey;


    @Autowired
    public MailManager(JavaMailSender javaMailSender, SendGrid sendgrid) {
        this.javaMailSender = javaMailSender;
        this.sendgrid = sendgrid;
        this.mime = "text/html";
    }

    public String getOrigin() {
        return origin;
    }


    public void send(final MailInvoice invoice) {
        if (Boolean.parseBoolean(sendgridEnabled)) {
            sendViaSendgrid(invoice);
        } else {
            sendViaLocalService(invoice);
        }
    }

    private void sendViaLocalService(final MailInvoice invoice) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false);

            helper.setFrom(origin);
            helper.setTo(invoice.getEmail());
            helper.setSubject(invoice.getSubject());
            helper.setText(invoice.getContent(), true);

            javaMailSender.send(message);
        } catch (final Exception exception) {
            throw wrapped(exception);
        }
    }

    private void sendViaSendgrid(final MailInvoice invoice) {
        try {
            final Request request = sendgridRequest(invoice);
            log.info("Sending email via sendgrid to " + invoice.getEmail());
            sendgrid.api(request);
        } catch (final IOException exception) {
            throw wrapped(exception);
        }
    }

    private Request sendgridRequest(final MailInvoice invoice) {
        try {
            final Request result = new Request();
            final Mail mail = sendgridMail(invoice);
            result.setMethod(Method.POST);
            result.setEndpoint("mail/send");
            result.setBody(mail.build());
            return result;
        } catch (final IOException exception) {
            log.error(exception);
            throw wrapped(exception);
        }
    }

    private Mail sendgridMail(final MailInvoice invoice) {
        final Email from = new Email(origin);
        final String subject = invoice.getSubject();
        final Email to = new Email(invoice.getEmail());
        final Content content = new Content(mime, invoice.getContent());
        return new Mail(from, subject, to, content);
    }

    private OperationException wrapped(final Exception exception) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_INTERNAL)
                .description("Error occurred at sending email")
                .cause(exception)
                .build();
    }
}

package ru.tsystems.tchallenge.service.utility.template;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import org.springframework.stereotype.Component;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_INTERNAL;


@Component
public class TemplateManager {

    private Handlebars handlebars = new Handlebars(new ClassPathTemplateLoader());

    public String render(final String path, final Object model) {
        try {
            return this.handlebars.compile(path).apply(model);
        } catch (Exception exception) {
            throw wrapped(exception, path);
        }
    }

    public String renderInline(final String template, final Object model) {
        try {
            return this.handlebars.compileInline(template).apply(model);
        } catch (Exception exception) {
            throw wrapped(exception, template);
        }
    }

    private OperationException wrapped(final Exception exception, Object attachment) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_INTERNAL)
                .description("Error occurred at sending email")
                .attachment(attachment)
                .cause(exception)
                .build();
    }
}

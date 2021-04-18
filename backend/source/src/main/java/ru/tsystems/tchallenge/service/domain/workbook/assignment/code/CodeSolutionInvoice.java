package ru.tsystems.tchallenge.service.domain.workbook.assignment.code;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.tsystems.tchallenge.codemaster.model.Language;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder.missing;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CodeSolutionInvoice implements ValidationAware {

    String code;
    Language language;

    @Override
    public void registerViolations() {
        if (Strings.isNullOrEmpty(code)) {
            throw missing(OperationExceptionType.ERR_ASSIGNMENT_UPDATE_CODE, "code");
        }
        if (language == null) {
            throw missing(OperationExceptionType.ERR_ASSIGNMENT_UPDATE_LANG, "language");
        }
    }
}

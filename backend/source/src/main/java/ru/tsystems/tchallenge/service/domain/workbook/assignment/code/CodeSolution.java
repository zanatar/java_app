package ru.tsystems.tchallenge.service.domain.workbook.assignment.code;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.tsystems.tchallenge.codemaster.model.Language;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CodeSolution {
    String code;
    Language language;
    String submissionId;
    String lastSuccessfulSubmissionId;
}

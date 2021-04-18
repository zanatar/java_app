package ru.tsystems.tchallenge.service.domain.workbook.assignment.code;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.tsystems.tchallenge.codemaster.model.Language;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CodeSolutionDocument {
    String code;
    Language language;
    String submissionId;
    String lastSuccessfulSubmissionId;
}

package ru.tsystems.tchallenge.service.domain.workbook.assignment;

import lombok.Data;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

@Data
public final class AssignmentUpdateInvoice implements ValidationAware {

    private String solution;

    @Override
    public void registerViolations() {
    }

}

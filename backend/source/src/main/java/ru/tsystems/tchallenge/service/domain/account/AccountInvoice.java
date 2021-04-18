package ru.tsystems.tchallenge.service.domain.account;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

import java.util.Set;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountInvoice implements ValidationAware {

    private String email;
    private AccountCategory category;
    private Set<AccountRole> roles;
    private ParticipantPersonality participantPersonality;
    private AccountPersonality personality;
    private String vkId;


    @Override
    public void registerViolations() {
        if ((vkId == null) && ((Strings.isNullOrEmpty(email) || !validateEmail(email)))) {
            throw emailIsInvalid(email);
        }

        if (category == null) {
            throw categoryIsInvalid();
        }

        if (roles == null || roles.isEmpty()) {
            throw rolesAreRequired();
        }

        personality.registerViolations();
        if (category == AccountCategory.PARTICIPANT) {
            if (participantPersonality == null) {
                throw participantPersonalityNotFound();
            }
            participantPersonality.registerViolations();
        }
    }

    private OperationException emailIsInvalid(String email) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_ACC_EMAIL)
                .attachment(email)
                .description("Email is invalid")
                .build();
    }

    private OperationException categoryIsInvalid() {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_ACC_CAT)
                .description("Category is required")
                .build();
    }

    private OperationException rolesAreRequired() {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_ACC_ROLES)
                .description("Roles are required")
                .build();
    }

    private OperationException participantPersonalityNotFound() {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_PARTICIPANT_PERSONALITY)
                .description("Roles are required")
                .build();
    }
}

package ru.tsystems.tchallenge.service.domain.account;

import lombok.Data;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

@Data
public class AccountUpdateInvoice implements ValidationAware {
    private AccountPersonality personality;
    private ParticipantPersonality participantPersonality;

    @Override
    public void registerViolations() {
        if (personality != null) {
            personality.registerViolations();
        }
        if (participantPersonality != null) {
            participantPersonality.registerViolations();
        }

    }

}

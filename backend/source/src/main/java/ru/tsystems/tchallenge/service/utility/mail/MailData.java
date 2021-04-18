package ru.tsystems.tchallenge.service.utility.mail;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Value
@Builder
public final class MailData {
    String backlink;
    String supportEmail;
    String email;
    String logoPath;
    String name;
    String event;
    String specialization;
    String maturity;
    Integer solveTime;
    String[] customContent;
}

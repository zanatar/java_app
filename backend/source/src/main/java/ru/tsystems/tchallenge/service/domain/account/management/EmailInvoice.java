package ru.tsystems.tchallenge.service.domain.account.management;

import lombok.Data;

@Data
public class EmailInvoice {
    private String subject;
    private String content;
}

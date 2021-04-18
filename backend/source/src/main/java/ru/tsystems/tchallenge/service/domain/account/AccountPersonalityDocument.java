package ru.tsystems.tchallenge.service.domain.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountPersonalityDocument {
    private String firstname;
    private String lastname;
    private String middlename;
    private String quickname;
}

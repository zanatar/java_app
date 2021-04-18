package ru.tsystems.tchallenge.service.domain.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantPersonalityDocument {
    private String essay;
    private String linkedin;
    private String hh;
    private String github;
    private String bitbucket;
    private String website;
}

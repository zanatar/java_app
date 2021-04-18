package ru.tsystems.tchallenge.service.domain.account;

import org.springframework.security.core.GrantedAuthority;

public enum AccountRole implements GrantedAuthority {

    PARTICIPANT, REVIEWER, ROBOT, MODERATOR, ADMIN;

    @Override
    public String getAuthority() {
        return name();
    }
}

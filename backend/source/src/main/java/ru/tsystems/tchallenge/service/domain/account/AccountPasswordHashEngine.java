package ru.tsystems.tchallenge.service.domain.account;

import org.mindrot.jbcrypt.BCrypt;

import org.springframework.stereotype.Component;

@Component
public class AccountPasswordHashEngine{

    public String hash(final String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean match(final String password, final String passwordHash) {
        return BCrypt.checkpw(password, passwordHash);
    }
}

package ru.tsystems.tchallenge.service.utility.validation;

import java.util.regex.Pattern;

public interface ValidationAware {

    default void validate() {
        registerViolations();
    }

    default boolean validateEmail(String email) {
        final Pattern emailPattern =
                Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$");
        return emailPattern.matcher(email).find();
    }

    void registerViolations();

}

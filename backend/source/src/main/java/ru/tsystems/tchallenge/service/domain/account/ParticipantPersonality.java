package ru.tsystems.tchallenge.service.domain.account;

import com.google.common.base.Strings;
import lombok.Data;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

import java.util.regex.Pattern;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.*;

@Data
public final class ParticipantPersonality implements ValidationAware {
    private String essay;
    private String linkedin;
    private String hh;
    private String github;
    private String bitbucket;
    private String website;

    private static final Pattern VALID_URL_PATTERN = Pattern.compile(
            "^(https?://)?(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*)$");

    private static final String PROTOCOL_REGEX = "^(https?://)?";
    private static final String URL_PATH_REGEX = "/\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)$";

    @Override
    public void registerViolations() {
        if (!Strings.isNullOrEmpty(website) && invalidUrl(website)) {
            throw invalidWebsite(website);
        }
    }


    private boolean invalidUrl(String url) {
        return !VALID_URL_PATTERN.matcher(url).matches();
    }

    private boolean invalidHH(String url) {
        return !Pattern.compile(
                PROTOCOL_REGEX + "([a-zA-Z0-9]+\\.)?hh\\.ru" + URL_PATH_REGEX
        ).matcher(url).matches();
    }

    private boolean invalidGithub(String url) {
        return !Pattern.compile(
                PROTOCOL_REGEX + "github\\.com" + URL_PATH_REGEX
        ).matcher(url).matches();
    }

    private boolean invalidBitbucket(String url) {
        return !Pattern.compile(
                PROTOCOL_REGEX + "bitbucket\\.org" + URL_PATH_REGEX
        ).matcher(url).matches();
    }

    private boolean invalidLinkedin(String url) {
        return !Pattern.compile(
                PROTOCOL_REGEX + "([a-zA-Z0-9]+\\.)?linkedin\\.com" + URL_PATH_REGEX
        ).matcher(url).matches();
    }

    private OperationException invalidLinkedinException(String url) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_LINKEDIN)
                .description("Invalid url in linkedin field")
                .attachment(url)
                .build();
    }

    private OperationException invalidHHException(String url) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_HH)
                .description("Invalid url in hh field")
                .attachment(url)
                .build();
    }

    private OperationException invalidGithubException(String url) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_GITHUB)
                .description("Invalid url in github field")
                .attachment(url)
                .build();
    }

    private OperationException invalidBitbucketException(String url) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_BITBUCKET)
                .description("Invalid url in bitbucket field")
                .attachment(url)
                .build();
    }


    private OperationException invalidWebsite(String url) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_BITBUCKET)
                .description("Invalid url in website field")
                .attachment(url)
                .build();
    }
}

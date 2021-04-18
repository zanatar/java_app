package ru.tsystems.tchallenge.service.reliability.exception;

import org.springframework.http.HttpStatus;

import javax.annotation.Nullable;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_FORBIDDEN;
import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_INTERNAL;

public final class OperationExceptionBuilder {

    public static OperationExceptionBuilder operationException() {
        return new OperationExceptionBuilder();
    }

    private Object attachment;
    private Throwable cause;
    private String description;
    private OperationExceptionType textcode;

    public OperationExceptionBuilder attachment(final @Nullable Object attachment) {
        this.attachment = attachment;
        return this;
    }

    public OperationExceptionBuilder cause(final @Nullable Throwable cause) {
        this.cause = cause;
        return this;
    }

    public OperationExceptionBuilder description(final @Nullable String description) {
        this.description = description;
        return this;
    }

    public OperationExceptionBuilder textcode(final OperationExceptionType textcode) {
        this.textcode = textcode;
        return this;
    }

    public static OperationException forbidden() {
        return new OperationException(
                new OperationException.Details("Access denied", null, ERR_FORBIDDEN),
                HttpStatus.FORBIDDEN,
                null);
    }

    public static OperationException missing(OperationExceptionType type, String fieldName) {
        return new OperationException(
                new OperationException.Details(fieldName + " is missing", null, type),
                HttpStatus.NOT_FOUND,
                null
        );
    }

    public static OperationException internal(String description) {
        return internal(description, null);
    }

    public static OperationException internal(String description, Object attachment) {
        return new OperationException(
                new OperationException.Details(description, attachment, ERR_INTERNAL),
                HttpStatus.INTERNAL_SERVER_ERROR,
                null
        );
    }


    public OperationException build() {
        OperationException.Details details = new OperationException.Details(description, attachment, textcode);
        switch (textcode) {
            case ERR_INTERNAL:
                return new OperationException(details, HttpStatus.INTERNAL_SERVER_ERROR, cause);
            case ERR_NOT_AUTHORIZED:
                return new OperationException(details, HttpStatus.UNAUTHORIZED, cause);
            case ERR_ACC_ILLEGAL_STATUS:
                return new OperationException(details, HttpStatus.FORBIDDEN, cause);
            case ERR_WORKBOOK:
                return new OperationException(details, HttpStatus.NOT_FOUND, cause);
            default:
                return new OperationException(details, HttpStatus.BAD_REQUEST, cause);
        }

    }
}

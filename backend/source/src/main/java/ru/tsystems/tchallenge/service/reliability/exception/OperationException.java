package ru.tsystems.tchallenge.service.reliability.exception;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

import javax.annotation.Nullable;


public class OperationException extends RuntimeException {
    @Getter
    private final HttpStatus status;


    @Getter
    private final Details details;

    OperationException(final @NonNull Details details,
                       final @NonNull HttpStatus status,
                       final @Nullable Throwable cause) {
        super(cause);
        this.details = details;
        this.status = status;
    }


    @Override
    public String getMessage() {
        final String description = details.description != null ? details.description : "No description available";
        String msg = String.format("%s: %s", details.getTextcode(), description);
        if (details.getAttachment() != null) {
            msg += String.format("%n%s", getAttachment());
        }
        return msg;
    }

    public String getAttachment() {
        return String.format("Attachment: %s", details.getAttachment());
    }


    @Data
    static class Details {
        private final String description;
        private final Object attachment;
        @NonNull
        private final OperationExceptionType textcode;
    }
}

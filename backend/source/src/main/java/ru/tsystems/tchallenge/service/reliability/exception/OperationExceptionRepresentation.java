package ru.tsystems.tchallenge.service.reliability.exception;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class OperationExceptionRepresentation {
    private Integer status;
    private String error;
    private String path;
    private Date timestamp;
    private OperationException.Details details;
}

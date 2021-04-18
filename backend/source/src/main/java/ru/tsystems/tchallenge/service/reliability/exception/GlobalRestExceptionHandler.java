package ru.tsystems.tchallenge.service.reliability.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.*;


@RestControllerAdvice
@Log4j2
public class GlobalRestExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(OperationException.class)
    public ResponseEntity<Object> handleMyException(OperationException exception,
                                                    HttpServletRequest request) {
        logger.info(exception);
        HttpStatus status = exception.getStatus();
        OperationException.Details details = exception.getDetails();

        // Don't need to send description for internal exception
        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            details = new OperationException.Details(null, null, details.getTextcode());
        }

        OperationExceptionRepresentation dto = OperationExceptionRepresentation.builder()
                .details(details)
                .error(status.getReasonPhrase())
                .path(request.getServletPath())
                .timestamp(new Date())
                .status(status.value())
                .build();
        return new ResponseEntity<>(dto, status);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException exception,
                                                    HttpServletRequest request) {
        logger.info(exception);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        OperationExceptionRepresentation dto = OperationExceptionRepresentation.builder()
                .details(new OperationException.Details("File size should be no more than 10 MB"
                        , null, ERR_MAX_UPLOAD_SIZE_EXCEEDED))
                .error(status.getReasonPhrase())
                .path(request.getServletPath())
                .timestamp(new Date())
                .status(status.value())
                .build();
        return new ResponseEntity<>(dto, status);
    }



    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException exception, HttpServletRequest request) {
        logger.info(exception);
        HttpStatus status = HttpStatus.FORBIDDEN;
        OperationExceptionRepresentation dto = OperationExceptionRepresentation.builder()
                .details(new OperationException.Details("Access denied", null, ERR_FORBIDDEN))
                .error(status.getReasonPhrase())
                .path(request.getServletPath())
                .timestamp(new Date())
                .status(status.value())
                .build();
        return new ResponseEntity<>(dto, status);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleRuntimeException(Exception exception, HttpServletRequest request) {
        logger.info(exception);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        OperationExceptionRepresentation dto = OperationExceptionRepresentation.builder()
                .details(new OperationException.Details(null, null, ERR_INTERNAL))
                .error(status.getReasonPhrase())
                .path(request.getServletPath())
                .timestamp(new Date())
                .status(status.value())
                .build();
        return new ResponseEntity<>(dto, status);
    }


}

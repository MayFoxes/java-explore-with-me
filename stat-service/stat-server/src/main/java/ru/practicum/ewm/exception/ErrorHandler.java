package ru.practicum.ewm.exception;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, MissingRequestHeaderException.class,
            ConstraintViolationException.class, MissingServletRequestParameterException.class,
            ValidationException.class})
    public ErrorResponse badRequest(final Exception e) {
        return handleException(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ErrorResponse internalServerError(final Exception e) {
        return handleException(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ErrorResponse handleException(final Exception e, HttpStatus status) {
        String error = status.is4xxClientError() ? "Incorrectly made request." : "Internal Server Error";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String errorStatus = status.name();
        String errorMessage = e.getMessage();

        log.error("{} - Status: {}, Description: {}, Timestamp: {}",
                errorMessage, errorStatus, error, timestamp);

        return new ErrorResponse(errorMessage, error, errorStatus, timestamp);
    }
}

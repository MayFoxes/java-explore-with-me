package ru.practicum.ewm.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({ValidationException.class, ConstraintViolationException.class})
    public ErrorResponse badRequestError(final Exception e) {
        return handleException(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ErrorResponse notFoundError(final Exception e) {
        return handleException(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ErrorResponse internalServerError(final Exception e) {
        return handleException(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UniqueException.class)
    public ErrorResponse conflictError(final Exception e) {
        return handleException(e, HttpStatus.CONFLICT);
    }

    private ErrorResponse handleException(final Exception e, HttpStatus status) {
        List<String> errors = Collections.singletonList(Arrays.toString(e.getStackTrace()));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String errorStatus = status.name();
        String errorMessage = e.getMessage();
        String reason = status.getReasonPhrase();

        log.error("errors: {}; message: {}; reason: {}; status: {};  Timestamp: {}",
                errors, errorMessage, reason, errorStatus, timestamp);

        return new ErrorResponse(errors, errorMessage, reason, errorStatus, timestamp);
    }
}

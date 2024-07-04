package ru.practicum.ewm.exception;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({ValidationException.class, ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class,
            MethodArgumentNotValidException.class, MismatchedInputException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError badRequestError(final Exception e) {
        return handleException(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFoundError(final NotFoundException e) {
        return handleException(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError internalServerError(final Throwable e) {
        return ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .message(e.getMessage())
                .reason("Request is INTERNAL_SERVER_ERROR")
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    @ExceptionHandler({UniqueException.class, ConflictException.class,
            PSQLException.class, DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError conflictError(final Exception e) {
        return handleException(e, HttpStatus.CONFLICT);
    }

    private ApiError handleException(final Exception e, HttpStatus status) {
        List<String> errors = Collections.singletonList(Arrays.toString(e.getStackTrace()));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String errorStatus = status.name();
        String errorMessage = e.getMessage();
        String reason = status.getReasonPhrase();

        log.error("errors: {}; message: {}; reason: {}; status: {};  Timestamp: {}",
                errors, errorMessage, reason, errorStatus, timestamp);

        return ApiError.builder()
                .status(errorStatus)
                .message(errorMessage)
                .reason(reason)
                .timestamp(timestamp)
                .build();
    }
}

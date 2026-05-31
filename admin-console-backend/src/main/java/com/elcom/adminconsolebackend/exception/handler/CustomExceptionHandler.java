package com.elcom.adminconsolebackend.exception.handler;

import com.elcom.adminconsolebackend.dto.message.ResponseMessage;
import com.elcom.adminconsolebackend.exception.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ValidationException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResponseMessage> handleAuthenticationException(AuthenticationException authenticationException) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ResponseMessage.error(
                        HttpStatus.UNAUTHORIZED,
                        "Bạn không có quyền truy cập tài nguyên này!"));
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ResponseMessage> handleAuthorizationException(AuthorizationException exception) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseMessage.error(
                        HttpStatus.FORBIDDEN,
                        exception.getMessage()));
    }

    @ExceptionHandler(ResourceExistException.class)
    public ResponseEntity<ResponseMessage> handleCreateExistResourceException(ResourceExistException resourceExistException) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ResponseMessage.error(
                        HttpStatus.CONFLICT,
                        resourceExistException.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseMessage> handleResourceNotFoundException(ResourceNotFoundException resourceExistException) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResponseMessage.error(
                        HttpStatus.NOT_FOUND,
                        resourceExistException.getMessage()));
    }

    @SneakyThrows
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ResponseMessage> handleHttpClientErrorException(HttpClientErrorException ex) {
        ResponseMessage o = objectMapper.readValue(ex.getResponseBodyAsByteArray(), ResponseMessage.class);
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(o);
    }

    @ExceptionHandler(ResourceConstraintException.class)
    public ResponseEntity<ResponseMessage> handleResourceConstraintException(ResourceConstraintException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseMessage.error(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseMessage handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        List<ObjectError> validationErrors = ex.getBindingResult().getAllErrors();
        validationErrors.forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        String errorMsg = "Data is invalid";
        if (!validationErrors.isEmpty()) {
            errorMsg = validationErrors.get(0).getDefaultMessage();
        }
        return ResponseMessage.withDetails(HttpStatus.BAD_REQUEST, errorMsg, errors);
    }

    @ExceptionHandler(ValidationAuthorException.class)
    public ResponseEntity<ResponseMessage> handleValidationException(ValidationAuthorException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseMessage.error(HttpStatus.BAD_REQUEST, ex.getMessage()));        // trả về message
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseMessage> handleException(RuntimeException ex) {
        log.error(ex.getMessage(), ex.getCause());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseMessage.error(HttpStatus.INTERNAL_SERVER_ERROR));
    }

}

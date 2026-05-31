package com.elcom.adminconsolebackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author anhdv
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationAuthorException extends RuntimeException {
    
    public ValidationAuthorException(String message) {
        super(message);
    }

    public ValidationAuthorException(String message, Throwable cause) {
        super(message, cause);
    }
}

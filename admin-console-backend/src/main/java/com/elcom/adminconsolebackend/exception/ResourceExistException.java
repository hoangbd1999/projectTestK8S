package com.elcom.adminconsolebackend.exception;

import lombok.Getter;

@Getter
public class ResourceExistException extends RuntimeException {
    private final String message;

    public ResourceExistException(String message) {
        this.message = message;
    }
}

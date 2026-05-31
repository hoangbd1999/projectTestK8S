package com.elcom.adminconsolebackend.exception;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ResourceConstraintException extends RuntimeException {
    public ResourceConstraintException(String message) {
        super(message);
    }
}

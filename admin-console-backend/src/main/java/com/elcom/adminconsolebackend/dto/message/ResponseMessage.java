package com.elcom.adminconsolebackend.dto.message;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

@Data
public class ResponseMessage<T> {

    private final int status;

    private final String message;

    private final T data;

    private ResponseMessage(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static ResponseMessage<?> error(HttpStatus status) {
        Assert.isTrue(status.isError(), "Error response must have 4xx or 5xx status");
        return withDetails(status, null);
    }

    public static ResponseMessage<?> error(HttpStatus status, String message) {
        Assert.isTrue(status.isError(), "Error response must have 4xx or 5xx status");
        return withDetails(status, message, null);
    }

    public static <T> ResponseMessage<T> success(T data) {
        return success(HttpStatus.OK, data);
    }

    public static <T> ResponseMessage<T> success(HttpStatus status, T data) {
        Assert.isTrue(status.is2xxSuccessful(), "Success response must have 2xx status");
        return withDetails(status, data);
    }

    public static <T> ResponseMessage<T> withDetails(HttpStatus status, T data) {
        String message = String.format("%d %s", status.value(), status.getReasonPhrase());
        return withDetails(status, message, data);
    }

    public static <T> ResponseMessage<T> withDetails(HttpStatus status, String message, T data) {
        return new ResponseMessage<>(status.value(), message, data);
    }
}

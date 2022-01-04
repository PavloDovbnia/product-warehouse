package com.rost.productwarehouse.security.exceptions;

public class PasswordChangeValidationException extends RuntimeException {

    public PasswordChangeValidationException(String message) {
        super(message);
    }
}

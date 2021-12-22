package com.rost.productwarehouse.security.exceptions;

public class PasswordResetTokenValidationException extends RuntimeException {

    public PasswordResetTokenValidationException(String message) {
        super(message);
    }
}

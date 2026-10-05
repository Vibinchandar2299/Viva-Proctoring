package com.airouteviva.exception;

public class GoRouterUnavailableException extends RuntimeException {
    public GoRouterUnavailableException(String message) {
        super(message);
    }

    public GoRouterUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

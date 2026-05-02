package com.nemchann.fitnessbackend.common.exception;

public class NotEnoughPrivilegesException extends RuntimeException {
    public NotEnoughPrivilegesException(String message) {
        super(message);
    }
}

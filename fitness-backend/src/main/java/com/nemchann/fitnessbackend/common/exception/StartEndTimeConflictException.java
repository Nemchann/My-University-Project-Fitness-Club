package com.nemchann.fitnessbackend.common.exception;

public class StartEndTimeConflictException extends RuntimeException {
    public StartEndTimeConflictException(String message) {
        super(message);
    }
}

package com.nemchann.fitnessbackend.common.exception;

public class AlreadyBookedException extends RuntimeException {
    public AlreadyBookedException(String message) {
        super(message);
    }
}

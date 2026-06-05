package com.nemchann.fitnessbackend.common.exception;

public class WorkoutAlreadyExistsException extends RuntimeException {
    public WorkoutAlreadyExistsException(String message) {
        super(message);
    }
}

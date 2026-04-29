package com.nemchann.fitnessbackend.common.exception;

public class WorkoutIsNotFoundException extends RuntimeException {
    public WorkoutIsNotFoundException(String message) {
        super(message);
    }
}

package com.nemchann.fitnessbackend.common.exception;

public class ScheduleIsNotActiveException extends RuntimeException {
    public ScheduleIsNotActiveException(String message) {
        super(message);
    }
}

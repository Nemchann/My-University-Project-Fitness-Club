package com.nemchann.fitnessbackend.common.exception;

public class RoomAlreadyOccupiedException extends RuntimeException {
    public RoomAlreadyOccupiedException(String message) {
        super(message);
    }
}

package com.nemchann.fitnessbackend.common.exception;


public class BookingTooLateException extends RuntimeException {
    public BookingTooLateException(String message) {
        super(message);
    }
}

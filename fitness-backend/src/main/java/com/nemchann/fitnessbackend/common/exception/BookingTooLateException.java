package com.nemchann.fitnessbackend.common.exception;

//Обработать в хендлере
public class BookingTooLateException extends RuntimeException {
    public BookingTooLateException(String message) {
        super(message);
    }
}

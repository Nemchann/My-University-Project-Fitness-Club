package com.nemchann.fitnessbackend.common.exception;

public class PaymentStatusNotFoundException extends RuntimeException {
    public PaymentStatusNotFoundException(String message) {
        super(message);
    }
}

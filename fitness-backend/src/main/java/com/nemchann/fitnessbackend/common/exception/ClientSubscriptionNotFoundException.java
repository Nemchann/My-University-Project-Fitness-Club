package com.nemchann.fitnessbackend.common.exception;

public class ClientSubscriptionNotFoundException extends RuntimeException{
    public ClientSubscriptionNotFoundException(String message) {
        super(message);
    }
}

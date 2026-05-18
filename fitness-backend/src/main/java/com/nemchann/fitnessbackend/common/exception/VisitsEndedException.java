package com.nemchann.fitnessbackend.common.exception;

//Добавить в хендлер
public class VisitsEndedException extends RuntimeException {
    public VisitsEndedException(String message) {
        super(message);
    }
}

package com.nemchann.fitnessbackend.booking.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BookingShortResponseDto {
    private String scheduleName;

    private LocalDate scheduleDate;

    private String status;

    private LocalDateTime startTime;
}

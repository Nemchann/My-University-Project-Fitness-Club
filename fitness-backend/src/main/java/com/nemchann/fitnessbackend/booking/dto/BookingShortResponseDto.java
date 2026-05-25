package com.nemchann.fitnessbackend.booking.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BookingShortResponseDto {

    private UUID bookingId;

    private String scheduleName;

    private LocalDate scheduleDate;

    private String status;

    private LocalDateTime startTime;

    private String trainerFullName;
}

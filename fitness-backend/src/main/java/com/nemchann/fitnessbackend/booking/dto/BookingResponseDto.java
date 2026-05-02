package com.nemchann.fitnessbackend.booking.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class BookingResponseDto {

    private UUID bookingId;

    private String scheduleName;

    private LocalDate scheduleDate;

    private String status;

    private LocalDateTime startTime;
}

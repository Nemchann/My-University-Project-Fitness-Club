package com.nemchann.fitnessbackend.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class BookingCreateDto {

    @NotNull
    private UUID userId;

    @NotNull
    private Integer scheduleId;

    private OffsetDateTime createdAt;

}

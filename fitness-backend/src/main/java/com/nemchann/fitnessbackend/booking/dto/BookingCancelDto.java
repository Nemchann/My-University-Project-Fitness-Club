package com.nemchann.fitnessbackend.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class BookingCancelDto {

    @NotNull
    private UUID bookingId;

    @NotNull
    private UUID userId;
}

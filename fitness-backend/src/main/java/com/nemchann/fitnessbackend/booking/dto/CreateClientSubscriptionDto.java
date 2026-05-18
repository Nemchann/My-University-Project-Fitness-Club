package com.nemchann.fitnessbackend.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateClientSubscriptionDto {

    @NotNull
    private UUID clientId;

    @NotNull
    private Integer subscriptionId;
}

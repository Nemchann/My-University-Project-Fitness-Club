package com.nemchann.fitnessbackend.booking.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubscriptionResponseDto {

    private String subscriptionName;

    private Double price;

    private Integer durationDays;

    private Integer visitsCount;
}

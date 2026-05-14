package com.nemchann.fitnessbackend.booking.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class ClientSubscriptionResponseDto {

    private LocalDate startDate;

    private Integer remainingVisits;

    private LocalDate endDate;

    private String subscriptionStatus;
}

package com.nemchann.fitnessbackend.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ClientActiveAndFutureSubscriptionsDto {
    private ClientSubscriptionResponseDto activeSubscription; // Текущий (может быть null, если всё просрочено)

    private List<ClientSubscriptionResponseDto> pendingSubscriptions; // Очередь будущих абонементов, тоже может быть пустым
}

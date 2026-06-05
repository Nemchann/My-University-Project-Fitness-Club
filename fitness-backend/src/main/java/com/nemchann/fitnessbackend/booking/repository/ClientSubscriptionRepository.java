package com.nemchann.fitnessbackend.booking.repository;

import com.nemchann.fitnessbackend.booking.entity.ClientSubscription;
import com.nemchann.fitnessbackend.booking.enums.SubscriptionStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientSubscriptionRepository extends JpaRepository<ClientSubscription, Integer> {
    Optional<ClientSubscription> findClientSubscriptionById(Integer id);

    Page<ClientSubscription> findByClientId(UUID clientId, Pageable pageable);

    Optional<ClientSubscription> findLastByClientId(UUID clientId);

    // 1. Ищем действующий абонемент. Вместо LIMIT используем Pageable во внутреннем вызове
    @Query("SELECT s FROM ClientSubscription s " +
            "WHERE s.client.id = :clientId " +
            "AND s.subscriptionStatus.subscriptionStatusName = :status " +
            "AND :currentDate BETWEEN s.startDate AND s.endDate " +
            "ORDER BY s.endDate ASC")
    List<ClientSubscription> findActiveSubscriptionsInternal(
            @Param("clientId") UUID clientId,
            @Param("status") SubscriptionStatusEnum status,
            @Param("currentDate") LocalDate currentDate,
            Pageable pageable
    );

    // Удобный дефолтный метод-обертка, который заменяет LIMIT 1
    default Optional<ClientSubscription> findCurrentActiveSubscription(UUID clientId, LocalDate currentDate) {
        List<ClientSubscription> result = findActiveSubscriptionsInternal(
                clientId,
                SubscriptionStatusEnum.ACTIVE,
                currentDate,
                org.springframework.data.domain.PageRequest.of(0, 1) // Берем строго 1 запись
        );
        return result.stream().findFirst();
    }

    // 2. Ищем следующий абонемент в очереди (PENDING)
    @Query("SELECT s FROM ClientSubscription s " +
            "WHERE s.client.id = :clientId " +
            "AND s.subscriptionStatus.subscriptionStatusName = :status " +
            "ORDER BY s.startDate ASC")
    List<ClientSubscription> findPendingSubscriptionsInternal(
            @Param("clientId") UUID clientId,
            @Param("status") SubscriptionStatusEnum status,
            Pageable pageable
    );

    default Optional<ClientSubscription> findNextPendingSubscription(UUID clientId) {
        List<ClientSubscription> result = findPendingSubscriptionsInternal(
                clientId,
                SubscriptionStatusEnum.PENDING,
                org.springframework.data.domain.PageRequest.of(0, 1)
        );
        return result.stream().findFirst();
    }
}

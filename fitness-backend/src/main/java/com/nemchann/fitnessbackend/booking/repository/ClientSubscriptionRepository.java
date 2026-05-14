package com.nemchann.fitnessbackend.booking.repository;

import com.nemchann.fitnessbackend.booking.entity.ClientSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClientSubscriptionRepository extends JpaRepository<ClientSubscription, Integer> {
    Optional<ClientSubscription> findClientSubscriptionById(Integer id);

    Page<ClientSubscription> findByClientId(UUID clientId, Pageable pageable);

    //Сделать метод, который возвращает последний абонемент пользователя

    Optional<ClientSubscription> findLastByClientId(UUID clientId);
}

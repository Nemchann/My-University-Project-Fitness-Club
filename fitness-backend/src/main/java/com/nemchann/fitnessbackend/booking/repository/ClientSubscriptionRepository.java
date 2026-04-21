package com.nemchann.fitnessbackend.booking.repository;

import com.nemchann.fitnessbackend.booking.entity.ClientSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientSubscriptionRepository extends JpaRepository<ClientSubscription, Integer> {
}

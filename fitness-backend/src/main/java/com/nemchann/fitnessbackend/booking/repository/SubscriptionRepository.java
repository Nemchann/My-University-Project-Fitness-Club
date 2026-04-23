package com.nemchann.fitnessbackend.booking.repository;

import com.nemchann.fitnessbackend.booking.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    Optional<Subscription> findBySubscriptionName(String subscriptionName);
}

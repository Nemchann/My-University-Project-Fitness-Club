package com.nemchann.fitnessbackend.booking.repository;

import com.nemchann.fitnessbackend.booking.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionStatusRepository extends JpaRepository<SubscriptionStatus, Integer> {
    Optional<SubscriptionStatus> findBySubscriptionStatusName(String subscriptionStatusName);
}

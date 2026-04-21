package com.nemchann.fitnessbackend.booking.repository;

import com.nemchann.fitnessbackend.booking.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
}

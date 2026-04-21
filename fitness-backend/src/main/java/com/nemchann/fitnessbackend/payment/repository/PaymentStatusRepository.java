package com.nemchann.fitnessbackend.payment.repository;

import com.nemchann.fitnessbackend.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentStatusRepository extends JpaRepository<PaymentStatus, Integer> {
}

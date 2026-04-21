package com.nemchann.fitnessbackend.payment.repository;

import com.nemchann.fitnessbackend.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
}

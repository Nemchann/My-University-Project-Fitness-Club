package com.nemchann.fitnessbackend.payment.repository;

import com.nemchann.fitnessbackend.payment.entity.PaymentStatus;
import com.nemchann.fitnessbackend.payment.enums.PaymentStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentStatusRepository extends JpaRepository<PaymentStatus, Integer> {
    Optional<PaymentStatus> findByPaymentStatusName(PaymentStatusEnum paymentStatusName);
}

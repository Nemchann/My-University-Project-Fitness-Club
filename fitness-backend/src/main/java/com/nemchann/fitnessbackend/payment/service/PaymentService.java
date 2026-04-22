package com.nemchann.fitnessbackend.payment.service;

import com.nemchann.fitnessbackend.payment.repository.PaymentRepository;
import com.nemchann.fitnessbackend.payment.repository.PaymentStatusRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private PaymentRepository paymentRepository;
    private PaymentStatusRepository paymentStatusRepository;
}

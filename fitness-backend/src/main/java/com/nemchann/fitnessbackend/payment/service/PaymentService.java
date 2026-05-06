package com.nemchann.fitnessbackend.payment.service;

import com.nemchann.fitnessbackend.common.exception.PaymentStatusNotFoundException;
import com.nemchann.fitnessbackend.common.exception.UserNotFoundException;
import com.nemchann.fitnessbackend.payment.dto.PaymentResponseDto;
import com.nemchann.fitnessbackend.payment.entity.Payment;
import com.nemchann.fitnessbackend.payment.entity.PaymentStatus;
import com.nemchann.fitnessbackend.payment.enums.PaymentStatusEnum;
import com.nemchann.fitnessbackend.payment.repository.PaymentRepository;
import com.nemchann.fitnessbackend.payment.repository.PaymentStatusRepository;
import com.nemchann.fitnessbackend.users.entity.User;
import com.nemchann.fitnessbackend.users.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final UserRepository userRepository;


    public PaymentResponseDto createPayment(UUID clientId){
        Payment payment = new Payment();

        User user = userRepository.findById(clientId)
                        .orElseThrow(() -> new UserNotFoundException("User is not found"));

        payment.setClient(user);
        payment.setAmount(0.1);
        payment.setTransactionDate(OffsetDateTime.now());
        PaymentStatus status = paymentStatusRepository.findByPaymentStatusName(PaymentStatusEnum.ACCEPTED)
                        .orElseThrow(() -> new PaymentStatusNotFoundException("Payment status is not found"));

        payment.setPaymentStatus(status);

        paymentRepository.save(payment);

        return mapToResponseDto(payment);
    }

    public PaymentResponseDto mapToResponseDto(Payment payment){
        PaymentResponseDto dto = new PaymentResponseDto();

        dto.setId(payment.getId());

        dto.setStatus(payment.getPaymentStatus().getPaymentStatusName().name());

        return dto;
    }
}

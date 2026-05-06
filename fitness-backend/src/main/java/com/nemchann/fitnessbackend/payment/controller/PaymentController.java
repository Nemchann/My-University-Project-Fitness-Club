package com.nemchann.fitnessbackend.payment.controller;

import com.nemchann.fitnessbackend.payment.dto.PaymentResponseDto;
import com.nemchann.fitnessbackend.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/fitness-club/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Controller", description = "Управление платежами")
public class PaymentController {

    private final PaymentService service;

    @PostMapping("/create_payment/{clientId}")
    @Operation(summary = "")
    public ResponseEntity<PaymentResponseDto> createPayment(UUID clientId){
        PaymentResponseDto dto = service.createPayment(clientId);

        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

}

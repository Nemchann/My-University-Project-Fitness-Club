package com.nemchann.fitnessbackend.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer Id;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "payment_status", nullable = false)
    private Integer paymentStatus;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column (name = "transaction_date", nullable = false)
    private OffsetDateTime transactionDate;

}

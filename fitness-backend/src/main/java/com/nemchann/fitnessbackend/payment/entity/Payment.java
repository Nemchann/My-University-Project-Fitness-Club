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

    @Column(name = "client_id")
    private UUID clientId;

    @Column(name = "payment_status")
    private Integer paymentStatus;

    @Column(name = "amount")
    private Double amount;

    @Column (name = "transaction_date")
    private OffsetDateTime transactionDate;

}

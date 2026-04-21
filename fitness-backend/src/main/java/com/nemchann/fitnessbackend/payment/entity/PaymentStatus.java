package com.nemchann.fitnessbackend.payment.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payment_statuses")
@Getter @Setter
public class PaymentStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer Id;

    @Column (name = "payment_status_name")
    private String paymentStatusName;
}

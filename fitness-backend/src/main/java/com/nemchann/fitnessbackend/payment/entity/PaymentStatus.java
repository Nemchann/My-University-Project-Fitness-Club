package com.nemchann.fitnessbackend.payment.entity;


import com.nemchann.fitnessbackend.payment.PaymentStatusEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payment_statuses")
@Getter @Setter
public class PaymentStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer Id;

    //ACCEPTED, PROCESSING, CANCELLED
    @Enumerated(EnumType.STRING)
    @Column (name = "payment_status_name", nullable = false, unique = true)
    private PaymentStatusEnum paymentStatusName;

    @OneToMany(mappedBy = "paymentStatus", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> paymentList = new ArrayList<>();
}

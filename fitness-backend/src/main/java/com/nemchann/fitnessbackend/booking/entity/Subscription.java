package com.nemchann.fitnessbackend.booking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "subscription_name")
    private String subscriptionName;

    @Column(name = "price")
    private Double price;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "visits_count")
    private Integer visitsCount;


}

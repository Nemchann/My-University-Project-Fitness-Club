package com.nemchann.fitnessbackend.booking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "subscription_name", nullable = false, unique = true)
    private String subscriptionName;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "visits_count")
    private Integer visitsCount;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClientSubscription> clientSubscriptionList = new ArrayList<>();


}

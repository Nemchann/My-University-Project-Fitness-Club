package com.nemchann.fitnessbackend.booking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subscription_statuses")
@Getter @Setter
public class SubscriptionStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "subscription_status_name", nullable = false, unique = true)
    private String subscriptionStatusName;

    @OneToMany(mappedBy = "subscriptionStatus", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClientSubscription> subscriptionList = new ArrayList<>();
}

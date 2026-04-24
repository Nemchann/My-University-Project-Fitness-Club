package com.nemchann.fitnessbackend.booking.entity;

import com.nemchann.fitnessbackend.booking.enums.SubscriptionStatusEnum;
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

    //ACTIVE, LAPSED, FROZEN
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status_name", nullable = false, unique = true)
    private SubscriptionStatusEnum subscriptionStatusName;

    @OneToMany(mappedBy = "subscriptionStatus", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClientSubscription> subscriptionList = new ArrayList<>();
}

package com.nemchann.fitnessbackend.booking.entity;

import com.nemchann.fitnessbackend.users.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "client_subscriptions")
@Getter
@Setter
public class ClientSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Column(name = "end_date", nullable = false)
    private Date endDate;

    @Column(name = "remaining_visits", nullable = false)
    private Integer remainingVisits;

    @Column(name = "subscription_status", nullable = false)
    private Integer subscriptionStatus;

}

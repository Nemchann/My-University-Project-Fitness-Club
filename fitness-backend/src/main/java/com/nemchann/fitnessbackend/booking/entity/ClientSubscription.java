package com.nemchann.fitnessbackend.booking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "client_subscriptions")
@Getter
@Setter
public class ClientSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "client_id")
    private UUID clientId;

    @Column(name = "subscription_id")
    private Integer subscriptionId;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "remaining_visits")
    private Integer remainingVisits;

    @Column(name = "subscription_status")
    private Integer subscriptionStatus;

}

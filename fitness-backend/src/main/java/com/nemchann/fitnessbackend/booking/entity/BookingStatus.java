package com.nemchann.fitnessbackend.booking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "booking_statuses")
@Getter
@Setter
public class BookingStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name="booking_status_name", nullable = false, unique = true)
    private String bookingStatusName;

    @OneToMany(mappedBy = "bookingStatus", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookingList = new ArrayList<>();

}

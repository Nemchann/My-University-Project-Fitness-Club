package com.nemchann.fitnessbackend.booking.entity;

import com.nemchann.fitnessbackend.booking.enums.BookingStatusEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "booking_statuses")
@Getter
@Setter
@NoArgsConstructor
public class BookingStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    //ACCEPTED, PROCESSING, CANCELLED
    @Enumerated(EnumType.STRING)
    @Column(name="booking_status_name", nullable = false, unique = true)
    private BookingStatusEnum bookingStatusName;

    @OneToMany(mappedBy = "bookingStatus", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookingList = new ArrayList<>();

}

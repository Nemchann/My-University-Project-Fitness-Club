package com.nemchann.fitnessbackend.booking.repository;

import com.nemchann.fitnessbackend.booking.entity.BookingStatus;
import com.nemchann.fitnessbackend.booking.enums.BookingStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingStatusRepository extends JpaRepository<BookingStatus, Integer> {
    Optional<BookingStatus> findByBookingStatusName(BookingStatusEnum bookingStatusName);
}

package com.nemchann.fitnessbackend.booking.repository;

import com.nemchann.fitnessbackend.booking.entity.Booking;
import com.nemchann.fitnessbackend.booking.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    boolean existsByUserIdAndScheduleId(UUID userId, Integer scheduleId);

    List<Booking> findByClientID(UUID clientId);

    List<Booking> findByScheduleId(Integer scheduleId);
}

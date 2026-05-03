package com.nemchann.fitnessbackend.booking.repository;

import com.nemchann.fitnessbackend.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    boolean existsByClientIdAndScheduleId(UUID clientId, Integer scheduleId);

    List<Booking> findByClientId(UUID clientId);

    List<Booking> findByScheduleId(Integer scheduleId);
}

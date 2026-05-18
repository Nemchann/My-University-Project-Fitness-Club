package com.nemchann.fitnessbackend.booking.repository;

import com.nemchann.fitnessbackend.booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    boolean existsByClientIdAndScheduleId(UUID clientId, Integer scheduleId);

    Page<Booking> findByClientId(UUID clientId, Pageable pageable);

    List<Booking> findAllByScheduleId(Integer scheduleId);

    // Находит будущие записи: дата в расписании больше текущей
    Page<Booking> findByClientIdAndScheduleScheduleDateAfter(UUID clientId, LocalDate now, Pageable pageable);

    // Находит прошедшие записи: дата в расписании меньше текущей
    Page<Booking> findByClientIdAndScheduleScheduleDateBefore(UUID clientId, LocalDate now, Pageable pageable);

    //Находит самую ближайшую запись на занятие
    Optional<Booking> findFirstByClientIdAndScheduleScheduleDateAfter(UUID clientId, LocalDate now);
}

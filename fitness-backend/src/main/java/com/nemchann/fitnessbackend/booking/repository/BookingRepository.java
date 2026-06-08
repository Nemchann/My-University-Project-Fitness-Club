package com.nemchann.fitnessbackend.booking.repository;

import com.nemchann.fitnessbackend.booking.entity.Booking;
import com.nemchann.fitnessbackend.booking.entity.BookingStatus;
import com.nemchann.fitnessbackend.booking.enums.BookingStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    boolean existsByClientIdAndScheduleId(UUID clientId, Integer scheduleId);

    Page<Booking> findByClientId(UUID clientId, Pageable pageable);

    List<Booking> findAllByScheduleId(Integer scheduleId);

    boolean existsByClientId(UUID clientId);

    // Находит будущие записи: дата в расписании больше текущей
    Page<Booking> findByClientIdAndScheduleStartTimeAfter(UUID clientId, LocalDateTime now, Pageable pageable);

    // Находит прошедшие записи: дата в расписании меньше текущей
    Page<Booking> findByClientIdAndScheduleScheduleDateBefore(UUID clientId, LocalDate now, Pageable pageable);

    // Находит самую ближайшую запись на занятие
    Optional<Booking> findFirstByClientIdAndScheduleScheduleDateAfter(UUID clientId, LocalDate now);

    @Modifying
    @Transactional
    @Query("UPDATE Booking b SET b.bookingStatus = :cancelledStatus " +
            "WHERE b.schedule.id = :scheduleId AND b.bookingStatus.bookingStatusName != 'CANCELLED'")
    void cancelAllBookingsForSchedule(@Param("scheduleId") Integer scheduleId,
                                      @Param("cancelledStatus") BookingStatus cancelledStatus);


    List<Booking> findAllByScheduleIdAndBookingStatus_BookingStatusNameNot
            (Integer scheduleId, BookingStatusEnum bookingStatusName);
}

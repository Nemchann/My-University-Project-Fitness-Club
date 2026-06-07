package com.nemchann.fitnessbackend.schedule.repository;

import com.nemchann.fitnessbackend.schedule.entity.Schedule;
import com.nemchann.fitnessbackend.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    Page<Schedule> findAllByIsActiveTrue(Pageable pageable);

    List<Schedule> findByScheduleDateOrderByStartTimeAsc(LocalDate date);

    // Тренировки в определенный промежуток времени
    @Query("SELECT s FROM Schedule s WHERE s.startTime >= :start AND s.startTime < :end ORDER BY s.startTime ASC")
    List<Schedule> findAllByStartTimeBetweenOrderByStartTimeAsc(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Доступные тренировки (не отмененные и не заполненные)
    @Query("SELECT s FROM Schedule s WHERE s.isActive = true " +
            "AND s.currentParticipants < s.maxParticipants " +
            "AND s.startTime > :now")
    Page<Schedule> findAvailableSchedules(@Param("now") LocalDateTime now, Pageable pageable);

    Page<Schedule> findAllByTrainer(User trainer, Pageable pageable);

    // Возможно додумать
//    @Query("SELECT s FROM Schedule s " +
//    "JOIN s.workout w " +
//    "JOIN w.workoutType wt " +
//    "HAVING wt = :workoutTypeEnum AND s.isActive = true")
//    Page<Schedule> findAvailableSchedulesByWorkoutType(@Param("workoutTypeEnum") WorkoutTypeEnum workoutTypeEnum, Pageable pageable);

    @Query("SELECT s FROM Schedule s WHERE s.room.id = :roomId " +
            "AND s.startTime < :endTime " +
            "AND s.endTime > :startTime " +
            "AND s.isActive = true") // Учитываем только активные тренировки
    Optional<Schedule> findOverlappingSchedule(@Param("roomId") Integer roomId,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    boolean existsByRoomIdAndStartTimeBeforeAndEndTimeAfterAndIsActiveTrue(
            Integer roomId, LocalDateTime endTime, LocalDateTime startTime
    );

    // Задействован ли тренер в тренировках в указанное время
    @Query("SELECT s FROM Schedule s WHERE s.trainer.id = :trainerId " +
            "AND s.startTime < :endTime " +
            "AND s.endTime > :startTime " +
            "AND s.isActive = true")
    Optional<Schedule> findOverlappingTrainerSchedule(@Param("trainerId") UUID trainerId,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);
}

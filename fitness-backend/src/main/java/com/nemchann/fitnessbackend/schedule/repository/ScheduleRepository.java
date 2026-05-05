package com.nemchann.fitnessbackend.schedule.repository;

import com.nemchann.fitnessbackend.schedule.entity.Schedule;
import com.nemchann.fitnessbackend.schedule.enums.WorkoutTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    Page<Schedule> findAllByIsActiveTrue(Pageable pageable);

    List<Schedule> findByScheduleDate(LocalDate date);

    @Query("SELECT s FROM Schedule s WHERE s.startTime >= :start AND s.startTime < :end ORDER BY s.startTime ASC")
    List<Schedule> findAllByStartTimeBetweenOrderByStartTimeAsc(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT s FROM Schedule s WHERE s.isActive = true " +
            "AND s.currentParticipants < s.maxParticipants " +
            "AND s.startTime > :now")
    Page<Schedule> findAvailableSchedules(@Param("now") LocalDateTime now, Pageable pageable);

    //Возможно додумать
//    @Query("SELECT s FROM Schedule s " +
//    "JOIN s.workout w " +
//    "JOIN w.workoutType wt " +
//    "HAVING wt = :workoutTypeEnum AND s.isActive = true")
//    Page<Schedule> findAvailableSchedulesByWorkoutType(@Param("workoutTypeEnum") WorkoutTypeEnum workoutTypeEnum, Pageable pageable);
}

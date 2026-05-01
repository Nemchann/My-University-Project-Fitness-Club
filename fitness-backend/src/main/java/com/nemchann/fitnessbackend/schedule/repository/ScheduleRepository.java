package com.nemchann.fitnessbackend.schedule.repository;

import com.nemchann.fitnessbackend.schedule.entity.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    Page<Schedule> findAllByIsActiveTrue(Pageable pageable);

    List<Schedule> findByScheduleDate(Date date);

    List<Schedule> findAllByStartTimeBetweenOrderByStartTimeAsc(LocalDateTime startOfWeek, LocalDateTime endOfWeek);
}

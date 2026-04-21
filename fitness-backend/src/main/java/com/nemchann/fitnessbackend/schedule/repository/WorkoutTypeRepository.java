package com.nemchann.fitnessbackend.schedule.repository;

import com.nemchann.fitnessbackend.schedule.entity.WorkoutType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkoutTypeRepository extends JpaRepository<WorkoutType, Integer> {
}

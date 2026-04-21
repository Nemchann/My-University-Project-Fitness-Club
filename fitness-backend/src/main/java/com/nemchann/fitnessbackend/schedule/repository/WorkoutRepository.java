package com.nemchann.fitnessbackend.schedule.repository;

import com.nemchann.fitnessbackend.schedule.entity.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Integer> {
}

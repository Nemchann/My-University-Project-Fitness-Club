package com.nemchann.fitnessbackend.schedule.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "workouts")
@Getter @Setter
public class Workout {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "workout_name", nullable = false, unique = true)
    private String workoutName;

    @Column(name = "workout_type_id", nullable = false)
    private Integer workoutTypeId;

    @Column(name = "description")
    private String description;

}

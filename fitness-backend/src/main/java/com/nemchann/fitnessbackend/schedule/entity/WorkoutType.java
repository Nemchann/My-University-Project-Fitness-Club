package com.nemchann.fitnessbackend.schedule.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_types")
@Getter
@Setter
public class WorkoutType {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "type_name", nullable = false, unique = true)
    private String typeName;

    @OneToMany(mappedBy = "workoutType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Workout> workoutList = new ArrayList<>();
}

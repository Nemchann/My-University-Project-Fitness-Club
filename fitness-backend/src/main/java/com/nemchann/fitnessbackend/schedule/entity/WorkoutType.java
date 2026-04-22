package com.nemchann.fitnessbackend.schedule.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
}

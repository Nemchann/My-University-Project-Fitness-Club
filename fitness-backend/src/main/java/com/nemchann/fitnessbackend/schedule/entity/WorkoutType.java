package com.nemchann.fitnessbackend.schedule.entity;

import com.nemchann.fitnessbackend.schedule.enums.WorkoutTypeEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_types")
@Getter
@Setter
@NoArgsConstructor
public class WorkoutType {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    //STRETCH, YOGA, STRENGTH, CARDIO, DANCE
    @Enumerated(EnumType.STRING)
    @Column(name = "type_name", nullable = false, unique = true)
    private WorkoutTypeEnum typeName;

    @OneToMany(mappedBy = "workoutType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Workout> workoutList = new ArrayList<>();
}

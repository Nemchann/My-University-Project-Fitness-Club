package com.nemchann.fitnessbackend.schedule.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workouts")
@Getter @Setter
@NoArgsConstructor
public class Workout {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "workout_name", nullable = false, unique = true)
    private String workoutName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_type_id", nullable = false)
    private WorkoutType workoutType;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Schedule> scheduleList = new ArrayList<>();

}

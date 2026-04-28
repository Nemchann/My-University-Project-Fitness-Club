package com.nemchann.fitnessbackend.schedule.dto;

import com.nemchann.fitnessbackend.schedule.entity.WorkoutType;
import jakarta.validation.constraints.NotBlank;

public class WorkoutCreateDto {

    @NotBlank
    private String workoutName;

    //Додумать
    private WorkoutType workoutType;

    @NotBlank
    private String description;

}

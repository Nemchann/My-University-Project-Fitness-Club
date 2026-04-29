package com.nemchann.fitnessbackend.schedule.dto;

import lombok.Data;

@Data
public class WorkoutResponseDto {

    private Integer id;

    private String workoutName;

    private String workoutType;

    private String description;
}

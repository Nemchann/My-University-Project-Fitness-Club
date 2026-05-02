package com.nemchann.fitnessbackend.schedule.dto;

import com.nemchann.fitnessbackend.schedule.entity.WorkoutType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WorkoutCreateDto {

    @NotBlank
    private String workoutName;

    @NotBlank
    private String workoutType;

    @NotBlank
    private String description;

}

package com.nemchann.fitnessbackend.schedule.dto;

import com.nemchann.fitnessbackend.schedule.entity.WorkoutType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

public class ScheduleCreateDto {

    @NotBlank
    private String workoutName;

    //Тут додумать
    private WorkoutType workoutType;

    private String description;

    @NotNull
    private Date scheduleDate;

    //Додумать
    private UUID trainerId;

    @NotNull
    private OffsetDateTime startTime;

    @NotNull
    private OffsetDateTime endTime;

    @NotNull
    private Integer maxParticipants;

}

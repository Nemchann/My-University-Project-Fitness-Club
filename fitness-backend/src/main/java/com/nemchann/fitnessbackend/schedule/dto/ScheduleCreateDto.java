package com.nemchann.fitnessbackend.schedule.dto;

import com.nemchann.fitnessbackend.schedule.entity.Schedule;
import com.nemchann.fitnessbackend.schedule.entity.WorkoutType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

@Data
public class ScheduleCreateDto {

    @NotBlank
    private Integer workoutId;

    @NotNull
    private LocalDate scheduleDate;

    @NotNull
    private UUID trainerId;

    @NotNull
    private OffsetDateTime startTime;

    @NotNull
    private OffsetDateTime endTime;

    @NotNull
    private Integer maxParticipants;

    @NotNull
    private String roomName;

    private OffsetDateTime createdAt;

}

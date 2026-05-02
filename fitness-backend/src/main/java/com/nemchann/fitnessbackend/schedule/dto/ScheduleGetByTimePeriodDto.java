package com.nemchann.fitnessbackend.schedule.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ScheduleGetByTimePeriodDto {

    @NotNull
    private LocalTime start;

    @NotNull
    private LocalTime end;
}

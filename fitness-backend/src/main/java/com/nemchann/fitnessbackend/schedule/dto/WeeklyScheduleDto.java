package com.nemchann.fitnessbackend.schedule.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WeeklyScheduleDto {

    @NotNull
    private LocalDate date;
}

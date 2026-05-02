package com.nemchann.fitnessbackend.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class ScheduleGetByTimeDto {

    @NotNull
    private LocalDate date;
}

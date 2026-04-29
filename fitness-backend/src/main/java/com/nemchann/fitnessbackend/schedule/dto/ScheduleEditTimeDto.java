package com.nemchann.fitnessbackend.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Date;

@Data
public class ScheduleEditTimeDto {

    private Integer id;

    @NotBlank
    private Date scheduleDate;

    @NotBlank
    private OffsetDateTime startTime;

    @NotBlank
    private OffsetDateTime endTime;

}

package com.nemchann.fitnessbackend.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScheduleEditRoomDto {

    @NotNull
    private Integer id;

    @NotBlank
    private String room;
}

package com.nemchann.fitnessbackend.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScheduleEditRoomDto {

    @NotBlank
    private Integer id;

    @NotBlank
    private String room;
}

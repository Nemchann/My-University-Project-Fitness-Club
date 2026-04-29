package com.nemchann.fitnessbackend.schedule.dto;

import com.nemchann.fitnessbackend.schedule.entity.WorkoutType;
import com.nemchann.fitnessbackend.users.entity.User;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

@Data
public class ScheduleResponseDto {

    private Integer id;

    private String workoutName;

    private String trainerSurname;

    private String trainerName;

    private String workoutType;

    private String description;

    private Date scheduleDate;

    private OffsetDateTime startTime;

    private OffsetDateTime endTime;

    private Integer maxParticipants;

    private Integer currentParticipants;

}

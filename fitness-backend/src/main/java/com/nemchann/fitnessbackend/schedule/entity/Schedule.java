package com.nemchann.fitnessbackend.schedule.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "schedule")
@Getter @Setter
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "workout_id", nullable = false)
    private Integer workoutId;

    @Column(name = "room_id", nullable = false)
    private Integer roomId;

    @Column(name = "trainer_id", nullable = false)
    private UUID trainerId;

    @Column(name = "schedule_date", nullable = false)
    private Date scheduleDate;

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private OffsetDateTime endTime;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Column(name = "current_participants", nullable = false)
    private Integer currentParticipants;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

}

package com.nemchann.fitnessbackend.schedule.controller;

import com.nemchann.fitnessbackend.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fitness-club/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedule Controller", description = "Управление тренировками и расписанием")
public class ScheduleController {
    private final ScheduleService service;


}

package com.nemchann.fitnessbackend.schedule.controller;

import com.nemchann.fitnessbackend.schedule.dto.*;
import com.nemchann.fitnessbackend.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fitness-club/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedule Controller", description = "Управление тренировками и расписанием")
public class ScheduleController {
    private final ScheduleService service;


    @PostMapping("/create_workout")
    @Operation(summary = "Создать вид тренировки")
    public ResponseEntity<WorkoutResponseDto> createWorkout(WorkoutCreateDto workoutCreateDto){
        WorkoutResponseDto workoutResponseDto = service.createWorkout(workoutCreateDto);

        return new ResponseEntity<>(workoutResponseDto, HttpStatus.CREATED);
    }

    @PostMapping("/create_schedule")
    @Operation(summary = "Создать тренировку")
    public ResponseEntity<ScheduleResponseDto> createSchedule(ScheduleCreateDto scheduleCreateDto){
        ScheduleResponseDto scheduleResponseDto = service.createSchedule(scheduleCreateDto);

        return new ResponseEntity<>(scheduleResponseDto, HttpStatus.CREATED);
    }

    @GetMapping("get_workout/{id}")
    @Operation(summary = "Получить вид тренировки по id")
    public ResponseEntity<WorkoutResponseDto> getWorkout(Integer id){
        WorkoutResponseDto workoutResponseDto = service.getWorkoutResponse(id);

        return new ResponseEntity<>(workoutResponseDto, HttpStatus.OK);
    }

    @GetMapping("get_schedule/{id}")
    @Operation(summary = "Получить тренировку по id")
    public ResponseEntity<ScheduleResponseDto> getSchedule(Integer id){
        ScheduleResponseDto scheduleResponseDto = service.getScheduleResponse(id);

        return new ResponseEntity<>(scheduleResponseDto, HttpStatus.OK);
    }

    @DeleteMapping("delete_schedule/{id}")
    @Operation(summary = "Удалить тренировку")
    public ResponseEntity<Void> deleteSchedule(Integer id){
        service.deleteSchedule(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("cancel_schedule/{id}")
    @Operation(summary = "Отменить тренировку")
    public ResponseEntity<Void> cancelSchedule(Integer id){
        service.cancelSchedule(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("appoint_trainer/{id}")
    @Operation(summary = "Назначить тренера на тренировку")
    public ResponseEntity<ScheduleResponseDto> appointTrainer(UUID trainerId, Integer scheduleId){
        ScheduleResponseDto scheduleResponseDto = service.appointATrainer(trainerId, scheduleId);

        return new ResponseEntity<>(scheduleResponseDto, HttpStatus.OK);
    }

    @PutMapping("/change_time/{id}")
    @Operation(summary = "")
    public ResponseEntity<ScheduleResponseDto> changeTime(ScheduleEditTimeDto scheduleEditTimeDto){
        ScheduleResponseDto scheduleResponseDto = service.editTime(scheduleEditTimeDto);

        return new ResponseEntity<>(scheduleResponseDto, HttpStatus.OK);
    }

    @PutMapping("/change_room/{id}")
    @Operation(summary = "Поменять комнату проведения тренировки")
    public ResponseEntity<ScheduleResponseDto> changeRoom(ScheduleEditRoomDto editRoomDto){
        ScheduleResponseDto scheduleResponseDto = service.editScheduleRoom(editRoomDto);

        return new ResponseEntity<>(scheduleResponseDto, HttpStatus.OK);
    }

    @PutMapping("/change_schedule_workout/{id}")
    @Operation(summary = "Поменять вид тренировки у проводимой тренировки")
    public ResponseEntity<ScheduleResponseDto> changeWorkout(Integer scheduleId, ScheduleEditWorkoutDto editWorkoutDto){
        ScheduleResponseDto scheduleResponseDto = service.editScheduleWorkout(scheduleId, editWorkoutDto);

        return new ResponseEntity<>(scheduleResponseDto, HttpStatus.OK);
    }

    @GetMapping("/get_schedules_by_week")
    @Operation(summary = "Получить все тренировки на данной неделе")
    public ResponseEntity<List<ScheduleResponseDto>> getSchedulesByWeek(WeeklyScheduleDto weeklyScheduleDto){
        List<ScheduleResponseDto> scheduleResponseDtos = service.getWeeklySchedule(weeklyScheduleDto);

        return new ResponseEntity<>(scheduleResponseDtos, HttpStatus.OK);
    }

    @GetMapping("/get_schedules_by_time_range")
    @Operation(summary = "Получить все сегодняшние тренировки в заданном промежутке времени")
    public ResponseEntity<List<ScheduleResponseDto>> getSchedulesByTimeRange(ScheduleGetByTimePeriodDto timePeriodDto){
        List<ScheduleResponseDto> scheduleResponseDtos = service.getTodaySchedulesByTimeRange(timePeriodDto);

        return new ResponseEntity<>(scheduleResponseDtos, HttpStatus.OK);
    }


}

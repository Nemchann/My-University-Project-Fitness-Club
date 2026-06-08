package com.nemchann.fitnessbackend.schedule.controller;

import com.nemchann.fitnessbackend.schedule.dto.*;
import com.nemchann.fitnessbackend.schedule.repository.WorkoutRepository;
import com.nemchann.fitnessbackend.schedule.service.ScheduleService;
import com.nemchann.fitnessbackend.users.dto.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fitness-club/schedules")
//@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@Tag(name = "Schedule Controller", description = "Управление тренировками и расписанием")
public class ScheduleController {
    private final ScheduleService service;


    @PostMapping("/workout")
    @Operation(summary = "Создать вид тренировки")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Вид тренировки успешно добавлен",
                    content = @Content(schema = @Schema(implementation = WorkoutResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Не найден тип тренировки",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Данный вид тренировки уже существует",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<WorkoutResponseDto> createWorkout(@Valid @RequestBody WorkoutCreateDto workoutCreateDto){
        WorkoutResponseDto workoutResponseDto = service.createWorkout(workoutCreateDto);

        return new ResponseEntity<>(workoutResponseDto, HttpStatus.CREATED);
    }

    @PostMapping("/schedule")
    @Operation(summary = "Создать тренировку")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Тренировка успешно добавлена",
                    content = @Content(schema = @Schema(implementation = ScheduleResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Не найден вид тренировки по id или зал по названию",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "На данное время данный зал занят",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Время начала тренировки позже чем время конца" +
                    " или вместимость комнаты меньше, чем максимальное количество клиентов",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<ScheduleResponseDto> createSchedule(@Valid @RequestBody ScheduleCreateDto scheduleCreateDto){
        ScheduleResponseDto scheduleResponseDto = service.createSchedule(scheduleCreateDto);

        return new ResponseEntity<>(scheduleResponseDto, HttpStatus.CREATED);
    }

    @GetMapping("/workouts")
    @Operation(summary = "Все виды тренировок")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Тренировки успешно получены",
                    content = @Content(schema = @Schema(implementation = List.class))
            )
    })
    public ResponseEntity<List<WorkoutResponseDto>> getWorkouts(){
        List<WorkoutResponseDto> responseDtos = service.getAllWorkouts();

        return new ResponseEntity<>(responseDtos, HttpStatus.OK);
    }

    @GetMapping("/workout/{id}")
    @Operation(summary = "Получить вид тренировки по id")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Вид тренировки успешно получен",
                    content = @Content(schema = @Schema(implementation = WorkoutResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Не найден вид тренировки по id",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<WorkoutResponseDto> getWorkout(@PathVariable Integer id){
        WorkoutResponseDto workoutResponseDto = service.getWorkoutResponse(id);

        return new ResponseEntity<>(workoutResponseDto, HttpStatus.OK);
    }

    @GetMapping("/schedule/{id}")
    @Operation(summary = "Получить тренировку по id")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Тренировка успешно получена",
                    content = @Content(schema = @Schema(implementation = ScheduleResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Не найдена тренировка по id",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<ScheduleResponseDto> getSchedule(@PathVariable Integer id){
        ScheduleResponseDto scheduleResponseDto = service.getScheduleResponse(id);

        return new ResponseEntity<>(scheduleResponseDto, HttpStatus.OK);
    }

//    @DeleteMapping("/delete_schedule/{id}")
//    @Operation(summary = "Удалить тренировку")
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "204",
//                    description = "Тренировка успешно удалена"
//            ),
//            @ApiResponse(
//                    responseCode = "404",
//                    description = "Не найдена тренировка по id",
//                    content = @Content(schema = @Schema(implementation = String.class))
//            )
//    })
//    public ResponseEntity<Void> deleteSchedule(@PathVariable Integer id){
//        service.deleteSchedule(id);
//
//        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//    }

    @DeleteMapping("/cancel/{id}")
    @Operation(summary = "Отменить тренировку")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Тренировка успешно отменена"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Не найдена тренировка по id",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<Void> cancelSchedule(@PathVariable Integer id){
        service.cancelSchedule(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/appoint_trainer/{scheduleId}")
    @Operation(summary = "Назначить тренера на тренировку")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Тренер успешно назначен",
                    content = @Content(schema = @Schema(implementation = ScheduleResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Указанный пользователь - не тренер",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Не найдена тренер по id",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Тренер уже задействован в другой тренировке",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<ScheduleResponseDto> appointTrainer(
            @Valid @RequestBody UUID trainerId, @PathVariable Integer scheduleId){
        ScheduleResponseDto scheduleResponseDto = service.appointATrainer(trainerId, scheduleId);

        return new ResponseEntity<>(scheduleResponseDto, HttpStatus.OK);
    }

    @PutMapping("/change_time")
    @Operation(summary = "Поменять время у тренировки")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Время успешно изменено",
                    content = @Content(schema = @Schema(implementation = ScheduleResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Не найдена тренировка по id",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Зал уже занят в это время",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Введены нелогичные данные",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<ScheduleResponseDto> changeTime(@Valid @RequestBody ScheduleEditTimeDto scheduleEditTimeDto){
        ScheduleResponseDto scheduleResponseDto = service.editTime(scheduleEditTimeDto);

        return new ResponseEntity<>(scheduleResponseDto, HttpStatus.OK);
    }

    @PutMapping("/change_room")
    @Operation(summary = "Поменять комнату проведения тренировки")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Зал успешно изменен",
                    content = @Content(schema = @Schema(implementation = ScheduleResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Не найдена тренировка по id или зал по названию",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Зал уже занят в это время",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Вместимость зала меньше, чем количество желающих попасть на тренировку",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<ScheduleResponseDto> changeRoom(@Valid @RequestBody ScheduleEditRoomDto editRoomDto){
        ScheduleResponseDto scheduleResponseDto = service.editScheduleRoom(editRoomDto);

        return new ResponseEntity<>(scheduleResponseDto, HttpStatus.OK);
    }

    @PutMapping("/change_workout/{scheduleId}")
    @Operation(summary = "Поменять вид тренировки у проводимой тренировки")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Вид тренировки успешно изменен",
                    content = @Content(schema = @Schema(implementation = ScheduleResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Не найдена тренировка по id или зал по названию",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Зал уже занят в это время",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<ScheduleResponseDto> changeWorkout(
            @PathVariable Integer scheduleId, @Valid @RequestBody ScheduleEditWorkoutDto editWorkoutDto){
        ScheduleResponseDto scheduleResponseDto = service.editScheduleWorkout(scheduleId, editWorkoutDto);

        return new ResponseEntity<>(scheduleResponseDto, HttpStatus.OK);
    }

    @GetMapping("/by_week")
    @Operation(summary = "Получить все тренировки на данной неделе")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Тренировки на неделю успешно получены",
                    content = @Content(schema = @Schema(implementation = List.class))
            )
    })
    public ResponseEntity<List<ScheduleResponseDto>> getSchedulesByWeek(@Valid @RequestBody WeeklyScheduleDto weeklyScheduleDto){
        List<ScheduleResponseDto> scheduleResponseDtos = service.getWeeklySchedule(weeklyScheduleDto);

        return new ResponseEntity<>(scheduleResponseDtos, HttpStatus.OK);
    }

    @GetMapping("/by_time_range")
    @Operation(summary = "Получить все сегодняшние тренировки в заданном промежутке времени")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Тренировки успешно получены",
                    content = @Content(schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Время введено некорректно",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<List<ScheduleResponseDto>> getSchedulesByTimeRange(@Valid @RequestBody ScheduleGetByTimePeriodDto timePeriodDto){
        List<ScheduleResponseDto> scheduleResponseDtos = service.getTodaySchedulesByTimeRange(timePeriodDto);

        return new ResponseEntity<>(scheduleResponseDtos, HttpStatus.OK);
    }

    @GetMapping("/by_date")
    @Operation(summary = "Получить тренировки определенной даты")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Тренировки успешно получены",
                    content = @Content(schema = @Schema(implementation = List.class))
            )
    })
    public ResponseEntity<List<ScheduleResponseDto>> getSchedulesByDate(@Valid @RequestParam LocalDate date){
        List<ScheduleResponseDto> responseDtos = service.findSchedulesByDate(date);

        return new ResponseEntity<>(responseDtos, HttpStatus.OK);
    }

    @GetMapping("/available")
    @Operation(summary = "Получить тренировки, на которые еще можно записаться")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Тренировки успешно получены",
                    content = @Content(schema = @Schema(implementation = Page.class))
            )
    })
    public ResponseEntity<Page<ScheduleResponseDto>> getAvailableSchedules(
            @PageableDefault(size = 10, sort = "workout") Pageable pageable){
        Page<ScheduleResponseDto> scheduleResponseDtos = service.getAvailableWorkouts(pageable);

        return new ResponseEntity<>(scheduleResponseDtos, HttpStatus.OK);
    }

    @GetMapping("/by_trainer/{trainerId}")
    @Operation(summary = "Тренировки данного тренера")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Тренировки успешно получены",
                    content = @Content(schema = @Schema(implementation = ScheduleResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Указанный пользователь - не тренер",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Не найден тренер по id",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<Page<ScheduleResponseDto>> getSchedulesByTrainer(
            @PathVariable UUID trainerId, @PageableDefault(size = 10, sort = "workout") Pageable pageable){
        Page<ScheduleResponseDto> responseDtos = service.getSchedulesByTrainer(trainerId, pageable);

        return new ResponseEntity<>(responseDtos, HttpStatus.OK);
    }


}

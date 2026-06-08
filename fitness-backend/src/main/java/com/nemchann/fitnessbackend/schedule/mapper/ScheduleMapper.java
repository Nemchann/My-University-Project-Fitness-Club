package com.nemchann.fitnessbackend.schedule.mapper;

import com.nemchann.fitnessbackend.common.exception.TrainerIsBusyException;
import com.nemchann.fitnessbackend.common.exception.WorkoutAlreadyExistsException;
import com.nemchann.fitnessbackend.schedule.dto.ScheduleCreateDto;
import com.nemchann.fitnessbackend.schedule.dto.ScheduleResponseDto;
import com.nemchann.fitnessbackend.schedule.dto.WorkoutCreateDto;
import com.nemchann.fitnessbackend.schedule.dto.WorkoutResponseDto;
import com.nemchann.fitnessbackend.schedule.entity.Room;
import com.nemchann.fitnessbackend.schedule.entity.Schedule;
import com.nemchann.fitnessbackend.schedule.entity.Workout;
import com.nemchann.fitnessbackend.schedule.entity.WorkoutType;
import com.nemchann.fitnessbackend.schedule.repository.RoomRepository;
import com.nemchann.fitnessbackend.schedule.repository.ScheduleRepository;
import com.nemchann.fitnessbackend.schedule.repository.WorkoutRepository;
import com.nemchann.fitnessbackend.schedule.repository.WorkoutTypeRepository;
import com.nemchann.fitnessbackend.users.entity.User;
import com.nemchann.fitnessbackend.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ScheduleMapper {
    private final ScheduleRepository scheduleRepository;
    private final WorkoutRepository workoutRepository;
    private final UserService userService;

    // Метод конвертации WorkoutCreateDto в Workout
    public Workout rewriteWorkoutDtoToWorkout(WorkoutCreateDto dto, WorkoutType type){

        Workout workout = new Workout();
        Optional<Workout> workoutOptional = workoutRepository.findByWorkoutName(dto.getWorkoutName());

        if(workoutOptional.isEmpty()) {
            workout.setWorkoutName(dto.getWorkoutName());
            workout.setWorkoutType(type);
            workout.setDescription(dto.getDescription());

            return workout;
        }else{
            throw new WorkoutAlreadyExistsException("Такой вид тренировки уже существует");
        }
    }

    // Метод конвертации Workout в WorkoutResponseDto
    public WorkoutResponseDto mapWorkoutToResponseDto(Workout workout){
        WorkoutResponseDto dto = new WorkoutResponseDto();

        dto.setId(workout.getId());
        dto.setWorkoutName(workout.getWorkoutName());
        dto.setWorkoutType(workout.getWorkoutTypeNameToString());
        dto.setDescription(workout.getDescription());

        return dto;
    }

    // Метод конвертации ScheduleCreateDto в Schedule
    public Schedule rewriteCreateDtoToSchedule(ScheduleCreateDto dto, Workout workout, Room room){
        Schedule schedule = new Schedule();

        User trainer = userService.getUser(dto.getTrainerId());
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();

        Optional<Schedule> scheduleOptional = scheduleRepository
                .findOverlappingTrainerSchedule(trainer.getId(), startTime, endTime);

        if (scheduleOptional.isPresent()){
            throw new TrainerIsBusyException("Тренер с логином " + trainer.getLogin() + " занят в данное время");
        }

        schedule.setWorkout(workout);
        schedule.setScheduleDate(dto.getScheduleDate());
        schedule.setTrainer(trainer);
        schedule.setRoom(room);
        schedule.setMaxParticipants(dto.getMaxParticipants());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        schedule.setCurrentParticipants(0);
        schedule.setActive(true);
        schedule.setCreatedAt(dto.getCreatedAt());

        return schedule;
    }

    // Метод конвертации Schedule в ScheduleResponseDto
    public ScheduleResponseDto mapScheduleToResponse(Schedule schedule){
        ScheduleResponseDto dto = new ScheduleResponseDto();

        dto.setId(schedule.getId());
        dto.setWorkoutName(schedule.getWorkout().getWorkoutName());
        dto.setScheduleDate(schedule.getScheduleDate());

        User trainer = schedule.getTrainer();
        // Передаем в dto имя + фамилия тренера
        if (trainer != null && trainer.getProfile() != null) {
            String fullName = trainer.getProfile().getSurname() + " " + trainer.getProfile().getSelfname();
            dto.setTrainerFullName(fullName);
        }

        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setMaxParticipants(schedule.getMaxParticipants());
        dto.setCurrentParticipants(schedule.getCurrentParticipants());
        dto.setDescription(schedule.getWorkout().getDescription());
        dto.setWorkoutType(schedule.getWorkout().getWorkoutTypeNameToString());
        dto.setRoom(schedule.getRoom().getRoomName().name());

        return dto;
    }
}

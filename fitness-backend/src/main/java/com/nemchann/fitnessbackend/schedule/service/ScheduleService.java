package com.nemchann.fitnessbackend.schedule.service;

import ch.qos.logback.core.util.Loader;
import com.nemchann.fitnessbackend.common.exception.*;
import com.nemchann.fitnessbackend.schedule.dto.*;
import com.nemchann.fitnessbackend.schedule.entity.Room;
import com.nemchann.fitnessbackend.schedule.entity.Schedule;
import com.nemchann.fitnessbackend.schedule.entity.Workout;
import com.nemchann.fitnessbackend.schedule.entity.WorkoutType;
import com.nemchann.fitnessbackend.schedule.enums.RoomEnum;
import com.nemchann.fitnessbackend.schedule.enums.WorkoutTypeEnum;
import com.nemchann.fitnessbackend.schedule.repository.RoomRepository;
import com.nemchann.fitnessbackend.schedule.repository.ScheduleRepository;
import com.nemchann.fitnessbackend.schedule.repository.WorkoutRepository;
import com.nemchann.fitnessbackend.schedule.repository.WorkoutTypeRepository;
import com.nemchann.fitnessbackend.users.entity.User;
import com.nemchann.fitnessbackend.users.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Temporal;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class ScheduleService {
    private final RoomRepository roomRepository;
    private final ScheduleRepository scheduleRepository;
    private final WorkoutRepository workoutRepository;
    private final WorkoutTypeRepository workoutTypeRepository;
    private final UserService userService;

    public ScheduleService(RoomRepository roomRepository, ScheduleRepository scheduleRepository,
                           WorkoutRepository workoutRepository, WorkoutTypeRepository workoutTypeRepository,
                           UserService userService){
        this.roomRepository = roomRepository;
        this.scheduleRepository = scheduleRepository;
        this.workoutRepository = workoutRepository;
        this.workoutTypeRepository = workoutTypeRepository;
        this.userService = userService;
    }

    //Создать вид тренировки
    public WorkoutResponseDto createWorkout(WorkoutCreateDto workoutCreateDto){
        WorkoutTypeEnum typeEnum = WorkoutTypeEnum.valueOf(workoutCreateDto.getWorkoutType().toUpperCase());

        Optional<WorkoutType> typeOptional = workoutTypeRepository.findByTypeName(typeEnum);

        if (typeOptional.isPresent()){
            WorkoutType type = typeOptional.get();
            Workout workout = new Workout();
            rewriteWorkoutDtoToWorkout(workoutCreateDto, workout, type);

            workoutRepository.save(workout);

            return mapWorkoutToResponseDto(workout);
        }else{
            throw new EntityNotFoundException("WorkoutType is not found");
        }
    }

    private void rewriteWorkoutDtoToWorkout(WorkoutCreateDto dto, Workout workout, WorkoutType type){

        workout.setWorkoutName(dto.getWorkoutName());
        workout.setWorkoutType(type);
        workout.setDescription(dto.getDescription());

    }

    private WorkoutResponseDto mapWorkoutToResponseDto(Workout workout){
        WorkoutResponseDto dto = new WorkoutResponseDto();

        dto.setId(workout.getId());
        dto.setWorkoutName(workout.getWorkoutName());
        dto.setWorkoutType(workout.getWorkoutTypeNameToString());
        dto.setDescription(workout.getDescription());

        return dto;
    }

    //Создать тренировку
    public ScheduleResponseDto createSchedule(ScheduleCreateDto createDto) {
        RoomEnum roomEnum = RoomEnum.valueOf(createDto.getRoomName().toUpperCase());

        Optional<Workout> workoutOptional = workoutRepository.findById(createDto.getWorkoutId());
        Room room = roomRepository.findByRoomName(roomEnum)
                .orElseThrow(() -> new RoomIsNotFoundException("Room is not found"));

        if(workoutOptional.isPresent()){
            Workout workout = workoutOptional.get();

            if (userService.isTrainer(createDto.getTrainerId())){
                Schedule schedule = rewriteCreateDtoToSchedule(createDto, workout, room);

                scheduleRepository.save(schedule);

                return mapScheduleToResponse(schedule);

            }else {
                throw new IsNotTrainerException("This user is not trainer");
            }

        }else{
            throw new WorkoutIsNotFoundException("Workout is not found");
        }
    }

    public Schedule rewriteCreateDtoToSchedule(ScheduleCreateDto dto, Workout workout, Room room){
        Schedule schedule = new Schedule();

        User trainer = userService.getUser(dto.getTrainerId());

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

    private ScheduleResponseDto mapScheduleToResponse(Schedule schedule){
        ScheduleResponseDto dto = new ScheduleResponseDto();

        dto.setId(schedule.getId());
        dto.setWorkoutName(schedule.getWorkout().getWorkoutName());
        dto.setScheduleDate(schedule.getScheduleDate());

//        String fullName = userService.getFullName(schedule.getTrainer());
//        dto.setTrainerFullName(fullName);
        User trainer = schedule.getTrainer();
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

        return dto;
    }

    //Найти тренировку по id
    public ScheduleResponseDto getScheduleResponse(Integer id){
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ScheduleIsNotFoundException("Schedule is not found"));

        return mapScheduleToResponse(schedule);
    }

    //Найти вид тренировки по id
    public WorkoutResponseDto getWorkoutResponse(Integer id){
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new WorkoutIsNotFoundException("Workout is not found"));

        return mapWorkoutToResponseDto(workout);
    }

    //Назначить тренера на тренировку
    @Transactional
    public ScheduleResponseDto appointATrainer(UUID trainerId, Integer scheduleId){
        User trainer = userService.getUser(trainerId);
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleIsNotFoundException("Schedule is not found"));

        if(userService.isTrainer(trainerId)){
            schedule.setTrainer(trainer);

            scheduleRepository.save(schedule);

            return mapScheduleToResponse(schedule);

        }else{
            throw new IsNotTrainerException("This user is not trainer");
        }
    }

    //Удалить тренировку
    @Transactional
    public void deleteSchedule(Integer id){
        Schedule schedule = scheduleRepository.findById(id)
                        .orElseThrow(() -> new ScheduleIsNotFoundException("Schedule is not found"));
        scheduleRepository.delete(schedule);
    }

    //Деактивировать тренировку
    @Transactional
    public void cancelSchedule(Integer id){
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ScheduleIsNotFoundException("Schedule is not found"));
        schedule.setActive(false);

        scheduleRepository.save(schedule);
    }

    //Поменять дату и время тренировки
    @Transactional
    public ScheduleResponseDto editTime(ScheduleEditTimeDto dto){
        Schedule schedule = scheduleRepository.findById(dto.getId())
                .orElseThrow(() -> new ScheduleIsNotFoundException("Schedule is not found"));

        schedule.setScheduleDate(dto.getScheduleDate());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());

        return mapScheduleToResponse(schedule);
    }

    //Поменять комнату проведения тренировки
    @Transactional
    public ScheduleResponseDto editScheduleRoom(ScheduleEditRoomDto scheduleEditRoomDto){
        ScheduleResponseDto scheduleResponseDto = new ScheduleResponseDto();

        Schedule schedule = scheduleRepository.findById(scheduleEditRoomDto.getId())
                .orElseThrow(() -> new ScheduleIsNotFoundException("Schedule is not found"));

        RoomEnum roomEnum = RoomEnum.valueOf(scheduleEditRoomDto.getRoom().toUpperCase());
        Room room = roomRepository.findByRoomName(roomEnum)
                .orElseThrow(() -> new RoomIsNotFoundException("Room is not found"));

        schedule.setRoom(room);
        scheduleRepository.save(schedule);

        return mapScheduleToResponse(schedule);

    }

    //Поменять вид тренировки
    @Transactional
    public ScheduleResponseDto editScheduleWorkout(Integer scheduleId, ScheduleEditWorkoutDto scheduleEditWorkoutDto){
        Workout workout = workoutRepository.findById(scheduleEditWorkoutDto.getWorkoutId())
                .orElseThrow(() -> new WorkoutIsNotFoundException("WorkoutIsNotFound"));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleIsNotFoundException("Schedule is not found"));

        schedule.setWorkout(workout);
        scheduleRepository.save(schedule);

        return mapScheduleToResponse(schedule);
    }

    @Transactional
    public List<ScheduleResponseDto> findSchedulesByDate(ScheduleGetByTimeDto scheduleGetByTimeDto){
        LocalDate date = scheduleGetByTimeDto.getDate();
        List<Schedule> schedules = scheduleRepository.findByScheduleDate(date);

        return schedules.stream()
                .map(this::mapScheduleToResponse)
                .toList();
    }

    @Transactional
    public List<ScheduleResponseDto> getWeeklySchedule(LocalDate date){
        LocalDateTime startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();

        LocalDateTime endOfWeek = startOfWeek.plusDays(6).with(LocalDateTime.MAX);

        return scheduleRepository.findAllByStartTimeBetweenOrderByStartTimeAsc(startOfWeek, endOfWeek)
                .stream()
                .map(this::mapScheduleToResponse)
                .toList();
    }

    //Сделать метод, который возвращает список тренировок в заданном промежутке времени с валидацией
    //самого промежутка времени
    // По возможности создать классы-мапперы

}

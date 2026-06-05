package com.nemchann.fitnessbackend.schedule.service;

import com.nemchann.fitnessbackend.booking.entity.Booking;
import com.nemchann.fitnessbackend.booking.entity.BookingStatus;
import com.nemchann.fitnessbackend.booking.entity.ClientSubscription;
import com.nemchann.fitnessbackend.booking.enums.BookingStatusEnum;
import com.nemchann.fitnessbackend.booking.repository.BookingRepository;
import com.nemchann.fitnessbackend.booking.repository.BookingStatusRepository;
import com.nemchann.fitnessbackend.booking.repository.ClientSubscriptionRepository;
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
import com.nemchann.fitnessbackend.users.repository.UserRepository;
import com.nemchann.fitnessbackend.users.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ScheduleService {
    private final RoomRepository roomRepository;
    private final ScheduleRepository scheduleRepository;
    private final WorkoutRepository workoutRepository;
    private final WorkoutTypeRepository workoutTypeRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingStatusRepository bookingStatusRepository;
    private final ClientSubscriptionRepository clientSubscriptionRepository;

    //Создать вид тренировки
    @Transactional
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

        Optional<Workout> workoutOptional = workoutRepository.findByWorkoutName(dto.getWorkoutName());

        if(workoutOptional.isEmpty()) {
            workout.setWorkoutName(dto.getWorkoutName());
            workout.setWorkoutType(type);
            workout.setDescription(dto.getDescription());
        }else{
            throw new WorkoutAlreadyExistsException("This workout already exists");
        }

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
    @Transactional
    public ScheduleResponseDto createSchedule(ScheduleCreateDto createDto) {
        RoomEnum roomEnum = RoomEnum.valueOf(createDto.getRoomName().toUpperCase());


        Room room = roomRepository.findByRoomName(roomEnum)
                .orElseThrow(() -> new RoomIsNotFoundException("Room is not found"));

        LocalDateTime newStart = createDto.getStartTime();
        LocalDateTime newEnd = createDto.getEndTime();

        if (newEnd.isBefore(newStart) || newEnd.isEqual(newStart)) {
            throw new StartEndTimeConflictException("End time must be after start time");
        }

        boolean isRoomBusy = scheduleRepository
                .existsByRoomIdAndStartTimeBeforeAndEndTimeAfterAndIsActiveTrue(room.getId(), newEnd, newStart);

        if (isRoomBusy) {
            throw new RoomAlreadyOccupiedException("Этот зал уже занят другой тренировкой в указанное время");
        }

        Workout workout = workoutRepository.findById(createDto.getWorkoutId())
                .orElseThrow(() -> new WorkoutIsNotFoundException("Workout is not found"));

        if (userService.isTrainer(createDto.getTrainerId())){
            Schedule schedule = rewriteCreateDtoToSchedule(createDto, workout, room);

            scheduleRepository.save(schedule);

            return mapScheduleToResponse(schedule);

        }else {
            throw new IsNotTrainerException("This user is not trainer");
        }

    }

    private Schedule rewriteCreateDtoToSchedule(ScheduleCreateDto dto, Workout workout, Room room){
        Schedule schedule = new Schedule();

        User trainer = userService.getUser(dto.getTrainerId());
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();

        Optional<Schedule> scheduleOptional = scheduleRepository
                .findOverlappingTrainerSchedule(trainer.getId(), startTime, endTime);

        if (scheduleOptional.isPresent()){
            throw new TrainerIsBusyException("Trainer " + trainer.getLogin() + " is busy");
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

    private ScheduleResponseDto mapScheduleToResponse(Schedule schedule){
        ScheduleResponseDto dto = new ScheduleResponseDto();

        dto.setId(schedule.getId());
        dto.setWorkoutName(schedule.getWorkout().getWorkoutName());
        dto.setScheduleDate(schedule.getScheduleDate());

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
        dto.setRoom(schedule.getRoom().getRoomName().name());

        return dto;
    }

    public List<WorkoutResponseDto> getAllWorkouts(){
        return workoutRepository.findAll()
                .stream()
                .map(this::mapWorkoutToResponseDto)
                .toList();
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

    public Schedule getSchedule(Integer id){
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ScheduleIsNotFoundException("Schedule is not found"));
    }

    //Назначить тренера на тренировку
    @Transactional
    public ScheduleResponseDto appointATrainer(UUID trainerId, Integer scheduleId){
        User trainer = userService.getUser(trainerId);
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleIsNotFoundException("Schedule is not found"));

        if (userService.isTrainer(trainerId)){
            schedule.setTrainer(trainer);

            LocalDateTime startTime = schedule.getStartTime();
            LocalDateTime endTime = schedule.getEndTime();

            Optional<Schedule> scheduleOptional = scheduleRepository
                    .findOverlappingTrainerSchedule(trainer.getId(), startTime, endTime);

            if (scheduleOptional.isPresent()){
                throw new TrainerIsBusyException("Trainer " + trainer.getLogin() + " is busy");
            }

            scheduleRepository.save(schedule);

            return mapScheduleToResponse(schedule);

        }else{
            throw new IsNotTrainerException("This user is not trainer");
        }
    }

    //Удалить тренировку
    @Transactional
    public void deleteSchedule(Integer id){
        //Добавить функциональность при бронировании. Что будет с бронированиями, если удалить тренировку?
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

        cancelBySchedule(id);

        scheduleRepository.save(schedule);
    }

    //Поменять дату и время тренировки
    @Transactional
    public ScheduleResponseDto editTime(ScheduleEditTimeDto dto){
        Schedule schedule = scheduleRepository.findById(dto.getId())
                .orElseThrow(() -> new ScheduleIsNotFoundException("Schedule is not found"));

        //Проверка на занятость зала
        Room room = schedule.getRoom();
        LocalDateTime start = schedule.getStartTime();
        LocalDateTime end = schedule.getEndTime();

        boolean isRoomBusy = scheduleRepository
                .existsByRoomIdAndStartTimeBeforeAndEndTimeAfterAndIsActiveTrue(room.getId(), end, start);

        if (isRoomBusy) {
            throw new RoomAlreadyOccupiedException("Этот зал уже занят другой тренировкой в указанное время");
        }

        schedule.setScheduleDate(dto.getScheduleDate());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());

        return mapScheduleToResponse(schedule);
    }

    //Поменять комнату проведения тренировки
    @Transactional
    public ScheduleResponseDto editScheduleRoom(ScheduleEditRoomDto scheduleEditRoomDto){

        Schedule schedule = scheduleRepository.findById(scheduleEditRoomDto.getId())
                .orElseThrow(() -> new ScheduleIsNotFoundException("Schedule is not found"));

        RoomEnum roomEnum = RoomEnum.valueOf(scheduleEditRoomDto.getRoom().toUpperCase());
        Room room = roomRepository.findByRoomName(roomEnum)
                .orElseThrow(() -> new RoomIsNotFoundException("Room is not found"));

        LocalDateTime start = schedule.getStartTime();
        LocalDateTime end = schedule.getEndTime();

        boolean isRoomBusy = scheduleRepository
                .existsByRoomIdAndStartTimeBeforeAndEndTimeAfterAndIsActiveTrue(room.getId(), end, start);

        if (isRoomBusy) {
            throw new RoomAlreadyOccupiedException("Этот зал уже занят другой тренировкой в указанное время");
        }

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

    //Получить тренировки данного дня
    //Исправить с body на сам запрос
    @Transactional
    public List<ScheduleResponseDto> findSchedulesByDate(LocalDate date){
        List<Schedule> schedules = scheduleRepository.findByScheduleDateOrderByStartTimeAsc(date);

        return schedules.stream()
                .map(this::mapScheduleToResponse)
                .toList();
    }

    //Получить тренировку на неделю
    //Можно поменять на Page
    @Transactional
    public List<ScheduleResponseDto> getWeeklySchedule(WeeklyScheduleDto dto){
        LocalDate date = dto.getDate();
        LocalDateTime startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();

        LocalDateTime startOfNextWeek = startOfWeek.plusDays(7);

        return scheduleRepository.findAllByStartTimeBetweenOrderByStartTimeAsc(startOfWeek, startOfNextWeek)
                .stream()
                .map(this::mapScheduleToResponse)
                .toList();
    }

    //Получить сегодняшние тренировки в заданном промежутке времени
    @Transactional
    public List<ScheduleResponseDto> getTodaySchedulesByTimeRange(ScheduleGetByTimePeriodDto timePeriodDto){
        LocalTime startTime = timePeriodDto.getStart();
        LocalTime endTime = timePeriodDto.getEnd();

        LocalDate today = LocalDate.now();

        LocalDateTime start = today.atTime(startTime);
        LocalDateTime end = today.atTime(endTime);

        return scheduleRepository.findAllByStartTimeBetweenOrderByStartTimeAsc(start, end)
                .stream()
                .map(this::mapScheduleToResponse)
                .toList();
    }

    //Посмотреть доступные
    @Transactional
    public Page<ScheduleResponseDto> getAvailableWorkouts(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();

        return scheduleRepository.findAvailableSchedules(now, pageable)
                .map(this::mapScheduleToResponse);
    }

    public Page<ScheduleResponseDto> getSchedulesByTrainer(UUID trainerId, Pageable pageable){

        if(userService.isTrainer(trainerId)){
            User trainer = userRepository.findById(trainerId)
                    .orElseThrow(() -> new UserNotFoundException("User is not found"));
            return scheduleRepository.findAllByTrainer(trainer, pageable)
                    .map(this::mapScheduleToResponse);
        }else{
            throw new IsNotTrainerException("You are not a trainer");
        }
    }

    //Метод, который будет использоваться в BookingService
    //Увеличить счетчик текущих посетителей
    @Transactional
    public void addParticipant(Integer scheduleId){
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleIsNotFoundException("Schedule is not found"));


        if (schedule.getCurrentParticipants() >= schedule.getMaxParticipants()){
            throw new IllegalStateException("No free slots available");
        }
        Integer currentParticipants = schedule.getCurrentParticipants() + 1;

        schedule.setCurrentParticipants(currentParticipants);

        scheduleRepository.save(schedule);
    }

    //Уменьшить счетчик текущих посетителей
    @Transactional
    public void removeParticipant(Schedule schedule){

        int currentParticipants = schedule.getCurrentParticipants() - 1;

        if(currentParticipants < 0){
            throw new ArithmeticException("Current participants mustn't be negative");
        }

        schedule.setCurrentParticipants(currentParticipants);

        scheduleRepository.save(schedule);
    }

    @Transactional
    public void cancelBySchedule(Integer scheduleId) {
        // 1. Находим все неотмененные бронирования на эту тренировку
        BookingStatus cancelledBookingStatus = bookingStatusRepository.findByBookingStatusName(BookingStatusEnum.CANCELLED)
                .orElseThrow(() -> new BookingNotFoundException("Booking status is not found"));

        List<Booking> bookings = bookingRepository.findAllByScheduleIdAndBookingStatus_BookingStatusNameNot(
                scheduleId, BookingStatusEnum.CANCELLED
        );

        for (Booking booking : bookings) {
            // Возвращаем занятие, если абонемент не безлимитный
            ClientSubscription sub = clientSubscriptionRepository
                    .findCurrentActiveSubscription(booking.getClient().getId(), LocalDate.now())
                    .orElseThrow(() -> new ClientSubscriptionNotFoundException("Your subscription is not found"));

            if (!sub.getSubscription().isUnlimited()) {
                sub.setRemainingVisits(sub.getRemainingVisits() + 1);
                clientSubscriptionRepository.save(sub); // Возвращаем занятие на баланс
            }

            booking.setBookingStatus(cancelledBookingStatus);
            bookingRepository.save(booking);
        }

    }


    // Добавить метод получения тренировок по типу тренировок (workoutType)
    // По возможности создать классы-мапперы

}

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
import com.nemchann.fitnessbackend.schedule.mapper.ScheduleMapper;
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
import java.time.temporal.ChronoUnit;
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
    private final ScheduleMapper mapper;

    // Создать вид тренировки
    @Transactional
    public WorkoutResponseDto createWorkout(WorkoutCreateDto workoutCreateDto){
        WorkoutTypeEnum typeEnum = WorkoutTypeEnum.valueOf(workoutCreateDto.getWorkoutType().toUpperCase());

        Optional<WorkoutType> typeOptional = workoutTypeRepository.findByTypeName(typeEnum);

        if (typeOptional.isPresent()){
            WorkoutType type = typeOptional.get();
            Workout workout = mapper.rewriteWorkoutDtoToWorkout(workoutCreateDto, type);

            workoutRepository.save(workout);

            return mapper.mapWorkoutToResponseDto(workout);
        }else{
            throw new EntityNotFoundException("Не найден тип тренировки");
        }
    }


    // Метод создания тренировки
    @Transactional
    public ScheduleResponseDto createSchedule(ScheduleCreateDto createDto) {
        RoomEnum roomEnum = RoomEnum.valueOf(createDto.getRoomName().toUpperCase());

        // Проверка зала
        Room room = roomRepository.findByRoomName(roomEnum)
                .orElseThrow(() -> new RoomIsNotFoundException("Зал не найден"));

        // Проверка на количество посетителей
        if (room.getCapacity() < createDto.getMaxParticipants()){
            throw new RoomCapacityExceededException("Максимальное количество участников больше, чем вместимость зала");
        }

        LocalDateTime newStart = createDto.getStartTime();
        LocalDateTime newEnd = createDto.getEndTime();

        // Проверка на адекватность временных рамок (админа)
        if (newEnd.isBefore(newStart) || newEnd.isEqual(newStart)) {
            throw new StartEndTimeConflictException("Время окончания тренировки должно быть позже, чем время начала тренировки");
        }

        long durationInMinutes = ChronoUnit.MINUTES.between(newStart, newEnd);

        // Проверка на длительность тренировки
        if (durationInMinutes < 30 || durationInMinutes > 120) {
            throw new InvalidScheduleDurationException("Длительность тренировки должна быть не менее 30 минут и не более двух часов");
        }

        boolean isRoomBusy = scheduleRepository
                .existsByRoomIdAndStartTimeBeforeAndEndTimeAfterAndIsActiveTrue(room.getId(), newEnd, newStart);

        // Проверка на занятость зала в указанное время
        if (isRoomBusy) {
            throw new RoomAlreadyOccupiedException("Этот зал уже занят другой тренировкой в указанное время");
        }

        Workout workout = workoutRepository.findById(createDto.getWorkoutId())
                .orElseThrow(() -> new WorkoutIsNotFoundException("Данный вид тренировки не найден"));

        // Проверка, что указанный пользователь тренер
        if (userService.isTrainer(createDto.getTrainerId())){
            Schedule schedule = mapper.rewriteCreateDtoToSchedule(createDto, workout, room);

            scheduleRepository.save(schedule);

            return mapper.mapScheduleToResponse(schedule);

        }else {
            throw new IsNotTrainerException("Данный пользователь не является тренером, он не умеет проводить тренировки");
        }

    }


    // Список всех видом тренировок
    public List<WorkoutResponseDto> getAllWorkouts(){
        return workoutRepository.findAll()
                .stream()
                .map(mapper::mapWorkoutToResponseDto)
                .toList();
    }

    // Найти тренировку по id
    public ScheduleResponseDto getScheduleResponse(Integer id){
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ScheduleIsNotFoundException("Тренировка не найдена"));

        return mapper.mapScheduleToResponse(schedule);
    }

    // Найти вид тренировки по id
    public WorkoutResponseDto getWorkoutResponse(Integer id){
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new WorkoutIsNotFoundException("Данный вид тренировки не найден"));

        return mapper.mapWorkoutToResponseDto(workout);
    }

    // Метод для BookingService
    public Schedule getSchedule(Integer id){
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ScheduleIsNotFoundException("Тренировка не найдена"));
    }

    // Назначение тренера на тренировку
    @Transactional
    public ScheduleResponseDto appointATrainer(UUID trainerId, Integer scheduleId){
        User trainer = userService.getUser(trainerId);
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleIsNotFoundException("Тренировка не найдена"));

        if (userService.isTrainer(trainerId)){

            LocalDateTime startTime = schedule.getStartTime();
            LocalDateTime endTime = schedule.getEndTime();

            // Находим тренировку, где есть данный тренер в указанное время
            Optional<Schedule> scheduleOptional = scheduleRepository
                    .findOverlappingTrainerSchedule(trainer.getId(), startTime, endTime);

            if (scheduleOptional.isPresent()){
                throw new TrainerIsBusyException("Тренер с логином " + trainer.getLogin() + " занят в данное время");
            }

            schedule.setTrainer(trainer);
            scheduleRepository.save(schedule);

            return mapper.mapScheduleToResponse(schedule);

        }else{
            throw new IsNotTrainerException("Данный пользователь не является тренером");
        }
    }

    // Удаление тренировки
//    @Transactional
//    public void deleteSchedule(Integer id){
//        Schedule schedule = scheduleRepository.findById(id)
//                        .orElseThrow(() -> new ScheduleIsNotFoundException("Тренировка не найдена"));
//        scheduleRepository.delete(schedule);
//    }

    // Деактивация тренировки
    @Transactional
    public void cancelSchedule(Integer id){
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ScheduleIsNotFoundException("Тренировка не найдена"));
        schedule.setActive(false);

        // Заодно отменяем бронирования клиентов и возвращаем одну тренировку на баланс абонемента
        cancelBySchedule(id);

        scheduleRepository.save(schedule);
    }

    // Изменение даты и времени тренировки
    @Transactional
    public ScheduleResponseDto editTime(ScheduleEditTimeDto dto){
        Schedule schedule = scheduleRepository.findById(dto.getId())
                .orElseThrow(() -> new ScheduleIsNotFoundException("Тренировка не найдена"));

        // Проверка на занятость зала
        Room room = schedule.getRoom();
        LocalDateTime start = schedule.getStartTime();
        LocalDateTime end = schedule.getEndTime();

        // Проверка на адекватность временных рамок (админа)
        if (end.isBefore(start) || end.isEqual(start)) {
            throw new StartEndTimeConflictException("Время окончания тренировки должно быть позже, чем время начала тренировки");
        }

        long durationInMinutes = ChronoUnit.MINUTES.between(start, end);

        // Проверка на длительность тренировки
        if (durationInMinutes < 30 || durationInMinutes > 120) {
            throw new InvalidScheduleDurationException("Длительность тренировки должна быть не менее 30 минут и не более двух часов");
        }

        LocalDateTime startDto = dto.getStartTime();
        LocalDateTime endDto = dto.getEndTime();

        boolean isRoomBusy = scheduleRepository
                .existsByRoomIdAndStartTimeBeforeAndEndTimeAfterAndIsActiveTrue(room.getId(), endDto, startDto);

        if (isRoomBusy) {
            throw new RoomAlreadyOccupiedException("Этот зал уже занят другой тренировкой в указанное время");
        }

        schedule.setScheduleDate(dto.getScheduleDate());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());

        return mapper.mapScheduleToResponse(schedule);
    }

    // Изменение комнаты проведения тренировки
    @Transactional
    public ScheduleResponseDto editScheduleRoom(ScheduleEditRoomDto scheduleEditRoomDto){

        Schedule schedule = scheduleRepository.findById(scheduleEditRoomDto.getId())
                .orElseThrow(() -> new ScheduleIsNotFoundException("Тренировка не найдена"));

        RoomEnum roomEnum = RoomEnum.valueOf(scheduleEditRoomDto.getRoom().toUpperCase());
        Room room = roomRepository.findByRoomName(roomEnum)
                .orElseThrow(() -> new RoomIsNotFoundException("Зал не найден"));

        LocalDateTime start = schedule.getStartTime();
        LocalDateTime end = schedule.getEndTime();

        boolean isRoomBusy = scheduleRepository
                .existsByRoomIdAndStartTimeBeforeAndEndTimeAfterAndIsActiveTrue(room.getId(), end, start);

        // Проверка на занятость зала
        if (isRoomBusy) {
            throw new RoomAlreadyOccupiedException("Этот зал уже занят другой тренировкой в указанное время");
        }

        if (room.getCapacity() < schedule.getMaxParticipants()){
            throw new RoomCapacityExceededException("Слишком много участников для данного зала");
        }

        schedule.setRoom(room);
        scheduleRepository.save(schedule);

        return mapper.mapScheduleToResponse(schedule);

    }

    // Изменение вида тренировки
    @Transactional
    public ScheduleResponseDto editScheduleWorkout(Integer scheduleId, ScheduleEditWorkoutDto scheduleEditWorkoutDto){
        Workout workout = workoutRepository.findById(scheduleEditWorkoutDto.getWorkoutId())
                .orElseThrow(() -> new WorkoutIsNotFoundException("Данный вид тренировки не найден"));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleIsNotFoundException("Тренировка не найдена"));

        schedule.setWorkout(workout);
        scheduleRepository.save(schedule);

        return mapper.mapScheduleToResponse(schedule);
    }

    // Получить тренировки данного дня. List - потому что тренировок не так много в один день
    @Transactional
    public List<ScheduleResponseDto> findSchedulesByDate(LocalDate date){
        List<Schedule> schedules = scheduleRepository.findByScheduleDateOrderByStartTimeAsc(date);

        return schedules.stream()
                .map(mapper::mapScheduleToResponse)
                .toList();
    }

    // Получить тренировки на текущую неделю
    @Transactional
    public List<ScheduleResponseDto> getWeeklySchedule(WeeklyScheduleDto dto){
        LocalDate date = dto.getDate();
        LocalDateTime startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();

        LocalDateTime startOfNextWeek = startOfWeek.plusDays(7);

        return scheduleRepository.findAllByStartTimeBetweenOrderByStartTimeAsc(startOfWeek, startOfNextWeek)
                .stream()
                .map(mapper::mapScheduleToResponse)
                .toList();
    }

    // Получить сегодняшние тренировки в заданном промежутке времени
    @Transactional
    public List<ScheduleResponseDto> getTodaySchedulesByTimeRange(ScheduleGetByTimePeriodDto timePeriodDto){
        LocalTime startTime = timePeriodDto.getStart();
        LocalTime endTime = timePeriodDto.getEnd();

        LocalDate today = LocalDate.now();

        LocalDateTime start = today.atTime(startTime);
        LocalDateTime end = today.atTime(endTime);

        if (end.isBefore(start) || end.isEqual(start)) {
            throw new StartEndTimeConflictException("Время окончания тренировки должно быть позже, чем время начала тренировки");
        }

        return scheduleRepository.findAllByStartTimeBetweenOrderByStartTimeAsc(start, end)
                .stream()
                .map(mapper::mapScheduleToResponse)
                .toList();
    }

    // Посмотреть все доступные тренировки
    @Transactional
    public Page<ScheduleResponseDto> getAvailableWorkouts(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();

        return scheduleRepository.findAvailableSchedules(now, pageable)
                .map(mapper::mapScheduleToResponse);
    }

    // Получить тренировки данного тренера
    public Page<ScheduleResponseDto> getSchedulesByTrainer(UUID trainerId, Pageable pageable){

        if(userService.isTrainer(trainerId)){
            User trainer = userRepository.findById(trainerId)
                    .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

            return scheduleRepository.findAllByTrainer(trainer, pageable)
                    .map(mapper::mapScheduleToResponse);
        }else{
            throw new IsNotTrainerException("Это не тренер");
        }
    }

    // Метод, который будет использоваться в BookingService
    // Увеличение счетчика текущих посетителей
    @Transactional
    public void addParticipant(Integer scheduleId){
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleIsNotFoundException("Тренировка не найдена"));

        // Если кончились места
        if (schedule.getCurrentParticipants() >= schedule.getMaxParticipants()){
            throw new IllegalStateException("Нет доступных мест");
        }
        Integer currentParticipants = schedule.getCurrentParticipants() + 1;

        schedule.setCurrentParticipants(currentParticipants);

        scheduleRepository.save(schedule);
    }

    // Уменьшение счетчика текущих посетителей
    @Transactional
    public void removeParticipant(Schedule schedule){

        int currentParticipants = schedule.getCurrentParticipants() - 1;

        if(currentParticipants < 0){
            throw new ArithmeticException("Количество посетителей не может быть отрицательным");
        }

        schedule.setCurrentParticipants(currentParticipants);

        scheduleRepository.save(schedule);
    }

    // Отменить бронирования пользователей при отмене тренировки админом
    @Transactional
    public void cancelBySchedule(Integer scheduleId) {
        // Находим все неотмененные бронирования на эту тренировку
        BookingStatus cancelledBookingStatus = bookingStatusRepository.findByBookingStatusName(BookingStatusEnum.CANCELLED)
                .orElseThrow(() -> new BookingStatusNotFoundException("Статус бронирования не найден"));

        List<Booking> bookings = bookingRepository.findAllByScheduleIdAndBookingStatus_BookingStatusNameNot(
                scheduleId, BookingStatusEnum.CANCELLED
        );

        for (Booking booking : bookings) {
            // Возвращаем занятие, если абонемент не безлимитный
            ClientSubscription sub = clientSubscriptionRepository
                    .findCurrentActiveSubscription(booking.getClient().getId(), LocalDate.now())
                    .orElseThrow(() -> new ClientSubscriptionNotFoundException("Ваш абонемент не найден"));

            if (!sub.getSubscription().isUnlimited()) {
                sub.setRemainingVisits(sub.getRemainingVisits() + 1);
                clientSubscriptionRepository.save(sub); // Возвращаем занятие на баланс
            }

            booking.setBookingStatus(cancelledBookingStatus);
            bookingRepository.save(booking);
        }

    }

}
package com.nemchann.fitnessbackend.booking.service;

import com.nemchann.fitnessbackend.booking.dto.*;
import com.nemchann.fitnessbackend.booking.entity.*;
import com.nemchann.fitnessbackend.booking.enums.BookingStatusEnum;
import com.nemchann.fitnessbackend.booking.enums.SubscriptionStatusEnum;
import com.nemchann.fitnessbackend.booking.repository.*;
import com.nemchann.fitnessbackend.common.exception.*;
import com.nemchann.fitnessbackend.schedule.entity.Schedule;
import com.nemchann.fitnessbackend.schedule.entity.Workout;
import com.nemchann.fitnessbackend.schedule.service.ScheduleService;
import com.nemchann.fitnessbackend.users.entity.Profile;
import com.nemchann.fitnessbackend.users.entity.User;
import com.nemchann.fitnessbackend.users.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingStatusRepository bookingStatusRepository;
    private final ClientSubscriptionRepository clientSubscriptionRepository; //Про абонементы
    private final SubscriptionRepository subscriptionRepository; //Про абонементы
    private final SubscriptionStatusRepository subscriptionStatusRepository; //Про абонементы
    private final ScheduleService scheduleService;
    private final UserService userService;


    @Transactional
    public BookingResponseDto createBooking(BookingCreateDto createDto){

        Booking booking = rewriteFromCreateDto(createDto);

        //Если эта тренировка есть, но со статусом CANCELLED, то можно записаться
        if (bookingRepository.existsByClientIdAndScheduleId(createDto.getUserId(), createDto.getScheduleId())){
            if (!booking.getBookingStatus().getBookingStatusName().equals(BookingStatusEnum.CANCELLED)){
                throw new AlreadyBookedException("You've already booked this schedule");
            }
        }

        //Если бронировать тренировку за 2 часа до нее и позже
        LocalDateTime edgeTime = LocalDateTime.now().plusHours(2);
        if (booking.getSchedule().getStartTime().isBefore(edgeTime)){
            throw new BookingTooLateException("It is too late to book that schedule");
        }

        ClientSubscription currentSub = clientSubscriptionRepository
                .findCurrentActiveSubscription(booking.getClient().getId(), createDto.getCreatedAt().toLocalDate())
                .orElseThrow(() -> new VisitsEndedException("Your visits ended. Buy new subscription"));

        try {
            // Пытаемся занять место в зале
            scheduleService.addParticipant(createDto.getScheduleId());

            BookingStatus acceptedStatus = bookingStatusRepository.findByBookingStatusName(BookingStatusEnum.ACCEPTED)
                    .orElseThrow(() -> new BookingNotFoundException("Booking status is not found"));
            booking.setBookingStatus(acceptedStatus);

            // Обрабатываем списание занятия или переход на новый абонемент
            handleSubscriptionProcessing(currentSub, booking.getClient().getId());

        } catch (IllegalStateException e) {
            // Если места кончились, отменяем бронь
            BookingStatus cancelledStatus = bookingStatusRepository.findByBookingStatusName(BookingStatusEnum.CANCELLED)
                    .orElseThrow(() -> new BookingNotFoundException("Booking status is not found"));
            booking.setBookingStatus(cancelledStatus);
        }

        bookingRepository.save(booking);
        return mapToResponseDto(booking);

    }

    private void handleSubscriptionProcessing(ClientSubscription currentSub, UUID clientId) {
        Subscription subscription = currentSub.getSubscription();
        boolean shouldExpire = false;

        if (!subscription.isUnlimited()) {
            // Если пакетный - уменьшаем количество визитов
            int remaining = currentSub.getRemainingVisits();
            if (remaining > 0) {
                currentSub.setRemainingVisits(remaining - 1);
                clientSubscriptionRepository.save(currentSub);

                // Если это было самое последнее занятие - помечаем, что этот абонемент пора закрыть
//                if (currentSub.getRemainingVisits() == 0) {
//                    shouldExpire = true;
//                }
            } else {
                shouldExpire = true;
            }
        } else {
            // Если безлимитный — проверяем только срок годности
            if (currentSub.getEndDate().isBefore(LocalDate.now())) {
                shouldExpire = true;
            }
        }

        // Если абонемент исчерпан (по дням или по занятиям) — закрываем его и ищем замену
        if (shouldExpire) {
            // Переводим текущий абонемент в LAPSED
            SubscriptionStatus lapsedStatus = subscriptionStatusRepository
                    .findBySubscriptionStatusName(SubscriptionStatusEnum.LAPSED)
                    .orElseThrow(() -> new SubscriptionStatusNotFoundException("Subscription status LAPSED not found"));

            currentSub.setSubscriptionStatus(lapsedStatus);
            clientSubscriptionRepository.save(currentSub);

            // Ищем следующий абонемент в очереди (PENDING)
            ClientSubscription nextSub = clientSubscriptionRepository
                    .findNextPendingSubscription(clientId)
                    .orElseThrow(() -> new VisitsEndedException("Your visits ended. No other subscriptions in queue."));

            // Активируем следующий абонемент (ACTIVE)
            SubscriptionStatus activeStatus = subscriptionStatusRepository
                    .findBySubscriptionStatusName(SubscriptionStatusEnum.ACTIVE)
                    .orElseThrow(() -> new SubscriptionStatusNotFoundException("Subscription status ACTIVE not found"));

            nextSub.setSubscriptionStatus(activeStatus);
            nextSub.setStartDate(LocalDate.now());
            nextSub.setEndDate(LocalDate.now().plusDays(nextSub.getSubscription().getDurationDays()));

            // Если новый абонемент тоже пакетный, сразу списываем с него ПЕРВОЕ занятие
            if (!nextSub.getSubscription().isUnlimited()) {
                nextSub.setRemainingVisits(nextSub.getRemainingVisits() - 1);
            }

            clientSubscriptionRepository.save(nextSub);
            System.out.println("Старый абонемент закрыт, новый активирован");
        }
    }

    private BookingResponseDto mapToResponseDto(Booking booking){
        BookingResponseDto responseDto = new BookingResponseDto();
        Schedule schedule = booking.getSchedule();
        Workout workout = schedule.getWorkout();

        User trainer = schedule.getTrainer();

        Profile profile = trainer.getProfile();

        String trainerFullName = profile.getSelfname() + " " + profile.getSurname();

        responseDto.setTrainerFullName(trainerFullName);

        responseDto.setBookingId(booking.getId());

        BookingStatusEnum bookingStatusEnum = booking.getBookingStatus().getBookingStatusName();
        responseDto.setStatus(bookingStatusEnum.name());

        responseDto.setScheduleName(workout.getWorkoutName());

        responseDto.setScheduleDate(schedule.getScheduleDate());
        responseDto.setStartTime(schedule.getStartTime());

        return responseDto;
    }

    private BookingShortResponseDto mapToShortResponseDto(Booking booking){
        BookingShortResponseDto responseDto = new BookingShortResponseDto();
        Schedule schedule = booking.getSchedule();
        Workout workout = schedule.getWorkout();

        User trainer = schedule.getTrainer();
        Profile profile = trainer.getProfile();

        BookingStatusEnum bookingStatusEnum = booking.getBookingStatus().getBookingStatusName();
        responseDto.setStatus(bookingStatusEnum.name());

        responseDto.setBookingId(booking.getId());

        responseDto.setStatus(bookingStatusEnum.name());

        responseDto.setScheduleName(workout.getWorkoutName());

        responseDto.setTrainerFullName(profile.getSelfname() + " " + profile.getSurname());

        responseDto.setScheduleDate(schedule.getScheduleDate());
        responseDto.setStartTime(schedule.getStartTime());

        return responseDto;
    }

    private UserInScheduleDto mapToUserScheduleDto(Booking booking){
        User user = booking.getClient();
        Profile profile = user.getProfile();

        UserInScheduleDto userInScheduleDto = new UserInScheduleDto();

        userInScheduleDto.setFullName(profile.getSurname() + " " + profile.getSelfname());
        userInScheduleDto.setPhone(profile.getPhone());
        userInScheduleDto.setEmail(profile.getEmail());

        return userInScheduleDto;
    }

    private SubscriptionResponseDto mapToSubscriptionResponseDto(Subscription subscription){
        SubscriptionResponseDto dto = new SubscriptionResponseDto();

        dto.setSubscriptionName(subscription.getSubscriptionName());
        dto.setPrice(subscription.getPrice());
        dto.setDurationDays(subscription.getDurationDays());
        dto.setVisitsCount(subscription.getVisitsCount());

        return dto;
    }

    private ClientSubscriptionResponseDto mapToClientSubscriptionResponseDto(ClientSubscription clientSubscription){
        ClientSubscriptionResponseDto dto = new ClientSubscriptionResponseDto();

        dto.setStartDate(clientSubscription.getStartDate());
        dto.setEndDate(clientSubscription.getEndDate());

        SubscriptionStatus status = subscriptionStatusRepository.findBySubscriptionStatusName(clientSubscription.getSubscriptionStatus().getSubscriptionStatusName())
                .orElseThrow(() -> new SubscriptionStatusNotFoundException("Subscription status is not found"));
        dto.setSubscriptionStatus(status.getSubscriptionStatusName().name());

        dto.setRemainingVisits(clientSubscription.getRemainingVisits());

        return dto;
    }

    //Проверки на существование пользователя и тренировки происходят тут (внутри сервисов)
    private Booking rewriteFromCreateDto(BookingCreateDto createDto){
        Booking booking = new Booking();
        User user = userService.getUser(createDto.getUserId());
        Schedule schedule = scheduleService.getSchedule(createDto.getScheduleId());

        BookingStatus status = bookingStatusRepository.findByBookingStatusName(BookingStatusEnum.PROCESSING)
                .orElseThrow(() -> new BookingNotFoundException("Booking status is not found"));


        booking.setClient(user);
        booking.setBookingStatus(status);
        booking.setSchedule(schedule);
        booking.setCreatedAt(createDto.getCreatedAt());

        return booking;
    }

    private ClientSubscription rewriteFromSubscriptionCreateDto(CreateClientSubscriptionDto dto){
        User client = userService.getUser(dto.getClientId());

        Subscription subscription = subscriptionRepository.findSubscriptionById(dto.getSubscriptionId())
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription is not found"));

        ClientSubscription clientSubscription = new ClientSubscription();

        clientSubscription.setSubscription(subscription);
        clientSubscription.setClient(client);
        clientSubscription.setStartDate(LocalDate.now());

        LocalDate endDate = LocalDate.now().plusDays(subscription.getDurationDays());
        clientSubscription.setEndDate(endDate);
        clientSubscription.setRemainingVisits(subscription.getVisitsCount());

        return clientSubscription;
    }



    @Transactional
    public void cancelBooking(BookingCancelDto cancelDto){

        Booking booking = bookingRepository.findById(cancelDto.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException("Booking is not found"));

        BookingStatus status = bookingStatusRepository.findByBookingStatusName(BookingStatusEnum.CANCELLED)
                .orElseThrow(() -> new BookingNotFoundException("Booking status is not found"));

        User client = booking.getClient();
        ClientSubscription clientSubscription = clientSubscriptionRepository
                .findCurrentActiveSubscription(client.getId(), LocalDate.now())
                .orElseThrow(() -> new ClientSubscriptionNotFoundException("No active subscriptions"));
        Subscription subscription = clientSubscription.getSubscription();

        //Отменяем один поход в клуб
        if (!subscription.isUnlimited()){
            Integer remainingVisits = clientSubscription.getRemainingVisits();
            clientSubscription.setRemainingVisits(remainingVisits + 1);
        }

        booking.setBookingStatus(status);
        scheduleService.removeParticipant(booking.getSchedule());

        bookingRepository.save(booking);

    }

    public BookingStatus getBookingStatus(BookingStatusEnum statusEnum){

        return bookingStatusRepository.findByBookingStatusName(statusEnum)
                .orElseThrow(() -> new BookingNotFoundException("Booking status is not found"));
    }

    public Page<BookingShortResponseDto> getClientBookings(UUID clientId, Pageable pageable){
        Page<Booking> bookings = bookingRepository.findByClientId(clientId, pageable);

        return bookings
                .map(this::setCompletedStatus)
                .map(this::mapToShortResponseDto);
    }

    private Booking setCompletedStatus(Booking booking){
        if (booking.getSchedule().getStartTime().isBefore(LocalDateTime.now())
                && !booking.getBookingStatus().getBookingStatusName().equals(BookingStatusEnum.CANCELLED)){
            BookingStatus bookingStatus = bookingStatusRepository.findByBookingStatusName(BookingStatusEnum.COMPLETED)
                    .orElseThrow(() -> new BookingStatusNotFoundException("Booking status is not found"));

            booking.setBookingStatus(bookingStatus);
        }
        return booking;
    }


    //Тут подумать насчет DTO
    public List<UserInScheduleDto> getClientsBySchedule(Integer scheduleId){
        List<Booking> bookingList = bookingRepository.findAllByScheduleId(scheduleId);

        return bookingList
                .stream()
                .map(this::mapToUserScheduleDto)
                .toList();
    }

    //Проверить, записан ли пользователь на тренировку или нет
    public boolean checkBookingStatus(UUID userId, Integer scheduleId){
        return bookingRepository.existsByClientIdAndScheduleId(userId, scheduleId);
    }

    @Transactional
    public void cancelBySchedule(Integer scheduleId) {
        List<Booking> bookings = bookingRepository.findAllByScheduleId(scheduleId);
        BookingStatus cancelledStatus = bookingStatusRepository.findByBookingStatusName(BookingStatusEnum.CANCELLED)
                .orElseThrow(() -> new BookingStatusNotFoundException("Booking status is not found"));
        bookings.forEach(b -> b.setBookingStatus((cancelledStatus))); // устанавливаем объект статуса
        bookingRepository.saveAll(bookings);
    }

    public Page<BookingResponseDto> futureBookings(UUID userId, Pageable pageable){
        return bookingRepository.findByClientIdAndScheduleStartTimeAfter(userId, LocalDateTime.now(), pageable)
                .map(this::mapToResponseDto);
    }

    public Page<BookingResponseDto> pastBookings(UUID clientId, Pageable pageable){
        return bookingRepository.findByClientIdAndScheduleScheduleDateBefore(clientId, LocalDate.now(), pageable)
                .map(this::setCompletedStatus)
                .map(this::mapToResponseDto);
    }

    public BookingResponseDto nearestBooking(UUID clientId){
        Booking booking = bookingRepository.findFirstByClientIdAndScheduleScheduleDateAfter(clientId, LocalDate.now())
                .orElseThrow(() -> new BookingNotFoundException("Booking is not found"));

        return mapToResponseDto(booking);
    }

    //Все абонементы
    public List<SubscriptionResponseDto> allSubscriptions(){
        return subscriptionRepository.findAll()
                .stream()
                .map(this::mapToSubscriptionResponseDto)
                .toList();
    }

    @Transactional
    public ClientSubscriptionResponseDto createClientSubscription(CreateClientSubscriptionDto createClientSubscriptionDto){
        ClientSubscription clientSubscription = rewriteFromSubscriptionCreateDto(createClientSubscriptionDto);

        ClientSubscription activeSub = clientSubscriptionRepository
                .findCurrentActiveSubscription(createClientSubscriptionDto.getClientId(), LocalDate.now())
                .orElse(null);

        SubscriptionStatus subscriptionStatus;

        if (activeSub != null){
            subscriptionStatus = subscriptionStatusRepository.findBySubscriptionStatusName(SubscriptionStatusEnum.PENDING)
                    .orElseThrow(() -> new SubscriptionStatusNotFoundException("Subscription status is not found"));
        }else{
            subscriptionStatus = subscriptionStatusRepository.findBySubscriptionStatusName(SubscriptionStatusEnum.ACTIVE)
                    .orElseThrow(() -> new SubscriptionStatusNotFoundException("Subscription status is not found"));
        }

        clientSubscription.setSubscriptionStatus(subscriptionStatus);

        clientSubscriptionRepository.save(clientSubscription);

        return mapToClientSubscriptionResponseDto(clientSubscription);
    }

    private void setLapsedSubscriptionStatus(ClientSubscription clientSubscription){
        LocalDate endDate = clientSubscription.getEndDate();

        if(endDate.isBefore(LocalDate.now())){
            SubscriptionStatus status = subscriptionStatusRepository.findBySubscriptionStatusName(SubscriptionStatusEnum.LAPSED)
                    .orElseThrow(() -> new SubscriptionStatusNotFoundException("Subscription status is not found"));
            clientSubscription.setSubscriptionStatus(status);
        }
    }

    @Transactional
    public ClientSubscriptionResponseDto getClientSubscription(Integer id){
        ClientSubscription clientSubscription = clientSubscriptionRepository.findClientSubscriptionById(id)
                .orElseThrow(() -> new ClientSubscriptionNotFoundException("Client subscription is not found"));

        setLapsedSubscriptionStatus(clientSubscription); // Ничего не произойдет,
        // если дата окончания действия абонемента после текущей

        return mapToClientSubscriptionResponseDto(clientSubscription);

    }

}

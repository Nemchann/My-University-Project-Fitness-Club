package com.nemchann.fitnessbackend.booking.service;

import com.nemchann.fitnessbackend.aop.annotations.LogActivity;
import com.nemchann.fitnessbackend.booking.dto.*;
import com.nemchann.fitnessbackend.booking.entity.*;
import com.nemchann.fitnessbackend.booking.enums.BookingStatusEnum;
import com.nemchann.fitnessbackend.booking.enums.SubscriptionStatusEnum;
import com.nemchann.fitnessbackend.booking.mapper.BookingMapper;
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
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingStatusRepository bookingStatusRepository;
    private final ClientSubscriptionRepository clientSubscriptionRepository; // Про абонементы
    private final SubscriptionRepository subscriptionRepository; // Про абонементы
    private final SubscriptionStatusRepository subscriptionStatusRepository; // Про абонементы
    private final ScheduleService scheduleService;
    private final UserService userService;
    private final BookingMapper mapper;


    // Создание записи на тренировку
    @LogActivity(value = "Бронирование тренировки")
    @Transactional
    public BookingResponseDto createBooking(BookingCreateDto createDto){

        Booking booking = mapper.rewriteFromCreateDto(createDto);

        // Если эта тренировка есть, но со статусом CANCELLED, то можно записаться
        if (bookingRepository.existsByClientIdAndScheduleId(createDto.getUserId(), createDto.getScheduleId())){
            if (!booking.getBookingStatus().getBookingStatusName().equals(BookingStatusEnum.CANCELLED)){
                throw new AlreadyBookedException("Вы ужe записаны на эту тренировку");
            }
        }

        // Если бронировать тренировку за 2 часа до нее и позже
        LocalDateTime edgeTime = LocalDateTime.now().plusHours(2);
        if (booking.getSchedule().getStartTime().isBefore(edgeTime)){
            throw new BookingTooLateException("Нельзя на записаться на прошедшую тренировку " +
                    "или за два часа и менее до ее начала");
        }

        Schedule schedule = scheduleService.getSchedule(createDto.getScheduleId());
        if (!schedule.isActive()){
            throw new ScheduleIsNotActiveException("На данную тренировку нельзя записаться, ее отменил админ");
        }

        // Проверка на наличие активного абонемента и проверка на нового клиента
        boolean hasPriorBookings = bookingRepository.existsByClientId(createDto.getUserId());

        ClientSubscription currentSub = null;

        //
        if (hasPriorBookings) {
            currentSub = clientSubscriptionRepository
                    .findCurrentActiveSubscription(booking.getClient().getId(), createDto.getCreatedAt().toLocalDate())
                    .orElseThrow(() -> new VisitsEndedException("У Вас кончился абонемент." +
                            " Пожалуйста, приобретите новый и можете сколь угодно ходить на наши тренировки!"));
        }


        // Можно получить исключение со стороны ScheduleService
        try {
            // Пытаемся занять место в зале
            scheduleService.addParticipant(createDto.getScheduleId());

            BookingStatus acceptedStatus = bookingStatusRepository.findByBookingStatusName(BookingStatusEnum.ACCEPTED)
                    .orElseThrow(() -> new BookingStatusNotFoundException("Статус бронирования не найден"));
            booking.setBookingStatus(acceptedStatus);

            // Обрабатываем списание занятия или переход на новый абонемент
            if (hasPriorBookings) { //
                // Старый клиент: честно списываем занятие с абонемента
                handleSubscriptionProcessing(currentSub, booking.getClient().getId()); //
            } else {
                System.out.println("Новый клиент записывается на занятие");
            }

        } catch (IllegalStateException e) {
            // Если места кончились, отменяем бронь
            BookingStatus cancelledStatus = bookingStatusRepository.findByBookingStatusName(BookingStatusEnum.CANCELLED)
                    .orElseThrow(() -> new BookingStatusNotFoundException("Статус бронирования не найден"));
            booking.setBookingStatus(cancelledStatus);
        }

        bookingRepository.save(booking);
        return mapper.mapToResponseDto(booking);

    }

    // Обработка абонемента клиента при бронировании
    private void handleSubscriptionProcessing(ClientSubscription currentSub, UUID clientId) {
        Subscription subscription = currentSub.getSubscription();
        boolean shouldExpire = false;

        if (!subscription.isUnlimited()) {
            // Если пакетный - уменьшаем количество визитов
            int remaining = currentSub.getRemainingVisits();
            if (remaining > 0) {

                setLapsedSubscriptionStatus(currentSub); // Ничего не будет, если дата конца позже, чем сейчас
                if (currentSub.getSubscriptionStatus().getSubscriptionStatusName().equals(SubscriptionStatusEnum.LAPSED)){
                    clientSubscriptionRepository.save(currentSub);
                    throw new VisitsEndedException("У Вас кончился абонемент." +
                            " Пожалуйста, приобретите новый и можете сколь угодно ходить на наши тренировки!");
                }
                currentSub.setRemainingVisits(remaining - 1);
                clientSubscriptionRepository.save(currentSub);

            } else {
                shouldExpire = true;
            }
        } else {
            // Если безлимитный - проверяем только срок годности
            if (currentSub.getEndDate().isBefore(LocalDate.now())) {
                shouldExpire = true;
            }
        }

        // Если абонемент исчерпан (по дням или по занятиям) - закрываем его и ищем замену
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
                    .orElseThrow(() -> new VisitsEndedException("У Вас кончился абонемент." +
                            " Пожалуйста, приобретите новый и можете сколь угодно ходить на наши тренировки!"));

            // Активируем следующий абонемент (ACTIVE)
            SubscriptionStatus activeStatus = subscriptionStatusRepository
                    .findBySubscriptionStatusName(SubscriptionStatusEnum.ACTIVE)
                    .orElseThrow(() -> new SubscriptionStatusNotFoundException("Subscription status ACTIVE not found"));

            nextSub.setSubscriptionStatus(activeStatus);
            nextSub.setStartDate(LocalDate.now());
            // endDate = startDate + nextSub.getSubscription().getDurationDays()
            nextSub.setEndDate(LocalDate.now().plusDays(nextSub.getSubscription().getDurationDays()));

            // Если новый абонемент тоже пакетный, сразу списываем с него первое занятие
            if (!nextSub.getSubscription().isUnlimited()) {
                nextSub.setRemainingVisits(nextSub.getRemainingVisits() - 1);
            }

            clientSubscriptionRepository.save(nextSub);
            System.out.println("Старый абонемент закрыт, новый активирован");
        }
    }

    // Отмена записи на тренировку - присваивание статуса CANCELLED
    @LogActivity(value = "Отмена бронирования тренировки")
    @Transactional
    public void cancelBooking(BookingCancelDto cancelDto){

        Booking booking = bookingRepository.findById(cancelDto.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException("Данная запись на занятие не найдена"));

        BookingStatus status = bookingStatusRepository.findByBookingStatusName(BookingStatusEnum.CANCELLED)
                .orElseThrow(() -> new BookingNotFoundException("Статус бронирования не найден"));

        User client = booking.getClient();
        ClientSubscription clientSubscription = clientSubscriptionRepository
                .findCurrentActiveSubscription(client.getId(), LocalDate.now())
                .orElseThrow(() -> new ClientSubscriptionNotFoundException("У вас нет активного абонемента. " +
                        "Пожалуйста, приобретите новый абонемент"));
        Subscription subscription = clientSubscription.getSubscription();

        // Отменяем один поход в клуб
        if (!subscription.isUnlimited()){
            Integer remainingVisits = clientSubscription.getRemainingVisits();
            clientSubscription.setRemainingVisits(remainingVisits + 1);
        }

        booking.setBookingStatus(status);
        scheduleService.removeParticipant(booking.getSchedule()); // Заодно убираем одного посетителя тренировки

        bookingRepository.save(booking);

    }

    // Page всех бронирований клиента
    public Page<BookingShortResponseDto> getClientBookings(UUID clientId, Pageable pageable){
        Page<Booking> bookings = bookingRepository.findByClientId(clientId, pageable);

        return bookings
                .map(this::setCompletedStatus)
                .map(mapper::mapToShortResponseDto);
    }

    // Применение статуса COMPLETED, если клиент посетил тренировку
    private Booking setCompletedStatus(Booking booking){
        // Если время начала тренировки прошло, и бронь клиента не была отменена, то ставим статус COMPLETED
        if (booking.getSchedule().getStartTime().isBefore(LocalDateTime.now())
                && !booking.getBookingStatus().getBookingStatusName().equals(BookingStatusEnum.CANCELLED)){
            BookingStatus bookingStatus = bookingStatusRepository.findByBookingStatusName(BookingStatusEnum.COMPLETED)
                    .orElseThrow(() -> new BookingStatusNotFoundException("Статус бронирования не найден"));

            booking.setBookingStatus(bookingStatus);
        }
        return booking;
    }

    // Список клиентов, посещающих данную тренировку
    public List<UserInScheduleDto> getClientsBySchedule(Integer scheduleId){
        List<Booking> bookingList = bookingRepository.findAllByScheduleId(scheduleId);

        return bookingList
                .stream()
                .map(mapper::mapToUserScheduleDto)
                .toList();
    }

    // Проверка, записан ли пользователь на тренировку или нет
    public boolean checkBookingStatus(UUID userId, Integer scheduleId){
        return bookingRepository.existsByClientIdAndScheduleId(userId, scheduleId);
    }

    @Transactional
    public void cancelBySchedule(Integer scheduleId) {
        List<Booking> bookings = bookingRepository.findAllByScheduleId(scheduleId);
        BookingStatus cancelledStatus = bookingStatusRepository.findByBookingStatusName(BookingStatusEnum.CANCELLED)
                .orElseThrow(() -> new BookingStatusNotFoundException("Статус бронирования не найден"));
        bookings.forEach(b -> b.setBookingStatus((cancelledStatus))); // устанавливаем объект статуса
        bookingRepository.saveAll(bookings);
    }

    // Page будущих тренировок пользователя
    public Page<BookingResponseDto> futureBookings(UUID userId, Pageable pageable){
        return bookingRepository.findByClientIdAndScheduleStartTimeAfter(userId, LocalDateTime.now(), pageable)
                .map(mapper::mapToResponseDto);
    }

    // Page прошедших тренировок пользователя
    public Page<BookingResponseDto> pastBookings(UUID clientId, Pageable pageable){
        return bookingRepository.findByClientIdAndScheduleScheduleDateBefore(clientId, LocalDate.now(), pageable)
                .map(this::setCompletedStatus)
                .map(mapper::mapToResponseDto);
    }

    // Самая близкая тренировка
    public BookingResponseDto nearestBooking(UUID clientId){
        Booking booking = bookingRepository.findFirstByClientIdAndScheduleScheduleDateAfter(clientId, LocalDate.now())
                .orElseThrow(() -> new BookingNotFoundException("Данная запись на тренировку не найдена"));

        return mapper.mapToResponseDto(booking);
    }

    // Все абонементы клуба
    public List<SubscriptionResponseDto> allSubscriptions(){
        return subscriptionRepository.findAll()
                .stream()
                .map(mapper::mapToSubscriptionResponseDto)
                .toList();
    }

    // Покупка абонемента клиентом
    @Transactional
    public ClientSubscriptionResponseDto createClientSubscription(CreateClientSubscriptionDto createClientSubscriptionDto){
        ClientSubscription clientSubscription = mapper.rewriteFromSubscriptionCreateDto(createClientSubscriptionDto);

        // Находим активный абонемент у клуба
        ClientSubscription activeSub = clientSubscriptionRepository
                .findCurrentActiveSubscription(createClientSubscriptionDto.getClientId(), LocalDate.now())
                .orElse(null);

        SubscriptionStatus subscriptionStatus;

        // Присваиваем статус
        if (activeSub != null){
            subscriptionStatus = subscriptionStatusRepository.findBySubscriptionStatusName(SubscriptionStatusEnum.PENDING)
                    .orElseThrow(() -> new SubscriptionStatusNotFoundException("Subscription status is not found"));
        }else{
            subscriptionStatus = subscriptionStatusRepository.findBySubscriptionStatusName(SubscriptionStatusEnum.ACTIVE)
                    .orElseThrow(() -> new SubscriptionStatusNotFoundException("Subscription status is not found"));
        }

        clientSubscription.setSubscriptionStatus(subscriptionStatus);

        clientSubscriptionRepository.save(clientSubscription);

        return mapper.mapToClientSubscriptionResponseDto(clientSubscription);
    }

    // Метод, который присваивает ClientSubscription статус LAPSED, если он просрочился
    private void setLapsedSubscriptionStatus(ClientSubscription clientSubscription){
        LocalDate endDate = clientSubscription.getEndDate();

        if(endDate.isBefore(LocalDate.now())){
            SubscriptionStatus status = subscriptionStatusRepository.findBySubscriptionStatusName(SubscriptionStatusEnum.LAPSED)
                    .orElseThrow(() -> new SubscriptionStatusNotFoundException("Subscription status is not found"));
            clientSubscription.setSubscriptionStatus(status);

            clientSubscriptionRepository.save(clientSubscription);
        }
    }

    // Абонемент пользователя по id
    @Transactional
    public ClientSubscriptionResponseDto getClientSubscription(Integer id){
        ClientSubscription clientSubscription = clientSubscriptionRepository.findClientSubscriptionById(id)
                .orElseThrow(() -> new ClientSubscriptionNotFoundException("Client Такой абонемент не найден"));

        setLapsedSubscriptionStatus(clientSubscription); // Ничего не произойдет,
        // если дата окончания действия абонемента после текущей

        return mapper.mapToClientSubscriptionResponseDto(clientSubscription);
    }

    // Все прошлые абонементы пользователя
    public Page<ClientSubscriptionResponseDto> getPastSubscriptions(UUID clientId, Pageable pageable) {
        Page<ClientSubscription> lapsedSubs = clientSubscriptionRepository
                .findByClientIdAndSubscriptionStatus_SubscriptionStatusName(clientId, SubscriptionStatusEnum.LAPSED, pageable);

        return lapsedSubs.map(mapper::mapToClientSubscriptionResponseDto);
    }

    // Активный и будущие абонементы клиента
    public ClientActiveAndFutureSubscriptionsDto getActiveAndFutureSubscriptions(UUID clientId) {
        // Берем текущий активный абонемент
        Optional<ClientSubscription> activeSubOpt = clientSubscriptionRepository
                .findCurrentActiveSubscription(clientId, LocalDate.now());

        // Берем список всех будущих абонементов в очереди (PENDING)
        List<ClientSubscription> pendingSubs = clientSubscriptionRepository
                .findPendingSubscriptionsInternal(
                        clientId,
                        SubscriptionStatusEnum.PENDING,
                        org.springframework.data.domain.PageRequest.of(0, 100)
                );

        // Маппим сущности в DTO
        ClientSubscriptionResponseDto activeDto = activeSubOpt
                .map(mapper::mapToClientSubscriptionResponseDto)
                .orElse(null);

        List<ClientSubscriptionResponseDto> pendingDtos = pendingSubs.stream()
                .map(mapper::mapToClientSubscriptionResponseDto)
                .toList();

        return new ClientActiveAndFutureSubscriptionsDto(activeDto, pendingDtos);
    }

}

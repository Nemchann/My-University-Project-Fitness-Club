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

        //Добавить: Если эта тренировка есть, но со статусом CANCELLED, то можно записаться
        if (bookingRepository.existsByClientIdAndScheduleId(createDto.getUserId(), createDto.getScheduleId())){
            throw new AlreadyBookedException("You've already booked this schedule");
        }

        //Если бронировать тренировку за 2 часа до нее и позже
        LocalDateTime edgeTime = LocalDateTime.now().plusHours(2);
        if (booking.getSchedule().getStartTime().isAfter(edgeTime)){
            throw new BookingTooLateException("It is too late to book that schedule");
        }

        ClientSubscription clientSubscription = clientSubscriptionRepository.findLastByClientId(booking.getClient().getId())
                .orElseThrow(() -> new ClientSubscriptionNotFoundException("Client subscription is not found"));

        try{
            scheduleService.addParticipant(createDto.getScheduleId());
            BookingStatus status = bookingStatusRepository.findByBookingStatusName(BookingStatusEnum.ACCEPTED)
                    .orElseThrow(() -> new BookingNotFoundException("Booking status is not found"));

            Integer remainingVisits = clientSubscription.getRemainingVisits();

            if (remainingVisits > 0){
                clientSubscription.setRemainingVisits(remainingVisits - 1);
            }else{
                //Подумать, что делать, если есть еще активные абонементы у пользователя
                throw new VisitsEndedException("Your visits ended. Buy new subscription");
            }
            //Подумать, что делать с безлимитным посещением

            booking.setBookingStatus(status);

        }catch(IllegalStateException e){
            BookingStatus status = bookingStatusRepository.findByBookingStatusName(BookingStatusEnum.CANCELLED)
                    .orElseThrow(() -> new BookingNotFoundException("Booking status is not found"));
            booking.setBookingStatus(status);
        }

        clientSubscriptionRepository.save(clientSubscription);
        bookingRepository.save(booking);

        return mapToResponseDto(booking);

    }

    private BookingResponseDto mapToResponseDto(Booking booking){
        BookingResponseDto responseDto = new BookingResponseDto();
        Schedule schedule = booking.getSchedule();
        Workout workout = schedule.getWorkout();

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

        BookingStatusEnum bookingStatusEnum = booking.getBookingStatus().getBookingStatusName();
        responseDto.setStatus(bookingStatusEnum.name());

        responseDto.setStatus(bookingStatusEnum.name());

        responseDto.setScheduleName(workout.getWorkoutName());

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

        return bookings.map(this::mapToShortResponseDto);
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
        return bookingRepository.findByClientIdAndScheduleScheduleDateAfter(userId, LocalDate.now(), pageable)
                .map(this::mapToResponseDto);
    }

    public Page<BookingResponseDto> pastBookings(UUID clientId, Pageable pageable){
        return bookingRepository.findByClientIdAndScheduleScheduleDateBefore(clientId, LocalDate.now(), pageable)
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

        SubscriptionStatus subscriptionStatus = subscriptionStatusRepository.findBySubscriptionStatusName(SubscriptionStatusEnum.ACTIVE)
                        .orElseThrow(() -> new SubscriptionStatusNotFoundException("Subscription status is not found"));
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

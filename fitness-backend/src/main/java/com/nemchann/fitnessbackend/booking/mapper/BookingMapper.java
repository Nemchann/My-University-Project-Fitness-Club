package com.nemchann.fitnessbackend.booking.mapper;

import com.nemchann.fitnessbackend.booking.dto.*;
import com.nemchann.fitnessbackend.booking.entity.*;
import com.nemchann.fitnessbackend.booking.enums.BookingStatusEnum;
import com.nemchann.fitnessbackend.booking.repository.*;
import com.nemchann.fitnessbackend.common.exception.BookingStatusNotFoundException;
import com.nemchann.fitnessbackend.common.exception.SubscriptionNotFoundException;
import com.nemchann.fitnessbackend.common.exception.SubscriptionStatusNotFoundException;
import com.nemchann.fitnessbackend.schedule.entity.Schedule;
import com.nemchann.fitnessbackend.schedule.entity.Workout;
import com.nemchann.fitnessbackend.schedule.service.ScheduleService;
import com.nemchann.fitnessbackend.users.entity.Profile;
import com.nemchann.fitnessbackend.users.entity.User;
import com.nemchann.fitnessbackend.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final BookingRepository bookingRepository;
    private final BookingStatusRepository bookingStatusRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ClientSubscriptionRepository clientSubscriptionRepository;
    private final SubscriptionStatusRepository subscriptionStatusRepository;
    private final UserService userService;
    private final ScheduleService scheduleService;

    // Методы мапперы из dto в entity и обратно
    // Метод-маппер из Booking в BookingResponseDto
    public BookingResponseDto mapToResponseDto(Booking booking){
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

    // Метод-маппер из Booking в BookingResponseDto
    // Ничем не отличается от предыдущего, оставлен в качестве обратной совместимости
    public BookingShortResponseDto mapToShortResponseDto(Booking booking){
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

    // Метод-маппер из Booking в UserInScheduleDto
    public UserInScheduleDto mapToUserScheduleDto(Booking booking){
        User user = booking.getClient();
        Profile profile = user.getProfile();

        UserInScheduleDto userInScheduleDto = new UserInScheduleDto();

        userInScheduleDto.setFullName(profile.getSurname() + " " + profile.getSelfname());
        userInScheduleDto.setPhone(profile.getPhone());
        userInScheduleDto.setEmail(profile.getEmail());

        return userInScheduleDto;
    }

    // Метод-маппер из Subscription в SubscriptionResponseDto
    public SubscriptionResponseDto mapToSubscriptionResponseDto(Subscription subscription){
        SubscriptionResponseDto dto = new SubscriptionResponseDto();

        dto.setId(subscription.getId());
        dto.setSubscriptionName(subscription.getSubscriptionName());
        dto.setPrice(subscription.getPrice());
        dto.setDurationDays(subscription.getDurationDays());
        dto.setVisitsCount(subscription.getVisitsCount());

        return dto;
    }

    // Метод маппер из ClientSubscription в ClientSubscriptionResponseDto
    public ClientSubscriptionResponseDto mapToClientSubscriptionResponseDto(ClientSubscription clientSubscription){
        ClientSubscriptionResponseDto dto = new ClientSubscriptionResponseDto();

        Subscription subscription = clientSubscription.getSubscription();
        dto.setUnlimited(subscription.isUnlimited());

        dto.setStartDate(clientSubscription.getStartDate());
        dto.setEndDate(clientSubscription.getEndDate());

        SubscriptionStatus status = subscriptionStatusRepository
                .findBySubscriptionStatusName(clientSubscription.getSubscriptionStatus().getSubscriptionStatusName())
                .orElseThrow(() -> new SubscriptionStatusNotFoundException("Subscription status is not found"));
        dto.setSubscriptionStatus(status.getSubscriptionStatusName().name());

        dto.setRemainingVisits(clientSubscription.getRemainingVisits());

        return dto;
    }

    // Проверки на существование пользователя и тренировки происходят тут
    // (внутри сервисов UserService и ScheduleService)
    // Метод-маппер из BookingCreateDto в Booking
    public Booking rewriteFromCreateDto(BookingCreateDto createDto){
        Booking booking = new Booking();
        User user = userService.getUser(createDto.getUserId());
        Schedule schedule = scheduleService.getSchedule(createDto.getScheduleId());

        BookingStatus status = bookingStatusRepository.findByBookingStatusName(BookingStatusEnum.PROCESSING)
                .orElseThrow(() -> new BookingStatusNotFoundException("Статус бронирования не найден"));

        booking.setClient(user);
        booking.setBookingStatus(status);
        booking.setSchedule(schedule);
        booking.setCreatedAt(createDto.getCreatedAt());

        return booking;
    }

    // Метод-маппер из CreateClientSubscriptionDto в ClientSubscription
    public ClientSubscription rewriteFromSubscriptionCreateDto(CreateClientSubscriptionDto dto){
        User client = userService.getUser(dto.getClientId());

        Subscription subscription = subscriptionRepository.findSubscriptionById(dto.getSubscriptionId())
                .orElseThrow(() -> new SubscriptionNotFoundException("Такой абонемент не найден"));
        boolean isUnlimited = subscription.isUnlimited();

        ClientSubscription clientSubscription = new ClientSubscription();

        clientSubscription.setSubscription(subscription);
        clientSubscription.setClient(client);
        clientSubscription.setStartDate(LocalDate.now());

        LocalDate endDate = LocalDate.now().plusDays(subscription.getDurationDays());
        clientSubscription.setEndDate(endDate);

        if (!isUnlimited){
            clientSubscription.setRemainingVisits(subscription.getVisitsCount());
        }else{
            clientSubscription.setRemainingVisits(null);
        }

        return clientSubscription;
    }
}

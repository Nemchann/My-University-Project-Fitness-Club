package com.nemchann.fitnessbackend.booking.service;

import com.nemchann.fitnessbackend.booking.dto.*;
import com.nemchann.fitnessbackend.booking.entity.Booking;
import com.nemchann.fitnessbackend.booking.entity.BookingStatus;
import com.nemchann.fitnessbackend.booking.enums.BookingStatusEnum;
import com.nemchann.fitnessbackend.booking.repository.*;
import com.nemchann.fitnessbackend.common.exception.AlreadyBookedException;
import com.nemchann.fitnessbackend.common.exception.BookingNotFoundException;
import com.nemchann.fitnessbackend.schedule.entity.Schedule;
import com.nemchann.fitnessbackend.schedule.entity.Workout;
import com.nemchann.fitnessbackend.schedule.service.ScheduleService;
import com.nemchann.fitnessbackend.users.entity.Profile;
import com.nemchann.fitnessbackend.users.entity.User;
import com.nemchann.fitnessbackend.users.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingStatusRepository bookingStatusRepository;
    private final ClientSubscriptionRepository clientSubscriptionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionStatusRepository subscriptionStatusRepository;
    private final ScheduleService scheduleService;
    private final UserService userService;


    //Везде, где List поменять на Page
    @Transactional
    public BookingResponseDto createBooking(BookingCreateDto createDto){

        Booking booking = rewriteFromCreateDto(createDto);

        if (bookingRepository.existsByClientIdAndScheduleId(createDto.getUserId(), createDto.getScheduleId())){
            throw new AlreadyBookedException("You've already booked this schedule");
        }


        try{
            scheduleService.addParticipant(createDto.getScheduleId());
            BookingStatus status = bookingStatusRepository.findByBookingStatusName(BookingStatusEnum.ACCEPTED)
                    .orElseThrow(() -> new BookingNotFoundException("Booking status is not found"));
            booking.setBookingStatus(status);
        }catch(IllegalStateException e){
            BookingStatus status = bookingStatusRepository.findByBookingStatusName(BookingStatusEnum.CANCELLED)
                    .orElseThrow(() -> new BookingNotFoundException("Booking status is not found"));
            booking.setBookingStatus(status);
        }


        bookingRepository.save(booking);
        userService.addBookingToUser(createDto.getUserId(), booking);

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

    //Додумать
    @Transactional
    public void cancelBooking(BookingCancelDto cancelDto){

        Booking booking = bookingRepository.findById(cancelDto.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException("Booking is not found"));

        userService.cancelBookingFromUser(cancelDto.getUserId(), booking);

        BookingStatus status = bookingStatusRepository.findByBookingStatusName(BookingStatusEnum.CANCELLED)
                .orElseThrow(() -> new BookingNotFoundException("Booking status is not found"));

        booking.setBookingStatus(status);

        bookingRepository.save(booking);

    }

    public List<BookingShortResponseDto> getClientBookings(UUID clientId){
        List<Booking> bookingList = bookingRepository.findByClientId(clientId);

        return bookingList
                .stream()
                .map(this::mapToShortResponseDto)
                .toList();
    }


    //Тут подумать насчет DTO
    public List<UserInScheduleDto> getClientsBySchedule(Integer scheduleId){
        List<Booking> bookingList = bookingRepository.findByScheduleId(scheduleId);

        return bookingList
                .stream()
                .map(this::mapToUserScheduleDto)
                .toList();
    }

    //Проверить, записан ли пользователь на тренировку или нет
    public boolean checkBookingStatus(UUID userId, Integer scheduleId){
        return bookingRepository.existsByClientIdAndScheduleId(userId, scheduleId);
    }

    //Сделать список прошедших тренировок у пользователя
}

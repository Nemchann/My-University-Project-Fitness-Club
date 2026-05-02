package com.nemchann.fitnessbackend.booking.service;

import com.nemchann.fitnessbackend.booking.dto.BookingCancelDto;
import com.nemchann.fitnessbackend.booking.dto.BookingCreateDto;
import com.nemchann.fitnessbackend.booking.dto.BookingResponseDto;
import com.nemchann.fitnessbackend.booking.entity.Booking;
import com.nemchann.fitnessbackend.booking.entity.BookingStatus;
import com.nemchann.fitnessbackend.booking.enums.BookingStatusEnum;
import com.nemchann.fitnessbackend.booking.repository.*;
import com.nemchann.fitnessbackend.common.exception.BookingNotFoundException;
import com.nemchann.fitnessbackend.common.exception.NotEnoughPrivilegesException;
import com.nemchann.fitnessbackend.schedule.entity.Schedule;
import com.nemchann.fitnessbackend.schedule.entity.Workout;
import com.nemchann.fitnessbackend.schedule.service.ScheduleService;
import com.nemchann.fitnessbackend.users.entity.Role;
import com.nemchann.fitnessbackend.users.entity.User;
import com.nemchann.fitnessbackend.users.enums.UserRole;
import com.nemchann.fitnessbackend.users.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

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


    @Transactional
    public BookingResponseDto createBooking(BookingCreateDto createDto){
        Booking booking = rewriteFromCreateDto(createDto);

        BookingStatus bookingStatus = new BookingStatus();

        try{
            scheduleService.addParticipant(createDto.getScheduleId());
            bookingStatus.setBookingStatusName(BookingStatusEnum.ACCEPTED);
        }catch(IllegalStateException e){
            bookingStatus.setBookingStatusName(BookingStatusEnum.CANCELLED);
        }

        booking.setBookingStatus(bookingStatus);

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

    private Booking rewriteFromCreateDto(BookingCreateDto createDto){
        Booking booking = new Booking();
        User user = userService.getUser(createDto.getUserId());
        Schedule schedule = scheduleService.getSchedule(createDto.getScheduleId());

        BookingStatus bookingStatus = new BookingStatus();
        bookingStatus.setBookingStatusName(BookingStatusEnum.PROCESSING);

        booking.setClient(user);
        booking.setBookingStatus(bookingStatus);
        booking.setSchedule(schedule);
        booking.setCreatedAt(createDto.getCreatedAt());

        return booking;
    }

    //Додумать
    @Transactional
    public void cancelBooking(BookingCancelDto cancelDto){

        Booking booking = bookingRepository.findById(cancelDto.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException("Booking is not found"));

        userService.deleteBookingFromUser(cancelDto.getUserId(), booking);

        BookingStatus bookingStatus = new BookingStatus();
        bookingStatus.setBookingStatusName(BookingStatusEnum.CANCELLED);

        booking.setBookingStatus(bookingStatus);

        bookingRepository.save(booking);

    }

}

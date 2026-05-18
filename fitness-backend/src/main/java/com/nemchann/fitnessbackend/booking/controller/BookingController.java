package com.nemchann.fitnessbackend.booking.controller;

import com.nemchann.fitnessbackend.booking.dto.*;
import com.nemchann.fitnessbackend.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fitness-club/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Controller", description = "Запись и отмена бронирования тренировок")
public class BookingController {
    private final BookingService service;

    @PostMapping("/create_booking")
    @Operation(summary = "Создать запись на тренировку")
    public ResponseEntity<BookingResponseDto> createBooking(@Valid @RequestBody BookingCreateDto createDto){
        BookingResponseDto responseDto = service.createBooking(createDto);

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @DeleteMapping("/cancel_booking")
    @Operation(summary = "Отменить запись на тренировку")
    public ResponseEntity<Void> cancelBooking(@Valid @RequestBody BookingCancelDto cancelDto){
        service.cancelBooking(cancelDto);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/get_clients_bookings/{clientId}")
    @Operation(summary = "Все записи клиента")
    public ResponseEntity<Page<BookingShortResponseDto>> getClientsBookings
            (@PathVariable UUID clientId, @PageableDefault(size = 10, sort = "schedule") Pageable pageable){
        Page<BookingShortResponseDto> responseDtos = service.getClientBookings(clientId, pageable);

        return new ResponseEntity<>(responseDtos, HttpStatus.OK);
    }

    @GetMapping("/upcoming/{clientId}")
    @Operation(summary = "Будущие записи клиента")
    public ResponseEntity<Page<BookingResponseDto>> getFutureBookings(
            @PathVariable UUID clientId, @PageableDefault(size = 10, sort = "schedule") Pageable pageable){
        Page<BookingResponseDto> bookingResponseDtos = service.futureBookings(clientId, pageable);

        return new ResponseEntity<>(bookingResponseDtos, HttpStatus.OK);
    }

    @GetMapping("/past/{clientId}")
    @Operation(summary = "Прошедшие записи клиента")
    public ResponseEntity<Page<BookingResponseDto>> getPastBookings(
            @PathVariable UUID clientId, @PageableDefault(size = 10, sort = "schedule") Pageable pageable){
        Page<BookingResponseDto> bookingResponseDtos = service.pastBookings(clientId, pageable);

        return new ResponseEntity<>(bookingResponseDtos, HttpStatus.OK);
    }

    @GetMapping("/nearest/{clientId}")
    @Operation(summary = "Ближайшая запись")
    public ResponseEntity<BookingResponseDto> getNearestBooking(@PathVariable UUID clientId){
        BookingResponseDto dto = service.nearestBooking(clientId);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping("/get_clients_by_schedule/{scheduleId}")
    @Operation(summary = "Посетители данной тренировки")
    public ResponseEntity<List<UserInScheduleDto>> getClientsBySchedule(@PathVariable Integer scheduleId){
        List<UserInScheduleDto> scheduleDtos = service.getClientsBySchedule(scheduleId);

        return new ResponseEntity<>(scheduleDtos, HttpStatus.OK);
    }

    @GetMapping("/check_booking_status/{userId}")
    @Operation(summary = "Проверить статус бронирования для пользователя (записан/не записан)")
    public boolean checkBookingStatus(@PathVariable UUID userId, @RequestParam Integer scheduleId){
        return service.checkBookingStatus(userId, scheduleId);
    }

    @PostMapping("/create_client_subscription")
    @Operation(summary = "Купить абонемент")
    public ResponseEntity<ClientSubscriptionResponseDto> createClientSubscription(@RequestBody @Valid CreateClientSubscriptionDto createDto){
        ClientSubscriptionResponseDto responseDto = service.createClientSubscription(createDto);

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/all_subscriptions")
    @Operation(summary = "Все абонементы")
    public ResponseEntity<List<SubscriptionResponseDto>> getAllSubscriptions(){
        List<SubscriptionResponseDto> dtos = service.allSubscriptions();

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }


}

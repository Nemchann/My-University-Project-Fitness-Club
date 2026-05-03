package com.nemchann.fitnessbackend.booking.controller;

import com.nemchann.fitnessbackend.booking.dto.*;
import com.nemchann.fitnessbackend.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    @Operation(summary = "")
    public ResponseEntity<BookingResponseDto> createBooking(@Valid @RequestBody BookingCreateDto createDto){
        BookingResponseDto responseDto = service.createBooking(createDto);

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @DeleteMapping("/cancel_booking")
    @Operation(summary = "")
    public ResponseEntity<Void> cancelBooking(@Valid @RequestBody BookingCancelDto cancelDto){
        service.cancelBooking(cancelDto);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/get_clients_bookings/{id}")
    @Operation(summary = "")
    public ResponseEntity<List<BookingShortResponseDto>> getClientsBookings(@RequestParam UUID clientId){
        List<BookingShortResponseDto> responseDtos = service.getClientBookings(clientId);

        return new ResponseEntity<>(responseDtos, HttpStatus.OK);
    }

    @GetMapping("/get_clients_by_schedule/{id}")
    @Operation(summary = "")
    public ResponseEntity<List<UserInScheduleDto>> getClientsBySchedule(@RequestParam Integer scheduleId){
        List<UserInScheduleDto> scheduleDtos = service.getClientsBySchedule(scheduleId);

        return new ResponseEntity<>(scheduleDtos, HttpStatus.OK);
    }

    @GetMapping("/check_booking_status/{id}")
    @Operation(summary = "")
    public boolean checkBookingStatus(@RequestParam UUID userId, @RequestParam Integer scheduleId){
        return service.checkBookingStatus(userId, scheduleId);
    }
}

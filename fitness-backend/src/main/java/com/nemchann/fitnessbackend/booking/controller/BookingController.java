package com.nemchann.fitnessbackend.booking.controller;

import com.nemchann.fitnessbackend.booking.dto.*;
import com.nemchann.fitnessbackend.booking.service.BookingService;
import com.nemchann.fitnessbackend.schedule.dto.ScheduleResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Запись успешно создана",
                    content = @Content(schema = @Schema(implementation = BookingResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Нет активного абонемента",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Не найдены тренировка или клиент по id",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Запись на эту тренировку уже создана",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Кончились места, запись производится за 2 часа до начала тренировки или позже," +
                            " тренировка отменена админом",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<BookingResponseDto> createBooking(@Valid @RequestBody BookingCreateDto createDto){
        BookingResponseDto responseDto = service.createBooking(createDto);

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @DeleteMapping("/cancel_booking")
    @Operation(summary = "Отменить запись на тренировку")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Запись успешно отменена"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Нет активного абонемента",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Не найдена запись по id или нет абонемента вовсе",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<Void> cancelBooking(@Valid @RequestBody BookingCancelDto cancelDto){
        service.cancelBooking(cancelDto);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/get_clients_bookings/{clientId}")
    @Operation(summary = "Все записи клиента")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Записи успешно получены",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Не найден клиент по id",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<Page<BookingShortResponseDto>> getClientsBookings
            (@PathVariable UUID clientId, @PageableDefault(size = 10, sort = "schedule") Pageable pageable){
        Page<BookingShortResponseDto> responseDtos = service.getClientBookings(clientId, pageable);

        return new ResponseEntity<>(responseDtos, HttpStatus.OK);
    }

    @GetMapping("/upcoming/{clientId}")
    @Operation(summary = "Будущие записи клиента")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Записи успешно получены",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Не найден клиент по id",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<Page<BookingResponseDto>> getFutureBookings(
            @PathVariable UUID clientId, @PageableDefault(size = 10, sort = "schedule") Pageable pageable){
        Page<BookingResponseDto> bookingResponseDtos = service.futureBookings(clientId, pageable);

        return new ResponseEntity<>(bookingResponseDtos, HttpStatus.OK);
    }

    @GetMapping("/past/{clientId}")
    @Operation(summary = "Прошедшие записи клиента")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Записи успешно получены",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Не найден клиент по id",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<Page<BookingResponseDto>> getPastBookings(
            @PathVariable UUID clientId, @PageableDefault(size = 10, sort = "schedule") Pageable pageable){
        Page<BookingResponseDto> bookingResponseDtos = service.pastBookings(clientId, pageable);

        return new ResponseEntity<>(bookingResponseDtos, HttpStatus.OK);
    }

    @GetMapping("/nearest/{clientId}")
    @Operation(summary = "Ближайшая запись")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Запись успешно получена",
                    content = @Content(schema = @Schema(implementation = BookingResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Не найден клиент по id",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<BookingResponseDto> getNearestBooking(@PathVariable UUID clientId){
        BookingResponseDto dto = service.nearestBooking(clientId);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping("/get_clients_by_schedule/{scheduleId}")
    @Operation(summary = "Посетители данной тренировки")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список клиентов успешно получен",
                    content = @Content(schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Не найдена тренировка по id",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<List<UserInScheduleDto>> getClientsBySchedule(@PathVariable Integer scheduleId){
        List<UserInScheduleDto> scheduleDtos = service.getClientsBySchedule(scheduleId);

        return new ResponseEntity<>(scheduleDtos, HttpStatus.OK);
    }

    @GetMapping("/check_booking_status/{userId}")
    @Operation(summary = "Проверить статус бронирования для пользователя (записан/не записан)")
    public boolean checkBookingStatus(@PathVariable UUID userId, @RequestParam Integer scheduleId){
        return service.checkBookingStatus(userId, scheduleId);
    }

    @PostMapping("/client_subscription")
    @Operation(summary = "Купить абонемент")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Абонемент успешно куплен",
                    content = @Content(schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Не найдены тренировка или клиент по id",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<ClientSubscriptionResponseDto> createClientSubscription(@RequestBody @Valid CreateClientSubscriptionDto createDto){
        ClientSubscriptionResponseDto responseDto = service.createClientSubscription(createDto);

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/subscriptions")
    @Operation(summary = "Все абонементы")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Абонементы клуба успешно получены",
                    content = @Content(schema = @Schema(implementation = List.class))
            )
    })
    public ResponseEntity<List<SubscriptionResponseDto>> getAllSubscriptions(){
        List<SubscriptionResponseDto> dtos = service.allSubscriptions();

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @GetMapping("/client_subscription/{id}")
    @Operation(summary = "Получить абонемент клиента по id")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Абонемент клиента успешно получен",
                    content = @Content(schema = @Schema(implementation = ClientSubscriptionResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Не найден абонемент клиента по id",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<ClientSubscriptionResponseDto> getClientSubscriptionById(
            @PathVariable Integer id){
        ClientSubscriptionResponseDto dto = service.getClientSubscription(id);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping("/subscriptions/past/{clientId}")
    @Operation(summary = "Прошлые абонементы пользователя")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Абонементы клиента успешно получены",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Не найден  клиент по id",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<Page<ClientSubscriptionResponseDto>> pastSubscriptions(
            @PathVariable UUID clientId, @PageableDefault(size = 10, sort = "startDate") Pageable pageable){
        Page<ClientSubscriptionResponseDto> responseDtos = service.getPastSubscriptions(clientId, pageable);

        return new ResponseEntity<>(responseDtos, HttpStatus.OK);
    }

    @GetMapping("/subscriptions/upcoming/{clientId}")
    @Operation(summary = "Активные и будущие абонементы пользователя")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Абонементы клиента успешно получены",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Не найден  клиент по id",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<ClientActiveAndFutureSubscriptionsDto> futureSubscriptions(
            @PathVariable UUID clientId){
        ClientActiveAndFutureSubscriptionsDto subscriptionsDto = service.getActiveAndFutureSubscriptions(clientId);

        return new ResponseEntity<>(subscriptionsDto, HttpStatus.OK);
    }

}

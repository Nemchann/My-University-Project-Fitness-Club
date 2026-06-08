package com.nemchann.fitnessbackend.users.controller;

import com.nemchann.fitnessbackend.users.dto.*;
import com.nemchann.fitnessbackend.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fitness-club/users")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "Управление пользователями и регистрация")
public class UserController {
    private final UserService service;

    @PostMapping("/register")
    @Operation(summary = "Создать пользователя")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Пользователь успешно зарегистрировался",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Пользователь с таким логином или email уже существует",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegistrationDto userRegistrationDto){
        UserResponseDto userResponseDto = service.createUser(userRegistrationDto);
        return new ResponseEntity<>(userResponseDto, HttpStatus.CREATED);
    }

    @PostMapping("/register_trainer")
    @Operation(summary = "Зарегистрировать тренера")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Пользователь успешно зарегистрировался",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Пользователь с таким логином или email уже существует",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<UserResponseDto> registerTrainer(@Valid @RequestBody UserRegistrationDto userRegistrationDto){
        UserResponseDto userResponseDto = service.createTrainer(userRegistrationDto);
        return new ResponseEntity<>(userResponseDto, HttpStatus.CREATED);
    }

    // Данный метод опасен
//    @DeleteMapping("/delete")
//    @Operation(summary = "Удалить пользователя")
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "204",
//                    description = "Пользователь успешно удален"
//            ),
//            @ApiResponse(
//                    responseCode = "404",
//                    description = "Пользователя с таким id не существует",
//                    content = @Content(schema = @Schema(implementation = String.class))
//            )
//    })
//    public ResponseEntity<Void> delete(@Valid @RequestBody UserEditingDto userEditingDto){
//        service.deleteUser(userEditingDto);
//        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по id")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно найден",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователя с таким id не существует",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<UserResponseDto> getUser(@PathVariable UUID id){
        UserResponseDto userResponseDto = service.getUserResponse(id);

        return ResponseEntity.ok(userResponseDto);
    }

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Пользователь успешно поменял пароль"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неверный пароль",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    @PutMapping("/change_password/{id}")
    @Operation(summary = "Поменять пароль пользователя")
    public ResponseEntity<Void> changePassword(@PathVariable UUID id,
                                                          @Valid @RequestBody PasswordChangeDto dto){
        service.changePassword(id, dto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/")
    @Operation(summary = "Все пользователи")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователи успешно найдены",
                    content = @Content(schema = @Schema(implementation = Page.class))
            )
    })
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @PageableDefault(size = 10, sort = "login") Pageable pageable
    ) {
        Page<UserResponseDto> users = service.findAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    //Доработать
    @GetMapping("/by_role")
    @Operation(summary = "Получить пользователей по названию роли")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователи успешно найдены",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Роли с таким именем не существует",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<Page<UserResponseDto>> getUsersByRole(
            @RequestParam String roleName, @PageableDefault(size = 10, sort = "login") Pageable pageable){
        Page<UserResponseDto> responseDtos = service.getByRoleName(roleName, pageable);

        return new ResponseEntity<>(responseDtos, HttpStatus.OK);
    }

    @GetMapping("/clients")
    @Operation(summary = "Все клиенты")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователи успешно найдены",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Роли с таким именем не существует",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<Page<UserResponseDto>> getAllClients(
            @PageableDefault(size = 10, sort = "login") Pageable pageable) {
        Page<UserResponseDto> userResponseDtos = service.getAllClients(pageable);

        return new ResponseEntity<>(userResponseDtos, HttpStatus.OK);
    }

    @GetMapping("/trainers")
    @Operation(summary = "Все тренеры")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователи успешно найдены",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Роли с таким именем не существует",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<Page<UserResponseDto>> getAllTrainers(
            @PageableDefault(size = 10, sort = "login") Pageable pageable) {
        Page<UserResponseDto> userResponseDtos = service.getAllTrainers(pageable);

        return new ResponseEntity<>(userResponseDtos, HttpStatus.OK);
    }


    @PostMapping("/authentification")
    @Operation(summary = "Авторизация существующего пользователя")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно авторизовался",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неверный пароль или логин",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<UserResponseDto> authUser(@Valid @RequestBody UserAuthentificationDto dto){
        UserResponseDto userResponseDto = service.authentification(dto);

        return ResponseEntity.ok(userResponseDto);
    }

    @PutMapping("/edit_profile")
    @Operation(summary = "Поменять профиль пользователя")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно поменял данные профиля",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Нет пользователя с данным id",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Данный email уже используется",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<UserResponseDto> editProfile(@Valid @RequestBody UserEditingDto dto){
        UserResponseDto userResponseDto = service.editProfile(dto);

        return ResponseEntity.ok(userResponseDto);
    }

    @GetMapping("/exists_by_login")
    @Operation(summary = "Наличие логина")
    public Boolean existsByLogin(String login){
        return service.isExistsLogin(login);
    }

    @GetMapping("/exists_by_email")
    @Operation(summary = "Наличие email")
    public Boolean existsByEmail(String email){
        return service.isExistsEmail(email);
    }

    @DeleteMapping("/deactivate/{id}")
    @Operation(summary = "Деактивировать пользователя")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Пользователь успешно деактивирован"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователя с таким id не существует",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID id){
        service.deactivateUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}

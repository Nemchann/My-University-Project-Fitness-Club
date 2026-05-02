package com.nemchann.fitnessbackend.users.controller;

import com.nemchann.fitnessbackend.users.dto.*;
import com.nemchann.fitnessbackend.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
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
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegistrationDto userRegistrationDto){
        UserResponseDto userResponseDto = service.createUser(userRegistrationDto);
        return new ResponseEntity<>(userResponseDto, HttpStatus.CREATED);
    }

    @PostMapping("/register_trainer")
    @Operation(summary = "Зарегистрировать тренера")
    public ResponseEntity<UserResponseDto> registerTrainer(@Valid @RequestBody UserRegistrationDto userRegistrationDto){
        UserResponseDto userResponseDto = service.createTrainer(userRegistrationDto);
        return new ResponseEntity<>(userResponseDto, HttpStatus.CREATED);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Удалить пользователя")
    public ResponseEntity<Void> delete(@Valid @RequestBody UserEditingDto userEditingDto){
        service.deleteUser(userEditingDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по id")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable UUID id){
        UserResponseDto userResponseDto = service.getUserResponse(id);

        return ResponseEntity.ok(userResponseDto);
    }

    @PutMapping("/change_password/{id}")
    @Operation(summary = "Поменять пароль пользователя")
    public ResponseEntity<Void> changePassword(@PathVariable UUID id,
                                                          @Valid @RequestBody PasswordChangeDto dto){
        service.changePassword(id, dto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/get_users")
    @Operation(summary = "Все пользователи")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @PageableDefault(size = 10, sort = "login") Pageable pageable
    ) {
        Page<UserResponseDto> users = service.findAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/authentification")
    @Operation(summary = "Авторизация существующего пользователя")
    public ResponseEntity<UserResponseDto> authUser(@Valid @RequestBody UserAuthentificationDto dto){
        UserResponseDto userResponseDto = service.authentification(dto);

        return ResponseEntity.ok(userResponseDto);
    }

    @PutMapping("/edit_profile/{id}")
    @Operation(summary = "Поменять профиль пользователя")
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
    public ResponseEntity<Void> deactivateUser(UUID id){
        service.deactivateUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}

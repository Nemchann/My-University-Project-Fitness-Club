package com.nemchann.fitnessbackend.users.controller;

import com.nemchann.fitnessbackend.users.dto.PasswordChangeDto;
import com.nemchann.fitnessbackend.users.dto.UserEditingDto;
import com.nemchann.fitnessbackend.users.dto.UserRegistrationDto;
import com.nemchann.fitnessbackend.users.dto.UserResponseDto;
import com.nemchann.fitnessbackend.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping("/delete")
    @Operation(summary = "Удалить пользователя")
    public ResponseEntity<Void> delete(@Valid @RequestBody UserEditingDto userEditingDto){
        service.deleteUser(userEditingDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по id")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable UUID id){
        UserResponseDto userResponseDto = service.getUser(id);

        return ResponseEntity.ok(userResponseDto);
    }

    @PutMapping("/change_password/{id}")
    @Operation(summary = "")
    public ResponseEntity<Void> changePassword(@PathVariable UUID id,
                                                          @Valid @RequestBody PasswordChangeDto dto){
        service.changePassword(id, dto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}

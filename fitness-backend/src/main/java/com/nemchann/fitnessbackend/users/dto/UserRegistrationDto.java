package com.nemchann.fitnessbackend.users.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;

//передается от фронтенда для регистрации пользователя
@Data
@NoArgsConstructor
public class UserRegistrationDto {

    @NotBlank(message = "Логин не может быть пустым")
    private String login;

    @Size(min = 8, message = "пароль не может быть коротким")
    private String password;

    @NotBlank(message = "Поле Фамилия не может быть пустым")
    private String surname;

    @NotBlank(message = "Поле Имя не может быть пустым")
    private String selfname;

    private String patronymic;

    @NotBlank(message = "Поле Номер телефона не может быть пустым")
    private String phone;

    @Email(message = "Некорректный email")
    @NotBlank(message = "Поле Электронная почта не может быть пустым")
    private String email;

    @NotNull(message = "Дата рождения обязательна")
    @Past(message = "Дата рождения должна быть в прошлом")
    private LocalDate birthday;

    private OffsetDateTime createdAt;
}

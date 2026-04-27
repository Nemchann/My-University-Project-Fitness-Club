package com.nemchann.fitnessbackend.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String email;

    @NotBlank(message = "Поле Электронная почта не может быть пустым")
    private Date birthday;
}

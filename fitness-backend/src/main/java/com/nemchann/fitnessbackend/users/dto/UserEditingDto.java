package com.nemchann.fitnessbackend.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
public class UserEditingDto {

    private UUID id;

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

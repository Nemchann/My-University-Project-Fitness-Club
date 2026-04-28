package com.nemchann.fitnessbackend.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class UserAuthentificationDto {

    @NotBlank
    private String login;

    @NotBlank
    @Size(min = 8, message = "Новый пароль должен быть не короче 8 символов")
    private String password;
}

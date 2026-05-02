package com.nemchann.fitnessbackend.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class PasswordChangeDto {
    @NotBlank
    private String oldPassword;

    @NotBlank
    @Size(min = 8, message = "Новый пароль должен быть не короче 8 символов")
    private String newPassword;
}

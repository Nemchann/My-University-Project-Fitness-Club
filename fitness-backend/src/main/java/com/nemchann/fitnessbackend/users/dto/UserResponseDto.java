package com.nemchann.fitnessbackend.users.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class UserResponseDto {
    private UUID id;

    private String login;

    private String email;

    private String surname;

    private String selfname;
}

package com.nemchann.fitnessbackend.users.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserResponseDto {
    private UUID id;

    private String login;

    private String email;

    private String surname;

    private String selfname;
}

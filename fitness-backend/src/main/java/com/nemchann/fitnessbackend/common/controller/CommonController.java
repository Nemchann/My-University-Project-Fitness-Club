package com.nemchann.fitnessbackend.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fitness-club/common")
@RequiredArgsConstructor
@Tag(name = "Common Controller",
        description = "Общий контроллер. Не имеет ничего общего с бизнес-логикой приложения")
public class CommonController {

    @GetMapping("/health")
    @Operation(summary = "Состояние системы")
    public ResponseEntity<Boolean> health(){
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}

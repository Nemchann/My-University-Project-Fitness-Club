package com.nemchann.fitnessbackend.users.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = false)
    private String password;

    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "is_active")
    private boolean isActive;

//    {
//        try {
//            this.password = getHash();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private String getHash() throws Exception {
//        // Создаем экземпляр алгоритма SHA-256
//        byte[] hash = MessageDigest.getInstance("SHA-256")
//                .digest(password.getBytes(StandardCharsets.UTF_8));
//
//        // Превращаем массив байтов в понятную шестнадцатеричную строку
//        return HexFormat.of().formatHex(hash);
//    }

}

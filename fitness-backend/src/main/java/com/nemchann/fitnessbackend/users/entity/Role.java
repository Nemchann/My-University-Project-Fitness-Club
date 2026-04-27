package com.nemchann.fitnessbackend.users.entity;

import com.nemchann.fitnessbackend.users.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    // CLIENT, TRAINER, ADMIN
    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", unique = true, nullable = false)
    private UserRole roleName;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> userList = new ArrayList<>();

}

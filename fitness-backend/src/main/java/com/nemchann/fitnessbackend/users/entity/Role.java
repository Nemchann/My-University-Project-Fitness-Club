package com.nemchann.fitnessbackend.users.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
@Getter
@Setter
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    // CLIENT, TRAINER, ADMINISTRATOR
    @Column(name = "role_name", unique = true)
    private String roleName;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> userList = new ArrayList<>();

    public Role(String roleName){
        this.roleName = roleName;
    }
}

package com.nemchann.fitnessbackend.users.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "profiles")
@Getter @Setter
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;

    @Column (name = "surname", nullable = false)
    private String surname;

    @Column (name = "selfname", nullable = false)
    private String selfname;

    @Column (name = "patronymic")
    private String patronymic;

    @Column (name = "birthday", nullable = false)
    private Date birthday;

    @Column (name = "phone", unique = true, nullable = false)
    private String phone;

    @Column (name = "email", unique = true)
    private String email;

}

package com.nemchann.fitnessbackend.users.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Entity
@Table(name = "profiles")
@Getter @Setter
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

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

    public Profile(User user, String surname, String selfname, String patronymic,
                   Date birthday, String phone, String email){
        this.user = user;
        this.surname = surname;
        this.selfname = selfname;
        this.patronymic = patronymic;
        this.birthday = birthday;
        this.phone = phone;
        this.email = email;
    }

}

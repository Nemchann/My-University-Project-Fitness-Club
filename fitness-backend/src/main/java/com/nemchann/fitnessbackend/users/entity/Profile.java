package com.nemchann.fitnessbackend.users.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Getter @Setter
@NoArgsConstructor
public class Profile {

    @Id
    private UUID id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Column (name = "surname", nullable = false)
    private String surname;

    @Column (name = "selfname", nullable = false)
    private String selfname;

    @Column (name = "patronymic")
    private String patronymic;

    @Column (name = "birthday", nullable = false)
    private LocalDate birthday;

    @Column (name = "phone", unique = true, nullable = false)
    private String phone;

    @Column (name = "email", unique = true)
    private String email;

    public Profile(User user, String surname, String selfname, String patronymic,
                   LocalDate birthday, String phone, String email){
        this.user = user;
        this.surname = surname;
        this.selfname = selfname;
        this.patronymic = patronymic;
        this.birthday = birthday;
        this.phone = phone;
        this.email = email;
    }


}

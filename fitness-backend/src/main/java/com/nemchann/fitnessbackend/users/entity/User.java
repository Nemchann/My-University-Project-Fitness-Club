package com.nemchann.fitnessbackend.users.entity;

import com.nemchann.fitnessbackend.booking.entity.Booking;
import com.nemchann.fitnessbackend.booking.entity.ClientSubscription;
import com.nemchann.fitnessbackend.payment.entity.Payment;
import com.nemchann.fitnessbackend.schedule.entity.Schedule;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @Column(nullable = false, name = "password_hash")
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "create_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "is_active")
    private boolean isActive;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> clientBookings = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClientSubscription> clientSubscriptions = new ArrayList<>();

    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Schedule> trainerSchedulesList = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> clientPayments = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @PrimaryKeyJoinColumn
    private Profile profile;

    public User(String login, String password, Role role){
        this.login = login;
        this.password = password;
        this.role = role;
        this.isActive = true;
        this.createdAt = OffsetDateTime.now();
    }


    public User(){
        this.isActive = true;
    }



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

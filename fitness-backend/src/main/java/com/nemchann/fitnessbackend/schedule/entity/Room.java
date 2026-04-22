package com.nemchann.fitnessbackend.schedule.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rooms")
@Getter
@Setter
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "room_name", nullable = false, unique = true)
    private String roomName;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

}

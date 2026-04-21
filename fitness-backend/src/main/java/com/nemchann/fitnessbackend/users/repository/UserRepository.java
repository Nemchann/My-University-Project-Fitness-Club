package com.nemchann.fitnessbackend.users.repository;

import com.nemchann.fitnessbackend.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}

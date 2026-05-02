package com.nemchann.fitnessbackend.users.repository;

import com.nemchann.fitnessbackend.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByLogin(String login);

    Page<User> findAllByIsActiveTrue(Pageable pageable);

}

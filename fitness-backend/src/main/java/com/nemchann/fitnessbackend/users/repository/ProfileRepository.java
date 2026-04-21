package com.nemchann.fitnessbackend.users.repository;

import com.nemchann.fitnessbackend.users.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Integer> {
}

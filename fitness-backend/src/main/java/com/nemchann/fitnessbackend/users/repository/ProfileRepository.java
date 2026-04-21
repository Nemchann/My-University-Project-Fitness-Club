package com.nemchann.fitnessbackend.users.repository;

import com.nemchann.fitnessbackend.users.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Integer> {
}

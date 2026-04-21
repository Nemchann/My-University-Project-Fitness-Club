package com.nemchann.fitnessbackend.users.repository;

import com.nemchann.fitnessbackend.users.entity.Role;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
}

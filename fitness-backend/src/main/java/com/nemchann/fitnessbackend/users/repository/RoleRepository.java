package com.nemchann.fitnessbackend.users.repository;

import com.nemchann.fitnessbackend.users.entity.Role;
import com.nemchann.fitnessbackend.users.enums.UserRole;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(UserRole userRole);
}

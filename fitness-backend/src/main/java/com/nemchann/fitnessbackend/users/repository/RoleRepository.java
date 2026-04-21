package com.nemchann.fitnessbackend.users.repository;

import com.nemchann.fitnessbackend.users.entity.Role;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
}

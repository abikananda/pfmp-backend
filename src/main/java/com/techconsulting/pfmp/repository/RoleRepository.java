package com.techconsulting.pfmp.repository;

import com.techconsulting.pfmp.domain.Role;
import com.techconsulting.pfmp.domain.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName roleName);
}
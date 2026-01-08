package com.tetgift.repository.jpa;

import com.tetgift.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<Users, Long>, JpaSpecificationExecutor<Users> {
}

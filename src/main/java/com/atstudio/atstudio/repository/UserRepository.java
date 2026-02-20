package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

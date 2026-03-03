package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByPhonePersonal(String phonePersonal);

    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR u.nickname LIKE %:keyword% OR u.email LIKE %:keyword%) " +
           "AND (:userType IS NULL OR u.userType = :userType) " +
           "AND u.isDeleted = false")
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("userType") UserType userType,
            Pageable pageable);
}

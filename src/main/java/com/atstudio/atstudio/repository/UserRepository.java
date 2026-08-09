package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.UserType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u "
            + "WHERE u.role = com.atstudio.atstudio.entity.enums.UserRole.ADMIN "
            + "AND u.isDeleted = false ORDER BY u.id")
    List<User> findActiveAdminsForRoleChange();

    Optional<User> findOneByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isDeleted = false")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.nickname = :nickname AND u.isDeleted = false")
    Optional<User> findByNickname(@Param("nickname") String nickname);

    @Query("SELECT u FROM User u WHERE u.phonePersonal = :phone AND u.isDeleted = false")
    Optional<User> findByPhonePersonal(@Param("phone") String phonePersonal);

    long countByIsDeletedFalse();

    long countByIsDeletedFalseAndRole(@Param("role") com.atstudio.atstudio.entity.enums.UserRole role);

    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR u.nickname LIKE %:keyword% OR u.email LIKE %:keyword%) " +
           "AND (:userType IS NULL OR u.userType = :userType) " +
           "AND u.isDeleted = false")
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("userType") UserType userType,
            Pageable pageable);
}

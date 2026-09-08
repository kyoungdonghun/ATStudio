package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.PasswordResetToken;
import com.atstudio.atstudio.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    @Query("SELECT t.user.id FROM PasswordResetToken t WHERE t.token = :token")
    Optional<Long> findUserIdByToken(@Param("token") String token);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM PasswordResetToken t WHERE t.token = :token AND t.user.id = :userId")
    Optional<PasswordResetToken> findByTokenForUpdate(
            @Param("token") String token, @Param("userId") Long userId);

    void deleteAllByUser(User user);
}

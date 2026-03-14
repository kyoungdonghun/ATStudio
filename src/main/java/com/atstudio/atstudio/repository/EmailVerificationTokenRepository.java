package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.EmailVerificationToken;
import com.atstudio.atstudio.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    void deleteAllByUser(User user);
}

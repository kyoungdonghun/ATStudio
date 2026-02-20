package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.SocialAccount;
import com.atstudio.atstudio.entity.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderId(SocialProvider provider, String providerId);
}

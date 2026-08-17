package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.UserConsent;
import org.springframework.data.repository.Repository;

public interface UserConsentRepository extends Repository<UserConsent, Long> {

    <S extends UserConsent> S save(S entity);
}

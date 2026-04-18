package com.atstudio.atstudio.service.auth;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.PasswordLoginProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordLoginPolicy {

    private final PasswordLoginProperties passwordLoginProperties;

    public boolean isEnabled() {
        return passwordLoginProperties.isEnabled();
    }

    public void ensureEnabled() {
        if (!isEnabled()) {
            throw new BusinessException(BUSINESS_ERROR.PASSWORD_LOGIN_DISABLED);
        }
    }
}

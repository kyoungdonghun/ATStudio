package com.atstudio.atstudio.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.security.rate-limit")
public class AuthRateLimitProperties {

    private boolean enabled = true;
    private Rule login = new Rule(10, 60);
    private Rule forgotPassword = new Rule(5, 900);
    private Rule resetPassword = new Rule(5, 900);
    private Rule refresh = new Rule(30, 60);

    public long maxWindowSeconds() {
        return Math.max(
                Math.max(login.windowSeconds, forgotPassword.windowSeconds),
                Math.max(resetPassword.windowSeconds, refresh.windowSeconds)
        );
    }

    @Getter
    @Setter
    public static class Rule {
        private int limit;
        private long windowSeconds;

        public Rule() {
        }

        public Rule(int limit, long windowSeconds) {
            this.limit = limit;
            this.windowSeconds = windowSeconds;
        }

        public boolean isConfigured() {
            return limit > 0 && windowSeconds > 0;
        }
    }
}

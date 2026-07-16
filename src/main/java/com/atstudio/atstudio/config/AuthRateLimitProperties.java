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
    private Rule registration = new Rule(5, 900);
    private Rule login = new Rule(10, 60);
    private Rule forgotPassword = new Rule(5, 900);
    private Rule resetPassword = new Rule(5, 900);
    private Rule refresh = new Rule(30, 60);
    private AvailabilityRule emailAvailability = new AvailabilityRule();
    private AvailabilityRule phoneAvailability = new AvailabilityRule();
    private AvailabilityRule nicknameAvailability = new AvailabilityRule();

    public long maxWindowSeconds() {
        return Math.max(
                Math.max(
                        Math.max(registration.windowSeconds, login.windowSeconds),
                        Math.max(forgotPassword.windowSeconds, resetPassword.windowSeconds)
                ),
                Math.max(
                        Math.max(refresh.windowSeconds, emailAvailability.maxWindowSeconds()),
                        Math.max(phoneAvailability.maxWindowSeconds(), nicknameAvailability.maxWindowSeconds())
                )
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

    @Getter
    @Setter
    public static class AvailabilityRule {
        private Rule client = new Rule(30, 60);
        private Rule identifier = new Rule(30, 60);

        public boolean isConfigured() {
            return client.isConfigured() && identifier.isConfigured();
        }

        public long maxWindowSeconds() {
            return Math.max(client.windowSeconds, identifier.windowSeconds);
        }
    }
}

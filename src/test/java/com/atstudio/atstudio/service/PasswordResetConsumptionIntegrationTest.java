package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.entity.PasswordResetToken;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.repository.EmailVerificationTokenRepository;
import com.atstudio.atstudio.repository.PasswordResetTokenRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.service.auth.PasswordLoginPolicy;
import jakarta.persistence.EntityManager;
import org.aopalliance.intercept.MethodInterceptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DataJpaTest
@Import({JpaConfig.class, PasswordResetConsumptionIntegrationTest.Config.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PasswordResetConsumptionIntegrationTest {
    @Autowired EmailService email;
    @Autowired UserRepository users;
    @Autowired PasswordResetTokenRepository resets;
    @Autowired LockProbe probe;
    @MockitoBean JavaMailSender mail;
    @MockitoBean PasswordEncoder encoder;
    @MockitoBean PasswordLoginPolicy policy;

    @AfterEach
    void noActualMail() {
        probe.barrier = null;
        probe.contexts.clear();
        verifyNoInteractions(mail);
    }

    @Test
    void twoIndependentTransactionsCanConsumeTheSameTokenOnlyOnce() throws Exception {
        Fixture fixture = fixture(false, false);
        when(encoder.encode(anyString())).thenAnswer(invocation -> "encoded-" + invocation.getArgument(0));
        probe.barrier = new CyclicBarrier(2);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> attempt(fixture.token(), "first"));
            var second = executor.submit(() -> attempt(fixture.token(), "second"));
            Outcome firstResult = first.get(20, TimeUnit.SECONDS);
            Outcome secondResult = second.get(20, TimeUnit.SECONDS);
            assertThat(java.util.List.of(firstResult.success(), secondResult.success()))
                    .containsExactlyInAnyOrder(true, false);
            Outcome winner = firstResult.success() ? firstResult : secondResult;
            Outcome loser = firstResult.success() ? secondResult : firstResult;
            assertThat(loser.error()).isEqualTo(BUSINESS_ERROR.INVALID_TOKEN);
            assertThat(probe.contexts).hasSize(2);
            assertThat(resets.findByToken(fixture.token()).orElseThrow().isUsed()).isTrue();
            User stored = users.findById(fixture.userId()).orElseThrow();
            assertThat(stored.getPassword()).isEqualTo("encoded-" + winner.password());
            assertThat(stored.getRefreshToken()).isNull();
            verify(encoder, times(1)).encode(anyString());
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    void aFailedPasswordUpdateRollsBackConsumptionAndSessionRevocation() {
        Fixture fixture = fixture(false, false);
        when(encoder.encode("fails")).thenThrow(new IllegalStateException("synthetic encoding failure"));

        assertThatThrownBy(() -> email.resetPassword(fixture.token(), "fails"))
                .isInstanceOf(IllegalStateException.class);

        assertThat(resets.findByToken(fixture.token()).orElseThrow().isUsed()).isFalse();
        User stored = users.findById(fixture.userId()).orElseThrow();
        assertThat(stored.getPassword()).isEqualTo("encoded-old");
        assertThat(stored.getRefreshToken()).isEqualTo("synthetic-refresh-hash");

        when(encoder.encode("retry")).thenReturn("encoded-retry");
        email.resetPassword(fixture.token(), "retry");
        assertThat(resets.findByToken(fixture.token()).orElseThrow().isUsed()).isTrue();
        assertThat(users.findById(fixture.userId()).orElseThrow().getPassword()).isEqualTo("encoded-retry");
    }

    @Test
    void missingUsedAndExpiredTokensNeverMutateCredentials() {
        assertRejected("missing-fixture-token", BUSINESS_ERROR.INVALID_TOKEN);
        Fixture used = fixture(true, false);
        Fixture expired = fixture(false, true);
        assertRejected(used.token(), BUSINESS_ERROR.INVALID_TOKEN);
        assertRejected(expired.token(), BUSINESS_ERROR.TOKEN_EXPIRED);
        for (Fixture fixture : new Fixture[]{used, expired}) {
            User stored = users.findById(fixture.userId()).orElseThrow();
            assertThat(stored.getPassword()).isEqualTo("encoded-old");
            assertThat(stored.getRefreshToken()).isEqualTo("synthetic-refresh-hash");
        }
        verifyNoInteractions(encoder);
    }

    private void assertRejected(String token, BUSINESS_ERROR expected) {
        assertThatThrownBy(() -> email.resetPassword(token, "unused"))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode()).isEqualTo(expected));
    }

    private Outcome attempt(String token, String password) {
        try {
            email.resetPassword(token, password);
            return new Outcome(true, password, null);
        } catch (BusinessException exception) {
            return new Outcome(false, password, exception.getErrorCode());
        }
    }

    private Fixture fixture(boolean used, boolean expired) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User user = users.saveAndFlush(User.builder().nickname("reset-" + suffix)
                .email("reset-" + suffix + "@example.test").password("encoded-old")
                .refreshToken("synthetic-refresh-hash").isVerified(true).build());
        String token = UUID.randomUUID().toString();
        resets.saveAndFlush(PasswordResetToken.builder().user(user).token(token).used(used)
                .expiresAt(LocalDateTime.now().plusMinutes(expired ? -5 : 5)).build());
        return new Fixture(user.getId(), token);
    }

    record Fixture(Long userId, String token) {}
    record Outcome(boolean success, String password, BUSINESS_ERROR error) {}

    static class LockProbe {
        volatile CyclicBarrier barrier;
        final Set<Object> contexts = ConcurrentHashMap.newKeySet();
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class Config {
        @Bean
        LockProbe lockProbe() {
            return new LockProbe();
        }

        @Bean
        EmailService emailService(JavaMailSender mail, EmailVerificationTokenRepository emails,
                                  PasswordResetTokenRepository resets, UserRepository users,
                                  PasswordEncoder encoder, PasswordLoginPolicy policy,
                                  LockProbe probe, EntityManager entityManager) {
            ProxyFactory factory = new ProxyFactory(users);
            factory.addAdvice((MethodInterceptor) invocation -> {
                CyclicBarrier barrier = probe.barrier;
                if (barrier != null && invocation.getMethod().getName().equals("findByIdForUpdate")) {
                    assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
                    probe.contexts.add(entityManager.getDelegate());
                    // Both requests reach the user lock; the original code has already read stale tokens here.
                    barrier.await(10, TimeUnit.SECONDS);
                }
                return invocation.proceed();
            });
            return new EmailService(mail, emails, resets, (UserRepository) factory.getProxy(), encoder, policy);
        }
    }
}

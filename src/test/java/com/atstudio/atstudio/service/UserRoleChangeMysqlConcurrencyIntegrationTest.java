package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.dto.user.UserAdminUpdateRequest;
import com.atstudio.atstudio.dto.user.UserDetailResponse;
import com.atstudio.atstudio.dto.user.WithdrawRequest;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserJob;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.AdminOperationAuditLogRepository;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.service.auth.PasswordLoginPolicy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.sql.init.mode=never",
        "spring.jpa.show-sql=false",
        "spring.datasource.hikari.maximum-pool-size=6",
        "spring.datasource.hikari.connection-timeout=5000"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        UserService.class,
        AdminOperationAuditService.class,
        AdminOperationRejectionAuditService.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@EnabledIfEnvironmentVariable(named = "ATSTUDIO_ADMIN_ROLE_MYSQL_PROOF_ENABLED", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Timeout(value = 20, unit = TimeUnit.SECONDS)
@DisplayName("WI-20260808-ATS-014 MySQL 관리자 역할 동시성 검증")
class UserRoleChangeMysqlConcurrencyIntegrationTest {

    private static final Pattern DISPOSABLE_DATABASE =
            Pattern.compile("^ats_disposable_\\d{8}_[a-z0-9]{8}$");

    @Autowired UserService userService;
    @Autowired UserRepository userRepository;
    @Autowired BillingAgreementRepository billingAgreementRepository;
    @Autowired UserSubscriptionRepository userSubscriptionRepository;
    @Autowired SubscriptionRepository subscriptionRepository;
    @Autowired AdminOperationAuditLogRepository adminOperationAuditLogRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    @MockitoBean PasswordEncoder passwordEncoder;
    @MockitoBean EmailService emailService;
    @MockitoBean PasswordLoginPolicy passwordLoginPolicy;

    @BeforeAll
    void verifyDisposableMySqlContract() {
        String database = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
        String engine = jdbcTemplate.queryForObject(
                "SELECT engine FROM information_schema.tables "
                        + "WHERE table_schema = DATABASE() AND table_name = 'users'",
                String.class);

        assertThat(database).matches(DISPOSABLE_DATABASE);
        assertThat(version).startsWith("8.");
        assertThat(engine).isEqualToIgnoringCase("InnoDB");
    }

    @BeforeEach
    void clearDisposableUsers() {
        jdbcTemplate.update("DELETE FROM admin_operation_audit_logs");
        jdbcTemplate.update("DELETE FROM user_subscriptions");
        jdbcTemplate.update("DELETE FROM billing_agreements");
        jdbcTemplate.update("DELETE FROM users");
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    }

    @Test
    @DisplayName("두 ADMIN의 동시 교차 강등은 한 건만 커밋되고 ADMIN을 한 명 유지")
    void concurrentCrossDemotionKeepsOneAdmin() {
        User first = userRepository.saveAndFlush(admin("first-admin", "first-admin@test.com"));
        User second = userRepository.saveAndFlush(admin("second-admin", "second-admin@test.com"));
        Long firstID = first.getId();
        Long secondID = second.getId();

        MysqlRaceTestSupport.RacePair<UserDetailResponse> race = MysqlRaceTestSupport.runPair(
                () -> userService.updateUserByAdmin(firstID, secondID, demotion()),
                () -> userService.updateUserByAdmin(secondID, firstID, demotion()));

        assertThat(race.outcomes().stream().filter(MysqlRaceTestSupport.RaceOutcome::succeeded))
                .hasSize(1);
        assertThat(race.outcomes().stream().filter(outcome -> !outcome.succeeded()))
                .singleElement()
                .satisfies(outcome -> assertThat(MysqlRaceTestSupport.exactBusinessError(outcome))
                        .isEqualTo(BUSINESS_ERROR.LAST_ADMIN_REQUIRED));
        assertThat(userRepository.countByIsDeletedFalseAndRole(UserRole.ADMIN)).isEqualTo(1L);
        assertThat(userRepository.findAll())
                .filteredOn(user -> user.getRole() == UserRole.USER)
                .singleElement()
                .satisfies(user -> assertThat(user.getRefreshToken()).isNull());
        assertThat(adminOperationAuditLogRepository.findAll())
                .hasSize(2)
                .filteredOn(audit -> audit.getAction()
                        == com.atstudio.atstudio.entity.enums.AdminOperationAuditAction.USER_ROLE_CHANGE)
                .hasSize(2);
        assertThat(adminOperationAuditLogRepository.findAll())
                .filteredOn(audit -> audit.getOutcome()
                        == com.atstudio.atstudio.entity.enums.AdminOperationAuditOutcome.SUCCEEDED)
                .hasSize(1);
        assertThat(adminOperationAuditLogRepository.findAll())
                .filteredOn(audit -> audit.getOutcome()
                        == com.atstudio.atstudio.entity.enums.AdminOperationAuditOutcome.REJECTED)
                .singleElement()
                .satisfies(audit -> assertThat(audit.getReasonCode())
                        .isEqualTo(BUSINESS_ERROR.LAST_ADMIN_REQUIRED.name()));
    }

    @Test
    @DisplayName("ADMIN 강등과 요청자 탈퇴 경합은 한 건만 커밋되고 ADMIN을 한 명 유지")
    void concurrentRoleChangeAndActorWithdrawalKeepsOneAdmin() {
        User actor = userRepository.saveAndFlush(admin("actor-admin", "actor-admin@test.com"));
        User target = userRepository.saveAndFlush(admin("target-admin", "target-admin@test.com"));
        Long actorID = actor.getId();
        Long targetID = target.getId();
        Subscription plan = subscriptionRepository.saveAndFlush(Subscription.builder()
                .name("WI-014-race-" + System.nanoTime())
                .description("Disposable role-change and withdrawal race plan")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(BigDecimal.TEN)
                .priceYearly(BigDecimal.valueOf(100))
                .downloadPerDay(10)
                .maxWhitelistChannels(10)
                .maxPlaylists(3)
                .build());
        BillingAgreement agreement = billingAgreementRepository.saveAndFlush(BillingAgreement.builder()
                .user(actor)
                .provider(PaymentProviderType.TOSS)
                .providerCustomerKey("wi-014-race-customer")
                .build());
        UserSubscription actorSubscription = userSubscriptionRepository.saveAndFlush(
                UserSubscription.builder()
                        .user(actor)
                        .subscription(plan)
                        .billingCycle(BillingCycle.MONTHLY)
                        .status(SubscriptionStatus.ACTIVE)
                        .startedAt(LocalDate.now())
                        .expiresAt(LocalDate.now().plusMonths(1))
                        .build());

        MysqlRaceTestSupport.RacePair<String> race = MysqlRaceTestSupport.runPair(
                () -> {
                    userService.updateUserByAdmin(actorID, targetID, demotion());
                    return "ROLE_CHANGE";
                },
                () -> {
                    userService.withdraw(actorID, withdrawal());
                    return "WITHDRAWAL";
                });

        assertThat(race.outcomes().stream().filter(MysqlRaceTestSupport.RaceOutcome::succeeded))
                .hasSize(1);
        assertThat(race.outcomes().stream().filter(outcome -> !outcome.succeeded()))
                .singleElement()
                .satisfies(outcome -> assertThat(MysqlRaceTestSupport.exactBusinessError(outcome))
                        .isEqualTo(BUSINESS_ERROR.LAST_ADMIN_REQUIRED));
        assertThat(userRepository.countByIsDeletedFalseAndRole(UserRole.ADMIN)).isEqualTo(1L);
        String winner = race.outcomes().stream()
                .filter(MysqlRaceTestSupport.RaceOutcome::succeeded)
                .map(MysqlRaceTestSupport.RaceOutcome::value)
                .findFirst()
                .orElseThrow();
        BillingAgreement persistedAgreement = billingAgreementRepository.findById(agreement.getId())
                .orElseThrow();
        UserSubscription persistedSubscription = userSubscriptionRepository.findById(actorSubscription.getId())
                .orElseThrow();
        if ("WITHDRAWAL".equals(winner)) {
            assertThat(persistedAgreement.getStatus()).isEqualTo(BillingAgreementStatus.CANCELLED);
            assertThat(persistedSubscription.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
        } else {
            assertThat(winner).isEqualTo("ROLE_CHANGE");
            assertThat(persistedAgreement.getStatus()).isEqualTo(BillingAgreementStatus.READY);
            assertThat(persistedSubscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        }
        assertThat(adminOperationAuditLogRepository.findAll()).hasSize(2);
        assertThat(adminOperationAuditLogRepository.findAll())
                .extracting(audit -> audit.getAction())
                .containsExactlyInAnyOrder(
                        com.atstudio.atstudio.entity.enums.AdminOperationAuditAction.USER_ROLE_CHANGE,
                        com.atstudio.atstudio.entity.enums.AdminOperationAuditAction.ADMIN_WITHDRAWAL);
        assertThat(adminOperationAuditLogRepository.findAll())
                .extracting(audit -> audit.getOutcome())
                .containsExactlyInAnyOrder(
                        com.atstudio.atstudio.entity.enums.AdminOperationAuditOutcome.SUCCEEDED,
                        com.atstudio.atstudio.entity.enums.AdminOperationAuditOutcome.REJECTED);
    }

    private User admin(String nickname, String email) {
        User user = User.builder()
                .nickname(nickname)
                .email(email)
                .password("encoded")
                .phonePersonal("010-0000-0000")
                .job(UserJob.EDITOR)
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.ADMIN)
                .isVerified(true)
                .build();
        user.updateRefreshToken("stored-refresh-hash");
        return user;
    }

    private UserAdminUpdateRequest demotion() {
        UserAdminUpdateRequest request = new UserAdminUpdateRequest();
        request.setRole(UserRole.USER);
        request.setReason("WI-014 concurrency proof");
        return request;
    }

    private WithdrawRequest withdrawal() {
        WithdrawRequest request = new WithdrawRequest();
        request.setPassword("password123");
        return request;
    }
}

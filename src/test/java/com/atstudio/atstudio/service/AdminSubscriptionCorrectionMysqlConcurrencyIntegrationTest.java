package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.dto.subscription.AdminSubscriptionCorrectionApproveRequest;
import com.atstudio.atstudio.dto.subscription.AdminSubscriptionCorrectionExecuteRequest;
import com.atstudio.atstudio.dto.subscription.AdminSubscriptionCorrectionRequest;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.AdminSubscriptionCorrectionStatus;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserJob;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.AdminSubscriptionCorrectionRepository;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.sql.init.mode=never",
        "spring.jpa.show-sql=false",
        "spring.datasource.hikari.maximum-pool-size=6",
        "spring.datasource.hikari.connection-timeout=5000",
        "spring.datasource.hikari.transaction-isolation=TRANSACTION_REPEATABLE_READ"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        PaymentProperties.class,
        AdminSubscriptionCorrectionService.class,
        AdminOperationAuditService.class,
        AdminOperationRejectionAuditService.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@EnabledIfEnvironmentVariable(
        named = "ATSTUDIO_SUBSCRIPTION_CORRECTION_MYSQL_PROOF_ENABLED",
        matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Timeout(value = 20, unit = TimeUnit.SECONDS)
@DisplayName("WI-20260808-ATS-015 MySQL subscription correction concurrency proof")
class AdminSubscriptionCorrectionMysqlConcurrencyIntegrationTest {

    private static final Pattern DISPOSABLE_DATABASE =
            Pattern.compile("^ats_disposable_\\d{8}_[a-z0-9]{8}$");
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Seoul");

    @Autowired AdminSubscriptionCorrectionService correctionService;
    @Autowired AdminSubscriptionCorrectionRepository correctionRepository;
    @Autowired BillingAgreementRepository billingAgreementRepository;
    @Autowired UserSubscriptionRepository userSubscriptionRepository;
    @Autowired SubscriptionRepository subscriptionRepository;
    @Autowired UserRepository userRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    @BeforeAll
    void verifyDisposableRepeatableReadMySqlContract() {
        String database = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
        String engine = jdbcTemplate.queryForObject(
                "SELECT engine FROM information_schema.tables "
                        + "WHERE table_schema = DATABASE() "
                        + "AND table_name = 'admin_subscription_corrections'",
                String.class);
        String isolation = jdbcTemplate.queryForObject("SELECT @@transaction_isolation", String.class);

        assertThat(database).matches(DISPOSABLE_DATABASE);
        assertThat(version).startsWith("8.");
        assertThat(engine).isEqualToIgnoringCase("InnoDB");
        assertThat(isolation).isEqualToIgnoringCase("REPEATABLE-READ");
    }

    @BeforeEach
    void clearDisposableCorrectionFixture() {
        jdbcTemplate.update("DELETE FROM admin_subscription_corrections");
        jdbcTemplate.update("DELETE FROM admin_operation_audit_logs");
        jdbcTemplate.update("DELETE FROM user_subscriptions");
        jdbcTemplate.update("DELETE FROM billing_agreements");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM subscriptions WHERE name LIKE 'WI-015-race-%'");
    }

    @Test
    @DisplayName("two concurrent requests create exactly one non-terminal correction")
    void concurrentRequestsCreateExactlyOneCorrection() {
        RaceFixture fixture = createRaceFixture(1);
        AdminSubscriptionCorrectionRequest request = new AdminSubscriptionCorrectionRequest(
                fixture.userSubscription().getId(),
                fixture.targetPlan().getId(),
                BillingCycle.MONTHLY,
                SubscriptionStatus.CANCELLED,
                LocalDate.now(BUSINESS_ZONE),
                false,
                false,
                "WI-015 concurrent request proof");

        MysqlRaceTestSupport.RacePair<Long> race = MysqlRaceTestSupport.runPair(
                () -> correctionService.requestCorrection(fixture.actor(), request).getData().id(),
                () -> correctionService.requestCorrection(fixture.actor(), request).getData().id());

        assertThat(race.outcomes().stream().filter(MysqlRaceTestSupport.RaceOutcome::succeeded))
                .singleElement();
        assertThat(race.outcomes().stream().filter(outcome -> !outcome.succeeded()))
                .singleElement()
                .satisfies(outcome -> assertThat(MysqlRaceTestSupport.exactBusinessError(outcome))
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_DUPLICATE));
        assertThat(correctionRepository.countByUserSubscription_Id(fixture.userSubscription().getId()))
                .isEqualTo(1L);
        assertThat(correctionRepository.findAll())
                .singleElement()
                .satisfies(correction -> assertThat(correction.getStatus())
                        .isEqualTo(AdminSubscriptionCorrectionStatus.REQUESTED));
    }

    @Test
    @DisplayName("approved execute and a new request finish without lock inversion")
    void approvedExecuteAndNewRequestDoNotDeadlock() {
        for (int round = 0; round < 3; round++) {
            int raceRound = round;
            RaceFixture fixture = createRaceFixture(10 + raceRound);
            AdminSubscriptionCorrectionRequest correctionRequest =
                    new AdminSubscriptionCorrectionRequest(
                            fixture.userSubscription().getId(),
                            fixture.targetPlan().getId(),
                            BillingCycle.MONTHLY,
                            SubscriptionStatus.CANCELLED,
                            LocalDate.now(BUSINESS_ZONE),
                            false,
                            false,
                            "WI-015 execute/request race setup " + raceRound);
            Long correctionID = correctionService
                    .requestCorrection(fixture.actor(), correctionRequest)
                    .getData()
                    .id();
            correctionService.approveCorrection(
                    correctionID,
                    fixture.actor(),
                    new AdminSubscriptionCorrectionApproveRequest("approved race " + raceRound));

            AdminSubscriptionCorrectionRequest competingRequest =
                    new AdminSubscriptionCorrectionRequest(
                            fixture.userSubscription().getId(),
                            fixture.currentPlan().getId(),
                            BillingCycle.YEARLY,
                            SubscriptionStatus.ACTIVE,
                            LocalDate.now(BUSINESS_ZONE).plusMonths(2),
                            false,
                            false,
                            "WI-015 coherent follow-up " + raceRound);
            MysqlRaceTestSupport.RacePair<String> race = MysqlRaceTestSupport.runPair(
                    () -> "EXECUTED:" + correctionService.executeCorrection(
                                    correctionID,
                                    fixture.actor(),
                                    new AdminSubscriptionCorrectionExecuteRequest(
                                            "execute race " + raceRound))
                            .getData()
                            .status(),
                    () -> "REQUESTED:" + correctionService
                            .requestCorrection(fixture.actor(), competingRequest)
                            .getData()
                            .id());

            MysqlRaceTestSupport.assertSucceeded(race.first());
            assertThat(race.first().value()).isEqualTo("EXECUTED:SUCCEEDED");
            assertThat(correctionRepository.findById(correctionID).orElseThrow().getStatus())
                    .isEqualTo(AdminSubscriptionCorrectionStatus.SUCCEEDED);

            if (race.second().succeeded()) {
                assertThat(race.second().value()).startsWith("REQUESTED:");
                assertThat(correctionRepository.countByUserSubscription_Id(
                        fixture.userSubscription().getId())).isEqualTo(2L);
            } else {
                assertThat(MysqlRaceTestSupport.exactBusinessError(race.second()))
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_DUPLICATE);
                assertThat(correctionRepository.countByUserSubscription_Id(
                        fixture.userSubscription().getId())).isEqualTo(1L);
            }
        }
    }

    private RaceFixture createRaceFixture(int sequence) {
        User administrator = userRepository.saveAndFlush(user(
                "wi-015-admin-" + sequence,
                "wi-015-admin-" + sequence + "@test.com",
                String.format("010-%04d-0001", 1500 + sequence),
                UserRole.ADMIN));
        User subscriber = userRepository.saveAndFlush(user(
                "wi-015-subscriber-" + sequence,
                "wi-015-subscriber-" + sequence + "@test.com",
                String.format("010-%04d-0002", 1500 + sequence),
                UserRole.USER));
        Subscription currentPlan = subscriptionRepository.saveAndFlush(
                plan("WI-015-race-current-" + sequence));
        Subscription targetPlan = subscriptionRepository.saveAndFlush(
                plan("WI-015-race-target-" + sequence));
        billingAgreementRepository.saveAndFlush(BillingAgreement.builder()
                .user(subscriber)
                .provider(PaymentProviderType.TOSS)
                .status(BillingAgreementStatus.ACTIVE)
                .providerCustomerKey("wi-015-race-customer-" + sequence)
                .billingKeyCiphertext("encrypted-test-key")
                .billingKeyFingerprint("test-fingerprint-" + sequence)
                .nextBillingAt(LocalDate.now(BUSINESS_ZONE).plusMonths(1))
                .build());
        UserSubscription userSubscription = userSubscriptionRepository.saveAndFlush(
                UserSubscription.builder()
                        .user(subscriber)
                        .subscription(currentPlan)
                        .billingCycle(BillingCycle.MONTHLY)
                        .status(SubscriptionStatus.ACTIVE)
                        .startedAt(LocalDate.now(BUSINESS_ZONE).minusMonths(1))
                        .expiresAt(LocalDate.now(BUSINESS_ZONE).plusMonths(1))
                        .build());
        CustomUserDetails actor = CustomUserDetails.builder()
                .id(administrator.getId())
                .email(administrator.getEmail())
                .role(UserRole.ADMIN)
                .build();
        return new RaceFixture(actor, userSubscription, currentPlan, targetPlan);
    }

    private User user(String nickname, String email, String phone, UserRole role) {
        return User.builder()
                .nickname(nickname)
                .email(email)
                .password("encoded")
                .phonePersonal(phone)
                .job(UserJob.EDITOR)
                .userType(UserType.INDIVIDUAL)
                .role(role)
                .isVerified(true)
                .build();
    }

    private Subscription plan(String name) {
        return Subscription.builder()
                .name(name)
                .description("Disposable WI-015 concurrency plan")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(BigDecimal.TEN)
                .priceYearly(BigDecimal.valueOf(100))
                .downloadPerDay(10)
                .maxWhitelistChannels(10)
                .maxPlaylists(3)
                .isActive(true)
                .build();
    }

    private record RaceFixture(
            CustomUserDetails actor,
            UserSubscription userSubscription,
            Subscription currentPlan,
            Subscription targetPlan) {
    }
}

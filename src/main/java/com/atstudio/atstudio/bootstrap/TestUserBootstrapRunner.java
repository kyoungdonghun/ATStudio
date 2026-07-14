package com.atstudio.atstudio.bootstrap;

import com.atstudio.atstudio.entity.CompanyCertification;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.CompanyCertificationStatus;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserJob;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.CompanyCertificationRepository;
import com.atstudio.atstudio.repository.PlaylistRepository;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE)
@ConditionalOnProperty(prefix = "app.bootstrap.test-users", name = "enabled", havingValue = "true")
public class TestUserBootstrapRunner implements ApplicationRunner {

    private final TestUserBootstrapProperties properties;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final CompanyCertificationRepository companyCertificationRepository;
    private final PlaylistRepository playlistRepository;
    private final PlaylistService playlistService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String encodedPassword = passwordEncoder.encode(properties.getDefaultPassword());

        User admin = upsertUser(
                new FixtureUser(
                        "qa_admin",
                        "qa.admin@atstudio.local",
                        "010-9000-0001",
                        null,
                        true,
                        UserRole.ADMIN,
                        UserJob.EDITOR,
                        UserType.INDIVIDUAL,
                        null
                ),
                encodedPassword
        );
        ensureNoActiveSubscription(admin);

        User basicUser = upsertUser(
                new FixtureUser(
                        "qa_user",
                        "qa.user@atstudio.local",
                        "010-9000-0002",
                        null,
                        true,
                        UserRole.USER,
                        UserJob.EDITOR,
                        UserType.INDIVIDUAL,
                        null
                ),
                encodedPassword
        );
        ensureNoActiveSubscription(basicUser);

        User subscriber = upsertUser(
                new FixtureUser(
                        "qa_subscriber",
                        "qa.subscriber@atstudio.local",
                        "010-9000-0003",
                        null,
                        true,
                        UserRole.USER,
                        UserJob.ARTIST,
                        UserType.INDIVIDUAL,
                        null
                ),
                encodedPassword
        );
        ensureSubscription(subscriber, "DELUXE", UserType.INDIVIDUAL, SubscriptionStatus.ACTIVE, 21);

        User graceUser = upsertUser(
                new FixtureUser(
                        "qa_grace",
                        "qa.grace@atstudio.local",
                        "010-9000-0004",
                        null,
                        true,
                        UserRole.USER,
                        UserJob.FREELANCER,
                        UserType.INDIVIDUAL,
                        null
                ),
                encodedPassword
        );
        ensureSubscription(graceUser, "STANDARD", UserType.INDIVIDUAL, SubscriptionStatus.CANCELLED, 7);

        User businessUser = upsertUser(
                new FixtureUser(
                        "qa_business",
                        "qa.business@atstudio.local",
                        "010-9000-0005",
                        "02-555-9005",
                        true,
                        UserRole.USER,
                        UserJob.EDITOR,
                        UserType.BUSINESS,
                        "ATStudio QA Biz"
                ),
                encodedPassword
        );
        ensureApprovedBusinessCertification(businessUser);
        ensureSubscription(businessUser, "PREMIUM", UserType.BUSINESS, SubscriptionStatus.ACTIVE, 30);

        log.info("Non-prod QA bootstrap ready: fixtureUserCount={}", 5);
    }

    private User upsertUser(FixtureUser fixture, String encodedPassword) {
        User user = userRepository.findOneByEmail(fixture.email()).orElseGet(() ->
                User.builder()
                        .nickname(fixture.nickname())
                        .email(fixture.email())
                        .role(fixture.role())
                        .userType(fixture.userType())
                        .build()
        );

        user.applyBootstrapFixture(
                fixture.nickname(),
                encodedPassword,
                fixture.phonePersonal(),
                fixture.phoneCompany(),
                fixture.verified(),
                fixture.role(),
                fixture.job(),
                fixture.userType(),
                fixture.companyName()
        );

        return userRepository.save(user);
    }

    private void ensureNoActiveSubscription(User user) {
        if (hasPaymentHistory(user)) {
            log.info("Skipping QA bootstrap subscription reset: reason=PAYMENT_HISTORY");
            return;
        }
        userSubscriptionRepository.findByUser(user).ifPresent(existing ->
                existing.adminUpdate(
                        SubscriptionStatus.EXPIRED,
                        existing.getBillingCycle(),
                        LocalDate.now().minusDays(1)
                )
        );
    }

    private void ensureSubscription(
            User user,
            String planName,
            UserType userType,
            SubscriptionStatus desiredStatus,
            int expiresInDays
    ) {
        if (hasPaymentHistory(user)) {
            log.info("Skipping QA bootstrap subscription fixture alignment: reason=PAYMENT_HISTORY");
            return;
        }

        Optional<Subscription> maybePlan = subscriptionRepository.findByNameAndUserTypeAndIsActiveTrue(planName, userType);
        if (maybePlan.isEmpty()) {
            log.warn(
                    "Skipping QA bootstrap subscription: reason=PLAN_NOT_FOUND, userType={}, plan={}",
                    userType,
                    planName
            );
            return;
        }

        Subscription plan = maybePlan.get();
        LocalDate startedAt = LocalDate.now().minusDays(14);
        LocalDate expiresAt = LocalDate.now().plusDays(expiresInDays);

        UserSubscription subscription = userSubscriptionRepository.findByUser(user)
                .map(existing -> alignSubscription(existing, plan, expiresAt, desiredStatus))
                .orElseGet(() -> userSubscriptionRepository.save(
                        UserSubscription.builder()
                                .user(user)
                                .subscription(plan)
                                .billingCycle(BillingCycle.MONTHLY)
                                .status(desiredStatus)
                                .startedAt(startedAt)
                                .expiresAt(expiresAt)
                                .build()
                ));

        if (playlistRepository.countByUserAndIsActiveTrue(user) == 0
                && subscription.getStatus() != SubscriptionStatus.EXPIRED) {
            playlistService.createDefaultPlaylist(user);
        }
    }

    private UserSubscription alignSubscription(
            UserSubscription existing,
            Subscription plan,
            LocalDate expiresAt,
            SubscriptionStatus desiredStatus
    ) {
        existing.upgrade(plan, BillingCycle.MONTHLY, expiresAt);
        existing.adminUpdate(SubscriptionStatus.ACTIVE, BillingCycle.MONTHLY, expiresAt);
        if (desiredStatus == SubscriptionStatus.CANCELLED) {
            existing.cancel();
        }
        return existing;
    }

    private boolean hasPaymentHistory(User user) {
        return subscriptionPaymentRepository.existsByUser(user);
    }

    private void ensureApprovedBusinessCertification(User user) {
        if (companyCertificationRepository.existsByUserAndStatusIn(
                user,
                java.util.List.of(CompanyCertificationStatus.APPROVED)
        )) {
            return;
        }

        companyCertificationRepository.save(
                CompanyCertification.builder()
                        .user(user)
                        .status(CompanyCertificationStatus.APPROVED)
                        .adminNote("Non-production QA bootstrap fixture")
                        .documentPath("bootstrap/company-certifications/qa-business.pdf")
                        .certificationCode("BOOTSTRAP-QA-BIZ")
                        .approvedAt(LocalDateTime.now())
                        .build()
        );
    }

    private record FixtureUser(
            String nickname,
            String email,
            String phonePersonal,
            String phoneCompany,
            boolean verified,
            UserRole role,
            UserJob job,
            UserType userType,
            String companyName
    ) {
    }
}

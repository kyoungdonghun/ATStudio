package com.atstudio.atstudio.bootstrap;

import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Profile("acceptance")
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@ConditionalOnProperty(prefix = "app.acceptance", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "app.bootstrap.test-users", name = "enabled", havingValue = "true")
public class AcceptanceSubscriptionPlanBootstrapRunner implements ApplicationRunner {

    private static final List<CanonicalPlan> CANONICAL_PLANS = List.of(
            new CanonicalPlan("STANDARD", UserType.INDIVIDUAL, "9900.00", "99000.00", 5, 1, 3),
            new CanonicalPlan("DELUXE", UserType.INDIVIDUAL, "19900.00", "199000.00", 20, 2, 10),
            new CanonicalPlan("PREMIUM", UserType.INDIVIDUAL, "29900.00", "299000.00", -1, 2, 10),
            new CanonicalPlan("STANDARD", UserType.BUSINESS, "19900.00", "199000.00", 10, 1, 3),
            new CanonicalPlan("DELUXE", UserType.BUSINESS, "49900.00", "499000.00", 50, 2, 10),
            new CanonicalPlan("PREMIUM", UserType.BUSINESS, "99900.00", "999000.00", -1, 2, 10)
    );

    private final SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<PlanKey, List<Subscription>> existingPlans = findExistingCanonicalPlans();
        List<Subscription> missingPlans = new ArrayList<>();

        for (CanonicalPlan canonicalPlan : CANONICAL_PLANS) {
            List<Subscription> matches = existingPlans.getOrDefault(canonicalPlan.key(), List.of());
            if (matches.isEmpty()) {
                missingPlans.add(canonicalPlan.toEntity());
                continue;
            }
            if (matches.size() > 1) {
                refuse(canonicalPlan.key(), "DUPLICATE");
            }

            validateExistingPlan(canonicalPlan, matches.get(0));
        }

        if (!missingPlans.isEmpty()) {
            subscriptionRepository.saveAll(missingPlans);
        }
    }

    private Map<PlanKey, List<Subscription>> findExistingCanonicalPlans() {
        Map<PlanKey, List<Subscription>> existingPlans = new LinkedHashMap<>();

        for (Subscription subscription : subscriptionRepository.findAll()) {
            PlanKey candidateKey = PlanKey.from(subscription);
            if (candidateKey == null || !isCanonicalKey(candidateKey)) {
                continue;
            }
            existingPlans.computeIfAbsent(candidateKey, ignored -> new ArrayList<>()).add(subscription);
        }

        return existingPlans;
    }

    private boolean isCanonicalKey(PlanKey candidateKey) {
        return CANONICAL_PLANS.stream().anyMatch(plan -> plan.key().equals(candidateKey));
    }

    private void validateExistingPlan(CanonicalPlan canonicalPlan, Subscription existingPlan) {
        if (!existingPlan.isActive()) {
            refuse(canonicalPlan.key(), "INACTIVE");
        }
        if (!canonicalPlan.matches(existingPlan)) {
            refuse(canonicalPlan.key(), "PROPERTY_MISMATCH");
        }
    }

    private void refuse(PlanKey key, String reason) {
        throw new IllegalStateException(
                "Canonical subscription plan bootstrap refused: reason=" + reason
                        + ", plan=" + key.name()
                        + ", userType=" + key.userType()
        );
    }

    private record PlanKey(String name, UserType userType) {

        private static PlanKey from(Subscription subscription) {
            if (subscription.getName() == null || subscription.getUserType() == null) {
                return null;
            }
            String normalizedName = subscription.getName().trim().toUpperCase(Locale.ROOT);
            return new PlanKey(normalizedName, subscription.getUserType());
        }
    }

    private record CanonicalPlan(
            String name,
            UserType userType,
            BigDecimal priceMonthly,
            BigDecimal priceYearly,
            int downloadPerDay,
            int maxWhitelistChannels,
            int maxPlaylists
    ) {

        private CanonicalPlan(
                String name,
                UserType userType,
                String priceMonthly,
                String priceYearly,
                int downloadPerDay,
                int maxWhitelistChannels,
                int maxPlaylists
        ) {
            this(
                    name,
                    userType,
                    new BigDecimal(priceMonthly),
                    new BigDecimal(priceYearly),
                    downloadPerDay,
                    maxWhitelistChannels,
                    maxPlaylists
            );
        }

        private PlanKey key() {
            return new PlanKey(name, userType);
        }

        private Subscription toEntity() {
            return Subscription.builder()
                    .name(name)
                    .userType(userType)
                    .priceMonthly(priceMonthly)
                    .priceYearly(priceYearly)
                    .downloadPerDay(downloadPerDay)
                    .maxWhitelistChannels(maxWhitelistChannels)
                    .maxPlaylists(maxPlaylists)
                    .isActive(true)
                    .build();
        }

        private boolean matches(Subscription subscription) {
            return name.equals(subscription.getName())
                    && userType == subscription.getUserType()
                    && priceMonthly.compareTo(subscription.getPriceMonthly()) == 0
                    && priceYearly.compareTo(subscription.getPriceYearly()) == 0
                    && downloadPerDay == subscription.getDownloadPerDay()
                    && maxWhitelistChannels == subscription.getMaxWhitelistChannels()
                    && maxPlaylists == subscription.getMaxPlaylists();
        }
    }
}

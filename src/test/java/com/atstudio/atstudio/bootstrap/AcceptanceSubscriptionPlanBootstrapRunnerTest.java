package com.atstudio.atstudio.bootstrap;

import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.core.annotation.Order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Acceptance subscription plan bootstrap")
class AcceptanceSubscriptionPlanBootstrapRunnerTest {

    private static final DefaultApplicationArguments NO_ARGUMENTS =
            new DefaultApplicationArguments(new String[0]);

    @Mock
    SubscriptionRepository subscriptionRepository;

    @InjectMocks
    AcceptanceSubscriptionPlanBootstrapRunner runner;

    @Test
    @DisplayName("Fresh acceptance data receives the six canonical plans")
    void run_seedsSixCanonicalPlansForFreshAcceptanceData() throws Exception {
        AtomicReference<List<Subscription>> capturedPlans = new AtomicReference<>();
        given(subscriptionRepository.findAll()).willReturn(List.of());
        given(subscriptionRepository.saveAll(any())).willAnswer(invocation -> {
            Iterable<Subscription> plans = invocation.getArgument(0);
            List<Subscription> savedPlans = StreamSupport.stream(plans.spliterator(), false).toList();
            capturedPlans.set(savedPlans);
            return savedPlans;
        });

        runner.run(NO_ARGUMENTS);

        verify(subscriptionRepository).saveAll(any());
        List<Subscription> savedPlans = capturedPlans.get();

        assertThat(savedPlans).hasSize(6);
        assertThat(savedPlans)
                .extracting(plan -> plan.getName() + ":" + plan.getUserType())
                .containsExactly(
                        "STANDARD:INDIVIDUAL",
                        "DELUXE:INDIVIDUAL",
                        "PREMIUM:INDIVIDUAL",
                        "STANDARD:BUSINESS",
                        "DELUXE:BUSINESS",
                        "PREMIUM:BUSINESS"
                );
        assertPlan(savedPlans, "STANDARD", UserType.INDIVIDUAL, "9900.00", "99000.00", 5, 1, 3);
        assertPlan(savedPlans, "DELUXE", UserType.INDIVIDUAL, "19900.00", "199000.00", 20, 2, 10);
        assertPlan(savedPlans, "PREMIUM", UserType.INDIVIDUAL, "29900.00", "299000.00", -1, 2, 10);
        assertPlan(savedPlans, "STANDARD", UserType.BUSINESS, "19900.00", "199000.00", 10, 1, 3);
        assertPlan(savedPlans, "DELUXE", UserType.BUSINESS, "49900.00", "499000.00", 50, 2, 10);
        assertPlan(savedPlans, "PREMIUM", UserType.BUSINESS, "99900.00", "999000.00", -1, 2, 10);
    }

    @Test
    @DisplayName("Restart preserves canonical rows without a second write")
    void run_isIdempotentAcrossRestart() throws Exception {
        List<Subscription> storedPlans = new ArrayList<>();
        given(subscriptionRepository.findAll()).willAnswer(ignored -> List.copyOf(storedPlans));
        given(subscriptionRepository.saveAll(any())).willAnswer(invocation -> {
            Iterable<Subscription> plans = invocation.getArgument(0);
            List<Subscription> savedPlans = StreamSupport.stream(plans.spliterator(), false).toList();
            storedPlans.addAll(savedPlans);
            return savedPlans;
        });

        runner.run(NO_ARGUMENTS);
        List<Subscription> firstRunPlans = List.copyOf(storedPlans);
        runner.run(NO_ARGUMENTS);

        assertThat(storedPlans).containsExactlyElementsOf(firstRunPlans);
        assertThat(storedPlans).hasSize(6);
        verify(subscriptionRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("An inactive canonical row refuses startup")
    void run_refusesInactiveCanonicalPlan() {
        given(subscriptionRepository.findAll()).willReturn(List.of(
                plan("STANDARD", UserType.INDIVIDUAL, "9900.00", "99000.00", 5, 1, 3, false)
        ));

        assertThatThrownBy(() -> runner.run(NO_ARGUMENTS))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("reason=INACTIVE")
                .hasMessageContaining("plan=STANDARD")
                .hasMessageContaining("userType=INDIVIDUAL");
        verify(subscriptionRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("A conflicting canonical property refuses startup")
    void run_refusesConflictingCanonicalPlan() {
        given(subscriptionRepository.findAll()).willReturn(List.of(
                plan("DELUXE", UserType.INDIVIDUAL, "1.00", "199000.00", 20, 2, 10, true)
        ));

        assertThatThrownBy(() -> runner.run(NO_ARGUMENTS))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("reason=PROPERTY_MISMATCH")
                .hasMessageContaining("plan=DELUXE")
                .hasMessageContaining("userType=INDIVIDUAL")
                .hasMessageNotContaining("199000.00");
        verify(subscriptionRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Duplicate canonical keys refuse startup")
    void run_refusesDuplicateCanonicalPlan() {
        Subscription standardPlan = plan(
                "STANDARD",
                UserType.INDIVIDUAL,
                "9900.00",
                "99000.00",
                5,
                1,
                3,
                true
        );
        given(subscriptionRepository.findAll()).willReturn(List.of(standardPlan, standardPlan));

        assertThatThrownBy(() -> runner.run(NO_ARGUMENTS))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("reason=DUPLICATE");
        verify(subscriptionRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Plan bootstrap is ordered before the QA user bootstrap")
    void runnerOrder_precedesTestUserBootstrapRunner() {
        Order planOrder = AcceptanceSubscriptionPlanBootstrapRunner.class.getAnnotation(Order.class);
        Order userOrder = TestUserBootstrapRunner.class.getAnnotation(Order.class);

        assertThat(planOrder).isNotNull();
        assertThat(userOrder).isNotNull();
        assertThat(planOrder.value()).isLessThan(userOrder.value());
    }

    private static void assertPlan(
            List<Subscription> plans,
            String name,
            UserType userType,
            String monthlyPrice,
            String yearlyPrice,
            int downloadPerDay,
            int maxWhitelistChannels,
            int maxPlaylists
    ) {
        Subscription plan = plans.stream()
                .filter(candidate -> candidate.getName().equals(name) && candidate.getUserType() == userType)
                .findFirst()
                .orElseThrow();

        assertThat(plan.getPriceMonthly()).isEqualByComparingTo(monthlyPrice);
        assertThat(plan.getPriceYearly()).isEqualByComparingTo(yearlyPrice);
        assertThat(plan.getDownloadPerDay()).isEqualTo(downloadPerDay);
        assertThat(plan.getMaxWhitelistChannels()).isEqualTo(maxWhitelistChannels);
        assertThat(plan.getMaxPlaylists()).isEqualTo(maxPlaylists);
        assertThat(plan.isActive()).isTrue();
    }

    private static Subscription plan(
            String name,
            UserType userType,
            String monthlyPrice,
            String yearlyPrice,
            int downloadPerDay,
            int maxWhitelistChannels,
            int maxPlaylists,
            boolean active
    ) {
        return Subscription.builder()
                .name(name)
                .userType(userType)
                .priceMonthly(new BigDecimal(monthlyPrice))
                .priceYearly(new BigDecimal(yearlyPrice))
                .downloadPerDay(downloadPerDay)
                .maxWhitelistChannels(maxWhitelistChannels)
                .maxPlaylists(maxPlaylists)
                .isActive(active)
                .build();
    }
}

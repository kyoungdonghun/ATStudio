package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.subscription.SubscriptionResponse;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionService 단위 테스트")
class SubscriptionServiceTest {

    @Mock SubscriptionRepository subscriptionRepository;
    @InjectMocks SubscriptionService subscriptionService;

    // -- 6.1 getActiveSubscriptions ------------------------------------------

    @Nested
    @DisplayName("getActiveSubscriptions()")
    class GetActiveSubscriptions {

        @Test
        @DisplayName("성공 - userType 없이 전체 활성 플랜 조회")
        void getAll_noFilter() {
            Subscription sub = buildSubscription(1L, "Basic", UserType.INDIVIDUAL);
            given(subscriptionRepository.findAllByIsActive(true)).willReturn(List.of(sub));

            List<SubscriptionResponse> result = subscriptionService.getActiveSubscriptions(null);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("Basic");
        }

        @Test
        @DisplayName("성공 - userType=INDIVIDUAL 필터")
        void getAll_withFilter() {
            Subscription sub = buildSubscription(1L, "Basic", UserType.INDIVIDUAL);
            given(subscriptionRepository.findAllByUserTypeAndIsActive(UserType.INDIVIDUAL, true))
                    .willReturn(List.of(sub));

            List<SubscriptionResponse> result = subscriptionService.getActiveSubscriptions("INDIVIDUAL");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).userType()).isEqualTo("INDIVIDUAL");
        }

        @Test
        @DisplayName("성공 - 빈 문자열 userType은 전체 조회")
        void getAll_blankFilter() {
            given(subscriptionRepository.findAllByIsActive(true)).willReturn(List.of());

            List<SubscriptionResponse> result = subscriptionService.getActiveSubscriptions("  ");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("실패 - 잘못된 userType → INVALID_ARGUMENT (400)")
        void getAll_invalidUserType() {
            assertThatThrownBy(() -> subscriptionService.getActiveSubscriptions("WRONG_TYPE"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));
        }
    }

    // -- 6.2 getSubscription -------------------------------------------------

    @Nested
    @DisplayName("getSubscription()")
    class GetSubscription {

        @Test
        @DisplayName("성공 - 플랜 상세 조회")
        void getSubscription_success() {
            Subscription sub = buildSubscription(1L, "Premium", UserType.BUSINESS);
            given(subscriptionRepository.findById(1L)).willReturn(Optional.of(sub));

            SubscriptionResponse result = subscriptionService.getSubscription(1L);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("Premium");
        }

        @Test
        @DisplayName("실패 - 미존재 플랜 → SUBSCRIPTION_NOT_FOUND")
        void getSubscription_notFound() {
            given(subscriptionRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> subscriptionService.getSubscription(99L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));
        }
    }

    // -- helpers -------------------------------------------------------------

    private Subscription buildSubscription(Long id, String name, UserType userType) {
        Subscription sub = Subscription.builder()
                .name(name)
                .description("Test plan")
                .userType(userType)
                .priceMonthly(BigDecimal.valueOf(9900))
                .priceYearly(BigDecimal.valueOf(99000))
                .downloadPerDay(10)
                .maxWhitelistChannels(3)
                .build();
        ReflectionTestUtils.setField(sub, "id", id);
        return sub;
    }
}

package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.subscription.*;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.*;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.CompanyCertificationRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.payment.PaymentService;
import com.atstudio.atstudio.service.payment.billing.BillingKeyCrypto;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeCommand;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeResult;
import com.atstudio.atstudio.service.payment.provider.recurring.RecurringPaymentProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSubscriptionService 단위 테스트")
class UserSubscriptionServiceTest {

    @Mock UserSubscriptionRepository userSubscriptionRepository;
    @Mock SubscriptionRepository subscriptionRepository;
    @Mock UserRepository userRepository;
    @Mock CompanyCertificationRepository companyCertificationRepository;
    @Mock PaymentService paymentService;
    @Mock PlaylistService playlistService;
    @Mock BillingAgreementRepository billingAgreementRepository;
    @Mock PaymentOrderRepository paymentOrderRepository;
    @Mock SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Mock BillingKeyCrypto billingKeyCrypto;
    @Mock RecurringPaymentProvider recurringPaymentProvider;

    UserSubscriptionService userSubscriptionService;

    @BeforeEach
    void setUp() {
        userSubscriptionService = new UserSubscriptionService(
                userSubscriptionRepository,
                subscriptionRepository,
                userRepository,
                companyCertificationRepository,
                paymentService,
                playlistService,
                billingAgreementRepository,
                paymentOrderRepository,
                subscriptionPaymentRepository,
                billingKeyCrypto,
                List.of(recurringPaymentProvider)
        );
    }

    // -- 6.3 subscribe -------------------------------------------------------

    @Nested
    @DisplayName("subscribe()")
    class Subscribe {

        @Test
        @DisplayName("성공 - INDIVIDUAL 회원 MONTHLY 구독")
        void subscribe_individual_success() {
            User user = buildUser(1L, UserType.INDIVIDUAL);
            Subscription sub = buildSubscription(10L, "Basic", UserType.INDIVIDUAL);
            UserSubscription saved = buildUserSubscription(100L, user, sub,
                    BillingCycle.MONTHLY, SubscriptionStatus.ACTIVE);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.empty());
            given(subscriptionRepository.findById(10L)).willReturn(Optional.of(sub));
            given(userSubscriptionRepository.findByUser(user)).willReturn(Optional.empty());
            given(userSubscriptionRepository.save(any(UserSubscription.class))).willReturn(saved);
            given(paymentService.processPayment(any(), any(), any(), any(), any()))
                    .willReturn(buildPayment());

            UserSubscriptionResponse result = userSubscriptionService.subscribe(
                    buildUserDetails(1L), new UserSubscriptionRequest(10L, BillingCycle.MONTHLY));

            assertThat(result.id()).isEqualTo(100L);
            assertThat(result.status()).isEqualTo("ACTIVE");
            verify(paymentService).processPayment(eq(user), eq(saved), eq(sub),
                    eq(BillingCycle.MONTHLY), eq(BigDecimal.valueOf(9900)));
            verify(playlistService).createDefaultPlaylist(user);
        }

        @Test
        @DisplayName("성공 - BUSINESS 인증 완료 회원 구독")
        void subscribe_business_certified_success() {
            User user = buildUser(2L, UserType.BUSINESS);
            Subscription sub = buildSubscription(10L, "BizPlan", UserType.BUSINESS);
            UserSubscription saved = buildUserSubscription(101L, user, sub,
                    BillingCycle.YEARLY, SubscriptionStatus.ACTIVE);

            given(userRepository.findById(2L)).willReturn(Optional.of(user));
            given(companyCertificationRepository.existsByUserAndStatusIn(eq(user), anyList()))
                    .willReturn(true);
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.empty());
            given(subscriptionRepository.findById(10L)).willReturn(Optional.of(sub));
            given(userSubscriptionRepository.findByUser(user)).willReturn(Optional.empty());
            given(userSubscriptionRepository.save(any(UserSubscription.class))).willReturn(saved);
            given(paymentService.processPayment(any(), any(), any(), any(), any()))
                    .willReturn(buildPayment());

            UserSubscriptionResponse result = userSubscriptionService.subscribe(
                    buildUserDetails(2L), new UserSubscriptionRequest(10L, BillingCycle.YEARLY));

            assertThat(result.id()).isEqualTo(101L);
            verify(paymentService).processPayment(eq(user), eq(saved), eq(sub),
                    eq(BillingCycle.YEARLY), eq(BigDecimal.valueOf(99000)));
            verify(playlistService).createDefaultPlaylist(user);
        }

        @Test
        @DisplayName("성공 - 만료된 기존 row 재사용")
        void subscribe_reusesExpiredSubscriptionRow() {
            User user = buildUser(1L, UserType.INDIVIDUAL);
            Subscription oldSub = buildSubscription(9L, "Old", UserType.INDIVIDUAL);
            Subscription newSub = buildSubscription(10L, "Basic", UserType.INDIVIDUAL);
            UserSubscription expired = buildUserSubscription(100L, user, oldSub,
                    BillingCycle.YEARLY, SubscriptionStatus.EXPIRED);
            ReflectionTestUtils.setField(expired, "expiresAt", LocalDate.now().minusDays(1));

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.empty());
            given(subscriptionRepository.findById(10L)).willReturn(Optional.of(newSub));
            given(userSubscriptionRepository.findByUser(user)).willReturn(Optional.of(expired));
            given(paymentService.processPayment(any(), any(), any(), any(), any()))
                    .willReturn(buildPayment());

            UserSubscriptionResponse result = userSubscriptionService.subscribe(
                    buildUserDetails(1L), new UserSubscriptionRequest(10L, BillingCycle.MONTHLY));

            assertThat(result.id()).isEqualTo(100L);
            assertThat(result.status()).isEqualTo("ACTIVE");
            assertThat(expired.getSubscription()).isEqualTo(newSub);
            assertThat(expired.getBillingCycle()).isEqualTo(BillingCycle.MONTHLY);
            verify(userSubscriptionRepository, never()).save(any(UserSubscription.class));
            verify(paymentService).processPayment(eq(user), eq(expired), eq(newSub),
                    eq(BillingCycle.MONTHLY), eq(BigDecimal.valueOf(9900)));
            verify(playlistService).createDefaultPlaylist(user);
        }

        @Test
        @DisplayName("실패 - BUSINESS 미인증 → COMPANY_CERTIFICATION_REQUIRED")
        void subscribe_business_notCertified() {
            User user = buildUser(2L, UserType.BUSINESS);

            given(userRepository.findById(2L)).willReturn(Optional.of(user));
            given(companyCertificationRepository.existsByUserAndStatusIn(eq(user), anyList()))
                    .willReturn(false);

            assertThatThrownBy(() -> userSubscriptionService.subscribe(
                    buildUserDetails(2L), new UserSubscriptionRequest(10L, BillingCycle.MONTHLY)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.COMPANY_CERTIFICATION_REQUIRED));
        }

        @Test
        @DisplayName("실패 - 중복 구독 → SUBSCRIPTION_ALREADY_EXISTS")
        void subscribe_duplicate() {
            User user = buildUser(1L, UserType.INDIVIDUAL);
            Subscription sub = buildSubscription(10L, "Basic", UserType.INDIVIDUAL);
            UserSubscription existing = buildUserSubscription(100L, user, sub,
                    BillingCycle.MONTHLY, SubscriptionStatus.ACTIVE);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.of(existing));

            assertThatThrownBy(() -> userSubscriptionService.subscribe(
                    buildUserDetails(1L), new UserSubscriptionRequest(10L, BillingCycle.MONTHLY)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.SUBSCRIPTION_ALREADY_EXISTS));
        }

        @Test
        @DisplayName("실패 - 미존재 플랜 → SUBSCRIPTION_NOT_FOUND")
        void subscribe_planNotFound() {
            User user = buildUser(1L, UserType.INDIVIDUAL);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.empty());
            given(subscriptionRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userSubscriptionService.subscribe(
                    buildUserDetails(1L), new UserSubscriptionRequest(99L, BillingCycle.MONTHLY)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));
        }
    }

    // -- 6.4 getMySubscription -----------------------------------------------

    @Nested
    @DisplayName("getMySubscription()")
    class GetMySubscription {

        @Test
        @DisplayName("성공 - 활성 구독 조회")
        void getMySubscription_success() {
            User user = buildUser(1L, UserType.INDIVIDUAL);
            Subscription sub = buildSubscription(10L, "Basic", UserType.INDIVIDUAL);
            UserSubscription us = buildUserSubscription(100L, user, sub,
                    BillingCycle.MONTHLY, SubscriptionStatus.ACTIVE);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.of(us));

            UserSubscriptionResponse result = userSubscriptionService.getMySubscription(
                    buildUserDetails(1L));

            assertThat(result.id()).isEqualTo(100L);
            assertThat(result.status()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("실패 - 활성 구독 없음 → NO_ACTIVE_SUBSCRIPTION")
        void getMySubscription_notFound() {
            User user = buildUser(1L, UserType.INDIVIDUAL);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> userSubscriptionService.getMySubscription(
                    buildUserDetails(1L)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));
        }
    }

    // -- 6.5 listAll ---------------------------------------------------------

    @Nested
    @DisplayName("listAll()")
    class ListAll {

        @Test
        @DisplayName("성공 - 페이지네이션 조회")
        void listAll_success() {
            User user = buildUser(1L, UserType.INDIVIDUAL);
            Subscription sub = buildSubscription(10L, "Basic", UserType.INDIVIDUAL);
            UserSubscription us = buildUserSubscription(100L, user, sub,
                    BillingCycle.MONTHLY, SubscriptionStatus.ACTIVE);

            Page<UserSubscription> page = new PageImpl<>(List.of(us));
            given(userSubscriptionRepository.findAll(any(Pageable.class))).willReturn(page);

            ResponseDTO<UserSubscriptionResponse> result = userSubscriptionService.listAll(1, 20);

            assertThat(result.getDataList()).hasSize(1);
            assertThat(result.getDataList().get(0).id()).isEqualTo(100L);
        }
    }

    // -- 6.6 getDetail -------------------------------------------------------

    @Nested
    @DisplayName("getDetail()")
    class GetDetail {

        @Test
        @DisplayName("성공 - 구독 상세 조회")
        void getDetail_success() {
            User user = buildUser(1L, UserType.INDIVIDUAL);
            Subscription sub = buildSubscription(10L, "Basic", UserType.INDIVIDUAL);
            UserSubscription us = buildUserSubscription(100L, user, sub,
                    BillingCycle.MONTHLY, SubscriptionStatus.ACTIVE);

            given(userSubscriptionRepository.findById(100L)).willReturn(Optional.of(us));

            UserSubscriptionResponse result = userSubscriptionService.getDetail(100L);

            assertThat(result.id()).isEqualTo(100L);
        }

        @Test
        @DisplayName("실패 - 미존재 → SUBSCRIPTION_NOT_FOUND")
        void getDetail_notFound() {
            given(userSubscriptionRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userSubscriptionService.getDetail(99L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));
        }
    }

    // -- 6.7 changeSubscription ----------------------------------------------

    @Nested
    @DisplayName("changeSubscription()")
    class ChangeSubscription {

        @Test
        @DisplayName("성공 - UPGRADE 빌링키 차액 결제 후 즉시 적용")
        void changeSubscription_upgrade_immediate() {
            User user = buildUser(1L, UserType.INDIVIDUAL);
            Subscription currentSub = buildSubscription(10L, "Basic", UserType.INDIVIDUAL);
            // priceMonthly=9900, 30일 구독 중 15일 남음
            UserSubscription us = buildUserSubscription(100L, user, currentSub,
                    BillingCycle.MONTHLY, SubscriptionStatus.ACTIVE);
            ReflectionTestUtils.setField(us, "startedAt", LocalDate.now().minusDays(15));
            ReflectionTestUtils.setField(us, "expiresAt", LocalDate.now().plusDays(15));

            Subscription newSub = buildSubscription(20L, "Premium", UserType.INDIVIDUAL);
            ReflectionTestUtils.setField(newSub, "priceMonthly", BigDecimal.valueOf(19900));
            BillingAgreement agreement = buildActiveAgreement(user);
            LocalDate originalExpiresAt = us.getExpiresAt();

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.of(us));
            given(subscriptionRepository.findById(20L)).willReturn(Optional.of(newSub));
            given(billingAgreementRepository.findByUserAndProvider(user, PaymentProviderType.TOSS_BILLING))
                    .willReturn(Optional.of(agreement));
            given(paymentOrderRepository.save(any(PaymentOrder.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("raw-billing-key");
            given(recurringPaymentProvider.getProviderType()).willReturn(PaymentProviderType.TOSS_BILLING);
            given(recurringPaymentProvider.charge(any()))
                    .willReturn(BillingChargeResult.success(
                            "tx_upgrade",
                            "CARD",
                            "1234",
                            "{\"paymentKey\":\"pay_upgrade\"}"));
            given(subscriptionPaymentRepository.save(any(SubscriptionPayment.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            ChangeSubscriptionResponse result = userSubscriptionService.changeSubscription(
                    buildUserDetails(1L),
                    new ChangeSubscriptionRequest(20L, BillingCycle.MONTHLY));

            assertThat(result.subscription().id()).isEqualTo(20L);
            assertThat(result.changeType()).isEqualTo("UPGRADE");
            assertThat(result.proratedAmount()).isNotNull();
            assertThat(result.proratedAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
            assertThat(result.expiresAt()).isEqualTo(originalExpiresAt);
            verify(paymentService, never()).processPayment(any(), any(), any(), any(), any());
            verify(subscriptionPaymentRepository).save(any(SubscriptionPayment.class));

            assertThat(us.getSubscription()).isEqualTo(newSub);
            assertThat(us.getBillingCycle()).isEqualTo(BillingCycle.MONTHLY);
            assertThat(us.getExpiresAt()).isEqualTo(originalExpiresAt);
            assertThat(us.getPendingSubscription()).isNull();
            assertThat(us.getPendingBillingCycle()).isNull();

            ArgumentCaptor<BillingChargeCommand> chargeCaptor =
                    ArgumentCaptor.forClass(BillingChargeCommand.class);
            verify(recurringPaymentProvider).charge(chargeCaptor.capture());
            assertThat(chargeCaptor.getValue().amount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
            assertThat(chargeCaptor.getValue().orderName()).contains("Premium");
        }

        @Test
        @DisplayName("success - UPGRADE keeps current annual cycle and schedules requested monthly cycle")
        void changeSubscription_upgradeKeepsCurrentCycleAndSchedulesNextCycle() {
            User user = buildUser(1L, UserType.INDIVIDUAL);
            Subscription currentSub = buildSubscription(10L, "Standard", UserType.INDIVIDUAL);
            ReflectionTestUtils.setField(currentSub, "priceMonthly", BigDecimal.valueOf(9900));
            ReflectionTestUtils.setField(currentSub, "priceYearly", BigDecimal.valueOf(99000));
            UserSubscription us = buildUserSubscription(100L, user, currentSub,
                    BillingCycle.YEARLY, SubscriptionStatus.ACTIVE);
            ReflectionTestUtils.setField(us, "startedAt", LocalDate.now());
            ReflectionTestUtils.setField(us, "expiresAt", LocalDate.now().plusYears(1));

            Subscription newSub = buildSubscription(20L, "Premium", UserType.INDIVIDUAL);
            ReflectionTestUtils.setField(newSub, "priceMonthly", BigDecimal.valueOf(29900));
            ReflectionTestUtils.setField(newSub, "priceYearly", BigDecimal.valueOf(299000));
            BillingAgreement agreement = buildActiveAgreement(user);
            LocalDate originalExpiresAt = us.getExpiresAt();

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.of(us));
            given(subscriptionRepository.findById(20L)).willReturn(Optional.of(newSub));
            given(billingAgreementRepository.findByUserAndProvider(user, PaymentProviderType.TOSS_BILLING))
                    .willReturn(Optional.of(agreement));
            given(paymentOrderRepository.save(any(PaymentOrder.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("raw-billing-key");
            given(recurringPaymentProvider.getProviderType()).willReturn(PaymentProviderType.TOSS_BILLING);
            given(recurringPaymentProvider.charge(any()))
                    .willReturn(BillingChargeResult.success(
                            "tx_upgrade",
                            "CARD",
                            "1234",
                            "{\"paymentKey\":\"pay_upgrade\"}"));
            given(subscriptionPaymentRepository.save(any(SubscriptionPayment.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            ChangeSubscriptionResponse result = userSubscriptionService.changeSubscription(
                    buildUserDetails(1L),
                    new ChangeSubscriptionRequest(20L, BillingCycle.MONTHLY));

            assertThat(result.changeType()).isEqualTo("UPGRADE");
            assertThat(result.billingCycle()).isEqualTo("MONTHLY");
            assertThat(result.proratedAmount()).isEqualByComparingTo(BigDecimal.valueOf(200000));
            assertThat(result.expiresAt()).isEqualTo(originalExpiresAt);
            assertThat(us.getSubscription()).isEqualTo(newSub);
            assertThat(us.getBillingCycle()).isEqualTo(BillingCycle.YEARLY);
            assertThat(us.getExpiresAt()).isEqualTo(originalExpiresAt);
            assertThat(us.getPendingSubscription()).isEqualTo(newSub);
            assertThat(us.getPendingBillingCycle()).isEqualTo(BillingCycle.MONTHLY);

            ArgumentCaptor<PaymentOrder> orderCaptor = ArgumentCaptor.forClass(PaymentOrder.class);
            verify(paymentOrderRepository).save(orderCaptor.capture());
            assertThat(orderCaptor.getValue().getBillingCycle()).isEqualTo(BillingCycle.YEARLY);

            ArgumentCaptor<SubscriptionPayment> paymentCaptor =
                    ArgumentCaptor.forClass(SubscriptionPayment.class);
            verify(subscriptionPaymentRepository).save(paymentCaptor.capture());
            assertThat(paymentCaptor.getValue().getBillingCycle()).isEqualTo(BillingCycle.YEARLY);
        }

        @Test
        @DisplayName("성공 - UPGRADE 차액은 정수 원으로 반올림")
        void changeSubscription_upgrade_roundsProratedAmountToWholeWon() {
            User user = buildUser(1L, UserType.INDIVIDUAL);
            Subscription currentSub = buildSubscription(10L, "Basic", UserType.INDIVIDUAL);
            ReflectionTestUtils.setField(currentSub, "priceMonthly", BigDecimal.valueOf(10000));
            UserSubscription us = buildUserSubscription(100L, user, currentSub,
                    BillingCycle.MONTHLY, SubscriptionStatus.ACTIVE);
            ReflectionTestUtils.setField(us, "startedAt", LocalDate.now().minusDays(20));
            ReflectionTestUtils.setField(us, "expiresAt", LocalDate.now().plusDays(10));

            Subscription newSub = buildSubscription(20L, "Premium", UserType.INDIVIDUAL);
            ReflectionTestUtils.setField(newSub, "priceMonthly", BigDecimal.valueOf(20000));
            BillingAgreement agreement = buildActiveAgreement(user);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.of(us));
            given(subscriptionRepository.findById(20L)).willReturn(Optional.of(newSub));
            given(billingAgreementRepository.findByUserAndProvider(user, PaymentProviderType.TOSS_BILLING))
                    .willReturn(Optional.of(agreement));
            given(paymentOrderRepository.save(any(PaymentOrder.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("raw-billing-key");
            given(recurringPaymentProvider.getProviderType()).willReturn(PaymentProviderType.TOSS_BILLING);
            given(recurringPaymentProvider.charge(any()))
                    .willReturn(BillingChargeResult.success(
                            "tx_upgrade",
                            "CARD",
                            "1234",
                            "{\"paymentKey\":\"pay_upgrade\"}"));
            given(subscriptionPaymentRepository.save(any(SubscriptionPayment.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            ChangeSubscriptionResponse result = userSubscriptionService.changeSubscription(
                    buildUserDetails(1L),
                    new ChangeSubscriptionRequest(20L, BillingCycle.MONTHLY));

            assertThat(result.proratedAmount()).isEqualByComparingTo(BigDecimal.valueOf(3333));
            ArgumentCaptor<BillingChargeCommand> chargeCaptor =
                    ArgumentCaptor.forClass(BillingChargeCommand.class);
            verify(recurringPaymentProvider).charge(chargeCaptor.capture());
            assertThat(chargeCaptor.getValue().amount()).isEqualByComparingTo(BigDecimal.valueOf(3333));
        }

        @Test
        @DisplayName("성공 - UPGRADE 차액 0원은 결제 호출 없이 적용")
        void changeSubscription_upgrade_skipsChargeWhenProratedAmountIsZero() {
            User user = buildUser(1L, UserType.INDIVIDUAL);
            Subscription currentSub = buildSubscription(10L, "Basic", UserType.INDIVIDUAL);
            UserSubscription us = buildUserSubscription(100L, user, currentSub,
                    BillingCycle.MONTHLY, SubscriptionStatus.ACTIVE);
            ReflectionTestUtils.setField(us, "startedAt", LocalDate.now().minusDays(30));
            ReflectionTestUtils.setField(us, "expiresAt", LocalDate.now());

            Subscription newSub = buildSubscription(20L, "Premium", UserType.INDIVIDUAL);
            ReflectionTestUtils.setField(newSub, "priceMonthly", BigDecimal.valueOf(19900));
            BillingAgreement agreement = buildActiveAgreement(user);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.of(us));
            given(subscriptionRepository.findById(20L)).willReturn(Optional.of(newSub));
            given(billingAgreementRepository.findByUserAndProvider(user, PaymentProviderType.TOSS_BILLING))
                    .willReturn(Optional.of(agreement));

            ChangeSubscriptionResponse result = userSubscriptionService.changeSubscription(
                    buildUserDetails(1L),
                    new ChangeSubscriptionRequest(20L, BillingCycle.MONTHLY));

            assertThat(result.proratedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(us.getSubscription()).isEqualTo(newSub);
            verify(paymentOrderRepository, never()).save(any(PaymentOrder.class));
            verify(recurringPaymentProvider, never()).charge(any());
            verify(subscriptionPaymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - UPGRADE 자동결제 등록 없음 → 구독 변경 없음")
        void changeSubscription_upgrade_requiresBillingAgreement() {
            User user = buildUser(1L, UserType.INDIVIDUAL);
            Subscription currentSub = buildSubscription(10L, "Basic", UserType.INDIVIDUAL);
            UserSubscription us = buildUserSubscription(100L, user, currentSub,
                    BillingCycle.MONTHLY, SubscriptionStatus.ACTIVE);
            Subscription newSub = buildSubscription(20L, "Premium", UserType.INDIVIDUAL);
            ReflectionTestUtils.setField(newSub, "priceMonthly", BigDecimal.valueOf(19900));

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.of(us));
            given(subscriptionRepository.findById(20L)).willReturn(Optional.of(newSub));
            given(billingAgreementRepository.findByUserAndProvider(user, PaymentProviderType.TOSS_BILLING))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> userSubscriptionService.changeSubscription(
                    buildUserDetails(1L),
                    new ChangeSubscriptionRequest(20L, BillingCycle.MONTHLY)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.BILLING_AGREEMENT_NOT_FOUND));

            assertThat(us.getSubscription()).isEqualTo(currentSub);
            verify(recurringPaymentProvider, never()).charge(any());
            verify(subscriptionPaymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("성공 - DOWNGRADE pending 저장 + payment 미호출")
        void changeSubscription_downgrade_pending() {
            User user = buildUser(1L, UserType.INDIVIDUAL);
            Subscription currentSub = buildSubscription(20L, "Premium", UserType.INDIVIDUAL);
            ReflectionTestUtils.setField(currentSub, "priceMonthly", BigDecimal.valueOf(19900));
            UserSubscription us = buildUserSubscription(100L, user, currentSub,
                    BillingCycle.MONTHLY, SubscriptionStatus.ACTIVE);
            ReflectionTestUtils.setField(us, "startedAt", LocalDate.now().minusDays(15));
            ReflectionTestUtils.setField(us, "expiresAt", LocalDate.now().plusDays(15));

            Subscription newSub = buildSubscription(10L, "Basic", UserType.INDIVIDUAL);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.of(us));
            given(subscriptionRepository.findById(10L)).willReturn(Optional.of(newSub));

            ChangeSubscriptionResponse result = userSubscriptionService.changeSubscription(
                    buildUserDetails(1L),
                    new ChangeSubscriptionRequest(10L, BillingCycle.MONTHLY));

            // DOWNGRADE 확인
            assertThat(result.changeType()).isEqualTo("DOWNGRADE");
            assertThat(result.proratedAmount()).isEqualByComparingTo(BigDecimal.ZERO);

            // pending 필드 설정됨
            assertThat(us.getPendingSubscription()).isEqualTo(newSub);
            assertThat(us.getPendingBillingCycle()).isEqualTo(BillingCycle.MONTHLY);

            // 현재 구독은 변경되지 않음 (Premium 유지)
            assertThat(us.getSubscription()).isEqualTo(currentSub);

            // payment 미호출 verify
            verify(paymentService, org.mockito.Mockito.never())
                    .processPayment(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("실패 - 활성 구독 없음 → NO_ACTIVE_SUBSCRIPTION")
        void change_noActiveSubscription() {
            User user = buildUser(1L, UserType.INDIVIDUAL);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> userSubscriptionService.changeSubscription(
                    buildUserDetails(1L),
                    new ChangeSubscriptionRequest(20L, BillingCycle.MONTHLY)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));
        }
    }

    // -- 6.8 adminUpdate -----------------------------------------------------

    @Nested
    @DisplayName("adminUpdate()")
    class AdminUpdate {

        @Test
        @DisplayName("성공 - status + billingCycle + expiresAt 전부 변경")
        void adminUpdate_allFields() {
            User user = buildUser(1L, UserType.INDIVIDUAL);
            Subscription sub = buildSubscription(10L, "Basic", UserType.INDIVIDUAL);
            UserSubscription us = buildUserSubscription(100L, user, sub,
                    BillingCycle.MONTHLY, SubscriptionStatus.ACTIVE);

            given(userSubscriptionRepository.findById(100L)).willReturn(Optional.of(us));

            LocalDate newExpiry = LocalDate.now().plusYears(1);
            UserSubscriptionResponse result = userSubscriptionService.adminUpdate(100L,
                    new AdminUpdateSubscriptionRequest(
                            SubscriptionStatus.CANCELLED, BillingCycle.YEARLY, newExpiry));

            assertThat(result.status()).isEqualTo("CANCELLED");
            assertThat(result.billingCycle()).isEqualTo("YEARLY");
            assertThat(result.expiresAt()).isEqualTo(newExpiry);
        }

        @Test
        @DisplayName("성공 - null 필드는 변경하지 않음")
        void adminUpdate_partial() {
            User user = buildUser(1L, UserType.INDIVIDUAL);
            Subscription sub = buildSubscription(10L, "Basic", UserType.INDIVIDUAL);
            UserSubscription us = buildUserSubscription(100L, user, sub,
                    BillingCycle.MONTHLY, SubscriptionStatus.ACTIVE);

            given(userSubscriptionRepository.findById(100L)).willReturn(Optional.of(us));

            UserSubscriptionResponse result = userSubscriptionService.adminUpdate(100L,
                    new AdminUpdateSubscriptionRequest(null, null, null));

            assertThat(result.status()).isEqualTo("ACTIVE");
            assertThat(result.billingCycle()).isEqualTo("MONTHLY");
        }

        @Test
        @DisplayName("실패 - 미존재 → SUBSCRIPTION_NOT_FOUND")
        void adminUpdate_notFound() {
            given(userSubscriptionRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userSubscriptionService.adminUpdate(99L,
                    new AdminUpdateSubscriptionRequest(null, null, null)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));
        }
    }

    // -- 6.9 adminCancel -----------------------------------------------------

    @Nested
    @DisplayName("adminCancel()")
    class AdminCancel {

        @Test
        @DisplayName("성공 - 관리자 취소")
        void adminCancel_success() {
            User user = buildUser(1L, UserType.INDIVIDUAL);
            Subscription sub = buildSubscription(10L, "Basic", UserType.INDIVIDUAL);
            UserSubscription us = buildUserSubscription(100L, user, sub,
                    BillingCycle.MONTHLY, SubscriptionStatus.ACTIVE);

            given(userSubscriptionRepository.findById(100L)).willReturn(Optional.of(us));

            userSubscriptionService.adminCancel(100L);

            assertThat(us.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
        }

        @Test
        @DisplayName("실패 - 미존재 → SUBSCRIPTION_NOT_FOUND")
        void adminCancel_notFound() {
            given(userSubscriptionRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userSubscriptionService.adminCancel(99L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));
        }
    }

    // -- 6.10 selfCancel -----------------------------------------------------

    @Nested
    @DisplayName("selfCancel()")
    class SelfCancel {

        @Test
        @DisplayName("성공 - 셀프 취소")
        void selfCancel_success() {
            User user = buildUser(1L, UserType.INDIVIDUAL);
            Subscription sub = buildSubscription(10L, "Basic", UserType.INDIVIDUAL);
            UserSubscription us = buildUserSubscription(100L, user, sub,
                    BillingCycle.MONTHLY, SubscriptionStatus.ACTIVE);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.of(us));

            userSubscriptionService.selfCancel(buildUserDetails(1L));

            assertThat(us.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
        }

        @Test
        @DisplayName("실패 - 활성 구독 없음 → NO_ACTIVE_SUBSCRIPTION")
        void selfCancel_noActive() {
            User user = buildUser(1L, UserType.INDIVIDUAL);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> userSubscriptionService.selfCancel(buildUserDetails(1L)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));
        }
    }

    // -- BD-1 구독 취소 유예 기간 테스트 ----------------------------------------

    @Nested
    @DisplayName("BD-1: CANCELLED 구독 유예 기간")
    class CancelledGracePeriod {

        @Test
        @DisplayName("selfCancel 후 status=CANCELLED이고 expiresAt 유지됨")
        void selfCancel_statusCancelledAndExpiresAtPreserved() {
            User user = buildUser(1L, UserType.INDIVIDUAL);
            Subscription sub = buildSubscription(10L, "Basic", UserType.INDIVIDUAL);
            LocalDate originalExpiresAt = LocalDate.now().plusDays(20);
            UserSubscription us = buildUserSubscription(100L, user, sub,
                    BillingCycle.MONTHLY, SubscriptionStatus.ACTIVE);
            ReflectionTestUtils.setField(us, "expiresAt", originalExpiresAt);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.of(us));

            userSubscriptionService.selfCancel(buildUserDetails(1L));

            assertThat(us.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
            assertThat(us.getExpiresAt()).isEqualTo(originalExpiresAt);
        }

        @Test
        @DisplayName("CANCELLED 상태 + expiresAt 이내 → getMySubscription 정상 반환")
        void cancelledWithinGracePeriod_getMySubscription_returnsSubscription() {
            User user = buildUser(1L, UserType.INDIVIDUAL);
            Subscription sub = buildSubscription(10L, "Basic", UserType.INDIVIDUAL);
            UserSubscription us = buildUserSubscription(100L, user, sub,
                    BillingCycle.MONTHLY, SubscriptionStatus.CANCELLED);
            ReflectionTestUtils.setField(us, "expiresAt", LocalDate.now().plusDays(10));

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.of(us));

            UserSubscriptionResponse result = userSubscriptionService.getMySubscription(
                    buildUserDetails(1L));

            assertThat(result.id()).isEqualTo(100L);
            assertThat(result.status()).isEqualTo("CANCELLED");
        }

        @Test
        @DisplayName("expiresAt 지난 CANCELLED 구독 → NO_ACTIVE_SUBSCRIPTION")
        void cancelledPastExpiry_getMySubscription_throwsNoActive() {
            User user = buildUser(1L, UserType.INDIVIDUAL);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> userSubscriptionService.getMySubscription(
                    buildUserDetails(1L)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));
        }
    }

    // -- helpers -------------------------------------------------------------

    private User buildUser(Long id, UserType userType) {
        User user = User.builder()
                .email("user" + id + "@test.com")
                .nickname("user" + id)
                .password("pw")
                .userType(userType)
                .role(UserRole.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

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

    private UserSubscription buildUserSubscription(Long id, User user, Subscription sub,
                                                    BillingCycle cycle, SubscriptionStatus status) {
        UserSubscription us = UserSubscription.builder()
                .user(user)
                .subscription(sub)
                .billingCycle(cycle)
                .status(status)
                .startedAt(LocalDate.now())
                .expiresAt(cycle == BillingCycle.MONTHLY
                        ? LocalDate.now().plusMonths(1)
                        : LocalDate.now().plusYears(1))
                .build();
        ReflectionTestUtils.setField(us, "id", id);
        return us;
    }

    private SubscriptionPayment buildPayment() {
        return SubscriptionPayment.builder()
                .amount(BigDecimal.ZERO)
                .billingCycle(BillingCycle.MONTHLY)
                .paymentStatus(PaymentStatus.DONE)
                .pgTransactionId("MOCK-test")
                .build();
    }

    private BillingAgreement buildActiveAgreement(User user) {
        BillingAgreement agreement = BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("ats_billing_customer_1")
                .build();
        agreement.activate("encrypted-key", "fingerprint", "CARD", "1234", LocalDate.now().plusDays(15));
        return agreement;
    }

    private CustomUserDetails buildUserDetails(Long id) {
        return CustomUserDetails.builder()
                .id(id)
                .email("user" + id + "@test.com")
                .password("pw")
                .role(UserRole.USER)
                .isDeleted(false)
                .isProfileComplete(true)
                .build();
    }
}

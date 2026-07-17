package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.entity.enums.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Entity @Builder.Default 값 검증")
class EntityDefaultValueTest {

    @Test
    @DisplayName("User: isVerified=false, role=USER, userType=INDIVIDUAL, isDeleted=false")
    void user_defaults() {
        User user = User.builder()
                .nickname("test")
                .email("test@test.com")
                .build();

        assertThat(user.isVerified()).isFalse();
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        assertThat(user.getUserType()).isEqualTo(UserType.INDIVIDUAL);
        assertThat(user.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("Track: isActive=false, playCount=0")
    void track_defaults() {
        Track track = Track.builder()
                .title("Test Track")
                .bpm(120)
                .tonality("C")
                .audioFile("test.mp3")
                .build();

        assertThat(track.isActive()).isFalse();
        assertThat(track.getPlayCount()).isZero();
    }

    @Test
    @DisplayName("Playlist: isActive=true")
    void playlist_defaults() {
        Playlist playlist = Playlist.builder()
                .title("Test Playlist")
                .build();

        assertThat(playlist.isActive()).isTrue();
    }

    @Test
    @DisplayName("Question: isPublic=false, status=OPEN")
    void question_defaults() {
        Question question = Question.builder()
                .title("Test Question")
                .content("Content")
                .category(QuestionCategory.OTHER)
                .build();

        assertThat(question.isPublic()).isFalse();
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.OPEN);
    }

    @Test
    @DisplayName("Notice: isPinned=false")
    void notice_defaults() {
        Notice notice = Notice.builder()
                .title("Test Notice")
                .content("Content")
                .build();

        assertThat(notice.isPinned()).isFalse();
    }

    @Test
    @DisplayName("Subscription: isActive=true")
    void subscription_defaults() {
        Subscription subscription = Subscription.builder()
                .name("STANDARD")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(BigDecimal.valueOf(9900))
                .priceYearly(BigDecimal.valueOf(99000))
                .downloadPerDay(5)
                .maxWhitelistChannels(1)
                .build();

        assertThat(subscription.isActive()).isTrue();
    }

    @Test
    @DisplayName("UserSubscription: status=ACTIVE")
    void userSubscription_defaults() {
        UserSubscription us = UserSubscription.builder()
                .billingCycle(BillingCycle.MONTHLY)
                .startedAt(LocalDate.now())
                .expiresAt(LocalDate.now().plusMonths(1))
                .build();

        assertThat(us.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    @DisplayName("BillingAgreement: status=READY, failureCount=0")
    void billingAgreement_defaults() {
        BillingAgreement agreement = BillingAgreement.builder()
                .provider(PaymentProviderType.TOSS)
                .providerCustomerKey("ats_billing_random")
                .build();

        assertThat(agreement.getStatus()).isEqualTo(BillingAgreementStatus.READY);
        assertThat(agreement.getFailureCount()).isZero();
    }

    @Test
    @DisplayName("CompanyCertification: status=PENDING")
    void companyCertification_defaults() {
        User mockUser = org.mockito.Mockito.mock(User.class);
        CompanyCertification cert = CompanyCertification.builder()
                .user(mockUser)
                .documentPath("/uploads/company-docs/1/")
                .build();

        assertThat(cert.getStatus()).isEqualTo(CompanyCertificationStatus.PENDING);
    }

    @Test
    @DisplayName("SubscriptionPayment: paymentStatus=READY")
    void subscriptionPayment_defaults() {
        SubscriptionPayment payment = SubscriptionPayment.builder()
                .billingCycle(BillingCycle.MONTHLY)
                .amount(BigDecimal.valueOf(9900))
                .build();

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.READY);
    }
}

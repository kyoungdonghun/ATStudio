package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.ConsentPolicyProperties;
import com.atstudio.atstudio.dto.user.*;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserConsent;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserJob;
import com.atstudio.atstudio.entity.enums.UserConsentType;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.entity.enums.WhitelistChannelStatus;
import com.atstudio.atstudio.repository.*;
import com.atstudio.atstudio.service.auth.PasswordLoginPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    private static final String ADVERSARIAL_OPERATOR_NOTE =
            "contact=alice@example.test authorization=Bearer eyJhbGciOiJub25lIn0.fake.signature";

    @Mock UserRepository userRepository;
    @Mock UserConsentRepository userConsentRepository;
    @Mock ConsentPolicyProperties consentPolicyProperties;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EmailService emailService;
    @Mock LikeRepository likeRepository;
    @Mock TrackDownloadRepository trackDownloadRepository;
    @Mock LicenseRepository licenseRepository;
    @Mock WhitelistChannelRepository whitelistChannelRepository;
    @Mock PlaylistService playlistService;
    @Mock PasswordLoginPolicy passwordLoginPolicy;
    @Mock BillingAgreementRepository billingAgreementRepository;
    @Mock PaymentOrderRepository paymentOrderRepository;
    @Mock UserSubscriptionRepository userSubscriptionRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock AdminOperationAuditService adminOperationAuditService;
    @Mock AdminOperationRejectionAuditService adminOperationRejectionAuditService;

    @InjectMocks UserService userService;

    @Test
    @DisplayName("withdraw() cancels local billing before publishing the ID-only cleanup event")
    void withdraw_cancelsLocalBillingBeforePublishingCleanup() {
        User user = buildUser(1L, "withdraw@test.com", "withdraw-user", null, UserJob.EDITOR);
        user.updateRefreshToken("stored-refresh-hash");
        BillingAgreement agreement = BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS)
                .providerCustomerKey("customer-key")
                .build();
        ReflectionTestUtils.setField(agreement, "id", 11L);
        agreement.activate("encrypted-key", "fingerprint", "CARD", "****1234", LocalDate.now());
        UserSubscription subscription = UserSubscription.builder()
                .user(user)
                .billingCycle(BillingCycle.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(LocalDate.now())
                .expiresAt(LocalDate.now().plusMonths(1))
                .build();
        WithdrawRequest request = new WithdrawRequest();
        request.setPassword("password123");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123", "encoded")).willReturn(true);
        given(billingAgreementRepository.findByUserIDAndProviderForUpdate(1L, PaymentProviderType.TOSS))
                .willReturn(Optional.of(agreement));
        given(userSubscriptionRepository.findByUserIDForUpdate(1L)).willReturn(Optional.of(subscription));
        given(userRepository.findActiveAdminsForRoleChange()).willReturn(List.of());
        org.mockito.Mockito.doAnswer(invocation -> {
            Object event = invocation.getArgument(0);
            assertThat(agreement.getStatus()).isEqualTo(BillingAgreementStatus.CANCELLED);
            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
            assertThat(user.isDeleted()).isFalse();
            assertThat(event).isEqualTo(new WithdrawalBillingCleanupRequestedEvent(11L));
            return null;
        }).when(eventPublisher).publishEvent(any(Object.class));

        userService.withdraw(1L, request);

        assertThat(user.isDeleted()).isTrue();
        assertThat(user.getRefreshToken()).isNull();
        assertThat(agreement.getBillingKeyCiphertext()).isEqualTo("encrypted-key");
        verify(eventPublisher).publishEvent(new WithdrawalBillingCleanupRequestedEvent(11L));
        verify(likeRepository).deleteAllByUser(user);
        verify(trackDownloadRepository).deleteAllByUser(user);
        verify(licenseRepository).deleteAllByUser(user);
        verify(whitelistChannelRepository).requestExternalRemovalForWithdrawal(eq(user), any());
        verify(whitelistChannelRepository).clearPrimaryByUserID(1L);
        verify(whitelistChannelRepository).deleteAllByUserAndStatusIn(
                eq(user),
                eq(java.util.Set.of(
                        WhitelistChannelStatus.DRAFT,
                        WhitelistChannelStatus.PENDING,
                        WhitelistChannelStatus.REVISION_REQUESTED,
                        WhitelistChannelStatus.REJECTED,
                        WhitelistChannelStatus.CANCELLED)));
        org.mockito.InOrder lockOrder = inOrder(
                billingAgreementRepository,
                userSubscriptionRepository,
                userRepository,
                paymentOrderRepository);
        lockOrder.verify(billingAgreementRepository)
                .findByUserIDAndProviderForUpdate(1L, PaymentProviderType.TOSS);
        lockOrder.verify(userSubscriptionRepository).findByUserIDForUpdate(1L);
        lockOrder.verify(userRepository).findActiveAdminsForRoleChange();
        lockOrder.verify(userRepository).findByIdForUpdate(1L);
        lockOrder.verify(paymentOrderRepository)
                .existsByBillingAgreementAndPurposeInAndStatusIn(
                        eq(agreement),
                        eq(java.util.Set.of(
                                PaymentPurpose.SUBSCRIBE,
                                PaymentPurpose.UPGRADE,
                                PaymentPurpose.RENEWAL)),
                        eq(java.util.Set.of(
                                PaymentOrderStatus.PROCESSING,
                                PaymentOrderStatus.PROVIDER_SUCCEEDED,
                                PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION)));
    }

    @Test
    @DisplayName("withdraw() rejects the last active ADMIN before any account mutation")
    void withdraw_lastActiveAdmin_isRejected() {
        User admin = buildUserWithRole(1L, "admin@test.com", "admin", UserRole.ADMIN);
        admin.updateRefreshToken("stored-refresh-hash");
        BillingAgreement agreement = BillingAgreement.builder()
                .user(admin)
                .provider(PaymentProviderType.TOSS)
                .providerCustomerKey("customer-key")
                .build();
        UserSubscription subscription = UserSubscription.builder()
                .user(admin)
                .billingCycle(BillingCycle.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(LocalDate.now())
                .expiresAt(LocalDate.now().plusMonths(1))
                .build();
        WithdrawRequest request = new WithdrawRequest();
        request.setPassword("password123");

        given(userRepository.findById(1L)).willReturn(Optional.of(admin));
        given(passwordEncoder.matches("password123", "encoded")).willReturn(true);
        given(billingAgreementRepository.findByUserIDAndProviderForUpdate(1L, PaymentProviderType.TOSS))
                .willReturn(Optional.of(agreement));
        given(userSubscriptionRepository.findByUserIDForUpdate(1L)).willReturn(Optional.of(subscription));
        given(userRepository.findActiveAdminsForRoleChange()).willReturn(List.of(admin));

        assertThatThrownBy(() -> userService.withdraw(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.LAST_ADMIN_REQUIRED));

        assertThat(admin.isDeleted()).isFalse();
        assertThat(admin.getRefreshToken()).isEqualTo("stored-refresh-hash");
        assertThat(agreement.getStatus()).isEqualTo(BillingAgreementStatus.READY);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        org.mockito.InOrder lockOrder = inOrder(
                billingAgreementRepository,
                userSubscriptionRepository,
                userRepository);
        lockOrder.verify(billingAgreementRepository)
                .findByUserIDAndProviderForUpdate(1L, PaymentProviderType.TOSS);
        lockOrder.verify(userSubscriptionRepository).findByUserIDForUpdate(1L);
        lockOrder.verify(userRepository).findActiveAdminsForRoleChange();
        verify(userRepository, never()).findByIdForUpdate(1L);
        verify(paymentOrderRepository, never())
                .existsByBillingAgreementAndPurposeInAndStatusIn(any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any());
        verify(likeRepository, never()).deleteAllByUser(any());
        verify(adminOperationRejectionAuditService).recordAdminWithdrawalRejected(
                1L,
                UserRole.ADMIN,
                false,
                BUSINESS_ERROR.LAST_ADMIN_REQUIRED);
        verify(adminOperationAuditService, never()).recordAdminWithdrawalSuccess(anyLong(), any());
    }

    @Test
    @DisplayName("withdraw() 성공 - 다른 활성 ADMIN이 남으면 관리자 탈퇴 허용")
    void withdraw_adminWithAnotherActiveAdmin_succeeds() {
        User withdrawingAdmin = buildUserWithRole(
                1L, "withdrawing@test.com", "withdrawing", UserRole.ADMIN);
        User remainingAdmin = buildUserWithRole(
                2L, "remaining@test.com", "remaining", UserRole.ADMIN);
        withdrawingAdmin.updateRefreshToken("stored-refresh-hash");
        WithdrawRequest request = new WithdrawRequest();
        request.setPassword("password123");

        given(userRepository.findById(1L)).willReturn(Optional.of(withdrawingAdmin));
        given(passwordEncoder.matches("password123", "encoded")).willReturn(true);
        given(userRepository.findActiveAdminsForRoleChange())
                .willReturn(List.of(withdrawingAdmin, remainingAdmin));

        userService.withdraw(1L, request);

        assertThat(withdrawingAdmin.isDeleted()).isTrue();
        assertThat(withdrawingAdmin.getRefreshToken()).isNull();
        assertThat(remainingAdmin.isDeleted()).isFalse();
        assertThat(remainingAdmin.getRole()).isEqualTo(UserRole.ADMIN);
        verify(userRepository, never()).findByIdForUpdate(1L);
        verify(adminOperationAuditService).recordAdminWithdrawalSuccess(1L, UserRole.ADMIN);
        verify(adminOperationRejectionAuditService, never())
                .recordAdminWithdrawalRejected(anyLong(), any(), anyBoolean(), any());
    }

    @Test
    @DisplayName("withdraw() stops before billing changes when the password is invalid")
    void withdraw_invalidPasswordDoesNotChangeBilling() {
        User user = buildUser(1L, "withdraw@test.com", "withdraw-user", null, UserJob.EDITOR);
        user.updateRefreshToken("stored-refresh-hash");
        WithdrawRequest request = new WithdrawRequest();
        request.setPassword("wrong-password");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong-password", "encoded")).willReturn(false);

        assertThatThrownBy(() -> userService.withdraw(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_CREDENTIALS));

        assertThat(user.isDeleted()).isFalse();
        assertThat(user.getRefreshToken()).isEqualTo("stored-refresh-hash");
        verify(billingAgreementRepository, never()).findByUserIDAndProviderForUpdate(anyLong(), any());
        verify(userSubscriptionRepository, never()).findByUserIDForUpdate(anyLong());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("withdraw() rejects an in-flight provider outcome before cancellation or deletion")
    void withdraw_inFlightPaymentFencesDeletion() {
        User user = buildUser(1L, "withdraw@test.com", "withdraw-user", null, UserJob.EDITOR);
        BillingAgreement agreement = BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS)
                .providerCustomerKey("customer-key")
                .build();
        agreement.activate("encrypted-key", "fingerprint", "CARD", "****1234", LocalDate.now());
        UserSubscription subscription = UserSubscription.builder()
                .user(user)
                .billingCycle(BillingCycle.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(LocalDate.now())
                .expiresAt(LocalDate.now().plusMonths(1))
                .build();
        WithdrawRequest request = new WithdrawRequest();
        request.setPassword("password123");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123", "encoded")).willReturn(true);
        given(billingAgreementRepository.findByUserIDAndProviderForUpdate(1L, PaymentProviderType.TOSS))
                .willReturn(Optional.of(agreement));
        given(userSubscriptionRepository.findByUserIDForUpdate(1L)).willReturn(Optional.of(subscription));
        given(paymentOrderRepository.existsByBillingAgreementAndPurposeInAndStatusIn(any(), any(), any()))
                .willReturn(true);

        assertThatThrownBy(() -> userService.withdraw(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE));

        assertThat(user.isDeleted()).isFalse();
        assertThat(agreement.getStatus()).isEqualTo(BillingAgreementStatus.ACTIVE);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        verify(eventPublisher, never()).publishEvent(any());
        verify(likeRepository, never()).deleteAllByUser(any());
    }

    // ── register() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register() 성공 - 저장 후 UserResponse 반환")
    void register_success_returnsUserResponse() {
        RegisterRequest request = buildRegisterRequest("new@test.com", "newNick");
        request.setMarketingAgreed(true);
        User savedUser = buildUser(1L, "new@test.com", "newNick", "010-1111-2222", UserJob.EDITOR);

        stubAllConsentPolicyVersions();
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByNickname("newNick")).thenReturn(Optional.empty());
        when(userRepository.findByPhonePersonal("010-1111-2222")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-pw");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.register(request);

        assertThat(response.email()).isEqualTo("new@test.com");
        assertThat(response.nickname()).isEqualTo("newNick");

        ArgumentCaptor<UserConsent> consentCaptor = ArgumentCaptor.forClass(UserConsent.class);
        verify(userConsentRepository, times(3)).save(consentCaptor.capture());
        assertThat(consentCaptor.getAllValues())
                .extracting(UserConsent::getConsentType)
                .containsExactly(
                        UserConsentType.TERMS_OF_SERVICE,
                        UserConsentType.PRIVACY_COLLECTION_AND_USE,
                        UserConsentType.MARKETING);
        assertThat(consentCaptor.getAllValues())
                .extracting(UserConsent::getPolicyVersion)
                .containsExactly("terms-v1", "privacy-v1", "marketing-v1");
        assertThat(consentCaptor.getAllValues())
                .extracting(UserConsent::getUser)
                .containsOnly(savedUser);
    }

    @Test
    @DisplayName("register() rejects missing required consent before persistence")
    void register_missingRequiredConsent_throwsInvalidArgument() {
        RegisterRequest request = buildRegisterRequest("new@test.com", "newNick");
        request.setTermsAgreed(null);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));

        verifyNoInteractions(userConsentRepository, emailService);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("register() rejects false required privacy consent before persistence")
    void register_falseRequiredPrivacyConsent_throwsInvalidArgument() {
        RegisterRequest request = buildRegisterRequest("new@test.com", "newNick");
        request.setPrivacyAgreed(false);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));

        verifyNoInteractions(userConsentRepository, emailService);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("register() 실패 - 이메일 중복 → EMAIL_ALREADY_REGISTERED 예외")
    void register_duplicateEmail_throwsException() {
        RegisterRequest request = buildRegisterRequest("dup@test.com", "nick");
        when(userRepository.findByEmail("dup@test.com"))
                .thenReturn(Optional.of(buildUser(1L, "dup@test.com", "nick", null, null)));

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.EMAIL_ALREADY_REGISTERED));
    }

    @Test
    @DisplayName("register() 실패 - 닉네임 중복 → NICKNAME_DUPLICATED 예외")
    void register_duplicateNickname_throwsException() {
        RegisterRequest request = buildRegisterRequest("new@test.com", "dupNick");

        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByNickname("dupNick"))
                .thenReturn(Optional.of(buildUser(2L, "other@test.com", "dupNick", null, null)));

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.NICKNAME_DUPLICATED));
    }

    @Test
    @DisplayName("register() 실패 - 전화번호 중복 → PHONE_ALREADY_REGISTERED 예외")
    void register_duplicatePhone_throwsException() {
        RegisterRequest request = buildRegisterRequest("new@test.com", "newNick");
        request.setPhonePersonal("010-1234-5678");

        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByNickname("newNick")).thenReturn(Optional.empty());
        when(userRepository.findByPhonePersonal("010-1234-5678"))
                .thenReturn(Optional.of(buildUser(3L, "other@test.com", "otherNick", "010-1234-5678", null)));

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PHONE_ALREADY_REGISTERED));
    }

    @Test
    @DisplayName("register() 실패 - 연락처 누락 → INVALID_ARGUMENT 예외")
    void register_missingPhone_throwsInvalidArgument() {
        RegisterRequest request = buildRegisterRequest("new@test.com", "newNick");
        request.setPhonePersonal(null);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));
    }

    @Test
    @DisplayName("register() 실패 - 개인 회원 직업 누락 → INVALID_ARGUMENT 예외")
    void register_individualMissingJob_throwsInvalidArgument() {
        RegisterRequest request = buildRegisterRequest("new@test.com", "newNick");
        request.setJob(null);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));
    }

    @Test
    @DisplayName("register() 실패 - 기업 회원 회사명 누락 → INVALID_ARGUMENT 예외")
    void register_businessMissingCompanyName_throwsInvalidArgument() {
        RegisterRequest request = buildRegisterRequest("biz@test.com", "bizNick");
        request.setUserType(UserType.BUSINESS);
        request.setJob(null);
        request.setCompanyName(null);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));
    }

    @Test
    @DisplayName("register() 성공 - 기업 회원은 직업 없이 회사명으로 가입 가능")
    void register_businessWithoutJob_succeeds() {
        RegisterRequest request = buildRegisterRequest("biz@test.com", "bizNick");
        request.setUserType(UserType.BUSINESS);
        request.setJob(null);
        request.setCompanyName("ATStudio Biz");
        User savedUser = User.builder()
                .email("biz@test.com")
                .nickname("bizNick")
                .password("encoded")
                .phonePersonal("010-1111-2222")
                .job(null)
                .companyName("ATStudio Biz")
                .role(UserRole.USER)
                .userType(UserType.BUSINESS)
                .build();
        ReflectionTestUtils.setField(savedUser, "id", 9L);

        stubRequiredConsentPolicyVersions();
        when(userRepository.findByEmail("biz@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByNickname("bizNick")).thenReturn(Optional.empty());
        when(userRepository.findByPhonePersonal("010-1111-2222")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-pw");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.register(request);

        assertThat(response.userType()).isEqualTo("BUSINESS");
        assertThat(response.companyName()).isEqualTo("ATStudio Biz");
        verify(userConsentRepository, times(2)).save(any(UserConsent.class));
    }

    @Test
    @DisplayName("register() 실패 - 이메일 로그인 비활성화 시 PASSWORD_LOGIN_DISABLED 예외")
    void register_disabled_throwsPasswordLoginDisabled() {
        RegisterRequest request = buildRegisterRequest("new@test.com", "newNick");
        org.mockito.Mockito.doThrow(new BusinessException(BUSINESS_ERROR.PASSWORD_LOGIN_DISABLED))
                .when(passwordLoginPolicy).ensureEnabled();

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PASSWORD_LOGIN_DISABLED));
    }

    // ── updatePassword() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("updatePassword() 성공 - 현재 비밀번호 일치 시 새 비밀번호로 변경")
    void updatePassword_success() {
        User user = buildUser(1L, "user@test.com", "nick", null, null);
        user.updateRefreshToken("stored-refresh-hash");
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPw", "encoded")).thenReturn(true);
        when(passwordEncoder.encode("newPw")).thenReturn("encoded-new");

        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setCurrentPassword("currentPw");
        request.setNewPassword("newPw");

        userService.updatePassword(1L, request);

        assertThat(user.getPassword()).isEqualTo("encoded-new");
        assertThat(user.getRefreshToken()).isNull();
    }

    @Test
    @DisplayName("updatePassword() 실패 - 현재 비밀번호 불일치 → INVALID_CREDENTIALS 예외")
    void updatePassword_wrongCurrentPassword_throwsException() {
        User user = buildUser(1L, "user@test.com", "nick", null, null);
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPw", "encoded")).thenReturn(false);

        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setCurrentPassword("wrongPw");
        request.setNewPassword("newPw");

        assertThatThrownBy(() -> userService.updatePassword(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_CREDENTIALS));
    }

    @Test
    @DisplayName("updatePassword() 실패 - 소셜 회원(password null) → INVALID_CREDENTIALS 예외")
    void updatePassword_nullPassword_throwsException() {
        User user = User.builder()
                .email("social@test.com")
                .nickname("socialNick")
                .password(null)
                .role(UserRole.USER)
                .userType(UserType.INDIVIDUAL)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setCurrentPassword("any");
        request.setNewPassword("newPw");

        assertThatThrownBy(() -> userService.updatePassword(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_CREDENTIALS));
    }

    @Test
    @DisplayName("updatePassword() 실패 - 존재하지 않는 사용자 → RESOURCE_NOT_FOUND 예외")
    void updatePassword_userNotFound_throwsException() {
        when(userRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setCurrentPassword("pw");
        request.setNewPassword("newPw");

        assertThatThrownBy(() -> userService.updatePassword(99L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    // ── completeProfile() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("completeProfile() 실패 - 이미 완성된 프로필 → PROFILE_ALREADY_COMPLETE 예외")
    void completeProfile_alreadyComplete_throwsException() {
        User user = buildUser(1L, "user@test.com", "nick", "010-0000-0000", UserJob.EDITOR);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        CompleteProfileRequest request = new CompleteProfileRequest();
        request.setNickname("nick");
        request.setPhonePersonal("010-1234-5678");
        request.setJob(UserJob.ARTIST);
        request.setUserType(UserType.INDIVIDUAL);

        assertThatThrownBy(() -> userService.completeProfile(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PROFILE_ALREADY_COMPLETE));
    }

    @Test
    @DisplayName("completeProfile() 실패 - 전화번호 중복 → PHONE_ALREADY_REGISTERED 예외")
    void completeProfile_duplicatePhone_throwsException() {
        User incompleteUser = buildUser(1L, "user@test.com", "nick", null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(incompleteUser));
        when(userRepository.findByNickname("nick")).thenReturn(Optional.of(incompleteUser));
        when(userRepository.findByPhonePersonal("010-1234-5678"))
                .thenReturn(Optional.of(buildUser(2L, "other@test.com", "otherNick", "010-1234-5678", UserJob.EDITOR)));

        CompleteProfileRequest request = new CompleteProfileRequest();
        request.setNickname("nick");
        request.setPhonePersonal("010-1234-5678");
        request.setJob(UserJob.ARTIST);
        request.setUserType(UserType.INDIVIDUAL);

        assertThatThrownBy(() -> userService.completeProfile(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PHONE_ALREADY_REGISTERED));
    }

    @Test
    @DisplayName("completeProfile() 실패 - 기업 회원 회사명 누락 → INVALID_ARGUMENT 예외")
    void completeProfile_businessWithoutCompanyName_throwsInvalidArgument() {
        User incompleteUser = buildUser(1L, "user@test.com", "nick", null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(incompleteUser));

        CompleteProfileRequest request = new CompleteProfileRequest();
        request.setNickname("biznick");
        request.setPhonePersonal("010-1234-5678");
        request.setJob(null);
        request.setUserType(UserType.BUSINESS);
        request.setCompanyName(null);

        assertThatThrownBy(() -> userService.completeProfile(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));
    }

    @Test
    @DisplayName("completeProfile() 성공 - 기업 회원은 회사명으로 프로필 완성 가능")
    void completeProfile_businessWithCompanyName_succeeds() {
        User incompleteUser = buildUser(1L, "user@test.com", "nick", null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(incompleteUser));
        when(userRepository.findByNickname("biznick")).thenReturn(Optional.empty());
        when(userRepository.findByPhonePersonal("010-1234-5678")).thenReturn(Optional.empty());

        CompleteProfileRequest request = new CompleteProfileRequest();
        request.setNickname("biznick");
        request.setPhonePersonal("010-1234-5678");
        request.setJob(null);
        request.setUserType(UserType.BUSINESS);
        request.setCompanyName("ATStudio Biz");

        UserResponse response = userService.completeProfile(1L, request);

        assertThat(response.userType()).isEqualTo("BUSINESS");
        assertThat(response.companyName()).isEqualTo("ATStudio Biz");
    }

    // ── updateMyProfile() ────────────────────────────────────────────────────

    @Test
    @DisplayName("updateMyProfile() 실패 - 전화번호 중복 → PHONE_ALREADY_REGISTERED 예외")
    void updateMyProfile_duplicatePhone_throwsException() {
        User currentUser = buildUser(1L, "user@test.com", "nick", "010-1111-2222", UserJob.EDITOR);
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByPhonePersonal("010-1234-5678"))
                .thenReturn(Optional.of(buildUser(2L, "other@test.com", "otherNick", "010-1234-5678", UserJob.ARTIST)));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("nick");
        request.setPhonePersonal("010-1234-5678");
        request.setJob(UserJob.EDITOR);

        assertThatThrownBy(() -> userService.updateMyProfile(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PHONE_ALREADY_REGISTERED));
    }

    @Test
    @DisplayName("updateMyProfile() 실패 - 기업 회원이 회사명을 빈값으로 덮어쓰면 INVALID_ARGUMENT 예외")
    void updateMyProfile_businessBlankCompanyName_throwsInvalidArgument() {
        User businessUser = User.builder()
                .email("biz@test.com")
                .nickname("bizNick")
                .password("encoded")
                .phonePersonal("010-1111-2222")
                .job(null)
                .companyName("ATStudio Biz")
                .role(UserRole.USER)
                .userType(UserType.BUSINESS)
                .build();
        ReflectionTestUtils.setField(businessUser, "id", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(businessUser));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("bizNick");
        request.setPhonePersonal("010-1111-2222");
        request.setCompanyName("   ");

        assertThatThrownBy(() -> userService.updateMyProfile(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));
    }

    @Test
    @DisplayName("updateMyProfile() 실패 - 개인 회원의 최종 직업이 없으면 INVALID_ARGUMENT 예외")
    void updateMyProfile_individualWithoutEffectiveJob_throwsInvalidArgument() {
        User incompleteIndividual = buildUser(1L, "user@test.com", "nick", "010-1111-2222", null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(incompleteIndividual));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("nick2");
        request.setPhonePersonal("010-1111-2222");

        assertThatThrownBy(() -> userService.updateMyProfile(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));
    }

    @Test
    @DisplayName("updateMyProfile() 성공 - 회사명 미전달 시 기존 회사명 유지")
    void updateMyProfile_omittedCompanyName_preservesExistingValue() {
        User businessUser = User.builder()
                .email("biz@test.com")
                .nickname("bizNick")
                .password("encoded")
                .phonePersonal("010-1111-2222")
                .job(null)
                .companyName("ATStudio Biz")
                .role(UserRole.USER)
                .userType(UserType.BUSINESS)
                .build();
        ReflectionTestUtils.setField(businessUser, "id", 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(businessUser));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("bizNick2");
        request.setPhonePersonal("010-1111-2222");
        request.setPhoneCompany("02-1234-5678");

        UserResponse response = userService.updateMyProfile(1L, request);

        assertThat(response.nickname()).isEqualTo("bizNick2");
        assertThat(response.companyName()).isEqualTo("ATStudio Biz");
    }

    // ── getUsers() (Admin) ────────────────────────────────────────────────────

    @Test
    @DisplayName("getUsers() 성공 - 검색어/userType 필터로 유저 목록 반환")
    void getUsers_success() {
        Page<User> page = new PageImpl<>(List.of());
        given(userRepository.searchUsers(any(), any(), any())).willReturn(page);

        ResponseDTO<UserListItemResponse> result = userService.getUsers("nick", UserType.INDIVIDUAL, 1, 20);

        assertThat(result).isNotNull();
        assertThat(result.getDataList()).isEmpty();
    }

    @Test
    @DisplayName("getUsers() - searchUsers JPQL은 isDeleted=false 조건을 포함하므로 탈퇴 계정 미노출 (C-2 PII 보호)")
    void getUsers_excludesDeletedUsers() {
        // Repository의 searchUsers JPQL에 AND u.isDeleted = false 조건이 포함됨.
        // 단위 테스트에서는 repository mock이므로, 반환된 목록에 deleted user가 없음을 검증.
        User activeUser = buildUser(1L, "active@test.com", "activeNick", null, null);
        Page<User> page = new PageImpl<>(List.of(activeUser));
        given(userRepository.searchUsers(any(), any(), any())).willReturn(page);

        ResponseDTO<UserListItemResponse> result = userService.getUsers(null, null, 1, 20);

        assertThat(result.getDataList()).hasSize(1);
        assertThat(result.getDataList().get(0).email()).isEqualTo("active@test.com");
    }

    // ── getUser() (Admin) ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getUser() 성공 - ID로 유저 상세 조회")
    void getUser_success() {
        User user = buildUser(1L, "user@test.com", "nick", null, null);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        UserDetailResponse result = userService.getUser(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("getUser() 실패 - 존재하지 않는 ID → RESOURCE_NOT_FOUND 예외")
    void getUser_notFound() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(99L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    // ── updateUserByAdmin() ───────────────────────────────────────────────────

    @Test
    @DisplayName("updateUserByAdmin() 성공 - role/isVerified 수정")
    void updateUserByAdmin_success() {
        User actor = buildUserWithRole(1L, "admin@test.com", "admin", UserRole.ADMIN);
        User user = buildUser(2L, "user@test.com", "nick", null, null);
        given(userRepository.findActiveAdminsForRoleChange()).willReturn(List.of(actor));
        given(userRepository.findByIdForUpdate(2L)).willReturn(Optional.of(user));

        UserAdminUpdateRequest request = new UserAdminUpdateRequest();
        request.setRole(UserRole.ADMIN);
        request.setIsVerified(true);
        request.setReason("  Role promotion approved  ");

        UserDetailResponse result = userService.updateUserByAdmin(1L, 2L, request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.role()).isEqualTo("ADMIN");
        verify(adminOperationAuditService).recordRoleChangeSuccess(
                1L,
                2L,
                UserRole.USER,
                UserRole.ADMIN,
                false,
                "Role promotion approved");
    }

    @Test
    @DisplayName("updateUserByAdmin() 실패 - ADMIN 자기 강등은 명시적 오류로 거절")
    void updateUserByAdmin_selfDemotion_isRejected() {
        User actor = buildUserWithRole(1L, "admin@test.com", "admin", UserRole.ADMIN);
        given(userRepository.findActiveAdminsForRoleChange()).willReturn(List.of(actor));

        UserAdminUpdateRequest request = adminUpdate(UserRole.USER);
        request.setReason(ADVERSARIAL_OPERATOR_NOTE);

        assertThatThrownBy(() -> userService.updateUserByAdmin(1L, 1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.SELF_ADMIN_DEMOTION_FORBIDDEN));
        assertThat(actor.getRole()).isEqualTo(UserRole.ADMIN);
        verify(adminOperationRejectionAuditService).recordRoleChangeRejected(
                1L,
                1L,
                UserRole.ADMIN,
                false,
                BUSINESS_ERROR.SELF_ADMIN_DEMOTION_FORBIDDEN,
                null);
    }

    @Test
    @DisplayName("updateUserByAdmin() 실패 - 마지막 활성 ADMIN 강등은 거절")
    void updateUserByAdmin_lastAdmin_isRejected() {
        User actor = buildUserWithRole(1L, "admin@test.com", "admin", UserRole.ADMIN);
        User target = buildUserWithRole(2L, "target@test.com", "target", UserRole.ADMIN);
        given(userRepository.findActiveAdminsForRoleChange()).willReturn(List.of(target));

        UserAdminUpdateRequest request = adminUpdate(UserRole.USER);

        assertThatThrownBy(() -> userService.updateUserByAdmin(1L, 2L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.LAST_ADMIN_REQUIRED));
        assertThat(target.getRole()).isEqualTo(UserRole.ADMIN);
        verify(adminOperationRejectionAuditService).recordRoleChangeRejected(
                1L,
                2L,
                UserRole.ADMIN,
                false,
                BUSINESS_ERROR.LAST_ADMIN_REQUIRED,
                null);
    }

    @Test
    @DisplayName("updateUserByAdmin() 성공 - 다른 ADMIN 강등 시 refresh token 제거")
    void updateUserByAdmin_validDemotion_clearsRefreshToken() {
        User actor = buildUserWithRole(1L, "actor@test.com", "actor", UserRole.ADMIN);
        User target = buildUserWithRole(2L, "target@test.com", "target", UserRole.ADMIN);
        target.updateRefreshToken("stored-refresh-hash");
        given(userRepository.findActiveAdminsForRoleChange()).willReturn(List.of(actor, target));

        UserDetailResponse result = userService.updateUserByAdmin(1L, 2L, adminUpdate(UserRole.USER));

        assertThat(result.role()).isEqualTo("USER");
        assertThat(target.getRole()).isEqualTo(UserRole.USER);
        assertThat(target.getRefreshToken()).isNull();
        verify(userRepository, never()).findByIdForUpdate(2L);
        verify(adminOperationAuditService).recordRoleChangeSuccess(
                1L,
                2L,
                UserRole.ADMIN,
                UserRole.USER,
                false,
                "Approved role change");
    }

    @Test
    @DisplayName("updateUserByAdmin() 실패 - 적용 직전 요청자의 현재 DB 역할 재검사")
    void updateUserByAdmin_actorRoleRecheck_rejectsStaleAdmin() {
        User actor = buildUserWithRole(1L, "actor@test.com", "actor", UserRole.ADMIN);
        User target = buildUserWithRole(2L, "target@test.com", "target", UserRole.ADMIN);
        given(userRepository.findActiveAdminsForRoleChange()).willAnswer(invocation -> {
            actor.updateByAdmin(UserRole.USER, null);
            return List.of(actor, target);
        });

        assertThatThrownBy(() -> userService.updateUserByAdmin(1L, 2L, adminUpdate(UserRole.USER)))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.ADMIN_ROLE_REQUIRED));
        assertThat(target.getRole()).isEqualTo(UserRole.ADMIN);
        verify(adminOperationRejectionAuditService).recordRoleChangeRejected(
                1L,
                2L,
                UserRole.ADMIN,
                false,
                BUSINESS_ERROR.ADMIN_ROLE_REQUIRED,
                null);
    }

    @Test
    @DisplayName("updateUserByAdmin() 실패 - 삭제된 사용자는 승격하거나 수정할 수 없음")
    void updateUserByAdmin_deletedTarget_isRejected() {
        User actor = buildUserWithRole(1L, "actor@test.com", "actor", UserRole.ADMIN);
        User deletedTarget = buildUserWithRole(
                2L, "deleted@test.com", "deleted", UserRole.ADMIN);
        deletedTarget.withdraw();
        given(userRepository.findActiveAdminsForRoleChange()).willReturn(List.of(actor));
        given(userRepository.findByIdForUpdate(2L)).willReturn(Optional.of(deletedTarget));

        UserAdminUpdateRequest request = adminUpdate(UserRole.USER);
        request.setIsVerified(true);

        assertThatThrownBy(() -> userService.updateUserByAdmin(1L, 2L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        assertThat(deletedTarget.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(deletedTarget.isVerified()).isFalse();
        assertThat(deletedTarget.isDeleted()).isTrue();
        verify(adminOperationAuditService, never()).recordRoleChangeSuccess(
                anyLong(), anyLong(), any(), any(), anyBoolean(), any());
        verify(adminOperationRejectionAuditService, never()).recordRoleChangeRejected(
                anyLong(), anyLong(), any(), anyBoolean(), any(), any());
    }

    @Test
    @DisplayName("updateUserByAdmin() 실패 - 실제 역할 변경에는 운영 사유 필수")
    void updateUserByAdmin_roleChangeWithoutReason_isRejected() {
        User actor = buildUserWithRole(1L, "actor@test.com", "actor", UserRole.ADMIN);
        User target = buildUserWithRole(2L, "target@test.com", "target", UserRole.USER);
        given(userRepository.findActiveAdminsForRoleChange()).willReturn(List.of(actor));
        given(userRepository.findByIdForUpdate(2L)).willReturn(Optional.of(target));

        UserAdminUpdateRequest request = new UserAdminUpdateRequest();
        request.setRole(UserRole.ADMIN);

        assertThatThrownBy(() -> userService.updateUserByAdmin(1L, 2L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.ADMIN_OPERATION_REASON_REQUIRED));
        assertThat(target.getRole()).isEqualTo(UserRole.USER);
        verify(adminOperationRejectionAuditService).recordRoleChangeRejected(
                1L,
                2L,
                UserRole.USER,
                false,
                BUSINESS_ERROR.ADMIN_OPERATION_REASON_REQUIRED,
                null);
        verify(adminOperationAuditService, never()).recordRoleChangeSuccess(
                anyLong(), anyLong(), any(), any(), anyBoolean(), any());
    }

    @Test
    @DisplayName("updateUserByAdmin rejects a blank reason for an actual role change")
    void updateUserByAdmin_roleChangeWithBlankReason_isRejected() {
        User actor = buildUserWithRole(1L, "actor@test.com", "actor", UserRole.ADMIN);
        User target = buildUserWithRole(2L, "target@test.com", "target", UserRole.USER);
        given(userRepository.findActiveAdminsForRoleChange()).willReturn(List.of(actor));
        given(userRepository.findByIdForUpdate(2L)).willReturn(Optional.of(target));
        UserAdminUpdateRequest request = new UserAdminUpdateRequest();
        request.setRole(UserRole.ADMIN);
        request.setReason(" \t ");

        assertThatThrownBy(() -> userService.updateUserByAdmin(1L, 2L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.ADMIN_OPERATION_REASON_REQUIRED));
        verify(adminOperationRejectionAuditService).recordRoleChangeRejected(
                1L,
                2L,
                UserRole.USER,
                false,
                BUSINESS_ERROR.ADMIN_OPERATION_REASON_REQUIRED,
                null);
    }

    @Test
    @DisplayName("updateUserByAdmin enforces the reason length in the service layer")
    void updateUserByAdmin_roleChangeWithOverlongReason_isRejected() {
        User actor = buildUserWithRole(1L, "actor@test.com", "actor", UserRole.ADMIN);
        User target = buildUserWithRole(2L, "target@test.com", "target", UserRole.USER);
        given(userRepository.findActiveAdminsForRoleChange()).willReturn(List.of(actor));
        given(userRepository.findByIdForUpdate(2L)).willReturn(Optional.of(target));
        UserAdminUpdateRequest request = new UserAdminUpdateRequest();
        request.setRole(UserRole.ADMIN);
        request.setReason("x".repeat(501));

        assertThatThrownBy(() -> userService.updateUserByAdmin(1L, 2L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));
        verify(adminOperationAuditService, never()).recordRoleChangeSuccess(
                anyLong(), anyLong(), any(), any(), anyBoolean(), any());
        verify(adminOperationRejectionAuditService, never()).recordRoleChangeRejected(
                anyLong(), anyLong(), any(), anyBoolean(), any(), any());
    }

    @Test
    @DisplayName("updateUserByAdmin() 성공 - verification-only 요청은 사유와 역할 감사를 요구하지 않음")
    void updateUserByAdmin_verificationOnly_doesNotRequireReasonOrRoleAudit() {
        User actor = buildUserWithRole(1L, "actor@test.com", "actor", UserRole.ADMIN);
        User target = buildUserWithRole(2L, "target@test.com", "target", UserRole.USER);
        given(userRepository.findActiveAdminsForRoleChange()).willReturn(List.of(actor));
        given(userRepository.findByIdForUpdate(2L)).willReturn(Optional.of(target));

        UserAdminUpdateRequest request = new UserAdminUpdateRequest();
        request.setIsVerified(true);

        UserDetailResponse result = userService.updateUserByAdmin(1L, 2L, request);

        assertThat(result.role()).isEqualTo("USER");
        assertThat(target.isVerified()).isTrue();
        verify(adminOperationAuditService, never()).recordRoleChangeSuccess(
                anyLong(), anyLong(), any(), any(), anyBoolean(), any());
        verify(adminOperationRejectionAuditService, never()).recordRoleChangeRejected(
                anyLong(), anyLong(), any(), anyBoolean(), any(), any());
    }

    @Test
    @DisplayName("updateUserByAdmin() 성공 - 동일 역할 요청은 사유와 역할 감사를 요구하지 않음")
    void updateUserByAdmin_unchangedRole_doesNotRequireReasonOrRoleAudit() {
        User actor = buildUserWithRole(1L, "actor@test.com", "actor", UserRole.ADMIN);
        User target = buildUserWithRole(2L, "target@test.com", "target", UserRole.USER);
        given(userRepository.findActiveAdminsForRoleChange()).willReturn(List.of(actor));
        given(userRepository.findByIdForUpdate(2L)).willReturn(Optional.of(target));

        UserAdminUpdateRequest request = new UserAdminUpdateRequest();
        request.setRole(UserRole.USER);
        request.setIsVerified(true);

        UserDetailResponse result = userService.updateUserByAdmin(1L, 2L, request);

        assertThat(result.role()).isEqualTo("USER");
        assertThat(target.isVerified()).isTrue();
        verify(adminOperationAuditService, never()).recordRoleChangeSuccess(
                anyLong(), anyLong(), any(), any(), anyBoolean(), any());
        verify(adminOperationRejectionAuditService, never()).recordRoleChangeRejected(
                anyLong(), anyLong(), any(), anyBoolean(), any(), any());
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private RegisterRequest buildRegisterRequest(String email, String nickname) {
        RegisterRequest req = new RegisterRequest();
        req.setEmail(email);
        req.setNickname(nickname);
        req.setPassword("password123");
        req.setPhonePersonal("010-1111-2222");
        req.setJob(UserJob.EDITOR);
        req.setUserType(UserType.INDIVIDUAL);
        req.setTermsAgreed(true);
        req.setPrivacyAgreed(true);
        return req;
    }

    private void stubAllConsentPolicyVersions() {
        stubRequiredConsentPolicyVersions();
        when(consentPolicyProperties.versionFor(UserConsentType.MARKETING))
                .thenReturn("marketing-v1");
    }

    private void stubRequiredConsentPolicyVersions() {
        when(consentPolicyProperties.versionFor(UserConsentType.TERMS_OF_SERVICE))
                .thenReturn("terms-v1");
        when(consentPolicyProperties.versionFor(UserConsentType.PRIVACY_COLLECTION_AND_USE))
                .thenReturn("privacy-v1");
    }

    private User buildUser(Long id, String email, String nickname, String phonePersonal, UserJob job) {
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .password("encoded")
                .phonePersonal(phonePersonal)
                .job(job)
                .role(UserRole.USER)
                .userType(UserType.INDIVIDUAL)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private User buildUserWithRole(Long id, String email, String nickname, UserRole role) {
        User user = buildUser(id, email, nickname, null, UserJob.EDITOR);
        user.updateByAdmin(role, null);
        return user;
    }

    private UserAdminUpdateRequest adminUpdate(UserRole role) {
        UserAdminUpdateRequest request = new UserAdminUpdateRequest();
        request.setRole(role);
        request.setReason("Approved role change");
        return request;
    }
}

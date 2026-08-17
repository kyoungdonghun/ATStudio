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
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.entity.enums.UserConsentType;
import com.atstudio.atstudio.entity.enums.WhitelistChannelStatus;
import com.atstudio.atstudio.repository.*;
import com.atstudio.atstudio.service.auth.PasswordLoginPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private static final int MAX_ADMIN_OPERATION_REASON_LENGTH = 500;

    private static final Set<WhitelistChannelStatus> WITHDRAWAL_DELETABLE_WHITELIST_STATUSES = Set.of(
            WhitelistChannelStatus.DRAFT,
            WhitelistChannelStatus.PENDING,
            WhitelistChannelStatus.REVISION_REQUESTED,
            WhitelistChannelStatus.REJECTED,
            WhitelistChannelStatus.CANCELLED);
    private static final Set<PaymentPurpose> PROVIDER_CHARGE_PURPOSES = Set.of(
            PaymentPurpose.SUBSCRIBE,
            PaymentPurpose.UPGRADE,
            PaymentPurpose.RENEWAL);
    private static final Set<PaymentOrderStatus> PROVIDER_OUTCOME_PENDING_STATUSES = Set.of(
            PaymentOrderStatus.PROCESSING,
            PaymentOrderStatus.PROVIDER_SUCCEEDED,
            PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION);

    private final UserRepository userRepository;
    private final UserConsentRepository userConsentRepository;
    private final ConsentPolicyProperties consentPolicyProperties;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final LikeRepository likeRepository;
    private final TrackDownloadRepository trackDownloadRepository;
    private final LicenseRepository licenseRepository;
    private final WhitelistChannelRepository whitelistChannelRepository;
    private final PasswordLoginPolicy passwordLoginPolicy;
    private final BillingAgreementRepository billingAgreementRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AdminOperationAuditService adminOperationAuditService;
    private final AdminOperationRejectionAuditService adminOperationRejectionAuditService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        passwordLoginPolicy.ensureEnabled();
        validateRequiredConsents(request);
        validateRegisterProfileFields(
                request.getPhonePersonal(),
                request.getJob(),
                request.getUserType(),
                request.getCompanyName()
        );

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException(BUSINESS_ERROR.EMAIL_ALREADY_REGISTERED);
        }
        ensureNicknameAvailable(request.getNickname(), null);
        ensurePhoneAvailable(request.getPhonePersonal(), null);

        User user = User.builder()
                .nickname(request.getNickname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phonePersonal(request.getPhonePersonal())
                .phoneCompany(request.getPhoneCompany())
                .job(request.getJob())
                .companyName(request.getCompanyName())
                .userType(request.getUserType())
                .role(UserRole.USER)
                .build();

        user = userRepository.save(user);
        recordAcceptedConsents(user, request);
        emailService.sendVerificationEmail(user);
        return toResponse(user);
    }

    public UserResponse getMyProfile(Long userID) {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateMyProfile(Long userID, UpdateProfileRequest request) {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            ensureNicknameAvailable(request.getNickname(), userID);
        }
        if (request.getPhonePersonal() != null && !request.getPhonePersonal().equals(user.getPhonePersonal())) {
            ensurePhoneAvailable(request.getPhonePersonal(), userID);
        }

        String companyName = request.getCompanyName() != null
                ? request.getCompanyName()
                : user.getCompanyName();
        String phonePersonal = request.getPhonePersonal() != null
                ? request.getPhonePersonal()
                : user.getPhonePersonal();
        var job = request.getJob() != null
                ? request.getJob()
                : user.getJob();

        validateUpdateProfileFields(phonePersonal, job, user.getUserType(), companyName);

        user.updateProfile(request.getNickname(), request.getPhonePersonal(),
                request.getPhoneCompany(), request.getJob(), companyName);
        return toResponse(user);
    }

    @Transactional
    public void withdraw(Long userID, WithdrawRequest request) {
        User authenticationSnapshot = userRepository.findById(userID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        validateWithdrawalPassword(authenticationSnapshot, request);

        BillingAgreement billingAgreement = billingAgreementRepository
                .findByUserIDAndProviderForUpdate(userID, PaymentProviderType.TOSS)
                .orElse(null);
        UserSubscription userSubscription = userSubscriptionRepository
                .findByUserIDForUpdate(userID)
                .orElse(null);
        List<User> activeAdmins = userRepository.findActiveAdminsForRoleChange();
        User user = findLockedUser(activeAdmins, userID);
        boolean withdrawingActiveAdmin = user != null;
        if (user == null) {
            user = userRepository.findByIdForUpdate(userID)
                    .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        }

        validateWithdrawalPassword(user, request);
        if (user.isDeleted()) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND);
        }
        if (withdrawingActiveAdmin && activeAdmins.size() <= 1) {
            rejectLastAdminWithdrawal(user);
        }
        if (billingAgreement != null && paymentOrderRepository
                .existsByBillingAgreementAndPurposeInAndStatusIn(
                        billingAgreement,
                        PROVIDER_CHARGE_PURPOSES,
                        PROVIDER_OUTCOME_PENDING_STATUSES)) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
        if (billingAgreement != null && !isTerminal(billingAgreement.getStatus())) {
            billingAgreement.cancel();
        }

        if (userSubscription != null && userSubscription.getStatus() == SubscriptionStatus.ACTIVE) {
            userSubscription.cancel();
        }

        if (billingAgreement != null && hasIssuedKey(billingAgreement)) {
            eventPublisher.publishEvent(new WithdrawalBillingCleanupRequestedEvent(billingAgreement.getId()));
        }

        // 관련 레코드 정리 (고아 레코드 방지)
        likeRepository.deleteAllByUser(user);
        trackDownloadRepository.deleteAllByUser(user);
        licenseRepository.deleteAllByUser(user);
        whitelistChannelRepository.requestExternalRemovalForWithdrawal(user, LocalDateTime.now());
        whitelistChannelRepository.clearPrimaryByUserID(user.getId());
        whitelistChannelRepository.deleteAllByUserAndStatusIn(
                user,
                WITHDRAWAL_DELETABLE_WHITELIST_STATUSES);

        user.withdraw();
        if (withdrawingActiveAdmin) {
            adminOperationAuditService.recordAdminWithdrawalSuccess(userID, UserRole.ADMIN);
        }
    }

    private void validateWithdrawalPassword(User user, WithdrawRequest request) {
        if (user.getPassword() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_CREDENTIALS);
        }
    }

    private boolean isTerminal(BillingAgreementStatus status) {
        return status == BillingAgreementStatus.CANCELLED || status == BillingAgreementStatus.EXPIRED;
    }

    private boolean hasIssuedKey(BillingAgreement billingAgreement) {
        String ciphertext = billingAgreement.getBillingKeyCiphertext();
        return ciphertext != null && !ciphertext.isBlank();
    }

    @Transactional
    public UserResponse completeProfile(Long userID, CompleteProfileRequest request) {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        if (user.isProfileComplete()) {
            throw new BusinessException(BUSINESS_ERROR.PROFILE_ALREADY_COMPLETE);
        }

        validateCompleteProfileFields(
                request.getPhonePersonal(),
                request.getJob(),
                request.getUserType(),
                request.getCompanyName()
        );
        ensureNicknameAvailable(request.getNickname(), userID);
        ensurePhoneAvailable(request.getPhonePersonal(), userID);

        user.completeProfile(request.getNickname(), request.getPhonePersonal(),
                request.getPhoneCompany(), request.getJob(), request.getUserType(), request.getCompanyName());
        return toResponse(user);
    }

    @Transactional
    public void updatePassword(Long userID, UpdatePasswordRequest request) {
        User user = userRepository.findByIdForUpdate(userID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        if (user.getPassword() == null
                || !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_CREDENTIALS);
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        user.clearRefreshToken();
    }

    public boolean isEmailAvailable(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    public boolean isPhoneAvailable(String phone) {
        return userRepository.findByPhonePersonal(phone).isEmpty();
    }

    public boolean isNicknameAvailable(String nickname) {
        return userRepository.findByNickname(nickname).isEmpty();
    }

    public ResponseDTO<UserListItemResponse> getUsers(String keyword, UserType userType, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size));
        Page<User> userPage = userRepository.searchUsers(keyword, userType, pageable);

        return ResponseDTO.<UserListItemResponse>withAll()
                .dataList(userPage.getContent().stream().map(UserListItemResponse::from).toList())
                .pageInfo(PageInfo.of(page, size, (int) userPage.getTotalElements(), 10))
                .build();
    }

    public UserDetailResponse getUser(Long userID) {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        return UserDetailResponse.from(user);
    }

    @Transactional
    public UserDetailResponse updateUserByAdmin(
            Long actorUserID,
            Long targetUserID,
            UserAdminUpdateRequest request) {
        List<User> activeAdmins = userRepository.findActiveAdminsForRoleChange();
        User actor = findLockedUser(activeAdmins, actorUserID);
        User target = findLockedUser(activeAdmins, targetUserID);
        if (target == null) {
            target = userRepository.findByIdForUpdate(targetUserID)
                    .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        }
        if (target.isDeleted()) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND);
        }

        UserRole previousRole = target.getRole();
        UserRole requestedRole = request.getRole();
        boolean roleChanged = requestedRole != null && requestedRole != previousRole;
        String reasonNote = normalizeOptionalAdminOperationReason(request.getReason());
        boolean demotingActiveAdmin = previousRole == UserRole.ADMIN
                && requestedRole == UserRole.USER
                && !target.isDeleted();

        if (demotingActiveAdmin && actorUserID.equals(targetUserID)) {
            rejectRoleChange(
                    BUSINESS_ERROR.SELF_ADMIN_DEMOTION_FORBIDDEN,
                    actorUserID,
                    targetUserID,
                    target);
        }
        if (demotingActiveAdmin && activeAdmins.size() <= 1) {
            rejectRoleChange(
                    BUSINESS_ERROR.LAST_ADMIN_REQUIRED,
                    actorUserID,
                    targetUserID,
                    target);
        }

        // The actor row is part of the deterministically locked admin set and is checked again
        // immediately before applying any administrator-controlled mutation.
        if (!isActiveAdmin(actor)) {
            if (roleChanged) {
                rejectRoleChange(
                        BUSINESS_ERROR.ADMIN_ROLE_REQUIRED,
                        actorUserID,
                        targetUserID,
                        target);
            }
            throw new BusinessException(BUSINESS_ERROR.ADMIN_ROLE_REQUIRED);
        }
        if (roleChanged && reasonNote == null) {
            rejectRoleChange(
                    BUSINESS_ERROR.ADMIN_OPERATION_REASON_REQUIRED,
                    actorUserID,
                    targetUserID,
                    target);
        }

        target.updateByAdmin(requestedRole, request.getIsVerified());
        if (demotingActiveAdmin) {
            target.clearRefreshToken();
        }
        if (roleChanged) {
            adminOperationAuditService.recordRoleChangeSuccess(
                    actorUserID,
                    targetUserID,
                    previousRole,
                    target.getRole(),
                    target.isDeleted(),
                    reasonNote);
        }
        return UserDetailResponse.from(target);
    }

    private User findLockedUser(List<User> users, Long userID) {
        return users.stream()
                .filter(user -> user.getId().equals(userID))
                .findFirst()
                .orElse(null);
    }

    private boolean isActiveAdmin(User user) {
        return user != null && !user.isDeleted() && user.getRole() == UserRole.ADMIN;
    }

    private void rejectRoleChange(
            BUSINESS_ERROR error,
            Long actorUserID,
            Long targetUserID,
            User target) {
        adminOperationRejectionAuditService.recordRoleChangeRejected(
                actorUserID,
                targetUserID,
                target.getRole(),
                target.isDeleted(),
                error,
                null);
        throw new BusinessException(error);
    }

    private void rejectLastAdminWithdrawal(User user) {
        adminOperationRejectionAuditService.recordAdminWithdrawalRejected(
                user.getId(),
                user.getRole(),
                user.isDeleted(),
                BUSINESS_ERROR.LAST_ADMIN_REQUIRED);
        throw new BusinessException(BUSINESS_ERROR.LAST_ADMIN_REQUIRED);
    }

    private String normalizeOptionalAdminOperationReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }
        String normalized = reason.trim();
        if (normalized.length() > MAX_ADMIN_OPERATION_REASON_LENGTH) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
        return normalized;
    }

    private void ensureNicknameAvailable(String nickname, Long currentUserId) {
        userRepository.findByNickname(nickname)
                .filter(existing -> currentUserId == null || !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new BusinessException(BUSINESS_ERROR.NICKNAME_DUPLICATED);
                });
    }

    private void ensurePhoneAvailable(String phonePersonal, Long currentUserId) {
        if (phonePersonal == null || phonePersonal.isBlank()) {
            return;
        }

        userRepository.findByPhonePersonal(phonePersonal)
                .filter(existing -> currentUserId == null || !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new BusinessException(BUSINESS_ERROR.PHONE_ALREADY_REGISTERED);
                });
    }

    private void validateRegisterProfileFields(String phonePersonal, com.atstudio.atstudio.entity.enums.UserJob job,
                                               UserType userType, String companyName) {
        if (phonePersonal == null || phonePersonal.isBlank()) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }

        if (userType == UserType.INDIVIDUAL && job == null) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }

        if (userType == UserType.BUSINESS && (companyName == null || companyName.isBlank())) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
    }

    private void validateRequiredConsents(RegisterRequest request) {
        if (!Boolean.TRUE.equals(request.getTermsAgreed())
                || !Boolean.TRUE.equals(request.getPrivacyAgreed())) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
    }

    private void recordAcceptedConsents(User user, RegisterRequest request) {
        recordConsent(user, UserConsentType.TERMS_OF_SERVICE);
        recordConsent(user, UserConsentType.PRIVACY_COLLECTION_AND_USE);

        if (Boolean.TRUE.equals(request.getMarketingAgreed())) {
            recordConsent(user, UserConsentType.MARKETING);
        }
    }

    private void recordConsent(User user, UserConsentType consentType) {
        userConsentRepository.save(UserConsent.agree(
                user,
                consentType,
                consentPolicyProperties.versionFor(consentType)));
    }

    private void validateCompleteProfileFields(String phonePersonal,
                                               com.atstudio.atstudio.entity.enums.UserJob job,
                                               UserType userType,
                                               String companyName) {
        if (phonePersonal == null || phonePersonal.isBlank()) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }

        if (userType == UserType.INDIVIDUAL && job == null) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }

        if (userType == UserType.BUSINESS && (companyName == null || companyName.isBlank())) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
    }

    private void validateUpdateProfileFields(String phonePersonal, com.atstudio.atstudio.entity.enums.UserJob job,
                                             UserType userType, String companyName) {
        if (phonePersonal == null || phonePersonal.isBlank()) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }

        if (userType == UserType.INDIVIDUAL && job == null) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }

        if (userType == UserType.BUSINESS && (companyName == null || companyName.isBlank())) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getPhonePersonal(),
                user.getPhoneCompany(),
                user.getJob() != null ? user.getJob().name() : null,
                user.getCompanyName(),
                user.getUserType().name(),
                user.getRole().name(),
                user.isVerified(),
                user.getCreatedAt());
    }
}

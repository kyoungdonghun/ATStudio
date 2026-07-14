package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.user.*;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.*;
import com.atstudio.atstudio.service.auth.PasswordLoginPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final LikeRepository likeRepository;
    private final DownloadQueueRepository downloadQueueRepository;
    private final PlayHistoryRepository playHistoryRepository;
    private final TrackDownloadRepository trackDownloadRepository;
    private final LicenseRepository licenseRepository;
    private final WhitelistChannelRepository whitelistChannelRepository;
    private final PasswordLoginPolicy passwordLoginPolicy;
    private final BillingAgreementRepository billingAgreementRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        passwordLoginPolicy.ensureEnabled();
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
        User user = userRepository.findByIdForUpdate(userID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        if (user.getPassword() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_CREDENTIALS);
        }

        BillingAgreement billingAgreement = billingAgreementRepository
                .findByUserAndProvider(user, PaymentProviderType.TOSS_BILLING)
                .orElse(null);
        if (billingAgreement != null && !isTerminal(billingAgreement.getStatus())) {
            billingAgreement.cancel();
        }

        UserSubscription userSubscription = userSubscriptionRepository.findByUser(user).orElse(null);
        if (userSubscription != null && userSubscription.getStatus() == SubscriptionStatus.ACTIVE) {
            userSubscription.cancel();
        }

        if (billingAgreement != null && hasIssuedKey(billingAgreement)) {
            eventPublisher.publishEvent(new WithdrawalBillingCleanupRequestedEvent(billingAgreement.getId()));
        }

        // 관련 레코드 정리 (고아 레코드 방지)
        likeRepository.deleteAllByUser(user);
        downloadQueueRepository.deleteAllByUser(user);
        playHistoryRepository.deleteAllByUser(user);
        trackDownloadRepository.deleteAllByUser(user);
        licenseRepository.deleteAllByUser(user);
        whitelistChannelRepository.deleteAllByUser(user);

        user.withdraw();
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
    public UserDetailResponse updateUserByAdmin(Long userID, UserAdminUpdateRequest request) {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        user.updateByAdmin(request.getRole(), request.getIsVerified());
        return UserDetailResponse.from(user);
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

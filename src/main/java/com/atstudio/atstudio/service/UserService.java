package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.user.*;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException(BUSINESS_ERROR.EMAIL_ALREADY_REGISTERED);
        }
        if (userRepository.findByNickname(request.getNickname()).isPresent()) {
            throw new BusinessException(BUSINESS_ERROR.NICKNAME_DUPLICATED);
        }

        User user = User.builder()
                .nickname(request.getNickname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phonePersonal(request.getPhonePersonal())
                .phoneCompany(request.getPhoneCompany())
                .job(request.getJob())
                .userType(request.getUserType())
                .role(UserRole.USER)
                .build();

        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getMyProfile(Long userID) {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        return toResponse(user);
    }

    public UserResponse updateMyProfile(Long userID, UpdateProfileRequest request) {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.findByNickname(request.getNickname()).isPresent()) {
                throw new BusinessException(BUSINESS_ERROR.NICKNAME_DUPLICATED);
            }
        }

        user.updateProfile(request.getNickname(), request.getPhonePersonal(),
                request.getPhoneCompany(), request.getJob());
        return toResponse(user);
    }

    public void withdraw(Long userID, WithdrawRequest request) {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        if (user.getPassword() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_CREDENTIALS);
        }

        user.withdraw();
    }

    public UserResponse completeProfile(Long userID, CompleteProfileRequest request) {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        if (user.isProfileComplete()) {
            throw new BusinessException(BUSINESS_ERROR.PROFILE_ALREADY_COMPLETE);
        }

        if (!request.getNickname().equals(user.getNickname())) {
            if (userRepository.findByNickname(request.getNickname()).isPresent()) {
                throw new BusinessException(BUSINESS_ERROR.NICKNAME_DUPLICATED);
            }
        }

        user.completeProfile(request.getNickname(), request.getPhonePersonal(),
                request.getPhoneCompany(), request.getJob(), request.getUserType());
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    @Transactional(readOnly = true)
    public boolean isPhoneAvailable(String phone) {
        return userRepository.findByPhonePersonal(phone).isEmpty();
    }

    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname) {
        return userRepository.findByNickname(nickname).isEmpty();
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getPhonePersonal(),
                user.getPhoneCompany(),
                user.getJob() != null ? user.getJob().name() : null,
                user.getUserType().name(),
                user.getRole().name(),
                user.isVerified(),
                user.getCreatedAt());
    }
}

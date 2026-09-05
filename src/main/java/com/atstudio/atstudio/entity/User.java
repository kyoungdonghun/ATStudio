package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import com.atstudio.atstudio.entity.enums.UserJob;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import jakarta.persistence.*;
import lombok.*;

import static com.atstudio.atstudio.common.validation.ValidationConstants.normalizeNickname;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20, unique = true)
    private String nickname;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(name = "phone_company", length = 20)
    private String phoneCompany;

    @Column(name = "phone_personal", length = 20)
    private String phonePersonal;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserJob job;

    @Column(name = "company_name", length = 100)
    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    @Builder.Default
    private UserType userType = UserType.INDIVIDUAL;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    public void updateRefreshToken(String hashedRefreshToken) {
        this.refreshToken = hashedRefreshToken;
    }

    public void clearRefreshToken() {
        this.refreshToken = null;
    }

    public void updateProfile(String nickname, String phonePersonal, String phoneCompany, UserJob job, String companyName) {
        if (nickname != null) this.nickname = normalizeNickname(nickname);
        if (phonePersonal != null) this.phonePersonal = phonePersonal;
        if (phoneCompany != null) this.phoneCompany = phoneCompany;
        if (job != null) this.job = job;
        this.companyName = companyName;
    }

    public void withdraw() {
        this.isDeleted = true;
        this.refreshToken = null;
    }

    public boolean isProfileComplete() {
        if (phonePersonal == null || phonePersonal.isBlank()) {
            return false;
        }

        if (userType == UserType.BUSINESS) {
            return companyName != null && !companyName.isBlank();
        }

        return job != null;
    }

    public void updateByAdmin(UserRole role, Boolean isVerified) {
        if (role != null) this.role = role;
        if (isVerified != null) this.isVerified = isVerified;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void verify() {
        this.isVerified = true;
    }

    public void completeProfile(String nickname, String phonePersonal, String phoneCompany, UserJob job, UserType userType, String companyName) {
        this.nickname = normalizeNickname(nickname);
        this.phonePersonal = phonePersonal;
        if (phoneCompany != null) this.phoneCompany = phoneCompany;
        this.job = job;
        this.userType = userType;
        this.companyName = companyName;
    }

    /**
     * Non-production fixture synchronization helper.
     * Keeps a known QA account usable across repeated local/stage boots.
     */
    public void applyBootstrapFixture(
            String nickname,
            String encodedPassword,
            String phonePersonal,
            String phoneCompany,
            boolean verified,
            UserRole role,
            UserJob job,
            UserType userType,
            String companyName
    ) {
        this.nickname = nickname;
        this.password = encodedPassword;
        this.phonePersonal = phonePersonal;
        this.phoneCompany = phoneCompany;
        this.isVerified = verified;
        this.role = role;
        this.job = job;
        this.userType = userType;
        this.companyName = companyName;
        this.isDeleted = false;
        this.refreshToken = null;
    }
}

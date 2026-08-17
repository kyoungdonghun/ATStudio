package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.entity.enums.UserConsentType;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Immutable
@Table(
        name = "user_consents",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_consents_user_type_version",
                columnNames = {"user_id", "consent_type", "policy_version"}))
@Access(AccessType.FIELD)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "consent_type", nullable = false, length = 40, updatable = false)
    private UserConsentType consentType;

    @Column(name = "policy_version", nullable = false, length = 100, updatable = false)
    private String policyVersion;

    @CreationTimestamp
    @Column(name = "agreed_at", nullable = false, updatable = false)
    private LocalDateTime agreedAt;

    private UserConsent(
            User user,
            UserConsentType consentType,
            String policyVersion) {
        this.user = user;
        this.consentType = consentType;
        this.policyVersion = policyVersion;
    }

    public static UserConsent agree(
            User user,
            UserConsentType consentType,
            String policyVersion) {
        return new UserConsent(user, consentType, policyVersion);
    }
}

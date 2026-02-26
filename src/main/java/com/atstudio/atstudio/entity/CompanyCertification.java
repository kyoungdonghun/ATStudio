package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import com.atstudio.atstudio.entity.enums.CompanyCertificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_certifications")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CompanyCertification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CompanyCertificationStatus status = CompanyCertificationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String adminNote;

    @Column(nullable = false, length = 500)
    private String documentPath;

    @Column(length = 50, unique = true)
    private String certificationCode;

    private LocalDateTime approvedAt;

    public void process(CompanyCertificationStatus newStatus, String adminNote,
                        String certificationCode, LocalDateTime approvedAt) {
        this.status = newStatus;
        this.adminNote = adminNote;
        this.certificationCode = certificationCode;
        this.approvedAt = approvedAt;
    }
}

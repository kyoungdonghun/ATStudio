package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import com.atstudio.atstudio.entity.enums.CompanyCertificationStatus;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
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

    @Version
    @Column(nullable = false)
    private long version;

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

    @Builder.Default
    @OneToMany(mappedBy = "certification", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompanyCertificationDocument> documents = new ArrayList<>();

    @Column(length = 50, unique = true)
    private String certificationCode;

    private LocalDateTime approvedAt;

    public void process(CompanyCertificationStatus newStatus, String adminNote,
                        String certificationCode, LocalDateTime approvedAt) {
        validateTransition(this.status, newStatus);
        this.status = newStatus;
        this.adminNote = adminNote;
        this.certificationCode = certificationCode;
        this.approvedAt = approvedAt;
    }

    public void updateDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    public void clearDocuments() {
        this.documents.clear();
    }

    public void addDocument(String originalFilename, String storedPath, String contentType, Long sizeBytes) {
        this.documents.add(CompanyCertificationDocument.builder()
                .certification(this)
                .originalFilename(originalFilename)
                .storedPath(storedPath)
                .contentType(contentType)
                .sizeBytes(sizeBytes)
                .build());
    }

    private void validateTransition(CompanyCertificationStatus from, CompanyCertificationStatus to) {
        boolean valid = switch (from) {
            case PENDING -> to == CompanyCertificationStatus.APPROVED
                         || to == CompanyCertificationStatus.REVISION_REQUESTED
                         || to == CompanyCertificationStatus.REJECTED;
            case REVISION_REQUESTED -> to == CompanyCertificationStatus.PENDING;
            default -> false;
        };
        if (!valid) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        }
    }
}

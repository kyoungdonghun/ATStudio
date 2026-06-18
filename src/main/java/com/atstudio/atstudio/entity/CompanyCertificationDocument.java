package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "company_certification_documents",
        indexes = {
                @Index(
                        name = "idx_company_certification_documents_certification_id",
                        columnList = "certification_id"
                )
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CompanyCertificationDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_id", nullable = false)
    private CompanyCertification certification;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "stored_path", nullable = false, length = 500)
    private String storedPath;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;
}

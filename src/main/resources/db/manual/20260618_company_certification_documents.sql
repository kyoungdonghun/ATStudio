-- Manual patch for existing local/operational DBs.
-- Purpose: align company certification document metadata with REQ-20260618-ATS-001.
-- Apply only after operator approval.

CREATE TABLE IF NOT EXISTS company_certification_documents
(
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    certification_id  BIGINT       NOT NULL,
    original_filename VARCHAR(255) NOT NULL COMMENT 'Original client filename for admin review display.',
    stored_path       VARCHAR(500) NOT NULL COMMENT 'StorageService relative path. Do not expose directly to users.',
    content_type      VARCHAR(100) NULL,
    size_bytes        BIGINT       NOT NULL,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_company_certification_documents_certification_id (certification_id),
    CONSTRAINT fk_company_certification_documents_certification
        FOREIGN KEY (certification_id) REFERENCES company_certifications (id)
        ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

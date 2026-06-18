package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.CompanyCertificationDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyCertificationDocumentRepository
        extends JpaRepository<CompanyCertificationDocument, Long> {

    Optional<CompanyCertificationDocument> findByIdAndCertificationId(
            Long id,
            Long certificationId
    );
}

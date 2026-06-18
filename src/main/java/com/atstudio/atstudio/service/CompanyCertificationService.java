package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.common.validation.ValidationConstants;
import com.atstudio.atstudio.dto.certification.CompanyCertificationDocumentDownload;
import com.atstudio.atstudio.dto.certification.CompanyCertificationResponse;
import com.atstudio.atstudio.dto.certification.CompanyCertificationReviewRequest;
import com.atstudio.atstudio.dto.certification.CompanyCertificationSummaryResponse;
import com.atstudio.atstudio.entity.CompanyCertification;
import com.atstudio.atstudio.entity.CompanyCertificationDocument;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.CompanyCertificationStatus;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.CompanyCertificationDocumentRepository;
import com.atstudio.atstudio.repository.CompanyCertificationRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompanyCertificationService {

    private static final List<CompanyCertificationStatus> OPEN_OR_LOCKED_STATUSES = List.of(
            CompanyCertificationStatus.PENDING,
            CompanyCertificationStatus.APPROVED,
            CompanyCertificationStatus.REVISION_REQUESTED
    );

    private final CompanyCertificationRepository certificationRepository;
    private final CompanyCertificationDocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    // ── 13.1 POST /api/company-certifications ────────────────────────────────

    @Transactional
    public CompanyCertificationResponse apply(CustomUserDetails userDetails,
                                               List<MultipartFile> documents) {
        User user = findUser(userDetails);

        if (user.getUserType() != UserType.BUSINESS) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }

        boolean hasOpenOrLockedCertification = certificationRepository.existsByUserAndStatusIn(
                user,
                OPEN_OR_LOCKED_STATUSES
        );
        if (hasOpenOrLockedCertification) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_DUPLICATE);
        }

        List<MultipartFile> validDocuments = validateDocuments(documents);
        String directory = buildDocumentDirectory(user.getId());
        List<StoredCertificationDocument> storedDocuments = storeDocuments(directory, validDocuments);
        registerDocumentFileCleanup(storedDocuments, List.of());

        CompanyCertification certification = CompanyCertification.builder()
                .user(user)
                .documentPath(toPublicDocumentDirectory(directory))
                .build();
        for (StoredCertificationDocument stored : storedDocuments) {
            certification.addDocument(
                    stored.originalFilename(),
                    stored.storedPath(),
                    stored.contentType(),
                    stored.sizeBytes()
            );
        }

        certification = certificationRepository.save(certification);
        return CompanyCertificationResponse.from(certification);
    }

    // ── 13.2 POST /api/company-certifications/me/documents ───────────────────

    @Transactional
    public CompanyCertificationResponse resubmit(CustomUserDetails userDetails,
                                                 List<MultipartFile> documents) {
        User user = findUser(userDetails);

        if (user.getUserType() != UserType.BUSINESS) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }

        CompanyCertification certification = certificationRepository.findTopByUserOrderByCreatedAtDescIdDesc(user)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        if (certification.getStatus() != CompanyCertificationStatus.REVISION_REQUESTED) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        }

        List<MultipartFile> validDocuments = validateDocuments(documents);
        String directory = buildDocumentDirectory(user.getId());
        List<StoredCertificationDocument> storedDocuments = storeDocuments(directory, validDocuments);
        List<String> previousStoredPaths = certification.getDocuments().stream()
                .map(CompanyCertificationDocument::getStoredPath)
                .toList();
        registerDocumentFileCleanup(storedDocuments, previousStoredPaths);

        certification.clearDocuments();
        certification.updateDocumentPath(toPublicDocumentDirectory(directory));
        storedDocuments.forEach(stored ->
                certification.addDocument(
                        stored.originalFilename(),
                        stored.storedPath(),
                        stored.contentType(),
                        stored.sizeBytes()
                ));
        certification.process(CompanyCertificationStatus.PENDING, null, null, null);

        return CompanyCertificationResponse.from(certification);
    }

    // ── 13.3 GET /api/company-certifications/me ──────────────────────────────

    public CompanyCertificationResponse getMyStatus(CustomUserDetails userDetails) {
        User user = findUser(userDetails);

        return certificationRepository.findTopByUserOrderByCreatedAtDescIdDesc(user)
                .map(CompanyCertificationResponse::from)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    // ── 13.4 GET /api/company-certifications ─────────────────────────────────

    public ResponseDTO<CompanyCertificationSummaryResponse> listAll(String status,
                                                                     int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "id")));

        Page<CompanyCertification> result;
        if (status != null && !status.isBlank()) {
            CompanyCertificationStatus certStatus;
            try {
                certStatus = CompanyCertificationStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
            }
            result = certificationRepository.findByStatus(certStatus, pageable);
        } else {
            result = certificationRepository.findAll(pageable);
        }

        List<CompanyCertificationSummaryResponse> dataList = result.getContent().stream()
                .map(CompanyCertificationSummaryResponse::from)
                .toList();
        int total = (int) result.getTotalElements();

        return ResponseDTO.<CompanyCertificationSummaryResponse>builder()
                .dataList(dataList)
                .pageInfo(PageInfo.of(page, size, total, 10))
                .build();
    }

    // ── 13.5 GET /api/company-certifications/{certificationId} ───────────────

    public CompanyCertificationResponse getDetail(Long certificationId) {
        CompanyCertification certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        return CompanyCertificationResponse.from(certification);
    }

    // ── 13.6 GET /api/company-certifications/{certificationId}/documents/{documentId}

    public CompanyCertificationDocumentDownload downloadDocument(Long certificationId, Long documentId) {
        CompanyCertificationDocument document = documentRepository.findByIdAndCertificationId(
                        documentId,
                        certificationId
                )
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        Resource resource = storageService.loadAsResource(document.getStoredPath());
        return new CompanyCertificationDocumentDownload(
                resource,
                document.getOriginalFilename(),
                document.getContentType()
        );
    }

    // ── 13.7 PUT /api/company-certifications/{certificationId} ───────────────

    @Transactional
    public CompanyCertificationResponse processReview(Long certificationId,
                                                       CompanyCertificationReviewRequest request) {
        CompanyCertification certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        String certificationCode = null;
        LocalDateTime approvedAt = null;

        if (request.status() == CompanyCertificationStatus.APPROVED) {
            certificationCode = "BIZ-" + UUID.randomUUID();
            approvedAt = LocalDateTime.now();
        }

        certification.process(request.status(), request.adminNote(),
                certificationCode, approvedAt);

        return CompanyCertificationResponse.from(certification);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private User findUser(CustomUserDetails userDetails) {
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    private List<MultipartFile> validateDocuments(List<MultipartFile> documents) {
        if (documents == null || documents.isEmpty()) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_VALID);
        }

        List<MultipartFile> validDocuments = documents.stream()
                .filter(doc -> doc != null && !doc.isEmpty())
                .toList();
        if (validDocuments.isEmpty()) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_VALID);
        }
        if (validDocuments.size() > ValidationConstants.CERT_DOC_MAX_COUNT) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_VALID);
        }

        for (MultipartFile doc : validDocuments) {
            String originalName = sanitizeOriginalFilename(doc.getOriginalFilename());
            int extensionIndex = originalName.lastIndexOf('.');
            if (extensionIndex < 0 || extensionIndex == originalName.length() - 1) {
                throw new BusinessException(BUSINESS_ERROR.INVALID_VALID);
            }
            String ext = originalName.substring(extensionIndex + 1).toLowerCase();
            if (!ValidationConstants.CERT_DOC_ALLOWED_EXTENSIONS.contains(ext)) {
                throw new BusinessException(BUSINESS_ERROR.INVALID_VALID);
            }
            if (doc.getSize() > ValidationConstants.CERT_DOC_MAX_SIZE_BYTES) {
                throw new BusinessException(BUSINESS_ERROR.INVALID_VALID);
            }
        }

        return validDocuments;
    }

    private List<StoredCertificationDocument> storeDocuments(String directory,
                                                             List<MultipartFile> documents) {
        List<StoredCertificationDocument> storedDocuments = new ArrayList<>();
        try {
            for (MultipartFile doc : documents) {
                storedDocuments.add(new StoredCertificationDocument(
                        sanitizeOriginalFilename(doc.getOriginalFilename()),
                        storageService.store(doc, directory),
                        doc.getContentType(),
                        doc.getSize()
                ));
            }
        } catch (RuntimeException e) {
            storedDocuments.stream()
                    .map(StoredCertificationDocument::storedPath)
                    .forEach(storageService::delete);
            throw e;
        }
        return Collections.unmodifiableList(storedDocuments);
    }

    private void registerDocumentFileCleanup(List<StoredCertificationDocument> newDocuments,
                                             List<String> previousStoredPaths) {
        List<String> newStoredPaths = newDocuments.stream()
                .map(StoredCertificationDocument::storedPath)
                .toList();
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                previousStoredPaths.forEach(storageService::delete);
            }

            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    newStoredPaths.forEach(storageService::delete);
                }
            }
        });
    }

    private String buildDocumentDirectory(Long userId) {
        return "company-docs/" + userId + "/" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String toPublicDocumentDirectory(String directory) {
        return "/uploads/" + directory + "/";
    }

    private String sanitizeOriginalFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_VALID);
        }
        return Paths.get(originalFilename).getFileName().toString();
    }

    private record StoredCertificationDocument(
            String originalFilename,
            String storedPath,
            String contentType,
            long sizeBytes
    ) {}
}

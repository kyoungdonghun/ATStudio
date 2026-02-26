package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.certification.CompanyCertificationResponse;
import com.atstudio.atstudio.dto.certification.CompanyCertificationReviewRequest;
import com.atstudio.atstudio.dto.certification.CompanyCertificationSummaryResponse;
import com.atstudio.atstudio.entity.CompanyCertification;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.CompanyCertificationStatus;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.CompanyCertificationRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompanyCertificationService {

    private final CompanyCertificationRepository certificationRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    // ── 13.1 POST /api/company-certifications ────────────────────────────────

    @Transactional
    public CompanyCertificationResponse apply(CustomUserDetails userDetails,
                                               List<MultipartFile> documents) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        if (user.getUserType() != UserType.BUSINESS) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }

        boolean hasPendingOrApproved = certificationRepository.existsByUserAndStatusIn(
                user, List.of(CompanyCertificationStatus.PENDING, CompanyCertificationStatus.APPROVED));
        if (hasPendingOrApproved) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_DUPLICATE);
        }

        String documentPath = storeDocuments(user.getId(), documents);

        CompanyCertification certification = CompanyCertification.builder()
                .user(user)
                .documentPath(documentPath)
                .build();
        certification = certificationRepository.save(certification);

        return CompanyCertificationResponse.from(certification);
    }

    // ── 13.2 GET /api/company-certifications/me ──────────────────────────────

    public CompanyCertificationResponse getMyStatus(CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        return certificationRepository.findByUser(user)
                .map(CompanyCertificationResponse::from)
                .orElse(null);
    }

    // ── 13.3 GET /api/company-certifications ─────────────────────────────────

    public ResponseDTO<CompanyCertificationSummaryResponse> listAll(String status,
                                                                     int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<CompanyCertification> result;
        if (status != null && !status.isBlank()) {
            CompanyCertificationStatus certStatus = CompanyCertificationStatus.valueOf(status);
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

    // ── 13.4 GET /api/company-certifications/{certificationId} ───────────────

    public CompanyCertificationResponse getDetail(Long certificationId) {
        CompanyCertification certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        return CompanyCertificationResponse.from(certification);
    }

    // ── 13.5 PUT /api/company-certifications/{certificationId} ───────────────

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

    private String storeDocuments(Long userId, List<MultipartFile> documents) {
        String directory = "company-docs/" + userId;
        if (documents != null && !documents.isEmpty()) {
            for (MultipartFile doc : documents) {
                if (!doc.isEmpty()) {
                    storageService.store(doc, directory);
                }
            }
        }
        return "/uploads/" + directory + "/";
    }
}

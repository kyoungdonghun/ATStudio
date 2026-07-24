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
import com.atstudio.atstudio.entity.CompanyCertificationAuditLog;
import com.atstudio.atstudio.entity.CompanyCertificationDocument;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.CompanyCertificationAuditAction;
import com.atstudio.atstudio.entity.enums.CompanyCertificationStatus;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.CompanyCertificationDocumentRepository;
import com.atstudio.atstudio.repository.CompanyCertificationAuditLogRepository;
import com.atstudio.atstudio.repository.CompanyCertificationRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.image.CanonicalImageService;
import com.atstudio.atstudio.service.storage.StorageDomain;
import com.atstudio.atstudio.service.storage.StorageMutationCoordinator;
import com.atstudio.atstudio.service.storage.StorageRoot;
import com.atstudio.atstudio.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
    private static final byte[] PDF_HEADER = {'%', 'P', 'D', 'F', '-'};
    private static final byte[] PDF_EOF = {'%', '%', 'E', 'O', 'F'};

    private final CompanyCertificationRepository certificationRepository;
    private final CompanyCertificationDocumentRepository documentRepository;
    private final CompanyCertificationAuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final StorageMutationCoordinator storageMutationCoordinator;
    private final CanonicalImageService canonicalImageService;

    // ── 13.1 POST /api/company-certifications ────────────────────────────────

    @Transactional
    public CompanyCertificationResponse apply(CustomUserDetails userDetails,
                                               List<MultipartFile> documents) {
        User user = findUserForUpdate(userDetails);

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

        List<VerifiedCertificationDocument> validDocuments = validateDocuments(documents);
        String directory = buildDocumentDirectory(user.getId());
        List<StoredCertificationDocument> storedDocuments = storeDocuments(directory, validDocuments);

        CompanyCertification certification = CompanyCertification.builder()
                .user(user)
                .documentPath(directory)
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
        User user = findUserForUpdate(userDetails);

        if (user.getUserType() != UserType.BUSINESS) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }

        CompanyCertification certification = certificationRepository.findByUserForUpdate(
                        user,
                        PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        if (certification.getStatus() != CompanyCertificationStatus.REVISION_REQUESTED) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        }

        List<VerifiedCertificationDocument> validDocuments = validateDocuments(documents);
        String directory = buildDocumentDirectory(user.getId());
        List<StoredCertificationDocument> storedDocuments = storeDocuments(directory, validDocuments);
        List<String> previousStoredPaths = certification.getDocuments().stream()
                .map(CompanyCertificationDocument::getStoredPath)
                .toList();
        storageMutationCoordinator.deleteAfterCommit(
                StorageDomain.COMPANY_CERTIFICATION,
                StorageRoot.PRIVATE,
                previousStoredPaths);

        certification.clearDocuments();
        certification.updateDocumentPath(directory);
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

        if (user.getUserType() != UserType.BUSINESS) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }

        return certificationRepository.findTopByUserOrderByCreatedAtDescIdDesc(user)
                .map(CompanyCertificationResponse::from)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    // ── 13.4 GET /api/company-certifications ─────────────────────────────────

    public ResponseDTO<CompanyCertificationSummaryResponse> listAll(String status,
                                                                     int page, int size) {
        int effectivePage = Math.max(1, page);
        int effectiveSize = Math.min(
                ValidationConstants.CERTIFICATION_ADMIN_MAX_PAGE_SIZE,
                Math.max(1, size));
        Pageable pageable = PageRequest.of(effectivePage - 1, effectiveSize,
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
                .pageInfo(PageInfo.of(effectivePage, effectiveSize, total, 10))
                .build();
    }

    // ── 13.5 GET /api/company-certifications/{certificationId} ───────────────

    public CompanyCertificationResponse getDetail(Long certificationId) {
        CompanyCertification certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        return CompanyCertificationResponse.from(certification);
    }

    // ── 13.6 GET /api/company-certifications/{certificationId}/documents/{documentId}

    @Transactional
    public CompanyCertificationDocumentDownload downloadDocument(
            Long certificationId,
            Long documentId,
            CustomUserDetails actorDetails) {
        CompanyCertificationDocument document = documentRepository.findByIdAndCertificationId(
                        documentId,
                        certificationId
                )
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        User actor = findActor(actorDetails);
        Resource resource = storageService.loadAsResource(StorageRoot.PRIVATE, document.getStoredPath());
        auditLogRepository.save(CompanyCertificationAuditLog.builder()
                .certification(document.getCertification())
                .actorUser(actor)
                .action(CompanyCertificationAuditAction.DOCUMENT_ACCESS_GRANTED)
                .documentId(document.getId())
                .build());
        return new CompanyCertificationDocumentDownload(
                resource,
                document.getOriginalFilename()
        );
    }

    // ── 13.7 PUT /api/company-certifications/{certificationId} ───────────────

    @Transactional
    public CompanyCertificationResponse processReview(Long certificationId,
                                                       CustomUserDetails actorDetails,
                                                       CompanyCertificationReviewRequest request) {
        CompanyCertification initial = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        userRepository.findByIdForUpdate(initial.getUser().getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        CompanyCertification certification = certificationRepository.findByIdForUpdate(certificationId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        User actor = findActor(actorDetails);
        String adminNote = normalizeReviewNote(request.status(), request.adminNote());
        CompanyCertificationStatus beforeStatus = certification.getStatus();

        String certificationCode = null;
        LocalDateTime approvedAt = null;

        if (request.status() == CompanyCertificationStatus.APPROVED) {
            certificationCode = "BIZ-" + UUID.randomUUID();
            approvedAt = LocalDateTime.now();
        }

        certification.process(request.status(), adminNote,
                certificationCode, approvedAt);
        auditLogRepository.save(CompanyCertificationAuditLog.builder()
                .certification(certification)
                .actorUser(actor)
                .action(CompanyCertificationAuditAction.REVIEWED)
                .fromStatus(beforeStatus.name())
                .toStatus(certification.getStatus().name())
                .build());

        return CompanyCertificationResponse.from(certification);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private User findUser(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getId() == null) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    private User findUserForUpdate(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getId() == null) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }
        return userRepository.findByIdForUpdate(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    private User findActor(CustomUserDetails actorDetails) {
        if (actorDetails == null || actorDetails.getId() == null) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }
        return userRepository.findById(actorDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    private List<VerifiedCertificationDocument> validateDocuments(List<MultipartFile> documents) {
        if (documents == null || documents.isEmpty()) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_VALID);
        }

        if (documents.stream().anyMatch(document -> document == null || document.isEmpty())) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_VALID);
        }
        List<MultipartFile> validDocuments = List.copyOf(documents);
        if (validDocuments.size() > ValidationConstants.CERT_DOC_MAX_COUNT) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_VALID);
        }

        long aggregateSize = validDocuments.stream().mapToLong(MultipartFile::getSize).sum();
        if (aggregateSize > ValidationConstants.CERT_DOC_MAX_AGGREGATE_SIZE_BYTES) {
            throw new BusinessException(BUSINESS_ERROR.IO_LARGE);
        }

        return validDocuments.stream()
                .map(this::verifyDocument)
                .toList();
    }

    private List<StoredCertificationDocument> storeDocuments(String directory,
                                                              List<VerifiedCertificationDocument> documents) {
        List<String> storedPaths = storageMutationCoordinator.storeAll(
                StorageDomain.COMPANY_CERTIFICATION,
                StorageRoot.PRIVATE,
                documents.stream().map(VerifiedCertificationDocument::storageFile).toList(),
                directory);
        return java.util.stream.IntStream.range(0, documents.size())
                .mapToObj(index -> {
                    VerifiedCertificationDocument document = documents.get(index);
                    return new StoredCertificationDocument(
                            document.originalFilename(),
                            storedPaths.get(index),
                            document.contentType(),
                            document.sizeBytes());
                })
                .toList();
    }

    private String buildDocumentDirectory(Long userId) {
        return "company-docs/" + userId + "/" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String sanitizeOriginalFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_VALID);
        }
        String trimmed = originalFilename.trim();
        if (trimmed.length() > ValidationConstants.CERT_DOC_FILENAME_MAX) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_VALID);
        }
        if (trimmed.indexOf('\0') >= 0
                || trimmed.contains("/")
                || trimmed.contains("\\")
                || trimmed.contains(":")
                || trimmed.equals(".")
                || trimmed.equals("..")) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_VALID);
        }
        return trimmed;
    }

    private VerifiedCertificationDocument verifyDocument(MultipartFile document) {
        if (document.getSize() > ValidationConstants.CERT_DOC_MAX_SIZE_BYTES) {
            throw new BusinessException(BUSINESS_ERROR.IO_LARGE);
        }

        String originalFilename = sanitizeOriginalFilename(document.getOriginalFilename());
        String extension = extensionOf(originalFilename);
        if (!ValidationConstants.CERT_DOC_ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_VALID);
        }

        byte[] bytes = readBytes(document);
        VerifiedFormat format = verifyFormat(bytes);
        verifyExtension(extension, format);
        verifyClientMime(document.getContentType(), format.submittedMimeTypes());

        if (format == VerifiedFormat.PDF) {
            MultipartFile storageFile = new VerifiedMultipartFile(
                    document.getName(),
                    originalFilename,
                    format.verifiedInputMimeType(),
                    bytes);
            return new VerifiedCertificationDocument(
                    originalFilename,
                    storageFile,
                    format.verifiedInputMimeType(),
                    storageFile.getSize());
        }

        MultipartFile canonicalImage = canonicalImageService.canonicalizeThumbnail(document);
        return new VerifiedCertificationDocument(
                originalFilename,
                canonicalImage,
                canonicalImage.getContentType(),
                canonicalImage.getSize());
    }

    private String extensionOf(String originalFilename) {
        int extensionIndex = originalFilename.lastIndexOf('.');
        if (extensionIndex < 0 || extensionIndex == originalFilename.length() - 1) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_VALID);
        }
        return originalFilename.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);
    }

    private byte[] readBytes(MultipartFile document) {
        try {
            return document.getBytes();
        } catch (IOException exception) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_VALID);
        }
    }

    private VerifiedFormat verifyFormat(byte[] bytes) {
        if (startsWith(bytes, PDF_HEADER) && endsWithPdfEof(bytes)) {
            return VerifiedFormat.PDF;
        }
        if (startsWith(bytes, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF})) {
            return VerifiedFormat.JPEG;
        }
        if (startsWith(bytes, new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        })) {
            return VerifiedFormat.PNG;
        }
        throw new BusinessException(BUSINESS_ERROR.INVALID_VALID);
    }

    private boolean startsWith(byte[] bytes, byte[] prefix) {
        return bytes.length >= prefix.length
                && Arrays.equals(Arrays.copyOf(bytes, prefix.length), prefix);
    }

    private boolean endsWithPdfEof(byte[] bytes) {
        int end = bytes.length - 1;
        while (end >= 0 && isPdfWhitespace(bytes[end])) {
            end--;
        }
        if (end + 1 < PDF_EOF.length) {
            return false;
        }
        for (int index = 0; index < PDF_EOF.length; index++) {
            if (bytes[end - PDF_EOF.length + 1 + index] != PDF_EOF[index]) {
                return false;
            }
        }
        return true;
    }

    private boolean isPdfWhitespace(byte value) {
        return value == 0x00
                || value == 0x09
                || value == 0x0A
                || value == 0x0C
                || value == 0x0D
                || value == 0x20;
    }

    private void verifyExtension(String extension, VerifiedFormat format) {
        boolean matches = switch (format) {
            case PDF -> extension.equals("pdf");
            case JPEG -> extension.equals("jpg") || extension.equals("jpeg");
            case PNG -> extension.equals("png");
        };
        if (!matches) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_VALID);
        }
    }

    private void verifyClientMime(String clientMime, List<String> acceptedMimeTypes) {
        if (clientMime == null || clientMime.isBlank()) {
            return;
        }
        String normalized = clientMime.toLowerCase(Locale.ROOT);
        if (normalized.equals("application/octet-stream") || acceptedMimeTypes.contains(normalized)) {
            return;
        }
        throw new BusinessException(BUSINESS_ERROR.INVALID_VALID);
    }

    private String normalizeReviewNote(CompanyCertificationStatus status, String adminNote) {
        String normalized = adminNote == null ? null : adminNote.trim();
        if (normalized != null
                && normalized.length() > ValidationConstants.CERTIFICATION_REVIEW_NOTE_MAX) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_VALID);
        }
        boolean requiresReason = status == CompanyCertificationStatus.REVISION_REQUESTED
                || status == CompanyCertificationStatus.REJECTED;
        if (requiresReason && (normalized == null || normalized.isBlank())) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_VALID);
        }
        return normalized == null || normalized.isBlank() ? null : normalized;
    }

    private record VerifiedCertificationDocument(
            String originalFilename,
            MultipartFile storageFile,
            String contentType,
            long sizeBytes
    ) {}

    private record StoredCertificationDocument(
            String originalFilename,
            String storedPath,
            String contentType,
            long sizeBytes
    ) {}

    private enum VerifiedFormat {
        PDF("application/pdf", List.of("application/pdf")),
        JPEG("image/jpeg", List.of("image/jpeg")),
        PNG("image/png", List.of("image/png"));

        private final String verifiedInputMimeType;
        private final List<String> submittedMimeTypes;

        VerifiedFormat(String verifiedInputMimeType, List<String> submittedMimeTypes) {
            this.verifiedInputMimeType = verifiedInputMimeType;
            this.submittedMimeTypes = submittedMimeTypes;
        }

        String verifiedInputMimeType() {
            return verifiedInputMimeType;
        }

        List<String> submittedMimeTypes() {
            return submittedMimeTypes;
        }
    }

    private record VerifiedMultipartFile(
            String name,
            String originalFilename,
            String contentType,
            byte[] bytes
    ) implements MultipartFile {

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return bytes.length == 0;
        }

        @Override
        public long getSize() {
            return bytes.length;
        }

        @Override
        public byte[] getBytes() {
            return bytes.clone();
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(bytes);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            java.nio.file.Files.write(dest.toPath(), bytes);
        }
    }
}

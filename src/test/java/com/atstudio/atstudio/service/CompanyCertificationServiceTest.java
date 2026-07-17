package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.common.validation.ValidationConstants;
import com.atstudio.atstudio.dto.certification.CompanyCertificationResponse;
import com.atstudio.atstudio.dto.certification.CompanyCertificationReviewRequest;
import com.atstudio.atstudio.dto.certification.CompanyCertificationSummaryResponse;
import com.atstudio.atstudio.entity.CompanyCertification;
import com.atstudio.atstudio.entity.CompanyCertificationAuditLog;
import com.atstudio.atstudio.entity.CompanyCertificationDocument;
import com.atstudio.atstudio.entity.enums.CompanyCertificationAuditAction;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.CompanyCertificationStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyCertificationService 단위 테스트")
class CompanyCertificationServiceTest {

    @Mock CompanyCertificationRepository certificationRepository;
    @Mock CompanyCertificationDocumentRepository documentRepository;
    @Mock CompanyCertificationAuditLogRepository auditLogRepository;
    @Mock UserRepository userRepository;
    @Mock StorageService storageService;
    @Mock StorageMutationCoordinator storageMutationCoordinator;
    @Mock CanonicalImageService canonicalImageService;

    @InjectMocks CompanyCertificationService certificationService;

    @BeforeEach
    void mirrorLockRepositoryStubs() {
        lenient().when(userRepository.findByIdForUpdate(anyLong()))
                .thenAnswer(invocation -> userRepository.findById(invocation.getArgument(0)));
        lenient().when(certificationRepository.findByUserForUpdate(any(), any()))
                .thenAnswer(invocation -> certificationRepository
                        .findTopByUserOrderByCreatedAtDescIdDesc(invocation.getArgument(0))
                        .map(List::of)
                        .orElseGet(List::of));
        lenient().when(certificationRepository.findByIdForUpdate(anyLong()))
                .thenAnswer(invocation -> certificationRepository.findById(invocation.getArgument(0)));
    }

    // ── 13.1 apply ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("apply()")
    class Apply {

        @Test
        @DisplayName("성공 - BUSINESS 회원 정상 신청")
        void apply_success() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(certificationRepository.existsByUserAndStatusIn(eq(user), anyList()))
                    .willReturn(false);
            given(storageMutationCoordinator.storeAll(
                    eq(StorageDomain.COMPANY_CERTIFICATION),
                    eq(StorageRoot.PRIVATE),
                    anyList(),
                    startsWith("company-docs/1/")))
                    .willReturn(List.of("company-docs/1/doc.pdf"));
            given(certificationRepository.save(any(CompanyCertification.class)))
                    .willAnswer(invocation -> {
                        CompanyCertification certification = invocation.getArgument(0);
                        ReflectionTestUtils.setField(certification, "id", 1L);
                        return certification;
                    });

            List<MultipartFile> documents = List.of(
                    new MockMultipartFile("documents", "doc.pdf",
                            "application/pdf", pdfBytes()));

            CompanyCertificationResponse result = certificationService.apply(
                    buildUserDetails(1L, UserRole.USER), documents);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.status()).isEqualTo("PENDING");
            assertThat(result.documents()).hasSize(1);
            assertThat(result.documents().get(0).contentType()).isEqualTo("application/pdf");
            verify(storageMutationCoordinator).storeAll(
                    eq(StorageDomain.COMPANY_CERTIFICATION),
                    eq(StorageRoot.PRIVATE),
                    anyList(),
                    startsWith("company-docs/1/"));
            InOrder lockOrder = inOrder(userRepository, certificationRepository);
            lockOrder.verify(userRepository).findByIdForUpdate(1L);
            lockOrder.verify(certificationRepository).existsByUserAndStatusIn(eq(user), anyList());
        }

        @Test
        @DisplayName("mixed empty multipart part is rejected before storage")
        void apply_mixedEmptyPart_rejectedBeforeStorage() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(certificationRepository.existsByUserAndStatusIn(eq(user), anyList())).willReturn(false);

            assertThatThrownBy(() -> certificationService.apply(
                    buildUserDetails(1L, UserRole.USER),
                    List.of(
                            new MockMultipartFile("documents", "valid.pdf", "application/pdf", pdfBytes()),
                            new MockMultipartFile("documents", "empty.pdf", "application/pdf", new byte[0]))))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.INVALID_VALID));

            verify(storageMutationCoordinator, never()).storeAll(any(), any(), anyList(), anyString());
        }

        @Test
        @DisplayName("실패 - INDIVIDUAL 회원 신청 시 RESOURCE_NOT_ACCESS")
        void apply_notBusinessMember() {
            User user = buildUser(1L, UserRole.USER, UserType.INDIVIDUAL);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            assertThatThrownBy(() -> certificationService.apply(
                    buildUserDetails(1L, UserRole.USER), List.of()))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_ACCESS));
        }

        @Test
        @DisplayName("실패 - RESOURCE_DUPLICATE 시 HTTP 409 Conflict 반환")
        void apply_duplicateReturns409() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(certificationRepository.existsByUserAndStatusIn(eq(user), anyList()))
                    .willReturn(true);

            assertThatThrownBy(() -> certificationService.apply(
                    buildUserDetails(1L, UserRole.USER), List.of()))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(BUSINESS_ERROR.RESOURCE_DUPLICATE);
                        assertThat(be.getStatus().value()).isEqualTo(409);
                    });
        }

        @Test
        @DisplayName("실패 - PENDING 신청 이미 존재 시 RESOURCE_DUPLICATE")
        void apply_duplicatePending() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(certificationRepository.existsByUserAndStatusIn(eq(user), anyList()))
                    .willReturn(true);

            assertThatThrownBy(() -> certificationService.apply(
                    buildUserDetails(1L, UserRole.USER), List.of()))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.RESOURCE_DUPLICATE));
        }

        @Test
        @DisplayName("실패 - APPROVED 신청 이미 존재 시 RESOURCE_DUPLICATE")
        void apply_duplicateApproved() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(certificationRepository.existsByUserAndStatusIn(eq(user), anyList()))
                    .willReturn(true);

            assertThatThrownBy(() -> certificationService.apply(
                    buildUserDetails(1L, UserRole.USER), List.of()))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.RESOURCE_DUPLICATE));
        }

        @Test
        @DisplayName("실패 - REVISION_REQUESTED 신청 이미 존재 시 RESOURCE_DUPLICATE")
        void apply_duplicateRevisionRequested() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(certificationRepository.existsByUserAndStatusIn(eq(user), anyList()))
                    .willReturn(true);

            assertThatThrownBy(() -> certificationService.apply(
                    buildUserDetails(1L, UserRole.USER), List.of()))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.RESOURCE_DUPLICATE));
        }
    }

    // ── 13.2 resubmit ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("resubmit()")
    class Resubmit {

        @Test
        @DisplayName("성공 - REVISION_REQUESTED 상태에서 보완 서류 재제출")
        void resubmit_success() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            CompanyCertification cert = buildCertification(1L, user,
                    CompanyCertificationStatus.REVISION_REQUESTED, "/uploads/company-docs/1/old/");

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(certificationRepository.findTopByUserOrderByCreatedAtDescIdDesc(user)).willReturn(Optional.of(cert));
            given(storageMutationCoordinator.storeAll(
                    eq(StorageDomain.COMPANY_CERTIFICATION),
                    eq(StorageRoot.PRIVATE),
                    anyList(),
                    startsWith("company-docs/1/")))
                    .willReturn(List.of("company-docs/1/new/doc.pdf"));

            List<MultipartFile> documents = List.of(
                    new MockMultipartFile("documents", "doc.pdf",
                            "application/pdf", pdfBytes()));

            CompanyCertificationResponse result = certificationService.resubmit(
                    buildUserDetails(1L, UserRole.USER), documents);

            assertThat(result.status()).isEqualTo("PENDING");
            assertThat(result.adminNote()).isNull();
            assertThat(result.documents()).hasSize(1);
            verify(storageMutationCoordinator).storeAll(
                    eq(StorageDomain.COMPANY_CERTIFICATION),
                    eq(StorageRoot.PRIVATE),
                    anyList(),
                    startsWith("company-docs/1/"));
            InOrder lockOrder = inOrder(userRepository, certificationRepository);
            lockOrder.verify(userRepository).findByIdForUpdate(1L);
            lockOrder.verify(certificationRepository).findByUserForUpdate(eq(user), any());
        }

        @Test
        @DisplayName("성공 - PNG 인증 이미지는 canonical JPEG로 변환 후 PRIVATE 저장")
        void apply_imageDocument_usesCanonicalImageAndPrivateStorage() throws Exception {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            MockMultipartFile original = new MockMultipartFile(
                    "documents", "biz.png", "image/png", pngSignatureBytes());
            MockMultipartFile canonical = new MockMultipartFile(
                    "documents", "thumbnail.jpg", "image/jpeg", new byte[]{1, 2, 3});

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(certificationRepository.existsByUserAndStatusIn(eq(user), anyList()))
                    .willReturn(false);
            given(canonicalImageService.canonicalizeThumbnail(original)).willReturn(canonical);
            given(storageMutationCoordinator.storeAll(
                    eq(StorageDomain.COMPANY_CERTIFICATION),
                    eq(StorageRoot.PRIVATE),
                    argThat(files -> files.size() == 1 && files.get(0) == canonical),
                    startsWith("company-docs/1/")))
                    .willReturn(List.of("company-docs/1/canonical.jpg"));
            given(certificationRepository.save(any(CompanyCertification.class)))
                    .willAnswer(invocation -> {
                        CompanyCertification certification = invocation.getArgument(0);
                        ReflectionTestUtils.setField(certification, "id", 1L);
                        return certification;
                    });

            CompanyCertificationResponse result = certificationService.apply(
                    buildUserDetails(1L, UserRole.USER), List.of(original));

            assertThat(result.documents().get(0).originalFilename()).isEqualTo("biz.png");
            assertThat(result.documents().get(0).contentType()).isEqualTo("image/jpeg");
            verify(canonicalImageService).canonicalizeThumbnail(original);
        }

        @Test
        @DisplayName("실패 - PDF trailing payload는 DB 저장 전에 거부")
        void apply_pdfTrailingPayload_rejectedBeforeStorage() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(certificationRepository.existsByUserAndStatusIn(eq(user), anyList()))
                    .willReturn(false);

            assertThatThrownBy(() -> certificationService.apply(
                    buildUserDetails(1L, UserRole.USER),
                    List.of(new MockMultipartFile("documents", "doc.pdf",
                            "application/pdf", "%PDF-1.7\n%%EOF<script>".getBytes()))))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.INVALID_VALID));
            verify(storageMutationCoordinator, never()).storeAll(any(), any(), anyList(), anyString());
            verify(certificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - path-like filename은 DB 저장 전에 거부")
        void apply_pathLikeFilename_rejectedBeforeStorage() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(certificationRepository.existsByUserAndStatusIn(eq(user), anyList()))
                    .willReturn(false);

            assertThatThrownBy(() -> certificationService.apply(
                    buildUserDetails(1L, UserRole.USER),
                    List.of(new MockMultipartFile("documents", "../doc.pdf",
                            "application/pdf", pdfBytes()))))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.INVALID_VALID));
            verify(storageMutationCoordinator, never()).storeAll(any(), any(), anyList(), anyString());
            verify(certificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - HWP/HWPX/DOC/DOCX baseline 외 형식은 거부")
        void apply_officeAndHwpDocuments_rejected() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(certificationRepository.existsByUserAndStatusIn(eq(user), anyList()))
                    .willReturn(false);

            for (String filename : List.of("doc.hwp", "doc.hwpx", "doc.doc", "doc.docx")) {
                assertThatThrownBy(() -> certificationService.apply(
                        buildUserDetails(1L, UserRole.USER),
                        List.of(new MockMultipartFile("documents", filename,
                                "application/octet-stream", new byte[]{1, 2, 3}))))
                        .isInstanceOf(BusinessException.class)
                        .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                                .isEqualTo(BUSINESS_ERROR.INVALID_VALID));
            }
            verify(storageMutationCoordinator, never()).storeAll(any(), any(), anyList(), anyString());
        }

        @Test
        @DisplayName("실패 - aggregate 50MiB 초과는 DB 저장 전에 거부")
        void apply_aggregateTooLarge_rejectedBeforeStorage() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(certificationRepository.existsByUserAndStatusIn(eq(user), anyList()))
                    .willReturn(false);

            List<MultipartFile> documents = java.util.stream.IntStream.range(0, 3)
                    .mapToObj(index -> new MockMultipartFile(
                            "documents",
                            "doc" + index + ".pdf",
                            "application/pdf",
                            new byte[(int) ValidationConstants.CERT_DOC_MAX_SIZE_BYTES]))
                    .map(MultipartFile.class::cast)
                    .toList();

            assertThatThrownBy(() -> certificationService.apply(
                    buildUserDetails(1L, UserRole.USER), documents))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.IO_LARGE));
            verify(storageMutationCoordinator, never()).storeAll(any(), any(), anyList(), anyString());
            verify(certificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("성공 - 재제출 시 기존 파일은 커밋 후 삭제")
        void resubmit_deletesPreviousFilesAfterCommit() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            CompanyCertification cert = buildCertification(1L, user,
                    CompanyCertificationStatus.REVISION_REQUESTED, "/uploads/company-docs/1/old/");
            cert.addDocument("old.pdf", "company-docs/1/old/old.pdf", "application/pdf", 3L);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(certificationRepository.findTopByUserOrderByCreatedAtDescIdDesc(user)).willReturn(Optional.of(cert));
            given(storageMutationCoordinator.storeAll(
                    eq(StorageDomain.COMPANY_CERTIFICATION),
                    eq(StorageRoot.PRIVATE),
                    anyList(),
                    startsWith("company-docs/1/")))
                    .willReturn(List.of("company-docs/1/new/new.pdf"));

            CompanyCertificationResponse result = certificationService.resubmit(
                    buildUserDetails(1L, UserRole.USER),
                    List.of(new MockMultipartFile("documents", "new.pdf",
                            "application/pdf", pdfBytes())));

            assertThat(result.status()).isEqualTo("PENDING");
            verify(storageMutationCoordinator).deleteAfterCommit(
                    StorageDomain.COMPANY_CERTIFICATION,
                    StorageRoot.PRIVATE,
                    List.of("company-docs/1/old/old.pdf"));
        }

        @Test
        @DisplayName("성공 - 재제출 롤백 시 새로 저장한 파일 정리")
        void resubmit_deletesNewFilesAfterRollback() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            CompanyCertification cert = buildCertification(1L, user,
                    CompanyCertificationStatus.REVISION_REQUESTED, "/uploads/company-docs/1/old/");
            cert.addDocument("old.pdf", "company-docs/1/old/old.pdf", "application/pdf", 3L);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(certificationRepository.findTopByUserOrderByCreatedAtDescIdDesc(user)).willReturn(Optional.of(cert));
            given(storageMutationCoordinator.storeAll(
                    eq(StorageDomain.COMPANY_CERTIFICATION),
                    eq(StorageRoot.PRIVATE),
                    anyList(),
                    startsWith("company-docs/1/")))
                    .willReturn(List.of("company-docs/1/new/new.pdf"));

            certificationService.resubmit(
                    buildUserDetails(1L, UserRole.USER),
                    List.of(new MockMultipartFile("documents", "new.pdf",
                            "application/pdf", pdfBytes())));

            verify(storageMutationCoordinator).deleteAfterCommit(
                    StorageDomain.COMPANY_CERTIFICATION,
                    StorageRoot.PRIVATE,
                    List.of("company-docs/1/old/old.pdf"));
        }

        @Test
        @DisplayName("실패 - REVISION_REQUESTED가 아니면 INVALID_STATE_TRANSITION")
        void resubmit_invalidState() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            CompanyCertification cert = buildCertification(1L, user,
                    CompanyCertificationStatus.PENDING, "/uploads/company-docs/1/");

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(certificationRepository.findTopByUserOrderByCreatedAtDescIdDesc(user)).willReturn(Optional.of(cert));

            assertThatThrownBy(() -> certificationService.resubmit(
                    buildUserDetails(1L, UserRole.USER), List.of()))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));
        }
    }

    // ── 13.2 getMyStatus ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("getMyStatus()")
    class GetMyStatus {

        @Test
        @DisplayName("성공 - 신청 존재 시 Response 반환")
        void getMyStatus_exists() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            CompanyCertification cert = buildCertification(1L, user,
                    CompanyCertificationStatus.PENDING, "/uploads/company-docs/1/");

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(certificationRepository.findTopByUserOrderByCreatedAtDescIdDesc(user)).willReturn(Optional.of(cert));

            CompanyCertificationResponse result = certificationService.getMyStatus(
                    buildUserDetails(1L, UserRole.USER));

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.status()).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("실패 - 신청 미존재 시 RESOURCE_NOT_FOUND")
        void getMyStatus_notFound() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(certificationRepository.findTopByUserOrderByCreatedAtDescIdDesc(user)).willReturn(Optional.empty());

            assertThatThrownBy(() -> certificationService.getMyStatus(
                    buildUserDetails(1L, UserRole.USER)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        }
    }

    // ── 13.3 listAll ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("listAll()")
    class ListAll {

        @Test
        @DisplayName("성공 - 필터 없이 전체 목록 조회")
        void listAll_noFilter() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            CompanyCertification cert = buildCertification(1L, user,
                    CompanyCertificationStatus.PENDING, "/uploads/company-docs/1/");

            Page<CompanyCertification> page = new PageImpl<>(List.of(cert));
            given(certificationRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                    .willReturn(page);

            ResponseDTO<CompanyCertificationSummaryResponse> result =
                    certificationService.listAll(null, 1, 20);

            assertThat(result.getDataList()).hasSize(1);
            assertThat(result.getDataList().get(0).id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 status 문자열 → INVALID_ARGUMENT(400)")
        void listAll_invalidStatus() {
            assertThatThrownBy(() -> certificationService.listAll("INVALID_STATUS", 1, 20))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT);
                        assertThat(be.getStatus().value()).isEqualTo(400);
                    });
        }

        @Test
        @DisplayName("성공 - status 필터 적용")
        void listAll_withStatusFilter() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            CompanyCertification cert = buildCertification(1L, user,
                    CompanyCertificationStatus.PENDING, "/uploads/company-docs/1/");

            Page<CompanyCertification> page = new PageImpl<>(List.of(cert));
            given(certificationRepository.findByStatus(eq(CompanyCertificationStatus.PENDING),
                    any(org.springframework.data.domain.Pageable.class))).willReturn(page);

            ResponseDTO<CompanyCertificationSummaryResponse> result =
                    certificationService.listAll("PENDING", 1, 20);

            assertThat(result.getDataList()).hasSize(1);
        }
    }

    // ── 13.4 getDetail ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("getDetail()")
    class GetDetail {

        @Test
        @DisplayName("성공 - 상세 조회")
        void getDetail_success() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            CompanyCertification cert = buildCertification(10L, user,
                    CompanyCertificationStatus.APPROVED, "/uploads/company-docs/1/");

            given(certificationRepository.findById(10L)).willReturn(Optional.of(cert));

            CompanyCertificationResponse result = certificationService.getDetail(10L);

            assertThat(result.id()).isEqualTo(10L);
            assertThat(result.status()).isEqualTo("APPROVED");
        }

        @Test
        @DisplayName("실패 - 미존재 시 RESOURCE_NOT_FOUND")
        void getDetail_notFound() {
            given(certificationRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> certificationService.getDetail(99L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        }
    }

    // ── 13.5 processReview ───────────────────────────────────────────────────

    @Nested
    @DisplayName("processReview()")
    class ProcessReview {

        @Test
        @DisplayName("성공 - APPROVED: certificationCode 생성 + approvedAt 기록")
        void processReview_approve() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            CompanyCertification cert = buildCertification(10L, user,
                    CompanyCertificationStatus.PENDING, "/uploads/company-docs/1/");

            given(certificationRepository.findById(10L)).willReturn(Optional.of(cert));
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            CompanyCertificationResponse result = certificationService.processReview(10L,
                    buildUserDetails(1L, UserRole.ADMIN),
                    new CompanyCertificationReviewRequest(
                            CompanyCertificationStatus.APPROVED, "서류 확인 완료"));

            assertThat(result.status()).isEqualTo("APPROVED");
            assertThat(result.certificationCode()).startsWith("BIZ-");
            assertThat(result.approvedAt()).isNotNull();
            ArgumentCaptor<CompanyCertificationAuditLog> auditCaptor =
                    ArgumentCaptor.forClass(CompanyCertificationAuditLog.class);
            verify(auditLogRepository).save(auditCaptor.capture());
            assertThat(auditCaptor.getValue().getAction())
                    .isEqualTo(CompanyCertificationAuditAction.REVIEWED);
            assertThat(auditCaptor.getValue().getActorUser()).isEqualTo(user);
            assertThat(auditCaptor.getValue().getFromStatus()).isEqualTo("PENDING");
            assertThat(auditCaptor.getValue().getToStatus()).isEqualTo("APPROVED");
            InOrder lockOrder = inOrder(userRepository, certificationRepository);
            lockOrder.verify(userRepository).findByIdForUpdate(1L);
            lockOrder.verify(certificationRepository).findByIdForUpdate(10L);
            assertThat(result.adminNote()).isEqualTo("서류 확인 완료");
        }

        @Test
        @DisplayName("성공 - REVISION_REQUESTED: adminNote 저장")
        void processReview_revisionRequested() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            CompanyCertification cert = buildCertification(10L, user,
                    CompanyCertificationStatus.PENDING, "/uploads/company-docs/1/");

            given(certificationRepository.findById(10L)).willReturn(Optional.of(cert));
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            CompanyCertificationResponse result = certificationService.processReview(10L,
                    buildUserDetails(1L, UserRole.ADMIN),
                    new CompanyCertificationReviewRequest(
                            CompanyCertificationStatus.REVISION_REQUESTED, "서류 보완 필요"));

            assertThat(result.status()).isEqualTo("REVISION_REQUESTED");
            assertThat(result.adminNote()).isEqualTo("서류 보완 필요");
            assertThat(result.certificationCode()).isNull();
            assertThat(result.approvedAt()).isNull();
        }

        @Test
        @DisplayName("성공 - REJECTED")
        void processReview_reject() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            CompanyCertification cert = buildCertification(10L, user,
                    CompanyCertificationStatus.PENDING, "/uploads/company-docs/1/");

            given(certificationRepository.findById(10L)).willReturn(Optional.of(cert));
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            CompanyCertificationResponse result = certificationService.processReview(10L,
                    buildUserDetails(1L, UserRole.ADMIN),
                    new CompanyCertificationReviewRequest(
                            CompanyCertificationStatus.REJECTED, "부적격"));

            assertThat(result.status()).isEqualTo("REJECTED");
            assertThat(result.adminNote()).isEqualTo("부적격");
            assertThat(result.certificationCode()).isNull();
            assertThat(result.approvedAt()).isNull();
        }

        @Test
        @DisplayName("실패 - 미존재 인증 심사 → RESOURCE_NOT_FOUND")
        void processReview_blankRejectionReason_rejectedBeforeMutation() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            CompanyCertification cert = buildCertification(10L, user,
                    CompanyCertificationStatus.PENDING, "/uploads/company-docs/1/");
            given(certificationRepository.findById(10L)).willReturn(Optional.of(cert));
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            assertThatThrownBy(() -> certificationService.processReview(
                    10L,
                    buildUserDetails(1L, UserRole.ADMIN),
                    new CompanyCertificationReviewRequest(CompanyCertificationStatus.REJECTED, "  ")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.INVALID_VALID));

            assertThat(cert.getStatus()).isEqualTo(CompanyCertificationStatus.PENDING);
            verifyNoInteractions(auditLogRepository);
        }

        @Test
        @DisplayName("missing certification is rejected")
        void processReview_notFound() {
            given(certificationRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> certificationService.processReview(99L,
                    buildUserDetails(1L, UserRole.ADMIN),
                    new CompanyCertificationReviewRequest(
                            CompanyCertificationStatus.APPROVED, null)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("guarded document download records only actor, target, and opaque document ID")
    void downloadDocument_recordsMinimumAuditEvidence() {
        User applicant = buildUser(1L, UserRole.USER, UserType.BUSINESS);
        User admin = buildUser(2L, UserRole.ADMIN, UserType.INDIVIDUAL);
        CompanyCertification cert = buildCertification(10L, applicant,
                CompanyCertificationStatus.PENDING, "/uploads/company-docs/1/");
        cert.addDocument("license.pdf", "company-docs/1/license.pdf", "application/pdf", 4L);
        CompanyCertificationDocument document = cert.getDocuments().get(0);
        ReflectionTestUtils.setField(document, "id", 77L);
        given(documentRepository.findByIdAndCertificationId(77L, 10L)).willReturn(Optional.of(document));
        given(userRepository.findById(2L)).willReturn(Optional.of(admin));
        given(storageService.loadAsResource(StorageRoot.PRIVATE, "company-docs/1/license.pdf"))
                .willReturn(new ByteArrayResource(new byte[]{1, 2, 3, 4}));

        certificationService.downloadDocument(10L, 77L, buildUserDetails(2L, UserRole.ADMIN));

        ArgumentCaptor<CompanyCertificationAuditLog> auditCaptor =
                ArgumentCaptor.forClass(CompanyCertificationAuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getAction())
                .isEqualTo(CompanyCertificationAuditAction.DOCUMENT_ACCESS_GRANTED);
        assertThat(auditCaptor.getValue().getActorUser()).isEqualTo(admin);
        assertThat(auditCaptor.getValue().getCertification()).isEqualTo(cert);
        assertThat(auditCaptor.getValue().getDocumentId()).isEqualTo(77L);
        assertThat(auditCaptor.getValue().getFromStatus()).isNull();
        assertThat(auditCaptor.getValue().getToStatus()).isNull();
    }

    @Test
    @DisplayName("company certification identity lookups fail closed")
    void identityLookupsFailClosed() {
        assertThatThrownBy(() -> certificationService.getMyStatus(null))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> certificationService.getMyStatus(
                CustomUserDetails.builder().role(UserRole.USER).build()))
                .isInstanceOf(BusinessException.class);

        given(userRepository.findById(404L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> certificationService.getMyStatus(buildUserDetails(404L, UserRole.USER)))
                .isInstanceOf(BusinessException.class);

        assertThatThrownBy(() -> certificationService.apply(null, List.of()))
                .isInstanceOf(BusinessException.class);

        User applicant = buildUser(1L, UserRole.USER, UserType.BUSINESS);
        CompanyCertification certification = buildCertification(
                10L, applicant, CompanyCertificationStatus.PENDING, "company-docs/1/");
        given(certificationRepository.findById(10L)).willReturn(Optional.of(certification));
        assertThatThrownBy(() -> certificationService.processReview(
                10L,
                null,
                new CompanyCertificationReviewRequest(CompanyCertificationStatus.APPROVED, null)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("document collection policy rejects null, empty, and excessive batches")
    void documentCollectionPolicyRejectsInvalidBatches() {
        User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(certificationRepository.existsByUserAndStatusIn(eq(user), anyList())).willReturn(false);

        assertThatThrownBy(() -> certificationService.apply(buildUserDetails(1L, UserRole.USER), null))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> certificationService.apply(buildUserDetails(1L, UserRole.USER), List.of()))
                .isInstanceOf(BusinessException.class);

        List<MultipartFile> tooMany = java.util.stream.IntStream
                .rangeClosed(1, ValidationConstants.CERT_DOC_MAX_COUNT + 1)
                .mapToObj(index -> new MockMultipartFile(
                        "documents",
                        "doc-" + index + ".pdf",
                        "application/pdf",
                        pdfBytes()))
                .map(MultipartFile.class::cast)
                .toList();
        assertThatThrownBy(() -> certificationService.apply(buildUserDetails(1L, UserRole.USER), tooMany))
                .isInstanceOf(BusinessException.class);
        verify(storageMutationCoordinator, never()).storeAll(any(), any(), anyList(), anyString());
    }

    @Test
    @DisplayName("document policy rejects unsafe names, extensions, content, MIME, and read failures")
    void documentPolicyRejectsUnsafeEvidence() throws IOException {
        User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(certificationRepository.existsByUserAndStatusIn(eq(user), anyList())).willReturn(false);

        List<MultipartFile> invalidDocuments = List.of(
                new MockMultipartFile("documents", null, "application/pdf", pdfBytes()),
                new MockMultipartFile("documents", " ", "application/pdf", pdfBytes()),
                new MockMultipartFile("documents", "x".repeat(ValidationConstants.CERT_DOC_FILENAME_MAX + 1) + ".pdf",
                        "application/pdf", pdfBytes()),
                new MockMultipartFile("documents", "folder\\doc.pdf", "application/pdf", pdfBytes()),
                new MockMultipartFile("documents", "drive:doc.pdf", "application/pdf", pdfBytes()),
                new MockMultipartFile("documents", ".", "application/pdf", pdfBytes()),
                new MockMultipartFile("documents", "..", "application/pdf", pdfBytes()),
                new MockMultipartFile("documents", "document", "application/pdf", pdfBytes()),
                new MockMultipartFile("documents", "document.", "application/pdf", pdfBytes()),
                new MockMultipartFile("documents", "document.txt", "text/plain", pdfBytes()),
                new MockMultipartFile("documents", "document.png", "image/png", pdfBytes()),
                new MockMultipartFile("documents", "document.pdf", "image/png", pdfBytes()),
                new MockMultipartFile("documents", "document.pdf", "application/pdf", new byte[] {1, 2, 3}));

        invalidDocuments.forEach(document -> assertThatThrownBy(() -> certificationService.apply(
                buildUserDetails(1L, UserRole.USER), List.of(document)))
                .isInstanceOf(BusinessException.class));

        MultipartFile oversized = mock(MultipartFile.class);
        given(oversized.isEmpty()).willReturn(false);
        given(oversized.getSize()).willReturn(ValidationConstants.CERT_DOC_MAX_SIZE_BYTES + 1);
        assertThatThrownBy(() -> certificationService.apply(
                buildUserDetails(1L, UserRole.USER), List.of(oversized)))
                .isInstanceOf(BusinessException.class);

        MultipartFile unreadable = mock(MultipartFile.class);
        given(unreadable.isEmpty()).willReturn(false);
        given(unreadable.getSize()).willReturn(100L);
        given(unreadable.getOriginalFilename()).willReturn("document.pdf");
        given(unreadable.getBytes()).willThrow(new IOException("unreadable"));
        assertThatThrownBy(() -> certificationService.apply(
                buildUserDetails(1L, UserRole.USER), List.of(unreadable)))
                .isInstanceOf(BusinessException.class);
        verify(storageMutationCoordinator, never()).storeAll(any(), any(), anyList(), anyString());
    }

    @Test
    @DisplayName("PDF whitespace and JPEG documents are accepted only after verified normalization")
    void verifiedPdfAndJpegEvidenceIsAccepted() throws IOException {
        User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(certificationRepository.existsByUserAndStatusIn(eq(user), anyList())).willReturn(false);
        byte[] pdfWithAllowedWhitespace = concat(
                "%PDF-1.7\n%%EOF".getBytes(StandardCharsets.US_ASCII),
                new byte[] {0x00, 0x09, 0x0A, 0x0C, 0x0D, 0x20});
        MockMultipartFile pdf = new MockMultipartFile(
                "documents", " verified.PDF ", null, pdfWithAllowedWhitespace);
        MockMultipartFile jpeg = new MockMultipartFile(
                "documents", "photo.jpeg", "application/octet-stream",
                new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00});
        MockMultipartFile canonical = new MockMultipartFile(
                "documents", "photo.jpg", "image/jpeg", new byte[] {1, 2, 3});
        given(canonicalImageService.canonicalizeThumbnail(jpeg)).willReturn(canonical);
        given(storageMutationCoordinator.storeAll(
                eq(StorageDomain.COMPANY_CERTIFICATION),
                eq(StorageRoot.PRIVATE),
                anyList(),
                startsWith("company-docs/1/")))
                .willReturn(List.of("company-docs/1/document.pdf", "company-docs/1/photo.jpg"));
        given(certificationRepository.save(any(CompanyCertification.class)))
                .willAnswer(invocation -> {
                    CompanyCertification saved = invocation.getArgument(0);
                    ReflectionTestUtils.setField(saved, "id", 99L);
                    return saved;
                });

        CompanyCertificationResponse result = certificationService.apply(
                buildUserDetails(1L, UserRole.USER), List.of(pdf, jpeg));

        assertThat(result.documents()).hasSize(2);
        assertThat(result.documents().get(0).originalFilename()).isEqualTo("verified.PDF");
        assertThat(result.documents().get(0).contentType()).isEqualTo("application/pdf");
        assertThat(result.documents().get(1).contentType()).isEqualTo("image/jpeg");
        verify(canonicalImageService).canonicalizeThumbnail(jpeg);
    }

    @Test
    @DisplayName("review note validation rejects oversized notes and normalizes blank approval notes")
    void reviewNoteValidation() {
        User applicant = buildUser(1L, UserRole.USER, UserType.BUSINESS);
        User admin = buildUser(2L, UserRole.ADMIN, UserType.INDIVIDUAL);
        CompanyCertification oversized = buildCertification(
                10L, applicant, CompanyCertificationStatus.PENDING, "company-docs/1/");
        given(certificationRepository.findById(10L)).willReturn(Optional.of(oversized));
        given(userRepository.findById(1L)).willReturn(Optional.of(applicant));
        given(userRepository.findById(2L)).willReturn(Optional.of(admin));

        assertThatThrownBy(() -> certificationService.processReview(
                10L,
                buildUserDetails(2L, UserRole.ADMIN),
                new CompanyCertificationReviewRequest(
                        CompanyCertificationStatus.REJECTED,
                        "x".repeat(ValidationConstants.CERTIFICATION_REVIEW_NOTE_MAX + 1))))
                .isInstanceOf(BusinessException.class);

        CompanyCertification approved = buildCertification(
                11L, applicant, CompanyCertificationStatus.PENDING, "company-docs/1/");
        given(certificationRepository.findById(11L)).willReturn(Optional.of(approved));
        CompanyCertificationResponse result = certificationService.processReview(
                11L,
                buildUserDetails(2L, UserRole.ADMIN),
                new CompanyCertificationReviewRequest(CompanyCertificationStatus.APPROVED, "   "));

        assertThat(result.status()).isEqualTo("APPROVED");
        assertThat(result.adminNote()).isNull();
    }

    private User buildUser(Long id, UserRole role, UserType userType) {
        User user = User.builder()
                .email("user" + id + "@test.com")
                .nickname("user" + id)
                .password("pw")
                .userType(userType)
                .role(role)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private CompanyCertification buildCertification(Long id, User user,
                                                     CompanyCertificationStatus status,
                                                     String documentPath) {
        CompanyCertification cert = CompanyCertification.builder()
                .user(user)
                .status(status)
                .documentPath(documentPath)
                .build();
        ReflectionTestUtils.setField(cert, "id", id);
        return cert;
    }

    private CustomUserDetails buildUserDetails(Long id, UserRole role) {
        return CustomUserDetails.builder()
                .id(id)
                .email("user" + id + "@test.com")
                .password("pw")
                .role(role)
                .isDeleted(false)
                .isProfileComplete(true)
                .build();
    }

    private byte[] pdfBytes() {
        return "%PDF-1.7\n1 0 obj\n<<>>\nendobj\n%%EOF\n".getBytes();
    }

    private byte[] pngSignatureBytes() {
        return new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00
        };
    }

    private byte[] concat(byte[] left, byte[] right) {
        byte[] result = new byte[left.length + right.length];
        System.arraycopy(left, 0, result, 0, left.length);
        System.arraycopy(right, 0, result, left.length, right.length);
        return result;
    }
}

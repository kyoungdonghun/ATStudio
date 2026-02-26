package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.certification.CompanyCertificationResponse;
import com.atstudio.atstudio.dto.certification.CompanyCertificationReviewRequest;
import com.atstudio.atstudio.dto.certification.CompanyCertificationSummaryResponse;
import com.atstudio.atstudio.entity.CompanyCertification;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.CompanyCertificationStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.CompanyCertificationRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.storage.StorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyCertificationService 단위 테스트")
class CompanyCertificationServiceTest {

    @Mock CompanyCertificationRepository certificationRepository;
    @Mock UserRepository userRepository;
    @Mock StorageService storageService;

    @InjectMocks CompanyCertificationService certificationService;

    // ── 13.1 apply ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("apply()")
    class Apply {

        @Test
        @DisplayName("성공 - BUSINESS 회원 정상 신청")
        void apply_success() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            CompanyCertification saved = buildCertification(1L, user,
                    CompanyCertificationStatus.PENDING, "/uploads/company-docs/1/");

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(certificationRepository.existsByUserAndStatusIn(eq(user), anyList()))
                    .willReturn(false);
            given(storageService.store(any(MultipartFile.class), eq("company-docs/1")))
                    .willReturn("company-docs/1/doc.pdf");
            given(certificationRepository.save(any(CompanyCertification.class))).willReturn(saved);

            List<MultipartFile> documents = List.of(
                    new MockMultipartFile("documents", "doc.pdf",
                            "application/pdf", new byte[]{1, 2, 3}));

            CompanyCertificationResponse result = certificationService.apply(
                    buildUserDetails(1L, UserRole.USER), documents);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.status()).isEqualTo("PENDING");
            assertThat(result.documentPath()).isEqualTo("/uploads/company-docs/1/");
            verify(storageService).store(any(MultipartFile.class), eq("company-docs/1"));
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
            given(certificationRepository.findByUser(user)).willReturn(Optional.of(cert));

            CompanyCertificationResponse result = certificationService.getMyStatus(
                    buildUserDetails(1L, UserRole.USER));

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.status()).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("성공 - 신청 미존재 시 null 반환")
        void getMyStatus_notFound() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(certificationRepository.findByUser(user)).willReturn(Optional.empty());

            CompanyCertificationResponse result = certificationService.getMyStatus(
                    buildUserDetails(1L, UserRole.USER));

            assertThat(result).isNull();
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

            CompanyCertificationResponse result = certificationService.processReview(10L,
                    new CompanyCertificationReviewRequest(
                            CompanyCertificationStatus.APPROVED, "서류 확인 완료"));

            assertThat(result.status()).isEqualTo("APPROVED");
            assertThat(result.certificationCode()).startsWith("BIZ-");
            assertThat(result.approvedAt()).isNotNull();
            assertThat(result.adminNote()).isEqualTo("서류 확인 완료");
        }

        @Test
        @DisplayName("성공 - REVISION_REQUESTED: adminNote 저장")
        void processReview_revisionRequested() {
            User user = buildUser(1L, UserRole.USER, UserType.BUSINESS);
            CompanyCertification cert = buildCertification(10L, user,
                    CompanyCertificationStatus.PENDING, "/uploads/company-docs/1/");

            given(certificationRepository.findById(10L)).willReturn(Optional.of(cert));

            CompanyCertificationResponse result = certificationService.processReview(10L,
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

            CompanyCertificationResponse result = certificationService.processReview(10L,
                    new CompanyCertificationReviewRequest(
                            CompanyCertificationStatus.REJECTED, "부적격"));

            assertThat(result.status()).isEqualTo("REJECTED");
            assertThat(result.adminNote()).isEqualTo("부적격");
            assertThat(result.certificationCode()).isNull();
            assertThat(result.approvedAt()).isNull();
        }

        @Test
        @DisplayName("실패 - 미존재 인증 심사 → RESOURCE_NOT_FOUND")
        void processReview_notFound() {
            given(certificationRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> certificationService.processReview(99L,
                    new CompanyCertificationReviewRequest(
                            CompanyCertificationStatus.APPROVED, null)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

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
}

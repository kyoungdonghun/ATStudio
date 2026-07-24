package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.certification.CompanyCertificationResponse;
import com.atstudio.atstudio.entity.CompanyCertification;
import com.atstudio.atstudio.entity.User;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("WI-019 Company certification independent security verification")
class CompanyCertificationSecurityVerificationTest {

    @Mock CompanyCertificationRepository certificationRepository;
    @Mock CompanyCertificationDocumentRepository documentRepository;
    @Mock CompanyCertificationAuditLogRepository auditLogRepository;
    @Mock UserRepository userRepository;
    @Mock StorageService storageService;
    @Mock StorageMutationCoordinator storageMutationCoordinator;

    @Test
    @DisplayName("PNG polyglot payload is re-encoded to canonical JPEG before private storage")
    void apply_pngPolyglot_isCanonicalizedBeforePrivateStore() throws Exception {
        CompanyCertificationService service = service();
        User user = businessUser(1L);
        MockMultipartFile original = new MockMultipartFile(
                "documents",
                "biz.png",
                "image/png",
                pngWithTrailingPayload());

        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(certificationRepository.existsByUserAndStatusIn(eq(user), anyList())).willReturn(false);
        given(storageMutationCoordinator.storeAll(
                eq(StorageDomain.COMPANY_CERTIFICATION),
                eq(StorageRoot.PRIVATE),
                anyList(),
                anyString()))
                .willReturn(List.of("company-docs/1/canonical.jpg"));
        given(certificationRepository.save(any(CompanyCertification.class)))
                .willAnswer(invocation -> {
                    CompanyCertification certification = invocation.getArgument(0);
                    ReflectionTestUtils.setField(certification, "id", 1L);
                    return certification;
                });

        CompanyCertificationResponse response = service.apply(actor(1L), List.of(original));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MultipartFile>> filesCaptor = ArgumentCaptor.forClass(List.class);
        verify(storageMutationCoordinator).storeAll(
                eq(StorageDomain.COMPANY_CERTIFICATION),
                eq(StorageRoot.PRIVATE),
                filesCaptor.capture(),
                anyString());

        MultipartFile stored = filesCaptor.getValue().get(0);
        assertThat(stored.getContentType()).isEqualTo("image/jpeg");
        assertThat(stored.getOriginalFilename()).isEqualTo("thumbnail.jpg");
        assertThat(stored.getBytes()).startsWith((byte) 0xFF, (byte) 0xD8, (byte) 0xFF);
        assertThat(new String(stored.getBytes(), StandardCharsets.ISO_8859_1))
                .doesNotContain("<script>")
                .doesNotContain("</svg>");
        assertThat(response.documents()).singleElement().satisfies(document -> {
            assertThat(document.originalFilename()).isEqualTo("biz.png");
            assertThat(document.contentType()).isEqualTo("image/jpeg");
        });
    }

    @Test
    @DisplayName("Forged PDF extension plus PNG bytes is rejected before storage")
    void apply_pdfExtensionWithPngBytes_rejectedBeforeStorage() throws Exception {
        CompanyCertificationService service = service();
        User user = businessUser(1L);
        MockMultipartFile forged = new MockMultipartFile(
                "documents",
                "biz.pdf",
                "application/pdf",
                pngBytes());

        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(certificationRepository.existsByUserAndStatusIn(eq(user), anyList())).willReturn(false);

        assertThatThrownBy(() -> service.apply(actor(1L), List.of(forged)))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_VALID));

        verify(storageMutationCoordinator, never()).storeAll(any(), any(), anyList(), anyString());
        verify(certificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Truncated PNG signature is rejected before DB mutation")
    void apply_truncatedPng_rejectedBeforeStorage() {
        CompanyCertificationService service = service();
        User user = businessUser(1L);
        MockMultipartFile truncated = new MockMultipartFile(
                "documents",
                "biz.png",
                "image/png",
                new byte[]{
                        (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00
                });

        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(certificationRepository.existsByUserAndStatusIn(eq(user), anyList())).willReturn(false);

        assertThatThrownBy(() -> service.apply(actor(1L), List.of(truncated)))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_VALID));

        verify(storageMutationCoordinator, never()).storeAll(any(), any(), anyList(), anyString());
        verify(certificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Changed account cannot read its historical BUSINESS certification")
    void getMyStatus_changedAccountCannotReadHistoricalCertification() {
        CompanyCertificationService service = service();
        User changedAccount = individualUser(1L);
        CompanyCertification historicalCertification = CompanyCertification.builder()
                .user(changedAccount)
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(changedAccount));
        lenient().when(certificationRepository.findTopByUserOrderByCreatedAtDescIdDesc(changedAccount))
                .thenReturn(Optional.of(historicalCertification));

        assertThatThrownBy(() -> service.getMyStatus(actor(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_ACCESS));

        verifyNoInteractions(certificationRepository);
    }

    private CompanyCertificationService service() {
        return new CompanyCertificationService(
                certificationRepository,
                documentRepository,
                auditLogRepository,
                userRepository,
                storageService,
                storageMutationCoordinator,
                new CanonicalImageService());
    }

    private User businessUser(Long id) {
        User user = User.builder()
                .email("biz@test.com")
                .nickname("biz-user")
                .password("pw")
                .role(UserRole.USER)
                .userType(UserType.BUSINESS)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private User individualUser(Long id) {
        User user = User.builder()
                .email("member@test.com")
                .nickname("member")
                .password("pw")
                .role(UserRole.USER)
                .userType(UserType.INDIVIDUAL)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private CustomUserDetails actor(Long id) {
        return CustomUserDetails.builder()
                .id(id)
                .email("biz@test.com")
                .password("pw")
                .role(UserRole.USER)
                .isDeleted(false)
                .isProfileComplete(true)
                .build();
    }

    private byte[] pngWithTrailingPayload() throws Exception {
        byte[] png = pngBytes();
        byte[] payload = "<script>alert(1)</script></svg>".getBytes(StandardCharsets.UTF_8);
        byte[] combined = new byte[png.length + payload.length];
        System.arraycopy(png, 0, combined, 0, png.length);
        System.arraycopy(payload, 0, combined, png.length, payload.length);
        return combined;
    }

    private byte[] pngBytes() throws Exception {
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                image.setRGB(x, y, Color.BLUE.getRGB());
            }
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}

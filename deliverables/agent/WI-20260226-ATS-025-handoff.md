[WI HEADER]
WI ID: WI-20260226-ATS-025
REQ: REQ-20260226-ATS-007
Agent: se
Depends On: -
Blocks: WI-20260226-ATS-026

[WI SUMMARY]
Why: "Business License"라는 명칭이 음악 라이선스(Section 7)와 혼동되어 "Company Certification"으로 전면 리네이밍 후, Company Certification 5개 API 구현.
Scope (in/out):
  In:
    Phase 1 — 리네이밍 (docs + code, ~15개 파일)
    Phase 2 — Company Certification 5개 API 신규 구현 + 테스트
  Out:
    기존 deliverables/ 증거 팩 수정 (이력 보존)
    파일 실제 저장 구현 (StorageService.store() 호출로 처리)
DoD:
  - 전체 컴파일 오류 없음
  - 기존 384개 테스트 회귀 없음
  - 5개 새 API 동작
  - 신규 테스트 작성 (ServiceTest + ControllerTest)
Constraints/Forbidden:
  - Entity 직접 반환 금지 — DTO 변환 필수
  - @Transactional(readOnly=true) 클래스 레벨 표준
  - deliverables/ 기존 파일 수정 금지
  - `BUSINESS_LICENSE_REQUIRED` → `COMPANY_CERTIFICATION_REQUIRED` 한 번에 전부 수정 (참조 누락 시 컴파일 에러)

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] 리네이밍 후 컴파일 성공 + 기존 384개 테스트 전부 통과
  - [ ] POST /api/company-certifications: 201 Created (BUSINESS 회원만)
  - [ ] POST: 비BUSINESS 회원 → 403
  - [ ] POST: 이미 PENDING/APPROVED 신청 존재 → 409
  - [ ] GET /api/company-certifications/me: 200 OK (내 인증 상태)
  - [ ] GET /api/company-certifications: 200 OK (관리자만, 페이지네이션)
  - [ ] GET /api/company-certifications/{certificationId}: 200 OK (관리자만)
  - [ ] PUT /api/company-certifications/{certificationId}: 200 OK (관리자 심사)
  - [ ] PUT: APPROVED → certificationCode("BIZ-{UUID}") 자동생성 + approvedAt 기록
  - [ ] PUT: 일반 회원 접근 → 403
Quality:
  - [ ] CompanyCertificationServiceTest 신규 작성
  - [ ] CompanyCertificationControllerTest 신규 작성
  - [ ] 전체 테스트 0 failures

[INPUT POINTERS]
Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Standards - se):
- docs/standards/development-standards.md

Tier 1 (Policies - 인증/권한 포함):
- docs/policies/security-policy.md

REQ/Context Docs:
- deliverables/user/REQ-20260226-ATS-007.md
- docs/design/api-spec.md          ← Section 13 (Company Certification) + Section 6.3 에러코드
- docs/design/db-schema.md         ← 리네이밍 후 반영할 테이블 구조 확인
- docs/design/usecase/business-license.md  ← 유스케이스 로직 참조 (리네이밍 대상)

Existing Code (참조):
- src/main/java/com/atstudio/atstudio/entity/BusinessLicenseRequest.java
- src/main/java/com/atstudio/atstudio/entity/enums/BusinessLicenseStatus.java
- src/main/java/com/atstudio/atstudio/repository/BusinessLicenseRequestRepository.java
- src/main/java/com/atstudio/atstudio/config/SecurityConfig.java          (line 83-85)
- src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java (line 103-111)
- src/main/resources/schema.sql                                            (line 133-151)
- src/test/java/com/atstudio/atstudio/entity/EntityDefaultValueTest.java  (line 104-112)
- src/main/java/com/atstudio/atstudio/service/QuestionService.java        (Service 패턴 참조)
- src/main/java/com/atstudio/atstudio/controller/QuestionController.java  (Controller 패턴 참조)
- src/main/java/com/atstudio/atstudio/service/StorageService.java         (파일 업로드 패턴)

---

## ══ PHASE 1: 리네이밍 상세 지시 ══

### 1-A. 엔티티: BusinessLicenseRequest → CompanyCertification

**삭제**: `src/main/java/com/atstudio/atstudio/entity/BusinessLicenseRequest.java`
**신규 생성**: `src/main/java/com/atstudio/atstudio/entity/CompanyCertification.java`

```java
package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import com.atstudio.atstudio.entity.enums.CompanyCertificationStatus;
import jakarta.persistence.*;
import lombok.*;
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

    @Column(length = 50, unique = true)
    private String certificationCode;

    private LocalDateTime approvedAt;

    public void process(CompanyCertificationStatus newStatus, String adminNote, String certificationCode, LocalDateTime approvedAt) {
        this.status = newStatus;
        this.adminNote = adminNote;
        this.certificationCode = certificationCode;
        this.approvedAt = approvedAt;
    }
}
```

### 1-B. Enum: BusinessLicenseStatus → CompanyCertificationStatus

**삭제**: `src/main/java/com/atstudio/atstudio/entity/enums/BusinessLicenseStatus.java`
**신규 생성**: `src/main/java/com/atstudio/atstudio/entity/enums/CompanyCertificationStatus.java`

```java
package com.atstudio.atstudio.entity.enums;

public enum CompanyCertificationStatus {
    PENDING, APPROVED, REVISION_REQUESTED, REJECTED
}
```

### 1-C. Repository: BusinessLicenseRequestRepository → CompanyCertificationRepository

**삭제**: `src/main/java/com/atstudio/atstudio/repository/BusinessLicenseRequestRepository.java`
**신규 생성**: `src/main/java/com/atstudio/atstudio/repository/CompanyCertificationRepository.java`

```java
package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.CompanyCertification;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.CompanyCertificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CompanyCertificationRepository extends JpaRepository<CompanyCertification, Long> {
    Optional<CompanyCertification> findByUser(User user);
    boolean existsByUserAndStatusIn(User user, java.util.List<CompanyCertificationStatus> statuses);
    Page<CompanyCertification> findAll(Pageable pageable);
    Page<CompanyCertification> findByStatus(CompanyCertificationStatus status, Pageable pageable);
}
```

### 1-D. SecurityConfig: URL 변경

**파일**: `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java`

변경 (line 83-85):
```java
// 기존:
.requestMatchers(HttpMethod.GET, "/api/business-licenses").hasRole("ADMIN")
.requestMatchers(HttpMethod.GET, "/api/business-licenses/*").hasRole("ADMIN")
.requestMatchers(HttpMethod.PUT, "/api/business-licenses/*").hasRole("ADMIN")

// 변경:
.requestMatchers(HttpMethod.GET, "/api/company-certifications").hasRole("ADMIN")
.requestMatchers(HttpMethod.GET, "/api/company-certifications/*").hasRole("ADMIN")
.requestMatchers(HttpMethod.PUT, "/api/company-certifications/*").hasRole("ADMIN")
```

### 1-E. BUSINESS_ERROR: 에러코드 변경

**파일**: `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java`

변경 (line 103-111):
```java
// 기존:
BUSINESS_LICENSE_REQUIRED(
    HttpStatus.FORBIDDEN,
    "기업회원 라이센스 심사 승인 후 이용 가능합니다.",
    "기업회원 라이센스 미승인 상태."),

// 변경:
COMPANY_CERTIFICATION_REQUIRED(
    HttpStatus.FORBIDDEN,
    "기업 인증 심사 승인 후 이용 가능합니다.",
    "기업회원 Company Certification 미승인 상태."),
```

### 1-F. schema.sql: 테이블/컬럼 변경

**파일**: `src/main/resources/schema.sql`

변경 (line 133-151):
```sql
-- 기존 섹션 전체 교체:
-- ─────────────────────────────────────────────
-- 2.3  company_certifications  (→ users)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS company_certifications
(
    id                 BIGINT                                                          NOT NULL AUTO_INCREMENT,
    user_id            BIGINT                                                          NOT NULL,
    status             ENUM ('PENDING', 'APPROVED', 'REVISION_REQUESTED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    admin_note         TEXT                                                            NULL     COMMENT 'Reason for revision request, etc.',
    document_path      VARCHAR(500)                                                    NOT NULL COMMENT 'Per-user dedicated folder path (e.g. /uploads/company-docs/{user_id}/).',
    certification_code VARCHAR(50)                                                     NULL     COMMENT 'UUID-based. Issued upon approval.',
    approved_at        DATETIME                                                        NULL,
    created_at         DATETIME                                                        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME                                                        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_company_certification_code (certification_code),
    CONSTRAINT fk_company_certifications_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
```

### 1-G. EntityDefaultValueTest: 참조 수정

**파일**: `src/test/java/com/atstudio/atstudio/entity/EntityDefaultValueTest.java`

변경 (line 104-112):
- import: `BusinessLicenseRequest` → `CompanyCertification`, `BusinessLicenseStatus` → `CompanyCertificationStatus`
- `@DisplayName("BusinessLicenseRequest: status=PENDING")` → `@DisplayName("CompanyCertification: status=PENDING")`
- `void businessLicenseRequest_defaults()` → `void companyCertification_defaults()`
- `BusinessLicenseRequest req = BusinessLicenseRequest.builder()` → `CompanyCertification req = CompanyCertification.builder()`
- `.build()` 전 `.user(mock user or null — builder 패턴 확인)` 처리 (user는 NOT NULL이므로 mock 필요 시 Mockito.mock(User.class) 사용)
- `BusinessLicenseStatus.PENDING` → `CompanyCertificationStatus.PENDING`

### 1-H. 문서 파일 수정

**⚠️ 문서 파일은 내용을 수정하되 파일명은 영어 원칙 유지. business-license.md → company-certification.md (신규 생성 후 old 삭제)**

1. **신규 생성**: `docs/design/usecase/company-certification.md`
   - `docs/design/usecase/business-license.md` 내용 기반으로 작성
   - 제목: "User — Company Certification Use Cases"
   - API Reference: Section 13 (Company Certification)
   - DB Reference: `company_certifications`
   - BL-001~005 → CC-001~005
   - 모든 "business license" → "company certification"
   - 모든 "business_license_requests" → "company_certifications"
   - 모든 "licenseCode" → "certificationCode"
   - 모든 "license_code" → "certification_code"
   - `BUSINESS_LICENSE_REQUIRED` → `COMPANY_CERTIFICATION_REQUIRED`

2. **삭제**: `docs/design/usecase/business-license.md`
   → Write tool로 빈 파일 생성 불가 → 대신 파일을 "Moved to company-certification.md" 한 줄만 남기거나, 내용을 company-certification.md로 이전 후 이 파일은 삭제 불가 시 redirect 메모로 대체

3. **수정**: `docs/design/usecase/index.md`
   - `business-license.md` 참조 → `company-certification.md`
   - `BL-001~005` → `CC-001~005`
   - 설명: "Business license review" → "Company certification"

4. **수정**: `docs/design/usecase/user-subscription.md`
   - `BL-001` → `CC-001`
   - `business license` → `company certification`
   - `business_license_requests.status=APPROVED` → `company_certifications.status=APPROVED`
   - `BUSINESS_LICENSE_REQUIRED` → `COMPANY_CERTIFICATION_REQUIRED`

5. **수정**: `docs/design/usecase/user-license.md`
   - `business_license_requests` → `company_certifications`
   - `business-license.md` → `company-certification.md`

6. **수정**: `docs/design/usecase/util.md`
   - `BL-001` → `CC-001`
   - `business license` → `company certification` (해당 라인)

7. **수정**: `docs/design/api-spec.md`
   - Section 13 제목: "Business License Review" → "Company Certification"
   - 13.1~13.5 URL: `/api/business-licenses` → `/api/company-certifications`
   - 13.1 response 필드: `documentPath` 유지
   - 13.2 response 필드: `licenseCode` → `certificationCode`
   - 13.5 response 필드: `licenseCode` → `certificationCode`
   - 13.5 description: `license_code is auto-generated` → `certification_code is auto-generated`
   - 6.3 errorCode: `BUSINESS_LICENSE_REQUIRED` → `COMPANY_CERTIFICATION_REQUIRED`
   - 6.3 message: "기업회원 라이센스 심사 승인 후 이용 가능합니다." → "기업 인증 심사 승인 후 이용 가능합니다."

8. **수정**: `docs/design/db-schema.md`
   - Section 3.1 제목: "Business License Application (`business_license_requests`)" → "Company Certification (`company_certifications`)"
   - 테이블명, 컬럼명, FK명 전부 변경
   - `license_code` → `certification_code`
   - ER다이어그램/관계도 섹션의 `business_license_requests` → `company_certifications`

9. **수정**: `docs/standards/exception-handling.md`
   - `BUSINESS_LICENSE_REQUIRED` → `COMPANY_CERTIFICATION_REQUIRED`
   - 메시지 수정

10. **수정**: `docs/standards/glossary.md`
    - `business-license` entry → `company-certification`으로 교체

---

## ══ PHASE 2: Company Certification API 구현 ══

### API 명세 (api-spec.md Section 13 기준, 리네이밍 후)

| API | Method | Path | Auth | 응답 |
|-----|--------|------|------|------|
| 13.1 기업 인증 신청 | POST | /api/company-certifications | auth (BUSINESS만) | 201 |
| 13.2 내 인증 상태 | GET | /api/company-certifications/me | auth | 200 |
| 13.3 신청 목록 | GET | /api/company-certifications | [ADMIN] | 200 |
| 13.4 신청 상세 | GET | /api/company-certifications/{certificationId} | [ADMIN] | 200 |
| 13.5 심사 처리 | PUT | /api/company-certifications/{certificationId} | [ADMIN] | 200 |

### DTO 명세

패키지: `src/main/java/com/atstudio/atstudio/dto/certification/`

**CompanyCertificationRequest.java** (13.1 신청용):
- multipart/form-data로 처리 → Controller에서 `@RequestPart List<MultipartFile> documents`

**CompanyCertificationReviewRequest.java** (13.5 심사용):
```java
public record CompanyCertificationReviewRequest(
    @NotNull CompanyCertificationStatus status,
    String adminNote
) {}
```

**CompanyCertificationResponse.java** (공통 응답):
```java
public record CompanyCertificationResponse(
    Long id,
    String status,
    String adminNote,
    String certificationCode,
    String documentPath,
    LocalDateTime approvedAt,
    LocalDateTime createdAt
) {
    public static CompanyCertificationResponse from(CompanyCertification cert) { ... }
}
```

**CompanyCertificationSummaryResponse.java** (13.3 목록용):
```java
public record CompanyCertificationSummaryResponse(
    Long id,
    Long userId,
    String userNickname,
    String status,
    LocalDateTime createdAt
) {
    public static CompanyCertificationSummaryResponse from(CompanyCertification cert) { ... }
}
```

### Service 명세

파일: `src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java`

```
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompanyCertificationService {
    private final CompanyCertificationRepository certificationRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    // 13.1 apply(CustomUserDetails, List<MultipartFile>) → CompanyCertificationResponse
    // 1. user 조회
    // 2. userType == BUSINESS 체크 → else: RESOURCE_NOT_ACCESS (403)
    // 3. existsByUserAndStatusIn(user, [PENDING, APPROVED]) → 409 RESOURCE_DUPLICATE
    // 4. StorageService.store() 또는 각 파일 저장 후 경로 생성
    //    (간단히: "/uploads/company-docs/{userId}/" 경로로 StorageService 위임)
    // 5. CompanyCertification.builder()로 저장 후 Response 반환
    @Transactional

    // 13.2 getMyStatus(CustomUserDetails) → CompanyCertificationResponse (null 허용)
    // findByUser → Optional.orElse(null) or throw RESOURCE_NOT_FOUND

    // 13.3 listAll(Optional<String> status, Pageable) → Page<CompanyCertificationSummaryResponse>

    // 13.4 getDetail(Long certificationId) → CompanyCertificationResponse
    @Transactional

    // 13.5 processReview(Long certificationId, CompanyCertificationReviewRequest) → CompanyCertificationResponse
    // 1. 조회 → RESOURCE_NOT_FOUND
    // 2. APPROVED 시: certificationCode = "BIZ-" + UUID.randomUUID(), approvedAt = now()
    // 3. cert.process(status, adminNote, certificationCode, approvedAt)
    @Transactional
}
```

**StorageService 파일 저장 패턴** (기존 코드 참조):
- `storageService.store(file)` 호출 시 저장 경로 반환
- 여러 파일인 경우 첫 파일 경로 또는 폴더 경로 `/uploads/company-docs/{userId}/` 저장

### Controller 명세

파일: `src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java`

```
@RestController
@RequestMapping("/api/company-certifications")
@RequiredArgsConstructor
public class CompanyCertificationController {
    private final CompanyCertificationService certificationService;

    @PostMapping (consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // @RequestPart("documents") List<MultipartFile> documents
    // @AuthenticationPrincipal CustomUserDetails userDetails
    // → 201 Created

    @GetMapping("/me")
    // → 200 OK (내 인증 상태, 없으면 null data 반환)

    @GetMapping     // [ADMIN] — SecurityConfig에서 처리됨
    // @RequestParam(required=false) String status
    // @RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="20") int size
    // → 200 OK (페이지네이션)

    @GetMapping("/{certificationId}")  // [ADMIN]
    // → 200 OK

    @PutMapping("/{certificationId}")  // [ADMIN]
    // @Valid @RequestBody CompanyCertificationReviewRequest
    // → 200 OK
}
```

### 테스트 명세

**CompanyCertificationServiceTest**:
1. apply_success: 정상 신청
2. apply_notBusinessMember: INDIVIDUAL 회원 → 403
3. apply_duplicatePending: 이미 PENDING 존재 → 409
4. apply_duplicateApproved: 이미 APPROVED 존재 → 409
5. getMyStatus_exists: 신청 존재 → Response 반환
6. getMyStatus_notFound: 신청 없음 → (null 반환 or RESOURCE_NOT_FOUND 정책에 따라)
7. listAll_noFilter: 전체 목록
8. listAll_withStatusFilter: 상태 필터
9. getDetail_success: 상세 조회
10. processReview_approve: APPROVED → certificationCode 생성 확인
11. processReview_revisionRequested: REVISION_REQUESTED → adminNote 저장
12. processReview_reject: REJECTED

**CompanyCertificationControllerTest**:
1. apply_success: POST 201
2. apply_forbidden_nonBusiness: POST 403
3. apply_conflict: POST 409
4. getMyStatus_success: GET /me 200
5. listAll_success: GET 200 (admin)
6. listAll_forbidden_user: GET 403 (일반 회원)
7. getDetail_success: GET /{id} 200 (admin)
8. processReview_approve: PUT 200 (admin)
9. processReview_forbidden: PUT 403 (일반 회원)

---

## 추가 구현 노트

1. **UserType 체크**: `user.getUserType() == UserType.BUSINESS` — UserType enum은 `INDIVIDUAL`, `BUSINESS` (CORPORATE 없음)
2. **getMyStatus 빈 응답**: 신청이 없는 경우 → `null` data 반환 (RESOURCE_NOT_FOUND 대신 빈 응답이 UX에 맞음). API spec 13.2 참조: "Returns null if no application exists."
3. **certificationCode 생성**: `"BIZ-" + UUID.randomUUID().toString()`
4. **pageInfo 구조**: 기존 PlaylistService, NoticeService 등의 페이지네이션 패턴 참조
5. **StorageService**: `src/main/java/com/atstudio/atstudio/service/StorageService.java` 확인 후 `store(MultipartFile)` 패턴 사용. 폴더 경로 저장 시: "/uploads/company-docs/{userId}/" 고정값 또는 StorageService 반환값 사용
6. **Pageable 생성**: `PageRequest.of(page - 1, size)` (1-based → 0-based 변환)

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260226-ATS-025-summary.md :
  - 리네이밍 완료 파일 목록
  - 신규 구현 API 목록
  - 주요 결정사항 (getMyStatus null 반환 정책 등)
Agent-facing -> deliverables/agent/WI-20260226-ATS-025-evidence-pack.md :
  - 수정/삭제/신규 파일 전체 목록
  - 테스트 케이스 목록 (ServiceTest N + ControllerTest M)
  - 다음 WI: WI-20260226-ATS-026 (빌드 + 전체 테스트 회귀)
Handoff Packet -> deliverables/agent/WI-20260226-ATS-025-handoff.md :
  - 이 파일

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 수정된 각 파일 경로 + 핵심 변경 라인 기록
Tests: ServiceTest/ControllerTest 메서드명 목록 기록
Rollback: Phase 1은 모두 역방향 rename으로 원복 가능 / Phase 2 신규 파일 삭제로 원복

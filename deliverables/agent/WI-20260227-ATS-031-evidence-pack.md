# WI-20260227-ATS-031 Evidence Pack — cr-C: User·Auth·Inquiry·Notice·CompanyCert·Util

## cr-C 검토 결과

### Auth Domain

#### 1.1 JWT 라이프사이클

| 항목 | 판정 | 파일:라인 |
|------|------|----------|
| AccessToken 생성 | ✅ | `JwtTokenProvider.java:27-37` — HMAC-SHA 서명, 설정 가능한 만료 |
| RefreshToken 생성 | ✅ | `JwtTokenProvider.java:39-48` — 별도 RT, role claim 없음 |
| 토큰 갱신 + 로테이션 | ✅ | `AuthService.java:67-100` — 양 토큰 갱신, 구 RT 덮어쓰기 무효화 |
| RefreshToken DB 저장 | ✅ | `AuthService.java:47` — BCrypt 해시 후 저장 |
| 만료 RT 처리 | ✅ | `AuthService.java:71-73` — refresh 흐름에서만 EXPIRED 허용, INVALID 거부 |
| 탈퇴 계정 refresh 차단 | ✅ | `AuthService.java:89-91` — `user.isDeleted()` 체크 (SEC-08) |
| DB 불일치 RT 초기화 | ✅ | `AuthService.java:82-86` — 불일치 시 RT 삭제 (SEC-07) |
| JWT 시크릿 관리 | ❌ | `application.yml:36` — 예측 가능한 기본값 fallback 존재 (CR-C-009) |

#### 1.2 소셜 로그인

| 항목 | 판정 | 파일:라인 |
|------|------|----------|
| 코드 교환 (Google/Kakao/Naver) | ✅ | `OAuth2Service.java:108-158` |
| 기존 소셜 계정 조회 | ✅ | `OAuth2Service.java:65-69` — `findByProviderAndProviderId` |
| 이메일 충돌 방지 | ✅ | `OAuth2Service.java:73-75` — SEC-09: `EMAIL_ALREADY_REGISTERED` |
| 신규 사용자 최소 레코드 | ✅ | `OAuth2Service.java:83-90` — password/phone/job=NULL |
| isProfileComplete 파생 | ✅ | `CustomUserDetails.java:55-57` — RULE-USER-002 준수 |
| OAuth 자격증명 외부화 | ✅ | `OAuth2Service.java:30-55`, `application.yml:45-57` — env var |
| OAuth2 null 체크 | ⚠️ | `OAuth2Service.java:117-158` — 응답 null 체크 없음 (CR-C-013) |

#### 1.3 Auth 코딩 표준

| 항목 | 판정 | 파일:라인 |
|------|------|----------|
| @Transactional(readOnly=true) 클래스 | ❌ CRITICAL | `AuthService.java:24`, `OAuth2Service.java:22` — 쓰기 모드 (CR-C-002) |
| @RequiredArgsConstructor | ✅ | 양 파일 확인 |
| Controller thin | ✅ | `AuthController.java` — 순수 위임 |

#### 1.4 SecurityConfig

| 항목 | 판정 | 파일:라인 |
|------|------|----------|
| CSRF 비활성화 | ✅ | `SecurityConfig.java:33` |
| CORS 설정 | ✅ | `SecurityConfig.java:34` |
| Stateless 세션 | ✅ | `SecurityConfig.java:35-36` |
| Public 엔드포인트 | ✅ | `SecurityConfig.java:52-67` |
| Admin 엔드포인트 | ✅ | `SecurityConfig.java:71-95` |
| 401/403 JSON 응답 | ✅ | `SecurityConfig.java:38-49` |
| BCrypt strength=10 | ✅ | `SecurityConfig.java:108` |

#### 1.5 JwtAuthenticationFilter

| 항목 | 판정 | 파일:라인 |
|------|------|----------|
| 토큰 추출 | ✅ | `JwtAuthenticationFilter.java:56-62` |
| 유효 토큰 → 인증 | ✅ | `JwtAuthenticationFilter.java:36-43` |
| 만료 토큰 → X-Token-Expired | ✅ | `JwtAuthenticationFilter.java:45-47` |
| 무효 토큰 → 초기화 | ✅ | `JwtAuthenticationFilter.java:49-51` |

---

### 5.x User Domain

| API | 판정 | 발견 이슈 | 파일:라인 |
|-----|------|----------|---------:|
| 5.1 POST /api/users | ⚠️ | CR-C-010: phonePersonal 유니크 체크 누락 | `UserService.java:28-47` |
| 5.4 GET /api/users/me | ✅ | - | `UserController.java:34-40` |
| 5.5 GET /api/users | ❌ | CR-C-003: is_deleted 필터 없음 → 탈퇴 회원 노출 | `UserRepository.java:21-27` |
| 5.6 GET /api/users/{userId} | ✅ | - | `UserController.java:78-84` |
| 5.7 PUT /api/users/me | ✅ | - | `UserController.java:42-49` |
| 5.8 PUT /api/users/{userId} | ✅ | role+isVerified만 수정 (RULE-USER-006 준수) | `UserService.java:134-138` |
| 5.9 DELETE /api/users/me | ✅ | 소프트 삭제 + 비밀번호 검증 | `UserService.java:73-83` |
| 5.10 PUT /api/users/me/complete-profile | ✅ | - | `UserController.java:59-66` |

#### User 비즈니스 규칙

| 규칙 | 판정 | 파일:라인 |
|------|------|----------|
| RULE-USER-001: BCrypt | ✅ | `UserService.java:39` |
| RULE-USER-002: isProfileComplete 파생 | ✅ | `User.java:82-84` |
| RULE-USER-003: userType 불변 | ✅ | `UserService.java:67-68` |
| RULE-USER-004: 탈퇴 = 소프트삭제 + 비밀번호 검증 | ✅ | `UserService.java:73-83` |
| RULE-USER-005: is_deleted 로그인 차단 | ✅ | `CustomUserDetailsService.java:21-23` |
| RULE-USER-006: 관리자 role+isVerified만 수정 | ✅ | `User.java:86-89` |
| 5.5: is_deleted=0 필터 | ❌ MAJOR | `UserRepository.java:21-27` — WHERE절에 누락 (CR-C-003) |

---

### 8.x Inquiry (Question/Answer) Domain

| API | 판정 | 발견 이슈 | 파일:라인 |
|-----|------|----------|---------:|
| 8.1 POST /api/questions | ✅ | multipart, Auth | `QuestionController.java:29-39` |
| 8.2 POST /api/questions/{id}/answers | ✅ | 소유자+관리자 권한 체크 | `QuestionController.java:43-54` |
| 8.3 GET /api/questions | ✅ | 가시성 매트릭스, addSpec() 패턴 | `QuestionService.java:227-230` |
| 8.4 GET /api/questions/{id} | ✅ | @EntityGraph(answers.user) | `AnswerRepository.java:12-13` |
| 8.5 GET .../attachments/{attachId} | ✅ | 파일 다운로드 | `QuestionController.java:85-96` |
| 8.6 PUT /api/questions/{id}/status | ❌ | CR-C-007: 상태 전환 검증 없음 | `Question.java:44-46` |
| 8.7 DELETE /api/questions/{id} | ❌ | CR-C-001 CRITICAL: cascade 삭제 누락 | `QuestionService.java:182-188` |

#### Inquiry 비즈니스 규칙

| 규칙 | 판정 | 파일:라인 |
|------|------|----------|
| RULE-INQ-001: 비공개 가시성 | ✅ | `QuestionService.java:199-206` |
| RULE-INQ-002: 상태 전환 검증 | ❌ MAJOR | `Question.java:44-46` — 임의 전환 허용 (CR-C-007) |
| RULE-INQ-002: 관리자 답변 시 자동 IN_PROGRESS | ✅ | `QuestionService.java:95-97` |
| RULE-INQ-003: 소유자=OPEN만 삭제/관리자=모든 상태 | ✅ | `QuestionService.java:181-189` |
| RULE-INQ-005: 답변 권한 | ✅ | `QuestionService.java:78-81` |
| RULE-INQ-006: 삭제 cascade | ❌ CRITICAL | `QuestionService.java:182-188` — 자식 미삭제 (CR-C-001) |
| QuestionAttachment BaseEntity | ⚠️ | `QuestionAttachment.java:17` — BaseEntity 미상속 (CR-C-011) |

---

### 9.x Notice Domain

| API | 판정 | 발견 이슈 | 파일:라인 |
|-----|------|----------|---------:|
| 9.1 POST /api/notices | ✅ | ADMIN, 201 Created | `NoticeController.java` |
| 9.2 GET /api/notices | ✅ | PUBLIC, 페이지네이션 | `NoticeService.java:50` |
| 9.3 GET /api/notices/{id} | ✅ | PUBLIC | - |
| 9.4 PUT /api/notices/{id} | ✅ | ADMIN | - |
| 9.5 DELETE /api/notices/{id} | ✅ | ADMIN, 204, 물리 삭제 | `NoticeService.java:81` |

| 규칙 | 판정 | 파일:라인 |
|------|------|----------|
| RULE-NOTICE-001: 고정 공지 우선 | ✅ | `NoticeService.java:50` — isPinnedDesc + createdAtDesc |
| RULE-NOTICE-002: 물리 삭제 | ✅ | `NoticeService.java:81` |
| @Transactional(readOnly=true) 클래스 | ✅ | `NoticeService.java:26` |

**Notice 도메인: 이슈 없음 — CLEAN**

---

### 13.x Company Certification Domain

| API | 판정 | 발견 이슈 | 파일:라인 |
|-----|------|----------|---------:|
| 13.1 POST /api/company-certifications | ❌ | CR-C-004: HTTP 400 (명세: 409) | `BUSINESS_ERROR.java:22-25` |
| 13.2 GET /api/company-certifications/me | ⚠️ | CR-C-012: null 반환 (비일관적) | `CompanyCertificationService.java:77` |
| 13.3 GET /api/company-certifications | ✅ | ADMIN | `SecurityConfig.java:84` |
| 13.4 GET /api/company-certifications/{id} | ✅ | ADMIN | `SecurityConfig.java:85` |
| 13.5 PUT /api/company-certifications/{id} | ❌ | CR-C-005/006: 비결정적 조회 + 전환 검증 없음 | `CompanyCertificationRepository.java:14`, `CompanyCertification.java:42-48` |

#### CompanyCert 비즈니스 규칙

| 규칙 | 판정 | 파일:라인 |
|------|------|----------|
| RULE-CC-001: BUSINESS 회원 체크 | ✅ | `CompanyCertificationService.java:48-49` |
| RULE-CC-002: PENDING/APPROVED 중복 방지 | ✅ | `CompanyCertificationService.java:52-55` |
| RULE-CC-002: HTTP 409 | ❌ MAJOR | `BUSINESS_ERROR.java:22-25` — BAD_REQUEST(400) 반환 (CR-C-004) |
| RULE-CC-003: REJECTED 후 재신청 | ⚠️ | `findByUser()` 비결정적 (CR-C-005) |
| RULE-CC-004: APPROVED → certificationCode 자동 발급 | ✅ | `CompanyCertificationService.java:126-129` |
| certificationCode prefix | ⚠️ | `CompanyCertificationService.java:127` — `"BIZ-"` prefix 사용 (명세: UUID only) |
| 상태 전환 검증 | ❌ MAJOR | `CompanyCertification.java:42-48` — 임의 전환 허용 (CR-C-006) |

---

### 14.x Util Domain

| API | 판정 | 발견 이슈 | 파일:라인 |
|-----|------|----------|---------:|
| 14.2 GET /check-email | ✅ | PUBLIC | `UtilController.java:27-33` |
| 14.3 GET /check-phone | ✅ | PUBLIC | `UtilController.java:35-41` |
| 14.4 GET /subscription-status | ✅ | Auth, noSubscription() | `UtilService.java:39-41` |
| 14.5 GET /download-count | ✅ | -1(무제한) 처리 | `UtilService.java:71-72` |
| 14.6 GET /user-type | ✅ | Auth | `UtilController.java:67-73` |
| 14.7 GET /check-nickname | ✅ | PUBLIC | `UtilController.java:43-49` |

**Util 도메인: 이슈 없음 — CLEAN**

---

### Common / Exception Handler

| 항목 | 판정 | 파일:라인 |
|------|------|----------|
| BusinessException 처리 | ✅ | `GlobalExceptionHandler.java:35-39` |
| TechnicException 처리 | ✅ | `GlobalExceptionHandler.java:41-45` |
| AccessDeniedException 처리 | ✅ | `GlobalExceptionHandler.java:116-118` — fallback 체인 내 처리 |
| TestController 운영 노출 | ❌ MAJOR | `TestController.java:1-18` — 인증 없는 `/test`, `/health` (CR-C-008) |
| RESOURCE_DUPLICATE HTTP 상태 | ❌ MAJOR | `BUSINESS_ERROR.java:22-25` — 400 (CR-C-004) |

---

## 코딩 표준 준수 (Rules 1-22)

| 파일 | readOnly 클래스 | mutating @Tx | Controller thin | DTO 분리 | @RequiredArgs |
|------|----------------|-------------|-----------------|---------|---------------|
| AuthService | ❌ CR-C-002 | ✅ | N/A | ✅ | ✅ |
| OAuth2Service | ❌ CR-C-002 | ✅ | N/A | ✅ | ✅ |
| UserService | ✅ | ✅ | N/A | ✅ | ✅ |
| QuestionService | ✅ | ✅ | N/A | ✅ | ✅ |
| NoticeService | ✅ | ✅ | N/A | ✅ | ✅ |
| CompanyCertificationService | ✅ | ✅ | N/A | ✅ | ✅ |
| UtilService | ✅ | N/A | N/A | ✅ | ✅ |
| 모든 Controller | N/A | N/A | ✅ | ✅ | ✅ |

---

## 발견 이슈 종합 목록

| # | 심각도 | 도메인 | 파일:라인 | 이슈 | 권장 조치 |
|---|--------|--------|---------|------|---------:|
| CR-C-001 | ❌ CRITICAL | Inquiry | `QuestionService.java:182-188` | cascade 삭제 누락 → DataIntegrityViolationException | `answerRepository.deleteAllByQuestion()` + `attachmentRepository.deleteAllByQuestion()` 선행 호출 |
| CR-C-002 | ❌ CRITICAL | Auth | `AuthService.java:24`, `OAuth2Service.java:22` | 클래스 레벨 `@Transactional` (쓰기 모드) | `@Transactional(readOnly=true)` 변경, mutating 메서드만 override |
| CR-C-003 | ❌ MAJOR | User | `UserRepository.java:21-27` | `searchUsers()` is_deleted 필터 없음 | JPQL에 `AND u.isDeleted = false` 추가 |
| CR-C-004 | ❌ MAJOR | CompanyCert | `BUSINESS_ERROR.java:22-25` | RESOURCE_DUPLICATE → HTTP 400 (명세: 409) | `HttpStatus.CONFLICT` 변경 또는 도메인 전용 에러코드 생성 |
| CR-C-005 | ❌ MAJOR | CompanyCert | `CompanyCertificationRepository.java:14` | `findByUser()` 복수 레코드 비결정적 반환 | `findTopByUserOrderByCreatedAtDesc()` 변경 |
| CR-C-006 | ❌ MAJOR | CompanyCert | `CompanyCertification.java:42-48` | `process()` 상태 전환 검증 없음 | 전환 허용 맵 기반 검증 추가 |
| CR-C-007 | ❌ MAJOR | Inquiry | `Question.java:44-46` | `updateStatus()` 상태 플로우 검증 없음 | RULE-INQ-002 기반 전환 검증 추가 |
| CR-C-008 | ❌ MAJOR | Common | `TestController.java:1-18` | 운영 환경 테스트 엔드포인트 노출 | 파일 삭제 또는 `@Profile("dev")` 격리 |
| CR-C-009 | ❌ MAJOR | Auth | `application.yml:36` | JWT 기본 시크릿 fallback 하드코딩 | 기본값 제거, `JWT_SECRET` 미설정 시 기동 실패 처리 |
| CR-C-010 | ⚠️ MINOR | User | `UserService.java:28-47` | phonePersonal 유니크 체크 누락 | `findByPhonePersonal` 존재 체크 추가 |
| CR-C-011 | ⚠️ MINOR | Inquiry | `QuestionAttachment.java:17` | BaseEntity 미상속 (기능적 문제 없음) | 일관성 위해 상속 추가 고려 |
| CR-C-012 | ⚠️ MINOR | CompanyCert | `CompanyCertificationService.java:77` | `getMyStatus()` null 반환 (타 엔드포인트와 불일관) | 명시적 빈 응답 또는 404 반환 고려 |
| CR-C-013 | ⚠️ MINOR | Auth | `OAuth2Service.java:117-158` | OAuth2 토큰 교환 응답 null 체크 없음 | `response.get("access_token")` null 가드 추가 |
| CR-C-014 | 📋 제안 | Common | `GlobalExceptionHandler.java:116-118` | AccessDeniedException fallback 체인 내 처리 | 전용 `@ExceptionHandler` 메서드로 분리 |
| CR-C-015 | 📋 제안 | Auth | `CustomUserDetailsService.java:21-23` | 탈퇴 계정 로그인 → 401 (명세: 403) | `DisabledException` 또는 `LockedException` 발생 |
| CR-C-016 | 📋 제안 | CompanyCert | `CompanyCertificationService.java:127` | certificationCode에 "BIZ-" prefix | prefix 제거 또는 컨벤션 문서화 |

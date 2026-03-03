[EVIDENCE PACK — WI-20260302-ATS-014]

## Review Status
PASS — CRITICAL 0, MAJOR 0, MINOR 4, SUGGESTION 1
Date: 2026-03-03

## Files Reviewed

| # | File | Verdict |
|---|------|---------|
| 1 | DownloadService.java | PASS |
| 2 | UserRepository.java | PASS (MINOR CR-M-1) |
| 3 | OAuth2Service.java | PASS (MINOR CR-M-2, CR-M-4) |
| 4 | BUSINESS_ERROR.java | PASS |
| 5 | CompanyCertificationService.java | PASS |
| 6 | DownloadServiceTest.java | PASS (MINOR CR-M-3) |
| 7 | OAuth2ServiceTest.java | PASS (MINOR CR-M-4) |
| 8 | UserServiceTest.java | PASS |

## Detailed Findings

### DownloadService.java
- L48: `downloadPerDay != -1 && todayCount >= downloadPerDay` — short-circuit 정확. 무제한(-1), 제로(0), 유한(N) 모두 올바른 동작
- L19-20: `@Transactional(readOnly = true)` 클래스 레벨 — development-standards.md 2A.4 준수
- L31: `@Transactional` 메서드 override — mutating 메서드 정확히 override

### UserRepository.java
- L21-24: `AND u.isDeleted = false` — searchUsers JPQL에 정확히 추가
- **CR-M-1 (MINOR)**: L15,17,19 — findByEmail/findByNickname/findByPhonePersonal 탈퇴 계정 미필터
  - 로그인 경로(CustomUserDetailsService): 별도 isDeleted 체크 → SAFE
  - 회원가입/중복체크: 탈퇴 계정의 이메일/닉네임/폰 재사용 차단됨 → 비즈니스 결정 필요

### OAuth2Service.java
- Token exchange null guard 매트릭스:
  - Google L129, Kakao L147, Naver L165 → 모두 SOCIAL_AUTH_FAILED
- UserInfo null guard 매트릭스:
  - Google L186, Kakao L202/206/210 (3단계), Naver L226/230 (2단계)
- **CR-M-2 (MINOR)**: L122-126 — OAuth2 POST body raw string. 실질 위험 낮음

### BUSINESS_ERROR.java
- L23: HttpStatus.CONFLICT (409) — RESOURCE_DUPLICATE ✓
- L33: HttpStatus.BAD_REQUEST (400) — INVALID_STATE_TRANSITION ✓
- 기존 항목 영향 없음

### CompanyCertificationService.java
- L88: `status != null && !status.isBlank()` — null 입력 선처리
- L90-94: try-catch IllegalArgumentException → INVALID_ARGUMENT(400) — 정확

### Test Coverage
- DownloadServiceTest: unlimitedPlan(-1) x2, limitedPlan underLimit/overLimit ✓
  - **CR-M-3**: downloadPerDay=0 테스트 누락
- OAuth2ServiceTest: 3 provider token null + 5 userInfo null = 8 케이스
  - **CR-M-4**: Kakao profile=null 미커버 (guard L209-211 존재)
- UserServiceTest: C-2 검증 포함 (mock 기반, JPQL 단위는 @DataJpaTest 필요)

## Standards Compliance
| Standard | Status |
|----------|--------|
| @Transactional pattern (dev-standards 2A.4) | COMPLIANT |
| Exception handling (dev-standards 2A.5) | COMPLIANT |
| Security — PII (security-policy.md) | COMPLIANT |

## Conclusion
차단 이슈 없음. WI-007~009 수정 정확. PASS.

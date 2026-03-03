[WI-014 CR SUMMARY]
WI ID: WI-20260302-ATS-014
REQ: REQ-20260302-ATS-012
Reviewer: cr
Date: 2026-03-03
Scope: WI-007~009 (Download/User/Auth/Error)

## Verdict

Status: PASS
CRITICAL: 0
MAJOR: 0
MINOR: 4
SUGGESTION: 1

## Confirmed Fixes (All Correct)

- C-1 DownloadService downloadPerDay != -1 guard — 정확. downloadPerDay=0도 의도대로 차단
- C-2 UserRepository searchUsers JPQL AND u.isDeleted = false — 정확
- M-1 DownloadService @Transactional(readOnly=true) 클래스 레벨 + download() override — 표준 준수
- M-2 BUSINESS_ERROR.RESOURCE_DUPLICATE HttpStatus.CONFLICT(409) — 정확
- INVALID_STATE_TRANSITION HttpStatus.BAD_REQUEST(400) — 적절
- M-9 OAuth2Service null guard — Google/Kakao/Naver 3개 provider 모두 token exchange + userInfo 수준에서 커버. Kakao 3단계(info/account/profile), Naver 2단계(body/response) 완전 적용
- M-11 CompanyCertificationService valueOf try-catch — IllegalArgumentException → INVALID_ARGUMENT(400) 정확

## MINOR Issues

| ID | File | Line | Description |
|----|------|------|-------------|
| CR-M-1 | UserRepository.java | 15,17,19 | findByEmail/findByNickname/findByPhonePersonal — isDeleted 미필터. 탈퇴 계정 이메일/닉네임/폰 재사용 차단됨. 비즈니스 결정 필요 (의도적인지 버그인지). 로그인 경로는 CustomUserDetailsService에서 별도 체크로 안전. |
| CR-M-2 | OAuth2Service.java | 122-126 | OAuth2 POST body raw string 연결 — URL 인코딩 미적용. OAuth2 auth code는 base64url-safe이므로 실질 위험 낮음 |
| CR-M-3 | DownloadServiceTest.java | (missing) | downloadPerDay=0 경계값 테스트 누락 |
| CR-M-4 | OAuth2ServiceTest.java | (missing) | Kakao profile=null 케이스 테스트 누락 (프로덕션 guard는 존재: OAuth2Service.java:209-211) |

## SUGGESTION

| ID | Description |
|----|-------------|
| CR-S-1 | OAuth2 POST body 구성 시 raw string 대신 UriComponentsBuilder 또는 MultiValueMap + FormHttpMessageConverter 사용 권고 |

## Approval

차단 이슈 없음. WI-007~009 모든 CRITICAL/MAJOR 수정 정확히 구현됨. 머지 승인.

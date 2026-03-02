[WI HEADER]
WI ID: WI-20260302-ATS-002
REQ: REQ-20260302-ATS-011
Agent: se
Depends On: -
Blocks: WI-20260302-ATS-004

[WI SUMMARY]
Why: 보안 입력검증 + 토큰 만료 검증 2건 수정.
     CR-B-005 — WhitelistChannelService.channelUrl.contains("youtube.com")로 검증.
                 "notarealsite-youtube.com" 같은 URL도 통과 → URL 파싱/정규식으로 강화 필요.
     CR-P-005 — AuthService.refresh()에서 RefreshToken 만료 여부 미검증.
                 만료된 토큰으로도 새 액세스 토큰 발급 가능 → 계정 탈취 지속 위험.
Scope (in):
  - WhitelistChannelService.java: URL 검증 로직 강화 (도메인 파싱 기반)
  - AuthService.java: refresh() 내 RefreshToken 만료 시각 검증 추가
  - 관련 단위 테스트 추가
Scope (out):
  - 다른 WI 범위 파일 수정
  - DB 스키마 변경
DoD:
  - channelUrl: "notarealsite-youtube.com" → 거부, "youtube.com/channel/xxx" → 허용
  - refresh(): 만료된 RefreshToken → 401/403 응답 (새 액세스 토큰 미발급)
  - 단위 테스트 0 failures
Constraints/Forbidden:
  - DB 스키마 변경 금지
  - URL 검증: java.net.URI 파싱 또는 정규식 사용 (외부 라이브러리 추가 금지)
  - RefreshToken 만료 체크: RefreshToken 엔티티의 만료 시각 필드 확인 후 결정

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] WhitelistChannelService: host가 "youtube.com" 또는 "www.youtube.com"인 URL만 허용
  - [ ] WhitelistChannelService: 잘못된 URL 형식 → 적절한 예외 (400)
  - [ ] AuthService.refresh(): RefreshToken 만료 시 INVALID_TOKEN 또는 TOKEN_EXPIRED 예외 → 401
  - [ ] AuthService.refresh(): 유효한 RefreshToken → 정상 동작 유지
Quality:
  - [ ] WhitelistChannelServiceTest: 유사 도메인(notarealsite-youtube.com) 거부 테스트
  - [ ] WhitelistChannelServiceTest: 정상 youtube.com URL 허용 테스트
  - [ ] AuthServiceTest: 만료된 RefreshToken → 401 테스트
  - [ ] 기존 테스트 전체 통과

[INPUT POINTERS]
Tier 0:
  - docs/standards/core-principles.md
  - docs/standards/development-standards.md

Tier 1 (보안):
  - docs/policies/security-policy.md

REQ:
  - deliverables/user/REQ-20260302-ATS-011.md

감사 근거:
  - docs/audit/backend-audit-report.md ← CR-B-005, CR-P-005

수정 대상 파일:
  - src/main/java/com/atstudio/atstudio/service/WhitelistChannelService.java:98
  - src/main/java/com/atstudio/atstudio/service/auth/AuthService.java:70-73
  - src/test/java/com/atstudio/atstudio/service/WhitelistChannelServiceTest.java
  - src/test/java/com/atstudio/atstudio/service/AuthServiceTest.java

참고:
  - src/main/java/com/atstudio/atstudio/entity/RefreshToken.java ← 만료 시각 필드 확인
  - BUSINESS_ERROR.java ← TOKEN_EXPIRED / INVALID_TOKEN 에러코드 확인

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260302-ATS-002-summary.md
Agent-facing → deliverables/agent/WI-20260302-ATS-002-evidence-pack.md
  (수정 파일:라인 목록, URL 검증 로직 전후 스니펫, RefreshToken 만료 체크 스니펫, 추가 테스트 목록)

[TRACEABILITY REQUIREMENTS]
Evidence: 수정 파일명·라인번호 필수
Tests: URL 거부/허용 시나리오, 토큰 만료 시나리오 테스트명 포함
Rollback: git revert

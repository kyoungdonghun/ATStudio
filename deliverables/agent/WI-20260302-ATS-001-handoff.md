[WI HEADER]
WI ID: WI-20260302-ATS-001
REQ: REQ-20260302-ATS-011
Agent: se
Depends On: -
Blocks: WI-20260302-ATS-004

[WI SUMMARY]
Why: 소유권 체크 누락 2건 + TestController 운영 노출 1건 수정.
     CR-A-004 — PlaylistService 수정/삭제 시 소유자 확인 없이 타인 Playlist 조작 가능.
     CR-C-006 — NoticeService 수정/삭제 시 작성자 확인 없이 타인 Notice 조작 가능 (ADMIN 예외).
     CR-C-008/CR-P-003 — TestController /test, /health 엔드포인트 인증 없이 운영 노출.
Scope (in):
  - PlaylistService.java: 수정/삭제 전 소유자(userId) 확인 → 불일치 시 403
  - NoticeService.java: 수정/삭제 전 작성자 확인 → 불일치 시 403 (ADMIN 역할은 통과)
  - TestController.java: 운영 환경 노출 제거 (파일 삭제 또는 SecurityConfig에서 인증 필수 처리)
  - 관련 단위 테스트 추가
Scope (out):
  - 다른 WI 범위 파일 수정
  - DB 스키마 변경
DoD:
  - Playlist: 요청자 != 소유자이면 403 FORBIDDEN
  - Notice: 요청자 != 작성자이면 403, ADMIN이면 통과
  - TestController: 미인증 /test 또는 /health 요청 시 401
  - 단위 테스트 0 failures
Constraints/Forbidden:
  - DB 스키마 변경 금지
  - 소유권 체크 로직은 서비스 레이어에서 처리
  - TestController 처리: 파일 읽어 실제 사용 여부 확인 후 삭제 or SecurityConfig 인증 추가 결정

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] PlaylistService: updatePlaylist(), deletePlaylist() — 소유자 불일치 시 BusinessException(FORBIDDEN 또는 ACCESS_DENIED) throw
  - [ ] NoticeService: updateNotice(), deleteNotice() — 작성자 불일치 시 403, ADMIN 역할이면 허용
  - [ ] TestController: 미인증 요청 401 또는 파일 삭제(운영 노출 제거)
Quality:
  - [ ] PlaylistServiceTest: 타인 Playlist 수정 시도 → 403 테스트 추가
  - [ ] NoticeServiceTest: 타인 Notice 수정/삭제 시도 → 403 테스트 추가
  - [ ] NoticeServiceTest: ADMIN 역할은 타인 Notice 삭제 허용 테스트 추가
  - [ ] 기존 테스트 전체 통과 (no regressions)

[INPUT POINTERS]
Tier 0:
  - docs/standards/core-principles.md
  - docs/standards/development-standards.md

Tier 1 (보안 관련):
  - docs/policies/security-policy.md
  - docs/policies/access-control-policy.md

REQ:
  - deliverables/user/REQ-20260302-ATS-011.md

감사 근거:
  - docs/audit/backend-audit-report.md ← CR-A-004, CR-C-006, CR-C-008/CR-P-003

수정 대상 파일:
  - src/main/java/com/atstudio/atstudio/service/PlaylistService.java
  - src/main/java/com/atstudio/atstudio/service/NoticeService.java
  - src/main/java/com/atstudio/atstudio/controller/TestController.java  ← 존재 여부 및 내용 먼저 확인
  - src/main/java/com/atstudio/atstudio/config/SecurityConfig.java       ← TestController 인증 처리 시
  - src/test/java/com/atstudio/atstudio/service/PlaylistServiceTest.java
  - src/test/java/com/atstudio/atstudio/service/NoticeServiceTest.java

참고:
  - src/main/java/com/atstudio/atstudio/entity/Playlist.java  ← 소유자 필드 확인
  - src/main/java/com/atstudio/atstudio/entity/Notice.java    ← 작성자 필드 확인
  - BUSINESS_ERROR.java ← FORBIDDEN/ACCESS_DENIED 에러코드 확인

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260302-ATS-001-summary.md
Agent-facing → deliverables/agent/WI-20260302-ATS-001-evidence-pack.md
  (수정 파일:라인 목록, 소유권 체크 로직 스니펫, TestController 처리 방식, 추가 테스트 목록)

[TRACEABILITY REQUIREMENTS]
Evidence: 수정 파일명·라인번호 필수. 소유권 체크 전후 스니펫 포함.
Tests: 403 시나리오 테스트 메서드명 포함
Rollback: git revert

[WI HEADER]
WI ID: WI-20260227-ATS-032
REQ: REQ-20260227-ATS-009
Agent: pg
Depends On: WI-20260227-ATS-029, WI-20260227-ATS-030, WI-20260227-ATS-031
Blocks: WI-20260227-ATS-033

[WI SUMMARY]
Why: 백엔드 감사 Phase 3 — 크로스컷 보안·권한·응답 일관성 검토. cr-A/B/C 도메인 검토 결과를 토대로,
     SecurityConfig 권한 매핑 전수 조사, JWT 보안 흐름 검증, API 응답 일관성(ResponseDTO) 스캔을 수행한다.
Scope (in):
  - SecurityConfig.java: 79개 API 권한 매핑 전수 검사 (누락/과도 허용 여부)
  - JWT 보안 흐름: JwtTokenProvider, JwtAuthenticationFilter, AuthService — 토큰 발급/검증/만료/무효화
  - API 응답 일관성: 전 Controller ResponseDTO 래핑 여부 스캔, 204 No Content 누락 패턴
  - 기존 cr 이슈 중 보안 관련 항목 재검토: CR-C-002(AuthService @Transactional), CR-C-008(TestController), CR-C-009(JWT 기본 시크릿)
  - GlobalExceptionHandler: 민감 정보 노출, 에러 응답 포맷 일관성
  - application.yml: 환경변수 사용 여부, 하드코딩된 시크릿 여부
Scope (out): 코드 수정, 도메인별 비즈니스 로직 재검토 (cr-A/B/C 범위), 성능 테스트
DoD:
  - SecurityConfig 79개 엔드포인트 권한 매핑 전수 ✅/⚠️/❌ 판정
  - JWT 보안 흐름 이슈 항목 파일·라인 포함
  - ResponseDTO 미래핑 Controller 목록 확인
  - 보안 이슈 종합 목록 (CR-P-XXX 체계)
Constraints/Forbidden: 코드 수정 절대 금지. Read-only 검토만 수행.

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] SecurityConfig 전 엔드포인트(79개) 권한 매핑 검사 완료 — 누락·과도 허용 식별
  - [ ] JWT 발급/갱신/무효화/만료 흐름 보안 검토 완료
  - [ ] 전 Controller ResponseDTO 래핑 여부 스캔 완료
  - [ ] application.yml 시크릿 관리 검토 완료 (환경변수/기본값)
  - [ ] GlobalExceptionHandler 에러 응답 포맷 일관성 검토 완료
  - [ ] 각 항목 ✅/⚠️/❌/📋 판정
Quality:
  - [ ] ❌/⚠️ 항목에는 파일명·라인번호 포함

[INPUT POINTERS]
Tier 0 (Constitution):
  - docs/standards/core-principles.md

Tier 1 (Security Policy — pg 필수):
  - docs/policies/security-policy.md
  - docs/policies/access-control-policy.md

Phase 2 검토 결과 (크로스컷 기준점):
  - deliverables/agent/WI-20260227-ATS-028-evidence-pack.md  ← Phase1 체크리스트 (검토 기준)
  - deliverables/agent/WI-20260227-ATS-029-evidence-pack.md  ← cr-A: Track/License/Tag/Playlist/PlayHistory
  - deliverables/agent/WI-20260227-ATS-030-evidence-pack.md  ← cr-B: Subscription/Whitelist/DownloadQueue/Likes
  - deliverables/agent/WI-20260227-ATS-031-evidence-pack.md  ← cr-C: User/Auth/Inquiry/Notice/CompanyCert/Util

검토 대상 파일 (보안·권한 레이어):
  - src/main/java/com/atstudio/atstudio/config/SecurityConfig.java
  - src/main/java/com/atstudio/atstudio/security/JwtTokenProvider.java
  - src/main/java/com/atstudio/atstudio/security/JwtAuthenticationFilter.java
  - src/main/java/com/atstudio/atstudio/security/CustomUserDetails.java
  - src/main/java/com/atstudio/atstudio/security/CustomUserDetailsService.java
  - src/main/java/com/atstudio/atstudio/config/JwtConfig.java
  - src/main/java/com/atstudio/atstudio/service/auth/AuthService.java
  - src/main/java/com/atstudio/atstudio/service/auth/OAuth2Service.java
  - src/main/java/com/atstudio/atstudio/common/exception/GlobalExceptionHandler.java
  - src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java
  - src/main/resources/application.yml
  - src/main/java/com/atstudio/atstudio/controller/  (전 Controller 디렉토리)

API 명세 참조:
  - docs/design/api-spec.md  (79개 API 권한 정의 기준)

[OUTPUT CONTRACT]
User-facing  → deliverables/user/WI-20260227-ATS-032-summary.md
  형식:
  ## pg 크로스컷 보안 검토 결과
  - SecurityConfig 권한 매핑: ✅ N개 정상 / ⚠️ N개 미흡 / ❌ N개 수정 필요
  - JWT 보안: 이슈 목록
  - 응답 일관성: 미래핑 Controller 목록
  - 최종 판정: PASS / CONDITIONAL PASS / FAIL

Agent-facing → deliverables/agent/WI-20260227-ATS-032-evidence-pack.md
  형식:
  ## SecurityConfig 권한 매핑 전수 테이블
  | API | URL | SecurityConfig 설정 | api-spec.md 권한 | 판정 |
  ## JWT 보안 검토
  ## ResponseDTO 래핑 스캔
  ## 발견 이슈 종합 (CR-P-XXX)

[TRACEABILITY REQUIREMENTS]
Evidence: 파일명·라인 포인터 필수 (❌/⚠️ 항목)
Rollback: Read-only → 불필요

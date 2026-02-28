[WI HEADER]
WI ID: WI-20260227-ATS-031
REQ: REQ-20260227-ATS-009
Agent: cr
Depends On: WI-20260227-ATS-028
Blocks: WI-20260227-ATS-032

[WI SUMMARY]
Why: 백엔드 감사 Phase 2-C. User·Auth·Inquiry·Notice·CompanyCert·Util 도메인 코드를 WI-028 체크리스트 기준으로 검토.
Scope (in):
  - 5.x User Admin (3 APIs), Auth (로그인/로그아웃/토큰), 8.x Inquiry/Question (7 APIs), 9.x Notice (5 APIs), 13.x CompanyCert (5 APIs), 14.x Util (3 APIs) — 총 약 27개 API
  - Controller / Service / Repository / Entity / DTO 전 레이어
Scope (out): 코드 수정, 타 도메인 검토
DoD: 담당 API 각각 ✅/⚠️/❌/📋 판정, 이슈에 파일·라인 포함
Constraints/Forbidden: 코드 수정 절대 금지

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] User Admin (5.5/5.6/5.8) 검토 완료 (관리자 권한 체크)
  - [ ] Auth 흐름 검토 완료 (JWT 발급/갱신/무효화)
  - [ ] Inquiry/Question 8.1~8.7 검토 완료 (접근 제어 owner/admin, 상태 전환 OPEN→IN_PROGRESS)
  - [ ] Notice 9.1~9.5 검토 완료 (관리자 CRUD, 공개 조회)
  - [ ] CompanyCert 13.1~13.5 검토 완료 (상태 전환 PENDING→APPROVED/REJECTED, certificationCode 발급)
  - [ ] Util 14.4/14.5/14.6 검토 완료
  - [ ] 각 항목 ✅/⚠️/❌/📋 판정
Quality:
  - [ ] ❌ 항목에는 파일명·라인번호 포함

[INPUT POINTERS]
Tier 0:
  - docs/standards/core-principles.md
  - docs/standards/development-standards.md

검토 기준 (반드시 먼저 읽을 것):
  - deliverables/agent/WI-20260227-ATS-028-evidence-pack.md

검토 대상 파일:
  - src/main/java/com/atstudio/atstudio/controller/UserController.java
  - src/main/java/com/atstudio/atstudio/service/UserService.java
  - src/main/java/com/atstudio/atstudio/entity/User.java
  - src/main/java/com/atstudio/atstudio/controller/AuthController.java
  - src/main/java/com/atstudio/atstudio/service/auth/  (auth 서비스)
  - src/main/java/com/atstudio/atstudio/config/SecurityConfig.java
  - src/main/java/com/atstudio/atstudio/controller/QuestionController.java
  - src/main/java/com/atstudio/atstudio/service/QuestionService.java
  - src/main/java/com/atstudio/atstudio/entity/Question.java
  - src/main/java/com/atstudio/atstudio/entity/Answer.java
  - src/main/java/com/atstudio/atstudio/entity/QuestionAttachment.java
  - src/main/java/com/atstudio/atstudio/controller/NoticeController.java
  - src/main/java/com/atstudio/atstudio/service/NoticeService.java
  - src/main/java/com/atstudio/atstudio/entity/Notice.java
  - src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java
  - src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java
  - src/main/java/com/atstudio/atstudio/entity/CompanyCertification.java
  - src/main/java/com/atstudio/atstudio/controller/UtilController.java
  - src/main/java/com/atstudio/atstudio/service/UtilService.java
  - src/main/java/com/atstudio/atstudio/common/exception/GlobalExceptionHandler.java
  - src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java
  - src/main/java/com/atstudio/atstudio/repository/  (관련 Repository)

[OUTPUT CONTRACT]
User-facing  → deliverables/user/WI-20260227-ATS-031-summary.md
Agent-facing → deliverables/agent/WI-20260227-ATS-031-evidence-pack.md
  형식:
  ## cr-C 검토 결과: User·Auth·Inquiry·Notice·CompanyCert·Util
  | 도메인 | API | 판정 | 발견 이슈 | 파일:라인 |

[TRACEABILITY REQUIREMENTS]
Evidence: 파일명·라인 포인터 필수 (❌/⚠️ 항목)
Rollback: Read-only → 불필요

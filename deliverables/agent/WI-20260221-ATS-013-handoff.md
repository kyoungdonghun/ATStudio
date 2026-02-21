[WI HEADER]
WI ID: WI-20260221-ATS-013
REQ: REQ-20260221-ATS-002
Agent: cr
Depends On: WI-20260221-ATS-012
Blocks: -

[WI SUMMARY]
Why: REQ-20260221-ATS-002 구현(f647b7f)에 대한 독립적 코드 리뷰. 7개 서비스 + 7개 컨트롤러 + 관련 DTO에 대해 Java 베스트 프랙티스, 보안, 트랜잭션 정확성, ATStudio 코딩 표준 준수 여부를 검증한다.
Scope (in/out):
  In:
    - Service 레이어 리뷰 (7개): TagService, UserService, UtilService, PlayHistoryService, LikeService, DownloadQueueService, NoticeService
    - Controller 레이어 리뷰 (7개): Tag/User/Util/PlayHistory/Like/DownloadQueue/NoticeController
    - DTO 리뷰: 각 서비스 관련 Request/Response DTO
    - 보안 관점: @PreAuthorize, 인가 로직, @AuthenticationPrincipal 사용
    - 트랜잭션: @Transactional 범위, readOnly 적용 여부
    - 예외처리: GlobalExceptionHandler 연동, 에러코드 일관성
  Out:
    - 테스트 코드 리뷰 (별도 WI)
    - Entity 레이어 (기존 코드, 이미 검증됨)
    - 새 기능 구현
    - 코드 자동 수정 (권고만 제공)
DoD:
  - 7개 서비스 각각 리뷰 완료
  - 7개 컨트롤러 각각 리뷰 완료
  - Critical/Major/Minor 이슈 분류 보고
  - 수정 권고사항 actionable하게 제시 (파일:라인 단위)
Constraints/Forbidden:
  - 코드 직접 수정 금지 — 권고사항만 보고
  - 범위 외 파일(Entity, Repository, Config) 수정 금지
  - 새 기능 제안 금지 (기존 구현 리뷰만)

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] 7개 서비스 전체 리뷰 완료 (TagService, UserService, UtilService, PlayHistoryService, LikeService, DownloadQueueService, NoticeService)
  - [ ] 7개 컨트롤러 전체 리뷰 완료
  - [ ] 각 이슈: 심각도(Critical/Major/Minor) + 파일:라인 + 수정 방법 명시
  - [ ] 보안 이슈 별도 섹션으로 강조
Performance:
  - [ ] N/A
Quality:
  - [ ] 리뷰 항목 누락 없음 (체크리스트 기반)
  - [ ] evidence-pack에 파일별 리뷰 결과 기록

[INPUT POINTERS]
Tier 0 (Constitution):
  - docs/standards/core-principles.md

Tier 0 (Standards):
  - docs/standards/development-standards.md

Tier 1 (Policies):
  - docs/policies/security-policy.md

Tier 2 (DTO Standards):
  - docs/standards/dto-standards.md
  - docs/standards/exception-handling.md

REQ/Context Docs:
  - deliverables/user/REQ-20260221-ATS-002.md

Service Files (primary review target):
  - src/main/java/com/atstudio/atstudio/service/TagService.java
  - src/main/java/com/atstudio/atstudio/service/UserService.java
  - src/main/java/com/atstudio/atstudio/service/UtilService.java
  - src/main/java/com/atstudio/atstudio/service/PlayHistoryService.java
  - src/main/java/com/atstudio/atstudio/service/LikeService.java
  - src/main/java/com/atstudio/atstudio/service/DownloadQueueService.java
  - src/main/java/com/atstudio/atstudio/service/NoticeService.java

Controller Files (primary review target):
  - src/main/java/com/atstudio/atstudio/controller/TagController.java
  - src/main/java/com/atstudio/atstudio/controller/UserController.java
  - src/main/java/com/atstudio/atstudio/controller/UtilController.java
  - src/main/java/com/atstudio/atstudio/controller/PlayHistoryController.java
  - src/main/java/com/atstudio/atstudio/controller/LikeController.java
  - src/main/java/com/atstudio/atstudio/controller/DownloadQueueController.java
  - src/main/java/com/atstudio/atstudio/controller/NoticeController.java

DTO Directory (reference):
  - src/main/java/com/atstudio/atstudio/dto/

Supporting Files (context only — do not modify):
  - src/main/java/com/atstudio/atstudio/exception/GlobalExceptionHandler.java
  - src/main/java/com/atstudio/atstudio/exception/ErrorCode.java
  - src/main/java/com/atstudio/atstudio/config/SecurityConfig.java
  - src/main/java/com/atstudio/atstudio/entity/ (all entity classes)

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260221-ATS-013-summary.md :
  - 전체 리뷰 요약 (Critical N, Major M, Minor K)
  - Critical 이슈 목록 (즉시 수정 필요)
  - Major 이슈 목록 (다음 WI에서 수정 권고)
  - 전반적 코드 품질 평가
Agent-facing -> deliverables/agent/WI-20260221-ATS-013-evidence-pack.md :
  - 파일별 상세 리뷰 결과
  - 이슈별: 파일:라인, 심각도, 현재 코드, 수정 제안
  - 보안 이슈 별도 섹션
  - 트랜잭션 분석 결과
Handoff Packet -> deliverables/agent/WI-20260221-ATS-013-handoff.md :
  - This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 모든 이슈에 파일:라인 번호 필수
Tests: N/A (리뷰 전용)
Rollback: N/A (코드 수정 없음)

[REVIEW CHECKLIST]
Per Service:
  - [ ] @Transactional 적용 여부 (클래스/메서드 레벨)
  - [ ] readOnly=true 적용 (조회 메서드)
  - [ ] 예외처리 일관성 (GlobalExceptionHandler 연동)
  - [ ] 의존성 주입 (@RequiredArgsConstructor + final)
  - [ ] N+1 문제 가능성
  - [ ] 비즈니스 로직이 Service에만 있는지 (Controller 누수 없는지)

Per Controller:
  - [ ] 얇은 컨트롤러 (thin controller) 원칙
  - [ ] @PreAuthorize 또는 SecurityConfig 기반 인가 정확성
  - [ ] @AuthenticationPrincipal CustomUserDetails 올바른 사용
  - [ ] ResponseEntity 반환 타입 + HTTP 상태코드 정확성
  - [ ] 요청 유효성 검증 (@Valid 등)
  - [ ] PathVariable/RequestParam 타입 안전성

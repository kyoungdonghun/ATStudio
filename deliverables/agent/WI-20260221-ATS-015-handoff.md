[WI HEADER]
WI ID: WI-20260221-ATS-015
REQ: REQ-20260221-ATS-003
Agent: se
Depends On: -
Blocks: WI-20260221-ATS-017

[WI SUMMARY]
Why: WI-013 코드 리뷰에서 발견된 N+1 쿼리 3건 수정. Like/DownloadQueue/PlayHistory 조회 시 Track이 LAZY 로드되어 N+1 쿼리 발생 → @EntityGraph로 EAGER fetch.
Scope (in/out):
  In:
    - LikeRepository.findAllByUser()에 @EntityGraph(attributePaths = "track") 추가 [M-01]
    - DownloadQueueRepository.findAllByUser()에 @EntityGraph(attributePaths = "track") 추가 [M-02]
    - PlayHistoryRepository.findAllByUserOrderByPlayedAtDesc()에 @EntityGraph(attributePaths = "track") 추가 [M-03]
  Out:
    - Repository 외 파일 수정 금지
    - 새 쿼리 메서드 추가 금지
    - 기존 메서드 서명 변경 금지
DoD:
  - 3개 Repository 메서드 각각 @EntityGraph 적용
  - @EntityGraph import 추가 (jakarta.persistence.EntityGraph 아님, Spring Data JPA 방식)
Constraints/Forbidden:
  - 수정 대상 3개 파일 외 변경 금지
  - 메서드 시그니처 변경 금지 (파라미터, 반환 타입 유지)
  - @Query 추가 금지 (@EntityGraph 어노테이션만 사용)

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] LikeRepository.findAllByUser(User user)에 @EntityGraph(attributePaths = "track") 적용
  - [ ] DownloadQueueRepository.findAllByUser(User user)에 @EntityGraph(attributePaths = "track") 적용
  - [ ] PlayHistoryRepository.findAllByUserOrderByPlayedAtDesc(User user, Pageable pageable)에 @EntityGraph(attributePaths = "track") 적용
  - [ ] import org.springframework.data.jpa.repository.EntityGraph 추가
Performance:
  - [ ] N+1 → 단일 JOIN 쿼리로 해결
Quality:
  - [ ] 컴파일 오류 없음

[INPUT POINTERS]
Tier 0 (Constitution):
  - docs/standards/core-principles.md

Tier 0 (Standards):
  - docs/standards/development-standards.md

REQ/Context Docs:
  - deliverables/user/REQ-20260221-ATS-003.md
  - deliverables/user/WI-20260221-ATS-013-summary.md

Files (primary targets):
  - src/main/java/com/atstudio/atstudio/repository/LikeRepository.java
  - src/main/java/com/atstudio/atstudio/repository/DownloadQueueRepository.java
  - src/main/java/com/atstudio/atstudio/repository/PlayHistoryRepository.java

Reference (entity 구조 확인용):
  - src/main/java/com/atstudio/atstudio/entity/Like.java
  - src/main/java/com/atstudio/atstudio/entity/DownloadQueue.java
  - src/main/java/com/atstudio/atstudio/entity/PlayHistory.java
  - src/main/java/com/atstudio/atstudio/entity/Track.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260221-ATS-015-summary.md :
  - 수정된 Repository 3개 목록 + @EntityGraph 적용 확인
Agent-facing -> deliverables/agent/WI-20260221-ATS-015-evidence-pack.md :
  - 수정 전/후 코드 스니펫
  - 예상 SQL JOIN 패턴 설명
Handoff Packet -> deliverables/agent/WI-20260221-ATS-015-handoff.md :
  - This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 각 Repository 파일:라인 번호 필수
Tests: WI-017 (qa)에서 검증
Rollback: git diff로 추적 가능

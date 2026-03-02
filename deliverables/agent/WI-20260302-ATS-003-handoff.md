[WI HEADER]
WI ID: WI-20260302-ATS-003
REQ: REQ-20260302-ATS-011
Agent: se
Depends On: -
Blocks: WI-20260302-ATS-004

[WI SUMMARY]
Why: 성능/안정성 3건 수정.
     CR-A-002 — TrackService 목록 필터 시 TrackTag JOIN 없이 tags 필터 → N+1 쿼리 발생.
     CR-A-005 — PlayHistoryService 단건 반복 save() → saveAll() 미사용으로 성능 저하.
     CR-C-004 — DownloadQueueService 클래스 레벨 @Transactional 누락 → 일관성 위험.
Scope (in):
  - TrackService.java / TrackRepository.java: N+1 제거 (@EntityGraph 또는 Specification과 함께 fetch join)
  - PlayHistoryService.java: 반복 save() → saveAll() 단일 호출
  - DownloadQueueService.java: 클래스 레벨 @Transactional(readOnly=true) 추가, mutating 메서드 override
  - 관련 단위 테스트 추가
Scope (out):
  - 다른 WI 범위 파일 수정
  - DB 스키마 변경
DoD:
  - Track 필터 조회 시 TrackTag 로딩에 N+1 없음
  - PlayHistory 복수 저장 시 saveAll() 단일 호출
  - DownloadQueueService 클래스 @Transactional(readOnly=true) 적용
  - 단위 테스트 0 failures
Constraints/Forbidden:
  - DB 스키마 변경 금지
  - @EntityGraph 추가 시 반드시 fetch=LAZY 연관만 대상으로 (불필요한 eager 방지)
  - 외부 라이브러리 추가 금지

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] TrackRepository 또는 TrackService: tags 필터 시 TrackTag JOIN 포함 → N+1 제거
  - [ ] PlayHistoryService: 여러 건 저장 시 saveAll() 호출 (단건 save() 루프 제거)
  - [ ] DownloadQueueService 클래스 선언: @Transactional(readOnly=true) 존재
  - [ ] DownloadQueueService mutating 메서드: @Transactional override 존재
Quality:
  - [ ] TrackServiceTest: 필터 조회 시 repository 호출 방식 검증
  - [ ] PlayHistoryServiceTest: saveAll() 호출 verify 테스트
  - [ ] DownloadQueueServiceTest: @Transactional 적용 확인
  - [ ] 기존 테스트 전체 통과

[INPUT POINTERS]
Tier 0:
  - docs/standards/core-principles.md
  - docs/standards/development-standards.md

REQ:
  - deliverables/user/REQ-20260302-ATS-011.md

감사 근거:
  - docs/audit/backend-audit-report.md ← CR-A-002, CR-A-005, CR-C-004

수정 대상 파일:
  - src/main/java/com/atstudio/atstudio/service/TrackService.java
  - src/main/java/com/atstudio/atstudio/repository/TrackRepository.java     ← @EntityGraph 추가 위치
  - src/main/java/com/atstudio/atstudio/repository/spec/TrackSpecification.java ← 현황 확인
  - src/main/java/com/atstudio/atstudio/service/PlayHistoryService.java
  - src/main/java/com/atstudio/atstudio/service/DownloadQueueService.java
  - src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java
  - src/test/java/com/atstudio/atstudio/service/PlayHistoryServiceTest.java
  - src/test/java/com/atstudio/atstudio/service/DownloadQueueServiceTest.java

참고:
  - src/main/java/com/atstudio/atstudio/entity/Track.java        ← trackTags @OneToMany 확인 (WI-003에서 추가됨)
  - src/main/java/com/atstudio/atstudio/entity/PlayHistory.java  ← 엔티티 구조 확인
  - docs/design/db-schema.md

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260302-ATS-003-summary.md
Agent-facing → deliverables/agent/WI-20260302-ATS-003-evidence-pack.md
  (수정 파일:라인 목록, @EntityGraph 적용 스니펫, saveAll 변경 전후, @Transactional 추가 스니펫)

[TRACEABILITY REQUIREMENTS]
Evidence: 수정 파일명·라인번호 필수
Tests: saveAll verify, N+1 제거 방식 명시
Rollback: git revert

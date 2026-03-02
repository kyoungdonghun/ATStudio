[WI HEADER]
WI ID: WI-20260302-ATS-006
REQ: REQ-20260302-ATS-011
Agent: cr
Depends On: WI-20260302-ATS-004
Blocks: -

[WI SUMMARY]
Why: WI-003(성능/안정성) 수정 코드 리뷰.
Scope (in): 성능/안정성 수정 파일 Read-only 리뷰
Scope (out): 코드 수정 금지

리뷰 대상:
  - TrackRepository.java (@EntityGraph 적용)
  - TrackService.java (buildTagsMap 제거, EntityGraph 활용)
  - PlayHistoryService.java (saveAll 검토 — 단건 API 확인)
  - DownloadQueueService.java (@Transactional 이미 적용)
  - 관련 테스트 파일들

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md, docs/standards/development-standards.md
REQ: deliverables/user/REQ-20260302-ATS-011.md
WI 결과: deliverables/user/WI-20260302-ATS-003-summary.md
회귀 검증: deliverables/user/WI-20260302-ATS-004-summary.md

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260302-ATS-005-summary.md → 아니라 WI-20260302-ATS-006-summary.md
Agent-facing → deliverables/agent/WI-20260302-ATS-006-evidence-pack.md

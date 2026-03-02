[WI HEADER]
WI ID: WI-20260302-ATS-005
REQ: REQ-20260302-ATS-011
Agent: cr
Depends On: WI-20260302-ATS-004
Blocks: -

[WI SUMMARY]
Why: WI-001(소유권/TestController) + WI-002(URL검증/RefreshToken) 보안 수정 코드 리뷰.
Scope (in): 보안 관련 수정 파일 전체 Read-only 리뷰
Scope (out): 코드 수정 금지

리뷰 대상:
  - NoticeService.java (validateNoticeOwnership 헬퍼)
  - NoticeController.java
  - PlaylistService.java (getOwnedPlaylist — 이미 구현됨 확인)
  - WhitelistChannelService.java (URI 파싱 URL 검증)
  - AuthService.java (RefreshToken 만료 체크)
  - BUSINESS_ERROR.java (REFRESH_TOKEN_EXPIRED 추가)
  - 관련 테스트 파일들

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md, docs/standards/development-standards.md
Tier 1: docs/policies/security-policy.md, docs/policies/access-control-policy.md
REQ: deliverables/user/REQ-20260302-ATS-011.md
WI 결과: deliverables/user/WI-20260302-ATS-001-summary.md, deliverables/user/WI-20260302-ATS-002-summary.md
회귀 검증: deliverables/user/WI-20260302-ATS-004-summary.md

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260302-ATS-005-summary.md
Agent-facing → deliverables/agent/WI-20260302-ATS-005-evidence-pack.md

[WI HEADER]
WI ID: WI-20260308-ATS-032
REQ: REQ-20260308-ATS-010
Agent: docops
Depends On: -
Blocks: -

---

[WI SUMMARY]
Why: MINOR — 프론트 준비 문서 path param 단축 기재({id}) → api-spec 실제 파라미터명 일괄 정리 + 기타 소규모 수정
Scope (in):
  - docs/check/atstudio-front-list.md — {id} → api-spec 실제 파라미터명 전면 교체 + 헤더 버전 수정
  - docs/check/modal-list.md — {id} → api-spec 실제 파라미터명 전면 교체
  - docs/check/screen-flow.md — Screen 8 ID 레이블 추가 + K-5 화면명 통일
Scope (out):
  - api-spec.md 수정 금지
  - 백엔드 코드 수정 금지
  - M-26/M-27 (PG 보류) 관련 내용 수정 금지
  - 화면/모달 추가·삭제 금지

DoD:
  - front-list 내 {id} 단축 표기 전부 api-spec 실제 파라미터명으로 교체
  - front-list 헤더 "API Spec v5 기준" → "API Spec v6 기준"
  - modal-list 내 {id} 단축 표기 전부 api-spec 실제 파라미터명으로 교체
  - screen-flow Section 4 내 Screen 8 "[8 재생목록 생성]" ID 레이블 추가
  - screen-flow K-5 화면명 "기업인증 심사" → "기업 인증 목록 / 심사 처리" 통일

Constraints/Forbidden:
  - 기존 화면/모달 내용 변경 금지 (path param명 수정만 허용)
  - MINOR 항목만 처리 (SUGGESTION-001 modal-list flow example 추가 제외)

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] front-list.md — `tracks/{id}/download` → `tracks/{trackId}/download`
- [ ] front-list.md — `playlists/{id}` → `playlists/{playlistId}` (§3.3, §3.5, §3.8)
- [ ] front-list.md — `users/{id}/licenses/{id}` → `users/{userId}/licenses/{licenseId}`
- [ ] front-list.md — 헤더 "API Spec v5 기준" → "API Spec v6 기준"
- [ ] modal-list.md — `playlists/{id}/tracks` → `playlists/{playlistId}/tracks` (M-05, M-12)
- [ ] modal-list.md — `playlists/{id}` → `playlists/{playlistId}` (M-07, M-13)
- [ ] modal-list.md — `tracks/{id}` → `tracks/{trackId}` (M-11)
- [ ] modal-list.md — `questions/{id}/status` → `questions/{questionId}/status` (M-18)
- [ ] modal-list.md — `whitelist-channels/{id}` → `whitelist-channels/{channelId}` (M-21)
- [ ] modal-list.md — `user-subscriptions/{id}` → `user-subscriptions/{userSubscriptionId}` (M-24)
- [ ] modal-list.md — `users/{id}` → `users/{userId}` (M-25)
- [ ] modal-list.md — `tags/{id}` → `tags/{tagId}` (M-28)
- [ ] screen-flow.md — Section 4에 Screen 8 "[8 재생목록 생성]" 레이블 추가
- [ ] screen-flow.md — K-5 "[K-5 기업인증 심사]" → "[K-5 기업 인증 목록 / 심사 처리]"

Quality:
- [ ] 수정 파일:라인 포인터 evidence에 포함
- [ ] 변경 내용이 api-spec 실제 파라미터명과 일치 확인

---

[INPUT POINTERS]

Tier 0 (Standards):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

REQ:
- deliverables/user/REQ-20260308-ATS-010.md

Phase 1 검증 근거:
- deliverables/agent/WI-20260307-ATS-027-evidence-pack.md  ← MINOR-001~010 전체 목록

api-spec (참조용, 수정 금지):
- docs/design/api-spec.md  ← path param명 확인용

Files (수정 대상):
- docs/check/atstudio-front-list.md
- docs/check/modal-list.md
- docs/check/screen-flow.md

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260308-ATS-032-summary.md
Agent-facing -> deliverables/agent/WI-20260308-ATS-032-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260308-ATS-032-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 수정 파일:라인 포인터 포함
Tests: 해당 없음 (문서 수정)

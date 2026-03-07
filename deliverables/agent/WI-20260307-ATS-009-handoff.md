[WI HEADER]
WI ID: WI-20260307-ATS-009
REQ: REQ-20260307-ATS-008
Agent: docops
Depends On: -
Blocks: -

---

[WI SUMMARY]
Why: Phase 1 Batch3 — Album/Playlist 도메인 교차 검증
Scope (in):
  - api-spec.md §3(Playlists), §15(Albums)
  - usecase/sound-album.md, usecase/sound-playlist.md
  - front-list.md: L-1, L-2, L-3, L-4, L-5, Screen 4, 5, 8, 9, C-1
  - modal-list.md: M-05, M-06, M-07, M-08, M-12, M-13, M-14
  - screen-flow.md: §4 (재생목록 흐름), §5 (앨범 흐름)
Scope (out):
  - 다른 배치(WI-007/008, WI-010~012) 담당 도메인 파일 탐색 금지
  - 문서 수정 금지 — 발견 + 보고만

DoD:
  - 위 문서 전부 읽고 교차 검증 완료
  - CONFLICT / GAP / OMISSION / SUGGESTION 형식으로 발견 목록 작성
  - 발견 없으면 "이상 없음" 명시

Constraints/Forbidden:
  - 환각 방지: 이 WI에 명시된 파일 외 다른 파일 탐색 금지
  - 문서 수정/생성 금지
  - api-spec.md는 §3/§15 섹션만 읽을 것

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] api-spec §3/§15 vs usecase 2종 교차 검증 완료
- [ ] usecase vs front-list 화면(L-1~5, Screen 4/5/8/9, C-1) 검증 완료
- [ ] front-list vs modal-list(M-05~08, M-12, M-13, M-14) 검증 완료
- [ ] screen-flow §4/§5 vs 위 문서들 정합 검증 완료
- [ ] Playlist 3개 제한 정책이 문서 간 일관되게 반영되어 있는지 검증

Quality:
- [ ] 각 발견 항목에 파일:섹션/라인 포인터 포함
- [ ] 심각도 분류 (CRITICAL / MAJOR / MINOR / SUGGESTION) 명시

---

[INPUT POINTERS]

Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Standards - docops):
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

REQ:
- deliverables/user/REQ-20260307-ATS-008.md

Files (검증 대상 — 이 목록 외 탐색 금지):
- docs/design/api-spec.md  ← §3 Playlists, §15 Albums 섹션만
- docs/design/usecase/sound-album.md
- docs/design/usecase/sound-playlist.md
- docs/check/atstudio-front-list.md  ← L-1~5, Screen 4, 5, 8, 9, C-1 항목만
- docs/check/modal-list.md  ← M-05, M-06, M-07, M-08, M-12, M-13, M-14 항목만
- docs/check/modal-list.md (Playlist 3-Item Limit Handling 섹션)
- docs/check/screen-flow.md  ← §4 재생목록 흐름, §5 앨범 흐름만

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260307-ATS-009-summary.md:
- 발견 요약 (CRITICAL/MAJOR/MINOR/SUGGESTION 건수), 주요 이슈 목록

Agent-facing -> deliverables/agent/WI-20260307-ATS-009-evidence-pack.md:
- 도메인별 상세 발견 목록 (심각도, 파일:섹션, 설명, 권고)
- 검증 완료된 항목 목록

Handoff Packet -> deliverables/agent/WI-20260307-ATS-009-handoff.md:
- 이 파일 (추적용)

---

[TRACEABILITY REQUIREMENTS]
Evidence: 각 발견 항목에 파일 경로 + 섹션명/라인 포인터 포함
Format:
  [CONFLICT] (파일A:섹션) vs (파일B:섹션) — 설명
  [GAP]      (문서A에 있음) vs (문서B에 없음) — 설명
  [OMISSION] (API/UC 정의 있음) + (화면/모달 미정의) — 설명
  [SUGGESTION] 제안 내용 — 설명

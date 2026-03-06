[WI HEADER]
WI ID: WI-20260306-ATS-004
REQ: REQ-20260306-ATS-004
Agent: cr
Depends On: WI-20260306-ATS-003
Blocks: -

---

[WI SUMMARY]

Why:
- 모든 정정 완료 후 "앨범" vs "재생목록" 의미 구별이 정확한지 독립 검증
- Playlist/Album 혼용 잔존 여부 및 영문 식별자 미변경 여부 확인

Scope:
- In:
  - WI-001 변경 문서 6개 리뷰
  - WI-002 Java 주석 변경분 리뷰 (변경 없을 경우 스킵)
  - 의미 구별 정확성 집중 검증 (Playlist → "재생목록", Album → "앨범" 유지)
- Out: 신규 기능 구현 리뷰 아님

DoD:
- CRITICAL 0, MAJOR 0
- 잔존 오기입 없음 확인
- Album 도메인 "앨범" 표기 보존 확인

Constraints/Forbidden:
- 코드 수정 금지 (리뷰 및 피드백만)

---

[ACCEPTANCE CRITERIA]

Quality:
- [ ] Playlist 맥락 "앨범" 잔존 0건
- [ ] Album 도메인 "앨범" 표기 보존 확인
- [ ] 영문 식별자 미변경 확인
- [ ] cr CRITICAL 0, MAJOR 0

---

[INPUT POINTERS]

Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Development Standards):
- docs/standards/development-standards.md

REQ/Context Docs:
- deliverables/user/REQ-20260306-ATS-004.md
- deliverables/agent/WI-20260306-ATS-001-evidence-pack.md
- deliverables/agent/WI-20260306-ATS-002-evidence-pack.md
- deliverables/agent/WI-20260306-ATS-003-evidence-pack.md

Files (리뷰 대상):
- docs/check/atstudio-front-list.md
- docs/design/api-spec.md (Section 3)
- docs/design/usecase/sound-playlist.md
- docs/design/usecase/index.md
- docs/design/db-schema.md
- docs/standards/glossary.md

참고 (변경 금지 기준):
- docs/design/api-spec.md (Section 15 — Album 유지 확인)
- docs/design/usecase/sound-album.md

---

[OUTPUT CONTRACT]

User-facing → deliverables/user/WI-20260306-ATS-004-summary.md:
- 리뷰 결과 (PASS / 이슈 목록)
- CRITICAL/MAJOR/MINOR 건수

Agent-facing → deliverables/agent/WI-20260306-ATS-004-evidence-pack.md:
- 이슈 발견 시 파일 경로 + 라인 + 내용
- 최종 판정: PASS or FAIL

Handoff Packet → deliverables/agent/WI-20260306-ATS-004-handoff.md:
- 이 파일

---

[TRACEABILITY REQUIREMENTS]

Evidence pointers:
- 이슈 발견 시 파일 경로 + 라인 번호 기록
- 최종 판정 근거

Tests: N/A
Rollback: N/A

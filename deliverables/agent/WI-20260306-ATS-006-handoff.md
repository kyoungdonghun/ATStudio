[WI HEADER]
WI ID: WI-20260306-ATS-006
REQ: REQ-20260306-ATS-005
Agent: cr
Depends On: WI-20260306-ATS-005
Blocks: -

---

[WI SUMMARY]

Why:
- 화면 목록 v2 작성 완료 후 정확성 독립 검증
- API 참조 누락, 명칭 혼용, 화면 수 오류 등 검출

Scope:
- In: `docs/ui/atstudio-front-list.md` v2 전체 리뷰
- Out: 문서 직접 수정 없음 (리뷰 및 피드백만)

DoD:
- CRITICAL 0, MAJOR 0
- 모든 화면에 API 참조 존재
- "재생목록"/"앨범" 혼용 없음
- 총 화면 수 정확

Constraints/Forbidden:
- 문서 직접 수정 금지

---

[ACCEPTANCE CRITERIA]

Quality:
- [ ] Album 섹션 5개 화면 — Section 15 API와 일치
- [ ] 재생목록 섹션 — Section 3 API 참조 완비 (3.3, 3.4, 3.8 포함)
- [ ] Screen 10 — 5.11 비밀번호 변경 모달 명시
- [ ] K-7 — 1.2, 1.6, 1.7 API 참조 정확
- [ ] 총 화면 수 카운트 정확
- [ ] CRITICAL 0, MAJOR 0

---

[INPUT POINTERS]

Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Development Standards):
- docs/standards/development-standards.md

REQ/Context Docs:
- deliverables/user/REQ-20260306-ATS-005.md
- deliverables/agent/WI-20260306-ATS-005-evidence-pack.md

Files (리뷰 대상):
- docs/ui/atstudio-front-list.md

Files (검증 기준):
- docs/design/api-spec.md (Section 3, 15 집중)

---

[OUTPUT CONTRACT]

User-facing → deliverables/user/WI-20260306-ATS-006-summary.md:
- 리뷰 결과 (PASS / FAIL)
- CRITICAL / MAJOR / MINOR 건수 및 내용

Agent-facing → deliverables/agent/WI-20260306-ATS-006-evidence-pack.md:
- 이슈 발견 시 파일 경로 + 라인 + 내용
- 최종 판정 근거

Handoff Packet → deliverables/agent/WI-20260306-ATS-006-handoff.md:
- 이 파일

---

[TRACEABILITY REQUIREMENTS]

Evidence pointers:
- 이슈 발견 시 라인 번호 기록
- 최종 판정 근거 명시

Tests: N/A
Rollback: N/A

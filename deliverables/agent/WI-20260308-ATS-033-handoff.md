[WI HEADER]
WI ID: WI-20260308-ATS-033
REQ: REQ-20260308-ATS-011
Agent: docops
Depends On: -
Blocks: -

---

[WI SUMMARY]
Why: development-standards.md에 TagController raw array 반환을 "의도된 예외"로 문서화
Scope (in):
  - docs/standards/development-standards.md — ResponseDTO 표준 섹션에 예외 규칙 추가
Scope (out):
  - TagController 코드 변경 금지
  - 다른 섹션 수정 금지

DoD:
  - ResponseDTO 표준 설명 근처에 "단순 참조 데이터(lookup data) 예외" 규칙 명시
  - 예외 조건 명확히: api-spec에 raw array로 명세된 경우에 한함
  - 현재 해당 예외 적용 엔드포인트: GET /api/tags (§2.2) 명시

Constraints/Forbidden:
  - 기존 내용 삭제/변경 금지 (추가만)

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] development-standards.md §2A (Controller 섹션 근처)에 예외 규칙 추가
      내용 예시:
      > **Exception — Lookup Data Raw Array**: Simple reference data endpoints (e.g., tag lists)
      > may return raw arrays (`ResponseEntity<List<T>>`) without ResponseDTO wrapper,
      > only when explicitly documented as raw array in api-spec.
      > Current exception: `GET /api/tags` (api-spec §2.2)

Quality:
- [ ] 기존 문서 포맷/스타일과 일치
- [ ] 수정 파일:라인 포인터 evidence에 포함

---

[INPUT POINTERS]

Tier 0 (Standards):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

REQ:
- deliverables/user/REQ-20260308-ATS-011.md

File (수정 대상):
- docs/standards/development-standards.md

참조 (수정 금지):
- src/main/java/com/atstudio/atstudio/controller/TagController.java
- docs/design/api-spec.md (§2.2)

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260308-ATS-033-summary.md
Agent-facing -> deliverables/agent/WI-20260308-ATS-033-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260308-ATS-033-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 수정 파일:라인 포인터 포함
Tests: 해당 없음 (문서 수정)

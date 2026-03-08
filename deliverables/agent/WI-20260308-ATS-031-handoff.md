[WI HEADER]
WI ID: WI-20260308-ATS-031
REQ: REQ-20260308-ATS-010
Agent: docops
Depends On: -
Blocks: -

---

[WI SUMMARY]
Why: MINOR — api-spec 에러 응답 4건 추가 (§10.1 409, §10.3 404, §11.1 409, §11.3 404)
Scope (in):
  - docs/design/api-spec.md §10.1 — 409 Conflict 에러 응답 추가
  - docs/design/api-spec.md §10.3 — 404 Not Found 에러 응답 추가
  - docs/design/api-spec.md §11.1 — 409 Conflict 에러 응답 추가
  - docs/design/api-spec.md §11.3 — 404 Not Found 에러 응답 추가
Scope (out):
  - 다른 섹션 수정 금지
  - 성공 응답 변경 금지
  - 백엔드 코드 수정 금지

DoD:
  - §10.1 POST /api/likes/{trackId} — 409 Conflict (이미 좋아요한 트랙) 에러 응답 추가
  - §10.3 DELETE /api/likes/{trackId} — 404 Not Found (좋아요 미존재) 에러 응답 추가
  - §11.1 POST /api/download-queue/{trackId} — 409 Conflict (이미 큐에 존재) 에러 응답 추가
  - §11.3 DELETE /api/download-queue/{trackId} — 404 Not Found (큐에 미존재) 에러 응답 추가
  - 기존 다른 섹션 패턴과 포맷 일치

Constraints/Forbidden:
  - 기존 UC 에러 응답 내용(likes.md, download-queue.md) 기반으로 추가
  - api-spec 버전 갱신 불필요 (이미 v6, MINOR 수정이므로 minor 버전 관리 생략)

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] §10.1 에러 응답 섹션에 409 Conflict 추가 ("Track already in likes")
- [ ] §10.3 에러 응답 섹션에 404 Not Found 추가 ("Track not in likes")
- [ ] §11.1 에러 응답 섹션에 409 Conflict 추가 ("Track already in queue")
- [ ] §11.3 에러 응답 섹션에 404 Not Found 추가 ("Track not in queue")

Quality:
- [ ] 에러 응답 포맷이 기존 섹션(§3.4, §6.3 등)과 일치
- [ ] 수정 파일:라인 포인터 evidence에 포함

---

[INPUT POINTERS]

Tier 0 (Standards):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

REQ:
- deliverables/user/REQ-20260308-ATS-010.md

Phase 1 검증 근거:
- deliverables/agent/WI-20260307-ATS-023-evidence-pack.md  ← MINOR-001~004 (에러 응답 누락)

근거 UC 파일 (참조용, 수정 금지):
- docs/design/usecase/likes.md
- docs/design/usecase/download-queue.md

File (수정 대상):
- docs/design/api-spec.md  ← §10.1, §10.3, §11.1, §11.3만 수정

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260308-ATS-031-summary.md
Agent-facing -> deliverables/agent/WI-20260308-ATS-031-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260308-ATS-031-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 수정 파일:라인 포인터 포함
Tests: 해당 없음 (문서 수정)

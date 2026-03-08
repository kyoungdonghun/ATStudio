[WI HEADER]
WI ID: WI-20260308-ATS-029
REQ: REQ-20260307-ATS-009
Agent: docops
Depends On: WI-023~027 (Phase 1 검증 완료)
Blocks: -

---

[WI SUMMARY]
Why: Phase 2 — MAJOR 문서 수정 4건 (M-1, M-2, M-6, M-7)
Scope (in):
  - M-1: docs/design/usecase/sound-track.md — §1.8 admin 트랙 목록 UC 신규 추가
  - M-2: docs/design/usecase/user-info.md — §5.11 비밀번호 변경 UC 신규 추가
  - M-6: docs/check/atstudio-front-list.md + screen-flow.md — POST /api/download-queue/{trackId} 경로 파라미터 수정
  - M-7: docs/check/screen-flow.md — M-17 컴포넌트명 StatusModal → ReviewModal 수정
  - docs/design/usecase/index.md — UC 카운트 갱신 (M-1, M-2 추가 시)
Scope (out):
  - MINOR/SUGGESTION 항목 수정 금지
  - 백엔드 코드 파일 수정 금지
  - api-spec.md 수정 금지 (MINOR 에러 응답 추가는 제외)

DoD:
  - sound-track.md에 admin track list UC 추가 (SOUND-XXX 다음 번호 부여)
  - user-info.md에 비밀번호 변경 UC 추가 (INFO-015 또는 다음 번호)
  - front-list.md 내 POST /api/download-queue → POST /api/download-queue/{trackId} 수정
  - screen-flow.md 내 동일 URL 수정 + M-17 컴포넌트명 ReviewModal 수정
  - index.md UC 카운트 갱신

Constraints/Forbidden:
  - 기존 UC 내용/번호 변경 금지
  - MINOR 항목(path parameter 단축 기재 등) 함께 수정 금지
  - api-spec.md 수정 금지

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] sound-track.md — admin 트랙 목록 UC 추가
      - Actor: Admin
      - API: GET /api/tracks/admin [ADMIN]
      - Query Params: page, size, isActive(optional)
      - 응답: AdminTrackListItemResponse (isActive 필드 포함)
- [ ] user-info.md — 비밀번호 변경 UC 추가
      - Actor: User (Member)
      - API: PUT /api/users/me/password [Auth]
      - 현재 비밀번호 확인 → 새 비밀번호 저장 → 204
      - Exception: 현재 비밀번호 불일치 → 400
- [ ] atstudio-front-list.md — `POST /api/download-queue` → `POST /api/download-queue/{trackId}` 전체 수정
- [ ] screen-flow.md — 동일 URL 수정 + M-17 `StatusModal` → `ReviewModal` 수정
- [ ] index.md — sound-track, user-info UC 카운트 갱신

Quality:
- [ ] 추가된 UC에 UC ID 부여 (기존 최대 번호 + 1)
- [ ] UC 형식이 기존 파일 패턴과 일치
- [ ] index.md 카운트 정확

---

[INPUT POINTERS]

Tier 0 (Standards):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

REQ:
- deliverables/user/REQ-20260307-ATS-009.md

Phase 1 검증 근거:
- deliverables/agent/WI-20260307-ATS-023-evidence-pack.md  ← MAJOR-001 (sound-track UC 누락)
- deliverables/agent/WI-20260307-ATS-024-evidence-pack.md  ← MAJOR-001 (user-info UC 누락)
- deliverables/agent/WI-20260307-ATS-027-evidence-pack.md  ← MAJOR-001/002 (front-list/screen-flow)

Files (수정 대상):
- docs/design/usecase/sound-track.md
- docs/design/usecase/user-info.md
- docs/design/usecase/index.md
- docs/check/atstudio-front-list.md
- docs/check/screen-flow.md

API Spec (참조용, 수정 금지):
- docs/design/api-spec.md  ← §1.8, §5.11 참조

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260308-ATS-029-summary.md
Agent-facing -> deliverables/agent/WI-20260308-ATS-029-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260308-ATS-029-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 수정 파일:라인 포인터 포함
Tests: 해당 없음 (문서 수정)

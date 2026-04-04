[WI HEADER]
WI ID: WI-20260307-ATS-027
REQ: REQ-20260307-ATS-009
Agent: docops
Depends On: -
Blocks: -

---

[WI SUMMARY]
Why: Track 3 — 프론트 준비 문서 내부 교차 검증 + api-spec 참조 유효성 확인 (read-only)
Scope (in):
  - docs/ui/atstudio-front-list.md (v4, 48개 화면)
  - docs/ui/modal-list.md (v1.2, 28개 모달)
  - docs/ui/screen-flow.md (v1.2, 48개 화면 흐름도)
  - docs/design/api-spec.md (API 참조 유효성 기준)
  - 검증 항목:
    1. front-list ↔ screen-flow: 화면 ID(A-1, B-1, ...) 및 화면명 일치
    2. front-list ↔ modal-list: 화면별 모달 참조 (M-xx) 유효성
    3. front-list ↔ api-spec: B/C/E/I/K/L/Screen 섹션 API 참조 유효성
       (존재하지 않는 API 참조, URL/Method 오류)
    4. screen-flow ↔ modal-list: 흐름도 내 모달 ID 유효성
    5. modal-list ↔ api-spec: 모달별 API 호출 참조 유효성
Scope (out):
  - 파일 수정 금지 (발견·보고만)
  - 백엔드 코드 검증 (WI-025/026 담당)
  - usecase ↔ api-spec 검증 (WI-023/024 담당)

DoD:
  - 5가지 교차 검증 각각 이슈 목록 산출
  - 프론트 착수 가능 여부 판단 ("착수 가능" / "수정 필요 N건")
  - CRITICAL/MAJOR/MINOR/SUGGESTION 분류 명시

Constraints/Forbidden:
  - 절대 파일 수정 금지
  - 모든 발견 이슈에 파일:라인 포인터 포함

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] front-list 48개 화면 ID 전수 확인 → screen-flow에 동일 ID 존재 여부
- [ ] front-list 각 화면의 modal 참조 (M-xx) → modal-list에 해당 모달 존재 여부
- [ ] front-list 각 화면의 API 참조 → api-spec에 해당 API 존재 여부 (섹션# 기준)
- [ ] screen-flow 흐름도 내 언급된 모달 ID → modal-list 유효성 확인
- [ ] modal-list 각 모달의 API 참조 → api-spec 유효성 확인
- [ ] 프론트 착수 가능성 종합 판단 포함

Quality:
- [ ] 발견 이슈별 파일:라인 포인터 포함
- [ ] CRITICAL: 화면 ID 불일치, 존재하지 않는 모달/API 참조
- [ ] MAJOR: 화면명 불일치, API URL 오류
- [ ] MINOR: 설명 누락, 오탈자, 참조 형식 불일치
- [ ] 최종 판단: "프론트 착수 가능" 또는 "수정 필요 (N건)"

---

[INPUT POINTERS]

Tier 0 (Standards):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

REQ:
- deliverables/user/REQ-20260307-ATS-009.md

Check Documents (검증 대상):
- docs/ui/atstudio-front-list.md
- docs/ui/modal-list.md
- docs/ui/screen-flow.md

API Spec (참조 유효성 기준):
- docs/design/api-spec.md

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260307-ATS-027-summary.md :
- 교차 검증 결과 요약 + 프론트 착수 가능 여부 판단
Agent-facing -> deliverables/agent/WI-20260307-ATS-027-evidence-pack.md :
- 이슈별 상세 근거 포인터 + 수정 제안
Handoff Packet -> deliverables/agent/WI-20260307-ATS-027-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 이슈별 파일:라인 포인터 필수
Tests: 해당 없음 (read-only 검증)

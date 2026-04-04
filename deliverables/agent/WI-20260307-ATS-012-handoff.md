[WI HEADER]
WI ID: WI-20260307-ATS-012
REQ: REQ-20260307-ATS-008
Agent: docops
Depends On: -
Blocks: -

---

[WI SUMMARY]
Why: Phase 1 Batch6 — 전체 교차 정합 검증 (Modal/Screen Flow/Front-list 3종)
Scope (in):
  - front-list.md 전체 (48개 화면)
  - modal-list.md 전체 (28개 모달)
  - screen-flow.md 전체 (v1.1)
  - 검증 관점: 도메인 WI(007~011)가 보기 어려운 cross-cutting 정합성
    - 화면 번호 참조 일치 (front-list ↔ modal-list ↔ screen-flow)
    - 모달 트리거 화면이 front-list에 실제로 존재하는지
    - screen-flow에서 참조하는 모달 번호가 modal-list에 실제로 존재하는지
    - 전체 화면 수/모달 수 카운트 일치
    - GNB 구조(§1)와 front-list 화면 접근 권한 일치
    - 전역 내비게이션 패턴(§11) vs 각 흐름 섹션 간 일치
Scope (out):
  - api-spec / usecase 파일 탐색 금지 (도메인 배치 WI 담당)
  - 문서 수정 금지 — 발견 + 보고만

DoD:
  - 3종 문서 전부 읽고 교차 정합 검증 완료
  - CONFLICT / GAP / OMISSION / SUGGESTION 형식으로 발견 목록 작성
  - 발견 없으면 "이상 없음" 명시

Constraints/Forbidden:
  - 환각 방지: front-list.md, modal-list.md, screen-flow.md 3종만 읽을 것. api-spec / usecase 파일 탐색 금지
  - 문서 수정/생성 금지

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] 모든 modal-list 항목의 "화면" 컬럼이 front-list에 실제 존재하는지 검증
- [ ] 모든 screen-flow 내 M-## 참조가 modal-list에 실제 존재하는지 검증
- [ ] front-list 화면 수 = 48개 (ERR-1/ERR-2 포함) 카운트 일치 검증
- [ ] modal-list 모달 수 = 28개 (M-01~M-28) 카운트 일치 검증
- [ ] GNB 구조(screen-flow §1)와 front-list 인증 권한([PUBLIC]/auth required/[ADMIN]) 일치 검증
- [ ] screen-flow §11 전역 패턴(404→ERR-1, 500→ERR-2)과 front-list ERR 항목 일치 검증
- [ ] Screen 2 제거 후 잔존 참조 없는지 전체 스캔

Quality:
- [ ] 각 발견 항목에 파일:라인 포인터 포함
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

Files (검증 대상 — 이 3종 외 탐색 금지):
- docs/ui/atstudio-front-list.md  ← 전체
- docs/ui/modal-list.md  ← 전체
- docs/ui/screen-flow.md  ← 전체

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260307-ATS-012-summary.md:
- 발견 요약 (CRITICAL/MAJOR/MINOR/SUGGESTION 건수), 주요 이슈 목록

Agent-facing -> deliverables/agent/WI-20260307-ATS-012-evidence-pack.md:
- 상세 발견 목록 (심각도, 파일:라인, 설명, 권고)
- 검증 완료된 항목 목록

Handoff Packet -> deliverables/agent/WI-20260307-ATS-012-handoff.md:
- 이 파일 (추적용)

---

[TRACEABILITY REQUIREMENTS]
Evidence: 각 발견 항목에 파일 경로 + 라인 포인터 포함
Format:
  [CONFLICT] (파일A:라인) vs (파일B:라인) — 설명
  [GAP]      (문서A에 있음) vs (문서B에 없음) — 설명
  [OMISSION] (정의 있음) + (참조 없음 또는 반대) — 설명
  [SUGGESTION] 제안 내용 — 설명

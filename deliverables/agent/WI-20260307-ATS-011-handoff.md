[WI HEADER]
WI ID: WI-20260307-ATS-011
REQ: REQ-20260307-ATS-008
Agent: docops
Depends On: -
Blocks: -

---

[WI SUMMARY]
Why: Phase 1 Batch5 — 기업인증/문의/공지/사업자/어드민 도메인 교차 검증
Scope (in):
  - api-spec.md §8(Questions), §9(Notices), §13(CompanyCert), §14(BusinessLicense) + Admin 전용 엔드포인트
  - usecase/company-certification.md, usecase/user-question.md, usecase/user-notice.md, usecase/business-license.md
  - front-list.md: I-1, I-2, Screen 13, 14, 15, 20, 21, 21-2, 22, K-1, K-2, K-3, K-4, K-5, K-6, K-7, Screen 18
  - modal-list.md: M-15, M-16, M-17, M-18, M-19, M-20, M-25, M-28
  - screen-flow.md: §9 (문의/공지 흐름), §10 (관리자 페이지 흐름), §8 중 기업인증 부분(I-1/I-2)
Scope (out):
  - 다른 배치(WI-007~010, WI-012) 담당 도메인 파일 탐색 금지
  - 문서 수정 금지 — 발견 + 보고만

DoD:
  - 위 문서 전부 읽고 교차 검증 완료
  - 기업인증 정책(REVISION_REQUESTED/REJECTED → admin 1:1 컨택, UI 재신청 없음)이 문서 간 일관되게 반영되어 있는지 검증
  - CONFLICT / GAP / OMISSION / SUGGESTION 형식으로 발견 목록 작성
  - 발견 없으면 "이상 없음" 명시

Constraints/Forbidden:
  - 환각 방지: 이 WI에 명시된 파일 외 다른 파일 탐색 금지
  - 문서 수정/생성 금지
  - api-spec.md는 §8/§9/§13/§14 + Admin 섹션만 읽을 것

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] api-spec §8/§9/§13/§14 vs usecase 4종 교차 검증 완료
- [ ] usecase vs front-list 화면(I-1/2, Screen 13~15, 20~22, K-1~7, Screen 18) 검증 완료
- [ ] front-list vs modal-list(M-15~20, M-25, M-28) 검증 완료
- [ ] screen-flow §8(기업인증)/§9/§10 vs 위 문서들 정합 검증 완료
- [ ] 기업인증 상태 전이 정책(REVISION_REQUESTED/REJECTED) 일관성 검증

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
- docs/design/api-spec.md  ← §8 Questions, §9 Notices, §13 CompanyCert, §14 BusinessLicense, Admin 엔드포인트 섹션만
- docs/design/usecase/company-certification.md
- docs/design/usecase/user-question.md
- docs/design/usecase/user-notice.md
- docs/design/usecase/business-license.md
- docs/check/atstudio-front-list.md  ← I-1/2, Screen 13~15, 20~22, K-1~7, Screen 18 항목만
- docs/check/modal-list.md  ← M-15, M-16, M-17, M-18, M-19, M-20, M-25, M-28 항목만
- docs/check/screen-flow.md  ← §8 중 I-1/I-2 기업인증, §9 문의/공지 흐름, §10 관리자 페이지 흐름만

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260307-ATS-011-summary.md:
- 발견 요약 (CRITICAL/MAJOR/MINOR/SUGGESTION 건수), 주요 이슈 목록

Agent-facing -> deliverables/agent/WI-20260307-ATS-011-evidence-pack.md:
- 도메인별 상세 발견 목록 (심각도, 파일:섹션, 설명, 권고)
- 검증 완료된 항목 목록

Handoff Packet -> deliverables/agent/WI-20260307-ATS-011-handoff.md:
- 이 파일 (추적용)

---

[TRACEABILITY REQUIREMENTS]
Evidence: 각 발견 항목에 파일 경로 + 섹션명/라인 포인터 포함
Format:
  [CONFLICT] (파일A:섹션) vs (파일B:섹션) — 설명
  [GAP]      (문서A에 있음) vs (문서B에 없음) — 설명
  [OMISSION] (API/UC 정의 있음) + (화면/모달 미정의) — 설명
  [SUGGESTION] 제안 내용 — 설명

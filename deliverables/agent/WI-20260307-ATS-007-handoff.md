[WI HEADER]
WI ID: WI-20260307-ATS-007
REQ: REQ-20260307-ATS-008
Agent: docops
Depends On: -
Blocks: -

---

[WI SUMMARY]
Why: Phase 1 Batch1 — Auth/User/MyPage/좋아요/라이선스/재생기록/채널 도메인 교차 검증
Scope (in):
  - api-spec.md §5(Users/Auth), §4(PlayHistory), §10(Likes), §7(Licenses), §12(Whitelist)
  - usecase/user-info.md, usecase/sound-playhistory.md, usecase/likes.md, usecase/user-license.md, usecase/whitelist.md
  - front-list.md: A-1~A-4, Screen 10, D-1, E-1, F-1, F-2, H-1
  - modal-list.md: M-01, M-02, M-21, M-23
  - screen-flow.md: §2 (인증 흐름), §8 (개인 페이지 흐름)
Scope (out):
  - 다른 배치(WI-008~012) 담당 도메인 파일 탐색 금지
  - 문서 수정 금지 — 발견 + 보고만

DoD:
  - 위 문서 전부 읽고 교차 검증 완료
  - CONFLICT / GAP / OMISSION / SUGGESTION 형식으로 발견 목록 작성
  - 발견 없으면 "이상 없음" 명시

Constraints/Forbidden:
  - 환각 방지: 이 WI에 명시된 파일 외 다른 파일 탐색 금지
  - 문서 수정/생성 금지
  - api-spec.md는 §5/§4/§10/§7/§12 섹션만 읽을 것

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] api-spec §5/§4/§10/§7/§12 vs usecase 5종 교차 검증 완료
- [ ] usecase vs front-list 화면(A-1~4, 10, D-1, E-1, F-1/2, H-1) 검증 완료
- [ ] front-list vs modal-list(M-01, M-02, M-21, M-23) 검증 완료
- [ ] screen-flow §2/§8 vs 위 문서들 정합 검증 완료

Quality:
- [ ] 각 발견 항목에 파일:섹션/라인 포인터 포함
- [ ] 심각도 분류 (CRITICAL / MAJOR / MINOR / SUGGESTION) 명시
- [ ] 발견 없는 영역도 "검증 완료" 명시

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
- docs/design/api-spec.md  ← §5 Users/Auth, §4 PlayHistory, §10 Likes, §7 Licenses, §12 Whitelist 섹션만
- docs/design/usecase/user-info.md
- docs/design/usecase/sound-playhistory.md
- docs/design/usecase/likes.md
- docs/design/usecase/user-license.md
- docs/design/usecase/whitelist.md
- docs/ui/atstudio-front-list.md  ← A-1~A-4, Screen 10, D-1, E-1, F-1, F-2, H-1 항목만
- docs/ui/modal-list.md  ← M-01, M-02, M-21, M-23 항목만
- docs/ui/screen-flow.md  ← §2 인증 흐름, §8 개인 페이지 흐름만

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260307-ATS-007-summary.md:
- 발견 요약 (CRITICAL/MAJOR/MINOR/SUGGESTION 건수), 주요 이슈 목록

Agent-facing -> deliverables/agent/WI-20260307-ATS-007-evidence-pack.md:
- 도메인별 상세 발견 목록 (심각도, 파일:섹션, 설명, 권고)
- 검증 완료된 항목 목록

Handoff Packet -> deliverables/agent/WI-20260307-ATS-007-handoff.md:
- 이 파일 (추적용)

---

[TRACEABILITY REQUIREMENTS]
Evidence: 각 발견 항목에 파일 경로 + 섹션명/라인 포인터 포함
Format:
  [CONFLICT] (파일A:섹션) vs (파일B:섹션) — 설명
  [GAP]      (문서A에 있음) vs (문서B에 없음) — 설명
  [OMISSION] (API/UC 정의 있음) + (화면/모달 미정의) — 설명
  [SUGGESTION] 제안 내용 — 설명

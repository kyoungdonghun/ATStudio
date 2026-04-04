[WI HEADER]
WI ID: WI-20260306-ATS-005
REQ: REQ-20260306-ATS-005
Agent: docops
Depends On: -
Blocks: WI-20260306-ATS-006

---

[WI SUMMARY]

Why:
- `docs/ui/atstudio-front-list.md` v1은 2026-02-20 작성 초본으로 Album 도메인 누락 및 여러 컨펌 포인트 미반영
- 확정된 7개 변경 항목을 반영하여 v2로 갱신

Scope:
- In: `docs/ui/atstudio-front-list.md` 단일 파일 전면 재작성 (v2)
- Out: 다른 문서 변경 없음, 백엔드 코드 변경 없음

DoD:
1. Album 섹션 5개 화면 추가 (Track 대칭)
2. 재생목록 상세 화면 추가
3. Screen 9, 10 API 참조 보완
4. 삭제 정책 명시 (confirm() / 비밀번호 모달)
5. K-7 어드민 트랙 관리 화면 추가
6. 총 화면 수 정확히 업데이트
7. 명칭 일관성: Playlist → "재생목록", Album → "앨범"

Constraints/Forbidden:
- Playlist 관련 "재생목록" 표기 유지 (REQ-20260306-ATS-004 확정)
- Album 관련 "앨범" 표기 유지
- 기존 화면 No 체계 최대한 유지 (신규 화면은 체계에 맞게 부여)
- API URL 표기 형식 유지 (`섹션번호.엔드포인트` 형식)

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] `## 💿 앨범 (Album)` 섹션 신규 추가, 5개 화면 포함
- [ ] `## 💿 재생목록 (Playlist)` 섹션: 상세 화면 추가, Screen 9 API 참조 보완
- [ ] `## 👤 개인 페이지` 섹션: Screen 10에 `5.11 PUT /api/users/me/password` + 모달 명시
- [ ] 삭제 정책 각 섹션 또는 상단 범례에 명시
- [ ] `## 🛡️ 관리자 페이지` 섹션: K-7 트랙 관리 추가
- [ ] 문서 하단 총 화면 수 정확히 업데이트
- [ ] "재생목록"/"앨범" 혼용 없음

Quality:
- [ ] 모든 화면에 관련 API 참조 기재
- [ ] 인증 컬럼 ([PUBLIC] / auth required / [ADMIN]) 정확히 기재

---

[INPUT POINTERS]

Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Documentation Standards):
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

REQ/Context Docs:
- deliverables/user/REQ-20260306-ATS-005.md
- deliverables/user/REQ-20260306-ATS-004.md (명칭 확정 참고)

Files (참고 — 현재 파일):
- docs/ui/atstudio-front-list.md (v1 현재 내용)

Files (API 참고):
- docs/design/api-spec.md (섹션 1~15 전체, 특히 Section 3 Playlist, Section 15 Album)
- docs/design/usecase/index.md

---

[OUTPUT CONTRACT]

User-facing → deliverables/user/WI-20260306-ATS-005-summary.md:
- 변경 항목별 완료 여부
- 추가된 화면 목록 및 최종 총 화면 수

Agent-facing → deliverables/agent/WI-20260306-ATS-005-evidence-pack.md:
- 섹션별 변경 내역 (추가/수정 화면 목록)
- 총 화면 수 카운트 근거

Handoff Packet → deliverables/agent/WI-20260306-ATS-005-handoff.md:
- 이 파일

---

[TRACEABILITY REQUIREMENTS]

Evidence pointers:
- 추가된 화면 No 목록 및 각 API 참조
- 총 화면 수 카운트 (섹션별 소계 + 합계)

Tests: N/A
Rollback: git checkout -- docs/ui/atstudio-front-list.md

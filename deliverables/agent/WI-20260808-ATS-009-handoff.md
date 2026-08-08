[WI HEADER]
WI ID: WI-20260808-ATS-009
REQ: REQ-20260808-ATS-003
Agent: tr
Depends On: -
Blocks: WI-20260808-ATS-011
[WI SUMMARY]
Why: 메인 화면 태그 탐색이 장르·분위기에만 제한된 현재 상태를 실제 태그 데이터와 검색 정책에 맞게 개선할 근거를 만든다.
Scope (in/out): 공개 화면/API, Home/TrackList 코드, 태그·음원 use case, 기존 SR-04의 읽기 전용 조사와 WI 산출물만 포함한다. 코드, SR, 태그 데이터는 수정하지 않는다.
DoD: `INSTRUMENT`·`USAGE` 미노출 사실, 실제 데이터, 검색 연결 가능성, Usage 우선 정보 구조와 과밀화 대안이 정리된다.
Constraints/Forbidden: `USAGE`를 라이선스로 재정의하지 않는다. 데이터가 없다고 추측하지 않는다. 홈에 네 섹션을 단순 누적하는 안만 제시하지 않는다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 메인 화면이 GENRE/MOOD만 fetch·render하는 사실을 확인한다.
- [ ] 공개 API의 GENRE 5, MOOD 4, INSTRUMENT 4, USAGE 1 현황을 기록한다.
- [ ] Track 목록이 instrument/usage query를 이미 지원하는지 확인한다.
- [ ] `USAGE`를 우선 노출하고 `INSTRUMENT`도 빠짐없이 제공하는 IA 대안을 비교·권고한다.
- [ ] SR-04와 새 SR의 범위를 분리한다.
Performance:
- [ ] 해당 없음(읽기 전용 조사).
Quality:
- [ ] 현재 사실, 콘텐츠 품질 문제, UI 제안, 후속 정책을 분리한다.
- [ ] 모바일/홈 과밀화와 빈 카테고리 처리 기준을 포함한다.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/glossary.md

Tier 2 (Task Context):
- docs/design/usecase/sound-tag.md
- docs/design/usecase/sound-track.md
- docs/SR/SR-04.md
- docs/ui/mockup/main.html

REQ/Context Docs:
- deliverables/user/REQ-20260808-ATS-003.md

Files:
- frontend/src/pages/public/HomePage.tsx
- frontend/src/pages/public/TrackListPage.tsx
- frontend/src/components/filter/TagFilterModal.tsx
- frontend/src/api/tags.ts
- src/main/java/com/atstudio/atstudio/service/TagService.java
- src/main/java/com/atstudio/atstudio/service/TrackService.java

Runtime evidence:
- Main DOM: `장르별 탐색`, `분위기별 탐색`만 존재
- `GET /api/tags?type=GENRE`: 5개
- `GET /api/tags?type=MOOD`: 4개
- `GET /api/tags?type=INSTRUMENT`: 4개
- `GET /api/tags?type=USAGE`: 1개 (`#비가오면`)

Repro/Logs:
- Browser DOM snapshot of `/`
- `GET /api/tags?type={GENRE|MOOD|INSTRUMENT|USAGE}`
- `rg -n "fetchTags|장르별 탐색|분위기별 탐색|instrument|usage" frontend/src docs/design docs/SR`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-009-summary.md : current gap, priorities, IA recommendation
Agent-facing -> deliverables/agent/WI-20260808-ATS-009-evidence-pack.md : evidence pointers, option comparison, SR-100 requirements
Handoff Packet -> deliverables/agent/WI-20260808-ATS-009-handoff.md : This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: 필요한 Home query/navigation/mobile/empty-state 회귀 테스트를 명시
Rollback: 읽기 전용 조사 산출물 제거 방법 기록

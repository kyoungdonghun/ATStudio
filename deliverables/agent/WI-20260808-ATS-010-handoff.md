[WI HEADER]
WI ID: WI-20260808-ATS-010
REQ: REQ-20260808-ATS-003
Agent: qa-fe
Depends On: -
Blocks: WI-20260808-ATS-011
[WI SUMMARY]
Why: 재생 지연 안내의 실제 이벤트 조건과 앨범 전체 재생에서 waveform이 사라지는 데이터 흐름을 재현·분석한다.
Scope (in/out): 공개 앨범/재생기, player store/bar/canvas, 앨범·플레이리스트·좋아요·다운로드·재생기록 Track 매핑과 API DTO의 읽기 전용 조사 및 WI 산출물만 포함한다. 코드, SR, 데이터는 수정하지 않는다.
DoD: 지연 안내 조건·해제 조건·순간 노출 재현, waveform null 원인, 다른 영향 화면, 권고 상태 모델과 데이터 계약이 근거와 함께 정리된다.
Constraints/Forbidden: native `waiting`을 곧 재생 실패로 단정하지 않는다. 가짜 waveform으로 해결하지 않는다. 앨범 한 화면만의 예외 패치로 권고하지 않는다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `waiting`/`stalled`에서 isStalled가 즉시 true이고 `timeupdate`/`canplay`/`playing`에서 해제되는 사실을 확인한다.
- [ ] 앨범 2 전체 재생 직후 지연 문구가 나타났다가 약 1.8초 안에 재생으로 전환된 런타임 관찰을 기록한다.
- [ ] 앨범 DTO가 duration/waveform을 제공하지 않고 AlbumDetailPage가 `waveformData:null` Track을 구성하는 원인을 확인한다.
- [ ] PlaylistDetail, LikeList, DownloadHistory, HistoryModal/PlaylistDrawer의 동일 축약 매핑을 교차검증한다.
- [ ] 지속 시간 기반 buffering UX와 full Track hydration/DTO 계약 대안을 제안한다.
Performance:
- [ ] N+1 상세 조회를 무비판적으로 권고하지 않고 batch/DTO 확장/온디맨드 hydration 대안을 비교한다.
Quality:
- [ ] 정상 시작 지연, 지속 버퍼링, 재생 오류를 분리한다.
- [ ] 실제 waveform peak 데이터와 flat-line fallback을 구분한다.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1/2 (Policies and Tech Context):
- docs/policies/quality-gates.md
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
- docs/SR/SR-90.md
- docs/design/usecase/sound-track.md

REQ/Context Docs:
- deliverables/user/REQ-20260808-ATS-003.md

Files:
- frontend/src/store/playerStore.ts
- frontend/src/layouts/PlayerBar.tsx
- frontend/src/components/player/WaveformCanvas.tsx
- frontend/src/pages/public/AlbumDetailPage.tsx
- frontend/src/api/albums.ts
- src/main/java/com/atstudio/atstudio/dto/album/AlbumTrackItemResponse.java
- frontend/src/pages/subscriber/PlaylistDetailPage.tsx
- frontend/src/pages/subscriber/LikeListPage.tsx
- frontend/src/pages/subscriber/DownloadHistoryPage.tsx
- frontend/src/components/player/HistoryModal.tsx
- frontend/src/components/player/PlaylistDrawer.tsx
- frontend/src/store/playerStore.test.ts
- frontend/src/layouts/PlayerBar.test.tsx

Runtime evidence:
- `/albums/2` title `앨범 생성 테스트02`, one track (`trackId=2`)
- 전체 재생 직후 `재생이 지연되고 있습니다...` status + retry; 약 1.8초 후 재생 0:01/1:33으로 전환
- `GET /api/albums/2` track item has only id/title/artist/thumbnail/order
- AlbumDetailPage constructs duration 0 and waveformData null; WaveformCanvas renders a flat line when peaks are empty

Repro/Logs:
- Browser navigate `/albums/2`, click `전체 재생`, immediate and delayed DOM snapshots
- `GET /api/albums/2`
- `rg -n "isStalled|waiting|stalled|waveformData: null|WaveformCanvas|playAll" frontend/src src/main/java src/test/java`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-010-summary.md : condition explanation, bug scope, recommendations
Agent-facing -> deliverables/agent/WI-20260808-ATS-010-evidence-pack.md : runtime/code evidence, alternative analysis, tests, SR-101 requirements
Handoff Packet -> deliverables/agent/WI-20260808-ATS-010-handoff.md : This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: fake-timer stalled timing, album/playlist/like/history waveform hydration, next/prev queue tests를 명시
Rollback: 읽기 전용 조사 산출물 제거 방법 기록

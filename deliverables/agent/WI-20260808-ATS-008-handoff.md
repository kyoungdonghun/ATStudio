[WI HEADER]
WI ID: WI-20260808-ATS-008
REQ: REQ-20260808-ATS-003
Agent: qa-integ
Depends On: -
Blocks: WI-20260808-ATS-011
[WI SUMMARY]
Why: 공개 화면의 음원 길이와 실제 미디어 길이가 다른 원인과 영향 범위를 교차 레이어로 확정한다.
Scope (in/out): 공개 UI/API/스트림, Track 생성·수정·DTO·화면·테스트의 읽기 전용 조사와 WI 산출물만 포함한다. 코드, DB, SR, 공개 데이터는 수정하지 않는다.
DoD: 세 트랙의 수치, 근본 원인, 영향 화면, 생성/수정/백필 요구와 테스트가 근거 포인터로 정리된다.
Constraints/Forbidden: 특정 한 곡 문제로 단정하지 않는다. 파일 크기/고정 bitrate 추정을 다른 고정값으로 교체하는 안을 권고하지 않는다. 실제 duration이나 DB를 변경하지 않는다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Track 1~3의 API duration과 실제 플레이어 duration을 표로 제시한다.
- [ ] 스트림 파일 크기와 실제 시간에서 약 320kbps임을 확인하고 128kbps 추정 오차를 설명한다.
- [ ] 생성 시 duration 추출과 음원 교체 시 duration 미갱신을 구분한다.
- [ ] Home, Track 목록, player fallback, API/다운로드 응답 등 영향 범위를 정리한다.
- [ ] 정확한 메타데이터 추출, 실패 정책, 기존 데이터 audit/backfill, 회귀 테스트 요구를 제안한다.
Performance:
- [ ] 읽기 전용 조사이며 대용량 전체 다운로드 없이 Range 헤더와 브라우저 메타데이터를 사용한다.
Quality:
- [ ] UI/API/실제 미디어/저장 값을 분리한다.
- [ ] 모든 핵심 판단에 파일·라인 또는 재현 명령 포인터가 있다.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1/2 (Policies and Task Context):
- docs/policies/quality-gates.md
- docs/design/api-spec.md
- docs/design/usecase/sound-track.md

REQ/Context Docs:
- deliverables/user/REQ-20260808-ATS-003.md

Files:
- src/main/java/com/atstudio/atstudio/service/TrackService.java
- src/main/java/com/atstudio/atstudio/entity/Track.java
- src/main/java/com/atstudio/atstudio/dto/track/
- src/main/java/com/atstudio/atstudio/dto/download/DownloadHistoryItemResponse.java
- src/test/java/com/atstudio/atstudio/service/TrackServiceAudioProcessingTest.java
- frontend/src/pages/public/HomePage.tsx
- frontend/src/pages/public/TrackListPage.tsx
- frontend/src/components/track/TrackRow.tsx
- frontend/src/store/playerStore.ts
- frontend/src/layouts/PlayerBar.tsx

Runtime evidence:
- Public base: `https://comparable-indicate-black-guidelines.trycloudflare.com`
- Track 1: API 229s (3:49), media 1:33, 3,756,312 bytes
- Track 2: API 229s (3:49), media 1:33, 3,756,312 bytes
- Track 3: API 1090s (18:10), media 7:26, 17,863,782 bytes

Repro/Logs:
- `GET /api/tracks/{1|2|3}`
- `GET /api/tracks/{1|2|3}/stream` with `Range: bytes=0-0`
- Browser play on `/tracks/{1|2|3}` and player total-time observation
- `rg -n "extractDuration|duration|loadedmetadata" frontend/src src/main/java src/test/java`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-008-summary.md : current finding, affected scope, recommendations
Agent-facing -> deliverables/agent/WI-20260808-ATS-008-evidence-pack.md : evidence pointers, calculations, tests, SR-99 requirements
Handoff Packet -> deliverables/agent/WI-20260808-ATS-008-handoff.md : This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: 실행하지 않더라도 생성·수정·VBR/CBR·백필 회귀 테스트를 명시
Rollback: 읽기 전용 조사 산출물 제거 방법 기록

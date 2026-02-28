[WI HEADER]
WI ID: WI-20260227-ATS-029
REQ: REQ-20260227-ATS-009
Agent: cr
Depends On: WI-20260227-ATS-028 (sa — 체크리스트 완료)
Blocks: WI-20260227-ATS-032

[WI SUMMARY]
Why: 백엔드 감사 Phase 2-A. Track·License·Tag·Playlist·PlayHistory 도메인 코드를 WI-028 체크리스트 기준으로 검토. Read-only.
Scope (in):
  - 섹션 1.x Track (7 APIs), 7.x License (4 APIs), 2.x Tag (4 APIs), 3.x Playlist (8 APIs), 4.x PlayHistory (3 APIs) — 총 26개 API
  - Controller / Service / Repository / Entity / DTO 전 레이어
  - 검토 기준: WI-028 evidence-pack 체크리스트
Scope (out): 코드 수정, 타 도메인 검토
DoD:
  - 26개 API 각각에 대해 ✅/⚠️/❌/📋 판정
  - 발견 이슈에 파일·라인 포인터 포함
  - evidence-pack.md 생성
Constraints/Forbidden:
  - 코드 수정 절대 금지 — 발견 보고만

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] Track 1.1~1.7 전 API 검토 완료
  - [ ] License 7.1~7.4 전 API 검토 완료
  - [ ] Tag 2.1~2.4 전 API 검토 완료
  - [ ] Playlist 3.1~3.8 전 API 검토 완료
  - [ ] PlayHistory 4.1~4.3 전 API 검토 완료
  - [ ] 각 항목 ✅/⚠️/❌/📋 판정
Quality:
  - [ ] ❌ 항목에는 파일명·라인번호 포함
  - [ ] 판정 근거 명시

[INPUT POINTERS]
Tier 0:
  - docs/standards/core-principles.md
  - docs/standards/development-standards.md

검토 기준 (반드시 먼저 읽을 것):
  - deliverables/agent/WI-20260227-ATS-028-evidence-pack.md  ← 체크리스트

검토 대상 파일:
  - src/main/java/com/atstudio/atstudio/controller/TrackController.java
  - src/main/java/com/atstudio/atstudio/service/TrackService.java
  - src/main/java/com/atstudio/atstudio/entity/Track.java
  - src/main/java/com/atstudio/atstudio/controller/LicenseController.java
  - src/main/java/com/atstudio/atstudio/service/LicenseService.java
  - src/main/java/com/atstudio/atstudio/entity/License.java
  - src/main/java/com/atstudio/atstudio/controller/TagController.java
  - src/main/java/com/atstudio/atstudio/service/TagService.java
  - src/main/java/com/atstudio/atstudio/entity/Tag.java
  - src/main/java/com/atstudio/atstudio/entity/TrackTag.java
  - src/main/java/com/atstudio/atstudio/controller/PlaylistController.java
  - src/main/java/com/atstudio/atstudio/service/PlaylistService.java
  - src/main/java/com/atstudio/atstudio/entity/Playlist.java
  - src/main/java/com/atstudio/atstudio/entity/PlaylistTrack.java
  - src/main/java/com/atstudio/atstudio/controller/PlayHistoryController.java
  - src/main/java/com/atstudio/atstudio/service/PlayHistoryService.java
  - src/main/java/com/atstudio/atstudio/entity/PlayHistory.java
  - src/main/java/com/atstudio/atstudio/service/DownloadService.java
  - src/main/java/com/atstudio/atstudio/entity/TrackDownload.java
  - src/main/java/com/atstudio/atstudio/dto/  (Glob으로 track/license/tag/playlist 관련 DTO)
  - src/main/java/com/atstudio/atstudio/repository/  (관련 Repository)
  - src/main/java/com/atstudio/atstudio/config/SecurityConfig.java  (권한 설정 참조)

[OUTPUT CONTRACT]
User-facing  → deliverables/user/WI-20260227-ATS-029-summary.md
Agent-facing → deliverables/agent/WI-20260227-ATS-029-evidence-pack.md
  형식:
  ## cr-A 검토 결과: Track·License·Tag·Playlist·PlayHistory
  | 도메인 | API | 판정 | 발견 이슈 | 파일:라인 |
  |--------|-----|------|----------|---------|
  | Track | 1.1 POST /api/tracks | ✅ | - | - |
  | Track | 1.5 GET /api/tracks/{id}/download | ❌ | N+1 위험 | TrackService.java:123 |

[TRACEABILITY REQUIREMENTS]
Evidence: 파일명·라인 포인터 필수 (❌/⚠️ 항목)
Rollback: Read-only → 불필요

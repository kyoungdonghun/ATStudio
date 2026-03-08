[WI HEADER]
WI ID: WI-20260307-ATS-023
REQ: REQ-20260307-ATS-009
Agent: cr
Depends On: -
Blocks: -

---

[WI SUMMARY]
Why: Track 1-A — api-spec §1~4, §10~11 ↔ usecase 정합성 검증 (read-only)
Scope (in):
  - api-spec.md §1(Track), §2(Tag), §3(Playlist), §4(PlayHistory), §10(Likes), §11(DownloadQueue)
  - usecase: sound-track.md, sound-tag.md, sound-playlist.md, sound-playhistory.md, likes.md, download-queue.md
  - 검증 항목: UC ID별 API URL/Method/응답코드/권한/요청·응답 필드 일치 여부
Scope (out):
  - 파일 수정 금지 (발견·보고만)
  - §5 이후 섹션
  - 백엔드 코드 검토 (Track2-A가 담당)

DoD:
  - 6개 도메인 각각 불일치 항목 목록 산출
  - CRITICAL/MAJOR/MINOR/SUGGESTION 분류 명시
  - 발견 없으면 "PASS" 명시

Constraints/Forbidden:
  - 절대 파일 수정 금지
  - 판단 근거(api-spec 섹션 + usecase UC ID)를 증거로 명시

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] api-spec §1 Track (1.1~1.8) ↔ sound-track.md UC ID 교차 확인
- [ ] api-spec §2 Tag (2.1~2.4) ↔ sound-tag.md 교차 확인
- [ ] api-spec §3 Playlist (3.1~3.8) ↔ sound-playlist.md 교차 확인
- [ ] api-spec §4 PlayHistory (4.1~4.3) ↔ sound-playhistory.md 교차 확인
- [ ] api-spec §10 Likes (10.1~10.3) ↔ likes.md 교차 확인
- [ ] api-spec §11 DownloadQueue (11.1~11.3) ↔ download-queue.md 교차 확인
- [ ] 검증 항목: URL, HTTP Method, 상태코드, 권한(Auth), 주요 요청·응답 필드

Quality:
- [ ] 이슈 각각에 근거 포인터 (api-spec 섹션# + usecase UC-ID + 라인) 포함
- [ ] CRITICAL: API 존재 여부 불일치, 상태코드 오류, 권한 오류
- [ ] MAJOR: 필드명 불일치, 요청·응답 구조 불일치
- [ ] MINOR: 설명 누락, 예시 오류, 오탈자

---

[INPUT POINTERS]

Tier 0 (Standards):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

REQ:
- deliverables/user/REQ-20260307-ATS-009.md

API Spec (검증 기준):
- docs/design/api-spec.md  ← §1(Track), §2(Tag), §3(Playlist), §4(PlayHistory), §10(Likes), §11(DownloadQueue)

Usecase (검증 기준):
- docs/design/usecase/sound-track.md
- docs/design/usecase/sound-tag.md
- docs/design/usecase/sound-playlist.md
- docs/design/usecase/sound-playhistory.md
- docs/design/usecase/likes.md
- docs/design/usecase/download-queue.md

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260307-ATS-023-summary.md :
- 도메인별 발견 이슈 요약 (CRITICAL/MAJOR/MINOR/SUGGESTION 카운트)
Agent-facing -> deliverables/agent/WI-20260307-ATS-023-evidence-pack.md :
- 이슈별 상세 근거 포인터, 판단 기준, 수정 제안
Handoff Packet -> deliverables/agent/WI-20260307-ATS-023-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 이슈별 api-spec 섹션# + usecase UC-ID + 파일:라인 포인터 포함
Tests: 해당 없음 (read-only 검증)

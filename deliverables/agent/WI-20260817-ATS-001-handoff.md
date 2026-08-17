[WI HEADER]
WI ID: WI-20260817-ATS-001
REQ: REQ-20260817-ATS-001
Agent: se
Depends On: -
Blocks: WI-20260817-ATS-002

[WI SUMMARY]
Why: 클라이언트 인수 확인용 전용 브랜치에서만 비정사각형 Track 썸네일을 임시 허용한다.
Scope (in/out): 프론트 Track 신규·수정 필드와 백엔드 Track 썸네일 정규화 경로, 관련 테스트와 안내만 변경한다. 개발 워크트리, Album·Playlist, DB, 카드 표시, SR-98은 변경하지 않는다.
DoD: 비정사각형 JPEG/PNG가 Track 신규·수정에서 프론트·백엔드 모두 허용되고 나머지 이미지 방어와 정규화가 유지되며 대상 테스트가 통과한다.
Constraints/Forbidden: 반드시 `C:\Users\jm991\AppData\Local\ATStudio\worktrees\v1-client-acceptance-20260817`에서만 작업한다. `C:\Users\jm991\Desktop\project\ATStudio`는 읽기 전후 상태 비교 외에는 접근·수정하지 않는다. 커밋·프로세스 재시작·공개 데이터 변경은 하지 않는다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Track 신규 등록과 수정에서 선택한 비정사각형 JPEG/PNG가 `valid` 상태가 되고 제출을 막지 않는다.
- [ ] 백엔드 Track 생성·교체가 디코딩된 비정사각형 이미지를 정사각형 오류 없이 정규화한다.
- [ ] JPEG/PNG, 10MB, 디코딩, 최대 변·픽셀, 비율 보존 축소, no-upscale, JPEG 출력 계약을 유지한다.
- [ ] 신규 등록 안내에서 `1:1 필수` 표현을 제거하고 `2048x2048px 권장 (필수 아님)`을 유지한다.
- [ ] 1:1 `cover` 미리보기와 기존 비정사각형 파일 경고는 표시 검토용으로 유지한다.
Performance:
- [ ] 새로운 이미지 디코딩·네트워크 요청·상세 조회를 추가하지 않는다.
Quality:
- [ ] 변경된 프론트 테스트와 백엔드 CanonicalImageService/TrackService 테스트가 통과한다.
- [ ] frontend typecheck, eslint 및 backend compile 또는 대상 build가 통과한다.
- [ ] `git diff --check`가 통과하고 대상 브랜치 외 파일은 변경되지 않는다.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from testing/quality scope):
- docs/policies/quality-gates.md
- docs/standards/evidence-pack-standard.md

Tier 2 (React and feature context):
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md
- docs/SR/SR-98.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-001.md

Files:
- frontend/src/pages/creator/TrackThumbnailField.tsx
- frontend/src/pages/creator/TrackUploadPage.tsx
- frontend/src/pages/creator/TrackThumbnailField.test.tsx
- frontend/src/pages/creator/TrackUploadPage.test.tsx
- frontend/src/pages/creator/TrackEditPage.test.tsx
- src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java
- src/main/java/com/atstudio/atstudio/service/TrackService.java
- src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java
- src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java

Repro/Logs:
- `npm test -- src/pages/creator/TrackThumbnailField.test.tsx src/pages/creator/TrackUploadPage.test.tsx src/pages/creator/TrackEditPage.test.tsx`
- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.image.CanonicalImageServiceTest" --tests "com.atstudio.atstudio.service.TrackServiceTest"`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-001-summary.md : 변경 범위, 임시 편차, 테스트, 위험과 복구 지점
Agent-facing -> deliverables/agent/WI-20260817-ATS-001-evidence-pack.md : 파일·라인, 명령·결과, diff, 롤백
Handoff Packet -> deliverables/agent/WI-20260817-ATS-001-handoff.md : This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: 프론트·백엔드 대상 테스트와 정적 검증 명령·종료 코드를 기록
Rollback: 프론트 정사각형 판정과 백엔드 `SQUARE_TRACK` 정책을 복원하는 정확한 위치 기록

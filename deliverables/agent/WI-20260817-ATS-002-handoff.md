[WI HEADER]
WI ID: WI-20260817-ATS-002
REQ: REQ-20260817-ATS-001
Agent: qa-integ
Depends On: WI-20260817-ATS-001
Blocks: -

[WI SUMMARY]
Why: 임시 Track 썸네일 계약이 프론트·백엔드에 정확히 적용되고 클라이언트 확인 서버만 반영했는지 독립 검증한다.
Scope (in/out): 대상 워크트리 diff·테스트·정적 검증·실행 프로세스 경로·로컬 및 공개 헬스를 확인하고 검증 산출물만 생성한다. 제품 코드를 수정하거나 개발 워크트리·DB·공개 데이터를 변경하지 않는다.
DoD: 요청 범위 일치, 음수 방어 유지, 테스트 통과, 개발 워크트리 비침범, 실행 서버 반영이 증거로 확인되거나 문제 등급과 포인터로 보고된다.
Constraints/Forbidden: 코드·테스트·기존 문서 수정 금지. Cloudflare 터널 종료 금지. DB 행·파일 업로드 금지. 비밀 환경값 읽기·출력 금지. 서버 반영에 추가 컴파일이 필요하면 대상 워크트리의 `classes`만 갱신하며 정확한 프로세스 경로와 헬스를 전후 확인한다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 프론트가 비정사각형 Track 썸네일을 허용하고 `1:1 필수` 안내를 노출하지 않는다.
- [ ] 백엔드 Track 썸네일 정책이 비정사각형을 허용하면서 다른 이미지 검증·정규화를 유지한다.
- [ ] Album·Playlist·미리보기 `cover`·기존 이미지 경고·DB 계약은 변경되지 않았다.
- [ ] 실행 중인 5173/8080 리스너가 클라이언트 워크트리 경로를 사용하고 개발 워크트리 경로를 사용하지 않는다.
- [ ] 로컬 프론트/API와 기존 공개 URL의 프론트/API가 정상 응답한다.
- [ ] 공개 관리자 음원 등록 화면의 안내가 변경된 임시 계약을 반영한다. 가능한 경우 실제 데이터 제출 없이 비정사각형 파일 선택 상태도 확인한다.
Performance:
- [ ] 추가 네트워크 요청·N+1·이미지 이중 디코딩이 없다.
Quality:
- [ ] 프론트 대상 테스트 17개와 백엔드 대상 테스트 48개가 통과한다.
- [ ] TypeScript typecheck, ESLint, Prettier, Java compile 및 `git diff --check`가 통과한다.
- [ ] 개발 워크트리의 작업 상태가 검증 시작 기준과 동일하며 이번 WI 산출물은 대상 워크트리에만 존재한다.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Testing and verification):
- docs/policies/quality-gates.md
- docs/standards/evidence-pack-standard.md

Tier 2 (Cross-layer context):
- docs/standards/frontend-standards.md
- docs/design/api-spec.md
- docs/SR/SR-98.md
- scripts/acceptance/status.ps1
- scripts/acceptance/AcceptanceLifecycle.psm1

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-001.md
- deliverables/agent/WI-20260817-ATS-001-handoff.md
- deliverables/agent/WI-20260817-ATS-001-evidence-pack.md
- deliverables/user/WI-20260817-ATS-001-summary.md

Files:
- frontend/src/pages/creator/TrackThumbnailField.tsx
- frontend/src/pages/creator/TrackEditPage.test.tsx
- frontend/src/pages/creator/TrackThumbnailField.test.tsx
- frontend/src/pages/creator/TrackUploadPage.test.tsx
- src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java
- src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java
- src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java

Repro/Logs:
- `npm test -- src/pages/creator/TrackThumbnailField.test.tsx src/pages/creator/TrackUploadPage.test.tsx src/pages/creator/TrackEditPage.test.tsx`
- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.image.CanonicalImageServiceTest" --tests "com.atstudio.atstudio.service.TrackServiceTest"`
- `npm run typecheck`, `npm run lint`, `npm run format:check`
- `.\gradlew.bat compileJava compileTestJava`
- `git diff --check`
- `Get-NetTCPConnection` and process command-path membership checks for ports 5173/8080
- Local/public HTTP GET health checks without credentials or mutations

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-002-summary.md : PASS/FAIL, finding counts, branch/runtime isolation, live reflection
Agent-facing -> deliverables/agent/WI-20260817-ATS-002-evidence-pack.md : commands, outputs, evidence pointers, risks, rollback recommendation
Handoff Packet -> deliverables/agent/WI-20260817-ATS-002-handoff.md : This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: 명령, 종료 코드, 테스트 수, 정적 검증 결과 포함
Rollback: 두 임시 플래그와 안내·테스트 복구 지점 및 서버 재적용 절차 기록

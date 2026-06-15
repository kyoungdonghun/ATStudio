[WI HEADER]
WI ID: WI-20260603-ATS-001
REQ: REQ-20260603-ATS-001
Agent: se
Depends On: -
Blocks: WI-20260603-ATS-002, WI-20260603-ATS-003

[WI SUMMARY]
Why: 기존 화이트리스트 채널 도메인을 플랜 한도, 상태 모델, 관리자 수동 처리, CSV export가 가능한 백엔드 구조로 확장한다.
Scope (in/out):
- In:
  - `whitelist_channels` schema/entity/dto/repository/service/controller 확장.
  - 상태 모델: `DRAFT`, `PENDING`, `EXPORTED`, `REGISTERED`, `REVISION_REQUESTED`, `REJECTED`, `CANCELLED`, `REMOVAL_REQUESTED`.
  - 대표 채널(`isPrimary`) 정책.
  - 플랜 한도 계산 기준: `PENDING`, `EXPORTED`, `REGISTERED`, `REVISION_REQUESTED`, `REMOVAL_REQUESTED`.
  - 사용자 요청/취소/삭제/해제 요청 API.
  - 관리자 조회/상태 변경/CSV export API.
  - CSV export batch/item 이력 저장.
- Out:
  - YouTube API/OAuth 소유 인증.
  - 외부 등록 대행 사이트 자동 API 연동.
  - 다운그레이드 시 적용 채널 재선택 UX.
DoD:
- 사용자와 관리자가 REQ의 상태 모델을 API로 다룰 수 있다.
- CSV export가 `userEmail`을 포함하고 export 이력을 남긴다.
- 기존 whitelist API 호출 흐름의 하위 호환을 최대한 유지한다.
Constraints/Forbidden:
- `users` 테이블에 대표 채널 단일 필드를 추가하지 않는다.
- 외부 처리 이후(`EXPORTED`, `REGISTERED`) 사용자 즉시 물리 삭제를 허용하지 않는다.
- CSV에 불필요한 개인정보를 추가하지 않는다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 채널 생성 시 `DRAFT`로 저장된다.
- [ ] 채널별 등록 요청 시 플랜 한도 초과를 차단한다.
- [ ] `REVISION_REQUESTED`는 플랜 한도에 포함된다.
- [ ] `DRAFT/PENDING/REVISION_REQUESTED/REJECTED` 삭제 또는 취소가 가능하다.
- [ ] `EXPORTED/REGISTERED` 삭제 시 `REMOVAL_REQUESTED`로 전환된다.
- [ ] 대표 채널은 사용자당 하나만 유지된다.
- [ ] 관리자는 목록 조회, 상태 변경, CSV export를 수행할 수 있다.
Performance:
- [ ] 관리자 목록 API는 paging을 지원한다.
Quality:
- [ ] Backend tests pass.
- [ ] `git diff --check` passes.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/security-policy.md

Tier 2 (Tech Stack / Domain):
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/usecase/whitelist.md
- docs/ui/screen-flow.md

REQ/Context Docs:
- deliverables/user/REQ-20260603-ATS-001.md

Files:
- src/main/java/com/atstudio/atstudio/entity/WhitelistChannel.java
- src/main/java/com/atstudio/atstudio/repository/WhitelistChannelRepository.java
- src/main/java/com/atstudio/atstudio/service/WhitelistChannelService.java
- src/main/java/com/atstudio/atstudio/controller/WhitelistChannelController.java
- src/main/java/com/atstudio/atstudio/dto/whitelist/
- src/main/resources/schema.sql

Repro/Logs:
- `gradlew.bat test`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260603-ATS-001-summary.md :
- Summary, risks, approval points
Agent-facing -> deliverables/agent/WI-20260603-ATS-001-evidence-pack.md :
- Evidence pointers, patch notes, repro & tests, follow-up WI
Handoff Packet -> deliverables/agent/WI-20260603-ATS-001-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Backend tests or focused tests required if implementation touches service/controller behavior
Rollback (if needed): Revert added whitelist status/export schema and API changes

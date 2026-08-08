[WI HEADER]
WI ID: WI-20260808-ATS-002
REQ: REQ-20260808-ATS-001
Agent: qa-integ
Depends On: WI-20260808-ATS-001
Blocks: -

[WI SUMMARY]
Why: SR 두 건이 실제 React/Spring/API 계약 및 외부 근거와 일치하는지 독립적으로 검증한다.
Scope (in/out): WI-001 산출물의 사실성·범위·인덱스 정합성을 검토하고 필요한 경우 해당 SR/인덱스의 최소 수정만 수행한다. 코드·DB 수정은 제외한다.
DoD: 모든 요구가 근거 포인터로 검증되고 문서 검사와 diff 검사가 통과하며, 오류가 있으면 최소 수정 후 Evidence Pack으로 남긴다.
Constraints/Forbidden: 구현을 시작하지 않는다. 태그 정책을 확정된 것으로 바꾸지 않는다. 기존 historical SR을 정규화하거나 사용자 파일을 되돌리지 않는다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] SR-94의 현재/목표 오류 흐름이 코드와 예외 표준에 맞다.
- [ ] SR-95의 현재 입력 제한과 권고/미결정 구분이 코드 및 공식 외부 사례에 맞다.
- [ ] `docs/SR/index.md`의 상태 집계와 실제 파일 수가 일치한다.
- [ ] `docs/index.md` SR 수와 전체 문서 수가 +2 반영되어 있다.
Performance:
- [ ] 해당 없음(문서 전용 WI).
Quality:
- [ ] validate-docs 관련 검사 통과 또는 도구 자체의 기존 실패를 명확히 분리한다.
- [ ] `git diff --check` 통과.
- [ ] 이 WI 범위 외 변경 없음.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/standards/exception-handling.md
- docs/policies/quality-gates.md
- docs/policies/archive-policy.md

Tier 2 (Task Context):
- docs/design/api-spec.md
- docs/design/usecase/sound-tag.md
- docs/SR/index.md
- docs/index.md

REQ/Context Docs:
- deliverables/user/REQ-20260808-ATS-001.md
- deliverables/agent/WI-20260808-ATS-001-evidence-pack.md

Files:
- docs/SR/SR-94.md
- docs/SR/SR-95.md
- frontend/src/pages/admin/TagManagePage.tsx
- frontend/src/api/tags.ts
- frontend/src/utils/validation.ts
- src/main/java/com/atstudio/atstudio/dto/tag/TagCreateRequest.java
- src/main/java/com/atstudio/atstudio/service/TagService.java
- src/main/java/com/atstudio/atstudio/entity/Tag.java
- src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java
- src/main/java/com/atstudio/atstudio/common/exception/GlobalExceptionHandler.java

External official references:
- https://support.splice.com/en/articles/8652594-finding-sounds
- https://splice.com/sounds/genres
- https://www.epidemicsound.com/music/genres/

Repro/Logs:
- `rg -n "Failed to save tag|TAG_NAME_DUPLICATED|TAG_NAME_MAX|existsByName" frontend/src src/main/java docs`
- SR 파일 수와 인덱스 상태 집계 PowerShell 명령
- validate-docs skill command
- `git diff --check`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-002-summary.md :
- 독립 검증 결과와 남은 위험
Agent-facing -> deliverables/agent/WI-20260808-ATS-002-evidence-pack.md :
- Evidence pointers, commands, outputs, 수정 내역, rollback
Handoff Packet -> deliverables/agent/WI-20260808-ATS-002-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: 수행 명령과 결과 필수
Rollback (if needed): 검증 WI가 수정한 문서 hunk만 되돌리는 방법 기록

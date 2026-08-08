[WI HEADER]
WI ID: WI-20260808-ATS-001
REQ: REQ-20260808-ATS-001
Agent: docops
Depends On: -
Blocks: WI-20260808-ATS-002

[WI SUMMARY]
Why: 인수 테스트에서 발견된 관리자 태그 관리 문제 두 건을 구현 전 검토가 가능한 독립 SR로 보존한다.
Scope (in/out): `docs/SR/SR-94.md`, `docs/SR/SR-95.md`, `docs/SR/index.md`, `docs/index.md` 작성·수정만 포함한다. 코드, DB, 활성 설계·정책 문서는 수정하지 않는다.
DoD: 두 SR이 현재 동작/요구/제안/미확정을 구분하고 OPEN으로 인덱싱되며 문서 집계가 94 SR, 전체 196으로 일치한다.
Constraints/Forbidden: 기존 SR을 재작성하지 않는다. SR-91을 재사용하지 않는다. 확인되지 않은 동작을 단정하지 않는다. 사용자·기존 작업 파일을 되돌리거나 추적되지 않은 ZIP을 건드리지 않는다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] SR-94는 현재 `409 TAG_NAME_DUPLICATED` 백엔드 계약과 프론트의 페이지 대체형 `Failed to save tag` 상태를 정확히 구분한다.
- [ ] SR-94는 생성/수정 모두에 대해 사전 중복 안내와 서버 409 인라인 처리, 모달·목록 상태 보존을 요구한다.
- [ ] SR-95는 현재 프론트 trim, 백엔드 비정규화, 50자 제한, DB 전역 unique 상태를 기록한다.
- [ ] SR-95는 앞뒤 공백 제거, 내부 공백 허용, 연속 공백 정규화, 제한적 문장부호 허용 권고와 미결정 항목을 구분한다.
- [ ] Splice와 Epidemic Sound의 공식 장르 표기 사례를 링크한다.
- [ ] `docs/SR/index.md`와 `docs/index.md`의 번호·상태·개수가 동기화된다.
Performance:
- [ ] 해당 없음(문서 전용 WI).
Quality:
- [ ] Markdown 표와 링크가 유효하다.
- [ ] SR historical record의 기존 한국어 작성 관례와 구조를 따른다.
- [ ] `git diff --check`에 걸리는 공백 오류가 없다.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/archive-policy.md
- docs/standards/exception-handling.md

Tier 2 (Task Context):
- docs/SR/index.md
- docs/SR/SR-70.md
- docs/SR/SR-78.md
- docs/SR/SR-84.md
- docs/SR/SR-90.md
- docs/design/usecase/sound-tag.md
- docs/index.md

REQ/Context Docs:
- deliverables/user/REQ-20260808-ATS-001.md

Files:
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
- `Get-ChildItem docs/SR -Filter 'SR-*.md'`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-001-summary.md :
- 생성 문서, 핵심 판단, 남은 정책 결정 사항
Agent-facing -> deliverables/agent/WI-20260808-ATS-001-evidence-pack.md :
- Evidence pointers, patch notes, 조사 근거, 검증 결과, rollback
Handoff Packet -> deliverables/agent/WI-20260808-ATS-001-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: 문서 파일 수·상태 집계와 `git diff --check` 결과 포함
Rollback (if needed): 이 WI가 추가한 두 SR과 인덱스 변경만 되돌리는 방법 기록

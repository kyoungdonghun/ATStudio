[WI HEADER]
WI ID: WI-20260603-ATS-003
REQ: REQ-20260603-ATS-001
Agent: docops
Depends On: WI-20260603-ATS-001, WI-20260603-ATS-002
Blocks: -

[WI SUMMARY]
Why: 구현된 화이트리스트 정책과 API/UI/DB 변경사항을 문서 SoT에 반영한다.
Scope (in/out):
- In:
  - API spec whitelist section update.
  - DB schema whitelist tables/status/export tables update.
  - Use case whitelist update.
  - UI screen-flow/client scenario update.
  - Glossary update if new terms are needed.
- Out:
  - 클라이언트용 별도 상세 운영 매뉴얼 신규 작성. 필요한 경우 후속 SR/guide로 분리.
DoD:
- 문서와 코드가 동일한 필드명, 상태 모델, 한도 기준, CSV export 정책을 설명한다.
Constraints/Forbidden:
- 구현되지 않은 YouTube 자동 인증/자동 등록을 현재 기능처럼 쓰지 않는다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] API spec includes user/admin whitelist endpoints and CSV export.
- [ ] DB schema includes expanded whitelist fields and export ledger tables.
- [ ] Use case documents include deletion/removal policy.
Performance:
- [ ] N/A
Quality:
- [ ] validate-docs passes.
- [ ] `git diff --check` passes.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/security-policy.md

Tier 2 (Domain):
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/usecase/whitelist.md
- docs/ui/screen-flow.md
- docs/client/1-scenarios.md

REQ/Context Docs:
- deliverables/user/REQ-20260603-ATS-001.md

Files:
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/usecase/whitelist.md
- docs/ui/screen-flow.md
- docs/client/1-scenarios.md
- docs/standards/glossary.md

Repro/Logs:
- `python .agents/skills/validate-docs/scripts/validate_docs.py`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260603-ATS-003-summary.md :
- Summary, risks, approval points
Agent-facing -> deliverables/agent/WI-20260603-ATS-003-evidence-pack.md :
- Evidence pointers, patch notes, repro & tests, follow-up WI
Handoff Packet -> deliverables/agent/WI-20260603-ATS-003-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: docs validation required
Rollback (if needed): Revert doc updates associated with REQ-20260603-ATS-001

[WI HEADER]
WI ID: WI-20260714-ATS-001
REQ: REQ-20260714-ATS-001
Agent: qa-integ
Depends On: -
Blocks: WI-20260714-ATS-004 through WI-20260714-ATS-034

[WI SUMMARY]
Why: Establish a verified, code-grounded traceability baseline for all 13 P1 findings before remediation changes begin.
Scope (in/out):
- In: Re-verify ATS020-P1-01 through ATS020-P1-13 against current branch code, tests, schema/manual patches, designs, operations docs, and client docs.
- In: Produce an audit-ID-to-contract-to-code-to-test-to-document matrix with explicit closure evidence requirements and affected owners.
- In: Identify conflicts, duplicate scope, hidden dependencies, and any finding whose stated cause or exit condition is no longer accurate.
- Out: Production code, test, schema, configuration, or existing current-state documentation edits.
- Out: Closing findings based on inference without reproducible evidence.
DoD:
- Every P1 row has confirmed current evidence, target behavior, required implementation WI, test proof, documentation proof, and final reviewer.
- Acceptance-related ATS020-X-01, X-02, and X-04 are mapped separately and do not silently expand the P1 scope.
- Any changed interpretation is called out as a decision or approval point rather than silently rewritten.
Constraints/Forbidden:
- Read-only inspection except for the three WI deliverables listed in the output contract.
- Do not edit application code, schemas, tests, existing docs, or runtime logs.
- Do not inspect or reproduce secret values, raw tokens, billing keys, card data, or certification document contents.
- Do not include P2/P3 remediation except as a clearly separated follow-up reference.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] ATS020-P1-01 through ATS020-P1-13 each have one complete trace row.
- [ ] Each trace row identifies current vulnerable behavior, exact source pointers, desired invariant, implementing WI, verification command/test, and documentation target.
- [ ] ATS020-X-01, X-02, and X-04 have bounded acceptance-environment rows.
- [ ] Conflicts between audit statements, current code, and current docs are explicitly adjudicated.
Performance:
- [ ] No runtime performance target applies; inspection remains bounded to referenced domains.
Quality:
- [ ] Evidence uses paths, symbols, tests, commands, and line pointers where stable.
- [ ] No claim is marked verified without reproducible repository evidence.
- [ ] `git diff --check` passes for the WI deliverables.

[INPUT POINTERS]
Tier 0 (Constitution - Required):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
- docs/standards/evidence-pack-standard.md

Tier 2 (Technology and current-state contracts):
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
- docs/design/api-spec.md
- docs/design/payment-integration-design.md
- docs/design/payment-refund-receipt-settlement-policy.md
- docs/design/payment-operations-runbook.md
- docs/design/payment-settlement-import-design.md
- docs/design/usecase/user-subscription.md
- docs/SR/SR-42.md
- docs/payment/
- docs/client/

REQ/Context Docs:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/audit/full-system-audit-20260713.md
- docs/audit/p0-release-blocker-closure-20260713.md

Files:
- src/main/java/com/atstudio/atstudio/
- src/main/resources/application.yml
- src/main/resources/schema.sql
- src/main/resources/db/manual/
- src/test/java/com/atstudio/atstudio/
- frontend/src/
- frontend/vite.config.ts

Repro/Logs:
- `git status --short --branch`
- `rg -n "ATS020-P1-|ATS020-X-0[124]" docs/audit/full-system-audit-20260713.md`
- `git diff --check`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-001-summary.md:
- Korean summary of confirmed scope, corrections, risks, and approval points.
Agent-facing -> deliverables/agent/WI-20260714-ATS-001-evidence-pack.md:
- Complete trace matrix, evidence pointers, conflict adjudication, repro commands, and next-WI triggers.
Handoff Packet -> deliverables/agent/WI-20260714-ATS-001-handoff.md:
- This packet.
Additional artifact -> docs/audit/p1-remediation-trace-matrix-20260714.md:
- English current-state traceability matrix; no finding closure claims yet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Identify required existing and new test targets; do not run destructive or live-service tests.
Rollback: Revert only the three WI-owned deliverables.

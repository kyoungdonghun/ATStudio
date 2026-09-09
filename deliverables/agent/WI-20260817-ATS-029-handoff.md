
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A057: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a057). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

[WI HEADER]
WI ID: WI-20260817-ATS-029
REQ: REQ-20260817-ATS-010
Agent: qa-integ
Depends On: WI-20260817-ATS-023
Blocks: operational external-effect rehearsal and final independent review

[WI SUMMARY]
Why: Verify that changes made after the client acceptance baseline did not regress V1's highest-risk user and runtime paths before any provider or SMTP rehearsal begins.
Scope (in/out):
- In: read-only code/configuration inspection; development-runtime smoke and targeted API/UI checks for authentication/session, routing, mutable catalogue flows, playback duration/waveform, tag search, and Hibernate validate boot against the current development database.
- Out: source/configuration edits; public client runtime/DB changes; production/stage access; payment/refund requests; external SMTP sends; DB schema/data mutation; secret disclosure.
DoD:
- Produce a risk-to-evidence matrix that separates UI observation, API response, and persisted/runtime state.
- Identify only confirmed defects as defects; label unexecuted or unavailable paths as unresolved.
- Confirm whether the current development database can boot with Hibernate ddl-auto=validate, without changing it.
- Produce exact, bounded follow-up recommendations for any blocker.
Constraints/Forbidden:
- Work only from $USERPROFILE\Desktop\project\ATStudio on branch codex/v1-release-rehearsal-fixes.
- Do not switch, merge, reset, delete, or commit branches/files.
- Do not alter the client snapshot runtime or its isolated database.
- Do not print secrets, credentials, JDBC URLs, raw provider payloads, or PII.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Authentication/session and return-route behavior is checked through existing tests and, where a local non-side-effect runtime is available, one representative UI/API flow.
- [ ] Track/album/playlist mutation cancellation/retry state, player duration/waveform, and tag keyword/category behavior are traced to concrete current contracts.
- [ ] Development Hibernate validate boot result is captured without schema/data changes.
- [ ] Every result states PASS, FAIL, BLOCKED, or NOT EXECUTED with supporting command/file/API evidence.
Quality:
- [ ] No workspace files changed by this WI except its two deliverables.
- [ ] `git diff --check` is run.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-010.md
- deliverables/user/REQ-20260817-ATS-009.md
- deliverables/user/WI-20260817-ATS-023-summary.md
- deliverables/agent/WI-20260817-ATS-023-evidence-pack.md
- scripts/acceptance/README.md

Files:
- frontend/src/ (routing, auth/session, player, catalogue and tag paths)
- src/main/java/com/atstudio/atstudio/ (corresponding API/service paths)
- src/main/resources/application.yml
- src/main/resources/schema.sql

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-029-summary.md:
- Korean summary of verified paths, confirmed defects, unresolved paths, and whether the next external-effect stage is safe to enter.
Agent-facing -> deliverables/agent/WI-20260817-ATS-029-evidence-pack.md:
- Risk/evidence matrix, exact commands, results, runtime boundaries, test results, and follow-up recommendations.
Handoff Packet -> deliverables/agent/WI-20260817-ATS-029-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Include the exact commands and outcomes; distinguish unavailable/external tests from failures.
Rollback: None; read-only verification. If any incidental generated artifact appears, report it and do not remove it.

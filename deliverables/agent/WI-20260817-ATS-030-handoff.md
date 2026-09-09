
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A059: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a059). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

[WI HEADER]
WI ID: WI-20260817-ATS-030
REQ: REQ-20260817-ATS-010
Agent: qa-integ
Depends On: WI-20260817-ATS-029
Blocks: external-effect rehearsal, backup/restore rehearsal, independent release evidence review

[WI SUMMARY]
Why: Obtain real application-startup and live UI/API evidence on an isolated operational-rehearsal runtime without changing the development source database or the client-acceptance environment.
Scope (in/out):
- In: preflight the two existing local boundaries; create one new guarded local operational-rehearsal database as a clone of the development database; use a new isolated storage root and unused localhost ports; boot the current development branch with Hibernate ddl-auto=validate; permit startup storage recovery and the scheduler only inside this clone; execute safe representative UI/API/runtime checks; capture secret-safe evidence.
- Out: source or tracked configuration changes; client snapshot/runtime/database requests or changes; development source database schema/data/storage mutation; stage/production/remote access; Cloudflare tunnel; SMTP delivery; Toss/provider API request; payment/refund/renewal/settlement mutation; branch/worktree deletion or switching.
DoD:
- A new, uniquely named local rehearsal DB and storage root are proven isolated before the application starts.
- Current branch boots with Hibernate ddl-auto=validate against the rehearsal DB.
- Startup storage recovery and scheduler behavior are observed only in the rehearsal boundary, with external provider/email operations fail-closed or disabled for this stage.
- Representative browser/API paths establish live, not only mocked, evidence for the risks in WI-029.
- Runtime is stopped cleanly when evidence collection completes; original development and client boundaries are unchanged.
Constraints/Forbidden:
- Work only from $USERPROFILE\Desktop\project\ATStudio on codex/v1-release-rehearsal-fixes.
- The user explicitly authorized creation and mutation of the new local rehearsal clone only. Never mutate the source development DB, client acceptance DB, or any non-loopback target.
- Before any database command, prove loopback target and a fresh guarded rehearsal-only name; no ambiguous target may be used.
- Keep credentials, JDBC URLs, raw provider payloads, and PII out of terminal output and deliverables.
- Do not invoke external SMTP or Toss endpoints. If scheduler/recovery configuration cannot be made safe without source/config edits, stop and report the blocker rather than bypassing the boundary.
- Do not stop existing ports 5173/8080. Use only unoccupied rehearsal ports.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Preflight records separation from the active client runtime and from the development source DB without exposing identities/secrets.
- [ ] One local rehearsal clone is created and schema/table count is compatible with the current 43-table baseline before boot.
- [ ] Spring starts with ddl-auto=validate and uses only rehearsal DB and storage values.
- [ ] Startup recovery and scheduler are observed or explicitly BLOCKED with an exact reason; no provider or SMTP invocation occurs.
- [ ] At least one representative live browser/API path covers authentication/session, catalogue/tag query, track playback metadata/waveform, and one protected mutation cancellation/retry contract without changing source boundaries.
- [ ] Owned rehearsal processes are stopped cleanly; listener/process evidence confirms no client process was touched.
Quality:
- [ ] No source/tracked configuration changes.
- [ ] `git diff --check` passes.
- [ ] User summary and evidence pack record UI, API, provider, and persistence/runtime evidence separately.

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
- deliverables/user/WI-20260817-ATS-029-summary.md
- deliverables/agent/WI-20260817-ATS-029-evidence-pack.md
- deliverables/user/WI-20260817-ATS-023-summary.md
- deliverables/agent/WI-20260817-ATS-023-evidence-pack.md
- scripts/acceptance/README.md
- scripts/acceptance/AcceptanceLifecycle.psm1

Files:
- src/main/resources/application.yml
- src/main/resources/application-acceptance.yml
- src/main/java/com/atstudio/atstudio/service/storage/StorageMutationRecoveryService.java
- src/main/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunner.java
- src/main/java/com/atstudio/atstudio/service/payment/ (scheduler/provider boundary)
- frontend/src/router/
- frontend/src/layouts/PlayerBar.tsx
- frontend/src/components/player/

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-030-summary.md:
- Korean concise result, exact boundary outcome, verified live paths, blockers, and whether the next external-effect stage can start.
Agent-facing -> deliverables/agent/WI-20260817-ATS-030-evidence-pack.md:
- Safe reproducibility steps, non-secret command names/results, runtime isolation proof, UI/API/persistence/runtime distinction, cleanup proof, and follow-up boundaries.
Handoff Packet -> deliverables/agent/WI-20260817-ATS-030-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Record commands/results, listener ownership checks, and all non-executed external actions.
Rollback: Stop only owned rehearsal processes. Leave rehearsal DB/storage untouched after the run; no cleanup or deletion is authorized in this WI.

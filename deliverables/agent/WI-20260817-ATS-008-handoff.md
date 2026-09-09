---
version: 1.0
last_updated: 2026-08-17
project: ATS
owner: qa-integ
category: wi-handoff
status: approved-execution
related_req: REQ-20260816-ATS-001
---

> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A026: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a026). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

[WI HEADER]
WI ID: WI-20260817-ATS-008
REQ: REQ-20260816-ATS-001
Agent: qa-integ
Depends On: WI-20260816-ATS-002 (historical blocked capture), WI-20260817-ATS-007 (documented predecessor readiness)
Blocks: -

[WI SUMMARY]
Why: Make one newly authorized, bounded, isolated observational proof attempt that remedies only the prior missing actual-target preflight capture. The attempt may observe the current disposable manifest but must not record a manifest expectation or advance the original REQ to runtime, browser, or product work.

Scope (in/out):
- In: One execution process generates exactly one fresh target matching `ats_disposable_YYYYMMDD_<eight-lowercase-alphanumeric>` and a separate high-entropy secret used only in memory to derive one sanitized, non-reversible correlation token. The process invokes wrapper `Preflight` once, parses every expected safe field, and invokes wrapper `Observe` exactly once only after complete capture validates. It uses the documented repo-external bundle only as an opaque wrapper input.
- In: Retain only the correlation token and `targetCorrelation=PASS`, source `CREATE TABLE` count/check, current manifest numeric fields and SHA-256 when emitted, the controlled `MYSQL_MANIFEST_EXPECTATION_UNRECORDED` refusal, and `cleanupAfterFailure`/target-removal result. Documentation validation is the only post-observation verification.
- Out: Any `Create`, `Validate`, or explicit `Drop` invocation; a second preflight, observation, target generation, retry, or remediation. Existing/local `atstudio` databases, all non-generated databases, external services, application or browser use, mail, payment, refund, OAuth, Git actions, commits, pushes, and source/product/config/manifest-expectation changes are excluded.

DoD:
- The next valid WI identifier is used without changing historical records.
- The one generated target is never printed, persisted, or placed in a deliverable. Its in-memory only derivation produces one non-reversible token, and equality checks prove that the same target variable was supplied to both wrapper invocations.
- `Preflight` capture is accepted only when every expected safe field is present, unique, syntactically valid, and has the required value: action, disposable-name guard, loopback host class, SQL order, source count/check, manifest expectation, normalized source-input hash formats, and PASS status. Missing, duplicate, malformed, or unexpected output stops before opaque credential handling and no `Observe` occurs.
- If preflight capture is complete, one `Observe` receives the same in-memory target and opaque bundle. The expected `UNRECORDED` refusal after schema, seed, and manifest observation is recorded as fail-closed behavior. The source, scripts, and active expectation remain unchanged.
- The observation capture is retained only after strict safe-field parsing confirms bounded manifest output, the controlled refusal reason, and cleanup-after-failure target removal. Any unclear guard, correlation, redaction, or cleanup condition ends the WI with no further database action or retry.
- User-facing and agent-facing records state whether the observation is sufficient to propose a future manifest expectation. They do not record or change such an expectation and do not create a follow-up WI.

Constraints/Forbidden:
- Use the documented repo-external acceptance environment bundle only through the wrapper argument. Do not open, print, enumerate, inspect, or record its location, content, values, or derived connection details; do not use an in-repository or ignored environment file.
- Never retain or emit the exact database name, host/port, connection string, credentials, environment values, raw command output, raw database rows, or PII. Suppress non-safe wrapper diagnostics; parse safe fields in memory only.
- A preflight capture failure is terminal before credentials, connector discovery, or database connection. An observation capture/cleanup ambiguity is terminal after its single permitted invocation. No regeneration, retry, cleanup command, or bypass is allowed.
- Treat `MYSQL_MANIFEST_EXPECTATION_UNRECORDED` as an expected safety refusal. It is not a source defect and must not prompt a code, script, schema, seed, or expectation update.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Exactly one fresh guarded target and one non-reversible correlation token are generated inside the execution process; no target identifier is retained.
- [ ] One wrapper `Preflight` has a complete, strict, safe-field capture before any opaque-input, credential, connector, or database work.
- [ ] A complete preflight is followed by exactly one wrapper `Observe` using the same in-memory target; an incomplete capture is followed by no observation.
- [ ] Only the permitted sanitized outcome fields are retained, including the expected manifest refusal and target-removal result when observation reaches them.
- [ ] `Create`, `Validate`, standalone `Drop`, app/browser, external, and existing-database actions are all not invoked.
Performance:
- [ ] Not applicable: one bounded observation attempt with no response-time or throughput target.
Quality:
- [ ] G1, G3, G4, G5, and G6 of `REQ-20260816-ATS-001` are either evidenced through sanitized results or the first unmet condition is reported fail-closed.
- [ ] Historical WI records remain unchanged, and deliverable links/metadata validate.
- [ ] The result distinguishes evidence sufficient to propose a future recorded expectation from any unauthorized expectation or source change.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Required by role and task):
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
- docs/standards/evidence-pack-standard.md

Tier 1 (Policies - Inferred from DB verification, secrets, quality, and access):
- docs/policies/quality-gates.md
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/architecture/system-design.md

Tier 2 (Workflow / technical context):
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md
- docs/templates/eval-report-template.md
- docs/templates/requirements-request-template.md
- docs/templates/wi-subagent-handoff-template.md

REQ/Context Docs:
- deliverables/user/REQ-20260816-ATS-001.md
- deliverables/agent/WI-20260816-ATS-002-handoff.md
- deliverables/user/WI-20260816-ATS-002-summary.md
- deliverables/agent/WI-20260816-ATS-002-evidence-pack.md
- deliverables/user/WI-20260817-ATS-007-summary.md
- deliverables/agent/WI-20260817-ATS-007-evidence-pack.md
- scripts/database/README.md

Files:
- scripts/database/bootstrap-disposable-mysql.ps1
- scripts/database/DisposableMysqlBootstrap.java
- scripts/database/test-bootstrap-guards.ps1

Repro/Logs:
- One in-memory PowerShell execution process only: generate target and secret correlation material, wrapper `Preflight`, then conditionally one wrapper `Observe`; emit no raw output and persist only the sanitized result fields specified in this packet.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-008-summary.md :
- State the single-attempt result, correlation proof, sanitized preflight/observation fields, cleanup result or exact uncertainty, whether a future expectation could be proposed, and residual scope boundary.
- Exclude target names, opaque-input location/content, host/port, connection strings, credentials, environment values, raw output, rows, and PII.
Agent-facing -> deliverables/agent/WI-20260817-ATS-008-evidence-pack.md :
- Provide pointers, sanitized action counts/results, gate mapping, documentation-validation result, no-change confirmation, and residual risk/rollback position.
Handoff Packet -> deliverables/agent/WI-20260817-ATS-008-handoff.md :
- This packet, created before the sole DB observation attempt.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/commands): Required; command evidence must be described only as the sanitized one-process action sequence.
Tests: Do not run a focused test. Run documentation validation after records are written.
Rollback (if needed): No product, source, configuration, or manifest-expectation change is permitted. The guarded observation path owns cleanup of only a target it created; no standalone cleanup command is authorized.

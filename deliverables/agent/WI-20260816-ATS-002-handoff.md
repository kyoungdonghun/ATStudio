
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A012: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a012). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

[WI HEADER]
WI ID: WI-20260816-ATS-002
REQ: REQ-20260816-ATS-001
Agent: qa-integ
Depends On: WI-20260816-ATS-001 (successful sanitized evidence and dependency gate)
Blocks: -

[WI SUMMARY]
Why: Independently prove the current 43-table schema on one fresh, generated, loopback-only disposable MySQL target under the existing guarded bootstrap, leaving the current existing database untouched and retaining only a sanitized manifest/evidence record.

Scope (in/out):
- In: Read the completed sanitized evidence from WI-20260816-ATS-001 before beginning; generate one fresh target name matching the bootstrap guard; run non-database guards and preflight; use the guarded bootstrap to observe when the active current manifest is unrecorded; record the bounded current manifest; and continue with guarded create, independent validate, an explicitly specified focused test, and exact-target drop only when the unchanged current guard authorizes each next action.
- In: Use the repository-external acceptance environment bundle only as opaque credential transport for the guarded bootstrap. Use only current `src/main/resources/schema.sql` followed by `src/main/resources/seed.sql` as bootstrap inputs.
- Out: The existing local acceptance/development database, `atstudio`, protected/system schemas, stage, production, remote hosts, any existing DB, and any target other than the single generated disposable target.
- Out: `scripts/database/patches/WI-20260809-ATS-068-user-consents.sql`. Do not run it against the disposable database; any current `user_consents` definition must come solely from current `schema.sql`.
- Out: Product/source/manifest-tool changes, manual DDL/DML, acceptance/app startup, browser actions, external providers, mail, payment, refund, OAuth, Git stage/commit/push, and environment/secret/ignored-config inspection or output.

DoD:
- WI-001 has successful sanitized evidence. If it is missing, blocked, failed, or ambiguous, do not generate a database name, load credentials, connect, create, validate, test, or drop; report the dependency block.
- Exactly one fresh target name is generated in memory and accepted by the `ats_disposable_YYYYMMDD_<eight-lowercase-alphanumeric>` guard. The name is never emitted to evidence, logs, or summaries; use a sanitized run token/one-way identity reference where correlation is necessary.
- The guard suite and `Preflight` establish the current 43-table source check, loopback classification, target-name guard, current SQL order, and manifest state before credentials or any connection are used.
- When the active guard reports `UNRECORDED`, use only the separately approved guarded `Observe` path to obtain its bounded current manifest. Record only the allowed manifest fields and sanitized result codes. Do not bypass, alter, or treat the observed evidence as a source/product manifest update.
- Because the current bootstrap fails closed while its active manifest expectation remains `UNRECORDED`, do not invoke guarded `Create`, `Validate`, a focused test, or explicit `Drop` unless the unchanged guard itself first accepts a current recorded-manifest state through an already authorized mechanism. If that condition is not met, record a guarded block and do not weaken or modify the tool.
- If an authorized successful create/validate path becomes available, validate only the generated exact target; run a focused test only when a test class/command is explicitly specified by the REQ or an already-approved input. No focused test is currently specified by this REQ, so do not infer or run one.
- Run explicit `Drop` only after successful create/validate, only against the same generated target and reconfirmed loopback endpoint, and only when identity correlation matches the creation record. If identity is uncertain or validation fails, do not drop; report the residual exact-target risk. The guarded observation path's own exact-target cleanup result is recorded when applicable.

Constraints/Forbidden:
- Maintain full separation from current existing databases. The disposable target is the only permitted connection/create/validate/drop target; never enumerate, inspect, mutate, recreate, or delete an existing database.
- The bootstrap's currently active 43-table/`UNRECORDED` fail-closed behavior is a safety control, not a defect to work around. Do not edit `schema.sql`, `seed.sql`, bootstrap scripts, Java helper, or any product file to make the proof proceed.
- Keep all generated target names, bundle paths/contents, connection strings, host details, ports, usernames, passwords, tokens, environment values, raw query rows, and PII out of output and deliverables. Retain only approved sanitized manifest fields, result codes, action states, and a non-reversible correlation reference.
- Do not start acceptance, the application, a browser, a tunnel, or provider/mail/payment/refund/OAuth traffic.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] WI-001 successful sanitized evidence is checked before this WI; a missing/blocked/ambiguous predecessor produces no database action.
- [ ] One fresh generated target passes guarded non-database preflight: allowed disposable-name pattern, loopback scope, source `CREATE TABLE` count `43`, fixed `schema.sql->seed.sql` order, and sanitized target redaction.
- [ ] The current manifest state is recorded through the guarded observation path when required, using only bounded fields: tables, columns, indexes, foreign keys, plans, plan keys, forbidden tables, forbidden columns, and manifest digest; no sensitive or exact target values are retained.
- [ ] No WI-068 patch is run in the disposable database. Bootstrap input is current `schema.sql` followed by current `seed.sql` only.
- [ ] Guarded create and independent validate run only when the unchanged guard authorizes the recorded-manifest path; otherwise they are explicitly recorded as fail-closed and not attempted.
- [ ] No focused test runs unless a specific test command is supplied by an already-approved input; none is inferred from historical evidence.
- [ ] When a successful create/validate path exists, exact-target drop occurs only after rechecking the generated target correlation and loopback endpoint. An identity/validation mismatch prevents drop and is reported.
- [ ] The current existing database remains untouched; no acceptance/app/browser/external/provider/mail/payment/refund/OAuth action occurs.
Performance:
- [ ] Not applicable: this is a single bounded disposable proof. It uses one generated target and no response-time or memory target.
Quality:
- [ ] G1, G3, G4, G5, and G6 from `REQ-20260816-ATS-001` are evidenced, or the first unmet guard is reported with no unsafe follow-on action.
- [ ] `scripts/database/test-bootstrap-guards.ps1` result is recorded in sanitized form before MySQL actions.
- [ ] Evidence is reproducible from sanitized action/result-code sequence, source file pointers, guard result, and non-reversible correlation reference without secrets, exact target names, or raw data.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Required by REQ strategy and qa-integ assignment):
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
- docs/standards/evidence-pack-standard.md

Tier 1 (Policies and architecture - Inferred from database verification, secret boundary, and WI workflow):
- docs/policies/quality-gates.md
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/architecture/system-design.md

Tier 2 (Tech stack and workflow - Required by workspace/context-injection rules):
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md
- docs/templates/eval-report-template.md
- docs/templates/requirements-request-template.md
- docs/templates/wi-subagent-handoff-template.md

REQ/Context Docs:
- deliverables/user/REQ-20260816-ATS-001.md
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/user/WI-20260816-ATS-001-summary.md
- deliverables/agent/WI-20260816-ATS-001-evidence-pack.md
- scripts/acceptance/README.md
- scripts/database/README.md
- scripts/database/bootstrap-disposable-mysql.ps1
- scripts/database/test-bootstrap-guards.ps1
- deliverables/user/WI-20260809-ATS-068-summary.md
- deliverables/agent/WI-20260809-ATS-068-decision-register.md
- deliverables/agent/WI-20260809-ATS-068-evidence-pack.md

Files:
- src/main/resources/schema.sql
- src/main/resources/seed.sql
- scripts/database/patches/WI-20260809-ATS-068-user-consents.sql (for explicit non-use verification only)

Repro/Logs:
- `scripts/database/test-bootstrap-guards.ps1` and guarded bootstrap action sequence: preserve only sanitized action/result-code output, bounded manifest fields, and non-reversible run correlation. Do not preserve command-line secrets, bundle path/content, exact generated database name, connection details, or raw MySQL output.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260816-ATS-002-summary.md :
- State the dependency result, current 43-table preflight/manifest result, whether the unchanged guard authorized each action, exact-target cleanup outcome or blocked cleanup reason, residual risk, and the separately scoped future runtime/browser boundary.
- Do not include target names, bundle paths/contents, connection details, credentials, environment values, raw rows, PII, or external-service output.
Agent-facing -> deliverables/agent/WI-20260816-ATS-002-evidence-pack.md :
- Provide sanitized pointers for predecessor gate, generated-target guard, guard-suite result, preflight, observation/current-manifest record, create/validate/test authorization or fail-closed result, exact-target cleanup result, non-execution of prohibited actions, reproducibility, and follow-up status.
- Show that only current `schema.sql` then `seed.sql` were eligible inputs and the WI-068 patch was not run against the disposable target.
Handoff Packet -> deliverables/agent/WI-20260816-ATS-002-handoff.md :
- This packet (for traceability).

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required. Use repository pointers, sanitized result codes, bounded manifest values, and a non-reversible run correlation only.
Tests: Run `scripts/database/test-bootstrap-guards.ps1`. A focused integration test is forbidden unless a specific already-approved test command is supplied; no such test is specified in this REQ.
Rollback (if needed): No rollback or manual cleanup of an existing database is authorized. The guarded observation path may perform its own exact-target cleanup after its expected fail-closed result. Explicit `Drop` is allowed only after successful authorized create/validate with matching target correlation and loopback confirmation; otherwise stop and report the residual state.

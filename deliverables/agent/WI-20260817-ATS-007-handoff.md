
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A024: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a024). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

[WI HEADER]
WI ID: WI-20260817-ATS-007
REQ: REQ-20260817-ATS-005
Agent: qa-integ
Depends On: Approved REQ-20260817-ATS-005; WI-20260817-ATS-002 compiler readiness = PASS; the existing approved patch remains unapplied; the first P5 aggregate validation must be true before any later stage.
Blocks: -

[WI SUMMARY]
Why: Execute the one final, fail-closed recovery authorized by REQ-20260817-ATS-005. The historical P5 FAIL is a diagnostic false negative, not a new blocker or a reason to repeat a prior WI.

Scope (in/out):
- In: Use the same one pre-existing approved private bundle, with its complete lifecycle strictly in memory. The first operational predicate is the corrected P5 aggregate calculation: parse with `ConvertFrom-Json -AsHashtable`, use `$map.Contains(...)` for every required contract field, and aggregate only existence, string-type, and nonblank results.
- In: Retain only the P5 aggregate success codes `P5_EXISTS_TRUE`, `P5_TYPE_TRUE`, and `P5_NONBLANK_TRUE`. Do not emit field-level outcomes, names, values, paths, identities, target details, credentials, or diagnostics. A P5 failure ends with only its fixed secret-safe stage code.
- In: Keep predicate stages distinct with narrow, stage-specific error boundaries. A generic `catch` must not combine, replace, or fabricate predicate results. Do not reference, assign, or shadow `$Host` or `$host`.
- In: Only after all three P5 aggregate booleans are true, audit the unchanged approved patch source exactly once, compile one fresh external volatile Java runner exactly once, and continue with the same private bundle held in memory.
- In: With exactly one JDBC connection, perform exactly this sequence once: one expected-object absence precheck, one application of the exact unchanged approved patch, and one post-execution structure set-delta verification. Do not perform a later stage unless every preceding stage has succeeded.
- In: Clean all external temporary runner artifacts on the terminal path, then create the required sanitized Summary and Evidence Pack and run the authorized documentation validation and scoped diff check.
- Out: Product, configuration, schema-source, or patch changes; bundle creation, replacement, search, copy, persistence, or disclosure; additional inputs; application/runtime/browser/test/build actions; non-approved network or database activity; SQL, DDL, or data activity beyond the exact one patch application and its approved bounded structural checks; any follow-up WI or parallel operation.

DoD:
- P5 is first, uses `ConvertFrom-Json -AsHashtable` and `$map.Contains(...)`, and the retained P5 output contains only the three aggregate true codes when it passes.
- No `$Host` or `$host` reference/assignment occurs, and no generic catch collapses separate predicate stages into a shared result.
- Only after P5 passes, the unchanged patch source audit count is `1`, fresh external Java-runner compilation count is `1`, JDBC connection count is `1`, absence-precheck count is `1`, patch-application count is at most `1`, and post-execution set-delta verification count is `1`.
- Success requires the expected object to be present after application, additions `1`, removals `0`, and unexpected additions `0`; every other terminal result fails closed.
- Historic false-negative or interruption records remain unmodified and are not blockers for this WI; they do not authorize a retry or another attempt.
- All temporary runner artifacts are removed. Only the required sanitized Summary and Evidence Pack are created, and `validate_docs.py` plus the scoped `git diff --check` pass.

Constraints/Forbidden:
- This packet authorizes one final qa-integ operation only. Do not delegate, parallelize, resume, repair, retry, rerun, compensate, roll back, delete data, issue `DROP`, add a connection, add a query, add SQL/DDL, or widen the target after any terminal outcome.
- Do not use an alternate bundle or reopen private input through another path. Do not print, log, retain, serialize, cite, attach, or otherwise disclose the bundle, contract-field names, derived datasource values, credentials, target identity, raw compiler/DB output, source text, command text, exception details, temporary paths, or logs.
- P5 stage results must remain distinct. Do not use a generic `catch`, common failure wrapper, or shared terminal state to merge stage outcomes. A failed prerequisite stops subsequent stages without inventing their result.
- Do not begin source audit, Java compilation, JDBC work, SQL, DDL, or data access before P5 records all three aggregate true codes. Do not open the connection unless the source audit and one fresh compilation also passed.
- Use only the exact unchanged existing patch found at `scripts/database/patches/WI-20260809-ATS-068-user-consents.sql`. Its object identity and SQL text are execution inputs only and must not appear in the Summary or Evidence Pack.
- After the one connection opens, perform no action outside the one absence precheck, one exact patch application, and one post-execution structure delta. The precheck and delta are limited to the patch's expected object set; do not inspect unrelated schema objects or data.
- Do not modify historical REQs, WIs, summaries, evidence, product files, configuration, schema source, patch source, policies, or design documents. Do not run product tests, builds, the application, a browser, or an external action. Git use is limited to the scoped documentation-only diff check below.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] P5 is the first predicate and uses `ConvertFrom-Json -AsHashtable` with `$map.Contains(...)`; its retained successful output is only `P5_EXISTS_TRUE`, `P5_TYPE_TRUE`, and `P5_NONBLANK_TRUE`.
- [ ] Predicate stages have independent state and secret-safe failures; no generic catch merges their results, and `$Host`/`$host` is never referenced, assigned, or shadowed.
- [ ] Exactly the same pre-existing private bundle is held only in memory for P5 and the later permitted stages; no alternate or persisted bundle is used.
- [ ] P5 success precedes exactly one source audit, exactly one fresh external Java compilation, exactly one JDBC connection, exactly one absence precheck, at most one exact patch application, and exactly one post-execution structure set-delta verification.
- [ ] Success is limited to expected-object `PRESENT`, additions `1`, removals `0`, and unexpected additions `0`; any other outcome terminates without a follow-up action.
Performance:
- [ ] The operation has one terminal path with no retry, repair, rollback, compensation, status interruption, sidecar review, or parallel activity after runner construction begins.
- [ ] The external runner source, class, logs, and directory are removed on every terminal path.
Quality:
- [ ] Summary and Evidence Pack contain only permitted sanitized P5 codes, stage categories/counts, bounded structural-delta counts, cleanup result, documentation-validation result, diff-check result, terminal result, and residual risk.
- [ ] `python .agents/skills/validate-docs/scripts/validate_docs.py` passes after creating the two deliverables.
- [ ] `git diff --check -- deliverables/user/REQ-20260817-ATS-005.md deliverables/agent/WI-20260817-ATS-007-handoff.md deliverables/user/WI-20260817-ATS-007-summary.md deliverables/agent/WI-20260817-ATS-007-evidence-pack.md` passes.

[INPUT POINTERS]
Tier 0 (Constitution - Required):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/evidence-pack-standard.md

Tier 1 (Policies - Required by REQ):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2 (Tech Stack - Auto-injected for qa-integ):
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-005.md
- deliverables/agent/WI-20260817-ATS-005-handoff.md
- deliverables/agent/WI-20260817-ATS-005-evidence-pack.md
- deliverables/user/WI-20260817-ATS-005-summary.md
- deliverables/agent/WI-20260817-ATS-002-handoff.md
- deliverables/agent/WI-20260817-ATS-002-evidence-pack.md
- deliverables/user/WI-20260817-ATS-002-summary.md
- deliverables/agent/WI-20260817-ATS-004-handoff.md

Files:
- scripts/database/patches/WI-20260809-ATS-068-user-consents.sql (read-only source audit; no source text or object identity in retained outputs)
- Private bundle: pre-existing approved input; intentionally no repository path or identity is recorded. Keep it in memory only.

Repro/Logs:
- Retain no raw command, runner source, compiler output, DB output, exception detail, private input, credential, datasource, target, patch text, or temporary-artifact pointer.
- Documentation validation: `python .agents/skills/validate-docs/scripts/validate_docs.py`
- Scoped documentation-only diff check: `git diff --check -- deliverables/user/REQ-20260817-ATS-005.md deliverables/agent/WI-20260817-ATS-007-handoff.md deliverables/user/WI-20260817-ATS-007-summary.md deliverables/agent/WI-20260817-ATS-007-evidence-pack.md`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-007-summary.md :
- State only the three P5 aggregate true codes when P5 succeeds, permitted source-audit/compiler/connection/precheck/patch/delta counts and categories, bounded delta counts, cleanup, documentation-validation, diff-check, terminal result, and residual risk.
- Do not state private-bundle, field, credential, datasource, target, SQL, raw diagnostic, command, source, path, or temporary-artifact details.

Agent-facing -> deliverables/agent/WI-20260817-ATS-007-evidence-pack.md :
- Map G1-G5 from REQ-20260817-ATS-005 to the preserved historical record, corrected P5 aggregate result, strict memory boundary, exact one-shot action counts, terminal cleanup, documentation validation, and scoped diff check.
- Confirm that no prohibited detail or action was retained. Historic false negatives are context only and do not authorize or require a repair attempt.

Handoff Packet -> deliverables/agent/WI-20260817-ATS-007-handoff.md :
- This packet is the sole traceable authorization for the final qa-integ operation.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required. Use only the REQ, this packet, the listed historical sanitized records, the approved patch path, fixed P5 aggregate codes, stage categories/counts, bounded delta counts, cleanup result, validation result, and diff-check result. Do not retain commands, raw output, source text, private values, or database-identifying detail.
Tests: No product test, build, application start, runtime action, browser action, or extra database action is authorized. Record only the bounded one-shot operation and documentation validation outcomes.
Rollback (if needed): No rollback, retry, repair, compensation, deletion, `DROP`, extra SQL, connection, query, or follow-up operation is authorized. On any non-success outcome, remove only the external temporary runner artifacts, preserve sanitized evidence, and stop.

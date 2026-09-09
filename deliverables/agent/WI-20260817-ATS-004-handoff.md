
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A020: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a020). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

[WI HEADER]
WI ID: WI-20260817-ATS-004
REQ: REQ-20260817-ATS-003
Agent: qa-integ
Depends On: WI-20260817-ATS-002 = PASS; WI-20260817-ATS-003 = PRE_EXECUTION_BLOCKED (MA interruption before runner construction)
Blocks: -

[WI SUMMARY]
Why: Complete the single final authorized attempt to apply the approved development-database patch. WI-20260817-ATS-003 was MA-interrupted before runner construction for a status check; it was an execution-precondition interruption, not a guard, private-bundle, compiler, or database technical failure. It performed zero DB connections, DDL, and data changes.

Scope (in/out):
- In: Confirm the predecessor evidence records `WI-20260817-ATS-002` as `PASS` and `WI-20260817-ATS-003` as `PRE_EXECUTION_BLOCKED` before runner construction. Treat the latter only as the recorded MA interruption described above.
- In: Create, compile, and use exactly one new minimal Java runner in an external volatile location. Do not reuse a prior runner or temporary artifact. Any PowerShell wrapper must not reference or assign `$Host` or `$host`, and must not assign an automatic variable.
- In: Before any DB connection, use exactly one pre-existing approved private bundle. Strictly validate its JSON, required datasource completeness, and exact approved scope entirely in memory. Keep the bundle and all derived target, credential, datasource, and path details opaque.
- In: Perform one sanitized source audit of `scripts/database/patches/WI-20260809-ATS-068-user-consents.sql` before DB interaction, then open exactly one DB connection and perform this exact order once: one `user_consents` absence precheck, one unchanged patch application, and one post-execution structure set-delta verification.
- In: Treat `user_consents` as the only expected added object. Success requires expected-object `PRESENT`, additions `1`, removals `0`, and unexpected additions `0`.
- In: Proceed from runner construction to one terminal result with no MA status interruption, progress-sidecar review, or other sidecar action while execution is in progress. Delete every external temporary source, class, log, and directory artifact on every terminal result, then create only the required sanitized Summary and Evidence Pack.
- Out: Any second attempt, retry, repair, rollback, compensation, data deletion, `DROP`, DB creation/removal, additional SQL, additional connection, additional query, target expansion, bundle creation/modification/replacement/search/copy/persistence, secret disclosure, product-file change, configuration change, schema-source change, patch change, application/runtime/browser/test/build action, external action, or Git action other than required documentation validation and `git diff --check`.
- Out: Any operational status interruption, progress report, sidecar review, or other action between runner construction and terminal cleanup. Do not create, use, retain, cite, or attach a disposable proof artifact.

DoD:
- The execution record identifies WI-20260817-ATS-002 as `PASS` and WI-20260817-ATS-003 as MA-interrupted before runner construction, not technically failed; the prior WI has no DB, DDL, or data change.
- Exactly one newly compiled external volatile runner reaches one terminal result without a status interruption or sidecar review after construction begins. Raw compiler diagnostics, commands, source text, paths, arguments, classpath values, and logs are not read for reporting or retained.
- Exactly one pre-existing approved private bundle passes strict in-memory JSON, required-datasource, and exact-scope validation before the single permitted DB connection. A missing, malformed, ambiguous, incomplete, mismatched, or unexpected condition fails closed before connection.
- The exact unchanged approved patch passes the source audit and is used in the only permitted DB sequence: connection `1`, absence precheck `1`, patch application `1`, and post-execution structure-delta verification `1`.
- Only expected-object `PRESENT`, additions `1`, removals `0`, and unexpected additions `0` constitute success. Every other terminal result is non-success, triggers temporary-artifact cleanup, preserves only sanitized evidence, and authorizes no further action.
- The required Summary and Evidence Pack retain only sanitized gate statuses, action counts, expected-object state, delta counts, cleanup outcome, documentation-validation outcome, and diff-check outcome. `validate_docs.py` and the target REQ documentation `git diff --check` pass.

Constraints/Forbidden:
- Fail closed. Any false, missing, malformed, incomplete, ambiguous, mismatched, or unexpected input or result stops at one terminal result. Do not branch to retry, alternate input, repair, rollback, compensation, deletion, `DROP`, extra SQL, extra query, or extra DB connection.
- This is the final exact one-runner, one-shot authorization. Do not resume or reinterpret WI-20260817-ATS-003 as a failed technical attempt, and do not use its non-started runner state to justify an extra attempt.
- Use one fresh external volatile runner only. Do not create, compile, retain, reuse, or execute runner artifacts in the repository. Do not use `$Host` or `$host` in any form and do not assign PowerShell automatic variables.
- Use exactly one pre-existing approved private bundle. Do not create, modify, replace, search for, copy, persist, infer, print, log, disclose, or retain its identity, path, name, value, contents, datasource details, target, credentials, derived values, or raw validation output.
- Complete all strict bundle validation in memory before the single connection. Complete the sanitized SQL source audit before DB interaction. Do not connect unless both gates have an unambiguous sanitized `PASS` result.
- After connection, perform no action other than the one absence precheck, one exact unchanged patch application, and one post-execution structure set-delta verification. Do not inspect unrelated objects or data. Do not apply the patch unless the precheck reports the expected absence.
- Do not accept an operational status interruption, emit a progress status, or perform a sidecar review after runner construction starts. Do not perform any other action until terminal cleanup is complete.
- Never retain, display, or document raw compiler diagnostics, DB errors, result sets, SQL output, command text, temporary-artifact details, secret values, credentials, datasource values, private-bundle data, or database-identifying information. Sanitize every retained result.
- Do not alter product files, configuration, schema source, the approved patch, data, policies, or design. Do not run the application, runtime, browser, product tests, builds, or external actions. Do not run Git except the required documentation-only `git diff --check`.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Predecessor evidence confirms `WI-20260817-ATS-002 = PASS`; WI-20260817-ATS-003 is recorded as MA-interrupted before runner construction and not a technical failure, with DB connection, DDL, and data-change counts all `0`.
- [ ] Exactly one new external volatile runner is compiled and used, without prior-runner reuse or `$Host`/`$host` use.
- [ ] Exactly one pre-existing private bundle passes strict opaque in-memory JSON, required-datasource, and scope validation before exactly one DB connection; no bundle, secret, credential, datasource, target, path, or raw output is retained.
- [ ] The runner performs exactly one sanitized SQL source audit, one `user_consents` absence precheck, one unchanged patch application, and one post-execution structure set-delta verification in that order after the single connection.
- [ ] The only success evidence is expected-object `PRESENT`, additions `1`, removals `0`, and unexpected additions `0`; every other result fails closed with cleanup and no follow-up action.
- [ ] Runner construction through terminal cleanup completes without operational status interruption, progress-sidecar review, retry, rollback, or any other action.
Performance:
- [ ] Not applicable: this is a single terminal, fail-closed operation with no retry or performance-tuning path.
Quality:
- [ ] G1 is traceable to the sanitized WI-002 `PASS` evidence and the WI-003 MA-interrupted pre-construction record.
- [ ] G2 is traceable to one new runner, exactly one connection, one precheck, and at most one patch application.
- [ ] G3 is traceable to the expected `user_consents` addition only, with removals `0` and unexpected additions `0`.
- [ ] G4 is traceable to terminal-only reporting after runner construction, no status interrupt, no sidecar review, and no retry on actual failure or mismatch.
- [ ] G5 is traceable to sanitized evidence, temporary-artifact cleanup, `validate_docs.py`, and target REQ documentation `git diff --check` outcomes.

[INPUT POINTERS]
Tier 0 (Constitution and standards - Required):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
- docs/standards/evidence-pack-standard.md

Tier 1 (Policies and architecture - Inferred from validation, secret boundary, and WI workflow):
- docs/policies/quality-gates.md
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/architecture/system-design.md

Tier 2 (Workflow - Required by context-injection rules):
- docs/templates/eval-report-template.md
- docs/templates/requirements-request-template.md
- docs/templates/wi-subagent-handoff-template.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-003.md
- deliverables/user/REQ-20260817-ATS-002.md
- deliverables/user/WI-20260817-ATS-002-summary.md
- deliverables/agent/WI-20260817-ATS-002-evidence-pack.md
- deliverables/user/WI-20260817-ATS-003-summary.md
- deliverables/agent/WI-20260817-ATS-003-evidence-pack.md

Files:
- scripts/database/patches/WI-20260809-ATS-068-user-consents.sql

Repro/Logs:
- Retain only sanitized predecessor status, interruption classification, pre-connection gate statuses, action counts, expected-object state, set-delta counts, terminal result, cleanup outcome, documentation-validation outcome, and documentation-only diff-check outcome. Do not retain commands, source text, raw compiler/DB diagnostics, DB output, private-bundle data, datasource data, credentials, secrets, database identifiers, temporary paths, or disposable proof.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-004-summary.md :
- State WI-002 `PASS`, WI-003 MA-interrupted-before-runner-construction classification, fresh-runner compilation category, strict in-memory validation category, SQL source-audit category, connection count, absence-precheck category, patch-application count/category, expected-object state, additions/removals/unexpected counts, cleanup category, documentation-validation outcome, diff-check outcome, and terminal result.
- State residual risk and the no-retry/no-repair/no-rollback/no-other-action boundary. Do not retain or disclose raw compiler/DB output, commands, paths, SQL text, bundle data, datasource/target details, credentials, secrets, or disposable proof.
Agent-facing -> deliverables/agent/WI-20260817-ATS-004-evidence-pack.md :
- Map G1 through G5 to the sanitized predecessor/interruption record, fresh-runner result, opaque in-memory validation, SQL source audit, exact action counts, set delta, no-interrupt terminal path, cleanup, and documentation-validation evidence.
- Confirm one connection, one precheck, one patch application, and one post-execution delta verification only when all pre-connection gates passed. Record `PRESENT`, additions `1`, removals `0`, and unexpected additions `0` only for success; every non-success is fail-closed with no follow-up action.
- Confirm prohibited actions, all raw compiler/DB diagnostics, private-bundle data, datasource/target data, credentials, secrets, temporary artifacts, and disposable proof were not retained.
Handoff Packet -> deliverables/agent/WI-20260817-ATS-004-handoff.md :
- This packet (for traceability).

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required. Use only the approved REQ, predecessor Summary and Evidence Pack, WI-003 Summary and Evidence Pack, this WI, the exact approved patch path, sanitized action labels, interruption classification, action counts, expected-object state, set-delta counts, cleanup category, documentation-validation outcome, and documentation-only diff-check outcome. Do not include command text, line-level SQL, raw output, or sensitive values.
Tests: No product test, build, application start, runtime action, browser action, external action, or additional DB action is authorized. Record only sanitized fresh-runner compilation readiness, in-memory validation, source audit, allowed action counts, structure-delta result, terminal cleanup, documentation validation, and diff check.
Rollback (if needed): No rollback, retry, repair, compensating action, data deletion, `DROP`, additional SQL, additional DB operation, or other action is authorized. On every terminal non-success result, clean external temporary artifacts, preserve only sanitized evidence, and stop.

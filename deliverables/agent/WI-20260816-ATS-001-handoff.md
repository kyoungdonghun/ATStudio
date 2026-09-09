
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A010: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a010). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

[WI HEADER]
WI ID: WI-20260816-ATS-001
REQ: REQ-20260816-ATS-001
Agent: qa-integ
Depends On: -
Blocks: WI-20260816-ATS-002
State: READY_TO_RERUN

[WI SUMMARY]
Why: Prepare the explicitly approved local acceptance/development database for a later runtime acceptance activity by applying the approved `user_consents` patch exactly once and preserving a sanitized structural proof. This WI does not start that runtime.

Scope (in/out):
- In: Use exactly one user-approved target: the current local MySQL `atstudio` development/acceptance database. Require an opaque, non-printing internal preflight validation that the connection resolves only to loopback MySQL and database name `atstudio`; run the exact `scripts/database/patches/WI-20260809-ATS-068-user-consents.sql` once only when every precondition passes; record sanitized pre/post structural checks and sanitized application logs.
- In: Preserve the prior bounded discovery and opaque-validation attempts as history. A new, single-use recovery path may use one precisely pre-established opaque external regular-file bundle. Its identity is fixed by prior metadata-only discovery and opaque non-output classification; it is not discovered, selected by recency, or substituted at execution. Before any connection, an in-memory parser must validate a flat JSON object with all three required datasource fields nonempty, a loopback MySQL endpoint on port `3306`, and database name `atstudio`. Any failure, ambiguity, or parser output fails closed before SQL.
- In: Verify that the only permitted schema addition is `user_consents`, that no existing data is deleted, and that no other schema object changes.
- Out: Any database other than the current local MySQL `atstudio` development/acceptance database, including remote, stage, production, protected/system schemas, and any disposable database for this first WI; more than one target database is forbidden.
- Out: Any environment-bundle access except the historical bounded discovery and the new precisely pre-established opaque recovery path above; any ignored configuration, credentials, connection strings, host details, usernames, tokens, raw SQL result rows, or PII. Do not print, copy, persist, or otherwise inspect them outside the required in-memory validation.
- Out: Application startup, acceptance lifecycle scripts, browser work, external providers, email, payment, refund, OAuth, product files, Git stage/commit/push, alternate bundle selection, or any cleanup that deletes data or drops a table/database.

DoD:
- The executor runs the required opaque recovery preflight and pre-structure check before SQL. The preflight uses only the precisely pre-established bundle and establishes the required flat-JSON, nonempty-datasource-field, loopback-MySQL-port-`3306`, and `atstudio` database classification in memory without output. Any mismatch, ambiguity, or inability to establish that scope fails closed before SQL.
- A sanitized pre-structure check proves `user_consents` is absent and records only approved status/count/diff fields, never raw rows or connection-sensitive values.
- The specified patch is applied at most once, and only after every precondition passes, with a sanitized application log showing start/result and no sensitive values.
- A sanitized post-structure check proves `user_consents` is present and that the structural delta is limited to that object; it records no data rows, credentials, host details, or exact database name.
- If `user_consents` already exists, the patch is non-idempotently conflicted, the target/precondition is uncertain, execution is partial/ambiguous, or any unexpected DDL/DML/data-deletion indication occurs, stop immediately. Do not rerun, repair, run compensating SQL, or broaden the target. This narrowly authorized rerun does not alter the historical no-DDL claims.
- Rollback statement: no automatic `DROP TABLE`, `DROP DATABASE`, data deletion, or compensating DDL is performed. An ambiguous or failed application is reported for separately approved manual recovery.

Constraints/Forbidden:
- The historical bounded discovery rule remains history only. The sole permitted rerun input is one precisely pre-established opaque external bundle based on prior metadata-only discovery and opaque non-output classification. Do not discover, search, select by recency, substitute, or use inherited environment credentials at execution.
- The in-memory parser must accept only a flat JSON object whose three required datasource fields are nonempty and whose datasource classification is loopback MySQL on port `3306` with database name `atstudio`. Any parser, shape, completeness, or scope failure is a sanitized fail-closed result; do not connect or execute SQL.
- Do not print, copy, persist, validate by output, or include any bundle path, filename, target name, value, URL, host, username, token, password, raw error, or bundle content in a deliverable or log.
- Execute only `scripts/database/patches/WI-20260809-ATS-068-user-consents.sql`; do not add SQL, make the patch idempotent, or apply it to a disposable database.
- Keep all evidence sanitized: status labels, approved scope classification, presence/absence, expected-object count, unexpected-object count, and pass/fail reason codes are allowed. Sensitive values and exact target identifiers are forbidden.
- Do not start the application or acceptance lifecycle. This WI is database-only preparation, not runtime, API, browser, or final acceptance evidence.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Before SQL, the executor uses only the precisely pre-established opaque external bundle and completes an in-memory flat-JSON/nonempty-three-datasource-field/loopback-MySQL-port-`3306`/`atstudio` preflight. The operator records only a sanitized PASS or failure reason; execution-time discovery, recency selection, or substitution is forbidden. Any mismatch or uncertainty terminates the WI before SQL. No other database, including protected/system schemas, remote, stage, production, or a disposable database, is contacted.
- [ ] The pre-check records `user_consents=ABSENT`; otherwise the WI stops with a non-idempotent-conflict result and does not execute SQL.
- [ ] The only SQL executed is `scripts/database/patches/WI-20260809-ATS-068-user-consents.sql`, at most once and only after preconditions pass.
- [ ] The post-check records `user_consents=PRESENT`, allowed structural additions limited to `user_consents`, unexpected structural additions `0`, and no existing-data deletion indication.
- [ ] The application log and both structural checks are sanitized and contain no bundle contents, connection details, credentials, environment values, raw rows, or PII.
- [ ] No application, acceptance runtime, browser, provider, mail, payment, refund, OAuth, Git, or product-file action occurs.
Performance:
- [ ] Not applicable: this WI has no response-time or memory target. Execution is bounded by the one-time guarded database operation.
Quality:
- [ ] G1, G2, G5, and G6 from `REQ-20260816-ATS-001` are evidenced or the WI is explicitly blocked before the risky action.
- [ ] No automatic rollback/drop is attempted; any failure state and residual risk are recorded in the two-set deliverables.
- [ ] The evidence is reproducible from sanitized command/action labels, result codes, and file pointers without exposing sensitive values.
- [ ] The WI remains `READY_TO_RERUN` until the current run records successful preflight, pre-structure check, one permitted patch application, and post-structure verification. `WI-20260816-ATS-002` remains blocked until then.

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
- scripts/acceptance/README.md
- scripts/database/README.md
- deliverables/user/WI-20260809-ATS-068-summary.md
- deliverables/agent/WI-20260809-ATS-068-decision-register.md
- deliverables/agent/WI-20260809-ATS-068-evidence-pack.md

Files:
- scripts/database/patches/WI-20260809-ATS-068-user-consents.sql

Repro/Logs:
- Authorized internal operator invocation for the approved local target: record only sanitized action labels, pre/post structural result codes, and patch outcome; do not print or record connection values, command arguments containing sensitive values, target identifiers, or raw database output.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260816-ATS-001-summary.md :
- State whether the one-time application completed or was blocked, the sanitized structural result, residual risk, rollback statement, and the approval boundary for the next WI.
- Do not include exact target names, bundle paths/contents, connection details, credentials, environment values, raw rows, PII, or application logs with sensitive content.
Agent-facing -> deliverables/agent/WI-20260816-ATS-001-evidence-pack.md :
- Provide sanitized evidence pointers for preflight, pre-structure check, one application attempt/result, post-structure check, quality-gate mapping, non-execution of runtime/browser/external actions, reproducibility steps, and follow-up WI `WI-20260816-ATS-002`.
- Record whether the result satisfies the dependency gate. A block or ambiguous state must prevent WI-002 from starting.
Handoff Packet -> deliverables/agent/WI-20260816-ATS-001-handoff.md :
- This packet (for traceability).

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required. Use sanitized result-code/action labels and repository file pointers only; no sensitive output or exact target identity.
Tests: No product test or application start is authorized. Record the structural verification result and the absence of runtime/browser/external actions.
Rollback (if needed): No automatic rollback is authorized. Never drop `user_consents`, a database, or data. Report any failed/ambiguous state for separately approved manual recovery.

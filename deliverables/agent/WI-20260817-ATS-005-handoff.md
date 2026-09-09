
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A022: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a022). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

[WI HEADER]
WI ID: WI-20260817-ATS-005
REQ: REQ-20260817-ATS-004
Agent: qa-integ
Depends On: WI-20260817-ATS-004 records JDBC connections, DDL, and data changes as 0; compiler readiness as PASS
Blocks: WI-20260817-ATS-006 until every required WI-20260817-ATS-005 predicate code is PASS

[WI SUMMARY]
Why: Restore predicate-level observability for the already approved private-bundle gate without treating an earlier wrapper's generic FAIL_CLOSED result as evidence that the bundle is defective.

Scope (in/out):
- In: Read the same single pre-existing approved private bundle strictly in memory. Independently evaluate and output only secret-safe predicate codes for: regular readable file; JSON syntax; flat dictionary; every required key's presence, type, and nonblank state; JDBC pattern; loopback host; port; and database name.
- In: For every required key, emit an individual fixed-code result or a fixed grouped schema that preserves one result per required key. A fixed contract field label or opaque fixed ordinal is allowed; observed keys, values, paths, and other private input are not.
- In: Maintain a separate narrow error boundary for each predicate. An unexpected exception must produce only a coarse fixed non-secret stage code that distinguishes its stage, such as `UNEXPECTED_EXCEPTION_FILE_READ` or `UNEXPECTED_EXCEPTION_JSON_PARSE`, without an exception message, type, stack trace, or other diagnostic detail.
- In: Remove any temporary artifact created during the diagnostic before creating the required sanitized deliverables. Record only the cleanup outcome, never artifact names or locations.
- Out: Java, Java compilation, compiler invocation, DB connection, JDBC connection, query, SQL, DDL, data operation, patch execution, application/runtime/browser action, product/configuration/schema-source change, private-bundle creation/modification/replacement/search/copy/persistence, and Git activity other than the required documentation validation and documentation-only diff check.

DoD:
- The same private bundle is handled only in memory and no private value, bundle identity, path, target, credential, raw error, identifier, observed key, or derived datasource value is displayed, logged, retained, or added to any deliverable.
- Each required predicate has its own sanitized result. The result set covers `FILE_READABLE_REGULAR`, `JSON_SYNTAX`, `FLAT_DICTIONARY`, each required-key `PRESENT_TYPE_NONBLANK` result, `JDBC_PATTERN`, `LOOPBACK_HOST`, `PORT`, and `DATABASE_NAME`.
- A generic catch does not collapse distinct predicates into `NOT_STARTED`, `FAIL_CLOSED`, or another shared status. Any unexpected exception is recorded only with a coarse, fixed, stage-specific non-secret code and does not erase results already produced for other predicates.
- JDBC connections, Java/compiler activity, SQL, DDL, and data changes remain `0`.
- `WI-20260817-ATS-006` is not started, delegated, or unblocked unless every required predicate code is `PASS` and no unexpected-exception stage code is present.
- Temporary-artifact cleanup, `validate_docs.py`, and the documentation-only `git diff --check` each pass. The sanitized Summary and Evidence Pack are written to the fixed paths below.

Constraints/Forbidden:
- Use exactly the same one pre-existing approved private bundle. Read it strictly in memory; do not create, modify, replace, search for, copy, persist, serialize, cache, print, log, cite, attach, or disclose it or any derivative.
- Do not emit a value for any predicate. Do not emit bundle paths, file names, target details, host values, port values, database names, credentials, key values, raw exception text, exception classes, stack traces, command text, or diagnostic logs.
- Treat a regular-file/readability failure as its own predicate result. Do not read a non-regular file and do not recover with an alternate input.
- Parse only after the file predicate permits it, but preserve the reason as that predicate's own code; unavailable downstream predicate work may be represented only by fixed prerequisite-aware codes, never by a generic catch-collapse status.
- Require a JSON object that is a flat dictionary. Reject nested arrays/objects and non-string or blank required values according to the approved fixed contract; do not report observed structures or values.
- Use fixed, independently reported validation stages for JDBC pattern, loopback host, port, and database name. Do not connect to, resolve, probe, or otherwise contact any host, port, database, or service.
- Do not use `$Host` or `$host` in any form and do not assign or shadow PowerShell automatic variables.
- Do not use Java, a compiler, SQL tooling, an application runtime, a browser, tests, build tools, or external actions. Do not touch DB, bundle, secrets, compiler, application, configuration, schema source, patch source, or Git except the required documentation validation and documentation-only diff check.
- Fail closed with no retry, repair, rollback, compensation, deletion, `DROP`, extra SQL, alternate bundle, target expansion, or follow-up DB activity. A predicate result that is not `PASS`, an unexpected-exception stage code, cleanup failure, or evidence-sanitization failure ends this WI without starting WI-006.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] The diagnostic uses only the same pre-existing approved private bundle and keeps the complete bundle lifecycle in memory.
- [ ] Sanitized individual predicate codes cover regular readable file, JSON syntax, flat dictionary, every required key's presence/type/nonblank state, JDBC pattern, loopback host, port, and database name.
- [ ] The result schema contains no private values, paths, bundle identity, credentials, target details, raw diagnostics, or observed input text.
- [ ] Each predicate is isolated from the others; a generic catch never overwrites multiple predicate results with `NOT_STARTED`, `FAIL_CLOSED`, or another common terminal status.
- [ ] An unexpected exception is retained only as a fixed coarse stage code that identifies the relevant stage and exposes no error detail.
- [ ] Java/compiler activity, JDBC connections, SQL, DDL, data operations, and patch applications are each `0`.
- [ ] WI-006 remains blocked unless all required predicate codes are `PASS` and no unexpected-exception stage code is emitted.
Performance:
- [ ] The diagnostic makes no network, database, Java/compiler, SQL, runtime, browser, or external-service call.
- [ ] No temporary artifact remains after the terminal result.
Quality:
- [ ] `validate_docs.py` passes after writing the sanitized Summary and Evidence Pack.
- [ ] `git diff --check -- deliverables/user/REQ-20260817-ATS-004.md deliverables/agent/WI-20260817-ATS-005-handoff.md deliverables/user/WI-20260817-ATS-005-summary.md deliverables/agent/WI-20260817-ATS-005-evidence-pack.md` passes.
- [ ] All retained output is secret-safe and limited to fixed predicate codes, action counts, cleanup outcome, validation outcome, and the WI-006 gate outcome.

[INPUT POINTERS]
Tier 0 (Constitution - Required):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Required by REQ and inferred from sensitive-input validation):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 1 (Workflow):
- docs/architecture/system-design.md

Tier 2 (Quality and workflow templates):
- docs/standards/evidence-pack-standard.md
- docs/templates/wi-subagent-handoff-template.md
- docs/templates/eval-report-template.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-004.md
- deliverables/user/WI-20260817-ATS-004-summary.md
- deliverables/agent/WI-20260817-ATS-004-evidence-pack.md
- deliverables/agent/WI-20260817-ATS-004-handoff.md

Files:
- Private bundle: approved pre-existing private input; intentionally no repository path or identity is recorded in this packet. Read only in memory.

Repro/Logs:
- Documentation validation only: `python .agents/skills/validate-docs/scripts/validate_docs.py`
- Documentation-only diff check only: `git diff --check -- deliverables/user/REQ-20260817-ATS-004.md deliverables/agent/WI-20260817-ATS-005-handoff.md deliverables/user/WI-20260817-ATS-005-summary.md deliverables/agent/WI-20260817-ATS-005-evidence-pack.md`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-005-summary.md :
- Record only the fixed predicate-code schema and results, Java/compiler/JDBC/SQL/DDL/data counts, cleanup outcome, documentation-validation outcome, documentation-only diff-check outcome, WI-006 gate outcome, and residual risk.
- Do not include private values, path/identity data, bundle details, target details, observed keys, raw diagnostics, command output, or temporary-artifact information.

Agent-facing -> deliverables/agent/WI-20260817-ATS-005-evidence-pack.md :
- Record pointers to this REQ, this handoff, and WI-004 Summary/Evidence; the same sanitized predicate results; action counts; temporary-cleanup outcome; validation commands and PASS/FAIL outcomes; and the explicit `Blocks: WI-20260817-ATS-006` gate.
- Do not retain bundle content or identity, values, paths, credentials, target data, host/port/database values, raw exceptions, logs, observed key names, or disposable proof artifacts.

Handoff Packet -> deliverables/agent/WI-20260817-ATS-005-handoff.md :
- This packet is the traceable delegation contract. It contains no private bundle pointer beyond the deliberate statement that it is pre-existing, private, and memory-only.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required, but use only the fixed document paths above. Do not create, retain, cite, or attach a private-input, command, log, or temporary-artifact pointer.
Tests: Not applicable. The allowed verification is documentation validation and the scoped documentation-only diff check; record only their sanitized outcomes.
Rollback (if needed): Not applicable. This diagnostic must not modify the DB, bundle, code, configuration, patch, schema source, or product. On any non-PASS predicate, unexpected-exception stage code, cleanup failure, or sanitization failure, terminate with WI-006 still blocked and perform no follow-up action.


> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A018: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a018). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

[WI HEADER]
WI ID: WI-20260817-ATS-003
REQ: REQ-20260817-ATS-002
Agent: qa-integ
Depends On: WI-20260817-ATS-002 = PASS
Blocks: -

[WI SUMMARY]
Why: Execute the approved one-shot database patch phase only after the predecessor's compile-proven readiness `PASS`, while keeping the approved private bundle, datasource target, credentials, and raw diagnostics opaque.

Scope (in/out):
- In: Create, compile, and use one fresh minimal Java runner only in an external volatile location. Do not reuse any prior runner or temporary artifact. Any PowerShell wrapper must not reference or assign `$Host` or `$host`, and must not assign any automatic variable.
- In: Before any DB connection, use exactly one pre-existing approved private bundle. Parse its JSON strictly and validate required datasource completeness and the exact approved scope entirely in memory. Do not document, print, persist, infer, or expose the bundle's identity, path, name, value, content, datasource target, credential, or connection detail.
- In: Perform the permitted sequence once: sanitized SQL source audit of `scripts/database/patches/WI-20260809-ATS-068-user-consents.sql`; one expected-object absence check; one exact application of that unchanged patch; and one post-execution structure set-delta verification. Open exactly one DB connection only after all in-memory validation and the SQL source audit pass.
- In: Treat `user_consents` as the sole expected added object. Report the post-execution result only as expected-object presence and set-delta counts: additions `1`, removals `0`, unexpected `0`.
- In: Delete every external temporary source, class, log, and directory artifact on every terminal result. Preserve only sanitized status categories and the sanitized cleanup result in the required deliverables.
- Out: Private-bundle creation, modification, replacement, search, copy, persistence, or disclosure; secret, credential, datasource, DB-target, or connection-detail disclosure; any additional DB connection, query, SQL, patch, schema operation, or target.
- Out: Retry, repair, rollback, compensation, `DROP`, data deletion, DB creation or removal, product source/configuration/schema/patch change, application/runtime/browser action, external action, provider/mail/payment/refund/OAuth action, or Git action other than the required documentation-only `git diff --check`.
- Out: Creation, use, retention, citation, or attachment of a `WI-20260816-ATS-002` disposable proof artifact or any equivalent disposable proof artifact.

DoD:
- `WI-20260817-ATS-002` is confirmed as the sanitized final `PASS` with compile-proven runner readiness before this WI starts; otherwise this WI records `BLOCKED` after cleanup and performs no private-bundle or DB action.
- A newly compiled external volatile runner succeeds before private-bundle validation or DB use. Compiler results remain sanitized; raw compiler diagnostics, commands, paths, source text, arguments, classpath details, and logs are neither read for reporting nor retained.
- Exactly one existing approved private bundle is strictly parsed and completely validated in memory for JSON, required datasource, and exact scope before the single DB connection. Any absent, malformed, ambiguous, mismatched, or unexpected condition fails closed before connection.
- The runner completes the approved order with no branch for retry: SQL source audit; expected-object absence check; exact patch application once; post-execution structure set-delta verification.
- A successful terminal result reports only `PRESENT`, additions `1`, removals `0`, and unexpected `0`, together with sanitized gate, action-count, and cleanup results. Every other terminal result is non-success and authorizes no follow-up action.
- All temporary artifacts are deleted on every terminal result, and the required User-facing Summary and Agent-facing Evidence Pack contain no raw compiler or DB output, secrets, bundle data, connection data, source text, command details, or disposable proof.

Constraints/Forbidden:
- Fail closed. A false, missing, malformed, incomplete, ambiguous, mismatched, or unexpected input or result stops the WI without retry, alternate input, repair, rollback, compensating action, data deletion, `DROP`, or additional SQL.
- Use one fresh external volatile runner. Do not create, compile, retain, reuse, or execute runner artifacts in the repository. Do not use PowerShell `$Host` or `$host` in any form, and do not assign PowerShell automatic variables.
- Use exactly one pre-existing private bundle and keep every aspect of it opaque. Its identity, path, name, value, contents, datasource details, target, credentials, derived values, and raw validation output must never occur in a command transcript, log, source artifact, handoff, summary, Evidence Pack, or other document.
- Do all strict JSON, required-datasource, and scope validation in memory before opening the single permitted DB connection. Do not connect when any validation or SQL source audit result is not an unambiguous sanitized `PASS`.
- Audit the supplied SQL source in memory before DB interaction, then use that exact unchanged patch once. Do not substitute, generate, edit, append, split, reformat, replay, or run additional SQL.
- After connection, perform only the approved absence check, the exact patch once, and the one post-execution set-delta check. Do not inspect unrelated objects or data. The expected-object absence check must not apply the patch unless it reports the expected absence.
- Never retain, display, or document raw compiler diagnostics, DB errors, result sets, SQL output, command text, paths, temporary artifact details, secret values, credentials, datasource values, private-bundle data, or database-identifying information. Sanitize every result before writing either deliverable.
- Do not alter product files, configuration, schema source, existing patches, or data. Do not run the application, runtime, browser, tests, build, compilation outside the external runner, Git operations other than the required documentation-only diff check, or any external action.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] The predecessor Evidence Pack and Summary establish `WI-20260817-ATS-002` as `PASS`, including one successful source-only compilation and cleanup; this WI otherwise remains `BLOCKED` with no private-bundle or DB action.
- [ ] One newly compiled external volatile runner is used, without prior-runner reuse or PowerShell `$Host`/`$host` use.
- [ ] Exactly one pre-existing private bundle passes strict in-memory JSON, required-datasource, and scope validation before exactly one DB connection; no bundle, secret, credential, datasource, target, or path information is retained.
- [ ] The runner performs the exact order once: SQL source audit, expected `user_consents` absence check, unchanged patch application once, then post-execution structure set-delta verification.
- [ ] The only success evidence is expected-object `PRESENT`, additions `1`, removals `0`, and unexpected `0`; all other outcomes fail closed with no retry or additional action.
- [ ] Every terminal result confirms external temporary source and artifact cleanup, and no disposable proof artifact is created or retained.
Performance:
- [ ] Not applicable: this is a one-shot, fail-closed operation with no retry or performance-tuning path.
Quality:
- [ ] G1 through G4 are traceable to the predecessor's sanitized `PASS` evidence and this WI's fresh-runner, opaque-input, and single-connection gates.
- [ ] G5 is traceable to one connection, one absence check, one exact patch application, and one post-execution set-delta verification.
- [ ] G6 is traceable to the absence of retries, repair, rollback, compensation, deletion, `DROP`, extra SQL, target expansion, product edits, runtime/browser actions, Git actions, and external actions.
- [ ] G7 documentation validation and documentation-only `git diff --check` pass for the two output deliverables without retaining raw diagnostics or sensitive data.

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

Tier 2 (Tech stack and workflow - Required by context-injection rules):
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md
- docs/design/api-spec.md
- docs/templates/eval-report-template.md
- docs/templates/requirements-request-template.md
- docs/templates/wi-subagent-handoff-template.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-002.md
- deliverables/agent/WI-20260817-ATS-002-handoff.md
- deliverables/user/WI-20260817-ATS-002-summary.md
- deliverables/agent/WI-20260817-ATS-002-evidence-pack.md

Files:
- scripts/database/patches/WI-20260809-ATS-068-user-consents.sql

Repro/Logs:
- Retain only sanitized gate statuses, action counts, expected-object state, set-delta counts, and terminal cleanup status. Do not retain commands, paths, source text, raw compiler/DB diagnostics, DB output, private-bundle data, datasource data, credentials, secrets, or disposable proof.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-003-summary.md :
- State the predecessor `PASS` gate, fresh-runner compilation category, strict in-memory validation category, SQL source-audit category, connection count, absence-check category, patch-application count/category, expected-object state, additions/removals/unexpected counts, cleanup category, and terminal result.
- State residual risk and the no-retry/no-repair/no-rollback boundary. Do not retain or disclose raw compiler/DB output, commands, paths, SQL text, bundle data, datasource/target details, credentials, secrets, or disposable proof.
Agent-facing -> deliverables/agent/WI-20260817-ATS-003-evidence-pack.md :
- Map G1 through G7 to sanitized predecessor-gate, fresh-runner, opaque-input validation, SQL-source audit, exact action-count, set-delta, cleanup, and documentation-validation evidence.
- Confirm exactly one connection only when all pre-connection gates passed; record expected `PRESENT`, additions `1`, removals `0`, and unexpected `0` only for success. Record any non-success as fail-closed with no follow-up action.
- Confirm that prohibited actions and all raw compiler/DB diagnostics, private-bundle data, datasource/target data, credentials, secrets, and disposable proof were not retained.
Handoff Packet -> deliverables/agent/WI-20260817-ATS-003-handoff.md :
- This packet (for traceability).

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required. Use only the approved REQ, the predecessor handoff/Summary/Evidence Pack, this WI, the exact approved patch path, sanitized action labels, sanitized status categories, action counts, expected-object state, set-delta counts, cleanup category, documentation-validation outcome, and documentation-only diff-check outcome. Do not include command text, line-level SQL, raw output, paths other than approved repository pointers, or sensitive values.
Tests: No product test, build, application start, runtime action, browser action, external action, or additional DB action is authorized. Record only sanitized fresh-runner compilation readiness, in-memory validation, SQL-source audit, allowed action counts, structure-delta result, cleanup, documentation validation, and diff check.
Rollback (if needed): No rollback, retry, repair, compensating action, data deletion, `DROP`, or additional SQL/DB operation is authorized. On every terminal non-success result, clean external temporary artifacts, preserve only sanitized evidence, and stop.

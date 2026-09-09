
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A016: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a016). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

[WI HEADER]
WI ID: WI-20260817-ATS-002
REQ: REQ-20260817-ATS-002
Agent: qa-integ
Depends On: -
Blocks: WI-20260817-ATS-003 (WI-002 one-shot DB patch and structure-delta verification; blocked unless this WI records `PASS`)

[WI SUMMARY]
Why: Establish whether the Java compiler and MySQL Connector/J compile classpath are ready without touching the database, private bundle, or any secret. The result is the only permitted gate for the subsequent one-shot DB WI.

Scope (in/out):
- In: Create a minimal Java runner skeleton and a PowerShell wrapper only in an external, volatile temporary location. Use `javac` and the MySQL Connector/J compile classpath only to determine source-only toolchain readiness.
- In: Use only non-reserved PowerShell variable names, including names such as `$atsToolchainStatus`, `$atsCompileResult`, and `$atsTemporaryArtifactRoot`. Do not assign to `$Host` or to any PowerShell automatic variable.
- In: Convert the compile execution into exactly one sanitized result category: `PASS`, `JAVAC_UNAVAILABLE`, `CONNECTOR_CLASSPATH_UNAVAILABLE`, `RUNNER_COMPILE`, `TEMP_CLEANUP_FAILED`, or `INDETERMINATE`.
- In: Delete every temporary source, class, log, and directory artifact before finishing. Record the deletion outcome only as a sanitized result category.
- Out: Any database connection or other DB action; SQL; schema inspection or patching; private-bundle access, search, read, copy, creation, replacement, or persistence; environment-secret access; product source, configuration, patch, schema, or documentation changes other than the two required evidence deliverables.
- Out: Application, runtime, browser, Git, external-service, provider, mail, payment, refund, OAuth, retry, repair, rollback, compensation, deletion, `DROP`, or unrelated work.

DoD:
- An external volatile runner performs a source-only `javac` readiness check against the MySQL Connector/J compile classpath without DB, private-bundle, secret, application, runtime, browser, Git, or external-service access.
- The wrapper uses no PowerShell automatic variables for assignment and does not use `$Host` as a variable.
- The runner exposes, records, and documents only one allowed sanitized category. It never exposes raw compiler diagnostics, command arguments, paths, classpath details, environment values, or log contents.
- All temporary artifacts are deleted before completion. A failed or indeterminate cleanup is reported as `TEMP_CLEANUP_FAILED` or `INDETERMINATE`, respectively, and is not `PASS`.
- `WI-20260817-ATS-003` is not delegated or executed unless this WI's sanitized final category is `PASS`; every other category leaves it `BLOCKED`.

Constraints/Forbidden:
- Fail closed. Any unavailable, ambiguous, false, incomplete, or unexpected condition must stop the diagnostic after artifact cleanup and must not trigger retry, repair, alternate input, or additional work.
- The external temporary artifact is allowed solely for this source-only diagnostic. It must not be created inside the repository, retained, reused, committed, or described by location in an evidence document.
- Do not connect to, enumerate, inspect, modify, or otherwise access any DB. Do not run SQL or open any private bundle.
- Do not read, resolve, print, persist, or infer environment secrets, credentials, connection details, private-bundle identifiers, paths, names, values, or contents.
- Do not write raw compiler output, raw errors, raw logs, command lines, arguments, classpath values, paths, source text, or temporary-artifact details into either evidence deliverable or another document.
- Do not change product files, configuration, existing patches, schema, or source code. Do not create a temporary runner inside the repository.
- Do not use Git except the documentation validation and `git diff --check` required by the approved REQ.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] The diagnostic creates and uses its minimal Java runner and PowerShell wrapper only outside the repository, and removes every temporary artifact before it completes.
- [ ] The wrapper assigns only non-reserved PowerShell variables and never assigns `$Host` or any automatic variable.
- [ ] The diagnostic performs no DB connection, SQL, private-bundle operation, secret access, product change, application/runtime/browser action, or external action.
- [ ] The only documented diagnostic outcome is one of `PASS`, `JAVAC_UNAVAILABLE`, `CONNECTOR_CLASSPATH_UNAVAILABLE`, `RUNNER_COMPILE`, `TEMP_CLEANUP_FAILED`, or `INDETERMINATE`.
- [ ] `WI-20260817-ATS-003` remains blocked unless the final sanitized category is `PASS`; a non-`PASS` result authorizes no retry, repair, or follow-on DB work.
Performance:
- [ ] Not applicable: this WI is a single source-only readiness diagnostic with no retry path.
Quality:
- [ ] G1, G2, G3, G4, G6, and G7 of `REQ-20260817-ATS-002` are mapped to sanitized evidence or an explicit fail-closed block.
- [ ] No raw diagnostics, secrets, private-bundle data, DB-target data, command arguments, paths, or temporary-artifact details occur in documentation.
- [ ] The two required evidence deliverables are complete, mutually consistent, and traceable to this WI and the approved REQ.
- [ ] Documentation validation and `git diff --check` are performed for the documentation-only deliverables.

[INPUT POINTERS]
Tier 0 (Constitution and standards - Required):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/evidence-pack-standard.md

Tier 1 (Policies - Inferred from readiness validation and secret boundary):
- docs/policies/quality-gates.md
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md

Tier 2 (Tech stack and workflow - Required by context-injection rules):
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md
- docs/design/api-spec.md
- docs/templates/eval-report-template.md
- docs/templates/requirements-request-template.md
- docs/templates/wi-subagent-handoff-template.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-002.md
- deliverables/user/REQ-20260817-ATS-001.md
- deliverables/agent/WI-20260817-ATS-001-handoff.md

Files:
- None. This WI must not read product files, private bundles, secrets, database resources, or existing patch contents.

Repro/Logs:
- Preserve only the final sanitized result category and the sanitized cleanup outcome. Do not preserve commands, arguments, paths, source text, diagnostics, logs, private-bundle data, DB data, or secrets.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-002-summary.md :
- State the final sanitized readiness category, sanitized temporary-artifact cleanup outcome, whether `WI-20260817-ATS-003` remains blocked or may proceed, residual risk, and the no-retry boundary.
- Do not include raw compiler diagnostics, source text, commands, arguments, paths, classpath values, private-bundle data, DB data, secrets, credentials, or environment values.
Agent-facing -> deliverables/agent/WI-20260817-ATS-002-evidence-pack.md :
- Map G1, G2, G3, G4, G6, and G7 to sanitized result categories and cleanup evidence; record that prohibited DB, private-bundle, secret, product-change, and external actions were not performed.
- State that `WI-20260817-ATS-003` is blocked unless the final category is `PASS`. Do not include raw compiler diagnostics, source text, commands, arguments, paths, classpath values, private-bundle data, DB data, secrets, credentials, or environment values.
Handoff Packet -> deliverables/agent/WI-20260817-ATS-002-handoff.md :
- This packet (for traceability).

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required. Use only the approved REQ, this WI, the two evidence paths, sanitized action labels, sanitized result categories, and sanitized cleanup outcome. Do not include sensitive values, raw output, source text, paths, or command details.
Tests: No product test, application start, runtime action, browser action, DB action, private-bundle action, secret access, or external action is authorized. Record only the source-only readiness category and cleanup outcome.
Rollback (if needed): No retry, repair, rollback, compensating action, data deletion, `DROP`, DB operation, or follow-on action is authorized for any non-`PASS` result. Keep `WI-20260817-ATS-003` blocked for separately approved handling.

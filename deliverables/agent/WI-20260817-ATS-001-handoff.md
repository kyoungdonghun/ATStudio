
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A014: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a014). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

[WI HEADER]
WI ID: WI-20260817-ATS-001
REQ: REQ-20260817-ATS-001
Agent: qa-integ
Depends On: -
Blocks: -

[WI SUMMARY]
Why: Correct the prior fail-closed classification. The prior block was caused by an ephemeral PowerShell preflight assigning to the reserved automatic variable `$Host`, which caused an automatic-variable collision before any connection or SQL. The private bundle was not defective.

Scope (in/out):
- In: Use one pre-established private bundle only. Its identity, location, filename, contents, values, target identity, and all connection details must remain unrecorded and unavailable to human review.
- In: Use one ephemeral runner outside this repository. It must use only non-reserved variable names and complete every form, completeness, and scope validation in memory before any database action. The runner itself and its location must not be written into the repository or either evidence deliverable.
- In: After successful in-memory validation, perform exactly this ordered sequence once: one connection, one absence check for the expected schema object, one exact application of `scripts/database/patches/WI-20260809-ATS-068-user-consents.sql`, and one post-structure delta check.
- In: Produce sanitized evidence that identifies only action labels, pass/fail reason codes, approved-scope classification, expected-object presence state, addition count, removal count, and unexpected-addition count.
- Out: Any alternate input, discovery, selection, substitution, retry, rebuild, rollback, compensating action, extra SQL, target expansion, product edit, configuration edit, patch edit, data deletion, `DROP`, database creation, or database removal.
- Out: Application, runtime, browser, Git, external service, provider, mail, payment, refund, OAuth, or other external action. No work for `WI-20260816-ATS-002` is authorized by this WI.

DoD:
- The runner proves in memory that the same pre-established private bundle satisfies every approved validation predicate without printing, persisting, copying, or exposing its identity, location, filename, contents, values, target identity, or connection details.
- The runner uses no reserved PowerShell automatic variable names. It reaches one connection only after the complete in-memory validation passes.
- The one absence check returns the expected object as absent; otherwise the WI stops before patch application.
- The listed patch is applied exactly once, with no other SQL or recovery action.
- The post-structure delta check records the expected object as present, removals `0`, and unexpected additions `0`; otherwise the WI is not successful.
- Any unknown, mismatch, ambiguity, unexpected result, or execution failure stops the WI immediately. It must record only a sanitized block or failure code and must not retry.

Constraints/Forbidden:
- Fail closed before connecting when any validation predicate is unknown, unavailable, ambiguous, or false. Do not infer, repair, relax, or replace a predicate.
- Never write, print, store, copy, inspect for human review, or include in a command, log, or deliverable any private-bundle identity, path, filename, value, contents, database target identity, connection detail, credential, raw error, raw database output, or personal data.
- Use no bundle other than the same pre-established one. Do not discover inputs, search locations, select by time or metadata, use fallback inputs, or use inherited credentials.
- Do not retry any stage. Do not rebuild any environment or artifact. Do not make product, configuration, patch, or repository edits beyond the two required evidence deliverables.
- Do not delete data, issue `DROP`, create or remove a database, run rollback or compensating SQL, or perform cleanup.
- Do not start or operate an application, runtime, browser, Git command, external service, provider, mail, payment, refund, OAuth, or any unrelated workflow.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] The root cause is recorded as the ephemeral PowerShell `$Host` automatic-variable collision, not as a defect in the private bundle.
- [ ] One same pre-established private bundle is validated completely in memory by an outside-repository ephemeral runner using non-reserved variable names; no private-bundle or database-target identity is output.
- [ ] No database action begins until validation passes. The only permitted action sequence is one connection, one expected-object absence check, one exact patch application, and one post-structure delta check.
- [ ] The patch executes only after the absence result. The post-structure result records expected object present, removals `0`, and unexpected additions `0`.
- [ ] Any unknown, failure, mismatch, or ambiguity produces sanitized fail-closed evidence without connection, further SQL, retry, rebuild, rollback, deletion, drop, or scope expansion.
- [ ] `WI-20260816-ATS-002` remains outside this WI and receives no execution, evidence, or state change.
Performance:
- [ ] Not applicable: the authorized work is a single guarded, ordered operation with no retry or rebuild path.
Quality:
- [ ] G1 through G5 in `REQ-20260817-ATS-001` are evidenced with sanitized status codes or the WI is explicitly blocked before the risky action.
- [ ] Evidence contains no sensitive input, target identity, raw output, personal data, or runner location.
- [ ] The required user-facing summary and agent-facing evidence pack are complete, internally consistent, and traceable to the approved REQ and patch pointer.
- [ ] The executing agent performs no product tests, runtime/browser actions, Git operations, or external actions.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Required by the QA integration role and task):
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
- docs/standards/evidence-pack-standard.md

Tier 1 (Policies and architecture - Inferred from guarded verification, sensitive-input boundary, and WI workflow):
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
- deliverables/user/REQ-20260817-ATS-001.md
- deliverables/user/REQ-20260816-ATS-001.md
- deliverables/agent/WI-20260816-ATS-001-handoff.md
- deliverables/user/WI-20260816-ATS-001-summary.md

Files:
- scripts/database/patches/WI-20260809-ATS-068-user-consents.sql

Repro/Logs:
- Authorized operator procedure: preserve only the ordered action labels and sanitized pass/fail reason codes. Do not preserve commands, arguments, identities, locations, values, raw output, or runner artifacts.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-001-summary.md :
- State whether the ordered one-pass operation completed or was blocked, the sanitized structural result, residual risk, and the no-retry/no-rollback boundary.
- Do not include private-bundle information, database-target identity, connection details, credentials, runner location, raw output, or personal data.
Agent-facing -> deliverables/agent/WI-20260817-ATS-001-evidence-pack.md :
- Provide sanitized evidence pointers for in-memory validation, the one permitted connection, absence check, exact patch result, post-structure delta check, quality-gate mapping, and non-execution of prohibited actions.
- Record only action labels, result codes, scope classification, expected-object state, addition/removal counts, and residual risk. A blocked or ambiguous result must state that no retry is authorized.
Handoff Packet -> deliverables/agent/WI-20260817-ATS-001-handoff.md :
- This packet (for traceability).

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required. Use repository document pointers and sanitized action/result labels only; exclude all sensitive values, input identities, target identities, runner locations, command arguments, and raw output.
Tests: No product test, application start, runtime action, browser action, Git operation, or external action is authorized. Record the guarded structural-verification result or the fail-closed block result.
Rollback (if needed): No automatic rollback, compensating SQL, data deletion, `DROP`, database removal, or retry is authorized. Report an unknown, failed, or ambiguous state for separately approved handling.

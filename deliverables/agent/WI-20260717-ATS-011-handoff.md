[WI HEADER]
WI ID: WI-20260717-ATS-011
REQ: REQ-20260716-ATS-004
Agent: se, pg
Depends On: WI-20260717-ATS-010
Blocks: WI-20260717-ATS-012 final aggregation, staging/commit, repository cleanup

[WI SUMMARY]
Why: The final value-suppressing secret scan found three committed local-example credential literals that violate the repository reference-only secret policy even though they are non-production values.
Scope (in): Replace the datasource password, JWT secret, and payment billing-key secret literals in application-local.example.yml with explicit environment-variable placeholders; add a focused repository contract test that fails if these fields regain literal values; classify the remaining scanner events as runtime variable references, test fixtures, or isolated disposable-DB proof fixtures. Scope (out): reading application-local.yml, changing runtime product behavior, changing production secrets, DB mutation, Git refs/index, or broad documentation changes.
DoD: The example file contains no literal value for the three credential fields; the contract test passes; focused and applicable backend quality gates pass; rerun secret scan reports zero unresolved events.
Constraints/Forbidden: Never read or print application-local.yml or any secret value. Do not expose prior literals in reports or diffs. Do not weaken startup fail-closed validation. Do not edit files outside the assigned write set.

[ACCEPTANCE CRITERIA]
- [ ] spring.datasource.password references DB_PASSWORD without a literal fallback.
- [ ] jwt.secret references JWT_SECRET without a literal fallback.
- [ ] app.payment.billing.encryption-keys[0].secret references PAYMENT_BILLING_KEY_0_SECRET without a literal fallback.
- [ ] A repository-level test proves those exact fields remain reference-only.
- [ ] No production behavior or ignored local configuration is changed.
- [ ] Focused test, diff check, and value-suppressing scan pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Context:
- application-local.example.yml
- src/test/java/com/atstudio/atstudio/config/V1BackendBaselineContractTest.java
- deliverables/agent/WI-20260717-ATS-010/repository-readiness.md

[WRITE SET]
- application-local.example.yml
- src/test/java/com/atstudio/atstudio/config/V1BackendBaselineContractTest.java
- deliverables/agent/WI-20260717-ATS-011/remediation.md

[OUTPUT CONTRACT]
Implementation evidence -> deliverables/agent/WI-20260717-ATS-011/remediation.md
Handoff Packet -> deliverables/agent/WI-20260717-ATS-011-handoff.md

[TRACEABILITY REQUIREMENTS]
Report paths, field names, commands, results, and classifications only. Never reproduce credential values. End with PASS/BLOCK and rollback instructions.

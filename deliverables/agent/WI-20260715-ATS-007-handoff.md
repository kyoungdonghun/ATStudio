[WI HEADER]
WI ID: WI-20260715-ATS-007
REQ: REQ-20260714-ATS-001
Agent: re
Depends On: WI-20260715-ATS-002, WI-20260715-ATS-003, WI-20260715-ATS-004, WI-20260715-ATS-005, WI-20260715-ATS-006
Blocks: WI-20260715-ATS-008

[WI SUMMARY]
Why: Close F-05 with independent production-engine proof that the Package A-F lock and uniqueness contracts converge on MySQL 8/InnoDB.
Scope (in): Package G only: a disposable-MySQL runner, MySQL-only Spring/JDBC concurrency tests and fixtures, seven races from design Section 10.2, exact loser assertions, failure diagnostics, Hibernate validate, guaranteed disposable DB cleanup, and two-set evidence.
Scope (out): Production code changes, retained/local/preview/production DB mutation, live Toss, UI, multi-server locks, and schema contraction.
DoD: A newly generated disposable database is created from current schema/manual patch, validated, used for all seven bounded races, and dropped in `finally`; no deadlock/timeout/connection/arbitrary exception is accepted; exact one-order/payment/refund/Incident effects are asserted; evidence is redacted.
Constraints/Forbidden: The user approved disposable DB create/drop only. Refuse any database name outside the approved disposable pattern. Never point at `atstudio`, preview, retained, stage, or production schemas. Do not print/store JDBC URLs, usernames, passwords, or secret values in repository evidence. Do not edit Package A-F production code; report a blocker instead. Preserve the running preview worktree/server.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Two renewal claimers converge to one order/command and one exact loser outcome.
- [ ] Normal finalizer versus reconciliation finalize-only converges to one payment effect.
- [ ] Two charged-upgrade requests converge to one order/provider attempt/payment effect.
- [ ] Two fresh refund claimers allow one lease and one exact in-progress loser.
- [ ] Two stale refund reclaimers allow one replacement lease; delayed old result is fenced.
- [ ] Cancellation/withdrawal cleanup race follows canonical agreement/subscription ownership and never clears key evidence twice.
- [ ] Reconciliation versus normal finalizer owns one provider transaction and resolves one matching Incident.
- [ ] Hibernate `ddl-auto=validate` passes before concurrency execution.
- [ ] Disposable DB is confirmed dropped after PASS or failure.
Performance:
- [ ] Every future/test has a strict timeout; lock wait/deadlock is a test failure with redacted diagnostics.
Quality:
- [ ] SQLState `40001`, lock timeout, deadlock, assertion timeout, connection failure, and arbitrary exceptions fail the test.
- [ ] Existing H2 focused suites remain passing after MySQL proof files are added.
- [ ] `git diff --check` passes.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/design/p1-payment-integrity-remediation-design.md
- deliverables/agent/WI-20260714-ATS-036-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-023-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-001-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-002-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-003-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-004-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-005-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-006-evidence-pack.md
Repro/precedent:
- deliverables/agent/WI-20260714-ATS-021/run-disposable-mysql-rehearsal.ps1
- deliverables/agent/WI-20260714-ATS-021/run-hibernate-validate-and-drop.ps1
- deliverables/agent/WI-20260714-ATS-021/DisposableMysqlRehearsal.java
Files:
- new src/test/java/... MySQL proof tests/support only
- new deliverables/agent/WI-20260715-ATS-007/* runner/log artifacts with redaction
- no production file ownership

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260715-ATS-007-summary.md
Agent-facing -> deliverables/agent/WI-20260715-ATS-007-evidence-pack.md
Handoff -> deliverables/agent/WI-20260715-ATS-007-handoff.md

[TRACEABILITY REQUIREMENTS]
List all seven races, exact winners/losers, row-count invariants, timeout policy, redacted commands/log pointers, Hibernate validation, DB-drop confirmation, rollback, and any residual engine/version assumptions. No secret or full JDBC evidence is permitted.

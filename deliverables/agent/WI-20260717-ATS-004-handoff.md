[WI HEADER]
WI ID: WI-20260717-ATS-004
REQ: REQ-20260716-ATS-004
Agent: se
Depends On: WI-20260717-ATS-001, WI-20260717-ATS-002
Blocks: WI-20260717-ATS-005, WI-20260717-ATS-006

[WI SUMMARY]
Why: Establish one fail-closed V1 database/configuration/payment-provider baseline that matches the cleaned application and is proven from an empty local MySQL database.
Scope (in/out): Implement INT-P01 through P05, backend/config consequences of resolved INT-V07 and V08, INT-R06, and conditional INT-R12. Canonical V1 payment provider identity is TOSS for card recurring subscriptions; flow/purpose distinguishes subscribe, renew, upgrade, refund, and other operations; retain provider interfaces for future multi-PG extension but remove speculative/legacy enum values and one-time selection config. Remove automatic root application-local.yml import, keep that file untracked/unread in output, require explicit local loading, and prevent acceptance inheritance. Make schema.sql the sole fresh-only fail-closed baseline matching current entities after WI-002, choose one deterministic six-plan baseline-data owner, retain guarded QA bootstrap disabled by default and production-forbidden, prove the schema on disposable/local-loopback MySQL, then remove the nine manual migration SQL files only after proof. Recreate the local atstudio DB only after loopback/database identity preflight; user approved loss of all local DB data. Out of scope: frontend, active docs, branch/worktree deletion, historical evidence rewriting, production/remote DB, product-policy change, multi-PG implementation.
DoD: Provider/config/schema/seed/tests are coherent; all legacy provider meanings and billing V1 envelope paths are gone; empty MySQL applies schema exactly once and second application fails; information_schema manifest matches entities; app starts with ddl-auto=validate; reusable MySQL proof suite passes; local atstudio DB is recreated from the baseline; manual SQL files are removed only after all prior proof passes; backend full tests/build pass.
Constraints/Forbidden: Never connect to non-loopback or production/stage DB. Never print, copy, commit, diff, or expose application-local.yml values. Keep application-local.yml untracked. Do not weaken billing-key V2 authenticated encryption, startup failures, recurring payment idempotency/claims/fences/locks/leases/reconciliation/audit/refund/storage safeguards, acceptance host/CORS/secret guards, QA bootstrap guards, or emergency admin subscription operations. Do not edit frontend or active docs. Do not delete manual SQL before fresh proof. Do not use createDatabaseIfNotExist/update as V1 validation. Do not revert concurrent work.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Persisted provider identity is TOSS for V1 recurring card operations, with purpose/flow separate and extensibility interfaces retained.
- [ ] Legacy MOCK/TOSS_BILLING/KAKAOPAY meanings, one-time provider selection, and V1 billing-key envelope/property paths have zero active references.
- [ ] Base config does not automatically import application-local.yml; local launch is explicit; acceptance cannot inherit ignored local secrets.
- [ ] QA bootstrap remains disabled by default and production-forbidden.
- [ ] schema.sql matches current entities, removes obsolete tables/columns, has accurate metadata, and contains no compatibility IF NOT EXISTS behavior.
- [ ] Exactly six subscription plans are owned by one deterministic baseline seed path; no demo users/tracks/albums/tags/notices are baseline data.
- [ ] Nine manual migration SQL files are removed only after successful fresh proof.
Performance:
- [ ] Existing payment concurrency and startup characteristics are not materially weakened.
Quality:
- [ ] Apply once to verified-empty loopback MySQL succeeds; second apply fails; information_schema manifest and constraints match.
- [ ] ddl-auto=validate startup succeeds against the rebuilt local atstudio DB.
- [ ] Billing encryption/config/provider, acceptance guard, schema contract, MySQL race, certification/download/whitelist/storage/waveform tests pass.
- [ ] Full backend test, JaCoCo report, and build pass.
- [ ] Secret scan of changed/staged files is clean.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2:
- docs/design/db-schema.md
- docs/design/payment-integration-design.md
- docs/design/api-spec.md

REQ / Decision Sources:
- deliverables/user/REQ-20260716-ATS-004.md
- deliverables/agent/WI-20260717-ATS-001-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-038-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-036-evidence-pack.md
- deliverables/agent/WI-20260717-ATS-002-evidence-pack.md

Files / Proof Assets:
- src/main/java/com/atstudio/atstudio/config/
- src/main/java/com/atstudio/atstudio/service/payment/
- src/main/java/com/atstudio/atstudio/entity/
- src/main/resources/application.yml
- src/main/resources/application-acceptance.yml
- application-local.example.yml
- application-local.yml (existence and explicit-load behavior only; never expose values)
- src/main/resources/schema.sql
- src/main/resources/seed.sql
- src/main/resources/db/manual/
- src/test/java/com/atstudio/atstudio/
- deliverables/agent/WI-20260715-ATS-007/run-package-g-mysql-proof.ps1
- deliverables/agent/WI-20260715-ATS-007/DisposableMysqlDatabaseManager.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260717-ATS-004-summary.md
Agent-facing -> deliverables/agent/WI-20260717-ATS-004-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260717-ATS-004-handoff.md

[TRACEABILITY REQUIREMENTS]
Map every edit/deletion to INT-P01..P05, INT-R06/R12, INT-V07/V08. Record preflight without secret values, DB host class/database name/emptiness, schema table/column/index/FK manifest, first/second-apply results, ddl-auto startup result, all tests, manual-SQL deletion gate, residual references, risk, and rollback. Use create-wi-evidence-pack after implementation.

---
version: 1.0.0
last_updated: 2026-10-23
project: ATS
owner: SE
category: evidence-pack
status: stable
related_wi: WI-20261023-ATS-005
---

# Evidence Pack: WI-20261023-ATS-005

## Summary

Implemented a runnable offline planner and future-approval-gated native adapter
for exactly two new TEST users, one new-user ADMIN promotion/audit and one
nonbilling one-day entitlement. Current native authorization remains absent.

## Scope / DoD Check

- [x] Exactly scoped DML; all existing rows, including every ID1 field, protected.
- [x] Existing active INDIVIDUAL plan/quota required; no automatic plan creation.
- [x] Actual retained-library BCrypt10 compatibility and72-byte bound tested.
- [x] External human/native approval bytes, candidate/target/input/plan/date
  bindings, freshness, replay claim and repeated precommit checks.
- [x] Single bounded SQL session; exact row-diff; durable commit intent;
  uncertain commit retained for read-only reconciliation, never auto-retried.
- [x] Pinned existing lifecycle reused; independently attempted cleanup.
- [x] Default CLI and synthetic planner run locally; focused tests recorded.
- [x] Exact candidate sent to WI006; no server/app/DB/payment/audio execution.

## Reference Documents (Tier 0-2)

Injected context: [generated handoff](WI-20261023-ATS-005-handoff.md), using
`create-wi-evidence-pack` after verifying that handoff exists.

| Tier | Injected pointers | Application |
| --- | --- | --- |
| 0 | `docs/standards/{core-principles,development-standards,documentation-standards,glossary}.md` | Approved local scope, immutable evidence, English documents |
| 1 | `docs/policies/{security-policy,quality-gates}.md` | Secret minimization and independent review |
| 2 | REQ007; current `docs/index.md`; WI004 summary; WI003 addendum | Current evidence and ID1 preservation |
| Evidence | `build/WI20261023-phase1-native/inventory-result.json` | Historical ID1 effective-entitlement observation |
| Implementation | Existing feature-provisioning/RuntimeFeatureProvisioner; phase1 operator; WI067/WI069 helpers | Narrow lifecycle/audit/uncertainty reuse |
| Source | schema.sql; User/UserSubscription/Subscription/AdminOperationAuditLog; AuthService; DownloadService; UserSubscriptionRepository; SecurityConfig | Exact fields, entitlement and BCrypt10 semantics |

MA supplied Tier0/security principles directly; SE performed scoped source and
evidence reads. This is not a claim of rereading the entire repository.

## Evidence Pointers

- [Exact22-file candidate](../../build/WI20261023-005-account-fixture/candidate.json)
- [Runnable CLI](../../build/WI20261023-005-account-fixture/fixture_cli.py)
- [Planner, approval and transaction](../../build/WI20261023-005-account-fixture/fixture_core.py)
- [Native lifecycle/inspection/reconciliation](../../build/WI20261023-005-account-fixture/fixture_native.py)
- [Bounded no-reconnect SQL session](../../build/WI20261023-005-account-fixture/mysql_session.py)
- [Operator contract and exact future commands](../../build/WI20261023-005-account-fixture/README.md)
- [BCrypt receipt](../../build/WI20261023-005-account-fixture/crypto-receipt.json)
- [SE test result](../../build/WI20261023-005-account-fixture/test-output.txt)
- [Independent review tests](../../build/WI20261023-006-fixture-review/)

Candidate manifest SHA-256:
`3a0f4fd30afc012a8e1a0c46d70b87ed1d2ef5492c2f8143121dc741da35654c`.
The previous `861ba343...66f3` manifest was superseded only for a README evidence
precision correction: phase1 proved no effective subscriptions, not no
historical subscription rows. The future guard deliberately HOLDs on any ID1
subscription row and never deletes one.

## Commands & Tests

`python -B -m unittest discover -s build/WI20261023-005-account-fixture -p test_fixture.py -v`
-> **12 PASS**. Covers default deny, runnable exact plan, absent/ineligible plan,
ADMIN/collision/work guards, approval binding/freshness, SQL literal safety,
preserved lifecycle flags, independent cleanup, exact reuse and actual Java
password-byte boundary. No DB/app/network started.

`python -B build/WI20261023-005-account-fixture/prepare_crypto.py`
-> Java17 `--release 17` compile and retained BCrypt10 encode/matches self-test
PASS; only synthetic values used. Class/library hashes are in the crypto
receipt. The retained application JAR is unchanged:
`f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81`.

Default CLI and offline `plan` -> preparation-only/synthetic-plan-written,
`allow_native=false`. The stale synthetic inspection cannot authorize apply.

PG identified and supplied native-pipe adversarial tests: short stderr before
ACK and nonzero exit originally failed. Merging diagnostics into the same pipe
and checking client exit/failure state fixed both; SE reran those2 PASS.
PG's later core suite including whole-snapshot4MiB limit was independently
authored and SE reran14 PASS. Final independent exact-byte allowance remains
WI006's responsibility. Historical failure evidence is retained there.

Final scoped verification: all22 candidate files match the exact manifest;
the two WI documents'12 relative links and whitespace/newlines pass. Nine
inspected application schema/entity/service/security sources match their
retained source-before hashes. This is not a new full643-source or target run.

## Risks / Rollback

Native SQL syntax/concurrency, privileges, current plan and real identities,
actual DML and restoration remain unexecuted. The candidate is locally prepared,
not target-accepted or authorized to run. Human authorization files are trusted
operator records pinned to the actual future decision; this code cannot prove
human origin from a self-authored JSON marker. MA must never fabricate them.

Possible COMMIT or lost acknowledgement requires new read-only reconciliation;
no retry, user deletion, data deletion, schema rollback or auto-extension exists.
Before/expected-after snapshots and claims remain root-private. Business rows
roll back before COMMIT, but auto-increment gaps may remain. KST midnight or a
different audio-test date requires new readiness/approval. Original service
mask/quarantine remain intact; transient lifetime bounds do not prove cleanup.

To decline: retain this isolated candidate without invoking it. No product
source/JAR, existing candidate or historical evidence was edited. No commit/push.

## Follow-up

WI005 immediately unblocks WI006 independent review, then WI007 documentation.
Any future native execution, verification flags, account data changes and later
audio/login operations require their separately explicit approval scopes.

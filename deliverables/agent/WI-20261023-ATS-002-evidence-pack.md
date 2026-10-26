---
version: 1.0.0
last_updated: 2026-10-23
project: ATS
owner: SE
category: evidence-pack
status: stable
related_wi: WI-20261023-ATS-002
---

# Evidence Pack: WI-20261023-ATS-002

## Summary

Prepared the exact DB-only account inventory and SSH stdin payload builder;
16 local focused tests pass. Native execution belongs to MA after independent
WI003 review. No remote connection, credential access, DB start or query was
performed by this agent.

## Scope / DoD Check

- [x] Existing WI067 diagnostic source helpers and bounded query reused.
- [x] Current v8r1/tree/JAR/source and historical successful control guards;
  fresh phase1 scope assertion does not reuse consumed auth launch authority.
- [x] Preliminary read-only probe separate from DB transient start.
- [x] Minimal nonsecret SELECT projection, designation separate from eligibility.
- [x] DB event scheduler/network/replica startup disabled before any SQL.
- [x] Finally runs four independent restoration checks after possible start.
- [x] Focused failure tests, executable payload builder and operator recipe.
- [x] Exact candidate supplied to WI003; MA owns remote state observations.

## Reference Documents (Tier 0-2)

Injected context came from the [handoff](WI-20261023-ATS-002-handoff.md).

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Approved scope, Korean conversation, evidence boundaries |
| 0 | `docs/standards/documentation-standards.md` | English evidence and reproducible pointers |
| 0 | `docs/standards/development-standards.md` | Minimal isolated preparation and failure verification |
| 0 | `docs/standards/glossary.md` | REQ/WI identity |
| 1 | `docs/policies/security-policy.md` | No secret/PII output or arbitrary actor assumption |
| 1 | `docs/policies/quality-gates.md` | Independent exact candidate review |
| 2 | `deliverables/user/REQ-20261011-ATS-007.md` | Active requirement and approved phase1 boundary |
| 2 | `docs/index.md` | Current decision entry, historical evidence navigation |
| 2 | `deliverables/agent/WI-20261021-ATS-009-evidence-pack.md` | Successful auth checkpoint and cleanup |
| 2 | `deliverables/user/WI-20261021-ATS-012-summary.md` | Three actors, entitlement and mutation boundaries |

Injection rules: MA supplied scoped Tier0/security principles in the generated
handoff; agent role SE operations. Applicable `create-wi-evidence-pack` skill
used after verifying the handoff exists. Current source and narrow historical
implementation evidence were inspected; no claim of rereading the full project.

## Evidence Pointers

- [Candidate manifest](../../build/WI20261023-002-account-preflight/candidate.json)
- [Operator and bounded lifecycle](../../build/WI20261023-002-account-preflight/account_operator.py)
- [Read-only projection](../../build/WI20261023-002-account-preflight/inventory.py)
- [In-memory SSH payload builder](../../build/WI20261023-002-account-preflight/make_payload.py)
- [Exact prerequisites and executable recipe](../../build/WI20261023-002-account-preflight/README.md)
- [Focused tests](../../build/WI20261023-002-account-preflight/test_preflight.py)
- [Recorded local result](../../build/WI20261023-002-account-preflight/test-output.txt)

Implementation bundle SHA-256:
`506181ad2714c6a0f5addb2fe8b3d557c6d81401b4028dc9731464afd092b6ef`.
The candidate manifest covers the four Python source/test files; documentation
is outside its execution identity. Source operator SHA
`c737d94dd8d184982167489de39b86e0c8a59a46d8c851ba6622adb7bf8b8435`,
cold helper SHA `d577629170099da3345c528e69a5fb60d7b52892dc20bf2de7b52307a86db766`,
bounded-query core SHA `84ce1f82769c5c6a70bcc3b5090a929dd21d526dc590198a3b221545af469c7e`.
Historical successful v8r1 runtime SHA
`3c1479f9cf0d55e854ae7c7f26d8b7d7c15825eedff15333c06092059f10b66d`.

## Commands & Outputs

`python -B -m unittest discover -s build/WI20261023-002-account-preflight -p test_preflight.py -v`
completed16 tests, PASS. The earlier test harness initially lacked Windows'
missing `SIGHUP` constant; the mock was corrected. No native action occurred.

The focused tests cover SQL transaction/statement restrictions, output privacy,
inclusive expiry/quota semantics, scope expiry, compiler-only payload creation,
exact source-start flags, no-start probe, success order, start/socket-timeout/
source/containment/query/parse failures, each cleanup failure, original
supervisor unit identity and empty native properties via `show --all`.

Repository observed `main`, HEAD `91a16e72693d7cd6ed04e0d36a5bd160af96f6d8`,
with existing unrelated modifications/untracked work. Owned changes are the
preflight directory and these two WI deliverables only. No commit/push.

Final scoped checks: implementation manifest exact PASS; three owned Markdown
files, ten relative links, terminal newlines and trailing whitespace PASS.
The seven inspected application/repository/entity files for account and
entitlement semantics match their hashes in the retained source-before receipt
exactly. This is a narrow source check, not a new full643-source/JAR validation.

## Risks / Rollback

Native executable/permissions/socket authentication and current account contents
are not locally proven. Missing/moved successful control evidence, non-cold
services, tool drift, pre-existing unit/log, more than100 users, or ambiguous
designation cause HOLD. Approval-only designation is distinguished from a
verified APPLIED claim/receipt; arbitrary rows remain unbound candidates.

The original MySQL service remains masked throughout. Only the owned temporary
unit can start. Python finally attempts stop/PID0/socket absence/original
baseline independently, with systemd300s as a separate process lifetime bound.
Native logs are retained, no deletion rollback. On interruption, MA confirms
actual cold restoration before another action; local SSH termination is not
proof. Declining the candidate requires no rollback: preserve the artifacts
and do not invoke them. Application sources and historical evidence unchanged.

## Follow-ups

WI-20261023-ATS-003 immediately reviews the exact executable candidate. MA then
performs only the approved phase1 probe/inventory/cleanup, and reports actor
roles/eligibility with provenance. Account creation, role changes, entitlement
grants, login, audio/app start, payments and business DML remain unauthorized.

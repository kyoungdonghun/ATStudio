---
version: 1.1
last_updated: 2026-09-09
project: ATS
owner: MA
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20260909-ATS-014-handoff.md
    reason: Approved integration scope
  - path: ../user/REQ-20260909-ATS-001.md
    reason: Approved bounded remediation
---

# Evidence Pack: WI-20260909-ATS-014

## Summary

Code remediation and final automated gates are complete on the uncommitted
`main` working tree based on `8161f0a00b088001a37962da719c3ace8fea44ad`.
Independent authentication, payment, and storage/dependency reviews returned
no open finding within their corrected boundaries. WI006-WI013 evidence is
accepted; current documentation, final document validation and preservation
checks passed. The bounded REQ is complete. This is not a production deployment
or an unrestricted security guarantee.

## Scope / DoD Check

- [x] Six P1 and six related P2 application findings addressed within existing policy.
- [x] Compatible dependency patches and a finite multipart part budget verified.
- [x] Backend build/test/coverage and frontend tests/type/lint/format/build passed without weakening gates.
- [x] Independent review counterexamples returned to their owners, corrected, and re-reviewed.
- [x] Tested backend and frontend input manifests match the source working tree.
- [x] WI013 current documentation, final validator, and final scope preservation accepted.
- [x] Parent REQ and two-set final report closed after all dependencies return.

## Reference Documents (Tier 0-2)

| Tier | Inputs | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md`, `development-standards.md`, `documentation-standards.md`, `glossary.md` | Existing policies, transaction boundaries, evidence and language rules |
| 1 | `docs/policies/security-policy.md`, `quality-gates.md` | Security boundaries and unchanged validation requirements |
| 2 | Approved REQ, WI001-WI013 handoffs/reports, historical WI018 register | Scope, implementation ownership, original findings and current evidence |

MA orchestrated the assigned pg/se/qa/docops/reviewer work. Every handoff was
created using the project handoff skill, with minimal context and no history
fork. At most three agents were open concurrently; heavy Gradle/npm runners
were serialized by MA. Specialist reviews read code and execution artifacts;
they were independent reviews, not independent reruns of every test.

## Evidence Pointers

All raw artifact names below are relative to
`output/release-remediation-20260909/`.

| Area | Evidence |
|---|---|
| Authentication and logging | [WI001](WI-20260909-ATS-001-evidence-pack.md), [WI004](WI-20260909-ATS-004-evidence-pack.md), [WI010 re-review](WI-20260909-ATS-010-evidence-pack.md) |
| Payment source/intent and lifecycle fencing | [WI002](WI-20260909-ATS-002-evidence-pack.md), [WI011 re-review](WI-20260909-ATS-011-evidence-pack.md) |
| Retained history, storage ownership and stale writes | [WI003](WI-20260909-ATS-003-evidence-pack.md), [WI012 review](WI-20260909-ATS-012-evidence-pack.md) |
| Patch baseline and multipart | [WI005](WI-20260909-ATS-005-evidence-pack.md), `built-artifact-dependencies.json`, `backend-multipart-focused-results.json` |
| Exact tested inputs | `backend-tested-source-manifest.json` (620), `frontend-final-snapshot.json` (334), `tested-source-final-check.json` (zero differences) |
| Existing artifacts | `preserved-artifacts.json` (149 SHA-256 entries), `prior-SR-93.snapshot`, `preservation-checkpoint.json` |
| Current documentation and finding dispositions | [WI013 closure matrix](WI-20260909-ATS-013-evidence-pack.md#dated-closure-matrix-2026-09-09); twelve updated current documents, without rewriting the historical WI018 register |
| Runtime boundary | `runtime-final-readonly.json`: original process starts and pinned backend JAR hash retained; local/public read-only HTTP probes 200 |
| Command exits | `command-exit-status.json`: MA tool-result transcription, not inference from quiet logs |

## Commands & Outputs

| Final check | Result | Raw artifact |
|---|---|---|
| `gradlew.bat build --no-daemon --max-workers=2 --console=plain` | PASS; 200 suites, 1,825 total, 1,806 passed, 19 skipped, zero failures/errors; 2m20s | `backend-full-final.log`, `backend-full-final-results.json` |
| JaCoCo report and verification in the build | PASS; LINE 88.5599%, METHOD 86.2736%, BRANCH 74.4033%; seven existing critical classes retain 100% line/method | `backend-coverage-final.json`, [WI006](WI-20260909-ATS-006-evidence-pack.md) |
| `npm run test:coverage` | PASS; 112 files, 1,563 tests, 79.28s | `frontend-r1-full-final-coverage.log`, `frontend-coverage-final.json` |
| Frontend coverage | Statements 90.26%, lines 92.84%, functions 91.21%, branches 82.83%; existing gates unchanged | Same final coverage artifacts |
| `npm run typecheck`, `npm run lint`, `npm run format`, `npm run build` | All exit 0 | `frontend-final-{typecheck,lint,format,build}.log`, command exit record |
| Fresh isolated `npm ci --ignore-scripts --no-fund`, followed by `npm audit --json` | Install and build usable; audit reports zero known vulnerabilities at this observation | `frontend-install.log`, `frontend-audit-final.json` |
| `python .agents/skills/validate-docs/scripts/validate_docs.py` | PASS; Tier 0, internal links, 702 traceability IDs and document index | `docs-final.log` |
| `git -c core.safecrlf=false diff --check` and preservation review | PASS; 149 prior artifacts byte-identical; all 468 prior SR-93 lines retained in order; no schema diff, deleted tracked path or staged path | `preservation-final.json`, `scope-final.json` |

Backend environment used disposable H2, synthetic JWT inputs, fake Provider
and mocked mail. The opt-in MySQL proof flags were false. Its 19 skips are
enumerated in WI006, not counted as passes. Compilation/package tasks were
incremental where reported up-to-date; this is not a clean-checkout proof.
Frontend tests used a separate 334-input copy with a fresh lockfile install,
no copied environment files, and no actual account/provider interaction.
These are Vitest/jsdom checks, not newly executed mobile/browser acceptance.

## Counterexample and Retry History

- Payment WI011 F1 exposed a stale local cancellation overwrite after upgrade
  finalization. Existing agreement/subscription lock ordering and an in-flight
  monetary-intent fence were applied; ten independent-transaction/fake-Provider
  regression cases passed. WI011 accepted the bounded correction.
- Authentication WI010 F1 exposed late password/social entrypoint responses;
  its R1 re-review then exposed a synchronous replacement login during staging
  clear notification. Five new tests failed against the preserved original
  store hash, then the corrected focused set passed all 163 cases. Logs:
  `frontend-r1-red.log`, `frontend-r1-red-snapshot.json`,
  `frontend-r1-green-focused.log`. The RED run's 57 deselected tests were a
  name-filter effect, not the full suite's environment skips.
- The first patched backend build failed in a Tomcat test fixture at compile
  time; the second had nine fixture NPE failures. Correcting the actual parser
  input interface/header setup preserved all ten scenarios, including the
  real 128/129-part boundary. Focused and final full results then passed.
- An early frontend copy accidentally omitted four coverage-test files.
  Its 108-file/1,399-test result is explicitly incomplete. Later 1,516 and
  1,558-test runs are pre-F1/pre-R1 checkpoints, not the final 1,563-test proof.
- Two new test expectations initially differed from the existing social error
  mapper text. Only the expectations were corrected; the product error-copy
  contract and the stale-session assertions were retained.

## Risks / Deployment and Rollback

- No actual payment/refund, SMTP send, OAuth account operation, real database
  DDL/data mutation, media recovery/deletion, secret read/change, server restart,
  deployment, commit, push, or branch cleanup was performed in this REQ.
- Source changes are not deployed to the pinned backend. Vite may HMR frontend
  source from this working tree, so the public demo is not proof of an entirely
  frozen frontend or of the new backend behavior. The lock-only operation changed
  the live hidden `node_modules/.package-lock.json` metadata; inspected installed
  package code was not upgraded. Fresh-install verification happened separately.
- Deploying typed access/refresh JWTs rejects old typeless sessions and requires
  login again. Do not restore the vulnerable typeless/refresh-as-Bearer fallback.
- Before deploying the source-bound payment command format, pause monetary
  admissions and inventory/drain or reconcile existing unbound unfinished
  commands. Do not mix old/new writers or invent historical source hashes.
  Existing DONE replay remains distinct from unresolved monetary recovery.
- Actual selected-target MySQL behavior, HTTPS/proxy/CORS/callback origins,
  backup/restore, media provisioning, secret rotation/access, scheduler and
  operational alert procedures retain their separate release acceptance gates.
  Previous Toss TEST/Gmail evidence is not repeated or upgraded into live proof.
- Same-process staging ownership and H2 concurrency evidence do not promise a
  multi-server storage/scheduler design. Existing single-server policy remains.
- Preserve this work as scoped reviewable patches. A blind rollback of security
  fixes or the payment format is unsafe; any rollback must consider sessions and
  in-flight orders. No retained-data rollback is required by this execution.

## Follow-ups / Chain

WI001-WI005 implementation, WI006-WI009 quality results, WI010-WI012 independent
reviews and WI013 documentation are accepted. WI014 completes the chain and
closes REQ-20260909-ATS-001 at the source-delivery boundary. Earlier packets'
pending statements are dated handoff checkpoints; this accepted integration
record and the WI013 matrix provide the later result. All assigned agents are
closed, and no test runner is left running.

The existing public backend/frontend/tunnel stay available; no automatic
commit, push or deployment was performed. Next work is an explicitly scoped
Git delivery and selected-target rollout/acceptance, including the existing
session and in-flight-order transition precautions. Another broad audit or
new product feature is not required by this handback. SR-93 remains open for
its named production gates, not for the source defects closed here.

- [Final user summary](../user/WI-20260909-ATS-014-summary.md)
- [Approved and completed bounded REQ](../user/REQ-20260909-ATS-001.md)
- [Current source correction matrix](WI-20260909-ATS-013-evidence-pack.md)

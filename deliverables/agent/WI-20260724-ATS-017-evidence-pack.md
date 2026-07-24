---
version: 1.0
last_updated: 2026-07-25
project: ATS
owner: cr
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20260724-ATS-017-handoff.md
    reason: Approved Work Item and cleanup contract
  - path: WI-20260724-ATS-013-evidence-pack.md
    reason: Disposable database and protected-state evidence
  - path: WI-20260724-ATS-025-evidence-pack.md
    reason: Corrected mail verification
  - path: WI-20260724-ATS-026-evidence-pack.md
    reason: Completed Toss test-only rehearsal and runtime ownership
---

# Evidence Pack: WI-20260724-ATS-017

## Summary

- **Final verdict: PASS**
- The exact remote-synchronized V1 rehearsal result is technically ready to
  enter client acceptance.
- Guarded cleanup removed only the owned rehearsal resources.
- Production readiness is not claimed and remains open under SR-93.

## Scope / DoD Check

- [x] Read the WI-017 handoff, all Tier 0 documents, security/quality/execution
  policies, SR-93, the payment acceptance checklist, WI-010 through WI-016,
  WI-025, and WI-026.
- [x] Tie the audit to the exact remote-synchronized commit.
- [x] Reproduce bounded corrected-code, document, runtime, database, and
  ownership checks.
- [x] Preserve minimum diagnostics before destructive cleanup.
- [x] Stop only the current WI-026 owned process trees.
- [x] Drop and prove absence of only the regex-guarded disposable database.
- [x] Remove only the six approved temporary roots.
- [x] Reconfirm listener, tunnel, database, path, Git, remote, and protected
  no-touch state after cleanup.
- [x] Run final documentation and diff gates.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and approval boundary |
| 0 | `docs/standards/development-standards.md` | Review and verification rules |
| 0 | `docs/standards/documentation-standards.md` | Evidence structure |
| 0 | `docs/standards/glossary.md` | Canonical terms |
| 1 | `docs/policies/security-policy.md` | Secret and protected-data boundary |
| 1 | `docs/policies/quality-gates.md` | Final quality gates |
| 1 | `docs/policies/execution-policy.md` | Destructive-action guard |
| 2 | `docs/SR/SR-93.md` | Production-readiness separation |
| 2 | `docs/payment/acceptance-test-checklist.md` | Acceptance matrix |
| Context | `deliverables/user/REQ-20260724-ATS-002.md` | Approved rehearsal scope |
| Context | `deliverables/agent/WI-20260724-ATS-010-evidence-pack.md` through `WI-20260724-ATS-016-evidence-pack.md` | Original rehearsal evidence |
| Context | `deliverables/agent/WI-20260724-ATS-025-evidence-pack.md` | Mail corrective evidence |
| Context | `deliverables/agent/WI-20260724-ATS-026-evidence-pack.md` | Toss corrective evidence |

## Pre-cleanup Evidence

### Repository

| Check | Result |
|---|---|
| Branch | `codex/v1-release-rehearsal-fixes` |
| Local HEAD | `f499de59817926da1eb81dcf3de783132bd0f597` |
| Remote-tracking HEAD | Same |
| Tracked/staged changes | 0 / 0 |
| Intentional untracked asset | `output/client-demo-screenshots-20260716-140514.zip` preserved |

### Independent Bounded Checks

| Check | Result |
|---|---|
| Corrective backend slice | 4 suites, 49/49 passed, 0 failed/error/skipped |
| Java compilation | PASS |
| Documentation validation | PASS |
| Broken internal links | 0 |
| Traceability IDs | 473 |
| Document index omissions | 0 |
| Local frontend/API | `200` / `200` |
| Public frontend/API through owned tunnel | `200` / `200` |

### Runtime Ownership

- The live `15173` listener was a descendant of the recorded WI-026 frontend
  root.
- The live `8080` listener was a descendant of the recorded WI-026 backend
  root.
- The live tunnel process matched the exact WI-026 tunnel ownership record.
- Tunnel origin was the loopback frontend only. Backend and MySQL had no direct
  exposure.
- Exact process and tunnel identifiers are intentionally omitted from this
  durable document.

### Disposable Database Guard

| Guard | Result |
|---|---|
| Datasource host | Loopback only |
| Database name | Exact `^ats_wi007_20260724_[a-z0-9]{8}$` match |
| Protected/system database negative check | PASS |
| Restricted bundle ACL | PASS |
| Current manifest | 39 tables, 449 columns, 153 indexes, 80 foreign keys |
| Manifest hash contract | PASS |
| Protected database selected or queried | No |

The exact disposable name and every credential remain only in the restricted
bundle and process memory.

### Protected-state No-touch Baseline

- Authoritative source:
  `deliverables/agent/WI-20260724-ATS-013-evidence-pack.md`.
- Evidence-file SHA-256 before cleanup:
  `d0317784eae05e281dadf5d8d285269f771d782b9618249db071276de975b9f4`.
- The cleanup path uses an unselected loopback administrative connection only
  to test/drop the disposable name. It does not connect to or query protected
  application data.

### Exact Approved Filesystem Targets

All targets resolved under `C:\Users\jm991\AppData\Local\ATStudio`, matched the
exact leaf name, and had zero top-level or nested reparse points:

1. `C:\Users\jm991\AppData\Local\ATStudio\release-rehearsal-3147873-20260724`
2. `C:\Users\jm991\AppData\Local\ATStudio\release-rehearsal-df35f9f-20260724`
3. `C:\Users\jm991\AppData\Local\ATStudio\release-rehearsal-runtime-3147873-20260724`
4. `C:\Users\jm991\AppData\Local\ATStudio\release-rehearsal-runtime-df35f9f-20260724`
5. `C:\Users\jm991\AppData\Local\ATStudio\wi016-mail-evidence-20260724T231700`
6. `C:\Users\jm991\AppData\Local\ATStudio\wi025-mail-evidence-20260724T234433`

Explicit exclusions:

- Repository `output/client-demo-screenshots-20260716-140514.zip`.
- Every branch and worktree.
- Historical acceptance-preview roots and the prior acceptance environment
  bundle.
- Production deployment and every protected database.

## Findings and Gate Matrix

| Severity | Pre-cleanup count | State |
|---|---:|---|
| P0 | 0 | None found |
| P1 | 0 | None found |
| P2 | 1 | React Router dependency advisory remains a production gate |
| P3 | 0 | None found |

| Item | Executed | Deferred / boundary |
|---|---|---|
| Backend full and coverage gates | WI-010/WI-020 evidence | No rerun required in this bounded final audit |
| Frontend full gates | WI-020 evidence | No rerun required in this bounded final audit |
| Disposable MySQL proof | WI-013/WI-020 evidence | Retained-data migration remains out of scope |
| Runtime API/UI | WI-024/WI-026 plus current readiness probes | Human typed confirmation remains |
| Toss test recurring payment/refund | WI-026 PASS | Live keys and real money excluded |
| Mail generation/transport | WI-025 17/17 PASS | External SMTP/real inbox remains |

The deferred human/operations gates are not product P0/P1 findings.

## Cleanup Result

- WI-026 owned frontend/backend/tunnel process trees: stopped.
- Recorded owned processes stopped: 17.
- Remaining listeners on `8080` and `15173`: 0.
- Disposable database drop: PASS.
- Independent disposable database absence count: 0.
- Protected/system-name negative guard before drop: PASS.
- Approved temporary-root deletion: 6/6.
- Approved temporary roots remaining: 0.
- Former temporary tunnel serving the application: false.
- Intentional repository demo ZIP: preserved.

The first native recursive-delete command was rejected before process creation
by the execution surface, so it deleted nothing. The first guarded clone then
encountered read-only Git pack files through the .NET deletion API. Only the
read-only attributes inside that already-approved root were cleared; deletion
then completed. The same bounded attribute handling was applied to the
remaining exact approved roots. No target list or base path changed.

The disposable database absence proof ran immediately after the guarded drop
and before destroying its restricted bundle. The bundle was then deleted with
the approved runtime root. A second database query was intentionally not made
after bundle destruction because the protected database remained a strict
no-contact boundary.

## Post-cleanup Verification

| Check | Result |
|---|---|
| Recorded frontend/backend/tunnel roots alive | 0 |
| Listeners on `8080` / `15173` | 0 |
| Former tunnel serves application | No |
| Approved temporary roots remaining | 0 |
| Disposable DB after guarded drop | Absent, count 0 |
| Local HEAD | `f499de59817926da1eb81dcf3de783132bd0f597` |
| Upstream ref | Same |
| Actual remote branch ref | Same |
| Product tracked diff | 0 |
| Staged changes | 0 |
| WI-013 protected evidence SHA-256 | Unchanged |
| Protected DB connection/query/data read during cleanup | 0 / 0 / 0 |
| Current and baseline local branches | Both preserved |
| Git worktrees | One original workspace; none deleted |
| Intentional demo ZIP | Preserved |

Repository status contains only the two permitted WI-017 documents and the
pre-existing intentional untracked demo ZIP.

## Final Severity and Readiness Decision

| Severity | Final count | Decision |
|---|---:|---|
| P0 | 0 | No client-acceptance blocker |
| P1 | 0 | No client-acceptance blocker |
| P2 | 1 | React Router dependency advisory remains a production gate |
| P3 | 0 | None found |

### Client Acceptance

- Technical readiness: `READY`.
- Final client sign-off: not claimed.
- Remaining human/operations checks:
  - External SMTP authentication and delivery to a designated real test inbox.
  - Human completion of the typed confirmation prompt in the ADMIN payment
    operations UI.

These checks are not product P0/P1 findings.

### Production

- Production readiness: `NOT READY`.
- SR-93 remains open for the dependency migration, production data strategy,
  live-key/real-money rehearsal, deployed HTTPS/CORS/secrets, backup/restore,
  monitoring, and explicit release approval.

## Final Quality Gates

| Gate | Result |
|---|---|
| Tier 0 documents | 4/4 PASS |
| Internal links | 0 broken |
| Traceability IDs | 473 supported matches |
| Document index omissions | 0 |
| Tracked `git diff --check` | PASS |
| WI-017 Evidence Pack whitespace check | PASS |
| WI-017 user summary whitespace check | PASS |

No file was staged, committed, or pushed.

## Risks / Rollback

- Abort without deletion if any ownership, database, or resolved-path guard
  differs from the checkpoint.
- Product source/configuration, Git history, branches, and the intentional
  demo ZIP are outside the cleanup scope.
- Temporary roots are reproducible from the committed branch and WI contracts;
  their durable, sanitized results remain in this Evidence Pack and its
  dependencies.

## Related Documents

- [WI-017 Handoff](WI-20260724-ATS-017-handoff.md)
- [WI-017 User Summary](../user/WI-20260724-ATS-017-summary.md)
- [WI-026 Evidence Pack](WI-20260724-ATS-026-evidence-pack.md)
- [Payment Production Readiness](../../docs/SR/SR-93.md)

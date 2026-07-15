---
version: 1.1
last_updated: 2026-07-15
project: ATS
owner: docops
category: audit
status: stable
dependencies:
  - path: full-system-audit-20260713.md
    reason: Historical source findings
  - path: ../design/p0-release-blocker-remediation-design.md
    reason: Approved remediation contract
  - path: ../../deliverables/agent/WI-20260713-ATS-009-evidence-pack.md
    reason: Security review evidence
  - path: ../../deliverables/agent/WI-20260713-ATS-010-evidence-pack.md
    reason: Payment transaction review evidence
  - path: ../../deliverables/agent/WI-20260713-ATS-011-evidence-pack.md
    reason: Cross-layer verification evidence
  - path: ../../deliverables/user/REQ-20260715-ATS-001.md
    reason: Superseding public full-track listening decision
  - path: ../../deliverables/agent/WI-20260715-ATS-018-evidence-pack.md
    reason: Full-resource stream implementation and focused regression evidence
---

# P0 Release Blocker Closure Report - 2026-07-13

> Purpose: Record the implementation and verification evidence for the three P0 findings from the 2026-07-13 full-system audit without rewriting the historical findings.

> **Supersession note (2026-07-15):** This report remains the historical closure evidence for the 2026-07-13 P0 remediation. REQ-20260715-ATS-001 and WI-20260715-ATS-018 supersede only its bounded listening-length and dedicated-preview selection decision: public listening now serves the complete active Track through the controller, with Range requests resolved against the full resource length. Public DTO storage-key redaction, direct static-original denial, and protected official download/License entitlement remain in force.

## Closure Decision

| Item | Decision |
|---|---|
| P0 remediation | **CLOSED in implementation commit `d11c62d` under REQ-20260713-ATS-001** |
| Historical audit | Preserved as the 2026-07-13 finding snapshot |
| Overall release | Remains **NO-GO** until the separate P1, deployment, migration, and quality gates are closed |
| External verification | No live Toss, SMTP, production DB, or stored-file migration was executed |

At the time of this closure, the original-media physical paths remained unchanged and the approved immediate boundary used deny-all static routing plus controller-mediated bounded listening and entitled download. The 2026-07-15 supersession note above replaces only the listening-length decision. A physical storage migration remains separate destructive work.

## Finding Closure Matrix

| Finding | Implemented behavior | Source pointers | Evidence | Focused tests | Commit state |
|---|---|---|---|---|---|
| `ATS020-P0-01` original Track exposure | 2026-07-13 closure state: public `audioFile=null`; admin DTO retains key; static original route denied; valid dedicated preview or bounded original prefix only; entitled download unchanged. The listening-length portion is superseded by REQ-20260715-ATS-001 and WI-20260715-ATS-018. | `TrackResponse`, `TrackService`, `TrackController`, `SecurityConfig`, frontend Track types | WI-003, WI-006, WI-009, WI-011 | `TrackServiceTest`, `TrackControllerTest`, `SecurityFilterChainTest`, `DownloadServiceTest` | Contract commit `80acc3b`; implementation commit `d11c62d` |
| `ATS020-P0-02` mail secret logging | One random `deliveryId`; success logs ID/outcome; failure adds exception class only; no recipient/body/URL/token/raw message/stack trace | `EmailService` | WI-004, WI-007, WI-009, WI-011 | `EmailServiceTest` captured-output success/failure cases | Contract commit `80acc3b`; implementation commit `d11c62d` |
| `ATS020-P0-03` withdrawal renewal risk | Password-first local cancellation; ID-only after-commit cleanup; agreement-scoped Incident; 01:15 retry; already-removed convergence; deleted-user renewal guards; no auto-refund | `UserService`, withdrawal cleanup services/event, billing repository, renewal and Incident services | WI-005, WI-008, WI-010, WI-011 | `UserServiceTest`, cleanup service/coordinator tests, repository, renewal, and Incident tests | Contract commit `80acc3b`; implementation commit `d11c62d` |

The implementation and WI-003 through WI-011 evidence are committed in `d11c62d`. WI-012 documentation and final quality evidence are committed separately so that the source boundary remains auditable.

## Verified 2026-07-13 Contracts (Historical Snapshot)

### Protected Media

The bullets below are the verified 2026-07-13 snapshot, not the current listening-length contract. The report-level supersession note governs current public playback.

- Public Track detail retains a nullable field but never returns the original storage key.
- A dedicated preview is trusted only after normalized `tracks/preview/` validation and original-key inequality.
- Original fallback public length is the smaller of 30 seconds and 50% of duration by byte ratio; unknown duration uses 25%.
- Multi-byte originals retain at least one private byte; a one-byte original exposes zero bytes.
- Range parsing is single-range only and returns `416` for malformed, multiple, zero-length, or out-of-bound requests.

### Mail Logging

- Delivery correlation metadata is retained without payload reconstruction from logs.
- SMTP exceptions remain absorbed so password-reset behavior does not reveal account existence or delivery outcome.

### Withdrawal Billing Cleanup

- Local charge eligibility ends before user deletion and does not depend on Provider cleanup success.
- Cleanup failure retains encrypted key material and records one deduplicated `WARNING` Incident per agreement.
- Retry selects only deleted/CANCELLED/key-retaining agreements; success or `ALREADY_REMOVED_BILLING_KEY` clears key material and resolves the Incident.
- Withdrawal does not create a refund or entitlement correction.

## Test Evidence

- WI-009 combined focused run: 11 suites, 133 tests, 0 failures, 0 errors, 0 skipped.
- WI-010 transaction review and WI-011 cross-layer matrix accepted the same merged source/test state.
- WI-013 forced full backend run: 102 suites, 786 tests, 0 failures, 0 errors, 0 skipped.
- WI-014: Java main/test compilation, frontend typecheck, ESLint with zero warnings, 14 Vitest files / 51 tests, and scoped Track API Prettier check all passed.
- WI-015: Gradle package build and frontend Vite production build both exited 0; generated `tsconfig.tsbuildinfo` was restored to its tracked baseline.
- WI-016: documentation validator, direct-file count contract, date/stale-claim scans, and `git diff --check` all passed.
- Provider behavior used test doubles, mail used mocked `JavaMailSender`, and repository tests used H2.

## Documentation and Validation

- Current-state API, DB, use case, security, payment, SR-93, and client acceptance documents were aligned by WI-012.
- Documentation validator: Tier 0 present, no broken links, 314 supported traceability IDs, and all documents indexed.
- Direct-file count contract: Standards 12, Audit 4, total 187.
- Diff check: exit 0 with no whitespace errors.

## Residual Boundaries

- At closure time, dedicated low-quality preview generation was not implemented. It is not a current requirement after REQ-20260715-ATS-001 restored full-track listening.
- Existing original files have not been physically migrated outside the storage root.
- Withdrawal cleanup retry assumes one scheduler owner.
- Live Provider/SMTP/MySQL behavior and broader non-P0 release gates remain unproven here.

## Rollback

For the WI-20260715-ATS-021 annotation only, revert this report's metadata, dependencies, supersession note, historical labels, and residual-boundary clarification. Do not rewrite the preserved 2026-07-13 evidence, change schema structure/data, or revert source, test, runtime, or unrelated shared-worktree edits.

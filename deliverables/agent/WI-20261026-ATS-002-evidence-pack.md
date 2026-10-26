---
version: 1.0
last_updated: 2026-10-26
project: ATS
owner: PG
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261026-ATS-002-handoff.md
    reason: Scope and traceability
  - path: ../user/REQ-20261024-ATS-001.md
    reason: Approved local preparation continuation
---
# Evidence Pack: WI-20261026-ATS-002

## Summary
Source-backed privacy flow inventory and an ordered, owner-specific remediation/verification plan prepared locally while client decisions and provider signup are pending.

## Scope / DoD Check
- [x] Actual required/conditional/optional inputs separated from entity capability and runtime activation.
- [x] Persistence, transient/provider/browser processing, and withdrawal handling mapped with source lines.
- [x] Reused WI010 redacted HWP review; no original personal data copied.
- [x] Developer facts, client policy choices and contract/legal unknowns separated; no retention durations invented.
- [x] Eight focused future verification scenarios prepared without runtime execution or test implementation.
- [x] Toss-maintained decision recorded; NCP source preparation not treated as deployment.

## Reference Documents (Tier 0–2)
| Tier | Document | Injection/use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | MA-injected scoped, sustainable, evidence-led work; current AGENTS stage-two approval rule governs continuation. |
| 0 | `docs/standards/development-standards.md` | MA-injected minimum scoped changes and reproducible evidence. |
| 0 | `docs/standards/documentation-standards.md` | MA-injected metadata/dependencies and English internal/Korean user outputs. |
| 0 | `docs/standards/glossary.md` | MA-injected canonical WI/REQ terms. |
| 1 | `AGENTS.md`, `docs/policies/security-policy.md` | Explicit local-only effect boundary and no secrets/PII in outputs. |
| 2 | Current Decision Entry, approved REQ, WI010 summary, WI002 handoff | Current facts, prior review and owned output scope. |

Injection rules: `.claude/config/context-injection-rules.json`; role PG, task privacy/source review; relevant Tier 0–1 principles injected by MA. Evidence Pack skill read and its existing-handoff gate passed.

## Evidence Pointers
- `build/WI-20261026-ATS-002/privacy-data-map.md`: 14 flow rows and exact withdrawal sequence.
- `build/WI-20261026-ATS-002/remediation-and-verification.md`: six ordered work steps, client/provider dependencies and eight future scenarios.
- `build/WI-20261026-ATS-002/source-manifest.json`: identities of selected source files, no values from runtime records.
- `deliverables/user/WI-20261026-ATS-002-summary.md`: concise Korean outcome.

Key independently inspectable locations (backend under `src/main/java/com/atstudio/atstudio/`):
- `service/UserService.java:155`, `entity/User.java:83`: guards, billing cancellation/event, selected relation cleanup versus retained profile.
- `service/UserService.java:439`, `:458`; `entity/UserConsent.java:28`: type-specific required inputs and immutable acceptance-only consent record.
- `service/EmailService.java:49`, `:91`, `:146`: raw random tokens, validity limits, reissue deletion/used markers; no expiry purge in inspected references.
- `service/CompanyCertificationService.java:72`, `:130`, `:217`: private file metadata, after-commit replacement cleanup and access audit.
- `service/WithdrawalBillingCleanupCoordinator.java:19`: provider cleanup after commit, separate scheduled retry at line 31.
- `repository/WhitelistChannelRepository.java:101`; `service/AdminWhitelistChannelService.java:148`: status-dependent removal requests and export copies.
- `service/payment/provider/recurring/TossBillingProvider.java:259`, `:577`: outgoing fields and selected/masked response payload, not raw response blanket storage.
- `frontend/src/store/authStore.ts:200`, `frontend/src/store/playerStore.ts:151`, `:236`: auth-key cleanup, separate browser history and track-ID hydration.

## Commands & Outputs
- Read-only `Get-Content` and scoped `rg -n`/`rg --files` against source and redacted reports.
- One attempted shell brace-list query was rejected by PowerShell before execution; replaced with an explicit file list. A few guessed filenames were absent; exact paths were resolved with `rg --files`. No system state was changed by these checks.
- Owned artifact link/path, metadata, newline and whitespace checks are recorded in `build/WI-20261026-ATS-002/local-document-check.json`.
- Source identity manifest produced with `Get-FileHash` on an explicit public-source allowlist only.

## Tests
No application test, server/DB/app/provider call, actual mail, payment, backup or restore was run. This task changes documentation only. The eight cases are future acceptance criteria, not test PASS claims.

## Risks / Rollback
- Source behavior is not a current production record inventory. Absence claims are bounded to inspected paths/search references; effective host logging/provider settings remain unverified.
- A broad immediate deletion could erase proposed rights evidence or violate a retention duty. A broad retention decision would overcollect. Neither is applied.
- New report files can be superseded without changing source/data. Historical WI010/HWP/PDF evidence remains untouched; no destructive rollback is required.

## Follow-ups
WI-20261026-ATS-003 is blocked by this WI and WI001. Trigger integration/independent high-risk cross-check immediately when both outputs are ready. Broader stage-two REQ remains open; actual policy remediation requires the stated decision and execution scope.

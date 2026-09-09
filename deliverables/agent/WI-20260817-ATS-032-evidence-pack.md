
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A061: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a061). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# Evidence Pack: WI-20260817-ATS-032

## Summary

- Corrective evidence record for the completed local isolated rehearsal observations supplied after WI-030 remained partial and WI-031 was interrupted before creating its own deliverables.
- This WI records browser, API, configuration, and persisted-state evidence separately. It does not authorize or claim an external-effect rehearsal or a release decision.

## Scope / DoD Check

- [x] Separates the passed local isolated rehearsal from all unexecuted external-effect scope.
- [x] Records the local browser CORS 403, direct-API precondition, and disposable-runner-only remediation without claiming a repository change.
- [x] Records public catalogue/player, administrator access, subscriber download, and persisted download-history observations.
- [x] Retains WI-030 as PARTIAL and WI-031 as INTERRUPTED; this WI is a corrective evidence record, not a replacement execution claim.
- [x] Creates the required user summary and agent evidence pack.
- [x] `git diff --check` passed after both deliverables were created; it produced no output and exited successfully.

## Reference Documents

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approved-WI execution boundary and transparent evidence requirement |
| 0 | `docs/standards/development-standards.md` | Two-set deliverables and evidence-pointer-first requirement |
| 0 | `docs/standards/documentation-standards.md` | Deliverable structure and cross-document reference rules |
| 0 | `docs/standards/glossary.md` | Canonical terminology check |
| 1 | `docs/policies/quality-gates.md` | Quality-gate and verification-evidence boundary |
| 1 | `docs/policies/security-policy.md` | Secret-safe, local-only, and external-service boundaries |
| REQ | `deliverables/user/REQ-20260817-ATS-010.md` | Approval gates for external effects and destructive operations |
| Context | `deliverables/user/WI-20260817-ATS-029-summary.md` | Earlier blocked non-mutating live-boot verification |
| Context | `deliverables/agent/WI-20260817-ATS-029-evidence-pack.md` | Earlier test and runtime-boundary evidence |
| Context | `deliverables/user/WI-20260817-ATS-030-summary.md` | Partial isolated rehearsal status and previous browser block |
| Context | `deliverables/agent/WI-20260817-ATS-030-evidence-pack.md` | Isolated runtime/API and non-execution boundary |
| Handoff | `deliverables/agent/WI-20260817-ATS-031-handoff.md` | Interrupted fixture/runtime scope |
| Handoff | `deliverables/agent/WI-20260817-ATS-032-handoff.md` | Corrective-evidence scope, supplied observations, and output contract |

## Evidence Pointers

- `deliverables/agent/WI-20260817-ATS-032-handoff.md`, "Verified observations supplied by MA": authoritative record of the completed local browser/API/runtime observations for this corrective WI.
- `%LOCALAPPDATA%\ATStudio\release-rehearsal-WI-20260817-ATS-031\backend.log`: read-only runtime pointer. Secret-safe marker inspection confirmed application-start, Hibernate-validation, and CORS markers; raw content is intentionally not reproduced.
- `%LOCALAPPDATA%\ATStudio\release-rehearsal-WI-20260817-ATS-031\rehearsal-static-proxy.mjs`: read-only disposable-runner pointer. Secret-safe marker inspection confirmed loopback routing material; raw configuration is intentionally not reproduced.
- `deliverables/user/WI-20260817-ATS-030-summary.md`: WI-030's prior result was PARTIAL because browser DOM checks and playable-track evidence were not complete in that run.
- `deliverables/agent/WI-20260817-ATS-031-handoff.md`: WI-031 was intended to provide the missing scoped demo fixture/runtime for browser verification, but has no matching summary or evidence pack.

## Reproduction Record

The following records the already-completed isolated rehearsal; this WI did not start, stop, or alter any runtime.

1. Start the fresh schema-and-seed rehearsal DB and isolated backend with Hibernate `ddl-auto=validate`; retain only the scoped demo catalogue fixture (10 tracks, one 10-track album, and category tags).
2. Through the loopback static proxy, inspect public catalogue, tag filtering, track detail playback, album full playback, waveform/progress, and browser console state.
3. Before CORS remediation, reproduce browser password-login HTTP 403. Separately verify direct backend login and `/api/users/me` as the API precondition; do not print credentials or tokens.
4. Expand only the disposable runner CORS allowlist to include the loopback browser origin. Do not edit repository source or tracked application configuration.
5. Use the QA administrator browser session to load `/admin/dashboard` and `/admin/payments`; record console state only. Do not submit payment/refund/provider actions.
6. Use the QA subscriber browser session to download one demo track and inspect `/downloads` for the persisted history item and daily count. Record the browser download-event capture timeout as a limitation.

## Evidence by Layer

| Layer | Verified evidence | Explicit limitation / no claim |
|---|---|---|
| Browser UI | Public catalogue, tag filter, track detail play, album full play, waveform/progress, administrator pages, and subscriber download flow were observed with no browser console error in the stated checks. | No Cloudflare/public-tunnel, production, or browser download-event API success claim. The download-event capture timed out. |
| API | Direct backend password login and `/api/users/me` succeeded before the CORS remediation, isolating the initial failure to the browser-origin boundary. | No credential, token, raw response, payment, refund, or provider API result is recorded. |
| Configuration | The initial HTTP 403 was caused by the missing loopback browser origin in the disposable runner CORS allowlist. Adding that origin to the disposable artifact enabled browser login. | No source-code or tracked configuration patch; no claim about client acceptance or deployed CORS configuration. |
| Persistence / runtime | Fresh rehearsal schema-and-seed data booted with Hibernate validation. One subscriber download produced one persisted `/downloads` history item and daily count `1 / 20`. | The history item is persisted-flow evidence, not browser download-event capture evidence. No backup/restore, scheduler, or production persistence claim. |
| External services | SMTP and provider endpoints were configured loopback fail-closed for the rehearsal. | No Gmail, SMTP delivery, Toss/provider, payment, refund, Cloudflare, backup/restore, or production action was executed. |

## Commands and Results

| Command / operation | Result |
|---|---|
| Secret-safe marker inspection of the approved runtime root | Runtime root, backend log, and disposable proxy script were present. Application-start, Hibernate-validation, CORS, and loopback-routing markers were present; raw contents were withheld. |
| `git diff --check` | PASS: executed after the two WI deliverables were created; no output and successful exit. |

## Limitations and Remaining Gates

- WI-030 remains PARTIAL. Its original isolated run lacked browser DOM and playable-track evidence; this WI records the subsequent supplied local observations and does not revise WI-030's historical result.
- WI-031 remains INTERRUPTED. It did not create its required user summary or agent evidence pack; this WI supplies only the corrective record required by its successor handoff.
- This local evidence does not prove external provider delivery/response, SMTP delivery, payment/refund behavior, Cloudflare tunnel behavior, backup/restore correctness, scheduler safety, or production readiness.
- WI-023 requires an explicit external-effect action plan and approval immediately before execution. WI-024 requires an exact database scope approval. WI-025 depends on WI-023 and WI-024; WI-026 depends on WI-025.

## Rollback / Cleanup

- No tracked product source, tracked application configuration, database, branch, service, or runtime script was changed by this WI.
- No runtime was started or stopped by this WI. The disposable rehearsal artifact is outside the repository and must be cleaned up separately without touching the client acceptance runtime.
- Do not delete a rehearsal DB, storage root, branch, or client-acceptance artifact without a new exact-scope approval.

## Changed Files

- `deliverables/user/WI-20260817-ATS-032-summary.md`
- `deliverables/agent/WI-20260817-ATS-032-evidence-pack.md`

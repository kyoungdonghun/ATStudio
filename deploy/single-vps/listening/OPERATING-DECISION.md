---
version: 1.2
last_updated: 2026-11-06
project: ATS
owner: EO
category: guide
status: local-implemented-native-pending
dependencies:
  - path: ../../../docs/registry/git-checkpoint-20261106.md
    reason: Canonical A/B/C continuity and recovery requirements
  - path: ../backup/PHASE-B.md
    reason: Existing trial wrapper stops its owned services
  - path: PUBLICATION-EVIDENCE.template.md
    reason: Actual publication evidence contract
---
# Listening operation decision and remote handoff

The user decided on 2026-11-05 that additions and edits are required after opening. This operating need is settled; do not ask it again. No server, catalog row or public-access setting has been changed. The current DATABASE candidate supports explicit revisioned publication without a restart. STATIC mode retains its startup-bound selection. See [WI007 completion: retained local evidence](../../../docs/registry/git-checkpoint-20261106.md#retained-evidence-limits); actual mode transition/bootstrap remains separately authorized.

## Selected need and local implementation

[REQ-20261105-ATS-002: retained local evidence](../../../docs/registry/git-checkpoint-20261106.md#retained-evidence-limits) defines the proposed minimal follow-up: a reserved existing database setting row with immutable catalog revisions, a dedicated source-plus-ADMIN publication API, and explicit selection independent of isActive. It compares alternatives and identifies exact code, concurrency, generic-setting bypass, audit, cache and recovery tests. No new schema/provider is presumed. REQ003 authorized implementation, completed under WI007–012. The plan remains historical design evidence; final implementation and independent corrections are linked above.

Selected tracks must be unpublished before public-content editing and explicitly reapproved after validation. Startup mode transition/bootstrap, real selected IDs, permitted writes and restart/rollback remain separately approved effects. An already admitted stream or saved client copy cannot be recalled by unpublishing.

For backup, reuse the existing online FREEZE/dump/SEAL/pin/verify/RELEASE protocol through the locally implemented bounded adapter that attaches to, but never owns or starts/stops, the approved public app/DB. Upload/edit/publication writes may temporarily be fenced; playback may experience resource contention. HOLD is reconciled from evidence, not automatically cleared. The adapter outer HOLD recovery is not implemented; inner reconciliation cannot release its outer lock/claim. Exact recovery authority and disposition are a native operating gate. A current-catalog isolated restore remains distinct from capture success. Native compatibility is not yet proven.

Full recurring automation, mail reporting and timer activation are later follow-ups. Before public use, approve actual recovery objectives, mutation/freeze windows, cost/capacity bounds and incident ownership. The decision to support ongoing changes does not itself authorize downtime, provider calls or actual data changes.

## Existing wrapper compatibility

The existing `phase_b_trial.py` / PHASE-B procedure is a bounded trial, not an always-on maintenance runner: its capture path attempts owned app/DB/helper stop on success and failure; startup has deadline/RuntimeMax limits; isolated restore occurs after source stop. Do not use that wrapper as-is against an always-on public site. Preserving the approved public app/DB/HTTPS and stopping only authorized temporary helper/access is a future operating requirement, not a property proven here. No historical capture or failed/HOLD claim is rewritten or replayed.

## Required concrete remote approval payload

Before proposing execution, record the exact target, current baseline and release/configuration hashes; selected actual tracks and rights evidence; operator source restrictions and private references (never secret bodies); public period; domain/certificate/renewal owner; CPU/RAM/disk/inode headroom; original/derivative/temp/DB/backup/restore byte budget; expected transfer and finite request/concurrency/egress/cost limits; explicit stop/recovery thresholds; named operation/incident owner; and current recovery evidence covering the selected catalog and recoverable settings/keys.

Bind actual app/DB ownership and startup/cleanup semantics, effective settings (including scheduled/audio workers), each approved data mutation, access before/after, and successful-service retention. Confirm exact operator routes need source restriction plus ADMIN authorization and cannot broaden account/payment access. Empty or example catalog/address settings are not actual approval.

## Bounded future acceptance order

1. Read-only reconcile target identity, current state, available capacity, latest compatible recovery and exact authorization. Do not infer runtime settings from tracked defaults.
2. Stage the separately approved pinned candidate and configuration. Keep rollback artifacts; do not replace historical evidence. Verify internal ports remain private and HTTPS access is limited to required surfaces.
3. Test anonymous selected list/detail/batch/full stream/Range/seek and consistent metadata; deny nonselected direct IDs, all blocked methods and normalized/encoded path variants, raw audio, Swagger/internal surfaces; verify stale-login visitors have no account side effects. Verify operator source restriction plus ADMIN and upload/edit flow only within approved real changes.
4. Obtain actual PC and physical mobile direct-link/reload/full-listening results and complete the evidence record with observed public timestamps and hashes.
5. Confirm recovery/continuity for the selected ongoing-change operation. On success retain approved app/DB/HTTPS for the approved period and clean only authorized temporary access. On failure use the last approved safe state and exact rollback; preserve failure/HOLD and unknown outcomes without automatic retry.

A remains open until these real conditions are accepted. Institution acceptance is separate. The canonical roadmap retains B complete client acceptance including LIVE PG and C final launch/maintenance approval, plus the five operational-safety follow-ups. This local handoff authorizes none of them.

## 2026-11-06 concrete follow-up design

[REQ-20261106-ATS-001: retained local evidence](../../../docs/registry/git-checkpoint-20261106.md#retained-evidence-limits)
now proposes exact read-only preflight, recovery/cost evidence, private DB bootstrap and
deployment, one capture, isolated restore and publication approval payloads. The
[outer HOLD contract](../backup/OUTER-HOLD-RECOVERY.md) passed independent local design
review; recovery
implementation and native acceptance remain pending. It preserves all old claims/locks
and rejects uncertain transfer, unsupported legacy evidence and cross-boot inner recovery.

Existing `online_restore.py` requires stopped source app/DB and no Java/MySQL/FFmpeg
process. The minimum proposal therefore places an explicitly approved stop/restore/restart
window **before** public opening. The live capture adapter still owns no source lifecycle.
An already public source requires an approved maintenance window or a separately prepared
compatible target; this design does not prove uninterrupted operation or online restore.

## Public checkpoint evidence boundary

This public copy redirects unavailable local evidence links to the shared
checkpoint boundary. The original document bytes and detailed REQ/WI evidence
remain in private local custody. No runtime contract or source code changed.

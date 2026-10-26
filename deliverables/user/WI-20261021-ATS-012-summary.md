---
version: 1.0.0
last_updated: 2026-10-23
project: ATS
owner: DocOps
category: work-summary
status: stable
related_wi: WI-20261021-ATS-012
dependencies:
  - path: ../agent/WI-20261021-ATS-012-handoff.md
    reason: Local-only approval and evidence integration boundary
---

# WI-20261021-ATS-012 Local Audio Preparation Summary

> Purpose: State the local preparation boundary and concrete account/data proposals before any server resumption.

**Local API preparation reviewed on 2026-10-23: LOCAL_ALLOW_API_PREPARATION_ONLY.
Server access, restart, account changes and data changes still need separate
approval. Native audio, human playback and browser UI acceptance remain NOT_RUN.**

## Proposed Account And Data Changes — Not Applied

| Object | Proposed bounded change and prerequisite | Retention and approval boundary |
| --- | --- | --- |
| Upload actor | Reuse the previously designated first-admin identity; do not ask to select it again. Its current role has not been reverified. Confirm actual role only after server-access approval; if it is not ADMIN, propose exact promotion before changing it. | No automatic role change, new administrator, password/reset or consent action. Retain the original role observation and approve any later restoration separately. |
| Negative actor | A distinct USER must lack both a license for the new test track and an effective entitlement. ACTIVE **or CANCELLED** with `expiresAt >= today` permits use; CANCELLED alone is not a negative fixture. | Prefer an existing designated test USER if suitable. Any account creation, verification mail or data change requires a concrete approval first. Credentials and real identifiers stay out of these documents. |
| Positive buyer | Use a third distinct actor: a USER with an effective entitlement and available daily quota for a first download, separately from ADMIN bypass. Prefer a suitable existing test USER. The current engine uses ADMIN + ENTITLED_USER + NO_ENTITLEMENT with three distinct tokens; anonymous is not an account. | A new entitlement is not currently supplied by the existing correction API: that API requires an existing user-subscription row. The normal creation path is payment-linked; it must not be invoked for this no-payment trial. If no eligible fixture exists, a bounded non-payment user-subscription fixture needs explicit design and approval. A two-account sequence with a mid-trial grant is not supported by this engine. |
| Track | Upload exactly the already approved synthetic WAV as one trial-owned track. Bound metadata; omit optional thumbnail/tags unless required by the final engine. Conversion writes the stream MP3 and updates processing status, duration and waveform. READY alone is not activation: an explicit activation PUT is needed for public stream and official download paths. | Approve the exact track creation/activation scope before execution. Preserve the original WAV, derived media and evidence by default. No additional track or replacement upload on automatic retry. |
| Login sessions | Supply the three existing actors' credentials once before the future bounded window, through its private input contract. The flow performs login for each actor, then ME for each actor, before the audio baseline in that same window. Login changes refresh-token hash and potentially `updated_at` for exactly those three user rows. The current audio engine does not implement logout; historical authentication logout success does not change this trial contract. | Approve these expected mutations for the designated actors. No real credentials are collected or used by this local preparation. No token/hash values in receipts, no password change, and no automatic auth-flow replay. |
| First official download | Exactly one entitled USER first download creates one `licenses` row, one `track_downloads` row and increments `download_count` by one. ADMIN is the upload actor and does not perform an official download in this engine. | Preserve licenses and download history. Re-download has a different path and cannot prove a first-download entitlement check. The final ledger requires exactly these single-download deltas; no additional authorized download is allowed by this plan. |
| Storage evidence | Upload/conversion creates the original WAV and derived MP3 plus four `storage_mutations` journal rows (two CREATE and two DELETE; DELETE retains the shared source). Worker temporary files are separately observed. | Track exact owned paths and journal deltas. Preserve committed records/files; distinguish worker-owned temporary cleanup from destructive cleanup of retained evidence. No blanket directory or DB cleanup. |
| End of trial | Stop the bounded app/DB trial and restore the original restricted HTTPS configuration using the retained successful cleanup design after separately approved execution. | Service cleanup is not data deletion. Recommend preserving the test records and optionally deactivating the exact test track only after that change is approved. Do not use DELETE-track as a harmless toggle: its implementation also removes relations. |

Public stream access is intentional product behavior. Test unauthenticated
official download and direct original-file access as separate denials; do not
classify a permitted public stream as a security failure. Authorized original
download must match the approved WAV hash.

## Current Local Integration Checkpoint

The [fresh PG receipt](../../build/WI011-audio-review/review-receipt-20261023.json)
binds WI013 manifest `3c837f740160d6a54fb3093d75e2148101efe9f67c11b39b615f3086598b48f2`
(62 members). Fresh injected local tests: 26+4+2 PASS. October21 component/reviewer
50+8 PASS remain historical, not rerun. The previous manifest review does not
approve changed bytes. Source643, retained JAR, approved WAV and copied tools
match; no rebuild or remote contact occurred. MA observed local SSH agent
Stopped/Disabled on October23; server state is only the earlier WI009 evidence.

WI010 + WI013 -> WI011 -> WI012 is complete for local API preparation only.
The API contract uses loopback and retains external HTTPS quarantine. HTTP/Range
assertions do not establish audible playback or browser UI. The [candidate
README](../../build/WI013-audio-native/README.md) documents a future exact-file
copy recipe: identify the single trial derivative from private binding/file
receipts, approve that MP3 transfer, verify size/SHA locally, listen to all ten
seconds and seek in an existing local player. Transfer is not implemented or
executed by this candidate. This would prove conversion plus local listening,
not the application's browser player. Stream response-body SHA is not directly
linked to derivative-file SHA in the current API checks.

Next gate: separate explicit server/data approval and target/actor prerequisite
checks. Missing actors or entitlement cause HOLD before mutation. Real Linux
compatibility, upload/conversion, rights and actual cleanup remain unverified.
No human playback request should precede its technical access preparation.

## Source Pointers And Verification Boundary

- [DownloadService](../../src/main/java/com/atstudio/atstudio/service/DownloadService.java): first-download entitlement/quota, ADMIN bypass, license/event/counter, re-download.
- [UserSubscriptionRepository](../../src/main/java/com/atstudio/atstudio/repository/UserSubscriptionRepository.java): ACTIVE/CANCELLED and inclusive expiry condition.
- [TrackService](../../src/main/java/com/atstudio/atstudio/service/TrackService.java): creation, WAV queueing, activation readiness and DELETE side effects.
- [AuthService](../../src/main/java/com/atstudio/atstudio/service/auth/AuthService.java): refresh-token hash update and logout clear.
- [AdminSubscriptionCorrectionService](../../src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java): existing-row lock prerequisite.
- [PaymentCommandTransactionService](../../src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java): payment-linked user-subscription creation; reference only, execution prohibited.

These are local source observations on 2026-10-21 by DocOps, not runtime role,
entitlement, server-state or successful audio-trial evidence. The October23 receipt accepts only the current local API preparation. REQ007 remains active.

---
version: 1.0.0
last_updated: 2026-10-24
project: ATS
owner: DocOps
category: evidence-pack
status: stable
related_wi: WI-20261024-ATS-004
dependencies:
  - path: WI-20261024-ATS-004-handoff.md
    reason: Local-only reuse audit and exact approval proposal
---

# Evidence Pack: WI-20261024-ATS-004

## Summary

Completed the approved local-only reuse audit and draft native audio scope.
Local identity reuse PASS does not establish operator/input readiness or native
allowance. Actual audio, target acceptance and human playback remain NOT_RUN.
WI004 is complete; REQ007 stays active and no native/implementation chain is
authorized automatically.

## Scope / DoD Check

- [x] Existing skill-generated handoff and injected Tier0 context applied.
- [x] Reused exact candidate/review and MA fresh local identity results.
- [x] Specified actors, login session exception and exact data/file effects.
- [x] Separated loopback byte checks, private MP3 transfer and human/browser UX.
- [x] Recorded missing caller/input/receipt bindings and finite lifecycle gates.
- [x] Added Korean draft approval, narrow current entry and own two-set outputs.
- [x] Focused new-scope documentation validation recorded.

## Reference Documents (Tier 0-2)

The [WI004 handoff](WI-20261024-ATS-004-handoff.md) injects Tier0 core principles,
documentation/development standards and glossary; Tier1 security/quality policies;
Tier2 current index/REQ007, completed WI003 and the audio contracts. Applied
create-wi-evidence-pack and validate-docs. Assignee DocOps; English technical
documents/Korean REQ; MA alone owns operational actions.

## Evidence Pointers and Reuse Results

- [Hash revision README](../../build/WI20261023-001-audio-hash/README.md) and
  [inherited native README](../../build/WI013-audio-native/README.md): scope and gates.
- [Exact PLAN/receipt gate](../../build/WI20261023-001-audio-hash/audio_native.py),
  [actor frame producer](../../build/WI20261023-001-audio-hash/audio_private.py),
  [login contract](../../build/WI20261023-001-audio-hash/audio/existing_login.py),
  [trial](../../build/WI20261023-001-audio-hash/audio/audio_trial.py) and
  [ledger](../../build/WI20261023-001-audio-hash/audio/audio_ledger.py).
- [PG20261023 receipt](../../build/WI20261023-003-review/review-receipt.json):
  audio_verdict LOCAL_HASH_LINKAGE_PREPARATION_PASS, phase2_authorized=false.
  Its overall phase1 allowance is not native audio permission. Historical audio
  checks 15 hash + 26 native-contract + 8 independent are reused, not rerun.
- [Committed fixture result](WI-20261024-ATS-003-evidence-pack.md): ADMIN 2,
  buyer USER 3, retained negative USER 1; grant day 2026-10-24 only. TEST verified
  flags do not prove credentials, email verification, consent or login.
- [Korean draft approval](../user/REQ-20261011-ATS-007.md),
  [current entry](../../docs/index.md) and [summary](../user/WI-20261024-ATS-004-summary.md).

MA-owned fresh local observations, 2026-10-24:

| Command/check | Result and limit |
| --- | --- |
| `python -B build/WI20261023-001-audio-hash/verify_candidate.py` (88066d) | exit 0 LOCAL_IDENTITY_PASS; manifest `cd32ca3fb64e84b324d95c1172041395d6413c7648be4995cec198835489ff92`; 68 members/base 62/copies 53; mismatch none; two overlays reproduced |
| `python -B build/WI011-audio-review/verify_identity.py` (d643dd) | exit 0; Source643 mismatch none; JAR/WAV match; v8r1 auth 16/PC delivery 12/private producer 7 mismatch none |
| Saved new-account input metadata/ACL (bb3d06) | exists and PRIVATE_ACL_OK; no values read; ID1 credential binding NOT_VERIFIED |
| Scoped source status and agent (08b50d, 01:15 KST) | `git status --short -- src frontend build.gradle settings.gradle` empty; agent Stopped/Disabled; no server contact |

MA reported main/HEAD `91a16e72693d7cd6ed04e0d36a5bd160af96f6d8`.
Retained JAR SHA-256 is `f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81`;
WAV is `d7b1011a45c84eb0c0d0308cc744289aa277b8b43ff0a4b97d6c59a1254ffa13`,
10 seconds/44100 Hz/stereo/16-bit. No new suites, build or network were used.
DocOps did not rerun these MA commands or inspect actual private input.

## Exact Proposed Effects and Runtime

Three existing-account password logins: ADMIN=2, ENTITLED_USER=3,
NO_ENTITLEMENT=1; freshly returned tokens go to ID/role checks. Only each exact
login user's refresh_token and possibly updated_at may change before the audio
baseline. MA's AuthService read confirms SHA256(new refresh token) storage and
refresh mismatch rejection. This replaces the prior refresh session; it does
not prove immediate invalidation of an already-issued access token. The future
approval must explicitly override ID1's prior preserve-all rule for these fields
only. All other pre-existing cells remain immutable; no session rollback is implied.
These session-field changes persist in the DB after app shutdown. There is no
automatic logout, refresh/reissue or restoration of ID1's prior refresh value.

| Proposed delta | Exact contract |
| --- | --- |
| Track | One new ADMIN2-owned synthetic ten-second track, waveform/processing metadata, READY and one activation; isActive=true row retained after the trial; download_count 0 to 1, play/like counts 0 |
| Files | One new approved WAV source and one new MP3 derivative retained after the trial; all old files preserved |
| Journals | Four storage_mutations: CREATE2 and DELETE-labelled2, final DONE; DELETE reasons SHARED_REFERENCE_RETAINED preserve the shared source |
| Official download | USER3 WAV download exactly once; one license and one track_download, source hash equality |
| HTTP checks | Anonymous loopback full MP3, range/seek comparisons and invalid range; anonymous/ID1 original denial and raw-path denial; full STREAM hash/size bound to the same final derivative |
| Exclusions | No role/profile/entitlement changes, signup/reset, extra album/playlist, payment/mail/consent, old data deletion or automatic mutation retry |

The retained exact FFmpeg arguments select libmp3lame/128k/44100/stereo with at
most one child. This is argument enforcement, not a measured native output
codec/bitrate result. Actual conversion, HTTP/file linkage and listening remain
unverified. The loopback anonymous stream checks the public-preview policy while
external HTTPS remains quarantined; no public endpoint/listener is added.

The proposal includes temporary DB/JAR/audio-worker lifecycle and the retained
nginx stop/restart plus original quarantine/service restoration. Preserve trial
data/evidence; runtime-overlay restoration is distinct from application deletion.
Require independent cleanup results; partial safety stops do not erase aggregate
HOLD/FAILED. Bounds: service 900s, engine 480s, 72 polls at 5s, HTTP 12s,
2MiB media response, audio wire 8MiB plus bounded login/ME, retained overall
128MiB wire/40MiB control-log/1GiB free-space. Inventory limits are 10,000 files,
128MiB per old file and 512MiB total; excess causes HOLD, not deletion. A KST
date rollover ends the trial; the grant is not automatically extended.

## Remaining Gates and Optional Listening Phase

1. Saved ADMIN2/buyer3 inputs can be reused, but ID1 email/current password and
   actor binding are NOT_VERIFIED. Request them through a reviewed dedicated
   private input path only after technical readiness; never search credentials,
   reset passwords or create replacement actors automatically. Use ID1's current
   password unchanged. Future wiring follows existing_login's 1..256/no-NUL
   email/password contract and three distinct emails, not capture014's new-account
   12..72-character password policy.
2. `audio_private.frame(actors)` is a pure in-memory producer. No reviewed actual
   credential-capture/transport/native caller integration has been established
   here. The old mail/profile producer is incompatible. Define protected custody,
   the exact root-pipe caller and retained private evidence without exposing bodies;
   this audit does not implement that missing path.
3. Require genuine fresh user-server-data, pg-audio-exact, target and backup-restore
   receipts bound to exact candidate/boot/expiry/actors. The local PG verdict cannot
   substitute for ALLOW_EXACT_AUDIO_CANDIDATE. Current backup/isolated-restore
   evidence-hash/boot/expiry binding is unverified; prior backup success is not
   declared absent. Never fabricate approval status or evidence. Actual target
   account/grant/quiet-work/cgroup/BPF/nft/JAR/WAV/baseline gates remain NOT_RUN.
4. The next approval may include a conditional private transfer: exactly one MP3
   produced by this trial, matched by ledger/size/SHA-256, to an owner-only user-PC
   path. After API/cleanup PASS, MA checks the actual track/key/file against that
   scope; do not require repeated approval when file/scope conditions are unchanged.
   Stop on a mismatch. No transfer is currently approved. Do not transmit ledgers
   or credentials.
   Verify local size/SHA-256 before using an existing local player, then ask the
   human to hear all ten seconds and seek forward/back. Record device/player and
   audible/seek result. Caller/transfer/receipt handling is not implemented or
   validated by this package; this audit neither builds nor executes it. This
   phase does not validate the application's browser-player UX or lift HTTPS
   quarantine. Human presence is requested only when the verified file is ready.

## Commands & Outputs

DocOps read the handoff, relevant READMEs, PLAN, producer/login/trial/ledger and
safe review receipt. Only current index/REQ additions and own two documents are
written. No source/tool edits, tests, service/network actions, private input or
credential search, server access or new implementation. Focused documentation
checks cover only new sections/files, metadata, links and whitespace.

Focused validation PASS: four owned scopes, 20 relative links, metadata/status/
date, whitespace/newline, verbatim local-only instruction and explicit unapproved
draft boundary. Required Tier0 paths exist. Session persistence, unchanged ID1
password, retained active media and conditional one-MP3 scope were checked in
the new text. No exact IPv4/HTTP URL is published; no broad historical check or
product/native test was run.

## Risks / Rollback

Do not label local reuse as executable native readiness or human acceptance.
The Korean text is an approval proposal, not a new user approval. Preserve old
results and exact freezes; scope correction is additive. No server rollback,
data deletion, grant extension or approval replay is performed or authorized.

## Follow-ups

The local-only WI004 is complete. Missing caller/input/receipt/transfer work and
native execution require a later explicit decision with concrete scope; none is
auto-triggered. REQ007 remains active. Preserve the unknown original Save cause.

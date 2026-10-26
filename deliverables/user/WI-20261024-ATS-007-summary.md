---
version: 1.2.0
last_updated: 2026-10-24
project: ATS
owner: DocOps
category: summary
status: stable
related_wi: WI-20261024-ATS-007
---

# WI-20261024-ATS-007 Summary

## Current: Local Preparation Complete; Native Execution Pending

WI007's local preparation is complete. SE implemented the private capture/caller/
transfer wiring and PG accepted the exact candidate for local preparation only,
with 17 focused checks PASS and no local blocker. MA confirmed saved input,
private ACL and actual frame/saved-account merge PASS at 2026-10-24 01:54:50 KST;
the user confirmed save and window closure. The two saved accounts were reused,
and ID1 was captured under its existing-password contract without reset.

Actual ID1 identity/login remains unverified. No server, app, login, audio,
payment, MP3 transfer or listening occurred. MA reported source scoped status
clean and SSH agent Stopped/Disabled. The [final evidence](../agent/WI-20261024-ATS-007-evidence-pack.md)
pins candidate 3425c08551ad168d99854a8150c3cc67fce77738248dc043173f48e49049c441
and PG receipt 24c52c605055f7f9c99219a1b8907d9fd9c6b5adfcf8635614653c285a990089.
Local Save checks used synthetic inputs; the actual save/ACL/frame result belongs
to MA's separate sanitized observation. Save tests were reused across the final
check-only delta with unchanged Save implementation; no historical suite rerun.

Stage 1 overall and REQ007 remain open. The following scope is a draft for later
approval, not authorization from the completed local-only work.

### Proposed application and data effects

- Log in ADMIN2, buyer USER3 and negative USER1 once each. Explicitly permit
  refresh_token and possible updated_at changes on those three users, including
  ID1; preserve every other existing cell. These session changes persist after
  shutdown, without rollback, automatic logout or reissue. Use ID1's existing
  password unchanged, without reset or new-signup restrictions.
- Upload one fixed ten-second WAV, create one active track with processing/
  waveform metadata and one MP3, and activate once. Retain the active row and
  both files. Allow four storage journals (CREATE2 plus shared-source-retaining
  DELETE2), one USER3 official download, one license and one track_download;
  download_count becomes 1 while play/like counts stay 0. No existing file deletion.
- Check loopback MP3 bytes/ranges/seek and same-file hash linkage, original-WAV
  hash, anonymous/ID1 download denial and raw-path denial. Preserve external
  HTTPS quarantine. Temporarily run DB/app/audio worker and restore the original
  nginx/runtime/access state within the fixed finite bounds. Do not retry
  uncertain mutations automatically.
- Use the existing grant only on 2026-10-24 KST; stop on rollover. No payment,
  mail/consent, reset, role/profile/grant changes or date extension. An optional
  approval may also cover exactly one trial MP3 matched by ledger/size/hash,
  copied privately to an owner-only PC path for listening/seek. MA checks the
  actual file against that condition without repeat approval when unchanged;
  browser-player UX is separate.

### Preflight evidence required before native release

The reviewed wiring and MA local input/merge checks are complete. Fresh genuine
user/data approval, exact native PG allowance, target/boot/candidate/day/expiry
and backup receipts, plus target account/entitlement/quiet-work/runtime checks,
remain required. Local input validation does not prove actual actor identity or
login success; neither historical audio review nor this local review is native
allowance. Saved inputs can be reused without recollection solely due to future
server receipt expiry.

PG's pre-review finding identifies the WI062 backup as a valid historical
43-table/6-row, audio-empty recovery proof that predates the fixtures and excludes
runtime keys. It does not establish current account recovery. MA must supply
genuine external evidence and current target/period/recovery-scope reasoning
covering today's fixture state and the runtime-key boundary. If that cannot be
established, native release stays HOLD. Any newly needed backup/isolated-restore
operation requires a concrete separately authorized plan; this draft grants none
and cannot fabricate BACKUP_AND_ISOLATED_RESTORE_PASS.

The prior [Korean scope proposal](REQ-20261011-ATS-007.md) remains a draft. No
native execution, private-input read or additional implementation is performed
by DocOps. WI007 is complete for local preparation; REQ007 remains active.
No further execution chain starts under this local-only approval.

## Historical Initial Approval Checkpoint

The user approved completing local audio wiring and independent review, plus
necessary dedicated-window inputs. **Stage 1 is not complete:** exact reviewed
candidate and MA capture outcomes are pending. WI007 and REQ007 remain active;
actual server execution requires separate approval after readiness.

The chain is SE005 local preparation -> PG006 exact review -> MA dedicated input
after review -> Doc007 integration. Reuse stored ADMIN2/buyer3 inputs and take
ID1's existing password unchanged under the existing-login contract, without
new-signup restrictions or reset. No private input or pointer is read by DocOps.

Frozen candidates/source/JAR and historical evidence are preserved. No network,
service/DB/app/login/audio/payment, password reset or grant/date extension is
authorized. Genuine current user/PG/target/backup receipt bindings remain future
native gates; local tests or input validation will not be reported as native success.

See [verbatim approval](REQ-20261011-ATS-007.md),
[governing handoff](../agent/WI-20261024-ATS-007-handoff.md) and
[initial evidence](../agent/WI-20261024-ATS-007-evidence-pack.md).

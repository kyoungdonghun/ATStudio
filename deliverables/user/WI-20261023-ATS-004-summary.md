---
version: 1.4.0
last_updated: 2026-10-23
project: ATS
owner: DocOps
category: summary
status: stable
related_wi: WI-20261023-ATS-004
---

# WI-20261023-ATS-004 Summary

## Final Phase 1 Result — 2026-10-23

**Approved phase 1 is complete.** MA observed the account inventory successfully,
all four DB cleanup checks passed, and a separate post-stop probe passed. Original
TCP22 firewall source restoration and unchanged 80/443 rules were read back.
Provider/OS-console logout, owned-key removal and zero remaining identities were
confirmed; SSH agent is Stopped/Disabled. WI004 is complete; REQ007 remains active.
Application startup/login and business DML were not performed. Phase 2 has not
been approved or executed.

| Verified result | Meaning and remaining limit |
| --- | --- |
| One account: ID 1, designated latest-auth-signup USER | Nondeleted, email verified, local password present; preserve it as the negative actor candidate |
| Effective subscriptions 0 | The account is unentitled; there is no current ADMIN or entitled USER |
| Credentials known / password-login policy NOT_CHECKED | Inventory does not prove successful login |
| Pending audio 0, unfinished storage 0 | No work pending in the bounded inspected categories |
| DB STOP/PID_ZERO/SOCKET_GONE/BASELINE PASS; post-stop PREFLIGHT_PASS | Approved temporary DB inspection and restoration completed |

The earlier UAC cancellations, SSH timeouts and open-access states remain
historical checkpoints below. The eventual temporary TCP22 source change and
restoration were explicitly approved. No exact IP or console token is included.
The restoration receipt's `owned_input_window_closed=false` records a conditional
process-close action that did not execute; no claim is made that every native
window was closed.

## Proposed Phase 2 Changes — Not Approved or Applied

Preserve ID 1's USER role and lack of entitlement. The existing three-actor audio
contract needs an ADMIN uploader, a positive USER and a negative USER. The bounded
proposal is:

| Separate fixture preparation before the audio baseline | Required decision or missing implementation |
| --- | --- |
| Create two new ATStudio accounts | Actual owners/private credentials and approved verified-fixture status are not selected; do not fabricate consent or a verification-mail event |
| Promote only the new upload account to ADMIN | One designated role update plus one admin_operation_audit_logs USER_ROLE_CHANGE row, retaining ID 1 unchanged; new uploader designation needs approval and must respect earlier user intent |
| Add one non-billing entitlement for the new positive USER | Existing active INDIVIDUAL plan ID has not been queried; at least one available download is required. The bounded provisioning tool is not implemented or reviewed |

Proposed entitlement values are MONTHLY/CANCELLED, start=expiry=the approved KST
execution date and both pending fields NULL. This is a one-day fixture, not an
applied grant or a normal paid monthly subscription. A missing/unsuitable plan
means HOLD, not automatic plan creation. No billing agreement, order, payment,
default playlist, consent or email-token row is implied; any required extra row
must be enumerated before approval. The correction API requires an existing
entitlement row and cannot supply this new grant by itself.

These fixture writes require a separately approved preparation stage. The frozen
audio PLAN accepts existing actors and sets account_role_profile_writes=False
and entitlement_writes=False; provisioning cannot be inserted into the audio run.
The fixture tool must be implemented and independently reviewed before execution.

The later bounded audio trial would require explicit approval for three actors'
login refresh/updated_at writes, one track upload/activation, original WAV/derived
MP3, four storage journal rows, one entitled USER's first-download license and
download row, and download_count +1. Retain generated records/media by default;
service cleanup or expiry is not deletion/demotion authority. The current engine
does not implement logout; additional logout/session revocation needs an explicit
scope decision. No extra album or 443 change is added to this proposal.

## Local Hash Verification and Remaining Audio Boundary

The full HTTP-stream length/SHA comparison to the same derivative file is now
implemented and independently reviewed in the new local candidate. The provided
and independent suites total 75 focused tests PASS. Source643, retained JAR and
original frozen tools remain unchanged. This resolves the earlier local missing
hash-linkage issue; it does not establish native audio, audible playback or
browser-player acceptance.

The API trial uses loopback and retains external HTTPS quarantine. Intended
product public streaming does not imply opening external HTTPS. The exact-file
transfer/listening recipe exists, but transfer tooling is not implemented and
no listening has occurred. Human listening should be requested only after the
technical preparation is ready.

Evidence: [inventory](../../build/WI20261023-phase1-native/inventory-result.json),
[restoration](../../build/WI20261023-phase1-native/final-restoration.json),
[PG result interpretation](../agent/WI-20261023-ATS-003-evidence-pack.md#addendum-phase1-native-result-interpretation-2026-10-23),
[WI004 evidence](../agent/WI-20261023-ATS-004-evidence-pack.md),
[current REQ007](REQ-20261011-ATS-007.md).

## Historical Intermediate Checkpoints

The following snapshots preserve what was known before the final result above.

**Latest: third UAC succeeded and SSH was attempted, but transport timed out;
phase 1 account inventory remains NOT_RUN. WI004 and REQ007 remain active.**
The current session retains one loaded key, SSH agent Running/Manual and provider
login. No cleanup PASS is claimed. The prior cancellation/zero-attempt checkpoint
below is historical.

Read-only provider inspection found the old SSH source /32 differed from the
current PC public IPv4. The server was operating with ELCAP ON. OS-console root
login succeeded; SSH units/listener and nginx listeners were present, and the
nft ruleset was empty. The OS console was exited to its login prompt. OS login
is distinct from application login. No DB start, account inventory, application
start/login or business DML occurred.

The provider firewall is unchanged. An unsaved proposal to change only the SSH
22 source and restore it after phase 1 awaits explicit approval. Other ports are
unchanged. Exact IP values and token URLs are omitted. Account reuse remains
unknown; actual inventory and access cleanup results are still pending.

**Current: local preparation/review complete; phase 1 account inventory NOT_RUN.
WI004 and REQ007 remain active.** Both local UAC attempts returned a cancellation
result. Its cause cannot be determined from that message. MA's final local
check found zero consent processes and SSH agent Stopped/Disabled; there is no
pending UAC request.

MA's execution history records zero server connection/SSH command attempts, DB
starts, application starts, logins and business-data writes. This is not a remote
state check. Account reuse and any required role/fixture changes remain unknown
because inventory did not run; no new native cleanup result is claimed.

Local hash linkage and the exact phase 1 account procedure have completed
independent PG review. The allowance is
`ALLOW_EXACT_PHASE1_DB_ONLY_WITH_TARGET_GATES`, with local audio preparation
`LOCAL_HASH_LINKAGE_PREPARATION_PASS`. Provided 15+26+16 and independent 8+10 tests
passed, total 75, in local Windows synthetic/source checks. Source643, the
retained JAR and original v8r1 assets remain unchanged. These results do not
establish successful native account inspection or audio playback.

The complete HTTP stream now has length/SHA linkage to the same trial track's
derivative in the new local candidate. MA must still pass exact target/provenance
and cold-quarantine gates, then report the bounded DB inventory and all four
cleanup checks. Native evidence is pending; WI004 is not complete.

The user's October23 approval is recorded in [REQ007](REQ-20261011-ATS-007.md)
and the [current decision entry](../../docs/index.md). Astra Extra High was
selected by the user. This checkpoint does not record a native start or result.

Approved: local stream/file hash linkage, independent review and MA-only phase 1
account reuse inspection, including necessary DB temporary start/read/stop.
Application startup, login and business-data changes remain prohibited. Phase 2
audio execution needs separate approval.

The chain has completed SE001/SE002 -> PG003 and now awaits MA phase 1 inspection
and cleanup -> DocOps004 final integration. Final account reuse,
unknown prerequisites, required role/fixture changes and cleanup results are
pending. Existing authentication success remains dated evidence and is not
scheduled for a full replay. REQ007 remains active.

See [in-progress evidence](../agent/WI-20261023-ATS-004-evidence-pack.md).

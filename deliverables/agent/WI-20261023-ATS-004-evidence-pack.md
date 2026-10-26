---
version: 1.4.0
last_updated: 2026-10-23
project: ATS
owner: DocOps
category: evidence-pack
status: stable
related_wi: WI-20261023-ATS-004
dependencies:
  - path: WI-20261023-ATS-004-handoff.md
    reason: Approved documentation scope and final-result dependency
---

# Evidence Pack: WI-20261023-ATS-004

## Summary

Approved phase 1 is complete: native inventory PASS, four DB cleanup checks PASS,
independent post-stop PREFLIGHT_PASS and original firewall/access restoration
confirmed. One designated unentitled USER exists; ADMIN and entitled USER are
absent. Exact phase 2 fixture/audio deltas are proposed, not approved or applied.
WI004 is complete; REQ007 remains active. Native audio is not yet accepted.

## Scope / DoD Check

- [x] Verbatim Korean user approval and Astra Extra High selection recorded.
- [x] SE001/SE002 parallel -> PG003 -> MA phase 1 -> DocOps004 chain recorded.
- [x] Necessary DB temporary start/read/stop distinguished from prohibited app,
  login and business-data changes; separate phase 2 approval retained.
- [x] Existing index/REQ historical bodies preserved by additive insertion.
- [x] Initial scoped documentation checks passed for four owned files.
- [x] Final exact-candidate independent review received and receipt hash verified.
- [x] Both local access cancellations and final local-state check recorded.
- [x] Superseding access-open/SSH-timeout/provider-console observations recorded.
- [x] Native account reuse/unknowns received.
- [x] Proposed role/fixture changes and actual restoration result integrated.
- [x] Final scoped documentation validation completed.
- [x] Coherent final user summary prepared, with phase 2 approval boundary.

## Reference Documents (Tier 0-2)

Injected context is from the WI004 handoff and MA instructions. Rules:
`.claude/config/context-injection-rules.json`, assignee `docops`, required tiers
`[0, 1]`, documentation task. Project tag ATS was checked in
`.claude/config/workspace.json`.

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | docs/standards/core-principles.md | Approved scope and orchestration |
| 0 | docs/standards/documentation-standards.md | Additive, evidence-led documentation |
| 0 | docs/standards/development-standards.md | Preserve source/runtime boundaries |
| 0 | docs/standards/glossary.md | Consistent REQ/WI terminology |
| 1 | docs/policies/security-policy.md | No secrets or unauthorized data changes |
| 1 | docs/policies/quality-gates.md | Exact-candidate review before native work |
| 2 | deliverables/user/REQ-20261011-ATS-007.md | Active requirement and verbatim approval |
| 2 | docs/index.md | Current decision entry and dated history |
| 2 | deliverables/agent/WI-20261021-ATS-009-evidence-pack.md | Historical bounded authentication/cleanup |
| 2 | deliverables/user/WI-20261021-ATS-012-summary.md | Earlier local audio preparation limits |

## Evidence Pointers

- [Current entry](../../docs/index.md): new October23 phase 1 approval checkpoint.
- [REQ007](../user/REQ-20261011-ATS-007.md): verbatim approval and blocked-WI chain.
- [WI004 handoff](WI-20261023-ATS-004-handoff.md): initial/final dependency contract.
- [User summary](../user/WI-20261023-ATS-004-summary.md): initial status and limits.

## Commands & Outputs

### Final October23 Phase 1 Completion and Restoration

Verification date: 2026-10-23. Environment: approved remote DB-only native
inspection, plus local SSH/provider access checks. Operational evidence owner:
MA. DocOps read the sanitized local receipts and checked their file hashes;
it did not rerun the native operations or expose private target identifiers.
PG's [native interpretation](../../build/WI20261023-003-review/native-result-interpretation.json),
SHA-256 `b76e762922a68617905c9224efbc8232836f092a26e9cfec7dafa6c2c5506668`,
independently supports the safe receipt interpretation. Its
[addendum](WI-20261023-ATS-003-evidence-pack.md#addendum-phase1-native-result-interpretation-2026-10-23)
is local PG evidence; browser/OS execution remains MA-owned. DocOps checked the
interpretation receipt hash without additional source inspection or test runs.

| Evidence | SHA-256 / result |
| --- | --- |
| [Successful preflight](../../build/WI20261023-phase1-native/probe-result-02.json), MA `645b0c` | `f5666777574c70f62a5bce90fbbf53cc63382e9769407b9b85e2de516d3e58a8`; PREFLIGHT_PASS |
| [Payload identity check](../../build/WI20261023-phase1-native/inventory-payload-check.json), MA `4f1915` | Receipt `1505083ddcb8b9cafacc5f7c349a482b4e3b002826294a88ec93ce29af848230`; GENERATED_INVENTORY_DELTA_PASS |
| [Inventory result](../../build/WI20261023-phase1-native/inventory-result.json), MA `68262b` | `5c1c69a30bbf30c6ffbf694e8278af88844a79088d20d92876fbefe0396de5aa`; INVENTORY_OBSERVED, inventory PASS |
| [Separate post-stop probe](../../build/WI20261023-phase1-native/post-stop-probe.json), MA `ed9af8` | `f5666777574c70f62a5bce90fbbf53cc63382e9769407b9b85e2de516d3e58a8`; PREFLIGHT_PASS |
| [Final restoration](../../build/WI20261023-phase1-native/final-restoration.json) | `e81844ae9de1a1780f67f9be5424f71160059569119645be81e366f3b763fe04` |

The generated inventory payload is 111873 bytes, SHA-256
`4fae8862471c5fb9f9cd3c07f3d2e712a0d18b06cf1d90391b299347f8692a2a`.
Only MODE/EXPECTED_BOOT/EXPECTED_MACHINE differ from the reviewed probe; all
embedded sources match and the target equals the successful probe. Fresh MA
source643/JAR/frozen identity check `2463c6` passed before native execution.

The inventory observed exactly one account: ID 1, USER,
LATEST_AUTH_SIGNUP_DESIGNATED, nondeleted, email verified, local password present,
account auth prerequisites true, effective subscription count 0,
entitled_user_with_quota false and unentitled_user true. Thus it is the negative
actor candidate; no current ADMIN or entitled USER exists. Credentials known and
password-login policy are NOT_CHECKED, so login ability is not proven. Pending
audio 0, unfinished storage 0 and existing_work_quiet true apply to this bounded
inspection. No app start/login or business DML occurred. Temporary MySQL startup
can perform internal lifecycle writes; this is not an immutable physical snapshot.

Inventory cleanup independently reports STOP, PID_ZERO, SOCKET_GONE and BASELINE
PASS. The separate post-stop read-only probe also passed. This completes the
approved DB-only inspection, not phase 2 audio or production acceptance.

The user explicitly approved temporary TCP22 source replacement with the current
PC /32, inspection/DB stop and restoration of the original /32, with other rules
unchanged. Initial policy processing was followed by login-required state. After
one saved-login renewal, readback still showed the original rule; MA then
resubmitted the exact approved change, observed success and confirmed current-PC
source readback. After inventory, original TCP22 restoration produced a success
alert and saved-rule readback; original 80/443 rules were unchanged. No exact IP,
address or console-token URL is included here. Earlier timeout/unknown-provider
checkpoints remain preserved, not relabelled as successes.

MA observed provider logout through the homepage Login link and closed its
created browser tabs. Recovery-console logout/login-prompt verification preceded
closing that console. Local cleanup `b1d5dc` removed the owned key successfully,
left zero identities and restored SSH agent Stopped/Disabled; final readback
`c79c74` confirmed the same. The sanitized receipt's
`owned_input_window_closed=false` means its conditional process-close action did
not execute; it is not a claim that every native window was closed, nor by itself
evidence of failed key/service cleanup.

### Proposed Phase 2 Fixture Stage and Audio Deltas

Proposal only: preserve ID 1's USER role and lack of entitlement. Create two new
ATStudio accounts, promote only the new upload account to ADMIN while retaining
one admin_operation_audit_logs USER_ROLE_CHANGE row, and create one non-billing
user-subscription row for the new positive USER. No existing account promotion
or entitlement change is proposed. New uploader designation needs explicit
selection and cannot silently override any earlier intended uploader mapping.
Actual owners/private inputs, approved verified-fixture status and the existing
active INDIVIDUAL plan ID remain unselected/unqueried. Do not fabricate human
consent or a verification-mail event. No provisioning tool is implemented or
approved yet; do not bypass the first-admin guard/audit with a direct ADMIN insert.

The frozen audio PLAN requires existing_actors_only and prohibits
account_role_profile_writes and entitlement_writes. Therefore the two users,
one new-user ADMIN promotion/audit row and one entitlement row belong in a separately
approved, bounded fixture stage before the audio baseline. A fixture candidate
must be implemented and independently reviewed before native-ready claims or
execution. Proposed entitlement values are MONTHLY/CANCELLED,
started_at=expires_at=the approved KST execution date and both pending fields NULL.
The existing active INDIVIDUAL plan must provide at least one available download;
missing/unsuitable plan means HOLD, not automatic plan seeding. This is a one-day
test fixture, not a normal paid monthly subscription. Billing agreements, orders,
payments, default playlists, consent and email-token rows remain excluded; any
required extra row must be enumerated before approval. Existing correction APIs
require an existing subscription row and are not a new non-payment grant mechanism.

After those prerequisites, the separately approved audio execution would change
three login refresh/updated_at rows; create/activate one trial track with original
WAV and derived MP3; create four storage journal rows (two CREATE/two DELETE
retaining the shared source); and create one license plus one track-download row
and increment download_count once for the entitled USER. ADMIN uploads only.
The current engine has no logout; any added logout/revocation needs an explicit
scope decision. Retain trial records/media by default; service cleanup or expiry
does not authorize deletion, demotion or revocation. No extra album or 443 rule
change is introduced by this proposal.

The complete-stream/file length+SHA linkage is implemented and locally reviewed
in the new 68-member candidate, with 75 local tests PASS; native audio remains
NOT_RUN. API execution stays on loopback with external HTTPS quarantine retained.
Intended product public streaming differs from external network exposure. The
private derivative-transfer/listening recipe is documented but transfer tooling
is not implemented; request human listening only after technical preparation.

### Latest October23 MA SSH and Provider Access Checkpoint

MA owns the operational observations; DocOps read only the sanitized local
access receipt and verified its SHA-256. No operational action was repeated.

| Receipt / evidence | Observation and limit |
| --- | --- |
| `10b671` | Third UAC request succeeded; local SSH agent Running/Manual |
| `9eb122` | One key identity loaded after visible unlock process PID 13908 |
| `2b32df` | Exact phase 1 probe SSH attempt failed; inventory did not run |
| `9d0dc5` | Bounded transport-only diagnostic exited 255, TIMED_OUT |
| [Sanitized access receipt](../../build/WI20261023-phase1-native/access-check.json) | SHA-256 `225ffc30ea7529b753a40250b07471c8e8e9caa41c6181fab9c8c4b3edd88651`; DB start not attempted |

MA's provider read-only inspection found the server operating, ELCAP ON and the
old fixed SSH source /32 different from the current PC public IPv4. Exact IP
values and console token URLs are excluded. Provider firewall rules were not
changed. An unsaved SSH-22-only source-change proposal, including restoration
after phase 1, awaits explicit approval; other ports remain unchanged.

MA used noVNC OS root login successfully, then ran read-only
`systemctl is-active ssh ssh.socket`, `ss -lntp` and `nft list ruleset`.
Both SSH units were active; SSH 22 and nginx 80/443 listeners were present;
the nft ruleset output was empty. MA exited the OS console and verified its login
prompt. OS administration login is distinct from prohibited application login;
no DB start, account inventory, application start/login or business DML occurred.
Listener observations do not prove application startup or public accessibility.

Current access remains open for the authorized session: key loaded, SSH agent
Running/Manual and provider logged in. No key removal, agent stop, provider
logout or cleanup PASS is claimed. The prior zero-SSH-attempt and
Stopped/Disabled checkpoints are preserved history and superseded for current
access state. Account eligibility, required role/fixture changes and final
phase 1 cleanup remain unresolved. This update adds no phase 2 authority.

### Latest October23 MA Access Result: Phase 1 NOT_RUN

Evidence owner is MA. DocOps did not repeat the access or local service checks.

| Receipt | Observation and limit |
| --- | --- |
| `8dc5f5` | First UAC request ended with the cancellation message `사용자가 작업을 취소했습니다` |
| `9862d9` | Second UAC request ended with the same cancellation result; no UAC helper remains pending |
| `ce7a7b` | Final local check: zero consent processes, SSH agent Stopped/Disabled |

The cancellation message does not distinguish a person's manual cancellation
from a Windows timeout. No cause is asserted. The earlier report's wording
"cancelled by the user" records the returned cancellation result, not proof of
the actor or cause; the earlier second-request-pending state is superseded.

MA explicitly recorded zero SSH-command/server-connection attempts, DB starts,
application starts, logins and business-data writes. These are execution-history
assertions, not remote observations or a new server-state scan. Source643/JAR and
the reviewed candidates were not changed by this documentation update.

The native inventory is NOT_RUN; account existence/role/entitlement reuse and
required role/fixture changes remain unknown. No failed-account finding can be
inferred. There is no new DB stop/cleanup result because DB startup was never
attempted. Only the original local SSH-agent state and absence of pending consent
were confirmed. The prior dated server cold/quarantine evidence is unchanged.

Local preparation/review remains complete with 75 tests PASS. WI004 stays active
pending actual phase 1 evidence; phase 2 remains separately unapproved. No full
authentication replay, native retry or remote action occurred in this update.

### October23 Local Preparation and Independent Review Checkpoint

Evidence owners are SE001, SE002 and PG003 for their local work; DocOps verified
the local PG receipt hash only, without rerunning their tests. Environment:
local Windows synthetic execution and source review, verified on 2026-10-23.

| Object | SHA-256 |
| --- | --- |
| [PG003 receipt](../../build/WI20261023-003-review/review-receipt.json) | `68fc134f9cf1ddf10649ed1f74a0741ee4426e748032a19ca16e6c51443988fe` |
| Audio manifest, 68 members | `cd32ca3fb64e84b324d95c1172041395d6413c7648be4995cec198835489ff92` |
| Account candidate manifest | `bb0f5cd13c3148931a3bcce048c0c91a19abc3129c5d3e8844f76955062f63aa` |
| Account implementation bundle | `506181ad2714c6a0f5addb2fe8b3d557c6d81401b4028dc9731464afd092b6ef` |
| Source643 manifest | `c6fc15d8f38c6597d159c7dd258048d25e74b3d03f5eb5596b701219a8ae207e` |
| Retained application JAR | `f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81` |

The PG verdict is `ALLOW_EXACT_PHASE1_DB_ONLY_WITH_TARGET_GATES`; the audio
verdict is `LOCAL_HASH_LINKAGE_PREPARATION_PASS`. Local provided suites:
hash 15 + native-adapter 26 + account 16 PASS; additional independent PG suites:
hash 8 + account 10 PASS, total 75. Source643/JAR/original v8r1 assets and all 53
frozen audio copied dependencies remain unchanged per SE/PG evidence. Original
62-member audio manifest and earlier review records remain historical.

The new overlay compares full HTTP-body length/SHA to the same derivative and
durably binds the result to the exact candidate/attempt/track. The account
allowance requires exact payload-file verification and preserved generated hash,
probe target/boot/machine/successful-receipt/cold-quarantine checks, and the same
probe boot/machine for inventory. Only the fixed temporary source-mode mysqld,
SELECT inventory and owned stop are allowed; all four cleanup checks must PASS.
No native start/query/cleanup or audio execution is proven by this local review.

MA's reported access-preparation checkpoint: local SSH agent initially
Stopped/Disabled; after the user authorized opening the input window, the first
UAC request was cancelled by the user and made no service change. A second UAC
request was pending at that report. This is attributed local-access history,
not a current server-state, remote-failure or account-inventory claim. Native
inspection and cleanup receipts remain due before final WI004 completion.

Sources: [WI001](WI-20261023-ATS-001-evidence-pack.md),
[WI002](WI-20261023-ATS-002-evidence-pack.md),
[WI003](WI-20261023-ATS-003-evidence-pack.md). These are preserved original
evidence, not rewritten to claim that subsequent work was performed by them.

### DocOps Local Documentation Checks

DocOps read-only local verification on 2026-10-23: branch `main`, HEAD
`91a16e72693d7cd6ed04e0d36a5bd160af96f6d8`. Index was already modified and REQ007
untracked before this WI. These are checkout observations, not runtime identity.
No remote action, secret read, app/DB start, login or business-data query by DocOps.

Changes use `apply_patch`; scope is index, REQ007 and this WI's evidence/summary.
Initial scoped validation on 2026-10-23: four files, 313 relative links, required
metadata, terminal newline/whitespace and Tier0 file existence PASS. Both original
historical tails matched their pre-edit normalized SHA-256 values. Scoped
`git diff --check -- docs/index.md deliverables/user/REQ-20261011-ATS-007.md
deliverables/agent/WI-20261023-ATS-004-evidence-pack.md
deliverables/user/WI-20261023-ATS-004-summary.md` exited 0 with only an index
CRLF-to-LF notice. Direct checks include untracked files omitted by Git diff.

After adding the local PG checkpoint, the same four-file scope passed again:
323 relative links, required metadata, newline/whitespace, Tier0 existence and
both preserved historical tails. Scoped `git diff --check` again exited 0 with
the same line-ending notice. This verifies documentation only; native receipts
and final closeout remain pending.

Latest access-result update validation was limited to its four new sections:
two new relative links, section whitespace, expected metadata versions, active
status and explicit NOT_RUN/Stopped/Disabled boundaries PASS. The earlier
323-link check was not repeated; no source tests or native operations ran.

The subsequent SSH-timeout/provider checkpoint was checked only across its four
new sections: five relative links, expected versions/active status and whitespace
PASS; no exact IPv4 literals or HTTP token URLs were present in those sections.
The sanitized local access receipt hash matched the value recorded above. No
historical link scan, source-test rerun or operational recheck was performed.

Final completion/proposal checks passed for four new sections and changed
summary/metadata: 19 relative links, three referenced PG-addendum anchor checks,
versions/status/whitespace and explicit active-REQ boundary. No exact IPv4 or
HTTP token URL was present in those sections. The first checker pass looked for
the active-REQ sentence only inside the native section; it was in the changed
summary, so the checker scope was corrected and passed without a product change.
No historical link scan, broad suite or native replay was performed. The local
PG-addendum read initially encountered a console encoding error; UTF-8 output
resolved that read without changing evidence.

The validate-docs skill's bundled script has no scoped CLI option; this bounded
check applied its relevant existence/link intent through an inline
Python check and scoped diff. No repository-wide validator, YAML/anchor audit,
product test or runtime test is claimed. Final checks apply only to the new
completion/proposal sections and changed WI004 summary/metadata.

## Risks / Rollback

Approval must not be confused with successful execution; previous local-only
records remain historical. Preserve all unrelated changes and earlier failures.
Rollback is a reviewed removal of only this WI's inserted checkpoints and owned
deliverables plus restoration of its metadata increments. Do not reset the file
or working tree to HEAD.

## Follow-ups

WI001/WI002 -> WI003 -> MA approved phase 1 -> WI004 is complete. No subsequent WI
is unblocked within this approved phase 1 chain. REQ007 remains active because
phase 2 fixture preparation/execution and audio acceptance require separate
approval and exact implementation/review. Preserve all earlier failure/access
checkpoints. No further native operation is authorized by this document.

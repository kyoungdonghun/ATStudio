---
version: 3.126.0
last_updated: 2026-11-07
project: ATS
owner: EO
category: registry
status: stable
dependencies:
  - path: design/index.md
    reason: Current design and retained-runtime entry points
  - path: ../deliverables/agent/WI-20260909-ATS-028-evidence-pack.md
    reason: Authoritative bounded TEST runtime verification
---

# Documentation Index

> Purpose: Provide overall documentation structure overview and "starting points"
> **Single operational flow**: `MA + Subagents + Skills` (details: `docs/architecture/system-design.md`, `AGENTS.md`, `CLAUDE.md`)

## Current Decision Entry

### 2026-11-07 Operational Listening Site and Source Preservation

[The current source checkpoint](registry/git-checkpoint-20261107.md) records
client administrator upload/edit/explicit publication and anonymous listening.
MA's November 7 native and external HTTPS/browser observations completed this
bounded site-readiness scope. Application and DB services were retained running
and enabled, while temporary SSH access and local key/agent state were restored.
The public catalog remained empty for client-owned real music upload.

This Git review performs no new server operation. The dated observations do not
establish current live health, physical-mobile/human audio playback, reboot,
recurring backup, full recovery, mail/PG onboarding or grant acceptance.
See the checkpoint for exact evidence ownership, source changes and exclusions;
the previous entries remain historical and must not reverse this completion.

### 2026-11-06 Reviewed Source Checkpoint

[The public checkpoint](registry/git-checkpoint-20261106.md) records the current
listening/catalog implementation, guarded online backup and v2 outer HOLD recovery,
source identities, local verification, and private evidence exclusions. It supersedes
earlier implementation-pending statements for these local capabilities. The approved
scope is REQ-20261106-ATS-003 / WI-20261106-ATS-005 / WI-20261106-ATS-006.

Observation: 2026-11-06 KST, Windows local source review by EO/docops with PG privacy
review and MA-owned Git execution. Backend v2 and frontend v3 inputs still match their
November 5 candidate manifests; all 41 November 6 outer-recovery candidate files match.
The latest coherent local outer-recovery run passed 51 tests. Earlier overlapping
test runs must not be summed as unique coverage.

This entry publishes a bounded reconstruction summary. Detailed local chronology,
raw logs, private configuration, data, credentials and built artifacts are not in this
checkpoint. Git is source preservation, not a database/media/private-evidence backup.
Native Linux/root/systemd/cgroup enforcement, current restore and live publication
remain unverified by this checkpoint; server work remains deferred. A final MA Git
receipt will separately identify the committed revision and remote verification.

### 2026-10-26 Local Preservation Commits Complete; Stage Two Still Open

Three reviewed local commits preserve source (`f6742ff`, 7 paths), operating tools
and retained sources (`254e2fe`, 168 paths), and documentation (`5134558`, 558 paths).
The [final report](../deliverables/user/WI-20261026-ATS-007-summary.md) and
[EO evidence](../deliverables/agent/WI-20261026-ATS-007-evidence-pack.md) accompany
the final checkpoint commit. No push/fetch, server, provider or paid operation occurred.

The archive retains 73 exact source/document files and 13 reference-only derivatives;
63 visible sensitive/unreviewed files and ignored private originals remain local.
Exact per-path attributes preserve 36 document byte streams; historical EOF whitespace
exceptions are individually hash-pinned. Original evidence and failed attempts remain
distinct. This is neither off-device backup nor complete runtime reconstruction.

REQ-20261026-ATS-001's local preservation chain is complete. **Operating stage two
remains open:** client policy decisions and combined NCP/Toss enrollment/contract are
pending; optional NCP remains NOT_DEPLOYED. Stage-three operational safeguards and
the separately reviewed backup role-guard repair remain unfinished. Existing account,
grant and bounded stage-one results are unchanged. Follow the dated checkpoints below
for operational evidence, including missing-from-Git build/private dependencies.

### 2026-10-26 Local Operating/Privacy Preparation Reviewed; Toss Retained

**Policy-independent local preparation is complete; stage two remains open and
stage-three operational safeguards are not complete.** The user confirmed Toss
will remain; KG Inicis was a document misunderstanding. Do not ask the client to
choose the PG again. Contract/LIVE readiness is still pending, and historical
HWP/PDF artifacts remain unchanged. See [integrated result](../deliverables/user/WI-20261026-ATS-003-summary.md).

[Operating review](../deliverables/agent/WI-20261026-ATS-001-evidence-pack.md)
reused the actual October24 backup/isolated-verification and TLS evidence. Its
fresh local check matched 11 public source pins and the preserved JAR; no target
was observed. That snapshot predates audio/reset and October25 role/grant changes.
The old backup tool requires ID1 USER and audit count1/2: first derive and review a
minimal current-state candidate preserving original guards/evidence, **before
requesting access or executing a new backup**. This repair is specified, not yet
implemented. Recurring hooks/calendar, independent alerts, complete recovery and
application reboot recovery still require implementation/actual evidence.

[Privacy inventory](../deliverables/agent/WI-20261026-ATS-002-evidence-pack.md)
maps 14 source-backed flows and eight future focused checks. Withdrawal preserves
profile fields but deletes downloads/licenses; record-specific treatment and
continuing-rights choices must precede coordinated changes. No retention period,
legal conclusion or deletion was selected. [Integration review](../deliverables/agent/WI-20261026-ATS-003-evidence-pack.md)
cross-checked these high-risk claims and prepared a bounded PC/mobile checklist.
ID1 ADMIN and licensed redownloads cannot represent an unentitled first-download
denial/quota test. Buyer3's old saved password remains ineligible.

This work made only local preparation documents/receipts. No server, private-body,
DB/app, provider, new backup/restore, mail, payment or cost operation occurred.
Client policy answers and combined NCP/Toss enrollment remain pending; prior
account/grant and bounded stage-one completion remain unchanged.

### 2026-10-25 Pre-Signup Preparation Reviewed; Combined Client Handoff Draft Ready

**Local mail/provider/operating preparation passed [WI006 independent review](../deliverables/agent/WI-20261025-ATS-006-evidence-pack.md).
Stage two remains open; provider enrollment, activation and target validation are
not complete.** The user will request NCP and Toss enrollment from the client
together; the immediate NCP signup prompt is withdrawn. Sole-proprietor
registration is user-confirmed. No outgoing client message was sent.

[Combined handoff](../build/WI-20261025-ATS-004/client-signup-handoff-ko.md) and
[policy content/input draft](../build/WI-20261025-ATS-004/policy-content-and-inputs-ko.md)
are unsent/unpublished. Existing six plans, recurring policy and ID1 reply/operations
mailbox are reused. NCP MAIL numeric pricing remains unknown; published Toss
standard fees do not establish the client's actual contract.

[Operating preparation](../build/WI-20261025-ATS-005/README.md) and
[11-pin local check](../build/WI-20261025-ATS-005/local-check.json) passed with
default SMTP, seeding off and a single application scheduler owner prepared.
These settings are not server-applied. Audio-only suppresses scheduling/startup
recovery but is not a synchronous-payment or withdrawal-side-effect kill switch.

[Optional NCP mail source/JAR](../build/WI-20261025-ATS-008/checks.json) is locally
prepared, NOT_DEPLOYED: `23a312c331d10d2875308ddea7e8034fb6e13ea48291027064565e833dc80d15`.
Initial 40 focused tests passed; the review-found interpolation defect was repaired
and 16 NCP tests rerun, with 26 unchanged earlier tests reused. No fresh whole-42
run is claimed. Historical release `f3b748...` and candidate `0ced47...` are preserved.
No new server, private-body or paid operation occurred in this preparation.

Remaining: client identity/terms/automatic-billing contract, actual prices,
domain/sender/DNS authority and private keys; operator policy decisions/final public
UI, approved restricted provider-review access, deployment, actual delivery and
LIVE tests. Reuse existing inputs; new costs/real payment retain prior confirmation.
Account/grant completion below is unchanged. See [WI007 result](../deliverables/user/WI-20261025-ATS-007-summary.md).

### 2026-10-25 Stage-Two Account/Grant Applied; Access Restored, Provider Work Open

**Account/grant application is complete; stage two as a whole remains open.**
MA executed the [WI002-reviewed candidate](../deliverables/agent/WI-20261025-ATS-002-evidence-pack.md)
`480834a7fadc69d542dd9113deb0af877d67a570170aa1819c682feec81efe2c`:
[inspection](../build/WI20261025-stage-two-native/inspect.json) recorded,
[apply](../build/WI20261025-stage-two-native/apply.json) APPLIED with acknowledged
COMMIT, and [read-only reconciliation](../build/WI20261025-stage-two-native/reconcile.json)
APPLIED_CONFIRMED. All five cleanup checks passed in each of the three phases.

ID1 is now ADMIN; ID2 ADMIN and ID3 USER are preserved. Existing grant1 retains
its 2026-10-24 start, daily-five plan and nonbilling status, with expiry extended
through 2026-11-24 KST inclusive. MA reports the exact four-cell delta plus two
truthful audits verified against all 43 tables; six plans and other cells remain
unchanged. No app start/login, new backup, payment or paid cloud operation occurred.

[Before](../build/WI20261025-stage-two-native/probe-before.json) and
[after](../build/WI20261025-stage-two-native/probe-after.json) independent probes
passed cold checks. [Final restoration at 20:17:41 KST](../build/WI20261025-stage-two-native/access-restoration.json)
records original TCP22 restored, TCP80/443 and six outbound rules preserved,
provider logout and [task-key removal/empty agent then Stopped/Disabled](../build/WI20261025-stage-two-native/ssh-cleanup.json).
The initial direct-fragment firewall attempt did not persist; MA recovered through
the normal console without further user UI assistance, then changed/restored only
the approved TCP22 rule. Original WI027-held candidate/history remains preserved.

Mail/PG/operating preparation is not activated or fully complete. Next: NCP account
and pricing/authority setup, then approved sender/DNS/API preparation; Toss merchant/
billing application and contract/MID/LIVE-key preparation; effective operating
configuration/deployment/scheduler decisions and bounded validation. User-selected
ID1 mailbox remains the reply/operations destination. Reuse stored references;
request only missing human/account input. New costs and real payment require prior
confirmation. No fresh full backup, disaster recovery or PG operational readiness
is established by these receipts. See [WI003 result](../deliverables/user/WI-20261025-ATS-003-summary.md).

### 2026-10-25 Stage Two Resumed; Minimal Local Recovery Repair in Progress

**The user resumed stage two: repair WI027's two recovery defects locally,
independently review the new candidate, then continue the previously approved
account/grant application and access restoration. Native execution has NOT RUN
at this checkpoint.** See [approved REQ](../deliverables/user/REQ-20261024-ATS-001.md)
and [current WI003 summary](../deliverables/user/WI-20261025-ATS-003-summary.md).

[SE001 repair](../deliverables/agent/WI-20261025-ATS-001-handoff.md) precedes
[PG002 independent review](../deliverables/agent/WI-20261025-ATS-002-handoff.md).
WI027 HOLD remains binding on original candidate
`3fbbec606c60ac73c752d4f082965692f589311a413746a8bb938733a0070b46`;
do not execute it or rewrite its historical evidence. Only a newly pinned,
review-passed candidate may enter fresh probe/inspect and bounded execution.
MA reports the local SSH agent currently Stopped/Disabled; server cold state
has not been freshly observed. Complete technical preparation before human
unlock/access handoff; do not repeat stage one.

The approved target remains ID1/2 ADMIN, ID3 USER, grant1 expiry 2026-11-24 KST
inclusive, with the four allowed column changes and two truthful audit inserts.
Existing plans/billing policy stay unchanged. User-confirmed NCP account absence,
Toss contract not applied and ID1 reply/operations mailbox selection remain the
provider preparation inputs. Mail/PG activation and whole-stage completion are
not established; new costs, real payments and tool-required handoffs retain their
separate boundaries. Historical checkpoints below are preserved.

### 2026-10-24 Astra Local Review Complete; Stage Two on HOLD

**[WI027 independent review](../deliverables/user/WI-20261024-ATS-027-summary.md)
completed with two reproduced recovery defects. Stage-two account/grant writes
remain NOT RUN; candidate `3fbbec606c60ac73c752d4f082965692f589311a413746a8bb938733a0070b46`
must not execute as-is.** This supersedes the prior local-ready/WI025 allowance
and earlier review-not-authorized notes below without changing their history.
Reconciliation reuses an exclusively created retained log, blocking a second
attempt after failure; the grant's write end-date also blocks later read-only
reconciliation. See [exact evidence and minimum repairs](../deliverables/agent/WI-20261024-ATS-027-evidence-pack.md).

Server work stays paused until the user resumes after usage replenishment.
First complete minimal local repairs and independent review, then the required
access handoff, fresh probe/inspect, apply once, original-attempt reconciliation,
independent cold verification and access restoration. No code repair, server,
provider, key, DB/app, payment or new backup operation occurred in WI027.
Stage-one completion and the MA pause receipt remain valid; current server cold
state was not reobserved. Provider/operating preparation remains incomplete and
is separate from account/grant application. Reuse existing evidence and inputs.

### 2026-10-24 Stage Two Approved; Resume Checkpoint Before Remote Access

**Current: [stage two is approved](../deliverables/user/REQ-20261024-ATS-001.md),
but account/grant writes have NOT RUN. Stage-one completion below remains valid.**
The intended changes are ID1 ADMIN promotion, ID2 ADMIN and ID3 USER reuse, and
grant1 expiry extension through 2026-11-24 KST inclusive. No new account, plan
change, billing activation or payment is authorized.

[WI023 local tooling](../deliverables/agent/WI-20261024-ATS-023-evidence-pack.md)
and [WI025 independent review](../deliverables/agent/WI-20261024-ATS-025-evidence-pack.md)
are ready for fresh native gates. Following the user's usage-limit concern, MA
recommended stopping at this clean pre-remote boundary. Temporary TCP22 approval
was not received: no firewall mutation, SSH server access, DB start or native
write occurred. MA's [15:54:23 KST pause receipt](../build/WI20261024-stage-two-native/pause-cleanup-result.json)
records the task key removed, agent empty before Stopped/Disabled, provider
logout observed and original firewall never changed. Current server cold state
was NOT_REOBSERVED because there was no server contact; no fresh PID0 claim is
made. Additional Astra Extra High review has not run or been firmly authorized.

Resume the exact reviewed candidate
`3fbbec606c60ac73c752d4f082965692f589311a413746a8bb938733a0070b46`:
fresh probe -> inspect -> apply -> reconcile -> independent cold verification ->
access restoration, after the required access handoff. Recheck time-sensitive
native gates; do not replay stage one or treat local tests as native proof.

[WI024 provider preparation](../deliverables/agent/WI-20261024-ATS-024-evidence-pack.md)
reuses existing mail, TEST Toss and billing-encryption references. MA's presence-
only check establishes field presence/mode, not validity, MID or LIVE readiness.
The user confirms no NCP account and no Toss merchant/billing application yet;
the existing ID1 mailbox is selected for replies/operations alerts and can be
changed later. Mail/PG activation, new costs and real payments remain pending
their required boundaries. See [WI026 checkpoint](../deliverables/user/WI-20261024-ATS-026-summary.md).

### 2026-10-24 Bounded Stage One Completed; Final Access Restored

**Current: the user-defined stage-one audio and password-reset scope is complete.
WI019 is completed; REQ007 remains active for broader requirements. MA's final
access restoration was observed at 05:50:36 KST.** This supersedes the pending
checkpoints below without changing any historical failure result.

[PG's final local interpretation](../build/WI20261024-018-stage-one-review/final-stage-one-interpretation.json)
accepts BOUNDED_STAGE_ONE_ACCEPTED_WITH_STATED_LIMITS after matching all nine
artifact pins; native/UI observations remain MA-owned.

The second audio API trial and all 13 cleanup checks passed; the user confirmed
MP3 sound and middle seek. Buyer3 completed the real mail-link/password-change,
new-login/logout sequence; the reset attempt also passed all 13 cleanup checks
and independent cold verification. The two audio files remained unchanged after
reset (1,925,002 bytes). Buyer3's old saved password is STALE_USE_FORBIDDEN; the
new password was not saved. See [WI019 final summary](../deliverables/user/WI-20261024-ATS-019-summary.md).

The [final access receipt](../build/WI20261024-stage-one-native/final-access-restoration-result.json)
records app/DB PID 0, original TCP22/443 rules restored, TCP80/other rules unchanged,
owned-key removal and empty agent before Stopped/Disabled, provider logout, agent-
tab closure and user-tab preservation. First-audio FAILED_LEDGER remains FAILED;
later successes are separate attempts. Backup proof covers the captured pre-audio/
reset state, not full disaster recovery or subsequent changes. Browser-player UX
belongs to stage 5 and is NOT_TESTED; this bounded TEST completion is not production
approval, a grant-date extension or authority for further work.

### 2026-10-24 Second Audio API Trial Passed; Human Playback and Reset Pending

**Current: the second audio trial is BOUNDED_AUDIO_API_OBSERVED, track 1,
actors ADMIN 2/entitled USER 3/unentitled USER 1; all 13 cleanup steps PASS.
Human playback and browser-player UX remain unverified; reset is not yet run.
WI019/REQ007 and the approved access session remain active.**

The [audio2 receipt](../build/WI20261024-stage-one-native/audio2-result.json)
supersedes the earlier audio outcome without altering that attempt's
FAILED_LEDGER. MA reports upload/conversion polling, activation, stream/range/seek,
invalid-range and authorization-denial checks, authorized download, a hash-verified
160,958-byte private MP3 copy and a separate fresh runtime-restore PASS.
[Reset code staging](../build/WI20261024-stage-one-native/reset-code-stage-result.json)
verified 44 frozen code files only; reset app/DB were not started and private
inputs not staged. User-approved temporary current-PC TCP22/443 and unlocked
SSH/provider access remain in use for continuous work; no final restoration or
stage-one completion is claimed. See [WI019 evidence](../deliverables/agent/WI-20261024-ATS-019-evidence-pack.md).

### 2026-10-24 Continuous Completion Reaffirmed; Minimal Audio Repair in Progress

**Current: the user explicitly reaffirmed completing stage 1 after the proposed
repair/review/retry. Necessary local correction, exact review and MA continuation
are authorized within the existing scope. The earlier no-retry-approval note
below is historical; ordinary tool corrections need no new scope approval.**

[SE021](../deliverables/agent/WI-20261024-ATS-021-handoff.md) prepares the minimal
audio/header-diagnostic repair in fresh attempt paths, then PG018 performs exact
review, MA continues bounded execution and Doc019 records actual results.
The old HOLD/FAILED_LEDGER and completed restoration remain preserved; no new
candidate/native outcome is claimed. Reuse the JAR/WAV/actors/backup proof;
no automatic replay after uncertain mutation, new backup or old-claim deletion.
The fixed 2026-10-24 grant day, 0-KRW extra-paid budget, payment/public-access
exclusions, direct human reset entry and action-time browser security gates
remain. WI019/REQ007 stay active; real audio/reset completion is still required.

### 2026-10-24 Audio Held at First ADMIN Login; Runtime and Access Restored

**Current: audio is HOLD; its aggregate cleanup remains FAILED at LEDGER
(12 other steps PASS). Later independent runtime verification and final access
restoration passed. Backup2/isolated verification remains PASS. Stage 1 and
WI019/REQ007 remain active, blocked before audio/reset completion.**

The [audio result](../build/WI20261024-stage-one-native/audio-hold-result.json)
records JAVA_RELEASED but no audio-result/final output. The
[stored baseline-to-first-login delta](../build/WI20261024-stage-one-native/audio-delta-result.json)
contains only ID2 refresh_token/updated_at; the login-success marker is false.
ID1/ID3 login and upload were not reached; reset was not run. This is not a later
SQL re-query or accepted login result. At 05:05:21 KST, MA's
[access receipt](../build/WI20261024-stage-one-native/access-restoration-result.json)
confirmed app/DB PID 0, original TCP22/443 rules, unchanged TCP80, owned-key removal,
empty agent then Stopped/Disabled, provider logout and agent-tab closure with
the user tab preserved. The failed aggregate is not relabeled PASS.

The cause is UNCLASSIFIED; duplicate Vary rejection is only a hypothesis.
Focused offline reproduction/review must precede any proposed retry; no server
retry is approved. See [WI019 current evidence](../deliverables/agent/WI-20261024-ATS-019-evidence-pack.md).

### 2026-10-24 Current Capture and Isolated Verification Passed; Audio/Reset Pending

**Current: the user-approved single retry passed current capture and isolated
verification: 43 tables/15 rows, snapshot IDENTIFIED, stage COMPLETE. All four
DB cleanup checks passed. WI019/REQ007 remain active; audio/reset and final
access restoration are not yet complete.** This supersedes the first capture
HOLD as the latest outcome while retaining that failed attempt below.

MA reports inventory4 again passed with 381 members/73,105,118 source bytes.
The [refreshed cost bound](../build/WI20261024-stage-one-native/cost-bound-second-result.json)
remains 252,828,618 retained bytes within the current included allowance and
0-KRW additional-paid limit, conditional on current tariffs and guards. The
[second capture receipt](../build/WI20261024-stage-one-native/capture2-result.json)
records app/business mutation NOT_RUN and full_disaster_recovery=false.
MA is proceeding to independent cold/exact backup-proof checks and audio setup;
their outcomes and session access restoration are still pending.
See [WI019 current evidence](../deliverables/agent/WI-20261024-ATS-019-evidence-pack.md).

### 2026-10-24 Inventory and Current Cost Bound Passed; Capture Stopped Before Backup

**Current: inventory3 passed with 381 members/73,105,118 source bytes. The first
remote capture stopped at SOURCE_SQL_WINDOW, snapshot NOT_ATTEMPTED. Source DB
was temporarily started and then stopped; all four cleanup checks and a separate
cold probe passed. WI019/REQ007 remain active; access stays open for the approved
session.** Earlier "DB not started" notes apply only to earlier attempts.

The reviewed current cost bound totals 252,828,618 retained bytes including a
100,000,000-byte growth reserve, within the 1,000,000,000-byte included allowance.
MA's current account/terms observations support this run's 0-KRW additional-paid
bound; PG found the public arithmetic/evidence consistent. This is not a permanent
tariff guarantee or authority receipt. Actual restic config/snapshot queries
succeeded, but capture's third SQL query failed with 1046: unqualified account
tables had no selected database context. SE017's minimal context repair and
PG018 remaining-stage review are underway. No dump/new backup/isolated restore/
app/audio/reset occurred. See [WI019 current evidence](../deliverables/agent/WI-20261024-ATS-019-evidence-pack.md)
and [separate cold probe](../build/WI20261024-stage-one-native/capture-post-cold-probe.json).

### 2026-10-24 Stage-One Interim Hold; Access Session Continues

**Current: WI019/REQ007 remain active; stage 1 is not complete.** MA applied
the user's action-time temporary TCP22/443 current-PC access approval and
unlocked SSH. The first inventory stopped at BOOTSTRAP_HOLD before staging.
After the user's explicit one-retry approval, the second attempt staged tools
only and stopped at NATIVE_AUTHORITY_OR_DISPATCH_HOLD. Read-only diagnosis
identified UNREVIEWED_SOURCE_FILE: the adapter mixed a current WI067 release
root/JAR-only allowance with the old WI060 frontend, omitting 143 current
frontend files from the allowlist. SE017's minimal overlay
repair and PG018 exact review are in progress.

No DB/app/backup/restore/audio/reset was attempted. A separate
[hold cold probe](../build/WI20261024-stage-one-native/hold-cold-probe.json)
returned PREFLIGHT_PASS with cold before/after PASS. Access is still open for
the approved session; restoration is not yet claimed. Earlier attempts and
staged artifacts are preserved. See [WI019 interim evidence](../deliverables/agent/WI-20261024-ATS-019-evidence-pack.md).

### 2026-10-24 Continuous Stage-One Completion Approved

**Current: the user authorized continuing stage 1 through completion, with
progress updates and without repeated routine approvals.** Stage 1 covers
audio upload/conversion/playback/seek, authorized original download and denial
checks, plus a separately identifiable reset-link/password-change test. Necessary
current-fixture backup/isolated restore is included. This supersedes older
read-only-only and separate stage-one execution waits within this scope.
Preparation/review and actual results are pending; WI019 and REQ007 remain active.

The chain is [SE017 minimal readiness/recipe](../deliverables/agent/WI-20261024-ATS-017-handoff.md)
-> [PG018 exact independent review](../deliverables/agent/WI-20261024-ATS-018-handoff.md)
-> MA bounded native operations and cleanup ->
[DocOps019 actual-result report](../deliverables/agent/WI-20261024-ATS-019-handoff.md).
Reuse reviewed tools/JAR/actors/WAV; verify changed bytes only. MA alone operates
provider/server/private references. Human credential entry and action-time
browser security confirmation remain required boundaries, not routine scope
reauthorization. Prepare prerequisites before unlock and retain access only for
this authorized work session.

Preserve ID1 and pre-existing business data with exact reviewed session/password
effects stated separately. Reset actor/UI/mail path and safe saved-credential
update must be resolved before execution, without choosing an actor implicitly.
Extra paid budget remains 0 KRW; unknown/positive cost stops capture. No automatic
grant-date extension, public access, purchase/payment, unrelated deletion/schema
changes or stages 2-6. Local PASS never establishes real audio/reset completion.
See [verbatim approval](../deliverables/user/REQ-20261011-ATS-007.md) and
[WI019 initial evidence](../deliverables/agent/WI-20261024-ATS-019-evidence-pack.md).

### 2026-10-24 Read-only Backup Preflight and Access Restoration Complete

**Current: WI-20261024-ATS-016 is complete. MA observed
READONLY_PREFLIGHT_AND_ACCESS_RESTORE_PASS at 2026-10-24 04:02:04 KST.**
Host credential/password stage references, PC escrow comparison and historical
repository/restore binding matched. Pre/after/final cold observations passed
without DB/app start or remote staging. Original TCP22 was restored, 80/443
unchanged, owned key removed and agent empty before Stopped/Disabled, provider
logout verified and the agent-created tab closed; the user's tab was preserved.
See [safe completion receipt](../build/WI20261024-backup-preflight-native/completion.json).

MA checked official tariffs before private-bucket metadata: API temporarily
free, provider-internal traffic free under the documented condition. Account
UI showed nine objects/60.6 MB, storage and traffic allowances of 1 GB each and
estimated storage cost 0 KRW excluding VAT, delayed about one hour. This is not
a future backup quote; rounded/delayed zero counters do not prove no prior calls.
Live cloud cryptographic identity and current fixture recovery remain unverified;
future backup zero additional cost is NOT_ESTABLISHED.

PG's exact local review covered SE6+PG4 focused checks; native/UI/OS facts belong
to MA. No backup/restore/grant change/login/audio occurred. REQ007 remains active.
Reuse the [WI013 next-execution plan](../deliverables/user/WI-20261024-ATS-013-summary.md)
only with separate actual approval and a supported cost limit; the current
additional-paid budget remains 0 KRW. See [WI016 final evidence](../deliverables/agent/WI-20261024-ATS-016-evidence-pack.md).
No further native action follows this completed read-only stage.

### 2026-10-24 Read-only Server, Repository, Password Reference and Cost Check Approved

**Current: the user approved read-only prechecks of the server, existing
repository, password-storage reference and additional cost, followed by access
restoration. WI-20261024-ATS-016 and REQ007 remain active; actual observations
are pending. DB start is not authorized.**

The chain is [SE014 bounded read-only preparation](../deliverables/agent/WI-20261024-ATS-014-handoff.md)
-> [PG015 exact review](../deliverables/agent/WI-20261024-ATS-015-handoff.md)
-> MA provider/server read-only checks and access restoration ->
[DocOps016 report](../deliverables/agent/WI-20261024-ATS-016-handoff.md).
MA checks provider billing/allowances first. Unknown fees HOLD potentially billed
cloud/API/metadata queries before dispatch; approved host-only inspection is
separate. Read-only does not itself mean free.

Use the narrow reviewed probe, not WI011 remote staging/capture. No new backup,
isolated restore, grant-date change, app/login/audio, deletion, purchase or
configuration change is approved, apart from separately authorized temporary
access restoration. Agents do not access the provider, server or private files.
Previous local backup/audio preparation remains complete; it supplies no native
backup/audio permission or current recovery PASS. See [verbatim approval](../deliverables/user/REQ-20261011-ATS-007.md)
and [WI016 initial evidence](../deliverables/agent/WI-20261024-ATS-016-evidence-pack.md).

### 2026-10-24 Existing-Repository Backup Preparation Reviewed Locally

**Current: WI-20261024-ATS-013's local preparation/report scope is complete.**
The exact 19-member candidate passed SE10 + independent PG6 focused checks;
11 helpers, Source643 and retained JAR were preserved. PG issued
ALLOW_EXACT_LOCAL_PREPARATION_ONLY with no local blocker and native_authorized=false.
MA's final exact-member/pin check passed. No server/provider/private input,
backup/restore, app/login/audio or grant-date mutation occurred.

The final adapter reuses the existing repository without init and verifies only
the **new snapshot** through file/hash/type/mode/owner/empty-directory checks and
complete SQL/strict equivalence. It excludes repository-wide `check --read-data`;
the earlier cost draft below describes a superseded broader check. Actual cost
is unknown: current additional paid budget is 0 KRW, not a zero-cost quote.
Later genuine evidence must cover metadata/upload/new-snapshot restore/requests/
tax and continuing retained storage; unknown or positive cost means HOLD.
No automatic purge/expiry or billing cap follows from rate limits.

Current fixture recovery coverage remains NOT_ESTABLISHED. Future native work
requires exact user approval, fresh target/inventory, existing repository/escrow
binding, verified cost basis and separate cleanup/access evidence. The grant
stays dated 2026-10-24; a later date proposal preserves started_at and changes
expires_at only to explicit D, plus updated_at/truthful audit, if separately
approved and implemented. This would extend entitlement through D, not activate
it only on D; a D-only trial gate is separate. See [WI013 final scope and limits](../deliverables/user/WI-20261024-ATS-013-summary.md)
and [exact evidence](../deliverables/agent/WI-20261024-ATS-013-evidence-pack.md).
REQ007 remains active; no native WI is auto-started.

### 2026-10-24 Local Backup Tool Preparation and Review Approved

**Current: the user approved minimal local backup-tool preparation and
independent review, including existing-repository reuse and a method to confirm
additional cost. WI-20261024-ATS-013 and REQ007 remain active; implementation
and exact review are pending. No server execution is authorized.**

The chain is [SE011 minimal preparation](../deliverables/agent/WI-20261024-ATS-011-handoff.md)
-> [PG012 exact review](../deliverables/agent/WI-20261024-ATS-012-handoff.md)
-> [DocOps013 approval report](../deliverables/agent/WI-20261024-ATS-013-handoff.md).
The next proposal must reuse the existing repository without init, preserve old
snapshots/evidence, and separate current fixture backup and isolated restore
from historical proof. New backup/restore and grant-date changes require later
exact approval. The current grant stays on 2026-10-24 KST; no automatic extension.

No private/provider/server/SSH/DB/cloud access, credentials, app/login/audio,
payment or deletion is authorized now. Cost preparation describes verification
of the actual billing terms, included capacity and bounded incremental usage;
no price, zero-cost conclusion or hard monetary cap is invented. Retained
`check --read-data` reads the full existing repository, and upload/download
rate limits are not byte or cost caps. No automatic purge/retention expiry is
authorized. See
[REQ approval and draft next scope](../deliverables/user/REQ-20261011-ATS-007.md)
and [WI013 initial evidence](../deliverables/agent/WI-20261024-ATS-013-evidence-pack.md).
Prior local audio preparation and the bounded read-only query remain complete;
current fixture recovery coverage remains NOT_ESTABLISHED.

### 2026-10-24 Read-only Query, DB Stop and Access Restoration Complete

**Current: WI-20261024-ATS-010 is complete. MA observed
READONLY_QUERY_AND_ACCESS_RESTORATION_PASS at 2026-10-24 02:58:43 KST.**
The inventory and all four DB cleanup checks passed; a separate cold post-probe
passed. Original TCP22 was restored, 80/443 unchanged, owned key removed with
agent empty, SSH agent Stopped/Disabled, provider login-required confirmed and
temporary tab closed, private ACL PASS. See [MA final receipt](../build/WI20261024-readonly-evidence-native/resume-completion.json).
This supersedes the pending restoration checkpoint below.

USER1 is unentitled, ADMIN2 matches, and USER3 has the one active plan1 quota5/day
grant, MONTHLY/CANCELLED with start=expiry=2026-10-24. Passwords were not queried
and login was not tested. Historical metadata validated within four fixed files;
current fixture backup coverage remains NOT_ESTABLISHED, without a newer-evidence
search or a claim that no other backup exists. No app/login/data changes/new
backup/restore/audio occurred. Local audio preparation remains complete, actual
audio unapproved, and REQ007 active. The grant is limited to 2026-10-24 KST;
date extension needs explicit change approval.

[WI010 final evidence](../deliverables/agent/WI-20261024-ATS-010-evidence-pack.md)
separates PG's exact local allowance from MA's native/UI/OS observations; no PG
native audit is claimed. No additional server action, review or test follows.

### 2026-10-24 Read-only Inventory Complete; Access Restoration Pending

**Current: MA's resumed read-only inventory is INVENTORY_OBSERVED / PASS.
DB STOP/PID_ZERO/SOCKET_GONE/BASELINE all passed, followed by a separate
PREFLIGHT_PASS / COLD_QUARANTINE_MATCH post-probe. Access restoration is still
pending; WI010 and REQ007 remain active.** This supersedes the earlier no-contact
checkpoint for this operation, without altering historical records.

The [safe inventory](../build/WI20261024-readonly-evidence-native/resume-inventory-600-public.json)
confirms USER1 without subscriptions, ADMIN2 without subscriptions and USER3
with one effective grant: active INDIVIDUAL plan1, quota5/day,
MONTHLY/CANCELLED, start=expiry=2026-10-24, pending fields NULL. No pending audio
or unfinished storage was observed. Password state was not queried; login was
not run. The four fixed historical backup metadata files validated, but current
fixture coverage is NOT_ESTABLISHED. No newer evidence search occurred, so this
is not a claim that no current backup exists.

The exact reviewed tool passed 14 focused local checks (SE8+PG6); old29 were
not rerun. Earlier 900-second-expiry GUARD HOLD attempts did not start DB.
The same tool with an allowed 600-second expiry passed cold confirmation and
completed the first DB run. A clock/upper-expiry explanation remains an inference.
No app/login/data writes/new backup/restore/audio occurred. Local audio
preparation remains complete; native audio remains unapproved. See
[WI010 current evidence](../deliverables/agent/WI-20261024-ATS-010-evidence-pack.md).

### 2026-10-24 Read-only Account, Grant and Backup-Evidence Check Approved

**Current: the user approved server contact, temporary DB start/stop and a
read-only check of accounts, grants and existing backup evidence, followed by
DB stop and access restoration. WI-20261024-ATS-010 and REQ007 remain active;
actual results are pending.** At this initial checkpoint MA reports no server
contact and SSH agent Stopped/Disabled. Local audio preparation remains complete;
actual audio execution is still unapproved.

The chain is [SE008 bounded read-only preparation](../deliverables/agent/WI-20261024-ATS-008-handoff.md)
-> [PG009 exact review](../deliverables/agent/WI-20261024-ATS-009-handoff.md)
-> MA approved query, DB stop and access restoration ->
[DocOps010 result integration](../deliverables/agent/WI-20261024-ATS-010-handoff.md).
Human unlock follows technical readiness. The planned bounded DB lifecycle is
300 seconds with 900-second approval expiry; no automatic full rerun.

This approval excludes app execution/login, business-data or schema changes,
new backup/restore, audio, payment, mail and deletion. Historical backup proof
and current fixture coverage must remain distinct; a read-only evidence check
cannot create current backup acceptance. MA alone operates the target;
DocOps/subagents do not read credentials or private input pointers/values.
See [REQ scope](../deliverables/user/REQ-20261011-ATS-007.md) and
[WI010 initial evidence](../deliverables/agent/WI-20261024-ATS-010-evidence-pack.md).

### 2026-10-24 Local Audio Wiring and Private Capture Complete

**Current: WI-20261024-ATS-007 is complete for local preparation only.**
SE005 and PG006 completed the exact seven-member candidate review with 17
focused checks (SE 7 Python + 1 Save; PG 8 Python + 1 Save), no local blocker,
and ALLOW_LOCAL_CAPTURE_AND_PREPARATION_ONLY. MA observed saved input and ACL/
actor-frame/saved-account merge PASS at 2026-10-24 01:54:50 KST; the user
confirmed save and window closure. Actual ID1 identity/login remains unverified.
This supersedes the pending local checkpoints below, without changing their history.

The [MA local readiness receipt](../build/WI20261024-audio-local-preparation/local-readiness.json)
records no server contact, native authorization false, source scoped status
clean and SSH agent Stopped/Disabled. No server/app/login/audio/payment or MP3
transfer/listening occurred. DocOps did not read private values or pointers.
See [WI007 final evidence](../deliverables/agent/WI-20261024-ATS-007-evidence-pack.md)
for candidate/review pins and observation ownership.

Stage 1 overall and REQ007 remain open. Native execution still needs explicit
user/data permission, genuine exact native PG allowance, current target/day/
expiry binding and backup/recovery coverage of the current fixture state.
WI062's historical 43-table/6-row, audio-empty proof predates the fixtures and
excludes runtime keys; it is not current recovery acceptance. Missing coverage
means HOLD, without fabricating a receipt or adding backup operations. The
[concise next-scope draft](../deliverables/user/WI-20261024-ATS-007-summary.md)
retains the ID1-inclusive persistent session exception, one retained active
track/WAV/MP3 and exact new rows, conditional one-MP3 delivery, and the
2026-10-24 KST grant boundary. No native follow-up is auto-triggered.

### 2026-10-24 Local Audio Wiring and Dedicated Input Approved

**Current: the user approved completing and independently reviewing the remaining
local audio wiring, with necessary inputs collected through a dedicated window.
No server execution is approved. Preparation/review and MA capture outcomes are
pending; stage 1 is not complete. WI-20261024-ATS-007 and REQ007 remain active.**

The chain is [SE005 local implementation](../deliverables/agent/WI-20261024-ATS-005-handoff.md)
-> [PG006 exact review](../deliverables/agent/WI-20261024-ATS-006-handoff.md)
-> MA dedicated local input after review -> [DocOps007 integration](../deliverables/agent/WI-20261024-ATS-007-handoff.md).
Reuse stored ADMIN2/buyer3 inputs and accept ID1's existing password unchanged,
under the existing-login 1..256/no-NUL contract, without new-signup restrictions
or reset. Subagents do not read private pointers or input values.

Retain frozen candidates and earlier evidence. Native user/PG/target/backup
receipts require genuine provenance and current binding; local approval cannot
create them. No network, service/DB/app, login/audio/payment, reset or date/grant
extension is authorized by this work. See [verbatim approval](../deliverables/user/REQ-20261011-ATS-007.md)
and [WI007 initial evidence](../deliverables/agent/WI-20261024-ATS-007-evidence-pack.md).

### 2026-10-24 Audio Reuse Audit Complete; Native Approval Proposal Only

**Current: local identity reuse PASS; WI-20261024-ATS-004's local audit/proposal
is complete. No server contact, service state changes, private-value reads or
audio execution occurred in this work. Native audio remains unapproved and
not ready to execute.** MA
reverified the 68-member audio candidate, 62-member base, 53 copied dependencies,
Source643, retained JAR and WAV. The existing audio verdict is
LOCAL_HASH_LINKAGE_PREPARATION_PASS, not a native audio allowance.

The proposed actors are ADMIN 2, entitled USER 3 and negative USER 1. Three
password logins require an explicit exception for refresh_token and possibly
updated_at on those exact users, including ID 1; all other pre-existing cells
stay preserved. A changed refresh-token hash replaces the previous refresh
session, without establishing immediate invalidation of an existing access
token. The proposal adds one synthetic track, WAV/MP3 files, four storage journal
rows, one activation, one official WAV download, one license/download row and
download_count 0 to 1. Session changes persist after app shutdown, without
automatic logout, token reissue or restoration of ID 1's old refresh value.
The active trial row and WAV/MP3 are retained. No role/grant/payment changes or
data deletion.

ID 1 credentials, a reviewed private caller/transport, fresh exact native PG/
target/user receipts and the current backup/isolated-restore receipt binding
remain unverified. ID 1 must use its existing password unchanged, without reset.
Existing backup success is not denied. The optional verified
single-MP3 private copy and local listening/seek are a separate proposed phase,
not browser-player UX or a new public endpoint. The next approval may include
one ledger/hash-matched MP3 copied to an owner-only user-PC path; MA checks the
actual file against those conditions without repeating an unchanged approval.
No transfer is currently approved. External HTTPS quarantine stays
in place. Date rollover beyond the 2026-10-24 grant stops the trial; no extension.
See the [Korean draft approval](../deliverables/user/REQ-20261011-ATS-007.md) and
[WI004 readiness limits](../deliverables/user/WI-20261024-ATS-004-summary.md).
REQ007 remains active; no implementation or native follow-up is auto-triggered.

### 2026-10-24 Exact Fixture Applied and Restoration Completed

**Current: APPLIED / COMMIT ACKNOWLEDGED, observed by MA at
2026-10-24T00:47:20+09:00. New ADMIN user 2, buyer USER 3, audit 1 and grant 1
were created with the exact approved effects. Inspection and apply each passed
all five DB cleanup checks; the separate cold post-probe and access restoration
passed. PG accepted the bounded operation. WI-20261024-ATS-003 is complete;
REQ007 remains active for separate app/login/audio work.** This actual outcome
supersedes the earlier local-readiness and unexecuted checkpoints below.

The buyer entitlement uses plan 1, quota 5/day, MONTHLY/CANCELLED,
start=expiry=2026-10-24 and NULL pending fields. It is limited to that KST day;
no date extension or replay is authorized. Verified flags are TEST-only, without
email-verification, consent or credential-login evidence. Existing ID 1 and all
pre-existing row/cell preservation are supported by the reviewed exact-delta
transaction and acknowledged commit, not a separate post-commit SQL reread.

The [final restoration](../build/WI20261024-fixture-apply-native/final-restoration.json)
records original TCP22 restored, 80/443 unchanged, provider logout, created tabs
zero, owned-key removal, identities zero, SSH agent Stopped/Disabled, closed
key-input window and private ACL PASS. MA owns native/UI/OS observations; PG
interpreted sanitized receipts. No app/startup, login, audio, mail/consent,
payment/refund, schema change, deletion or automatic write retry occurred.
See [WI003 final result](../deliverables/user/WI-20261024-ATS-003-summary.md).
Source/JAR preservation is MA-reported; the original Save failure remains unknown.

### 2026-10-24 Local Apply Preparation Reviewed; Access Confirmation Pending

**Current: SE001 preparation and PG002 exact local review are complete with
ALLOW_EXACT_APPROVED_SINGLE_FIXTURE_APPLY. SE 7 + PG 8 focused checks PASS.
MA's local preflight passed. Provider login and the TCP22 proposal are prepared,
but rule submission, SSH, DB and writes have not occurred at this checkpoint.
The specific temporary TCP22 change awaits the current user confirmation required
at the browser security-action boundary. WI003 and REQ007 remain active.**

Candidate SHA-256 is `f42d9cfe325f8105d908db46f67b5fa93bc72ada6178eb878af8f75a2f5f7719`;
the [PG002 receipt](../build/WI20261024-002-fixture-apply-review/review-receipt.json)
binds that exact candidate. Source643/JAR and frozen WI005/011/014 are preserved.
The [MA local preflight](../build/WI20261024-fixture-apply-native/local-preflight.json)
records input private-storage/ACL checks, server_contact=false and SSH agent
Stopped/Disabled. These local results do not establish a current native
inspection, apply or cleanup result.

Actual execution still requires MA's current target/approval gates, fresh
inspection with all five cleanup checks, then a single bounded apply on
2026-10-24 KST only. Exact approved effects and exclusions below remain unchanged;
no app/login/audio/payment or automatic date renewal. See
[WI003 current checkpoint](../deliverables/user/WI-20261024-ATS-003-summary.md).

### 2026-10-24 Bounded Account and Entitlement Writes Approved

**Current: the user approved the exact account/entitlement change for
2026-10-24 Asia/Seoul after local preparation, independent review and a fresh
passing inspection, followed by DB shutdown and access restoration. Preparation
and review are pending results; no new inspect/apply or cleanup success is
claimed. WI-20261024-ATS-003 and REQ007 remain active.** Stop if the KST date
changes; no automatic date extension.

Preserve ID 1 and every pre-existing row/column. The only approved writes are
two new USER inserts, one newly inserted uploader's USER-to-ADMIN update, one
audit insert and one new-buyer subscription insert. Use existing active
INDIVIDUAL plan 1 with quota 5/day; MONTHLY/CANCELLED, start=expiry=2026-10-24,
pending fields NULL. Verified flags are TEST fixture semantics only, without
mail-verification or human-consent events. App/startup, login, audio, payment,
billing/renewal and schema changes remain excluded.

The chain is [SE001 local preparation](../deliverables/agent/WI-20261024-ATS-001-handoff.md)
-> [PG002 exact review](../deliverables/agent/WI-20261024-ATS-002-handoff.md)
-> MA fresh inspection, bounded apply and DB/access cleanup
-> [DocOps003 integration](../deliverables/agent/WI-20261024-ATS-003-handoff.md).
MA reuses saved private inputs; old evidence/approvals and frozen candidates
remain preserved. The retained-path collision requires the reviewed local
preparation; approval is not permission to delete or overwrite old evidence.
An uncertain commit stops writes without automatic replay. See
[verbatim approval](../deliverables/user/REQ-20261011-ATS-007.md) and
[initial WI003 evidence](../deliverables/agent/WI-20261024-ATS-003-evidence-pack.md).
WI013's completed read-only result below remains dated evidence, not fresh
authority or current inspection for this apply.

### 2026-10-23 Saved-Input Inspection and Restoration Completed

**Current: INSPECTION_RECORDED / INSPECTED_CONDITIONS_PASS. Saved inputs were
reused without recollection. DB_SESSION, STOP, PID_ZERO, SOCKET_GONE and BASELINE
all PASS, followed by an independent target-bound PREFLIGHT_PASS /
COLD_QUARANTINE_MATCH. Access restoration is confirmed. WI013 is complete;
REQ007 remains active for separately approved date/write/audio work.**

Selected-input identity collisions, ADMIN count and existing work are zero.
ID 1 passes the preserved unentitled USER guard; credentials/login remain
unverified. Eligible active finite INDIVIDUAL plans are ID 1 (5/day) and ID 2
(20/day). Plan 1 is the smallest sufficient recommendation for a later proposed
grant, not a selection or applied entitlement. Date/apply remain false; no
account/business changes, app/login or audio occurred.

MA restored the original saved TCP22 rule, left 80/443 unchanged, logged out of
the provider and closed its created tabs. Owned key removal, zero identities,
SSH agent Stopped/Disabled, closed key-input window and final private ACL PASS
are confirmed in the [restoration receipt](../build/WI20261023-fixture-inspect-resume/final-restoration.json).
PG independently accepted the sanitized inspection/DB-cleanup/restoration receipts;
MA owns native/private-target and provider observations.

Future preparation proposes two new accounts, one new-account ADMIN promotion
with audit, and one non-billing buyer entitlement while preserving ID 1 and all
existing rows. No date is selected. Retained fixed approval/evidence files cause
the wrapper's staging collision guard to stop a later unchanged run; a future
inspection/apply needs a reviewed approval/collision-handling plan and applicable
approval. No automatic rerun, renewal, deletion or replacement is authorized.
See [WI013 final result and remaining gates](../deliverables/user/WI-20261023-ATS-013-summary.md).
The earlier Save failure remains unexplained and all earlier checkpoints remain historical.

### 2026-10-23 Read-Only Inspection with Saved Inputs Reapproved

**Current: the user explicitly approved MA proceeding with the saved inputs for
read-only inspection, including server access and temporary DB start/stop,
followed by restoration confirmation and reporting. No input recollection is
needed. Actual server contact and inspection have not occurred at this
checkpoint. WI013 and REQ007 remain active.** Date/apply, account/business
changes, app/login and audio remain excluded.

MA's local preflight b0bd47 reported zero Source643 mismatches, retained JAR
match, exact frozen WI005/011/014 trees (22/5/4 members), exact PG012 receipt,
and saved-input existence/restricted ACL. Branch main/HEAD 91a16e7 was retained;
the current client source was unchanged and SSH agent was stopped. These are
local prerequisites, not a new server or inspection result. No private values,
file body or private digest are included in this record.

MA will coordinate applicable provider-action and owned-key boundaries, then
fresh target/approval records, staging, bounded inspection and cleanup. Earlier
capture and restoration results stay historical until new operation receipts
arrive. See [verbatim renewed approval](../deliverables/user/REQ-20261011-ATS-007.md)
and [WI013 execution checkpoint](../deliverables/agent/WI-20261023-ATS-013-evidence-pack.md).

### 2026-10-23 Local Private Capture Completed; Inspection Pending

**Current: local private capture is COMPLETE. The user confirmed successful
Save and window closure; MA verified file existence and restricted ACL using
the reviewed verifier. The capture process is absent. Inspection remains
NOT_RUN and this operation has not started the DB. WI013 and REQ007 remain
active.** No server contact occurred for this completion check.

MA tool 09417c used metadata/ACL checks only: private input exists, ACL verification
exit 0 / PRIVATE_ACL_OK, process 22096 absent, and SSH agent Stopped/Disabled.
No input values, file body or private digest were read or output. Previously
confirmed access restoration remains the last access state. This actual local
capture outcome is distinct from the prior synthetic tests; it does not resolve
the unknown cause of the original Save failure.

Date/apply, account/business changes, app/login and audio remain unapproved and
unperformed. Subsequent inspect retains its fresh target, exact approval, expiry,
staging and cleanup gates. See [WI013 current outcome](../deliverables/user/WI-20261023-ATS-013-summary.md)
and [attributed capture evidence](../deliverables/agent/WI-20261023-ATS-013-evidence-pack.md).
Earlier failure, repair and waiting checkpoints below remain historical.

### 2026-10-23 Local Capture Repair Reviewed; Input Result Pending

**Current: WI014/WI015 local repair and exact independent review are complete,
with ALLOW_EXACT_LOCAL_CAPTURE_REPAIR_ONLY. MA confirmed the revised Korean
local-only input window is open under the existing private-input approval.
Actual Save is not yet performed; WI013 awaits human input and inspection remains
NOT_RUN. WI013 and REQ007 remain active.** No server work resumed for this repair
or relaunch; the previously confirmed access restoration remains the last state.

The [four-member candidate](../build/WI20261023-014-capture-repair/candidate.json)
is bound by the [PG015 receipt](../build/WI20261023-015-capture-review/review-receipt.json).
SE's seven focused checks and PG's four independent checks passed; PG also reran
all seven SE checks. The actual .NET Click callback reached protected synthetic
storage. Korean actor/field messages, fixed runtime/storage error codes and
retention of entries on error improve diagnosis. The original callback also
passed synthetic protected storage, so the earlier real failure remains
UNCONFIRMED and is not attributed to the user's values.

The first helper had no window handle. MA stopped only that owned helper and
relaunched once with the documented visible Normal setting, before any user
entry attempt. Process presence, Korean title and a nonzero window handle were
confirmed. This local launch-setting correction is separate from the unresolved
original Save failure; no private values were read.

Local input capture now precedes the remote 900-second timer. This changes only
preparation order: later inspect still requires fresh target, exact approval,
expiry, staging and cleanup gates. Prior WI011/WI005 candidates and WI012's
bounded scope are preserved. Actual date/apply, app/login and audio remain
unapproved. See [WI013 current results](../deliverables/user/WI-20261023-ATS-013-summary.md);
the Save failure, cold baseline and access restoration below remain historical.

### 2026-10-23 Fixture Staging Completed; Private Input Save on Hold

**Current: exact tool staging reached INSPECT_STAGING_READY, but the dedicated
input window rejected Save with a generic VALIDATION error. The latest
metadata-only check found no input file and no capture process. Inspection is
NOT_RUN; this operation never started
the DB. Readiness is on HOLD pending local diagnosis/repair. WI013 and REQ007
remain active.** The process disappearance cause and closer are unknown; no
private input body was read for documentation.

MA's [cold post-stage probe](../build/WI20261023-fixture-inspect-native/post-stage-probe.json)
is PREFLIGHT_PASS with inventory NOT_RUN and DB-started false. Original TCP22
source restoration was confirmed by saved-rule readback; 80/443 are unchanged.
Provider logout and zero remaining created browser tabs were observed. The owned
SSH key was removed, identities are zero and fresh service readback is
Stopped/Disabled. The GUI had initially been deliberately preserved; MA's later
metadata-only check found its process absent and no input file. This is not an
all-windows-closed or input-erasure claim. No date/apply, business DML, app/login
or audio occurred.

Earlier local tests/review remain scoped evidence. A synthetic invocation of the
actual .NET Click Save callback passed JSON/validation/mock storage, and PG's
narrow JSON checks passed. The real failure cause is unresolved: the generic
catch combines confirmation, field-validation and runtime failures, and earlier
coverage had not exercised that callback. The bounded next chain is
[SE014 local diagnostic repair](../deliverables/agent/WI-20261023-ATS-014-handoff.md)
-> [PG015 exact independent review](../deliverables/agent/WI-20261023-ATS-015-handoff.md)
-> WI013 integration under existing preparation approval. This does not touch
the real input window, launch another window or resume server work. Source/JAR
preservation is MA-reported. See [WI013 current outcome](../deliverables/user/WI-20261023-ATS-013-summary.md)
and its [sanitized cleanup record](../deliverables/agent/WI-20261023-ATS-013-evidence-pack.md).
The approval and earlier successful reads below remain historical checkpoints.

### 2026-10-23 Private Fixture Inputs and Read-Only Inspection Approved

**Current: the user agrees to the future fixture scope and verified TEST flags,
but authorizes only server access, exact tool staging, temporary DB start/stop,
dedicated-window private input capture/retention, private evidence and read-only
inspection. The actual date and writes remain explicitly deferred. Preparation
and review are in progress; no new inspection result is claimed. WI013 and
REQ007 remain active.** Application start/login and audio remain excluded.

The chain is [SE011 capture/inspection preparation](../deliverables/agent/WI-20261023-ATS-011-handoff.md)
-> [PG012 review](../deliverables/agent/WI-20261023-ATS-012-handoff.md)
-> MA private capture, approved staging/inspect/restoration
-> [DocOps013 integration](../deliverables/agent/WI-20261023-ATS-013-handoff.md).
The exact frozen fixture candidate is reused. Existing ID 1 and all existing
rows remain immutable; agreement to future effects is not apply authority.

Real inputs are received only through the dedicated window after technical
readiness and remain in access-restricted private storage/transport. No secrets,
private input digests or private snapshots belong in public reports. New
account creation, ADMIN promotion/audit and entitlement grant are not authorized
now. No execution date is selected by this document. Fresh inspection is still
required, and its maximum 900-second age means a later apply may need a new
inspection under its exact authorization. Earlier successful reads/restoration
remain dated evidence. See [verbatim approval](../deliverables/user/REQ-20261011-ATS-007.md)
and [WI013 in-progress evidence](../deliverables/agent/WI-20261023-ATS-013-evidence-pack.md).

### 2026-10-23 Read-Only Account and Plan Recheck Completed

**Current: INVENTORY_OBSERVED / inventory PASS; STOP, PID_ZERO, SOCKET_GONE and
BASELINE all PASS, plus a target-bound post-stop PREFLIGHT_PASS. Original TCP22
source restored, other rules unchanged, provider logged out and key removed;
SSH agent Stopped/Disabled. Input-window closure remains unconfirmed. WI010 is
complete; REQ007 remains active.** See [native inventory](../build/WI20261023-recheck-native/inventory-result.json)
and [final restoration](../build/WI20261023-recheck-native/final-restoration.json).

The only account is ID 1, designated USER, verified/nondeleted with a local
password present. Total subscription rows and effective subscriptions are both
0; pending audio/unfinished storage are 0. Credentials and actual login remain
NOT_CHECKED. Six plans exist: active finite INDIVIDUAL IDs 1 (5/day) and 2
(20/day) meet the frozen fixture criteria. ID 1 is the smallest sufficient
candidate for one planned download, a recommendation only; no plan was selected
or granted. Unlimited ID 3 and BUSINESS IDs 4-6 are excluded by that contract.

The exact five-file reader passed 29 local tests before MA's bounded native run.
The first pre-DB GUARD HOLD remains historical; a clock-boundary explanation is
plausible, not proven. No account/business/schema writes, app/login or audio
occurred. The frozen fixture tool remains unchanged. Its later private schema/
snapshot/new-identity collision inspection is still required under separate
fixture approval; this recheck does not replace it. See
[WI010 final results and remaining gates](../deliverables/user/WI-20261023-ATS-010-summary.md).

### 2026-10-23 Read-Only Account and Plan Recheck Approved

**Current: the user approved server access, temporary DB start/stop and read-only
account/plan rechecking only. Preparation/review is in progress; native recheck
has not run at this approval checkpoint. WI010 and REQ007 remain active.** No
application start/login, real account/business-data change or audio trial is
authorized. The user-selected Astra Extra High setting continues.

The approved chain is [SE008 minimal read-only payload](../deliverables/agent/WI-20261023-ATS-008-handoff.md)
-> [PG009 exact review](../deliverables/agent/WI-20261023-ATS-009-handoff.md)
-> MA bounded read-only native execution -> [DocOps010 integration](../deliverables/agent/WI-20261023-ATS-010-handoff.md).
The separate reader extends the successful WI002 query only with plan details
and total subscription counts. It does not modify or activate the frozen WI005
fixture candidate and does not require new-account credentials or verified-TEST
flag approval for this read-only scope.

MA must verify exact source/runtime/target identity, then report actual reads,
independent STOP/PID_ZERO/SOCKET_GONE/BASELINE results, post-stop probe and SSH
restoration. Earlier phase 1 success remains dated evidence; no new result or
cleanup PASS is inferred from approval. Existing ID 1 remains unchanged, and
fixture writes/audio remain separately unapproved. See [verbatim scope](../deliverables/user/REQ-20261011-ATS-007.md)
and [WI010 in-progress evidence](../deliverables/agent/WI-20261023-ATS-010-evidence-pack.md).

### 2026-10-23 Local Fixture Tool Preparation and Review Completed

**Current: LOCAL_PREPARATION_REVIEW_PASS for the exact 22-file fixture candidate;
SE 12 + PG 26 = 38 focused local tests PASS. WI005 -> WI006 -> WI007 is complete.
REQ007 remains active. Server contact, native fixture writes and audio execution
were not authorized or performed in this local work.** The [PG receipt](../build/WI20261023-006-fixture-review/review-receipt.json)
binds [candidate.json](../build/WI20261023-005-account-fixture/candidate.json).
Source643, retained JAR and seven earlier manifests have zero mismatches; no
application rebuild or dependency installation occurred.

The implemented contract preserves ID 1 and every existing row/column. Future
approved effects are exactly two new TEST users, one designated new-user
USER-to-ADMIN update with one audit row, and one non-billing entitlement. Verified
TEST flags do not claim human consent or a verification-mail event. A future
fresh inspection must select an existing active INDIVIDUAL plan with at least
one daily download; any existing ID 1 subscription row, even expired, causes
preservation/HOLD. Phase 1's historical effective-subscription count 0 does not
prove this stricter condition.

The grant is MONTHLY/CANCELLED for one explicit KST day, with both pending fields
NULL; no automatic extension or date substitution. Native inspection and apply
need separate exact approvals after current private identities, plan, target,
candidate and date are bound. ID 1 credentials/login ability remain unverified.
Local Java17/retained-JAR BCrypt compatibility passed, but native MySQL/fixture
execution and audio/hash/listening acceptance remain NOT_RUN. See
[WI007 final scope and operator gates](../deliverables/user/WI-20261023-ATS-007-summary.md).
The earlier phase 1 success below remains dated evidence, not new execution.

### 2026-10-23 Local Fixture Preparation and Independent Review Approved

**Current: the user approved local implementation and independent review of the
bounded fixture tool. WI005/WI006 preparation and review are in progress, not
complete. REQ007 remains active. No server access or real account/data/audio
execution is authorized by this extension.** The user-selected Astra Extra High
setting continues. The chain is [SE005 implementation](../deliverables/agent/WI-20261023-ATS-005-handoff.md)
-> [PG006 independent review](../deliverables/agent/WI-20261023-ATS-006-handoff.md)
-> [DocOps007 final integration](../deliverables/agent/WI-20261023-ATS-007-handoff.md).

The tool's planned effect contract preserves every field of existing ID 1;
prepares exactly two new accounts; promotes only the designated new uploader
USER to ADMIN with one audit row; and grants one non-billing entitlement to the
new positive USER. These are future effects to implement and review, not changes
already applied. Actual owners/private inputs, approved verified-fixture
semantics, exact existing plan/date and target/candidate authorization remain
future execution gates. All local test identities and passwords are synthetic.

Server/provider/SSH contact, service or DB/app start, real signup/login/mail and
business/schema changes remain outside this local approval. Server access,
actual account/data changes and audio execution require separate approval after
readiness. The completed phase 1 inventory/restoration below remains valid dated
evidence, not a current target guarantee or authority for the new candidate.
See [verbatim approval](../deliverables/user/REQ-20261011-ATS-007.md) and
[WI007 in-progress evidence](../deliverables/agent/WI-20261023-ATS-007-evidence-pack.md).

### 2026-10-23 Phase 1 Account Inventory and Restoration Completed

**Current: approved phase 1 completed. INVENTORY_OBSERVED / inventory PASS; DB
STOP, PID_ZERO, SOCKET_GONE and BASELINE all PASS, followed by a separate
PREFLIGHT_PASS. Original SSH firewall source restored; provider/console logout,
owned-key removal and SSH agent Stopped/Disabled confirmed. WI004 is complete;
REQ007 remains active and phase 2 is unapproved.** MA owns the native observations;
the [inventory](../build/WI20261023-phase1-native/inventory-result.json) and
[final restoration](../build/WI20261023-phase1-native/final-restoration.json) are
the sanitized evidence. No application start/login or business DML occurred.

Exactly one current account was observed: ID 1, designated latest-auth-signup
USER, verified/nondeleted with a local password present, no effective
subscription, and eligible as the unentitled candidate. Credentials and password
login policy were not checked. There is no current ADMIN or entitled USER;
pending audio and unfinished storage counts are zero. This establishes a bounded
inventory, not account-login or audio acceptance.

The proposed next phase preserves ID 1 and separately prepares two new accounts,
promotes only the new upload account to ADMIN, and grants one non-billing
entitlement to the new positive USER. The provisioning tool, private owner/input
selection and existing plan ID remain unresolved; nothing is provisioned or
approved. These fixture writes must precede the frozen audio engine's baseline
in a separately approved preparation stage: the audio contract accepts existing
actors and disallows account-role/profile and entitlement writes. See the
[exact proposal and limits](../deliverables/user/WI-20261023-ATS-004-summary.md).

The stream/file length+SHA linkage is implemented and independently reviewed in
the new local candidate (75 focused tests PASS); native audio and human playback
remain NOT_RUN. The audio API uses loopback with external HTTPS quarantine
retained; no new 443 rule change is implied. Earlier access failures/open-session
checkpoints below are historical. The restored TCP22 source and unchanged 80/443
rules were read back; no exact IP or console token is published here.

### 2026-10-23 SSH Timeout and Firewall Source Mismatch

**Current: local preparation/review complete; phase 1 account inventory NOT_RUN.
SSH access was attempted after the third UAC request succeeded. The current
authorized access session remains open: key loaded, SSH agent Running/Manual,
provider session logged in. WI004 and REQ007 remain active; cleanup is not PASS.**
MA's exact phase 1 probe failed, then a bounded transport-only diagnostic returned
exit 255/TIMED_OUT; see the [sanitized access result](../build/WI20261023-phase1-native/access-check.json).
No DB start, account inventory, application start/login or business DML occurred.

MA's read-only provider inspection found the server operating, ELCAP ON and a
prior fixed SSH source /32 different from the current PC public IPv4. Via the
noVNC OS console, root login succeeded; `ssh` and `ssh.socket` were active, SSH 22
and nginx 80/443 listeners were present, and `nft list ruleset` was empty. The OS
console was exited and its login prompt verified. This OS administrative login
is distinct from the prohibited application login. These observations do not
establish account eligibility or phase 1 target-gate success.

The provider firewall was not changed. An unsaved proposal to change SSH 22's
source only, then restore it after phase 1, awaits explicit user approval; other
ports remain unchanged. Exact IP values and console token URLs are not retained
here. Earlier zero-SSH-attempt and Stopped/Disabled checkpoints are historical,
superseded for current access state by this report. Phase 2 approval remains
separate; [WI004 evidence](../deliverables/agent/WI-20261023-ATS-004-evidence-pack.md)
tracks the pending inventory and access cleanup.

### 2026-10-23 Phase 1 Not Run After Local Access Cancellation

**Current: local preparation/review complete (75 tests PASS); phase 1 account
inventory NOT_RUN because local access input was blocked. WI004 and REQ007 remain
active. No UAC request is pending.** MA execution receipts `8dc5f5` and `9862d9`
both returned a cancellation result. The result does not establish whether a
person cancelled the request or Windows timed out; no cause is inferred.

MA's final local check `ce7a7b` observed zero consent processes and SSH agent
Stopped/Disabled. MA's execution history records zero SSH command/server
connection attempts, DB starts, application starts, logins and business-data
writes. This is a local state/history check, not a remote scan or a new server
cold/quarantine verification. Account reuse and required role/fixture changes
remain unknown because no inventory ran. The earlier second-UAC-pending note is
superseded. Separate phase 2 audio approval remains required; see
[WI004 current result](../deliverables/agent/WI-20261023-ATS-004-evidence-pack.md).

### 2026-10-23 Local Hash Linkage and Phase 1 Review Completed

**Current: LOCAL_HASH_LINKAGE_PREPARATION_PASS and
ALLOW_EXACT_PHASE1_DB_ONLY_WITH_TARGET_GATES. MA's native account inventory and
cleanup results have not yet been integrated; WI004 and REQ007 remain active.**
The [PG003 receipt](../build/WI20261023-003-review/review-receipt.json) binds the
new 68-member audio manifest and exact account-inspection candidate. Local
provided hash/adapter/account suites passed 15+26+16 tests; independent PG suites
passed 8+10, totaling 75 local synthetic tests. This is not native audio or DB
acceptance. Source643, retained JAR and original v8r1 assets remain unchanged.

The new local overlay compares the complete HTTP stream's length/SHA-256 to the
same trial track's derivative file and binds durable evidence to the exact
attempt. The earlier missing-linkage note remains historical. The account
procedure allows only the reviewed temporary DB start, bounded SELECT inventory
and owned stop after exact target/provenance/cold-quarantine checks; all four
cleanup checks must pass. A phase 1 technical allowance adds no phase 2 audio
authority. No app start, login or business-data mutation is permitted. See
[WI004 exact pins and pending boundary](../deliverables/agent/WI-20261023-ATS-004-evidence-pack.md).

### 2026-10-23 Phase 1 Account Reuse Inspection Approved

**Current approval: local stream/file hash linkage and independent review, then
MA-only phase 1 account-reuse inspection. Native inspection has not been recorded
as started or completed at this checkpoint; REQ007 remains active.** The user
selected Astra Extra High and approved necessary server access and temporary DB
start/read/stop. Application startup, login and business-data changes are excluded;
phase 2 audio execution requires separate approval. No account, role or entitlement
change is approved by this inspection.

The approved chain is [SE001 hash linkage](../deliverables/agent/WI-20261023-ATS-001-handoff.md)
in parallel with [SE002 read-only procedure](../deliverables/agent/WI-20261023-ATS-002-handoff.md),
then [PG003 exact-candidate review](../deliverables/agent/WI-20261023-ATS-003-handoff.md),
MA phase 1 execution, and [DocOps004 final integration](../deliverables/agent/WI-20261023-ATS-004-handoff.md).
The final checkpoint must report reusable accounts versus unknown prerequisites,
any exact role/fixture changes still needed, and cleanup evidence. Earlier
authentication success remains dated evidence; a full authentication trial is not
part of this inspection. See the [verbatim approval and scope](../deliverables/user/REQ-20261011-ATS-007.md)
and [in-progress evidence](../deliverables/agent/WI-20261023-ATS-004-evidence-pack.md).

The following checkpoints retain their original, dated scope and outcomes. This
approval supersedes their local-only restriction solely for the bounded phase 1
operations above; it is not a native success or audio acceptance result.

### 2026-10-23 Local Audio API Preparation Reviewed After Interruption

**Current: LOCAL_ALLOW_API_PREPARATION_ONLY. No server contact or native audio
execution; REQ007 remains active.** WI010/WI013 local preparation and WI011
independent review are integrated in [WI012 evidence](../deliverables/agent/WI-20261021-ATS-012-evidence-pack.md).
The [fresh PG receipt](../build/WI011-audio-review/review-receipt-20261023.json)
binds the current 62-member manifest
`3c837f740160d6a54fb3093d75e2148101efe9f67c11b39b615f3086598b48f2`;
the October21 review of `5d4b72` remains historical. Fresh local injected suites
26+4+2 PASS; earlier 50+8 tests were not rerun. Source643/JAR/WAV/copied-source
identity matches; MA observed local SSH agent Stopped/Disabled on October23.
Server cold/HTTPS restoration remains the October21 WI009 evidence, not a new check.

Existing success tools and JAR are reused. Separate future approval must cover
three existing actors' login refresh changes, one upload/activation, source+MP3,
four storage journal rows, one entitled USER license/download and count+1.
No account/role/entitlement provisioning, payment, mail or data deletion is
implemented or approved. Missing eligible actors require a concrete decision.
The API phase preserves external HTTPS quarantine and cannot prove audible
playback or browser UI. An exact derivative-copy/listening recipe is documented,
but transfer is not implemented/executed; native execution and human playback
remain NOT_RUN. Direct streamed-body SHA to derivative-file SHA linkage is not
implemented. See [account/data proposal](../deliverables/user/WI-20261021-ATS-012-summary.md).
Server resumption requires separate user confirmation after this bounded preparation.

### 2026-10-21 Bounded Gmail Authentication Flow Completed

**Current, MA native evidence around19:50 KST: BOUNDED_AUTH_FLOW_OBSERVED,
JAVA_RELEASED, cleanup PASS. Independent cold/quarantine/configuration checks PASS;
console logged out, owned key removed, agent Stopped/Disabled.**
The v8r1 trial observed human signup/consent, email verification, login/ME/logout
and reset-mail request. User confirmed reset mail receipt; reset link was not
opened and password was not changed. Gmail sends to the designated Naver mailbox;
paid Naver Cloud mail was not provisioned. No public release or new purchase.
MA source643/JAR identity remained unchanged; no application rebuild was needed.
This supersedes the earlier human-flow deferral and agent-restoration pending
state, not the recorded outcomes of earlier failed attempts.
Recovery-console input showed a password-like suffix in the login-name field:
credential rotation is recommended, not performed; no value or screenshot copied.
REQ007 stays active for remaining scope. Streaming/Official Download, provider
migration and overall production readiness are not accepted by this auth trial;
album/Track404 under narrow auth routing proves neither feature failure nor success.
[WI009 evidence and exact pins](../deliverables/agent/WI-20261021-ATS-009-evidence-pack.md)
separate MA native/human evidence from local PG review and DocOps validation.

### Historical Checkpoints

All dated entries below retain their original outcomes and access states.

### 2026-10-21 Runtime V7 Reached OPEN, Signup Deferred

**Current: Finish `7bd618` accepted; `0bf20c` safety cleanup PASS, app/DB stopped.
Full cleanup aggregate FAILED remains; console/key cleanup complete,
agent Running/Manual empty; administrator restore deferred until home, UAC caller cancelled.**
Server: app masked/PID0, no UID999; MySQL masked/inactive/PID0;
Nginx quarantine active/PID86404, nft empty. Original attempt `e6fd8e` phase WINDOW,
app JAVA_RELEASED: POLICY/BPF/LEDGER PASS, MAIL FAILED because human flow was not
completed. Later independent post-stop result.json POLICY/BPF/LEDGER/MAIL failures
followed removal of the scope; they are not original-attempt BPF failure.
MA `16c669` previously confirmed ready.json OPEN/AWAITING_HUMAN_FLOW and
app84237/supervisor84036/MySQL84178/Nginx84459 running. This proves the bounded
collation fix passed the prior ledger blocker and reached actual OPEN/signup UI,
not signup/mail acceptance. Deferral is not a code failure.
After human READY: Release `e92e2d` RELEASE_STAGED (receipt prefix4e9ddeb) ->
full preflight `7f5487` PASS -> producer `d87eb4` STAGED (prefix0556b9) ->
Start `b39ea4` START_REQUESTED. Early Status `118692` ENTRY_GATE preceded
ready.json; it was not-ready, not trial failure. Signup UI was reached with approved
profile fields filled; the user deferred before submitting. No consent or signup
completion is assumed. Expiry1792541901 (MA-reported09:18:21KST).
Access: `ce797f` tty1 root agetty only, console logged out; signup/noVNC tabs closed.
`172453` exact owned SSH key removed, agent has no identities. User cannot approve
UAC from mobile; `cfaacd` terminated exact restore caller PID66804, identified by
`c95ddb` matching pwsh and unique AGENT_RESTORE_PROCESS. `7bc391` confirms
session3252 ended with -1; it is not running or awaiting UAC. No identities or remote-authentication keys loaded;
agent intentionally Running/Manual, not Stopped/Disabled. At home: administrator
stop/disable and state verification, then reconfirm participation for fresh MA trial
gates and human password/consent/signup/mail-receipt steps; do not replay this attempt.
No app restart or firewall widening. Current pause is human mobile postponement. Remote-PC browser
retains its IP; direct mobile needs a different IP. No access-rule widening.
Human signup/receipt deferred; no signup/mail acceptance, final cleanup or REQ closure.
Original failed attempts remain historical; setup and OPEN do not establish trial PASS.
[WI062 current evidence](../deliverables/agent/WI-20261011-ATS-062-evidence-pack.md#2026-10-21-runtime-v7-reached-open-signup-deferred).

#### Historical October21 Diagnosis And Failed Trial

**Current: MA `cc788c` native DIAGv2 COMPLETE PASS; diagnostic plus four cleanup
checks PASS. No DB changes/app/email/signup; runtime v7 integration pending.**
Three columns use utf8mb4_unicode_ci versus CONVERT-side0900 collation; original
CHECKS query line90 and each of three individual comparisons fail with1267.
Explicit unicode_ci on the RHS gives FULL parse/Ledger PASS, all initial-zero
checks true. This is bounded diagnostic acceptance, not actual trial success.
Original LEDGER_PREFLIGHT HOLD/app NOT_RUN remains: `f7007f` final POLICY+BPF PASS,
LEDGER/MAIL FAILED, overall cleanup FAILED; all safety steps PASS. Last `a0cf42`: app masked/PID0,
supervisor loaded/failed/PID0, MySQL masked/inactive/PID0, Nginx81244 active;
`f7007f` nft empty, UID999 process count0, grant absent. Safety is not full cleanup.
A separate restore BPF command failure after cgroup removal is not an original
attempt BPF failure. SE/PG are integrating the exact fix into runtime v7 with safe
history preservation; no integrated retry success or final resolution is claimed.

Human participation returned; SSH unlock and root-console login completed.
SSH/root console are ACTIVE for current work, superseding prior closure.
Earlier Release `37ea47` used CIDR instead of plain management IP: preflight
`110c7b` MANAGEMENT before producer; in-memory plain-IP check `acccf8` PASS.
[Exact unused-release preservation review](../build/WI071-review/pg-release-refresh-780c-final-20261021.json)
preceded `93b4aa` COLD_PRESERVED and independent `c656ce` nine-archive/runtime/cold
PASS. Corrected Release `4a5a86` -> full preflight `9da019` PASS -> producer
`545285` STAGED -> Start `3f6cb0` -> current HOLD. No final resolution or REQ closure.
[WI062 current evidence](../deliverables/agent/WI-20261011-ATS-062-evidence-pack.md#2026-10-21-bounded-ledger-diagnosis-pass)
records pins and boundaries. All following dated checkpoints remain historical.

### 2026-10-18 V6c Transport HOLD, Installed Tools Verified

**Interim: v6c Public `29cde0` HOLD BOUNDED_TRANSPORT: receiver reply reports183
files versus186. Installed tools independently verified by MA `6fefed`: runtime,
full tree/r.verify_tree, base environment, resolver/listeners and JAR/SPA PASS.**
This does not relabel Public HOLD as PASS. App/DB cold, Nginx70173, nft empty.
Runtime v6 strict three-row BPF review has72 local PG tests PASS. `3139ae`
CheckPublic186 PASS preceded cold `579ddc` COLD_PRESERVED and `fdcf4f` independent
six-archive/cold PASS. Earlier delivery v6/v6b bugs remain HOLD history.
SE/PG are preparing v6d ACK fix/continuation; no Public retry/reinstall, timed
Release, producer or new Start since the failed v5 attempt. No Gmail/signup.
Prior `4c5af7` PREJAVA_PROOF/app NOT_RUN/full cleanup FAILED, including
BPF/LEDGER/MAIL failures, remains unchanged. Historical app cause is unconfirmed.
Actual sequence: `e843e1` PUBLIC_STAGED -> `a2253d` pre-release PASS -> `258a88`
Release -> `58f7a6` full preflight PASS -> `303824` producer STAGED -> `ae71a4` Start.
Earlier COLD_PRESERVED/archive verification remains historical; its off-path mask
and PID63385 state is superseded by the current safety receipt.
Earlier `0440e7` isolated38/38 PASS and independent `05df13` remain bounded,
not integrated BPF/app/Gmail acceptance; the earlier masked-app state is historical.
[Runtime v6 review](../build/WI071-review/pg-runtime-bpf-v6-final-20261018.json)
does not establish actual app BPF acceptance. SSH active/root console logged in;
fresh human availability confirmed18:12-18:13, not a waiver of action-time gates.
Next: SE/PG v6d ACK review and bounded continuation before further trial gates.
Prior access closure is historical. Restore-only proof `093d70` remains bounded.
REQ007 active; no additional paid product/public exposure; no final closeout.
[WI062 interim evidence](../deliverables/agent/WI-20261011-ATS-062-evidence-pack.md#2026-10-18-v7b-isolated-pass-interim)
contains exact pins and PG/MA receipt pointers.

The following completed checkpoint is preserved as history; its native-proof-pending
and closed-access statements describe that earlier checkpoint only.

### 2026-10-18 V5 Local PASS, Native Proof Pending

**Current: owned nft table removed; app/supervisor/DB stopped; SSH and recovery
console access closed.** MA `cbe121` and independent `c4cd9b` confirm separate
recovery PASS; `f4cc15`, `8fa6e2` and UI logout confirm final access cleanup.
The original STAGE_NFT HOLD/cleanup FAILED/app NOT_RUN remains unchanged.

[PG v5 decision](../build/WI071-review/pg-positive-uid-v5-local-final-20261018.json)
is LOCAL_SOURCE_ALLOW_NATIVE_PROOF_PENDING:13 tests and228 additional assertions
PASS,81/81 statements and16/16 branches, local synthetic scope only. Positive-UID
dedicated-chain correction preserves the allowlist, BPF and zero-counter gates.
No v5 installation/restart, signup or Gmail occurred. The bounded trial window
expired; later human availability needs reconfirmation.
Next: separately reviewed isolated native proof without external access, including
other/missing socket UID, allow/deny, counters and cleanup; only then later
integration with fresh runtime review and human availability. No failed-window replay.
REQ007 remains active; no additional paid product or public release.
[WI062 checkpoint](../deliverables/agent/WI-20261011-ATS-062-evidence-pack.md#2026-10-18-v5-local-pass-native-proof-pending)
records the freeze, evidence and validation limits. October17 backup/restore PASS
remains bounded: runtime keys excluded, audio empty, full DR false.

#### Historical October18 Receipts

The following sequence records earlier states, not the current access/runtime state.
MA `352463` reported
actual v3 LISTENER_ONLY PASS after exact public identity, preserved v2 tools and
v3 public staging. PG's exact-byte ALLOW retains existing action-time gates.
The v2 HOLD was trailing ASCII spaces in a TCP owner field, not evidence of a
malicious listener. This does not resolve the historical BPF cause.

Fresh MA cold-state evidence has unchanged target/boot, MySQL masked/PID0,
auth/supervisor/private paths absent and original Nginx PID34885 unchanged.
Management IP/provider boundaries are unchanged. No app/DB start, auth, mail
or account mutation is confirmed in this checkpoint.
After the14:47 console login, MA confirmed cold app/DB and exact quarantine,
owned-key removal/empty agent, Stopped/Disabled service and console logout to
login. The human then confirmed recovery done and participation available.
After a temporary agent/key reopen, MA `ac0872` again confirmed exact owned-key
removal, empty agent and Stopped/Disabled; console logout to login was verified.
MA had reported no runtime change after cold postcheck `dfde3c` at that earlier checkpoint. **The human's later
reply and screenshot confirm recovery root login at15:15 and participation yes.**
That access window reopened with recovery root logged in and an owned SSH key;
the final closure below supersedes that state. MA `c116e7` confirmed v3 Release STAGED; producer `4f1de0`
held before private stage/control/unit creation. MA diagnosed BASE_ENV:83 normal
operating assignments conflict with the reader's incorrect nine-only assumption.
The v4 fixes are now accepted: MA `639b11` CheckPublic PASS, `9ad672`
COLD_PRESERVED, `e9dd98` PUBLIC_STAGED, `db7b12` BASE_ENV_RESOLVER_LISTENERS PASS,
`036287` fresh Release and `3adee4` FULL_PREFLIGHT PASS. Producer `7ba4ff` held
at ENTRY_GATE; `234df4` confirms stage/control/log/supervisor paths absent and
BASE_CONTRACT PASS. That attempt's private-framing stage was unresolved.
The subsequent MA sequence `079d47` cold refresh -> `ab839e` new Release ->
`37b603` preflight PASS -> `435224` producer STAGED -> `952b07` Start reached
`c1f45a` STAGE_NFT HOLD/app NOT_RUN, cleanup FAILED due to NFT representation.
MA `23974d` reports nft1.0.9 omits implicit protocols and represents ct state as
integer2; the table remained at that checkpoint. `5a9f10` confirmed cold app/MySQL and exact baselines,
with overlays/ready/grant absent. Counter6/240 ->7/324 despite idle scoped UID
needs separate investigation, not an inferred cause or auth PASS. Separate recovery
`cbe121` OWNED_TABLE_REMOVED followed PG ALLOW `68631704`; independent `c4cd9b`
Native.verify_restore PASS confirms app/supervisor/DB stopped. The original attempt
remains STAGE_NFT HOLD/cleanup FAILED/app NOT_RUN; no replay occurred.
Final access cleanup: `f4cc15` owned key removed/agent empty, `8fa6e2` Stopped/Disabled,
and UI Ctrl+d/login-prompt screenshot confirmed console logout. The bounded10-minute
trial window expired without signup/mail; later human availability needs reconfirmation. Only local positive-UID/parser fixes and
PG review continue; no v5 runtime install, new Release or retrial in this failed window.

October17 bounded backup/isolated-restore PASS stands; runtime keys were excluded,
audio empty and full DR false. REQ007 remains active: ordinary scoped repairs,
then existing-Gmail trial; unexpected costs first, no external release, extra
paid products, prune or deletion. The local-review-pending state above was superseded
by the current v5 decision; it does not establish native policy acceptance.
Older entries retain their historical dates.

### 2026-10-17 Bounded Restore PASS, Auth V2 Pending

**Backup/restore PASS is accepted for the separate auth prerequisite;
authentication remains pending listener-v2 review, actual flow NOT RUN.** MA
`12ca09` reports restore-only PASS against the original snapshot, cleanup true/
errors empty, apps NOT_RUN. PG070 accepts ACCEPT_BOUNDED_BACKUP_RESTORE_FOR_AUTH_GATE.
Actual column metadata and all other SQL bytes match, with209 redundant charset
insertions and complete43-table/6-row count-map equality; different raw hashes
are preserved. Runtime keys were excluded and audio was empty: **full DR is false**.

Independent poststate has MySQL masked/PID0, writers0, unchanged original Nginx
and boot, approximately95.94GB decimal free. Public WI068 JAR,143 SPA files and
39 tools were staged and native hash-matched, **not started**. The old auth
producer allowance ended in preflight HOLD/UNREVIEWED_LISTENER, before private
stage writes. Existing scoped DNS/DHCP listener recognition needs the narrow
WI067-v2/PG071 review; no new open ports, app/mail or account mutation occurred.

The original auth release expires at13:33:58 KST; do not replay it. Only a later
fresh release after exact v2 review may proceed. Human availability for direct
password entry/terms acknowledgement is not a completed signup or email receipt.
**SSH remains active and the recovery console logged in; access cleanup NOT DONE.**
REQ007 stays active, no extra paid product or external publication.
[WI062 restore/auth checkpoint](../deliverables/agent/WI-20261011-ATS-062-evidence-pack.md#2026-10-17-restore-pass-and-auth-v2-checkpoint)
supersedes the older restore-HOLD/bootstrap-only entries below without rewriting them.

### 2026-10-17 Backup Executed, Restore Acceptance Held

**MA's approximately12:45-12:48 KST milestone supersedes backup NOT RUN:**
repository initialization, encrypted backup, `restic check --read-data`, verified
full-file restore and isolated SQL import occurred. The Run ended **HOLD /
SQL_SCHEMA_DRIFT**, cleanup true/errors empty, no application start. Backup
execution is established; restore acceptance and data equivalence are **NOT PASS**.

MA reports148 files,71,682,666bytes processed and63,535,367packed bytes in the
existing Private bucket, with no extra paid product. Diagnostics found only added
`CHARACTER SET utf8mb4` before existing `COLLATE utf8mb4_unicode_ci` across209
column DDL lines/43 tables. Diagnostic stripping matches schema/full SQL, but
actual metadata and narrow parser verification remain pending acceptance.

PG070's exact one-use allowance is consumed; no first-capture/init rerun, prune
or deletion is authorized. PG071's WI067 runtime/local-quality ALLOW and208 tests
do not clear backup/restore PASS, human password/consent or actual email-receipt
gates. WI068 JAR identity remains unchanged. SSH agent remains **Running/Manual,
cleanup PENDING**. REQ007 stays active; no external publication.

[WI062 backup milestone](../deliverables/agent/WI-20261011-ATS-062-evidence-pack.md#2026-10-17-backup-milestone)
records receipts, exact review pins, postcheck and limits. Older entries below,
including today's bootstrap-only/backup-NOT-RUN checkpoint, are preserved history.

### 2026-10-17 Bootstrap Installed, Backup Not Run

**REQ007 remains active: backup and isolated restore, then existing-Gmail
features.** MA reports restic **INSTALLED_NOT_EXECUTED**, PC_COPY_HASH_MATCHED
and private permissions checked on October17 around11:56 KST. Init, backup and
restore remain **NOT RUN**. WI070 permits **ALLOW_BOOTSTRAP_ONLY** on the exact
reviewed manifest; capture/restore remains under review and HOLD, not global ALLOW.

WI068's offline JAR is complete. PG's October17 11:55:48 KST receipt confirms
643 source/336 frontend/143 SPA identity PASS; DocOps independently matched the
JAR hash. Neither proves target execution or Gmail delivery. MA's11:53 KST
receipt reports actual app units masked/PID0 with historical failed state,
MySQL masked/inactive/PID0, no Java/FFmpeg/mysqld, Nginx active and historical
MySQL/quarantine pins matched. Approved management-IP replacement restored SSH.
SSH agent is **Running/Manual, owned key loaded, cleanup PENDING**.

See [WI062 October17 batch](../deliverables/agent/WI-20261011-ATS-062-evidence-pack.md#2026-10-17-batched-checkpoint)
for receipt IDs, hashes, attribution and remaining gates. The approved private
backup cost bounds remain; unexpected costs require escalation first. No added
paid product, public release, prune or old-data wipe is implied. Preserve the
bounded WI060 web PASS without promoting feature/backup completion.

**Historical boundary:** all older entries below retain their observation dates.
Their "not installed", "build active/incomplete", no-transfer and earlier
access-closed statements are superseded for current decisions by this batch.

### 2026-10-11 DocOps Checkpoint Delivered, REQ007 Active

**This documentation checkpoint is finalized; REQ007 remains active, backup
NOT RUN.** SSH agent is temporarily Running/Manual with cleanup pending; new
key PC save/metadata/ACL PASS is confirmed, no server transfer reported.
PG065 source ALLOW stands; WI068 offline build is active, not completed.

MA `54c439`: official restic0.19.1 asset download/hash PASS; extracted ELF
identity is in [WI062 final checkpoint](../deliverables/agent/WI-20261011-ATS-062-evidence-pack.md#documentation-checkpoint-closeout).
Restic is **NOT INSTALLED**; init/backup/restore remain NOT RUN. WI066's first
synthetic proof held before JDBC because the Windows monitor used a different
PID; all owned processes stopped. MA reports a `--no-monitor` fix with8 tests
and authorization of one fresh proof, not a proof PASS. Native state unchanged.
WI070 review and downstream feature gates remain open. MA owns further work
and cleanup; DocOps ends this assignment now and resumes only with a later
batched handoff. No per-tool current-entry growth or future wait is scheduled.

Earlier local-preparation checkpoint (retained):

MA `d41c45` reports the approved replacement key file saved in user-only private
storage with ACL PASS; **no server transfer yet**. DocOps did not read that file.
The old file remains quarantined. Key rotation and safe local-file handling
are distinct from server credential use, repository initialization or backup.

[PG065 final audio review](../deliverables/agent/WI-20261011-ATS-065-evidence-pack.md)
is ALLOW_SOURCE_FOR_WI068_BUILD with147 tests PASS. MA released one WI068 offline
build; it is **not completed**, and no new artifact/runtime PASS is claimed.
MA also read three WI066 implementation files and issued an exact15-minute,
one-use gate for isolated synthetic local MySQL proof. This authorizes that
local proof only, not a result or target/native execution.

[WI070 handoff](../deliverables/agent/WI-20261011-ATS-070-handoff.md) now exists
and PG delegation is reported by MA; final backup review remains pending.
Continue WI069 -> WI070 exact review -> gated MA backup/isolated restore ->
WI067/WI068-backed features, retaining source/proof gates. No new restic install,
init, backup or feature result is supplied. Current SSH cleanup remains due;
web PASS is preserved and REQ007 remains active. See
[WI062 local-preparation checkpoint](../deliverables/agent/WI-20261011-ATS-062-evidence-pack.md#replacement-file-and-local-gates).

### 2026-10-11 Key Rotation Verified, Actual Units Observed, Backup Not Run

**MA's fresh browser observation verifies user-performed rotation:** the old
21:42 key is STOPPED and the new21:49:27 key is ACTIVE. No values are recorded.
The old key was never used on the server, per MA; its PC file remains quarantined
and must not be reused. A separate replacement filename is designated for future
use, not evidence that the new credential is saved or transferred.

MA's native observation identifies the actual WI026/WI032/WI042/WI055/WI060
app units as masked, PID0, with historical failed state; MySQL masked/inactive,
Nginx quarantine active. This supersedes the earlier empty `atstudio.service`
query as current unit evidence, without treating historical failures as a new
incident. SSH agent remains temporarily Running/Manual and requires cleanup.
No restic install, init or backup has run; restore and feature stages remain pending.

[WI062 rotation/unit checkpoint](../deliverables/agent/WI-20261011-ATS-062-evidence-pack.md#verified-rotation-and-actual-unit-state)
distinguishes verified rotation from pending private handling and execution.
The incident history, web PASS, private-bucket confirmation and approved backup
scope remain intact. REQ007 is active; no credential value or private file was read.

### 2026-10-11 Key Incident, Revocation Pending, Bucket Confirmed

**MA reports one inadvertent inclusion of the existing auto-issued Object
Storage key in a browser DOM diagnostic tool result during a redaction attempt.**
No value or private-file path is recorded here. MA promptly informed the user
and requested disabling the old key and generating a replacement through the
provider UI; confirmation is pending. The old copy remains quarantined DO NOT
USE in the approved user-only private folder. MA reports no public/Git upload,
server transfer or use. Do not reuse it or treat replacement/revocation as done.
This required human key step does not cancel the approved backup purchase scope.

MA confirmed the new bucket Private,0 bytes,0 objects at **21:44:18 KST** under
service323308, superseding the pending bucket submission. Read-only `098c4d`
reports MySQL off/masked, Nginx active, no Java/FFmpeg, free95,953,276,928bytes,
and restic absent. The queried `atstudio.service` returned empty UnitFileState:
**not confirmation of the actual app unit**. No-Java and historical WI060
postchecks do not repair that identity gap or prove new app-unit state.

[WI062 incident/state evidence](../deliverables/agent/WI-20261011-ATS-062-evidence-pack.md#object-key-incident-and-confirmed-bucket)
keeps values out and attributes the observations. Credential-dependent work
awaits the user's old-key disable/replacement confirmation; backup/restore and
features remain pending. SSH cleanup is still required for the reopened task
access. Web PASS and prior cleanup receipts retain their historical authority.

### 2026-10-11 Object Storage Activated, Bucket Pending, SSH Reopened

**MA reports Object Storage service activated; private-bucket creation is only
submitted, not yet authoritatively confirmed.** One application-button action
at provider `/space`, followed by reload showing Bucket/auth-key controls,
established the reported activation state. MA saw no explicit TOS checkbox or
new paid add-on in that flow; this is an observation, not a claim that no terms
apply or usage is free. No Object Storage keys or backup upload are reported.

MA reopened owned SSH access using the previously authorized existing credential.
Receipt `1a8fd3` reports agent Running/Manual and the sole owned-key fingerprint
verified; no credential or fingerprint value is recorded here. **This new native
access belongs to the current task and must be cleaned up.** Prior cleanup
receipts remain valid for the earlier window, not proof that access is closed now.
No new noVNC login or root session is reported.

[WI062 activation checkpoint](../deliverables/agent/WI-20261011-ATS-062-evidence-pack.md#provider-activation-and-reopened-access)
separates the MA screenshot pointer, submission and remaining gates. Web PASS
is unchanged; bucket confirmation, adapter/review, backup/isolated restore and
Gmail feature results remain unproved by activation alone. Existing cost/private-
bucket/no-public-release/no-prune bounds apply; REQ007 stays active.

### 2026-10-11 Option2 Approved, Backup/Restore Before Gmail Features

**The user explicitly approved an extension of REQ007:** build Object Storage+
restic on the quoted cost, perform backup and isolated restore, then validate
actual features with existing Gmail. This supersedes earlier recommendation-only
and unapproved-activation entries; it is not a request for repeat approval of
the same bounded work. Execution remains subject to the documented gates.

Approved bounds: one new private bucket, usage-based storage20 KRW/GB-month
excluding VAT, initial repository target below1 GB. If actual requirements exceed
that target or unexpected cost/direct user inputs arise, report them with the
impact before expanding the plan. The target is not a provider billing cap.
No public release, implied paid add-ons, prune or old-data wipe is authorized.

Execution plan: WI069 SE target-backup adapter/local tests -> WI070 PG exact-byte
review -> MA provider activation/install/capture/isolated restore after gates ->
WI067/WI068-backed gated existing-Gmail feature execution. Existing local
preparation retains its own handoff gates. WI068 still
requires its source/test/review gate before a new JAR. Approval is not activation,
upload, restore or feature success; none is newly evidenced by this checkpoint.
See [approved REQ007 extension](../deliverables/user/REQ-20261011-ATS-007.md#approved-backup-scope-extension)
and [WI062 authority/chain](../deliverables/agent/WI-20261011-ATS-062-evidence-pack.md#approved-option2-scope-extension).
Existing web PASS and authenticated-access cleanup receipts remain valid;
REQ007 and downstream stages remain active/incomplete.

### 2026-10-11 Owned Access Closed, Backup Still Undecided

**Owned authenticated access is closed; the bounded web PASS is preserved.
REQ007 and downstream stages are not complete.** MA `e51940`: owned SSH key
removed and agent empty verified. `5c5b07`: hidden elevated cleanup completed;
Win32_Service reports SSH agent Stopped/Disabled. Recovery noVNC was already
logged out with `login:` screenshot verification; its tab remains at that prompt,
not an open root session. Earlier active-access entries below are historical.

App/DB remain stopped/masked from the WI060 postcheck; quarantine HTTPS is
restored. This is MA's reported carry-forward state, not a new DocOps native
inspection. [WI062 access closeout](../deliverables/agent/WI-20261011-ATS-062-evidence-pack.md#final-owned-access-cleanup)
records the receipts and verification limits.

The backup question is still a recommendation request, not a choice or purchase
approval. MA's Object Storage/restic recommendation and attributed quote below
do not authorize activation. **No Object Storage activation or backup upload.**
Feature/backup/restore/load/stakeholder gates remain incomplete. This closes
the present documentation checkpoint only; it does not close REQ007 or those stages.

### 2026-10-11 Recovery Logout Done, Backup Decision Unapproved

**Bounded web PASS is preserved; feature gates and REQ007/WI062 remain open.**
MA reports recovery noVNC logout completed with exact exit keys and screenshot
confirmation of the `login:` prompt. The tab remains open. SSH agent is still
temporarily active; its cleanup is next, not complete. This supersedes the
earlier authenticated-console-open checkpoint without claiming all access closed.

The user asks which backup route to choose:1 local encrypted temporary backup
or2 final Object Storage/restic. MA recommends2 for end-to-end production
backup/restore validation. MA reports verifying20 KRW/GB-month excluding VAT;
DocOps did not independently verify pricing. This is a quoted storage unit
rate, not a total-cost or hard-cap guarantee. **No purchase/activation approval
has been given, and no activation has occurred.** Recommendation is not
selection, installation or backup/restore completion. See
[WI062 checkpoint](../deliverables/agent/WI-20261011-ATS-062-evidence-pack.md#recovery-logout-and-backup-decision).

### 2026-10-11 WI060 Bounded Web PASS, Functional Work Remains

**Web stage BOUNDED_PASS; REQ007/WI062 remain active. SSH/noVNC remain open
for overall functional work, so final access cleanup is not complete.** MA's
approximately21:23-21:25 KST native window ended with S5 `d1a995` exit0,
BOUNDED_PASS/app VERIFIED/view FINISHED/cleanup PASS. All18 state rows passed,
including exact BPF comparisons and final policy/DB checks. The36-row cleanup
table reports applicable rows PASS, receiver close NA. S7 `d69af6` is
PASS_READ_ONLY; independent external `0a064d` reports normal-TLS/no-proxy
HTTP200 with the exact original quarantine body, PASS.

[WI062 final web evidence](../deliverables/agent/WI-20261011-ATS-062-evidence-pack.md#final-bounded-web-stage)
records elapsed comparison timings, browser/screenshot provenance and limits.
`88dccf` attributes the identical static-login screenshot bytes to a fresh MA
capture. Existing-context desktop/simulated-mobile checks are not complete
request proof, physical-mobile/non-management-network acceptance, capacity,
mail delivery or production approval. The historical BPF cause remains
NOT_DETERMINED; the new PASS does not rewrite WI058 HOLD.

MA now releases WI064's source gate for local apply/test only; **no new JAR yet**.
WI063/64 local work and independent feature review precede later functional
runtime gates. Web PASS does not complete features, backup/restore, load,
stakeholder access or REQ007. Earlier pending checkpoints below remain history.

### 2026-10-11 WI060 Ready/HTTP Observed, Finish Requested

**MA observed admission PASS/app VERIFIED/view OPEN; S6 finish was requested
once, but final runtime and cleanup receipts are still pending. No overall
trial PASS.** READY `1dc94e`; HTTP allowed17/17 `268e4d`, denied17/17 with404/405
`f387d5`; S6 `32eb68` once. No precise times were supplied for these receipts.

B01-B07 were observed in the existing browser context at1265x712 desktop and
390x844 simulated-mobile viewport: guest Home, Tracks, synthetic no-match
search/clear, Albums card/list, six seeded subscription plans with monthly/yearly
views, login OFF, reload/back/forward/deep link. Supported developer logs had
no warnings/errors, not proof of all requests. AX represented a toggle as a
native checkbox while the DOM exposed a button; MA corrected the locator and
reported no app defect. Viewport was reset; no auth/media/payment occurred.

[WI062 evidence](../deliverables/agent/WI-20261011-ATS-062-evidence-pack.md#ready-http-and-browser-checkpoint)
records screenshot identity and limits: existing context, incomplete request
proof, simulated rather than physical mobile, and no new non-management-network
proof. The screenshot is not current-live or cleanup evidence. REQ007/WI062
remain active pending final comparisons, result and access closeout.

### 2026-10-11 About21:22-21:23 KST WI060 Dispatched, Final Pending

**WI060 native trial has started; current state is DISPATCHED/PENDING, not
READY, HTTP success or overall PASS.** MA started one trial after
[PG061 final ALLOW](../deliverables/agent/WI-20261011-ATS-061-evidence-pack.md)
on the same SE060 final freeze/bundle. PG closes F061-01/F061-02 on those bytes;
its local review is not native acceptance. MA `f1fff1` matched all81 manifest
files. Maintenance read-only `1dd3a6` showed certbot/apt services idle and the
nearest scheduled timer about8 hours away, a snapshot rather than a guarantee.

MA sequence: S0 `68083c` PASS; L1 `904916` local641/336/143 identity PASS at
21:22:40 KST and155 public files; S1 `309229` staged12 tools; S2 `ffa158` PASS;
S3 `cb5240` one approval; S4 `c2d14e` DISPATCHED/PENDING once. No READY/HTTP
receipt has arrived. MA reports no SMTP/auth/business mutations; this does not
substitute for final DB comparison. Final result and cleanup receipts are
required before trial PASS; access closeout is still pending.

[WI062 evidence and exact pins](../deliverables/agent/WI-20261011-ATS-062-evidence-pack.md#wi060-dispatch-checkpoint)
batch the previously queued freeze/maintenance update with this start receipt.
REQ007/WI062 stay active. Earlier NOT RUN and interim HOLD entries remain
historical; mail delivery, downstream feature completion and WI064 source-gate
release are not inferred from dispatch.

### 2026-10-11 STARTTLS Probe Only, Final Checkpoint Pending

MA receipt `fb62bc` reports a bounded remote OpenSSL STARTTLS probe to
`smtp.gmail.com:587` with hostname verification and `verify_return_error`:
exit0, `certificate_verified=true`. **No authentication or mail send occurred;
this is not delivery evidence or app-BPF SMTP permission.** No app/DB start
occurred. MA's CUA inbound inspection confirmed unchanged management22/443
and global80. Exact probe time was not supplied.

[WI064](../deliverables/agent/WI-20261011-ATS-064-handoff.md) is a separate
prepare-only assignment: patch/proposal/test-plan work under `build/WI064-audio-only/`
and its own reports, no application-source writes until MA releases the WI060
source-identity gate, and no new JAR before independent review. Assignment is
not implementation or validation completion. PG061 interim HOLD and native060
NOT RUN remain the latest reported trial state; final receipts remain pending.

### 2026-10-11 About21:13 KST Provider587 Applied, PG061 Interim HOLD

MA reports explicit user approval at action time for **provider outbound TCP587
only**. CUA saved the policy, received the authoritative success alert, and
subsequent outbound inspection showed TCP80/443/53/587 and UDP53/123, all IPv4.
This supersedes the earlier provider587-absent checkpoint only. App BPF remains
unchanged with no SMTP permission; no send, new cost or general-ingress change
is reported. Provider policy application is not SMTP delivery or a feature PASS.

PG061's interim verdict is **HOLD**: `F061-01` postgrant final DB skipped and
`F061-02` released grant underreported; MA relayed both to SE060. No finding
closure or final exact-byte ALLOW is reported, and **native060 has not run**.
REQ007/WI062 remain active; final candidate/review/trial/cleanup receipts are
pending. [WI062 evidence](../deliverables/agent/WI-20261011-ATS-062-evidence-pack.md#provider587-and-interim-review)
retains MA attribution and private-screenshot handling. Earlier entries below
remain dated history; the autonomous repair and mail-cost boundaries persist.

### 2026-10-11 WI063 Local Preparation And Profile Boundary

MA reports the test nickname is designated and reasonable synthetic remaining
profile fields are permitted; no recipient or profile values are recorded here.
Synthetic fields do not assert an actual phone/occupation or supply new consent.
[WI063](../deliverables/agent/WI-20261011-ATS-063-handoff.md) is assigned for
local-only first-admin promotion helper and bounded WAV fixtures, not buyer
grants, account creation or native promotion. No app-source change or new JAR
is reported. Assignment is not implementation, review or runtime completion.
REQ007/WI062 remain active awaiting final MA receipts; current access is still
not reported closed. No global documentation fixes are authorized by this update.

### 2026-10-11 WI062 Fresh Read-Only Receipts, Access Still Open

**REQ007/WI062 remain active; no new trial has run.** MA supplied fresh
post-restart read-only receipts: `81a9b0` reports systemd255.4-1ubuntu8.17,
kernel6.8.0-146-generic, app/DB masked with PID0, no Java and Nginx active;
`0ab7e7` reports empty jobs, absent old trial overlays and the same server JAR
identity as WI058 (70,306,351 bytes). Local `f086fd` at
**2026-10-11T20:59:32+09:00** reports641/336/143 identity PASS. These are bounded
read-only/identity results, not new trial admission, HTTP or feature acceptance.
Other receipts have no separately supplied precise time.

SSH agent is now temporarily **Running/Manual with the owned key; access is
not yet closed**, per MA. This supersedes the initial Stopped/Disabled checkpoint
below for current access tracking; historical cleanup remains preserved.
The user corrected the autofill interaction to click login once even when the
fields appeared empty, and MA reports the actual CUA click **SUCCESS** with no
missing login input. This records the interaction outcome only, without exposing
the site/account, inferring application authentication or claiming a new trial.

The current Gmail/no-new-service-cost test intent, later client-paid Naver
option and autonomous bounded repair/review/rerun authority below are unchanged.
Exact-byte WI060/WI061 gates and MA-only native execution remain required.
See [WI062 receipts and limits](../deliverables/agent/WI-20261011-ATS-062-evidence-pack.md#fresh-ma-receipts)
and the [REQ007 addendum](../deliverables/user/REQ-20261011-ATS-007.md#wi062-restart-addendum).

### 2026-10-11 WI062 Restart Checkpoint, Bounded Repairs Authorized

**REQ007 remains approved/active; WI062 is an initial checkpoint awaiting MA
receipts, not a runtime closeout.** MA reports that the app crashed, earlier
agents could not be found, and a replacement SE060 was spawned. At this reported
checkpoint there was no WI060 candidate and no new remote action. MA confirmed
the SSH agent Stopped/Disabled; DocOps did not independently inspect its state.
No precise checkpoint time was supplied. This app/session interruption is not
evidence that the target application crashed or restarted.

The latest human instruction authorizes feasible, bounded repairs, independent
reviews and fresh one-use reruns within REQ007 without repeated minor approval.
Major cost, destructive, security or design decisions still require escalation;
exact-byte review and current-target gates remain mandatory, and native actions
remain MA-only. Earlier statements requiring fresh approval for every next
diagnostic step are retained as history, not a new minor-approval requirement.
No prior consumed claim or review authorizes reuse against changed bytes.

**Mail intent:** no new service cost applies to the current bounded test using
existing Gmail. Later client-paid Naver service is acceptable in principle,
not an activation, purchase or integration instruction. New-host mail remains
OFF/unconfigured/unsent on the latest supplied evidence; provider TCP587,
application and actual delivery gates remain open. No provider price or free
quota is asserted.

The retained [WI058 HOLD and cleanup evidence](../deliverables/agent/WI-20261011-ATS-058-evidence-pack.md)
and [WI059 local design](../deliverables/agent/WI-20261011-ATS-059-evidence-pack.md)
are not new runtime receipts. [WI060](../deliverables/agent/WI-20261011-ATS-060-handoff.md)
must supply a fresh candidate, then [WI061](../deliverables/agent/WI-20261011-ATS-061-handoff.md)
must review its exact bytes before any MA trial. Feature runtime remains blocked;
neither WI058 HOLD nor the unknown BPF cause is upgraded to PASS.
See the [REQ007 addendum](../deliverables/user/REQ-20261011-ATS-007.md#wi062-restart-addendum),
[WI062 checkpoint evidence](../deliverables/agent/WI-20261011-ATS-062-evidence-pack.md)
and [summary](../deliverables/user/WI-20261011-ATS-062-summary.md).

### 2026-10-11 WI058 Completed Attempt HOLD, All Access Closed

**WI058 is complete as an attempted trial with HOLD, not native PASS. REQ007
remains active; feature runtime is blocked.** MA's approximately20:25-20:31 KST
native window reached admission PASS/app VERIFIED, HTTP allowed17/17 and
denied17/17 PASS, and B01-B07 existing-context desktop/simulated-mobile observations.
Final `dfb997` exited2/HOLD/POST_STATE_CHANGED at READ_ONLY_CHECKS, view FINISHED,
cleanup PASS. POST_VIEW policy passed; BPF IDENTITY_CHANGED at203459ms and final
DB NOT_RUN through short-circuiting. No whole-window business-DB-unchanged proof.

SE/PG read-only conclusions identify exact sorted effective-cgroup
`(direction, program_id)` tuple inequality, not semantic program/map change.
VIEW_RELOAD PASS means command success only.203459ms is comparison time, not
change time; raw before/after BPF snapshots were not persisted. Cause and policy
semantic equivalence remain NOT_DETERMINED; daemon-reload regeneration is only
plausible. This does not resolve historical WI042's cause or justify a retry.

S7 `b03b3a` remains HOLD/TRANSPORT_OR_STEP as the expected result gate, not a new
restoration failure. Independent `a07843` normal-TLS/no-proxy quarantine200/exact
body and `c8fed6` stopped/masked app/DB, no Java, absent trial overlays and active
Nginx corroborate cleanup.36 cleanup rows are PASS where applicable, receiver
close NA. Scoped key removed/agent empty (`e575e8`), agent Stopped/Disabled
(`837420`), owned recovery console exited to login/closed, trial tab closed and
viewport reset. Provider handoff tab retained, unrelated tabs untouched; no
active execution sessions remain. HTTP/UI success does not upgrade trial HOLD.

[WI058 evidence](../deliverables/agent/WI-20261011-ATS-058-evidence-pack.md) and
[summary](../deliverables/user/WI-20261011-ATS-058-summary.md) retain all receipts,
PG exact-byte approval, screenshot provenance and browser limits: existing
context, incomplete request proof, simulated rather than real mobile and no
non-management-network proof. [WI059](../deliverables/agent/WI-20261011-ATS-059-handoff.md)
local design is complete only; fresh diagnostic design/approval is the next gate,
with no automatic retry or new plan execution. [REQ007](../deliverables/user/REQ-20261011-ATS-007.md)
retains the conditional Gmail fallback choice; new-host mail is OFF/unconfigured/
unsent, provider587/application/delivery gates pending, and no Naver activation,
purchase or integration was applied. Prior checkpoints below remain history.

### 2026-10-11 SE055 Final Freeze, Independent Review Pending

**SE055 final freeze confirmed with no further edits, as relayed by MA. WI056
final PG verdict remains pending; WI058 stays active and native changes/trial
remain NOT RUN.** Final runtime-bundle SHA-256:
`97c7c6bbc0129787424b794f4dcdbb55efb4ab60246a9ad705d0a9363d6a15a4`;
freeze SHA-256:
`733af373ed54dec8049bb9b71cc40af3f9f18f530c5af86a7cab204aa5cf43e7`.
MA relayed the final pins and SE055's all9 local groups exit0, including
candidate130 and PG-derived16. Counts overlap and are not added as unique tests;
PG-derived local cases are not the independent final verdict. No precise time
was supplied for this update. [WI058 evidence](../deliverables/agent/WI-20261011-ATS-058-evidence-pack.md)
records attribution and limits. Earlier F3 repair/pending-freeze checkpoints
below retain their historical scope; the owner freeze does not establish PG
closure or native success. Final recipe/review, native and cleanup receipts
remain required under [REQ007](../deliverables/user/REQ-20261011-ATS-007.md).

### 2026-10-11 20:05 Read-Only Refresh, Final Candidate Still Pending

**WI058 remains active; native changes/trial and final cleanup NOT RUN.** MA
receipt `c33edf` reports no pending systemd jobs and certbot/apt services idle,
with no service changed; its individual clock time was not supplied. Provider
UI refresh at approximately20:04 KST showed unchanged inbound management22/443
and global80 for ACME-path use; approximately20:05 outbound showed TCP80/443/53
and UDP53/123, with SMTP TCP587 absent. No UI changes or SMTP test occurred.
These are bounded observations, not future job inactivity or new-host mail
readiness. Gmail local-reference availability and target setup/delivery NOT RUN
remain distinct. SE055 is still repairing PG F3's result-publication error
before final freeze; WI055 final identity and WI056 final PG verdict remain
pending. [WI058 evidence](../deliverables/agent/WI-20261011-ATS-058-evidence-pack.md)
and [REQ007 progress](../deliverables/user/REQ-20261011-ATS-007.md) retain the
earlier observations and all native/cleanup gates. No completion is asserted.

### 2026-10-11 REQ007 Read-Only Checkpoint, New Trial NOT RUN

**REQ007 approved/active; WI058 remains active. Final WI055 candidate and WI056
PG verdict are pending. Native changes, final-candidate preflight, startup,
app HTTP/browser, trial result and final cleanup are NOT RUN.** MA observations
at approximately19:37-19:43 KST report PASS_LOCAL_IDENTITY_ONLY for641 backend,
336 frontend and143 SPA inputs, matching staged JAR, DB masked/inactive, no
running app, original Nginx active/quarantine HTTP200 and actual WI042 overlays
absent. These are attributed pre-trial snapshots, not final execution gates or
post-trial restoration. Provider22/443 remain management-line only in that
snapshot;80 is global for ACME-path use only. Temporary owned-key SSH agent is
Running/Manual and recovery console authenticated; access cleanup is NOT RUN.

The user permits existing Gmail for bounded function testing now and a later
switch. No client-owned Naver Cloud account exists yet; future client-owned
Naver SENS MAIL is MA's recommendation, with no new service activated. MA receipt
`4d347c` confirms an available historical local mail reference through bounded
regular/non-link and expected-field presence checks only. No credential values
were printed or copied to the server and no SMTP send occurred. New-host SMTP
setup/delivery remain NOT RUN; prior Gmail TEST evidence is preserved, not
promoted to new-host mail readiness.

[WI058 evidence](../deliverables/agent/WI-20261011-ATS-058-evidence-pack.md) and
[summary](../deliverables/user/WI-20261011-ATS-058-summary.md) separate receipt
owners, timing, identity and stage limits. [WI057](../deliverables/agent/WI-20261011-ATS-057-evidence-pack.md)
source audit is complete; [WI059](../deliverables/agent/WI-20261011-ATS-059-handoff.md)
local feature planning is active. Feature runtime still needs web/cleanup PASS
and its own gates. [REQ007](../deliverables/user/REQ-20261011-ATS-007.md) is the
current progress authority. Earlier REQ006 failures/recovery remain historical;
original POST_STATE_CHANGED cause remains NOT_DETERMINED. No full web, public
launch, feature or capacity PASS is asserted.

### 2026-10-11 Local State Diagnostics Complete, Native HOLD Unchanged

**WI052 v2 -> WI053 -> WI054 local amendment COMPLETE; PG verdict
LOCAL_DIAGNOSTICS_READY. REQ006 remains active/HOLD and NOT_NATIVE_READY.**
The candidate records 15 planned observations across policy/BPF/DB checks and
existing reload markers with PASS/DIFFERENT/ERROR/NOT_RUN results. Sequence/elapsed_ms
records detection time, not exact change time. Security comparisons, read order
and short-circuit behavior remain; raw values/IDs/secrets are not exported, and
both CLI entrypoints reject LOCAL_ONLY. [SE052 v2](../deliverables/agent/WI-20261011-ATS-052-evidence-pack.md#v2-repair-addendum)
repairs only two projection functions after the preserved v1 PG13 PASS/2 FAIL.
[Final PG053](../deliverables/agent/WI-20261011-ATS-053-evidence-pack.md#v2-independent-re-review-addendum)
closes both findings: independent15/15, candidate rerun100/100, original106/106;
these overlap, including15 copied PG tests in100. All are Windows/Python3.14.3
synthetic/mocked checks, not native, durability or capacity proof.

Exact v2 freeze SHA-256: `0c007d886750dd4a27bc606c45be62e2e8103db4a475e058db79751d7a8760ec`.
[WI054 evidence](../deliverables/agent/WI-20261011-ATS-054-evidence-pack.md) and
[summary](../deliverables/user/WI-20261011-ATS-054-summary.md) record identity and limits.
Original cause NOT_DETERMINED, HTTP wrapper/cross-filesystem cleanup/native
packaging limits and historical HTTPS/access closeout are unchanged. No new
trial, app restart or public opening is authorized. No added cost or user input;
existing server charges continue.

### 2026-10-11 Post-View Failure Located, Exact Operand Undetermined

**WI050 -> WI051 diagnostic-only work is complete; REQ006 remains active/HOLD.**
PG located the failed second comparison at runtime.py:1013 after the viewing
window. Its unequal operand is NOT_DETERMINED: policy/BPF/DB snapshots and branch
outcomes were not retained, and short-circuit evaluation may skip the final DB read.
The view's own daemon-reload makes BPF identity replacement a credible hypothesis,
not native proof. Raw deny-order/UnitFileState differences are other candidates;
no actual count mutation or whole-window DB immutability is established.
[PG050](../deliverables/agent/WI-20261011-ATS-050-evidence-pack.md) records13 pure
synthetic assertions PASS, not a native rerun. [WI051 evidence](../deliverables/agent/WI-20261011-ATS-051-evidence-pack.md)
and [summary](../deliverables/user/WI-20261011-ATS-051-summary.md) integrate the
result without changing prior recovery/access closeout or raw receipts. No new
operation, purchase or manual input; any implementation/trial needs separate approval.

### 2026-10-11 Original HTTPS and Access Cleanup Restored, Web Goal HOLD

**WI045 bounded recording/recovery/access closeout is complete. REQ006 remains
active/HOLD: the original web objective is not complete.** After fresh provider
and target checks at18:04:42 KST, MA ran exact WI048 v2 L1/L2 once each. L2 exited0,
RECOVERED_ORIGINAL_HTTPS, nginx_start VERIFIED, durability PASS,12 checks PASS;
trial_status remained HOLD. The18:05:40 certificate-validated, no-proxy quarantine
GET returned200/exact body. At18:05:42 original Nginx was active, app/DB masked
PID0, TLS and retained evidence matched. Provisional disk PENDING_DURABILITY is
not the success authority; bounded stdout and separate observations are.

Owned-key removal/empty agent at18:06:23 and Stopped/Disabled at18:06:28 passed.
At18:08 (minute precision), the user's logout reply and MA's explicit tty1 login
prompt confirmed no root shell. Owned noVNC tab closed; provider tab retained
for handoff. No required native sessions remain. No app/DB trial was repeated.

WI042's HTTP17 inner PASS/wrapper HOLD, POST_STATE_CHANGED cause NOT_DETERMINED,
cleanup FAILED and WI046's consumed HOLD remain unchanged. L3/browser/S7 are
NOT_RUN; this is neither full web acceptance nor production/capacity approval.
[WI045 evidence](../deliverables/agent/WI-20261011-ATS-045-evidence-pack.md),
[receipt](../deliverables/agent/WI-20261011-ATS-045-native-receipt.json),
[summary](../deliverables/user/WI-20261011-ATS-045-summary.md) and
[REQ006](../deliverables/user/REQ-20261011-ATS-006.md) retain the waiting checkpoints
below as history; provider login/recovery/access cleanup are no longer pending.

### 2026-10-11 Web Trial HOLD, HTTPS Recovery Waiting for Login

**WI045 CHECKPOINT, not completion: REQ006 is active/HOLD. MA is awaiting the
user's provider login. WI048 v2 L1/L2 are NOT_RUN; original Nginx remains inactive,
app/DB stopped and masked, and SSH/noVNC cleanup is NOT_YET_DONE.** Latest access
observations are minute-precision17:52 root console logged in and17:54 agent
Running/Manual with the expected sole key, retained under the recovery approval.

WI042 reached app VERIFIED at17:01:12. L2 reported17 HTTP200/PASS cases, but its
wrapper exited1/HOLD_ALLOWED_HTTP; L3/browser were NOT_RUN. Final trial result was
HOLD/POST_STATE_CHANGED with cleanup FAILED (25 PASS/2 NOT_APPLICABLE/9 FAILED).
That primary cause remains NOT_DETERMINED. WI046 preserved/detached both overlays
but stopped before Nginx start on After ordering; its consumed HOLD is unchanged.

[WI048](../deliverables/agent/WI-20261011-ATS-048-evidence-pack.md) and
[PG049 v2](../deliverables/agent/WI-20261011-ATS-049-evidence-pack.md) establish local
readiness and CONDITIONAL_ALLOW only. MA matched14 frozen files at17:45:07 and
passed the bounded read-only target gate at17:51:18. TLS pins matched at17:52:30;
the peer-check false was resolved at17:52:55 as sudo environment loss, not network
drift. Provider session expiry still blocks fresh firewall UI confirmation.

[WI045 evidence](../deliverables/agent/WI-20261011-ATS-045-evidence-pack.md),
[receipt](../deliverables/agent/WI-20261011-ATS-045-native-receipt.json),
[summary](../deliverables/user/WI-20261011-ATS-045-summary.md) and
[REQ006](../deliverables/user/REQ-20261011-ATS-006.md) separate these observations.
No recovery, full web acceptance, production release or capacity PASS is claimed.
Prior checkpoints below retain their dated authority and outcomes.

### 2026-10-11 Local Parser Preparation Complete, Native HOLD Unchanged

**WI042 -> WI043 -> WI044 local-only preparation is complete. PG043 verdict:
LOCAL_READY, no blocking defect found in the bounded repair. REQ006 remains
operational HOLD; no new native/SSH/agent/secret-use authority is granted.**

[WI042](../deliverables/agent/WI-20261011-ATS-042-evidence-pack.md) records ordered
Exec lists and shared pre-claim parser checks, with106/106 owner Python tests
and8/8 synthetic PowerShell assertions. [WI043](../deliverables/agent/WI-20261011-ATS-043-evidence-pack.md)
independently passed9/9 focused tests, reran106/106 owner tests and8/8 assertions,
and matched27 frozen files,11 recipe bodies and79 historical pins. The genuine
prepare/JSON/restore and exact S2 body ran with mocked effects; repeated-command
fixtures are repository-derived/synthetic, not fresh native output. Overlapping
counts are not unique native proofs or a coverage percentage.

Exact bundle: `78ce821757111b6f4bd440a79680ed12e4e40b6592616293e4d768ddd6dbe00c`.
MA independently matched freeze/bundle/review set/README/11 blocks at16:39:57 KST
(`c0f2b7`), then the pinned QA036 helper matched641 backend inputs,336 frontend
inputs,143 SPA files and the unchanged JAR at16:40:17.341 (`5ea2e5`). No rebuild
or new target operation occurred. Even prepared S2 requires new native approval.

Historical WI039 PREPARE/DUPLICATE_JSON/HOLD and WI041's16:18 access cleanup remain
unchanged. Prior WI035/WI038/WI040 and WI034 scoped success are preserved; WI026's
older cleanup cause remains NOT_DETERMINED. Native loaded-MySQL compatibility,
startup/restoration, HTTP/browser and public/capacity acceptance remain unproved
by this local work. [REQ006 local-only closeout](../deliverables/user/REQ-20261011-ATS-006.md),
[WI044 evidence](../deliverables/agent/WI-20261011-ATS-044-evidence-pack.md) and
[summary](../deliverables/user/WI-20261011-ATS-044-summary.md) separate these scopes.

### 2026-10-11 Fresh Web Attempt HOLD, Access Cleanup Confirmed

**REQ006 remains active/HOLD. MA's once-only WI039 attempt ended at 16:15:40 KST
with PREPARE / DUPLICATE_JSON / HOLD, app NOT_RUN.** The exact 15 cleanup rows
are 6 PASS and 9 NOT_APPLICABLE, not a full 36-step PASS. DB start, OPEN,
L2/L3 and application-browser checks were NOT_RUN; baseline was never created.

PG's final bounded diagnosis confirmed a P2 parser defect: Nginx has two valid
ExecReload commands, but Session.service applies r.unique and rejects them with
the misleading DUPLICATE_JSON code. Public contract duplicate-key count was0.
Local tests missed this valid multi-command shape. The preserved
[WI040 conditional ALLOW](../deliverables/agent/WI-20261011-ATS-040-evidence-pack.md)
is review-time evidence; the new native HOLD governs current readiness.

MA's 16:14:47 identity check matched641 backend inputs,336 frontend inputs and
143 SPA files, with the unchanged JAR/SPA pins. [WI039](../deliverables/agent/WI-20261011-ATS-039-evidence-pack.md)
records95/95 owner mocks and8/8 PowerShell assertions; PG reran95/95 plus9/9
focused tests and8/8 assertions. These overlapping local results do not prove
native behavior. Exact bundle: `fb643ff04e5c0f1e90f1b110eafa312c311a3d560a822769ff43f5ed432d379b`.

At16:17:12 the pinned legacy read-only preflight remained HOLD22/23, solely
because historical approval-absence was false; all other checks were true.
At16:18:03 the supervisor was loaded/static/failed/PID0, app not-found/inactive/
dead/PID0, and MySQL masked/inactive/dead/PID0. The app was not a masked unit.
Owned key removal/no identities was confirmed16:18:03; agent Stopped/Disabled
16:18:23; user logout and MA login-prompt screenshot16:18:30. The recovery tab
closed; provider portal remained. At16:18:43 normal-TLS, no-cookie/proxy/redirect
HTTPS returned200 and the exact quarantine body, not restricted-app acceptance.

Frozen WI035/WI038/WI040 history and prior WI034 BOUNDED_PASS remain intact;
WI026's old cleanup cause is still NOT_DETERMINED. A new user approval covers
local-only parser repair/tests/review/preflight preparation via WI042 -> WI043
-> WI044, with no renewed native/start/secret-use permission. No new trial.
[REQ006](../deliverables/user/REQ-20261011-ATS-006.md),
[WI041 evidence](../deliverables/agent/WI-20261011-ATS-041-evidence-pack.md),
[receipt](../deliverables/agent/WI-20261011-ATS-041-native-receipt.json) and
[summary](../deliverables/user/WI-20261011-ATS-041-summary.md) retain the limits.

### 2026-10-11 Restricted Web Attempt Held Before Application Start

**REQ006 remains active/HOLD: the consumed WI035 attempt returned authoritative
PREPARE / WEB_SERVICE / HOLD at 15:31:09 KST, app NOT_RUN.** Its 15 cleanup rows
are exactly 6 PASS and 9 NOT_APPLICABLE, not a full 36-step PASS. S4 was dispatched
once at 15:30:48; OPEN was never reached. DB start, PC HTTP assertions L2/L3,
application browser navigation and S6/S7 were NOT_RUN.

MA refreshed identity at 15:28:35: 641 backend inputs, 336 frontend inputs and
143 SPA files matched [WI036](../deliverables/agent/WI-20261011-ATS-036-evidence-pack.md).
[WI035](../deliverables/agent/WI-20261011-ATS-035-evidence-pack.md) records local
Python85/85 and PowerShell8/8 PASS; [WI037](../deliverables/agent/WI-20261011-ATS-037-evidence-pack.md)
records PG's conditional exact-byte ALLOW and target-free 85/85, 11/11 and 8/8
checks. These are local evidence, not native web or browser acceptance. Provider
portal expiry was followed by user reauthentication; MA's fresh management-rule
and root-recovery observation was approximately 15:28 KST, not an evergreen gate.

MA's 15:32 read-only diagnosis found absent optional Exec properties rejected
by Session.service before baseline creation; a separate masked-MySQL empty-Type
predicate would also reject. SE/PG concurred; `--all` alone is not a fix. No
repair was applied to WI035. Original Nginx PID11156/TLS pins remained unchanged,
MySQL masked/inactive/PID0 and new app not-found/PID0. At 15:39:42 the old
preflight was still 22/23, with only retained historical approval-absence false;
at 15:39:43 MA's normal-TLS, no-redirect/no-cookie HTTPS check returned the exact
quarantine response. No web configuration switch or restoration cycle occurred.

Owned SSH key removal/no identities was confirmed at 15:40:09. Restore UAC was
cancelled; agent remained Running/Manual at 15:43:54. User logout plus MA's
approximately 15:46 login-prompt observation confirmed root-console logout.
Temporary agent continuation is separately approved for the new work, not final
Stopped/Disabled restoration. The user approved a fresh repair/review/one-trial
chain: WI039 SE -> WI040 PG -> MA conditional fresh attempt -> WI041 DocOps;
the new attempt has not started. Frozen WI035 and its HOLD remain preserved.

[REQ006 amendment](../deliverables/user/REQ-20261011-ATS-006.md),
[WI038 evidence](../deliverables/agent/WI-20261011-ATS-038-evidence-pack.md),
[sanitized receipt](../deliverables/agent/WI-20261011-ATS-038-native-receipt.json)
and [summary](../deliverables/user/WI-20261011-ATS-038-summary.md) record scope.
WI034 BOUNDED_PASS and the older WI026 HOLD/cause NOT_DETERMINED remain dated
history. Existing popular-album/latest and Tag-document discrepancies stay
deferred. No login/media/payment/mail, capacity, pristine-browser, complete
traffic-capture or production acceptance; no additional paid product.

### 2026-10-11 Bounded Private Diagnostic Run and Cleanup Passed

**REQ005 scoped run complete: MA's single native attempt at 14:09:01 KST
returned BOUNDED_PASS, startup VERIFIED and all 23 cleanup steps PASS.** The
separate 14:09:35 read-only postcheck found app/DB PID0, both masks, no worker
processes or jobs, matching normalized listener endpoints and TLS/JAR pins.
The app remained masked/failed with exit143; the predicate accepts this stopped
state, not proof of graceful shutdown. MySQL was inactive/dead/masked.

MA matched 641/641 source inputs at 13:58:31 and the unchanged 70,306,351-byte JAR,
SHA-256 `01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb`.
[WI032 owner evidence](../deliverables/agent/WI-20261011-ATS-032-evidence-pack.md)
records 88/88 local mocked tests; [WI033](../deliverables/agent/WI-20261011-ATS-033-evidence-pack.md)
records PG's rerun 88/88 plus 7/7 focused tests and exact-byte conditional ALLOW.
These overlapping local executions are separate from MA's native result.
Runtime SHA-256: `189342bef8ac90496b59efe754efbc4f846a582efd409f56e19dc8adaa16857f`;
bundle: `11db69c6d82da14083864ea7e14fa239f0823e2665b5d1c3048c70edf9271dc4`.

Final preflight at 14:08:12 was 22/23, not blanket PASS: only the retained
historical approval-absence predicate was false. All nine fresh WI032 targets
were separately absent at 14:08:20. Four-file upload, root staging and fresh
approval preceded the single attempt. SSH identity removal/no identities and
agent Stopped/Disabled were confirmed by 14:10:24. At approximately 14:13 KST,
the user confirmed console logout and MA's screenshot showed the tty1 login
prompt; the owned recovery tab closed, while the provider main console remained
as handoff. No root recovery shell was left open in that observation.

**Prior WI026 cleanup FAILED/HOLD and cause NOT_DETERMINED remain unchanged;
the failure did not reproduce, and no retrospective correction is claimed.**
REQ003 retains its historical active/HOLD record. Proof is on-host IPv4/BPF only;
Internet/IPv6 delivery proof, public release and full application/load/backup
acceptance remain outside this result. No further trial is authorized.
[REQ005 closeout](../deliverables/user/REQ-20261011-ATS-005.md),
[WI034 evidence](../deliverables/agent/WI-20261011-ATS-034-evidence-pack.md),
[public receipt](../deliverables/agent/WI-20261011-ATS-034-native-receipt.json) and
[summary](../deliverables/user/WI-20261011-ATS-034-summary.md) separate the owners,
times and limits. Earlier checkpoints below retain their dated scope.

### 2026-10-11 Local Cleanup Diagnostics Allowed, Native HOLD Unchanged

**REQ004 local-only scope complete: implementation, independent review and WI031
documentation validation passed. No native execution approval.** Candidate
`build/WI029-cleanup-diagnostics/runtime.py`,49,865 bytes, SHA-256
`209a124f40e6ad55ffb45231d1048970209c33d2f331babb8b0ae8445e3b0e7b`.
[WI029](../deliverables/agent/WI-20261011-ATS-029-evidence-pack.md) records SE's
67/67 mocked tests; [WI030](../deliverables/agent/WI-20261011-ATS-030-evidence-pack.md)
records PG's independent rerun67/67 plus17/17 adversarial tests and
`ALLOW_LOCAL_DIAGNOSTIC_CANDIDATE`. Both verified on Windows/Python3.14.3,
2026-10-11; overlapping executions are not unique native proofs.

The local candidate reports up to23 fixed cleanup/baseline steps, safe bounded
failure metadata, continued best-effort checks and sticky HOLD. CLI rejects
native invocation with `LOCAL_ONLY`. Per-command limits remain; additional
failure-path checks may take longer, with no aggregate cleanup deadline claimed.
MA and PG confirmed six candidate pins,38 historical files and the WI028 receipt;
MA additionally confirmed seven baseline key artifacts, including the JAR,
unchanged. No deployment, source/JAR rebuild or native operation occurred.

**REQ003 remains active/HOLD: prior app VERIFIED / cleanup FAILED, cause
NOT_DETERMINED.** This change cannot reconstruct missing historical return codes
or establish retrospective restoration PASS. No further native WI is approved.
[REQ004](../deliverables/user/REQ-20261011-ATS-004.md),
[WI031 evidence](../deliverables/agent/WI-20261011-ATS-031-evidence-pack.md) and
[summary](../deliverables/user/WI-20261011-ATS-031-summary.md) record local scope.
The MA-owned native checkpoint and original receipt below remain unchanged.

### 2026-10-11 Private Backend Startup Verified, Cleanup HOLD

**REQ003 remains approved/active/HOLD. First bounded private backend startup
and two GET checks succeeded; automated cleanup FAILED. No overall PASS or
publication approval.** All native observations below belong to MA on2026-10-11
KST; DocOps integrated them without new target access.

MA verified641/641 source inputs unchanged at13:03:30, dirty
`main/58b1011dad796017e47dbcc82a4225ca449bcdf6`. New JAR:70,306,351 bytes,
SHA-256 `01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb`;
no rebuild. [WI027 conditional ALLOW](../deliverables/agent/WI-20261011-ATS-027-evidence-pack.md)
covered bundle `7136e749de6b7dd2ae6ecbf2585a3dc1ddb0fd083ef81ab48f96e2f82aa44f05`
and the exact [frozen recipe](../build/WI026-private-start/recipe-pins.json).

The preserved WI024 attempt at12:56:16 stopped at PREPARE/PATH_ANCESTRY before
app start; cleanup PASS. WI026 used a new root-only log path and fresh attempt
names without weakening `/var/log` permissions. At13:07 MA mistakenly selected
an older preflight (`fbd216` prefix); its mysql_config failure was superseded by
the already-approved [current preflight](../build/WI014-final-xbind-preflight-80d4a7.py),
SHA-256 `6b1d7ed691b37043af778598decc46bf73e12b7c08f47b7d0f1128fce44de383`.
Its13:20:09 result was23/24 true: only historical approval-file absence was false,
with that record preserved. No configuration repair or blanket24/24 PASS.

The once-only WI026 invocation returned at13:20:33.780: `app_start=VERIFIED`,
`status=HOLD`, `cleanup=FAILED`, `CLEANUP_VERIFICATION_FAILED`, SSH exit2.
The proof was **ON_HOST_ONLY**: TCP/UDP positive controls2 each, negative
deliveries0, BPF attachment checked; Internet and IPv6 delivery proof NOT_RUN.
Readiness/strict-audit markers and one capabilities GET plus one empty Tag DTO
GET were observed. Capabilities verified authentication-feature flags OFF,
not scheduler binding. Background OFF relies on exact unit/overlay/source
review plus no named worker threads. Equal before/after counts across43 tables
are bounded observations, not a complete SQL audit or proof against UPDATE or
transient DML; no intentional business DB operation was reported.

MA's separate13:22:09 postcheck found no named app/DB processes, pending jobs or
specified app/DB/receiver ports; MySQL inactive/masked, app unit masked/failed with PID0,
unchanged9-row listener baseline and three TLS pins. App failed/masked is an
accepted cleanup state and does not identify the failure. Missing per-step
diagnostics leave the cleanup cause **NOT_DETERMINED**; the postcheck neither
rewrites the failed receipt nor proves full restoration PASS. At13:23:05 the
owned SSH key was removed, no agent identities remained and Stopped/Disabled
was restored. This fresh attempt is exhausted; no automatic retry.

[REQ003 current progress](../deliverables/user/REQ-20261011-ATS-003.md),
[WI028 evidence](../deliverables/agent/WI-20261011-ATS-028-evidence-pack.md),
[sanitized receipt](../deliverables/agent/WI-20261011-ATS-028-native-result.json)
and [summary](../deliverables/user/WI-20261011-ATS-028-summary.md) distinguish
startup success from unresolved cleanup. Canonical `app_runnable=false` and
production HOLD remain. No full-site/frontend/auth/media/load/mail/backup test
or external publication; diagnostic changes or another attempt need new approval.
Earlier checkpoints below remain historical, including REQ002's local-only state.

### 2026-10-11 Local First-Start Candidate Complete, Production HOLD

**REQ002 local preparation and WI016-021 complete; no upload, native validation
of the new JAR, full application start or publication.** Current source is the
dirty `main/58b1011dad796017e47dbcc82a4225ca449bcdf6` checkout, not HEAD alone.
QA built offline on Windows 11 / Java17 / Gradle9.3.0 at11:25:45-11:26:15 KST;
final local checks passed at11:29:41 KST. Exact new candidate:
`build/WI020-release-v3/build/libs/ATStudio-0.0.1-SNAPSHOT.jar`, 70,306,351 bytes,
SHA-256 `01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb`.
The [WI020 manifest](../deliverables/agent/WI-20261011-ATS-020-artifact.json)
has SHA-256 `8a74e29f15708dad5a8278dffc3e8add1eb7b8dbcd6ddb18045ec85c32c63fe6`.

Retain old `build/libs/ATStudio-0.0.1-SNAPSHOT.jar` and its WI026 manifest;
the old JAR is not a background-OFF fallback. The separate SPA at
`build/WI-20261010-ATS-026-frontend-dist` is reusable: QA matched143 files and
the historical336 frontend inputs, preserving the four coverage-test exclusions
listed in [WI020 evidence](../deliverables/agent/WI-20261011-ATS-020-evidence-pack.md).
No frontend rebuild or full source/typecheck equivalence is claimed. The legacy
`release-readiness.py` still pins the old artifact: new backend drift is expected,
not a regression verdict. Never update historical pins to validate this release.

SE68/68 and independent RE87/87 are overlapping evidence: RE reran those68 and
added19, not155 distinct tests. Candidate SA19/19 and PG27/27 are bounded
static/synthetic checks, with nested owner cases not counted again. PG's initial
eight surviving mutations/HOLD remain historical, superseded by the repaired
checker and [independent PASS](../deliverables/agent/WI-20261011-ATS-018-evidence-pack.md#2026-10-11-independent-repair-retest-local-pass).
Master OFF suppresses scheduled/ready/private-executor work, not manual calls,
AFTER_COMMIT work or storage-root creation; strict storage/JWT/keyring/DDL guards
remain. MA independently matched1,130 unique public records plus six immutable
pins at11:32:40 KST on Windows; SSH agent Stopped/Disabled. DocOps attributes
these observations to MA, not to a new target inspection.

WI015's MA Linux read-only Hibernate PASS at10:34:46 KST remains valid for its
exact old probe/target. QA found136 model classes, schema/seed and101 libraries
identical between JARs; that narrows mapping drift, not new-executable native
validation or startup. Canonical fresh manifest remains UNRECORDED and
`app_runnable=false`. The candidate false gate/new-JAR placeholder remain intact.
Linux syntax, effective systemd/BPF IPv4/IPv6 denial, loopback/Unix relay checks,
actual OFF binding and bounded startup/cleanup require fresh target evidence.

**Next requires separate exact remote approval** for staging the new identity,
existing-secret use, internal DB and fence proof, then bounded private app start
with no external route. The user enters the SSH key privately in that approved
window, not now. Historical Gmail TEST success remains valid; production SMTP,
initial ADMIN, backup/restore, NS, expiry alerts, first unattended renewal,
actual reboot and capacity gates remain open. No extra purchase/install, SSH,
private-env read, secret/DB operation or publication occurred under REQ002.

Follow [REQ002 scoped completion](../deliverables/user/REQ-20261011-ATS-002.md),
[WI021 evidence](../deliverables/agent/WI-20261011-ATS-021-evidence-pack.md) and
[WI021 summary](../deliverables/user/WI-20261011-ATS-021-summary.md).
No further WI remains in this approved chain. All earlier checkpoints below
retain their original observation and approval boundaries.

### 2026-10-11 Native Read-Only Schema PASS, Scoped REQ Complete

**REQ001 bounded verification and WI015 integration complete; full production
HOLD.** MA's approved Linux target window returned native/Hibernate VALIDATE_PASS
at **01:34:46 UTC / 10:34:46 KST**: 43 tables, 520 columns, 234 index entries,
91 foreign-key columns. Before/after bootstrap observations matched; probe exit0,
stop0/mask0, cleanup_verified=true, owned process group clear and restricted TLS
unchanged. MA's independent postcheck passed at01:35:19 UTC /10:35:19 KST.
DocOps integrated supplied sanitized receipts, not a new remote verification.

Exact v5 bundle SHA-256
`463c95667ee51e5fe2f01303a43ccebe0c0547375a374730c511c924212c7e54`;
operator `64dc16d9b74315647497b9d335e5e309e27f544e03a5f76df35cb66fe0288136`;
final preflight `6b1d7ed691b37043af778598decc46bf73e12b7c08f47b7d0f1128fce44de383`.
WI014's latest local predicate review was52/52 PASS; candidate SE55/PG26 and
operator SE19/PG19 remain distinct historical local results. Fresh MA preflight
passed at01:34:32 UTC; root stage178 files/113653377 bytes, Linux PLAN_ONLY and
no-connect MAPPING_READY preceded the single actual operator run. Earlier SCP
0664 staging rejection (before root creation) and preflight conflicts/known
mysqlx-bind HOLDs were preparation gates, not DB validation failures.

MA confirmed MySQL inactive/persistently masked, no DB/app ports or jobs, app
absent and Nginx/Certbot timer retained. Root tty1=0 at01:36:14 UTC; exact owned
public key removal exit0, empty agent rc1, restore exit0 and Windows
Stopped/Disabled at10:36:26 KST, with a later fresh recheck still restored.
Owned NoExit window and console23 closed; instance22 handoff and user tab9
preserved. Original tab18 closed outside this cleanup, not by it.

**This supersedes the current native NOT_RUN judgment below, not its historical
facts.** Canonical fresh manifest remains UNRECORDED, app_runnable=false; no app
launch/publication, schema/DML/secret change or additional purchase/install.
NS, expiry alerts, first unattended renewal/actual reboot, backup/restore,
mail/ADMIN, worker/egress fencing and full application startup remain separate
gates. A read-only Hibernate PASS is not canonical-schema or deployment approval.

Follow [REQ001 scoped completion](../deliverables/user/REQ-20261011-ATS-001.md),
[WI015 evidence and receipt ledger](../deliverables/agent/WI-20261011-ATS-015-evidence-pack.md),
[WI015 summary](../deliverables/user/WI-20261011-ATS-015-summary.md) and
[WI014 latest preflight review](../deliverables/agent/WI-20261011-ATS-014-evidence-pack.md#latest-addendum-exact-loopback-x-bind).
The following dated entries retain their original cutoffs and approvals.

### 2026-10-11 Local v5 Review PASS, Native NOT_RUN

**WI011 correction and WI012 exact-v5 scoped review PASS; WI008 documentation
integration complete. REQ001 remains approved/active, production HOLD pending
separate native authority and gates.** SE verified55/55 local tests through
02:30 KST; PG independently verified26/26 against prepared v5 at02:33 KST on
Windows/PowerShell, Java17.0.12. PG's prior29/29 JDBC guard evidence is reused,
not added to its new26-test count. Source HEAD remains
`main/58b1011dad796017e47dbcc82a4225ca449bcdf6`; DocOps rechecked the12 public
source/manifest/example/contract/runbook pins without rebuilding or rerunning tests.

Candidate `build/WI011-schema-v5`, bundle SHA-256
`463c95667ee51e5fe2f01303a43ccebe0c0547375a374730c511c924212c7e54`,
accepts only the exact `SPRING_CONFIG_LOCATION="classpath:/"` line. The full
83-key public-bootstrap synthetic fixture accepts v5 and reproduces ENV_KEYS
with pinned v4. External/import/other configuration, injection, duplicate and
malformed-input rejection remains. PG rehashed171 generated files identical
across v4/v5 and178 v5 staged inputs; this is local evidence, not a v5 upload.
Backend638/frontend336/SPA143 comparisons and four historical exclusions remain.

**No v5 operator or remote retry is approved.** Old rev3 and old approvals are
invalid for v5. Next: new explicit access/secret-use approval, separately
reviewed exact v5 signal-aware operator, fresh target/capture/identity/TLS gates,
then bounded read-only native schema validation. No new remote/DB/SSH-agent
operation occurred after the02:24 cleanup. Native schema remains NOT_RUN,
canonical fresh manifest UNRECORDED, app_runnable=false; app not started or
published. Prior cleanup/TLS receipts below remain historical, not fresh probes.
NS, alerts, unattended renewal/reboot and remaining startup gates remain open.

Follow [REQ001](../deliverables/user/REQ-20261011-ATS-001.md),
[WI011 evidence](../deliverables/agent/WI-20261011-ATS-011-evidence-pack.md),
[WI012 exact review](../deliverables/agent/WI-20261011-ATS-012-evidence-pack.md)
and [WI008 final integration](../deliverables/agent/WI-20261011-ATS-008-evidence-pack.md#latest-v5-local-result-addendum).
The prior v4 HOLD, pending-review checkpoint and receipt history below retain
their original cutoffs; local v5 PASS does not convert the stopped attempt to PASS.

### 2026-10-11 Schema Probe Environment Compatibility HOLD

**REQ001 approved/active, production HOLD; native operator executed, schema
child NOT_RUN; this window's cleanup verified.** WI008 integrates MA's
2026-10-11 02:18-02:24 KST receipts, not a DocOps native verification. Source
`main/58b1011dad796017e47dbcc82a4225ca449bcdf6`; backend638/frontend336/SPA143
recorded comparisons match per WI006/009/010, with the four exact historical
frontend coverage-test exclusions retained in the WI008 ledger. This is not
complete frontend source/typecheck equivalence or an application rebuild.

Exact v4 `build/WI009-schema-v4` bundle:
`f2e59839caa096fff83b427ad47563be8f7b96c58bdd2825bda9216e109eaeec`.
MA verified178 root-staged public files/113653320 bytes, including the copied
retained pinned JAR; Linux plan and no-connect MAPPING_READY preceded execution.
The separately pinned operator rev3
`85384f2c118119e96e2166f8456448dcf16ad5a915798b0be211db4f246b5189`
temporarily started loopback MySQL, then stopped at APPROVED_ENV_VALIDATION with
GateError at02:21:59. Schema approval was not created; the child never started.
Reaching that phase implies the before-baseline matched the retained receipt;
no after-observation ran and no before/after equality PASS exists for this window.

**Current HOLD finding in the attempted v4:** public `validate_bundle.py:39` and
`production-bootstrap.py:317-335` require/preserve
`SPRING_CONFIG_LOCATION="classpath:/"`, but
the pinned v4 `release-readiness.py:256-262` rejected every `SPRING_CONFIG_` key. MA/PG's
02:22 KST follow-up withdraws WI010's execution-readiness PASS to HOLD.
This is a source-proven incompatibility and likely trigger; the DB-off remote
diagnostic at02:23:04 reports only ENV_KEYS, not the private offending line.
WI010's original scoped PASS and local tests remain historical evidence,
unchanged. No private configuration body, value or hash was imported.

Operator cleanup verified stop0/mask0, owned process group clear and restricted
TLS unchanged. Independent MA02:22:46 postcheck confirms MySQL inactive/masked,
no DB ports/jobs/Java/FFmpeg, app absent, Nginx/timer active/enabled and unchanged
TLS pins. Root tty1=0 at02:23:26; exact owned key removed, empty agent confirmed,
then elevated restore exit0 and fresh Windows Stopped/Disabled at approximately
02:24. Temporary console/instance tabs closed; firewall handoff retained.
The earlier02:18:41 process-name HOLD was resolved by PG-accepted kernel-thread
metadata classification and fresh02:21:50 PASS, without disabling controls;
this is not protection against privileged capture.

**New direct user approval:** "검증 도구 수정·회귀시험·재검토 진행" authorizes only
the exact existing `classpath:/` allowlist correction, full public-bootstrap
synthetic fixture regression and independent review. External/import/other
configuration rejection must remain. WI011(SE) -> WI012(PG) -> WI008 final
integration is pending; there is no forecast PASS, server reconnect/restart
authority or native retry. Never edit private env to bypass the guard. MA owns
the new handoffs; this checkpoint does not close WI008 or REQ001. Concurrent
WI011 source changes were observed during integration; they do not replace
the attempted v4 identity or establish a reviewed new candidate.

Existing JDBC and restricted TLS evidence remain bounded successes. Hibernate
native NOT_RUN, canonical fresh manifest UNRECORDED, `app_runnable=false`;
even future Hibernate PASS would not prove canonical DDL or full startup.
NS remains unresolved; expiry alerts, first unattended renewal and actual reboot
remain unverified. Recovery/backup, mail, ADMIN, worker/egress fencing and runtime
activation need separate gates. No schema/data/grant/password change, new
purchase/install or public application launch occurred in this attempt.

Follow [REQ001](../deliverables/user/REQ-20261011-ATS-001.md),
[WI008 checkpoint](../deliverables/agent/WI-20261011-ATS-008-evidence-pack.md),
[WI008 summary](../deliverables/user/WI-20261011-ATS-008-summary.md) and the
preserved [WI010 review](../deliverables/agent/WI-20261011-ATS-010-evidence-pack.md).
The older checkpoints below retain their original observation boundaries.

### 2026-10-11 Restricted TLS and Renewal Configuration

**WI005 final receipt integration; restricted TLS/renewal configuration PASS;
REQ010 partial, NS unresolved and alerts not configured.** MA verified staging
registration/validation, production single-apex issuance, native trusted TLS,
negative-path/SNI/spoofed-header checks and actual renewal dry-run/hook success.
Post-renewal trusted served-certificate hash matched production, not staging.
The user's LTE screenshot showed connection failure/no marker; fresh allowed-line
control at01:15:48 KST returned trusted200/exact marker. This is bounded independent
failure, not proof of the precise drop layer or independently confirmed VPN absence.
The proposed web closure was NEVER applied; its editor was cancelled unsaved.

At01:16:14 MA enabled the known single timer and Nginx; timer active/waiting,
next2026-10-11 20:59:44 KST, LastTrigger empty. Renewal status is
`SCHEDULE_ENABLED_DRY_RUN_PASS`, not first-unattended-run proof. Package cron
is inactive on systemd; root has no additional Certbot cron. Service static/
inactive Result=success/0 is not a scheduled-run receipt. Actual reboot,
future changed certificate, expiry alerts and unattended renewal remain unproven.

Final MA01:16:47: fresh pinned SSH/sudo and Nginx/sshd syntax PASS; Nginx and
timer enabled/active, MySQL persistently masked/inactive, app absent, no Java/
FFmpeg/CA processes, pending jobs, app/DB ports or IPv6 web listener. Root tty1=0
and recovery UI login prompt confirmed. At01:16:52.899 the exact window key was
removed, empty agent checked before fresh Windows Stopped/Disabled verification;
all MA execution sessions ended. Public ACME80 and management-only443/SSH remain,
egress preserved; no app/DB start/publication or extra purchase/install/reboot.
NS last checked01:14:09 still authoritative AA NODATA, same serial; TLS success
does not repair it. Browser preview was client-policy blocked, not a TLS trust
failure; native proof exists but no browser-proof claim. No overall production readiness.

Follow [REQ010](../deliverables/user/REQ-20261010-ATS-010.md),
[WI004 review](../deliverables/agent/WI-20261011-ATS-004-evidence-pack.md) and
[WI005 final ledger](../deliverables/agent/WI-20261011-ATS-005-evidence-pack.md).
The following approval/interim and older closure checkpoints retain their own
observation cutoffs; superseded plans are not executed-action receipts.

### 2026-10-11 Restricted HTTPS Resume Approved

**REQ010 restricted TLS in progress; production certificate and allowed-client
TLS observed; NS anomaly unresolved; final acceptance/cleanup pending.** The user explicitly approved recording the unresolved
NS problem and proceeding with restricted HTTPS testing, keeping app/DB private
and requiring notice before extra costs or user-performed actions. This supersedes
the earlier issuance HOLD as an authorization decision, not as runtime acceptance.
Prior LE v1.8 no-email staging/production/renewal consent is retained and must
match the action-time agreement/scope before CA activity.

Before this resume, MA reports expected parent delegation and correct A at both
authorities, but apex NS AA NOERROR/NODATA with SOA serial2026101101, corroborated
by Google/Cloudflare DoH. Cause remains unknown; no confirmed input fix. Earlier
target REFUSED resolved; nslookup PTR REFUSED is not a target NS failure. Empty
apex host input is correct; provider NS does not require own-nameserver glue
registration. Certificate success would not resolve this NS anomaly.

MA preflight at01:00:47 KST confirms pinned SSH/sudo PASS, same boot, root recovery
tty1=1 and one expected key in the temporary Running/Manual agent. App unit absent;
MySQL/Nginx masked/inactive, Certbot runtime-masked/inactive, no jobs/Java/FFmpeg
or web/app/DB listeners. Seven retained source hashes, bootstrap comparison/native
parse and22-path metadata/ACL checks passed. At01:00:58 native DNS still shows
the same NS anomaly; current ACME directory terms matchv1.8. At01:04:56 MA
rechecked bootstrap/identity, unmasked only Nginx and verified effective unit:
IPv4 port80 active, not enabled, no other web/app/DB listeners. Action-time
approved80/public-ACME and443/current-management-only rules were saved/read back,
preserving SSH/egress; fresh SSH PASS. Ten external Windows HTTP probes using
real DNS passed, including exact synthetic body and negative-path/method checks.
WI004 passed the exact no-email staging sequence. MA01:08:21 staging account
registration exit0/private metadata PASS and01:08:45 staging dry-run exit0 show
actual CA validation. Production registration01:09:08/issuance01:09:26 exit0,
single-apex protected lineage and native trust/hostname/key-pair checks passed.
At01:09:49 exact retained TLS render/reload served IPv4-only80/443; nine external
TLS probes passed (19 external HTTP/TLS total), including trusted real-DNS marker
and negative paths/method/Host. At01:10:43 loopback spoofed headers returned403
and absent/unknown SNI were rejected; exact reviewed hook/manual reload passed.
MA01:13:12 renewal dry-run exit0/all simulated renewals succeeded, explicit hook
success without HOLD, followed by trusted SslStream served-production-certificate
hash equality and marker200 (20 probes in that reported batch). The user's
requested LTE photo then showed the exact HTTPS marker URL, visible LTE and
ERR_CONNECTION_ABORTED/no marker, with a fresh trusted allowed-line200 control.
This is bounded user-operated independent failure, not absolute firewall
causality or VPN-absence proof. The proposed closure was superseded BEFORE any
rule removal/stop: only an editor had been opened and was cancelled unsaved.
Restricted HTTPS continues under existing approval; actual timer activation and
final SSH/root cleanup remain pending, renewal NOT_OPERATIONAL until receipt.
At01:14:09 global IPv6 count0 and both apex NS still AA NODATA. Alerts absent,
no extra product purchase fee; REQ acceptance in progress, not complete.
No app/DB operation, extra purchase or public
application release is authorized. Ordinary network use remains under the
existing contract, not a zero-cost guarantee. Prior closure below remains historical.

Follow [REQ010](../deliverables/user/REQ-20261010-ATS-010.md),
[WI004 review](../deliverables/agent/WI-20261011-ATS-004-evidence-pack.md)
and [WI005 draft ledger](../deliverables/agent/WI-20261011-ATS-005-evidence-pack.md).

### 2026-10-11 Domain and Restricted TLS Window

**REQ010 partial / HOLD_DNS_ISSUANCE; temporary window safely closed; not TLS-ready.**
WI003 integrates the partial result, not REQ completion or public readiness.
MA confirmed `atmusic.co.kr` registered/in use from2026-10-10 to2027-10-10;
KRW18700 paid is a user report, not a receipt audit. A native test-isolation
failure was corrected under explicit approval: SE/PG Windows each36/36 and
MA Ubuntu3.12 36/36 PASS. Earlier35-test evidence and failed staging are retained.
Seven exact packages including Certbot2.9.0, quarantine config/drop-in and
protected stages were installed/retained. Native parsing/effective unit and
bounded external synthetic HTTP/denial tests passed; no HTTPS proof exists.

At00:35:40 KST both authorities give correct A but authoritative apex NS
NOERROR/NODATA with SOA serial2026101101. Earlier primary REFUSED resolved.
Direct parent referral names both expected authorities; earlier recursive NS
is not parent proof. Child apex NS inconsistency persists; internal provider
cause is unknown and generation/sync is only an inference. No provider inquiry
or guessed DNSSEC/NS change occurred. User approved LE v1.8 no-email
staging/production/renewal terms but requires DNS resolution before continuation;
no exception approval, CA account, issuance or renewal dry-run.

MA closure at00:36:25 and final checks at00:38:00: only this window's inbound80/443
rules removed, SSH-only inbound verified in a separate authenticated console tab,
existing egress preserved. Nginx disabled then persistently masked/inactive,
MySQL persistently masked/inactive; Certbot service/timer inactive with runtime-only
masks and no timer enablement link. No80/443/8080/3306/33060/33062 listeners,
fresh pinned SSH/sudo PASS, recovery root tty1 count0. Exact window key removed,
empty agent verified before fresh Windows Stopped/Disabled; no execution running.
DNS/A, packages, configs, public tokens and stages remain; no deletion/reboot or
app/DB start. Renewal is NOT_OPERATIONAL and no expiry alerts exist. PG approved
bounded closure; production and remaining DNS/TLS/app/SMTP/backup/load gates HOLD.

Follow [REQ010](../deliverables/user/REQ-20261010-ATS-010.md),
[WI001 preparation](../deliverables/agent/WI-20261011-ATS-001-evidence-pack.md),
[WI002 review](../deliverables/agent/WI-20261011-ATS-002-evidence-pack.md),
[WI003 closure receipts](../deliverables/agent/WI-20261011-ATS-003-evidence-pack.md)
and the [domain/TLS runbook](../deploy/single-vps/runbooks/domain-tls.md).
Earlier dated checkpoints below retain their original observation boundaries.

### 2026-10-10 JDBC Verification and Configuration Readiness

**REQ009 native JDBC PASS; host and Windows cleanup complete; production HOLD.**
MA verified the existing app JAR's JDBC driver connection and read-only aggregates
at 22:47:49 KST: exact account/server/database, server read-only session, 43 tables,
6 active plan keys and zero non-plan rows. Before/after observation, four DML
grants and private env metadata remained unchanged. Final host receipt at
22:48:10 and exact-key/empty-agent/Stopped-Disabled receipt at 22:48:31 completed
this window; all MA sessions ended. WI032 document checks passed; REQ009 is
complete only within its approved JDBC/configuration-audit scope.

WI029 and independent WI031 each passed 29 synthetic tests; final PG PASS covers
the exact bundle and procedure. These are separate from MA's native result.
The whole JAR is root-private verification staging only; SPA remains local.
No Hikari/Hibernate/Spring startup: Hibernate `NOT_RUN`, canonical manifest
`UNRECORDED`, `app_runnable=false`. Configuration readiness is an audit only;
domain/mail placeholders remain unresolved, not applied or cleared. No new fee,
installation, purchase, DNS/public opening, SMTP or Provider action occurred.
`atmusic.co.kr` ownership/purchase is the next user decision before DNS/HTTPS;
availability and price are not asserted. Mail account/sender/private entry also
need user decisions. Baseline 2vCPU/8GB/100GB is not measured capacity; KRW70000
per month remains a budget, not a hard cap.

Follow [REQ009](../deliverables/user/REQ-20261010-ATS-009.md),
[WI029 preparation](../deliverables/agent/WI-20261010-ATS-029-evidence-pack.md),
[WI030 audit](../deliverables/agent/WI-20261010-ATS-030-evidence-pack.md),
[WI031 final PG review](../deliverables/agent/WI-20261010-ATS-031-evidence-pack.md),
[WI032 receipt ledger](../deliverables/agent/WI-20261010-ATS-032-evidence-pack.md),
[JDBC runbook](../deploy/single-vps/runbooks/jdbc-readiness.md) and
[configuration readiness](../deploy/single-vps/runbooks/production-readiness.md).
The earlier dated sections below retain their original verification boundaries.

### 2026-10-10 Production Preparation Window

**REQ008 / WI028 complete in the bounded preparation scope; native result
`BOOTSTRAP_APPLIED_NOT_RUNNABLE`; production HOLD.** MA applied the reviewed v3
candidate once at22:17:00KST. Fresh `atstudio_prod` and the loopback-only
`atstudio_app` account exist:43 tables,6 exact plans,0 non-plan rows, exactly
four nongrantable DML privileges. DB/JWT/billing secrets were generated only on
the server; private file metadata is root0600/single-link under root0700.
Read-only post-apply observation/grant checks and receipt comparison passed.
No env body or secret was included in these reports. JAR/SPA remain local
artifacts; no application deployment/start is claimed.

Final MA host receipt22:17:35KST confirms same approved boot, exact MySQL/Nginx
masks/inactive, absent app unit, no relevant jobs or80/443/8080/3306/33060/33062
listeners, and sshd syntax PASS. At22:17:53KST the exact approved key was removed,
agent empty was confirmed before stop, hidden cleanup exited0, and fresh Windows
service observation was Stopped/Disabled. All MA command sessions completed.
No extra charge, installation, domain purchase, public opening, app/mail/ADMIN
operation, commit or push. Cleanup belongs to this window, not WI024.

**Historical preparation sequence:** the earlier HOLD/pending states below
are superseded by the v3 success and final cleanup above, not erased.
The second MA native preflight receipt found that the corrected stage/native plan and NULL truth table
`1,1,0,0,0,1` passed, but a second native preflight refused ACTIVE AUDIT plugins
`sha2_cache_cleaner` and `mysqlx_cache_cleaner`. Target DB/account and approval/
env/claim/receipt were then absent; no DB or secrets had been created. Components absent,
replication0, loopback3306 only and no X/admin listener were reported. MA again
stopped/remasked MySQL. PG's read-only classification advisory is complete;
the user then approved exact identification of these two built-ins only. WI025
v3 completed that correction and37/37 local tests using byte-exact names and
SQL NULL library; bundle `b91766934c522b1c4549b8a3057f95eae94c9a542e107fb9c7c39d7df3b24b9c`.
WI027 subsequently completed independent37/37 retesting and final v3 review.
MA's fresh v3 stage/hash/plan, native37 plugin fixtures with0 mismatches/unknowns,
six NULL predicates and full preflight all passed before the single apply.
Both old stages/helper hashes remain preserved; no destructive or SQL retry.

WI025 local preparation and WI027 bounded-sequence review passed for the
initial candidate. WI026's approved isolated frontend completion supersedes its
earlier VITE-name HOLD: current JAR and 143-file SPA are locally prepared, not
deployed. DOCOPS rechecked their recorded identities at 21:54:38 KST.

MA reports trusted root archive extraction, all 16 public input hashes and the
native plan PASS, then temporary loopback-only MySQL start at 21:54 KST.
Before an approval JSON or any DB/account/secret creation, the native guard
returned `MYSQL_CONFIGURATION`: the disabled `admin_address` was NULL, so the
empty-string comparison was not true. Read-only diagnosis found the other
configuration predicates true, named DB/account absent and approval/env/claim/
receipt absent. MA confirms MySQL stopped and remasked, exit 0; old staging is
preserved. This is an actual pre-mutation HOLD, not DB readiness or a partial
initialization. No target application was started.

The user subsequently approved the narrow NULL-guard correction and independent
re-review. Reopened WI025 completed the one-line correction and34 local tests
(32 existing plus2 new); the previous39 validator tests were not rerun.
V2 bundle was `15aa91639be237e03c71b7f2df2b4923879b849981b9abdf71efda19aedea91d`.
WI027's corrected-candidate review PASS includes independent34/34 retest and
the exact fresh `/root/wi025-v2` sequence. The second native HOLD then required
the separately approved precise plugin correction and v3 review/native gates.
All older execution identities remain history, not approval for replay.

Follow [REQ008](../deliverables/user/REQ-20261010-ATS-008.md),
[WI025 preparation](../deliverables/agent/WI-20261010-ATS-025-evidence-pack.md),
[WI026 completion addendum](../deliverables/agent/WI-20261010-ATS-026-evidence-pack.md#completion-addendum-approved-isolated-frontend-build),
[WI027 review](../deliverables/agent/WI-20261010-ATS-027-evidence-pack.md) and
[WI028 receipt ledger](../deliverables/agent/WI-20261010-ATS-028-evidence-pack.md).

Domain `atmusic.co.kr` purchase remains pending before DNS/HTTPS. Runtime TCP/JDBC
authentication, canonical fresh-manifest reconciliation, Hibernate validation,
complete app configuration/activation, first ADMIN, SMTP/Provider tests, off-host
encrypted secret recovery, backup/restore, scheduler ownership, alerts and load
remain separately gated. No new purchase, public release or production approval.
The 2vCPU/8GB/100GB baseline and resource candidates are not measured capacity.
WI024's optional pam_lastlog and AppArmor advisories remain historical and
unpatched; this window makes no new full-security claim. Earlier dated entries
below retain their original scope and pending states as history.

### 2026-10-10 OS Maintenance Window

**WI024 complete at2026-10-10 21:23:32KST, from MA runtime and cleanup receipts.**
180 packages upgraded/25 added/0 removed, one reboot to kernel6.8.0-146 and
bounded postboot checks passed. This window's SSH sessions and root console are
closed; exact owned-key removal, empty agent and Stopped/Disabled are verified.
REQ007 remains active; production HOLD. Domain purchase before DNS/HTTPS,
app DB/secrets/start, backup/restore, alerts and load remain unfinished.
No functional regression was detected in the checked scope; the pre-existing
optional pam_lastlog warning and AppArmor vendor boolean-comparison warning
remain unpatched. This is not full security, capacity or production acceptance.

Follow [REQ007](../deliverables/user/REQ-20261010-ATS-007.md),
[WI022 local preparation](../deliverables/agent/WI-20261010-ATS-022-evidence-pack.md),
[WI023 independent local review](../deliverables/agent/WI-20261010-ATS-023-evidence-pack.md),
the [maintenance runbook](../deploy/single-vps/runbooks/os-maintenance.md) and
[WI024 receipt integration](../deliverables/agent/WI-20261010-ATS-024-evidence-pack.md).
WI022 preparation and WI023 local PASS do not establish live maintenance success.
The dated chronology below is historical; the completion paragraph above
supersedes its earlier pending states. Detailed receipts remain in WI024.

MA's new-window 20:59-21:02 KST receipt confirms fresh pinned administrator
access/PTY/sudo and user-authenticated root recovery console, unchanged policies
and layout, empty dpkg audit and idle package locks. MySQL/Nginx remain exactly
masked, inactive/dead; listeners are SSH22 and loopback DNS53 only.
MA subsequently reports protected copies of 16 public configurations and timer
baseline preservation PASS, with only the two APT timers paused. Official index
refresh and dependency check exited0. Simulation: 180 upgrades, 25 new packages,
zero removals and two phased residuals; all 205 selected origin/version/architecture
checks passed. Preview refusal exited1 intentionally after the prompt/abort;
archive bytes975471520 and net installed bytes321550336 are plan quantities.
An absent optional initramfs directory caused a later probe exit1, not preview
failure. The identical download-only plan completed rc0 (displayed975MB/227s,
4305kB/s); that receipt alone did not prove installation. Reviewed critical hooks and a provisional
boot budget precede a fresh plan/access/space recheck. The assumed330MiB work
plus256MiB untouched reserve fits the observed free boot space, but is not a
measured future peak. Command-local4000KB/s throttling is not provider quota or billing proof.

MA actual upgrade chunk4ed5cd exited0:180 upgraded/25 new/0 removed/2 phased.
Fresh pinned administrator/sudo at21:15:49KST, empty audit/dependency check,
16 unchanged configuration hashes and sshd/visudo passed; masks remained and no
failed units appeared. One transient new-SSH refusal recovered on the next probe;
the retained PTY stayed connected. Actual Plymouth hooks rebuilt all three old
initrds plus new146, superseding the earlier latest-only hook expectation.
Final boot free490549248 bytes is an end snapshot, not continuous peak evidence.
Subsequent MA receipts match all205 installed versions, retain16 configuration
hashes and pass three SSH connection contexts/layout14/native MySQL validation
without starting MySQL or SQL. Journal nine values and synchronized KST remain.
AppArmor's warning was localized to a vendor numeric comparison of feature
literal `yes`; restriction1, successful profile load and enforcementY were
observed. The missing nautilus conffile matches explicit package removal logic;
nautilus is not installed. No vendor patch was applied. MA reports STOP D passed:
idle jobs/locks, no scheduled shutdown,146 boot assets/entry and retained124,
fresh admin PTY/sudo and current root recovery. The SSH ucf-dist was reviewed
without merge; effective local policy remains. Exactly one reboot was issued;
the following later receipts establish its actual result.

MA rebooted once at21:20:34KST; expected connection reset was followed by fresh
strict-pinned SSH, changed boot ID, kernel6.8.0-146 and sudo0. Postboot21:21:14
checks passed: Ubuntu24.04/noble now point release24.04.5, empty audit/check0,
no failed units/jobs, exact MySQL/Nginx masks inactive, unchanged16 policy and
two source hashes,205 exact package versions, SSH controls/layout/native MySQL
validation without start/query. SSH/socket, journal/logrotate and restored APT
timers are healthy. NTP initially unsynchronized became synchronized; KST retained.
Root-key denial, publickey-only method offer, fresh admin PTY/sudo and user root
console authentication passed. Residual simulation: zero upgrades/new/removals,
two phased packages not forced. Existing optional PAM module warning and recurring
AppArmor numeric-comparison warning remain documented, not patched; PG reported
no blocking issue, not full-profile enforcement or complete security acceptance.

MA final cleanup0b7968 at21:23:32KST confirms root tty1 absent after UI logout,
private screenshot login prompt, APT timers active/enabled, NTPyes and no failed
units. All owned SSH sessions ended. Windows68f020 confirms exact-key removal0,
empty-agent list rc1 before stop, hidden elevated helper0 and Stopped/Disabled.
This is new-window proof, not reused WI021 cleanup. Initial/final space snapshots
are not continuous peak or capacity proof. Cached182 below is historical, not
the selected plan or an all-security classification. WI023's append-only PG
warning review has a new hash; its historical identity is retained in WI024.

### 2026-10-10 Host-Hardening Continuation

Current follow-up under [REQ007](../deliverables/user/REQ-20261010-ATS-007.md):
[WI019 correction](../deliverables/agent/WI-20261010-ATS-019-evidence-pack.md#2026-10-10-correction-and-recovery-addendum),
[WI020 local review](../deliverables/agent/WI-20261010-ATS-020-evidence-pack.md)
and [WI021 execution integration](../deliverables/agent/WI-20261010-ATS-021-evidence-pack.md).
Use the [corrected staged runbook](../deploy/single-vps/runbooks/host-hardening.md).
SE corrected the journal destination to `zz-atstudio.conf`; PG independently
retested the corrected source locally. This is not native application proof.

WI021 integration is complete from MA's final20:39KST runtime/cleanup receipt;
the earlier pending states below and in prior records are historical. The MA phase-one
receipt reports matched candidate transfer hashes, fresh administrator key/PTY/sudo
acceptance before and after SSH reload, root-key denial, publickey-only server
method offer, preserved root local password and native service-layout checks.
These are MA-attributed2026-10-10 host observations, not SE/PG remote verification.
MA's corrected phase-two receipt records journal install/merged-policy/restart,
bounded warning check and exact synthetic receipt PASS; reported usage16.0M.
The earlier premature journal PASS/8MiB report is superseded in WI021, not reused.
MA's later MySQL receipt reports corrected staging, three native validators and
bounded startup PASS, loopback3306 only with no X listener, then verified stop,
remask and no queued job/listener. The first live-value query did not execute
because the native client rejected `--no-login-paths`. After the SE/PG-reviewed
compatibility correction, MA's second bounded socket query passed all expected
values, followed by verified stop/dead/remask and no listeners or queued job.
Final host check at20:39:14KST passed sshd/visudo/layout and all five applied
configuration hash comparisons. MySQL/Nginx were exact /dev/null masked and
inactive/dead; only SSH22 and loopbackDNS53 listened, with no failed units.
Both owned SSH sessions exited0. Exact owned-key removal and empty-agent list
preceded confirmed Stopped/Disabled at20:39:34KST. MA verified noVNC root logout
with a fresh Ubuntu tty1 login-prompt screenshot. These are this window's own
receipts, not reuse of WI018 cleanup. Final SE runbook43755E6F...B80F1E1A and
testBD24759B...38F42915 received PG source-hash reconciliation PASS; full hashes
and the original journal/AppArmor/client failure history remain in WI021.
No full security or capacity claim follows. Domain purchase before DNS/HTTPS,
app DB/secrets/start, backup/restore, alerts and load remain tracked.
**Before public invitation: separately approve OS security updates, reboot and
post-update revalidation.** MA's read-only cached `apt list --upgradable` reported
182 upgradable packages from mixed noble-updates/noble-security sources, including
OpenSSH, OpenSSL, kernel, sudo, systemd and libc. Not all182 are classified as
security updates, and no new CVE finding is established. No OS upgrade/reboot
ran in this continuation; no reboot marker was present at that observation.
**REQ007 active; production HOLD.** Earlier checkpoints retain their dated bounds.

### 2026-10-10 First-Host Checkpoint

Follow [REQ007](../deliverables/user/REQ-20261010-ATS-007.md) for the approved
first-host work, [WI016 preparation](../deliverables/agent/WI-20261010-ATS-016-evidence-pack.md),
[WI017 independent review](../deliverables/agent/WI-20261010-ATS-017-evidence-pack.md)
and the [first-host runbook](../deploy/single-vps/runbooks/first-host-setup.md).
[WI018 integration](../deliverables/agent/WI-20261010-ATS-018-evidence-pack.md)
records MA's actual first-host results, separately from local preparation.

Verification: **2026-10-10, named Ubuntu24.04.4 host and local Windows operator
environment; execution/evidence owner MA**. One existing purchased server was
rebuilt in place under the exact displayed0KRW approval, not replaced by another
purchase. Recovery-console authentication, independent post-rebuild Ed25519
comparison and strict-pinned native SSH key login/reconnection passed. The user
rotated the initial root password privately; MA observed success without capture.

Java17.0.20.1, Nginx1.24.0, FFmpeg6.1.1 and MySQL8.0.46 were installed under the
subsequent explicit package/MySQL distribution-initialization approval. Apt exited0,
dpkg audit and failed-unit listing were empty; **Nginx/MySQL remain masked and
inactive/dead**. Distribution system data/accounts were initialized; no ATStudio
SQL/schema/users or application secrets were created and no application deployed.
One-second synthetic libmp3lame128k encoding passed; real-WAV/upload/load acceptance
was not run. Target listeners were SSH22 on IPv4/IPv6 and loopbackDNS53 only.
One Windows line reached22 but not80/443/3306/33060/8080 within1500ms; no independent
network proof. Resource values and warnings are in WI018, not capacity guarantees.

Owned temporary-agent key removal and an empty identity list were verified.
MA's final `Get-Service ssh-agent` check at **19:57:21 KST** confirmed
**Stopped/Disabled**. The noVNC root console was logged out; MA's fresh screenshot
showed the Ubuntu tty1 login prompt. These are MA-attributed cleanup observations.
SSH global root/password/forwarding defaults remain permissive; source-restricted
provider SSH ingress remains in place, not a substitute for hardening. Separate
admin/sudo and second-session proof, SSH hardening, service directories, log
retention, MySQL tuning, app DB/secrets/activation, domain purchase before DNS/HTTPS,
backup/restore, alerts and load remain pending. No additional goods were purchased.
53,020 KRW/month including VAT is the dated server amount, not a new quote;
70,000 KRW/month is a budget, not an enforceable billing cap.

SE WI016/PG WI017 are local Windows procedure/synthetic evidence only; PG's local
PASS is not independent live-host verification. DOCOPS integrates MA attribution
and checks documents locally. Older pre-purchase/rebuilding and Stage B NOT RUN
statements retain their dated bounds; first-host setup has now run, but application
deployment and production acceptance have not. **REQ007 active; production HOLD.**

### 2026-10-10 Local Implementation and Review

[REQ006](../deliverables/user/REQ-20261010-ATS-006.md) implements the selected
settings, pre-body storage-mutation admission and guarded backup core locally.
Start with [WI015 integration](../deliverables/user/WI-20261010-ATS-015-summary.md),
the [current configuration](../deploy/single-vps/CONFIGURATION.md) and
[backup integration boundary](../deploy/single-vps/backup/README.md).

Verification: 2026-10-10, Windows/local isolated tests and synthetic fixtures.
[SE WI011](../deliverables/agent/WI-20261010-ATS-011-evidence-pack.md) records
Java105 (57 new); [WI012](../deliverables/agent/WI-20261010-ATS-012-evidence-pack.md)
settings39; [WI013](../deliverables/agent/WI-20261010-ATS-013-evidence-pack.md)
backup35. MA independently reran all selected Java105 (12 suites, zero
failures/errors/skips; 43s, five tasks executed), settings39 (15.496s) and
backup35 (0.739s). [Final PG WI014](../deliverables/agent/WI-20261010-ATS-014-evidence-pack.md)
reproduced Java57/settings39/backup35 plus seven injected-failure probes and
found no new blocking defect within local preparation. Reviewed source hashes
are in those EPs; earlier pending-review statements there are historical.

DOCOPS observed local `main` HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`
with dirty/untracked implementation; HEAD alone does not identify the candidate.
**The old JAR was not updated. Do not apply the new environment/Hikari10 alone:**
build and hash the reviewed new source and prove the admission guard is active
before applying it on an approved target. Pool10 is not deadlock-freedom proof.

Backup core and default-OFF recurring authorization exist; **every host hook
(stop/fence/dump/resume included) is unconfigured**. The timer is inactive;
actual daily-service/operations-lock integration, independent alert transport,
external-disk copy and actual restore are not implemented. Default backup CLI
reports `side_effects:false`, `NOT_CONFIGURED`. Cold verification retrieves only
dump+manifest, not all media, and is not restore proof. Native Linux, restic/S3,
real services/DB, load and restore remain **NOT RUN; Stage B / production HOLD**.
Local closure creates no new purchase precondition or cost/user-count guarantee.
Historical Gmail/WAV/phone TEST success below retains its original bounds.

### 2026-10-10 Settings and Restic Design

Historical design checkpoint under REQ005; superseded for current implementation
by REQ006 above. Its unchanged-template statement describes that earlier checkpoint.

Start with the [latest pre-purchase audit](../deliverables/user/WI-20261010-ATS-007-summary.md),
then the [selected resource baseline](../deploy/single-vps/runbooks/resource-baseline.md)
and [restic backup plan](../deploy/single-vps/runbooks/restic-backup-plan.md).
[REQ005](../deliverables/user/REQ-20261010-ATS-005.md) authorizes design and one
requested memory note only: **no purchase, installation or backup execution**.
See the [integration result](../deliverables/user/WI-20261010-ATS-010-summary.md)
for selected-versus-template values and remaining gates.

Verification: 2026-10-10, Windows/local source and documents; SA WI008 and PG
WI009 authored the plans, MA accepted their boundaries, DOCOPS WI010 integrates
and validates navigation. Source HEAD remains `58b1011`; this is not deployed
artifact identity. Executable templates remain unchanged. Target performance,
S3/restic compatibility and recovery are **NOT RUN**; **Stage B / production HOLD**.

This dated entry supersedes older preparation decisions below, not historical
Gmail/WAV/128kbps/phone TEST evidence. The user waived provider inquiry; the old
inquiry draft is not a mandatory contact step. Allowance/proration, inbound
50Mbps, attack billing/blocking and enforceable cost-cap terms remain uncertain.
The dated 53,020 KRW server + 1,100 KRW average 50GB storage example is not an
all-in ceiling; external retrieval and other charges are separate. Restic does
not guarantee fixed storage, zero retrieval cost or recovery success.

### Preserved 2026-09-29 Evidence Refresh

Read this entry before judging implementation, verification or hosting readiness.
The **2026-09-29** refresh under [REQ-20260929-ATS-001](../deliverables/user/REQ-20260929-ATS-001.md)
compares documents with source; it is **not a new runtime PASS**. Latest completion
evidence already existed before this refresh. Failure to consult it was a
judgment error, not proof that the evidence was unavailable.

| Area | Last verification / environment | Supported result and evidence | Remaining boundary |
|---|---|---|---|
| Source identity | 2026-09-29 local checkout inspection | `main` at `58b1011` before this documentation WI; [dated source checkpoints](#current-v1-baseline) | No remote fetch, deployed-artifact or current process verification; recheck the actual checkout for each task. |
| Gmail | 2026-09-08 development / TEST | [WI011](../deliverables/agent/WI-20260908-ATS-011-evidence-pack.md#current-recipient-evidence-wi012-follow-up): 3 Korean messages received/readable in Inbox. [WI012](../deliverables/agent/WI-20260908-ATS-012-evidence-pack.md#ma-execution-record-2026-09-08-kst): one website-request backend SUCCESS, user receipt and reset-page opening. | WI012 did not verify Inbox placement or submit a password change. New-host SMTP, origins and delivery need separately approved target checks. |
| WAV / MP3 | 2026-09-09 retained TEST runtime | [WI028](../deliverables/agent/WI-20260909-ATS-028-evidence-pack.md): 22 original/128kbps MP3 pairs PASS; MA's 20-item UI batch, retry, seek and refresh/manual resume recorded. | External FFmpeg with `libmp3lame` is required for new WAV-derived playback; legacy/new MP3 direct playback is separate. See [provisioning](design/runtime-storage-operations.md#ffmpeg-provisioning-and-configuration). No target capacity benchmark. |
| Phone download | 2026-09-09 user-assisted actual phone | [Final phone follow-up](../deliverables/agent/WI-20260909-ATS-028-evidence-pack.md#user-acceptance-follow-up-2026-09-09): download-save/open-play PASS. | Phone-local bytes/SHA, whole-duration playback and desktop/all-mobile coverage unverified. Earlier in-app native-event UNKNOWN and separate API SHA PASS remain distinct. |
| Reconstruction | 2026-09-09 source/history clone and local staging | [REQ004 delivery receipt](../deliverables/user/REQ-20260909-ATS-004.md#ma-delivery-receipt): WI020 PASS and source/history delivered; [WI021/WI022 local handoff](../scripts/reconstruction/README.md#5-private-recovery-inventory-and-keyring) completed. | Morning pre-WAV staging is non-atomic, not a current post-WAV DB/media recovery point. Encryption, off-device upload and actual restore remain open; source/history delivery is not pending. |
| Production / schema | 2026-09-09 retained TEST checkpoint | [SR-93](SR/SR-93.md#remaining-production-gates): retained 43 tables / 520 columns; 10 historical missing references, strict startup false. | Production **HOLD**; fresh-bootstrap manifest **UNRECORDED**. Target deployment, data/backup strategy, acceptance and release approval remain separate. |

### 2026-10-03 Stage A Closeout

Purchase-preparation **Stage A is complete** under
[REQ-20261003-ATS-001](../deliverables/user/REQ-20261003-ATS-001.md); start with the
[single-VPS bundle](../deploy/single-vps/README.md). Overall REQ remains approved /
in progress; **Stage B NOT RUN, production HOLD**. Existing Gmail, WAV/128kbps
MP3, phone and reconstruction evidence above retains its original bounds.

Verification: **2026-10-03, Windows/local synthetic fixtures, QA WI005**.
[Independent QA](../deliverables/agent/WI-20261003-ATS-005-evidence-pack.md)
records 30/30 tests, CLI/text/JSON and syntax PASS, 13 documents/103 links/30
dependencies checked and 27/27 preservation hashes unchanged, with no blocking
findings. [PG review](../deliverables/agent/WI-20261003-ATS-004-evidence-pack.md)
independently resolved both P2 template findings. These are attributed results,
not new runtime checks by this documentation closeout. Actual Linux
Nginx/systemd/MySQL, target access/TLS, restore, alerts and load remain **NOT RUN**;
backup automation and supported user counts are not established.

Next inputs: exact provider plan/OS/host/domain/account and separate purchase
approval; confirmed allowance/reset/proration, inbound/50Mbps applicability,
attack billing/blocking and cost limits; approved SSH recovery/access, external
secret setup, exact fresh DB/first ADMIN scope, backup destinations/budget and
alert recipients. Named-target remote/DB/secret/external-send actions need their
own confirmation. General public access and LIVE payment/refund remain separate.
No price or provider term was refreshed here.

### Evidence Reading Order

1. Verify the actual branch, HEAD, working-tree changes and source/artifact identity;
   a dated commit or process receipt is not a current deployment check.
2. Follow the latest relevant REQ/WI closeout and dated addenda before using an
   earlier failure, pending item or default setting to claim a feature is absent
   or unverified. Use [dated checkpoints](#current-v1-baseline) to locate evidence.
3. Identify effective configuration provenance and test environment. The tracked
   [mail defaults](../src/main/resources/application.yml) (`localhost:1025`) do
   not prove effective runtime settings. [WI012](../deliverables/agent/WI-20260908-ATS-012-evidence-pack.md#ma-execution-record-2026-09-08-kst)
   records selected external mail keys mapped into backend child-process overrides;
   preserve that provenance without reading/publishing private configuration bodies.
4. Report verification date, environment, evidence owner and limits separately
   from document edit date. Historical `deliverables/`, SR and audit records retain
   their original dates; current entries link the superseding completion.
5. For future sizing/cost work, separate measured values (date/environment/unit),
   dated workload assumptions and unverified provider terms. Recheck target
   traffic/overage/backup/pricing terms only in that later approved work; this WI
   refreshes no forecast or price and makes no hosting or architecture decision.

## Document Overview by Category

| Category | Document Count | Index File | Description |
| ---------- | ------- | ------------------------------------------ | -------------------- |
| Architecture | 1 | [Architecture Index](architecture/index.md) | High-level design/principles |
| Design | 30 | [Design Index](design/index.md) | Meta + ATStudio domain design (API, DB, use cases, protocol references) |
| Policies | 8 | [Policies Index](policies/index.md) | Operational policy documents |
| Standards | 13 | [Standards Index](standards/index.md) | Standard documents and reference assets |
| Templates | 18 | [Templates Index](templates/index.md) | Document/artifact templates |
| Registry | 6 | [Registry Index](registry/index.md) | Asset/context/project registries and V1 artifact retention |
| Audit | 6 | [Audit Index](audit/index.md) | Audit reports and remediation baselines |
| Client | 8 | [Client Index](client/index.md) | Easy client acceptance testing guides and SR intake references |
| Payment | 7 | [Payment Index](payment/index.md) | Payment system guide, flows, operations, acceptance checklist, and client brief |
| SR | 100 | [SR Index](SR/index.md) | Screen Review items (SR-01~90, SR-92~101) |
| Retrospective | 4 | [Retrospective Index](retrospective/index.md) | Engineering lessons, domain design, kick.md, process |
| ADR | 1 | [ADR Index](adr/index.md) | Decision records |
| UI | 3 | [UI Index](ui/index.md) | Screen inventory, flow, and modal planning documents |
| Eval | 0 | [Eval Index](eval/index.md) | Evaluation documents |

**Total Document Count**: Recursive Markdown file counts in the categories above,
excluding `index.md` files and including retained historical/reference files.
Current total: **205**. This documentation inventory is separate from the
205-path artifact-cleanup baseline.

## Required Documents Mapping by Role

### MA (Main Agent)

- **Required (design)**: [System Design](architecture/system-design.md)
- **Required (rules)**: `AGENTS.md` (root, primary) — Orchestration gates, routing rules, Tier 0, workspace structure
- **Claude Entry Point**: `CLAUDE.md` (root, active) — Active operating instructions for Claude Code sessions; must stay aligned with `AGENTS.md`
- **Policies**: [Policy Documents](policies/)
- **Standards (including Tier 0)**: [Standards Documents](standards/)

### Subagents / Skills

- **Subagents (Source of Truth)**: `.claude/agents/` — Context provided via WI handoff packets
- **Skills (Codex-exposed)**: `.agents/skills/`
- **Skills (Claude-facing assets)**: `.claude/skills/`

## Project-specific Document Overview

### PRJ-ATS-001: ATStudio (Shorts Music Marketplace)

| Category | Document | Description |
|----------|----------|-------------|
| Standards | [core-principles.md](standards/core-principles.md) | Section 13: ATStudio Domain Principles |
| Standards | [development-standards.md](standards/development-standards.md) | Section 2A: Java/Spring Boot Coding Standards |
| Standards | [dto-standards.md](standards/dto-standards.md) | Entity/DTO separation, ResponseDTO, RequestDTO |
| Standards | [exception-handling.md](standards/exception-handling.md) | Business/Technic exceptions, GlobalExceptionHandler |
| Standards | [frontend-standards.md](standards/frontend-standards.md) | React + TypeScript architecture (Phase 2 — active) |
| Standards | [glossary.md](standards/glossary.md) | Section 3-A: ATStudio Domain Terms |
| Policies | [security-policy.md](policies/security-policy.md) | Section 6: JWT/MySQL Secrets Management |
| Registry | [project-registry.md](registry/project-registry.md) | PRJ-ATS-001 registration |
| Client | [testing-guide.md](client/testing-guide.md) | External testing guide and SR intake reference set |
| Payment | [payment/index.md](payment/index.md) | Payment documentation pack for current subscription payment system |
| Config | `.claude/config/workspace.json`, `.claude/config/context-injection-rules.json` | ATStudio routing, tech_stack, context injection |

- **Tech Stack**: Java 17, Spring Boot 4.x, MySQL 8.x + React 18, TypeScript 5.6, Vite 6 (Phase 2 — active)
- **Project Stats: Pre-WI014 recorded snapshot (not recounted)**: 150 method-level REST mappings, 43 DB tables / 43 JPA entities, recorded disposable MySQL manifest (43 tables, 511 columns, 175 indexes, 91 foreign keys, 6 plans/plan keys, zero forbidden tables/columns, SHA-256 `b177b34780fabc75ea8b4608a0d210167a81d414d2778cc1d1dc5c0e39c8fea4`), 53 distinct screen UIs, 13 agents
- **Screen Count Unit**: 53 distinct visual page UIs = 53 `createLazyPage(...)` declarations, including 2 error screens. The router separately contains 56 path routes plus 1 index redirect; reused pages are not additional screens.
- **Phase 2 Freshness**: React/Vite SPA uses the [current V1 baseline](#current-v1-baseline), `main`. Public access requires the operator-controlled acceptance lifecycle and a newly verified URL; historical demo URLs are never current runtime evidence.
- **Project Registry**: See [Project Registry](registry/project-registry.md)

### Current V1 Baseline

**2026-09-29 documentation/source checkpoint:** local `main` HEAD is `58b1011`
(2026-09-09 phone closeout), inspected before WI001 edits; only the current
REQ/handoff were untracked at entry. This is not a fresh remote or runtime check.
Use the [current decision table](#current-decision-entry) with the latest-first
dated checkpoints below; none of their original verification dates is advanced.

**2026-09-09 current WAV checkpoint (WI029):** [WI026](../deliverables/agent/WI-20260909-ATS-026-evidence-pack.md) closed source implementation and automated verification; separately approved [REQ006 / WI028](../deliverables/agent/WI-20260909-ATS-028-evidence-pack.md) then verified actual retained TEST application. The retained DB is **43 tables / 520 columns**, with nine added Track columns and one queue index. Its ALTER column ordinals differ from fresh source, so the [fresh-bootstrap manifest remains UNRECORDED](design/db-schema.md#current-source-and-mysql-verification-boundary); the earlier 511-column manifest is historical. No new DB is required or authorized by this documentation closeout.

WI028 records 22 actual original/128kbps MP3 pairs PASS, MA's 20-item UI batch, interrupted-processing retry, list/detail seek and refresh/manual resume. The [2026-09-09 user-acceptance follow-up](../deliverables/agent/WI-20260909-ATS-028-evidence-pack.md#user-acceptance-follow-up-2026-09-09) closes only human-assisted actual-phone download-save/open-play: **PASS**, from MA-reviewed native download-complete notification and user-confirmed local playback. Earlier in-app native-event UNKNOWN and separate API original SHA-256 PASS remain distinct; neither establishes phone-local bytes/SHA, whole-duration playback or desktop/all-mobile coverage. MA confirmed test33-only follow-up deactivation and logout. The ten historical missing references and strict-on-startup=false remain, so production SR-93 stays **HOLD**. See [runtime coverage and limits](design/runtime-storage-operations.md#actual-retained-runtime-verification-addendum-2026-09-09). WI029 performs documentation checks only, not a new runtime or suite run; commit/push closeout is MA-owned under [REQ007](../deliverables/user/REQ-20260909-ATS-007.md).

**2026-09-09 reconstruction delivery and local staging checkpoint:** [REQ004's final receipt](../deliverables/user/REQ-20260909-ATS-004.md#ma-delivery-receipt) records WI020 PASS and published/remote-clone-verified source/history at `65b8cce9d7c60370d761e3e6d3c34821a7c7e675`. WI021/WI022 completed the morning local-only private handoff, including the dump and copy/hash checks. This non-atomic pre-WAV set is not a current post-WAV recovery point; encryption, off-device upload and actual restored-runtime proof remain open. See the [reconstruction guide](../scripts/reconstruction/README.md#5-private-recovery-inventory-and-keyring).

**Earlier 2026-09-09 WI015 checkpoint (historical):** MA confirmed `main=origin/main` at documentation checkpoint `1c467d1` before that follow-up. Product `2d47504` was already pushed and applied to the named TEST runtime under [REQ-20260909-ATS-002](../deliverables/user/REQ-20260909-ATS-002.md). The [WI015 runtime record](payment/index.md#2026-09-09-source-and-runtime) separates that prior application and four HTTP 200 results from the [supplied post-restart UI check](payment/acceptance-test-checklist.md#2026-09-09-post-restart-ui-check). No new live checks or full suites were run for that documentation follow-up. Production SR-93 remained **HOLD**; its target gates were not closed by TEST application or limited user UI evidence.

**2026-09-08 Gmail closeout checkpoint:** [WI011/WI012 mail closeout](payment/index.md#wi011wi012-mail-and-documentation-closeout-2026-09-08) confirms the bounded results in the decision table. WI012's final receipt supersedes WI011's residual pending-link fragment, not its original send history. Base mail defaults are not evidence that these checks never occurred.

**Historical 2026-09-08 checkpoint:** The following source/runtime and review boundaries remain dated history, superseded by the update above and the later remediation record in SR-93.

- **Source checkout (2026-09-08)**: `main` is the official V1 source baseline in the root `ATStudio` checkout under the latest [REQ-20260908-ATS-003 approval](../deliverables/user/REQ-20260908-ATS-003.md). The retired client worktree is not a maintained baseline; its temporary thumbnail policy is preserved as history and is not imported into `main`.
- **Commit boundaries**: Product delivery is `7eae086c899cd4534be69da03bbc1cc55fe4349d`. The later mail-documentation closeout, `53284823b2824d04ee364f4c3f0ec9e8adeb4635`, was pushed before main unification. Neither that documentation commit nor WI014 changes the product baseline.
- **Historical references**: Earlier branch names, Vite 6.4.3 observations, commit IDs, counts and acceptance records remain dated evidence. The [WI013 decision register](../deliverables/agent/WI-20260908-ATS-013-branch-decision-register.md) records the pre-promotion assessment; the latest REQ approval supersedes its original branch recommendation.
- **Runtime and approval**: Git promotion does not restart or deploy the public runtime and is not deployed-production or security approval. Use the [dated source/runtime and mail evidence](payment/index.md#2026-09-08-source-and-runtime) for the WI009-WI012 boundaries. [SR-93 and target-production gates](SR/SR-93.md#remaining-production-gates) remain OPEN; the production target and bounded security review require separate work.

## Starting Point Guides

### Development Reconstruction

Start with the [root README](../README.md) and
[development reconstruction guide](../scripts/reconstruction/README.md) for
toolchain setup, read-only source preflight, selected local configuration and
private DB/media/keyring restore gates. A source check or same-PC clean build
is not a new-PC/full restore or production approval. The historical retention
register below locates recovered records; private originals remain separate.

### Current Operations And Historical Evidence

Use the current design, policy, client and payment guides for operations.
`deliverables/`, `docs/SR/`, `docs/audit/` and `docs/retrospective/` contain dated
work records, not fresh runtime or production approval. The
[V1 artifact retention register](registry/v1-artifact-retention-20260909.md)
maps the approved 205-path historical set and its latest reviewed retention
decisions, including recovered development documents. Use its per-entry
classification for Git availability. A remote checkout includes no private
archive or raw evidence.
Historical literal paths are register lookup keys, not setup dependencies.
The [development history recovery register](registry/development-history-recovery-20260909.md)
provides per-document recovery rationale and provenance for reviewed public history.

### New Users (First Time)

1. **`AGENTS.md`** (root): Current Codex work workflow, orchestration gates, routing — **start here** ⭐
2. **[System Design](architecture/system-design.md)**: Big picture/principles and migration-state architecture
3. **[Kickoff Prompt Template](templates/ma-session-kickoff-prompt.md)**: Copy-paste for new sessions
4. **`CLAUDE.md`** (root): Active entry point for Claude Code sessions and Claude-oriented handoffs

### External Testers / Client Review

1. **[Client Testing Guide](client/testing-guide.md)**: External tester entry point and preparation notes
2. **[Quick Checklist](client/1-quick-checklist.md)**: Short first-pass acceptance review
3. **[Full Feature Checklist](client/2-full-feature-checklist.md)**: Full client-facing acceptance checklist
4. **[Admin Checklist](client/3-admin-checklist.md)**: Admin-page acceptance review
5. **[Payment Client Brief](payment/client-brief.md)**: Draft payment-system explanation for client-facing review

### Payment System Review

1. **[Payment Documentation Pack](payment/index.md)**: Start here for payment feature scope and reading order
2. **[Payment Feature Inventory](payment/feature-inventory.md)**: Current implemented payment capabilities
3. **[Payment Acceptance Test Checklist](payment/acceptance-test-checklist.md)**: Payment-specific acceptance test checklist

### Document Authors

1. **[Documentation Standards](standards/documentation-standards.md)**: Documentation writing standards
2. **[Glossary](standards/glossary.md)**: Standard glossary
3. **[Glossary Sources (official)](standards/glossary-sources.md)**: Terminology standard sources

### Agent Developers

1. **[System Design](architecture/system-design.md)**: Overall design (SoT for agent roles)
2. **[Development Standards](standards/development-standards.md)**: Development standards
3. **[Evidence Pack Standard](standards/evidence-pack-standard.md)**: Evidence pack required fields/reproduction/pointer specification
4. **Agent definitions (SoT)**: `.claude/agents/*.md` — 13 agents (ps, eo, sa, se, re, pg, tr, uv, docops, qa, qa-fe, qa-integ, cr)
5. **Codex-exposed skills**: `.agents/skills/*` — Runtime-facing project skills used in current Codex sessions

## Document Scope Classification

Documents are classified by scope for context injection to domain projects.

### Meta-only (Exclude for Domain Projects)

These documents describe meta framework operation. **DO NOT inject for ATStudio domain work** unless explicitly needed:

| Category | Documents | Purpose |
|----------|-----------|---------|
| Architecture | `system-design.md` | MA+Subagent architecture |
| Design | `base-agent.md` (archived), `protocols/agent-communication.md` | Agent design/A2A protocol |
| Standards | `evidence-pack-standard.md`, `evolution-pattern.md` | Meta internal standards |
| Policies | `template-governance.md`, `future-policy-stubs.md` | Meta governance |
| Registry | `project-registry.md`, `context-registry.md`, `asset-registry.md`, `workboard.md` | Meta work management; the V1 artifact retention register is ATS-specific |

### Universal (Apply to All Projects)

These documents apply to all projects including domain projects:

| Category | Documents | Purpose |
|----------|-----------|---------|
| Standards | `core-principles.md`, `development-standards.md`, `documentation-standards.md` | Tier 0 constitution |
| Standards | `prompt-caching-strategy.md`, `glossary.md`, `glossary-sources.md`, `pricing-sources.md` | Standards/references |
| Policies | `security-policy.md`, `execution-policy.md`, `quality-gates.md` | Security/quality |
| Policies | `access-control-policy.md`, `versioning-policy.md`, `archive-policy.md` | Access/versioning/archive lifecycle |
| ADR | All decision records | Architectural decisions |

**Rule**: For ATStudio (`ATS`) domain work, exclude meta-only documents from context injection unless explicitly required.

## Document Update Rules

- Document count by category is managed in category indexes
- Required documents mapping by role is managed in this file
- Project-specific documents refer to project sections in category indexes
- **Document scope classification is managed in this file** (meta-only vs universal)

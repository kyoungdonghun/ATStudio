---
version: 1.7.0
last_updated: 2026-10-24
project: ATS
owner: DocOps
category: evidence-pack
status: completed
related_wi: WI-20261024-ATS-019
dependencies:
  - path: WI-20261024-ATS-019-handoff.md
    reason: Continuous bounded stage-one execution and actual-result reporting
---

# Evidence Pack: WI-20261024-ATS-019

## Summary

Recorded the user's continuous stage-one completion authorization and
SE017 -> PG018 -> MA native execution/cleanup -> Doc019 chain. Stage 1 includes
audio upload/conversion/playback/seek/download/denial, reset-link/password-change
testing and necessary current-fixture backup/isolated restore. Prior read-only-
only and separate stage-one execution waits are superseded within this scope.
The user-defined bounded stage-one audio and password-reset scope is complete.
Current capture/isolated verification, second audio trial, human sound/seek,
actual buyer3 reset and final runtime/access restoration are recorded separately.
WI019 is completed; REQ007 remains active for broader requirements. The first
audio HOLD/FAILED_LEDGER remains unchanged historical evidence.

## 2026-10-24 Final Bounded Stage-One Completion

The [audio2 receipt](../../build/WI20261024-stage-one-native/audio2-result.json)
records the bounded track1 API trial and all 13 cleanup PASS. The
[human audio attestation](../../build/WI20261024-stage-one-native/human-audio-result.json)
adds actual MP3 sound and middle-seek confirmation. browser_player_ux remains
NOT_TESTED; the dedicated SPA player UX belongs to stage 5.

The [human reset sequence](../../build/WI20261024-stage-one-native/reset-human-finish-result.json)
records OLD_LOGIN/OLD_ME, RESET_LINK, capability checks, RESET_CONSUME,
CONSUMED_TOKEN_DENIED, OLD_PASSWORD_DENIED, OLD_REFRESH_DENIED, NEW_LOGIN/ME,
LOGOUT/ME and FINAL. MA relays the user's actual mail-link opening, password
entry, new-login and logout confirmation. FINISH_REQUESTED alone is not native
completion: the subsequent [reset result](../../build/WI20261024-stage-one-native/reset-result.json)
records BOUNDED_AUTH_FLOW_OBSERVED, JAVA_RELEASED, attempt/result cleanup PASS
with all 13 steps, independent cold restore PASS and app/DB PID 0.
Its native field remains OBSERVED_NOT_INDEPENDENTLY_ACCEPTED. MA owns the native
observation; PG's public-evidence consistency check is not direct native/UI
reexecution. No broader independent-production acceptance is claimed.

[Buyer3 credential state](../../build/WI20261024-stage-one-native/buyer3-credential-state.json)
is STALE_USE_FORBIDDEN for the original saved credential; new_password_saved=false.
Do not reuse the old password. Original private inputs were retained, not
rewritten or deleted. Approved audio rows/files and login/reset effects remain;
runtime/access restoration is not a rollback of password/session/business data
or a claim that every old access JWT was immediately invalidated.

The [post-reset media comparison](../../build/WI20261024-stage-one-native/post-reset-media-result.json)
records two unchanged audio files/1,925,002 bytes; MA compared them against the
audio2 hashes. The completed 43-table/15-row backup proof captures the pre-audio/
reset state. It is not a backup of subsequent changes or full disaster recovery.

The [final access receipt](../../build/WI20261024-stage-one-native/final-access-restoration-result.json)
was observed by MA at 2026-10-24 05:50:36 KST: final independent runtime restore
PASS, app/DB PID 0, original TCP22/443 rules restored, TCP80/other rules unchanged,
owned SSH key removed, agent empty before Stopped/Disabled, provider logout
verified by login-required state, agent tab closed and user tab preserved.
Its artifact map pins the six public functional/credential/media receipts and
restoration evidence. Addresses and screenshots are not reproduced here.

[PG's final local interpretation](../../build/WI20261024-018-stage-one-review/final-stage-one-interpretation.json),
SHA256 `50736750a0960704066f077cd6311178cce686150eea96c9e33e11344b10df95`,
is BOUNDED_STAGE_ONE_ACCEPTED_WITH_STATED_LIMITS. It matches all nine artifact
pins (mismatches empty) from final-restoration SHA256
`3193886d2bf7a9a17e3d4b1f21904ba4ff45e035c9112986c290c272d9fcabbb`.
The review is local safe-artifact interpretation with no new tests or direct
server/provider/private-input access. It supplements, not rewrites, the native
reset receipt's observation qualifier and the historical failed attempt.

This closes WI019's bounded stage-one report, not REQ007 or stages 2-6. The
2026-10-24 grant day and additional-paid budget of 0 KRW remain unchanged;
no automatic extension, production/public approval or further operation follows.
Earlier failures, including the first audio aggregate FAILED_LEDGER, remain
distinct from the successful second audio and reset attempts.

## Exact Local Review and Reused Preparation Pointers

[SE021 evidence](WI-20261024-ATS-021-evidence-pack.md) pins the 82-member audio
candidate `144c3ab80e828ea063859c7218d92ea3a0f7d8f4621d1fd5ba8469efb7ba2c2d`.
[PG018's audio repair receipt](../../build/WI20261024-018-stage-one-review/audio-repair-review-receipt.json),
SHA256 `3d46754904b0f233b38bab21cf1884807a7ab938df275aa0a737a293e49e580a`,
allowed the exact approved attempt; SE six/PG four focused checks are attributed
to their owners and were not rerun by DocOps.
[Reset preparation](WI-20261024-ATS-020-evidence-pack.md) and the
[exact reset review](../../build/WI20261024-018-stage-one-review/reset-review-receipt.json)
pin 44 members/manifest `1fd18abc8c15094e57ca4827ac31766d3e5e3e809319bb05fae0d5b51e6a376a`;
review SHA256 `96853af5bf317e32ced044fc9b7c31e8e4b31337053278e697c58869de810081`.
[PG018 evidence](WI-20261024-ATS-018-evidence-pack.md) retains the source-allowlist,
SQL-context/cost and backup-proof review chain with exact historical pins.
Preparation documents' earlier native-pending notes are superseded by the MA
results above, not rewritten. These local allowances alone are not native PASS.

## Historical 2026-10-24 Second Audio API Trial; Session Still Open

MA's [audio2 receipt](../../build/WI20261024-stage-one-native/audio2-result.json)
records BOUNDED_AUDIO_API_OBSERVED, track_id 1, ADMIN 2, ENTITLED_USER 3 and
NO_ENTITLEMENT 1. All 13 cleanup steps PASS, including LEDGER. This is this
attempt's outcome; the earlier FAILED_LEDGER is not relabeled.
human_playback=NOT_RUN and browser_player_ux=NOT_TESTED remain explicit.

MA separately reports actions UPLOAD, POLL twice, ACTIVATE, STREAM, RANGE, SEEK,
INVALID_RANGE, ANON_DOWNLOAD, NO_ENTITLEMENT, RAW_BLOCK_ANONYMOUS,
RAW_BLOCK_ENTITLED_USER and DOWNLOAD. MA also reports a hash-verified private
MP3 copy of 160,958 bytes and independent fresh runtime-restore PASS. These
relayed observations are not additional fields in the compact audio2 receipt
and were not independently reobserved by DocOps; no private file/path/hash was read.

[Reset staging](../../build/WI20261024-stage-one-native/reset-code-stage-result.json)
is RESET_CODE_IDENTITY_PASS for 44 frozen code files, app_db NOT_STARTED and
private_inputs NOT_STAGED. This is code identity, not reset acceptance.
Human playback and reset readiness/results remain pending. MA reports the
user-approved temporary current-PC TCP22/443 rules applied, SSH unlocked and
provider logged in for continuous authorized work. Final access restoration is
not claimed. WI019/REQ007 remain active; stage 1 is not yet complete.

## 2026-10-24 Continuous Completion Reaffirmed

The latest direct user instruction reaffirms completing stage 1 following the
proposed local repair/review/retry. [SE021's generated handoff](WI-20261024-ATS-021-handoff.md)
governs the minimal derived audio/header handling and fixed safe diagnostics in
fresh attempt paths, followed by PG018 exact review, MA bounded continuation
and Doc019 actual reporting. The [REQ approval](../user/REQ-20261011-ATS-007.md)
preserves the verbatim instruction. Earlier no-retry-approval notes are historical;
ordinary corrections within this approved scope are not a new permission gate.

No fresh candidate or native execution result is asserted. Preserve the old
HOLD/FAILED_LEDGER, consumed claims and restoration evidence. Reuse the unchanged
JAR/WAV, saved actors and completed backup proof; no automatic uncertain-mutation
replay, new backup, old-claim deletion or wider network authority. The fixed
2026-10-24 date, 0-KRW additional-paid budget, payment/public-access exclusions,
human direct reset entry and action-time browser security confirmation remain.
DocOps performs documentation only. WI019/REQ007 remain active until actual
audio/reset and cleanup outcomes satisfy the existing stage-one contract.

## Historical 2026-10-24 Audio HOLD and Separate Final Restoration

MA's [audio HOLD receipt](../../build/WI20261024-stage-one-native/audio-hold-result.json)
records JAVA_RELEASED, OPEN_ADMISSION/HOLD and aggregate cleanup FAILED:
LEDGER FAILED, the other 12 steps PASS. audio-result.json/audio-final.json do
not exist. The [sanitized stored delta](../../build/WI20261024-stage-one-native/audio-delta-result.json)
identifies LOGIN_ADMIN and only users.2.refresh_token/users.2.updated_at changed
from the stored baseline to ledger-001. login_success_marker=false, so server-
side session effects do not establish an accepted login test. MA reports ID1/ID3
login not reached; upload NOT_REACHED and reset NOT_RUN. No later DB restart or
SQL re-query extended this observation boundary; no session rollback is claimed.

The [independent current-backup proof](../../build/WI20261024-stage-one-native/backup-proof-result.json)
confirms 43 tables/15 rows, expected fixture counts, actual column metadata
equality and cold before/after PASS. Raw SQL hashes differ: the bounded proof
identifies 209 MySQL 8.0.46 redundant column-charset insertions and all other
bytes equal. This is the captured pre-login state, not full disaster recovery,
whole-repository health or a backup of the subsequent session delta.

MA's [final access receipt](../../build/WI20261024-stage-one-native/access-restoration-result.json),
observed 2026-10-24 05:05:21 KST, records later independent
NativeAudio.verify_restore PASS, app/DB MainPID 0, original TCP22/443 rules
restored and TCP80 unchanged. The owned SSH key was removed, agent empty before
stop, then Stopped/Disabled; provider logout required login; the agent tab closed
and the user tab remained. This separate runtime/access PASS does not replace
the attempt's aggregate FAILED_LEDGER or certify ledger reconciliation.

The cause remains UNCLASSIFIED. Duplicate Vary response rejection is only a
hypothesis, not established by these safe receipts. MA relays SE's offline
synthetic send check: one Vary header accepted/body read once; three Vary lines
held at LOGIN_HEADERS before body read. This reproduces a local compatibility
issue, not the actual cause: the server response headers were not captured.
SE also reports retained JAR byte identity and embedded spring-web 7.0.9 matching
the local cache; read-only javap inspection found three separate Vary additions
in DefaultCorsProcessor with non-null configuration before isCorsRequest.
This supports the concrete local compatibility issue without proving native cause.
The proposed next scope is local response-header compatibility, fixed safe error
codes and independent review; no new candidate or server retry is approved here.
WI019/REQ007 remain active and stage-one completion blocked.
DocOps read only the four named safe receipts, with MA as native evidence owner;
no private inputs, raw logs, server/provider access or new runtime tests occurred.

## Historical 2026-10-24 Second Current Capture and Isolated Verification Passed

The user separately approved the SQL-context repair and one retry. MA's
[second capture receipt](../../build/WI20261024-stage-one-native/capture2-result.json)
records CURRENT_CAPTURE_ISOLATED_VERIFY_PASS, code NONE, stage COMPLETE,
43 tables/15 rows and snapshot_state IDENTIFIED. STOP/PID_ZERO/SOCKET_GONE/
COLD_BASELINE all PASS. automatic_retry=false; app and business_mutation
NOT_RUN; full_disaster_recovery=false. This is bounded current capture and
isolated verification, not full disaster recovery or audio/reset acceptance.
The first SQL HOLD remains historical evidence below.

MA reports inventory4 again passed with 381 members/73,105,118 source bytes.
The [refreshed cost calculation](../../build/WI20261024-stage-one-native/cost-bound-second-result.json)
retains WITHIN_CURRENT_INCLUDED_STORAGE and 252,828,618 retained bytes within
1,000,000,000, with 747,171,382 remaining. The additional paid budget is 0 KRW;
current zero API/internal-egress tariffs remain required. No dedup/compression
saving or deletion is assumed, provider_billing_enforcement=false and the
calculation itself issues no authority.

DocOps read only these public receipts; inventory4 and the separate retry
approval are MA-relayed facts. Private snapshot/proof bodies were not read.
MA is proceeding to independent cold/exact private backup-proof checks and
audio setup; their outcomes are not claimed yet. Access remains active for
the approved session. WI019/REQ007 stay active with audio/reset and final
access restoration pending; no overall stage-one completion is asserted.

## Historical 2026-10-24 Inventory/Cost and First Capture SQL Hold

MA's [inventory3 result](../../build/WI20261024-stage-one-native/inventory3-result.json)
is CURRENT_COLD_INVENTORY_OBSERVED, exit 0, 381 members/73,105,118 source bytes.
The previous frontend allowlist repair succeeded natively. The
[cost bound](../../build/WI20261024-stage-one-native/cost-bound-result.json)
and [current MA source observations](../../build/WI20261024-stage-one-native/cost-source-observations.json)
support WITHIN_CURRENT_INCLUDED_STORAGE: new-repository upper bound 89,258,698 bytes,
retained total 252,828,618 including existing 63,569,920 and other-growth reserve
100,000,000, within allowance 1,000,000,000; remaining 747,171,382. No dedup,
compression saving or deletion was assumed. Current free API/provider-internal
traffic terms and continuing retained storage within allowance support the
current 0-KRW bound. Tariff permanence/provider billing enforcement are false;
the calculation issues no authorization and fresh authority/cost gates remain.

PG's [public inventory/cost interpretation](../../build/WI20261024-018-stage-one-review/inventory-cost-interpretation.json)
is PUBLIC_CURRENT_INVENTORY_AND_COST_BOUND_CONSISTENT. It checked public evidence
and arithmetic, not provider/private facts independently, and establishes no
backup/restore/audio/reset or access-cleanup outcome. Its input pins are retained
in that artifact; DocOps did not rerun tests or inspect underlying private evidence.

The first remote capture's [safe result](../../build/WI20261024-stage-one-native/capture-result.json)
is HOLD / CAPTURE_OR_VERIFICATION_HOLD at SOURCE_SQL_WINDOW,
snapshot_state=NOT_ATTEMPTED, automatic_retry=false. The
[sanitized command diagnostic](../../build/WI20261024-stage-one-native/capture-diagnostics.json)
shows RESTIC_CONFIG and RESTIC_SNAPSHOTS exit 0, MYSQL_SOURCE_START exit 0,
two containment MYSQL_QUERY commands exit 0, then the third MYSQL_QUERY exit 1
with SQL 1046. MA's local cause analysis identifies unqualified accounts.queries
table names paired with cold.query argv lacking a database selection. SE017's
minimal context repair and PG018 remaining-stage review are in progress;
no repaired execution success is claimed.

Source DB was actually started and stopped in this attempt. STOP/PID_ZERO/
SOCKET_GONE/COLD_BASELINE all PASS; a separate
[post-capture cold probe](../../build/WI20261024-stage-one-native/capture-post-cold-probe.json)
returns PREFLIGHT_PASS, cold_before/cold_after PASS. No dump was produced and no
new backup/snapshot, isolated restore, app, audio or reset was attempted. Successful
repository commands do not establish whole-repository integrity or current fixture
recovery. Access remains active for the authorized session. Earlier no-DB notes
below are historical only; preserve all attempts/candidates. WI019/REQ007 stay active.

## Historical 2026-10-24 Initial Operational Holds

MA reports the user approved/applied temporary TCP22/443 access limited to the
current PC and unlocked SSH for this continuous session. Fresh WI014 cold probe
PASS preceded inventory1 BOOTSTRAP_HOLD before staging. Read-only diagnosis
confirmed that exact stage absent; the same prefix check later passed. A clock
sample placed the server 1.823 seconds behind the PC, without proving the first
HOLD's cause. The user explicitly allowed one retry with new records, a three-
second margin and server-epoch gate.

Inventory2 staged tools only, then NATIVE_AUTHORITY_OR_DISPATCH_HOLD. MA's
read-only diagnosis identified UNREVIEWED_SOURCE_FILE: current_members mixed the
current WI067 release root/JAR-only allowance with old WI060 frontend, omitting
143 current frontend files from the allowlist. This is the observed
tool allowlist mismatch, not evidence of an application failure or DB mutation.
SE017's in-memory overlay reuses the already reviewed frozen runtime
artifact.json spa_files list of 143 files; no broad permission or unknown-file
allowance is proposed. Repair and PG018 exact review are in progress; no repaired
candidate or successful rerun is claimed.

DocOps read MA's [sanitized diagnostics](../../build/WI20261024-stage-one-native/inventory-hold-diagnostics.json)
and the separate safe [hold cold probe](../../build/WI20261024-stage-one-native/hold-cold-probe.json):
PREFLIGHT_PASS, cold_before/cold_after PASS, db/app/backup/restore NOT_RUN.
The diagnostic confirms snapshot NOT_ATTEMPTED; MA also reports audio/reset not
attempted. These are MA receipts/relayed facts, not direct DocOps server/private-
file observations. Access remains open for this
approved session, so restoration is not marked PASS. Preserve both attempts,
staged artifacts and old frozen candidate/history. WI019/REQ007 remain active.

## Scope / DoD Check

- [x] Generated handoff and verbatim user approval recorded.
- [x] Current entry/REQ and two-set initial documents created.
- [x] Continuous scope, private/human boundaries and unchanged limits distinguished.
- [x] Interim native HOLDs and separate cold-state evidence recorded without closure.
- [x] Actual inventory/cost progress and temporary DB start/stop distinguished from backup success.
- [x] Second bounded current capture/isolated verification integrated without stage-one closure.
- [x] Actual audio HOLD/session delta and failed aggregate separated from later runtime/access restoration.
- [x] Second audio API result and 13 cleanup PASS integrated separately from pending human playback/reset.
- [x] SE017/SE020/SE021 exact preparation, cost and reset decisions linked without duplicating recipes.
- [x] PG018 exact independent local review and focused evidence linked with ownership limits.
- [x] MA actual backup/audio/reset, user confirmations and final cleanup integrated separately.
- [x] Bounded stage-one completion and remaining broader requirements reported without inferred production PASS.

## Reference Documents (Tier 0-2)

The [WI019 handoff](WI-20261024-ATS-019-handoff.md), together with the SE017 and
PG018 handoffs, supplies Tier0 core principles, development/documentation
standards and glossary; Tier1 security policy; Tier2 current index/REQ007, WI016
completion/provider receipts, reviewed WI011 backup and WI005 audio wiring
READMEs, and historical WI-20261021-ATS-009 evidence. Assignee DocOps; injected
context follows `.claude/config/context-injection-rules.json`. Applied
create-wi-evidence-pack and scoped validate-docs; English records/Korean REQ.
Private input/provider/server access remains MA-only.

## Evidence Pointers

- [Current entry](../../docs/index.md) and [REQ007 verbatim continuous approval](../user/REQ-20261011-ATS-007.md).
- [SE017 handoff](WI-20261024-ATS-017-handoff.md) -> [PG018 evidence](WI-20261024-ATS-018-evidence-pack.md), with SE020/SE021 evidence above: exact preparation/review history.
- [WI016 completed read-only precheck](WI-20261024-ATS-016-evidence-pack.md): historical factual input, not a fabricated new native outcome.
- [Own summary](../user/WI-20261024-ATS-019-summary.md): completed bounded stage one, preserved failures and remaining limits.

## Current Authorization and Remaining Preconditions

The latest instruction authorizes completing stage 1 without repeated routine
scope approvals. Necessary current-fixture backup/isolated restore and bounded
audio/reset execution are now included, subject to exact review and existing
guards. Older read-only-only/native-approval waits remain historical and do not
override this later instruction. No actual outcome is implied by authorization.

MA alone handles provider/server operations and private references. Human
password entry and browser-security expansion confirmation at action time remain
required. Prepare technical prerequisites before unlock and maintain access only
for the approved work session. Agents do not inspect private files or account
values. Reuse frozen tools, JAR, actors and approved WAV; review changed bytes
independently and avoid broad old-suite reruns.

Preserve ID1 and pre-existing business data. The reviewed audio contract's exact
login-session exception must be stated explicitly rather than claiming every
existing cell is unchanged. SE/PG must resolve reset actor, existing mail/UI path,
human-entered new password, exact password/session effects and safe saved-input
update before execution. No reset actor, including ID1, is selected implicitly
by this initial record.

Additional paid budget remains 0 KRW. Actual source/upload/restore/request/
retained-storage bounds and genuine provider evidence remain prerequisites;
unknown or positive cost stops capture. Reuse the existing repository without
init or deletion and retain old snapshots. Grant-date extension is not automatic;
the October 24 KST boundary remains. Public access, purchase/payment, unrelated
deletion/schema changes, commit/push and stages 2-6 are excluded. No broader
business mutation or destructive cleanup is inferred.

Stage 1 requires actual audio and reset outcomes plus cleanup evidence. Local
synthetic PASS, prior success and saved inputs are insufficient. Exact candidate,
focused checks, review verdict and MA actual results remain pending now.

## Commands & Outputs

Read the generated WI019/SE017/PG018 handoffs and current index/REQ metadata.
Added only current authorization checkpoints and own two-set documents. No
private file, provider/server, source/product test or operational command was
accessed/run. Validation covers only new scopes/links, metadata/status/date,
verbatim approval and whitespace, without a historical link sweep.

Initial validation PASS: four owned documents/four new scopes, 19 relative links,
metadata/status/date, whitespace/terminal newline and verbatim Korean approval.
Continuous authority and pending real outcomes are distinct; the 0 KRW budget
and human/security boundaries remain explicit. No runtime or product test ran.

Interim validation PASS: four owned documents/four new scopes and eight relative
links; metadata/status/date, whitespace and active/pending boundaries. Only the
two safe MA diagnostic/cold receipts were read. No private/native operation,
test rerun, access-restoration claim or stage-one closure was added.

Capture-HOLD update validation PASS: four owned documents/five new scopes,
19 relative links, metadata/status/date, whitespace and the actual DB-start/
stop versus no-backup distinction. Read only sanitized native and public PG
receipts; no provider/private/server access or test rerun. Cost arithmetic is
current and conditional, not permanent pricing or new authority. WI019 stays active.

Second-capture update validation PASS: four owned documents/five changed scopes,
10 relative links, metadata/status/date, new-scope whitespace and no raw IPs.
The actual bounded capture/isolated-verification PASS is distinct from pending
audio/reset and final access restoration. No private reads or runtime tests ran.

Audio-HOLD/restoration update validation PASS: four owned documents/six changed
scopes, 15 relative links, metadata/status/date, whitespace and no raw IPs.
Aggregate FAILED_LEDGER, later restoration PASS, unconfirmed native cause and
active/blocked scope remain distinct. No runtime tests or private reads ran.

Reaffirmed-authorization update validation PASS: four owned documents/five
changed scopes, six relative links, metadata/status/date, whitespace and the
verbatim Korean instruction. No new native result, runtime test or private read.

Second-audio update validation PASS: four owned documents/six changed scopes,
nine relative links, metadata/status/date and whitespace. The second attempt's
13 cleanup PASS remains distinct from the first FAILED_LEDGER, pending human
playback/reset and still-open access session. No runtime tests/private reads ran.

Final closeout validation PASS: four owned documents/seven changed scopes,
34 relative links, metadata/status/date, new-scope whitespace, no raw addresses
or URLs, completed-WI/active-REQ distinction, preserved FAILED_LEDGER and stale
credential boundary. Existing Tier0/handoff paths are present. No historical
link sweep, runtime tests, source changes or private reads were performed.

## Risks / Rollback

Continuous authorization does not manufacture cost evidence, remove exact
candidate/target guards or human security boundaries, or prove actual completion.
Preserve historical failures/receipts and report unresolved blockers precisely.
DocOps performs additive documentation corrections only; MA owns temporary
access/runtime restoration under the reviewed scope. No deletion is implied.

## Follow-ups

WI019 has no remaining work and blocks no further WI in its handoff. Keep
REQ007 active for broader requirements; do not automatically begin stages 2-6
or production work. Buyer3's old saved credential stays forbidden and the new
password is not stored. Preserve historical attempts, backup-scope limits and
the unchanged grant date/cost boundaries. No additional runtime action, private
input request, date extension or automatic retry is initiated by this closeout.

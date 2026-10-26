---
version: 1.5.0
last_updated: 2026-10-23
project: ATS
owner: DocOps
category: evidence-pack
status: stable
related_wi: WI-20261023-ATS-013
dependencies:
  - path: WI-20261023-ATS-013-handoff.md
    reason: Inspect-only approval and final-result integration contract
---

# Evidence Pack: WI-20261023-ATS-013

## Summary

Final outcome: saved-input read-only inspection completed with
INSPECTION_RECORDED / INSPECTED_CONDITIONS_PASS, all five DB cleanup checks PASS,
and a separate target-bound PREFLIGHT_PASS / COLD_QUARANTINE_MATCH. Final access
restoration is confirmed. WI013's approved bounded inspection/report scope is
complete; REQ007 remains active for future separately approved date/write/audio
work. No input recollection, account/business changes, app/login or audio occurred.

## Historical Renewed-Approval Checkpoint

Current outcome: the user explicitly reapproved MA's read-only inspection using
the saved inputs, with server access, temporary DB start/stop and restoration
reporting. No recollection is required. MA's local preflight passed; no actual
server contact or inspect has occurred at this checkpoint. WI013 and REQ007
remain active. Date/apply, account/business writes, app/login and audio remain
excluded. Local capture completion below is preserved as a preceding result.

## Historical Actual Local Capture Checkpoint

Current outcome: local private capture is COMPLETE. The user confirmed successful
Save/window closure; MA's metadata/ACL-only check confirmed file existence,
PRIVATE_ACL_OK and capture-process absence. No server contact occurred during
this check; inspection remains NOT_RUN and this operation has not started the
DB. WI013 and REQ007 remain active. Date/apply, account/business writes,
app/login and audio remain unapproved and unperformed.

## Historical Local Repair and Input-Wait Checkpoint

Current outcome: WI014/WI015 local repair and exact review are complete. MA
confirmed the revised Korean LOCALONLY capture window under existing approval
after correcting a local helper launch setting. Actual Save remains NOT_RUN;
WI013 awaits human input. No server work resumed for repair/relaunch and the
earlier access restoration remains the last state; inspection is NOT_RUN.
WI013 and REQ007 remain active. Local acceptance is not real-input success or
permission to select a date/apply.

## Historical Save-Failure Checkpoint

Current outcome: staging succeeded, but the actual dedicated-window Save was
rejected with a generic VALIDATION error. The latest metadata-only check found
no input file and no capture process; the disappearance cause and closer are
unknown. Inspection is NOT_RUN and this operation never started the DB. The
cold post-stage probe and access restoration are confirmed. The GUI had been
deliberately preserved before that later check. Readiness remains HOLD pending
bounded local diagnosis/repair and exact independent review. WI013 and REQ007 remain active.

## Historical Initial Approval Checkpoint

Recorded the latest approval: future fixture scope and verified TEST semantics
are agreed, while execution is limited to server access/staging, dedicated-window
private inputs and evidence retention, temporary DB start/stop and read-only
inspection. Actual date/apply remain explicitly deferred. Preparation/review are
in progress; no capture, staging, native inspection or restoration result is
claimed at this initial checkpoint. WI013 and REQ007 remain active.

## Scope / DoD Check

- [x] Exact Korean approval and continuing Astra Extra High preference recorded.
- [x] SE011 -> PG012 -> MA capture/staging/inspect/restoration -> Doc013 chain recorded.
- [x] Agreement to TEST scope distinguished from absent date/write authority.
- [x] ID 1/all-existing-row preservation and private-input confidentiality recorded.
- [x] Initial scoped documentation validation passed.
- [x] Exact preparation/candidate/review/test/preservation evidence integrated.
- [x] Actual staging, Save failure, NOT_RUN inspection and limitations integrated.
- [x] DB/access restoration and WI012 independent interpretation integrated.
- [x] Concrete proposal/date/write boundary and final scoped report completed.
- [x] Cold-state and access restoration distinguished from later process absence.
- [x] WI014 diagnostic repair and WI015 exact independent review integrated.
- [x] MA's revised-window appearance confirmed separately from Save success.
- [x] Actual revised-window capture completion integrated with user/MA attribution.
- [x] Renewed read-only execution approval with saved inputs recorded.

## 2026-10-23 Final Saved-Input Inspection and Restoration

Verification date: 2026-10-23. MA owns the bounded native execution, private
target/approval binding and provider/local-service observations. PG012 owns
independent interpretation of sanitized receipts; DocOps read those public
receipts and documentation only. The saved input was reused without recollection.

| Evidence | Actual outcome | SHA-256 |
| --- | --- | --- |
| [Stage](../../build/WI20261023-fixture-inspect-resume/stage-result.json) | INSPECT_STAGING_READY, input reused, DB not yet started, SSH exit 0 | `258e3c39390bf7517961a443700a9056a2f679495cd8cc62d15876850fd07be9` |
| [Inspection](../../build/WI20261023-fixture-inspect-resume/inspect-result.json) | INSPECTION_RECORDED, COMPLETE, INSPECTED_CONDITIONS_PASS, SSH exit 0 | `95ffefe3b44cca856a1d63afd60fe774fc648adea29d49f519812ed755f1292a` |
| [Separate post-stop probe](../../build/WI20261023-fixture-inspect-resume/post-stop-probe.json) | PREFLIGHT_PASS / COLD_QUARANTINE_MATCH, inventory NOT_RUN, SSH exit 0 | `a48e4b4fdba5e12b4761f96a0c7c22e17dc398e30236340ba140f0b0aa434c60` |
| [Final restoration](../../build/WI20261023-fixture-inspect-resume/final-restoration.json) | RESTORATION_CONFIRMED | `98aa3c5f0024d2516210cd909f7e38f85c7a5da7bf935606e601631dea8d57bc` |
| [PG native interpretation](../../build/WI20261023-012-fixture-inspect-review/native-interpretation.json) | OBSERVED_READONLY_INSPECTION_AND_DB_CLEANUP_PASS | `29d5a07f745c195bcc2d4ad393a644efaf11eba2dd3f7a2550401126a8ceeb76` |
| [PG access interpretation](../../build/WI20261023-012-fixture-inspect-review/access-interpretation.json) | BOUNDED_READONLY_INSPECTION_AND_RESTORATION_ACCEPTED | `5260f1c008012b9a4e28bc318a0ec05112d3843ef939f4eb1ec0da939aed6e88` |

MA receipts: fresh target/record 46a1d9; stage 2c1d8e; inspect 1ece45; independent
target-bound post-probe 358504; owned-key removal fed790; service/key-window
confirmation dd7822; final restoration abe0cf. DB_SESSION, STOP, PID_ZERO,
SOCKET_GONE and BASELINE all PASS. The separate probe's inventory NOT_RUN means
it checked the cold baseline; it does not negate the completed inspection.

Selected-input identity collisions, ADMIN count and existing work are zero,
with no HOLD reasons. ID 1 passes the frozen guard: USER, nondeleted, verified,
nonempty password and no subscription rows. Credentials/login are unverified.
Eligible active finite INDIVIDUAL plans are ID 1 at 5/day and ID 2 at 20/day.
Unlimited INDIVIDUAL and BUSINESS plans are excluded. Plan/date are not selected;
apply_authorized and date_selected are false. No business/schema writes,
account creation/promotion/grant, app/login or audio occurred.

Final restoration confirms original TCP22 saved-rule readback, unchanged 80/443,
provider logout with Login link visible and zero created browser tabs. The owned
key was removed with exit 0 and zero identities before the agent was stopped.
Fresh agent state is Stopped/Disabled; the key-input window is closed. Final
private ACL verification passed and inputs/evidence remain retained. Address-
bearing screenshots are retained by MA but not reproduced in this document.

The [PG012 evidence addendum](WI-20261023-ATS-012-evidence-pack.md) preserves the
earlier local review and its pending-access interpretation. Native target/payload
pins were reported by MA, not recomputed privately by PG or DocOps. Access
completion is supported by the final MA restoration receipt and separate PG
access interpretation; the earlier pending-access record remains historical.
Local provenance remains WI011 candidate
`21daba86032674c87844d9a584fee5ec42e3c689db806e53c0a961ebfa407245`, PG012 receipt
`1de3b8b0543164439c88006d8142822505c702bc8237ac94aa1b468fc0a2072a`, SE 8 + PG 10
local checks and the Windows synthetic ACL selftest. Those original checks did
not establish actual GUI Save success; WI014/015 repair and later actual capture
are separately recorded below. MA reported Source643/JAR and frozen WI005/011/014
preserved; this final documentation pass did not repeat that scan.

### Future Proposal and Execution Boundary

The proposal preserves ID 1 and all existing rows/fields. Future separately
approved writes would insert two new USER accounts, promote only the designated
new uploader to ADMIN with one audit row, and add one non-billing entitlement
for the new buyer. Plan 1 (5/day) is the minimum sufficient recommendation, not
selected or applied. Proposed entitlement terms remain MONTHLY/CANCELLED for
one explicitly approved KST day, start=expiry and pending fields NULL. No date
is chosen, no extension is automatic, and app/login/audio need separate scope.

Successful inspection intentionally retains fixed HUMAN/APPROVAL files and
private evidence. An unchanged later wrapper stage will hit the existing-file
collision guard. A future inspect/apply therefore needs a reviewed local plan
for approval/collision handling and applicable actual approval; fresh inspection
is not an automatic next action. No replay, renewal, deletion or replacement is
authorized here. This future gate does not negate the completed inspection.
The original real Save failure remains UNCONFIRMED. WI013 completes the current
approved chain; REQ007 stays active without triggering further execution.

## 2026-10-23 Renewed Read-Only Execution Approval

The [REQ's verbatim approval](../user/REQ-20261011-ATS-007.md) authorizes MA to use
the already saved inputs for read-only inspection, including server contact and
temporary DB start/stop, then confirm restoration and report results. It does
not authorize recollection, date/apply, account/business/schema changes,
app/login or audio. All existing rows and fields, including ID 1, stay preserved.

MA local preflight b0bd47 reported Source643 mismatch count 0, retained JAR
match, exact frozen WI005 22-member / WI011 five-member / WI014 four-member
trees, exact PG012 review receipt, saved-input existence and restricted ACL
true. Branch main/HEAD 91a16e7 and unchanged current client source were reported;
SSH agent was stopped. These are MA-owned local observations. DocOps did not
rerun source checks or access private input values, body or digest.

No new server contact or inspect had occurred at this checkpoint. MA will
coordinate the provider-action/owned-key boundaries, then fresh target and exact
approval records, staging, read-only inspection and cleanup. Later execution
retains its fresh-record/expiry gates; the approval is not permission to replay
an expired operator record. DB and access restoration for the new run remain
pending actual receipts. Prior capture and restoration are historical evidence,
not prospective claims about the new run. The work remains inside existing
WI013; no new implementation or test scope is added.

## 2026-10-23 Actual Local Capture Completion

The user explicitly confirmed successful Save and window closure. MA then used
metadata and the reviewed WI014 ACL verifier only; DocOps did not inspect the
private directory, file body, values or digest.

| Evidence owner / receipt | Observation | Boundary |
| --- | --- | --- |
| User, relayed by MA | Save completed and window closed | Actual human confirmation, separate from synthetic checks |
| MA tool 09417c | private_input_exists=true | Existence only; no content or private digest read/output |
| MA tool 09417c / reviewed WI014 Verify | private_acl_ok=true, exit 0, PRIVATE_ACL_OK | Restricted ACL verified without reading values |
| MA tool 09417c | Capture PID 22096 absent | Confirms that capture process ended; no all-windows claim |
| MA tool 09417c | SSH agent Stopped/Disabled; server_contact=false | No server resumption; prior access restoration remains the last state |
| Operation boundary | inspect NOT_RUN, DB not started | No native fixture inspection or data/application acceptance |

This establishes local capture completion under the existing private-input
approval. It does not establish a fresh private schema/snapshot/identity
inspection, grant a date/apply, or permit account/business DML, app/login or
audio. Subsequent inspect retains fresh target, exact approval, expiry, staging
and cleanup requirements. The original Save failure remains UNCONFIRMED; the
later successful capture is not a retrospective diagnosis. Prior synthetic
tests and earlier failure/access checkpoints remain unchanged.

## 2026-10-23 Exact Local Repair Review and Relaunch Checkpoint

The [WI014 candidate](../../build/WI20261023-014-capture-repair/candidate.json)
contains four members, SHA-256
`907206f3e918310b30c70afcd5bdd02f448fbf42cd8446ce6b2b58460e989450`;
bundle `97965d8016575e148270ddaf3660cf89f5028c29519fab40ab3ca8e97f7c7c0a`.
The [PG015 receipt](../../build/WI20261023-015-capture-review/review-receipt.json)
SHA-256 is `fc4897a1c981fa495cfe6ab5bab995dc407a5723e990116059c71528502b1c1c`,
verdict ALLOW_EXACT_LOCAL_CAPTURE_REPAIR_ONLY. DocOps verified that receipt hash
and read the [SE014 evidence](WI-20261023-ATS-014-evidence-pack.md) and
[PG015 evidence](WI-20261023-ATS-015-evidence-pack.md); no test rerun or real input
access was needed for this integration.

SE seven focused checks and PG four independent checks PASS; PG also reran the
seven SE checks. The actual .NET Click event uses synthetic controls and reaches
real current-user-only ACL storage. Tested cases include invalid field,
confirmation, runtime and storage-preservation failures, followed by correction
and successful Save. Korean actor/field labels and fixed runtime/storage codes
replace a generic error and retain entries without reflecting values in tested
output. These are local synthetic results, not visible human interaction.

The original frozen callback also passed actual protected synthetic storage.
The earlier real Save failure remains UNCONFIRMED, with no attribution to user
input. Confirmed scope is diagnostic separation and local ordering. No automatic
transfer from an old process is implemented; no promise of avoiding re-entry.

MA confirmed the exact four members and receipt, prepared a fresh restricted
current-user directory, and used an explicit actual Python executable and the
existing approval flag. Initial helper 10372 (launch 79639a, first check 008808)
had an empty title and no window handle. MA stopped only that owned helper,
then followed the README with one visible Normal launch; no user entry attempt
occurred between the two. Tool 22f7ef confirmed process 22096, title
`ATStudio 비공개 입력 — 로컬 저장만` and a nonzero main-window handle. MA sent the
input instruction; no private values were read. Real Save and inspect remain
NOT_RUN. This local launch-setting correction is separate from the unresolved
original Save failure. Window appearance is MA-observed, not independently seen
by DocOps or PG. No server contact occurred during this repair/relaunch, and
the prior access-restoration checkpoint remains the last observed access state.

Local capture now precedes the remote 900-second timer. Future inspection keeps
fresh target, exact approval pin, expiry, staging and cleanup gates unchanged.
WI015 neither issues nor renews remote authority. WI011's five-member and
WI005's 22-member candidates are preserved; WI012's earlier bounded review
remains historical. No date/apply, DB/app/login/DML/audio activity is authorized
by the repair. WI014 -> WI015 is complete; WI013 awaits actual capture results.

## 2026-10-23 Actual Save Failure and Sanitized Cleanup Record

Evidence owner: MA for native staging, probe, provider UI and local service
observations; SE/PG for bounded synthetic diagnosis. DocOps read only sanitized
stage/probe receipts and integrated supplied observations. This is a public
record inside the four-file WI013 documentation scope, not a new native probe.

| Observation | Actual result / boundary |
| --- | --- |
| Exact staging | INSPECT_STAGING_READY, SSH exit 0, DB started false, input collected false |
| Actual input Save | Generic VALIDATION rejection; latest metadata-only check found no input file |
| Read-only fixture inspection | NOT_RUN; no account/private schema/snapshot/identity result |
| Cold post-stage probe | PREFLIGHT_PASS, phase COMPLETE, inventory NOT_RUN, DB started by this operation false, SSH exit 0 |
| Firewall | Original TCP22 source restored with saved-rule readback; 80/443 unchanged; exact addresses omitted |
| Provider/browser | Logout followed by visible Login link; created tab 6 closed and tab list empty |
| Local SSH | Owned-key removal exit 0; no identities; fresh Win32_Service readback Stopped/Disabled (MA tool 33763a) |
| Earlier input GUI observation | Process 27684 deliberately preserved at restoration; this is historical |
| Latest metadata-only check | MA tool fbc633: capture process 27684 absent, private input file absent, agent Stopped; no values read or window closed by that check; cause/closer unknown |
| Excluded actions | No DB start, account/business/schema DML, date/apply, app start/login or audio in this operation |
| Preservation | Source/JAR unchanged, MA-reported; no DocOps source/runtime rerun |

- [Stage receipt](../../build/WI20261023-fixture-inspect-native/stage-result.json)
  SHA-256 `14119ea1513382c0106c29a511a3f3a4eee4fc395a65be3df97216ab6056ee54`.
- [Cold probe](../../build/WI20261023-fixture-inspect-native/post-stage-probe.json)
  SHA-256 `a4635b7d4bdfd80841a57a705ee21d65fbd83ace50f31f1ca672ca05b50f5647`.
- MA retained the firewall restoration screenshot; its address-bearing contents
  are not reproduced here. Provider/service observations are attributed to MA,
  not independently observed by DocOps.

Only staging and cold-state proof were accomplished. Since the DB never started,
this is not an inspection PASS or a four-item post-inspection DB cleanup PASS.
Earlier account/plan results remain historical and do not substitute for the
missing fresh private inspection. Process absence does not establish that all
native windows are closed or that a private-input deletion action occurred.

The bounded synthetic actual .NET Click Save callback passed JSON creation,
validation and mock storage with direct Python; PG's independent narrow JSON
checks passed. Those results do not show that real private input capture worked.
The actual root cause remains unresolved. The generic catch combines confirmation,
field-validation and runtime exceptions; earlier coverage omitted the actual
Save callback. Prior scoped tests remain PASS within that limited scope.

The governing repair chain is [WI014 SE local minimal diagnosis/repair](WI-20261023-ATS-014-handoff.md)
-> [WI015 PG exact independent review](WI-20261023-ATS-015-handoff.md)
-> WI013 integration under existing preparation approval. Actual-window access,
a new GUI launch and server resumption are outside that repair activity. Do not
resume readiness claims before the exact repaired path and review are supported.

## Reference Documents (Tier 0-2)

MA and the [WI013 handoff](WI-20261023-ATS-013-handoff.md) injected the governing
context. Continuing create-wi-evidence-pack and validate-docs skills apply; the
handoff exists. Assignee DocOps, project ATS, English evidence and Korean REQ.

| Tier | Document | Applied boundary |
| --- | --- | --- |
| 0 | docs/standards/core-principles.md | Explicit inspect-only authority and preservation |
| 0 | docs/standards/documentation-standards.md | Additive two-set evidence and language |
| 0 | docs/standards/development-standards.md | Exact source/candidate identity and bounded checks |
| 0 | docs/standards/glossary.md | REQ/WI terminology |
| 1 | docs/policies/security-policy.md | Restricted input/snapshot storage and no secret output |
| 1 | docs/policies/quality-gates.md | Independent review and actual native receipts |
| 2 | docs/index.md Current Decision Entry | Current versus historical approval/result |
| 2 | deliverables/user/REQ-20261011-ATS-007.md | Verbatim new approval |
| 2 | deliverables/user/WI-20261023-ATS-007-summary.md | Frozen fixture tool and exact execution gates |
| 2 | deliverables/user/WI-20261023-ATS-010-summary.md | Dated account/plan results and restoration |

## Evidence Pointers

- [Current entry](../../docs/index.md) and [REQ007](../user/REQ-20261011-ATS-007.md): latest approval and chain.
- [SE011 handoff](WI-20261023-ATS-011-handoff.md): private capture/inspection preparation.
- [PG012 handoff](WI-20261023-ATS-012-handoff.md): exact independent review.
- [WI013 summary](../user/WI-20261023-ATS-013-summary.md): initial scope and pending evidence.
- Retained handoff inputs: frozen WI005 candidate/README, WI006 review,
  WI008 reader and previous read-only final-restoration receipt.

## Current Permission and Remaining Gates

The user agrees to keeping ID 1 unchanged, the proposed fixture scope and
verified TEST flags. This is not permission to create two accounts, promote an
ADMIN, create an audit or grant an entitlement now. All existing rows/fields
remain immutable in this scope. TEST flags do not claim a verification-mail
event or fabricated human consent.

MA alone coordinates the authorized server/staging/temporary read-only DB
lifecycle. Real new-account inputs must use the dedicated window after technical
readiness; do not request them early. Retain inputs/snapshots only in restricted
private storage and transport. No real identity/password, private input digest
or snapshot body belongs in public evidence or chat. DocOps does not read those
private inputs. Subagent actions remain local.

No actual grant date or apply is approved. The document date is not a selected
execution date. App/login/audio/mail/payment, business/schema writes, publication,
deletion, commit and push remain excluded. Reuse exact reviewed frozen bytes;
old success is not a current target guarantee.

Earlier ID 1 total/effective rows 0, no ADMIN and eligible plans 1/2 are dated
observations. Fresh schema/snapshot/new-identity inspection remains necessary.
Inspection age <=900 seconds means a later approved apply may require new
inspection; no silent extension or reuse beyond its allowed age.

## Commands & Outputs

DocOps read the generated WI013 handoff and current index/REQ entry headers.
Only additive index/REQ checkpoints and own WI013 evidence/summary are changed
with apply_patch. No remote access, secret/private input read, service/DB/app
action, business query, product test, staging or capture was performed by DocOps.
Initial documentation checks cover only the new owned sections/files.

Initial validation PASS: four owned scopes, 18 relative links, required metadata/
status/version, whitespace/new-file terminal newline and verbatim Korean
approval. No exact IPv4 literal or HTTP URL appeared in those scopes. No
historical link scan, implementation-test rerun, private-input read or remote
operation was performed.

Save-failure checkpoint validation PASS: four owned files/four new sections,
19 new relative links, metadata/version/status, whitespace and terminal newline.
New sections contain no exact IPv4 literal or HTTP URL. Status remains active,
inventory NOT_RUN is explicit, and latest process/file absence is attributed to
MA's metadata-only check. Validation did not scan historical links or rerun
source, product, private-input, process or native tests.

Local-repair/relaunch integration validation PASS: four owned files, seven new
or changed scopes and 13 relative links; metadata/version/status, whitespace,
terminal newline and no exact public IPv4/HTTP URL in those scopes. WI013 stays
active and real Save/inspect remain NOT_RUN. No historical link scan or test/
native/process/private-input recheck was performed by DocOps.

Actual-capture integration validation PASS: four owned files, seven new/changed
scopes and five relative links; metadata/version/status, whitespace and terminal
newline. The checked scopes contain no exact IPv4/HTTP URL; local capture is
COMPLETE while inspect stays NOT_RUN and WI013 stays active. No new tests,
private-body reads, operational checks or historical link scan were performed.

Renewed-approval integration validation PASS: four owned files, seven new/changed
scopes and six relative links; metadata/version/status, whitespace, terminal
newline and verbatim Korean approval. No exact IPv4/HTTP URL appears in those
scopes. WI013 remains active; no private-input or operational checks, new tests
or historical link scan were performed by DocOps.

Final integration validation PASS: four owned files, seven new/changed scopes
and 15 relative links; metadata/version/status, completed DoD, whitespace and
terminal newline. No exact IPv4/HTTP URL appears in those scopes. WI013 is
complete/stable and REQ007 remains active. No new tests, operational/private
checks or historical link scan were performed for documentation completion.

## Risks / Rollback

Never turn scope/TEST agreement into date/apply authority or local preparation
into native success. Retain earlier evidence and frozen candidates. Rollback
means stopping and preserving inputs/logs/claims, not deletion or compensating
DML. Documentation reversal must touch only this WI's additions/metadata and
preserve unrelated work; no reset to HEAD is authorized.

## Follow-ups

WI013's approved read-only inspection/restoration/report scope is complete. No
further WI or native action is triggered within that scope. REQ007 remains active
for the proposed account/entitlement preparation and later audio acceptance;
date, actual writes and audio remain unapproved. Before any later execution,
the retained approval/evidence collision must be handled by a reviewed local
plan under applicable authority, without unapproved deletion or replacement.

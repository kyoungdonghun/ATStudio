---
version: 1.1.0
last_updated: 2026-10-24
project: ATS
owner: DocOps
category: evidence-pack
status: stable
related_wi: WI-20261024-ATS-013
dependencies:
  - path: WI-20261024-ATS-013-handoff.md
    reason: Approved local backup-tool preparation and exact-review scope
---

# Evidence Pack: WI-20261024-ATS-013

## Summary

Completed the SE011 -> PG012 -> Doc013 local preparation/report chain. The exact
19-member existing-repository adapter passed SE10 + independent PG6 focused
checks; PG allowed local preparation only, with no local blocker and native
authorization false. WI013 is complete; REQ007 remains active. Actual cost,
current recovery and date changes remain unverified/proposal-only.

## Scope / DoD Check

- [x] Generated handoff and verbatim user approval recorded.
- [x] Current entries and two-set initial evidence created.
- [x] Existing-repository, cost, recovery and date approval boundaries separated.
- [x] Final SE011 candidate/preservation/focused evidence integrated.
- [x] Exact PG012 verdict and remaining execution gates integrated.
- [x] Final local-readiness and precise later-approval report completed.

## 2026-10-24 Final Local Completion

| Public evidence | Exact identity |
| --- | --- |
| [Final candidate](../../build/WI20261024-011-backup-preparation/candidate.json) | 158d2bb3064d112e625b7b8db914925636fe9b72f68161671da862d84dc19839; 19 members |
| Final bundle | 57ff70762fcb531a8ba3669a12fb33f120184931dcf6d4655988681e2bf4680f |
| [PG012 final additive receipt](../../build/WI20261024-012-backup-review/review-receipt-final.json) | c6307fa7a5ccb539480eef388ec765fe2d02419f743078e3cfd6c0189c08f534 |
| [Final README](../../build/WI20261024-011-backup-preparation/README.md) | ExistingCapture contract, real authority/cost evidence and later transport gates |
| [Date proposal](../../build/WI20261024-011-backup-preparation/grant-date-proposal.md) | Proposal only; no DML/date gate implemented |

PG observed the final exact local result at 2026-10-23T18:29:08.926069Z
(2026-10-24 03:29:08 KST): ALLOW_EXACT_LOCAL_PREPARATION_ONLY,
native_authorized=false, no local blocking findings. Independent6 + SE10 = 16
focused checks passed; SE tests were not independently rerun and old whole suites
were not rerun. Eleven reused helpers and original/copied dependencies matched;
Source643 and retained JAR passed. MA final command0321ef verified candidate raw
SHA, all 19 members and the PG receipt pin without tests/native access. MA also
reported empty scoped source diff and unchanged retained JAR. DocOps only read
the public README/proposal/receipt and recomputed candidate/receipt hashes.

The final refreeze changes only README column-metadata storage wording; the
other 18 members, including all five executable/test files and 11 helpers, are
unchanged. SE10/PG6 evidence is reused without rerunning tests. Prior candidate
218733ad0e005b01deff8417aa136d8406417604f63c5c3b7d85d40355900041 is preserved
in `build/WI20261024-011-backup-preparation-history/`; the earlier
[review receipt](../../build/WI20261024-012-backup-review/review-receipt.json),
f138d628e36fbccc6555e43de666047a152cfaa02e8444798cf78c6dda96d013, remains
historical. Final additive allowance above supersedes its candidate binding.

The final scope supersedes the initial full-repository-cost draft below.
ExistingCapture never invokes the consumed first-capture entry or restic init.
It prepares one current backup and isolated restore of **that new snapshot**,
including all file hash/type/mode/owner/empty-directory checks, full 43 table
counts/column metadata and full deterministic dumps (at most 2MiB each). Column
metadata is private work comparison evidence, not part of the snapshot payload.
SQL acceptance uses exact raw equality or the retained narrow 209-column charset
proof with all other bytes unchanged; no generic normalization. Repository-wide
check --read-data is excluded; runtime secrets/credentials and full disaster
recovery are excluded. All actual backup/restore acceptance remains NOT_RUN.

The default additional paid budget is 0 KRW; actual cost is UNVERIFIED.
Future MA-reviewed external evidence must bind the actual account/repository,
measured repository bytes/object count, metadata/list/upload and exact-new-
snapshot restore requests/egress/tax, retained storage and continued storage
beyond the planning horizon. Evidence must be observed within 600 seconds and
cover the approval expiry; approval lasts at most 1800 seconds. Unknown/nonzero/
stale cost or unsupported finite bounds mean HOLD. This is authorization
checking, not provider billing enforcement. Throughput 4096/deadline/size limits
do not guarantee a monetary cap. No purge, deletion at retention expiry or
purchase is assumed. Initial inventory requires separate authority and reads
cold metadata without DB/cloud; capture then needs genuine current complete
target, escrow, expected fixture, decision and cost bindings.

Future ordering first permits separately approved read-only provider billing/
allowance-page inspection without purchases or settings changes. Read-only S3
requests can incur charges: establish request charges and a finite 0-KRW bound
before any potentially billed API/metadata query or transfer. Unknown fees stop
that step; an initial read-only phase is not automatically free. Only then may
separately approved bounded repository metadata queries support the cost proof.

Grant1 remains started_at=expires_at=2026-10-24. The future minimal proposal
preserves started_at and changes expires_at only to explicit D, plus updated_at
and one truthful audit if separately implemented/reviewed/approved. Since the
application ignores started_at for activation, future D extends entitlement
through D; it does not restrict it to D alone. A D-only trial gate is separate
and not implemented here. No product approval/payment/consent event may be
fabricated. Pre/post-correction backups are distinct recovery points; the
expected current row/audit counts must bind the selected point.

No server/provider/private-input access, new backup, restore, app/login/audio or
grant-date change occurred. Current fixture recovery coverage remains
NOT_ESTABLISHED. Snapshot uncertainty is NOT_ATTEMPTED/POSSIBLE/IDENTIFIED;
lost native response is UNKNOWN_POSSIBLE, requiring preserved artifacts and
separately approved read-only reconciliation rather than automatic retry.
STOP/PID_ZERO/SOCKET_GONE/COLD_BASELINE remain separate future observations;
cleanup does not depend on unexpired operation/cost authority. Native work,
cost confirmation and any date-write implementation need later explicit scope.

## Reference Documents (Tier 0-2)

The [governing handoff](WI-20261024-ATS-013-handoff.md) injects Tier0 core
principles, documentation/development standards and glossary; Tier1 security/
quality policies and DocOps role; Tier2 current index/REQ007, historical WI069/
WI070 evidence, WI010 completion and safe inventory, and the retained restore-
only handoff. Assignee DocOps; context source `.claude/config/context-injection-rules.json`.
Applied create-wi-evidence-pack and scoped validate-docs; English technical
records/Korean REQ. No actual private/provider/server access is performed.

## Evidence Pointers

- [Current entry](../../docs/index.md) and [REQ007 approval/proposal](../user/REQ-20261011-ATS-007.md).
- [SE011 handoff](WI-20261024-ATS-011-handoff.md) -> [PG012 handoff](WI-20261024-ATS-012-handoff.md): completed local implementation/exact-review chain.
- [SE011 evidence](WI-20261024-ATS-011-evidence-pack.md) and [PG012 evidence](WI-20261024-ATS-012-evidence-pack.md): implementation/review ownership; final additive receipt above governs the README refreeze.
- [WI069 historical evidence](WI-20261011-ATS-069-evidence-pack.md), [WI070 historical review](WI-20261011-ATS-070-evidence-pack.md) and [restore-only handoff](../../build/WI069-backup/restore-only-handoff-20261017.md): existing repository/snapshot preservation and bounded historical acceptance; no reexecution authority.
- [WI010 current observation](WI-20261024-ATS-010-evidence-pack.md): historical metadata validated/current fixture coverage NOT_ESTABLISHED, no newer-evidence search and no absence claim.
- [Own summary](../user/WI-20261024-ATS-013-summary.md): completed local preparation and concrete next-approval outline.

## Historical Initial Scope and Gates

The user approves local minimal preparation/review only. No server/provider/SSH/
DB/cloud API, credentials/private pointers, new backup/restore, app/login/audio,
payment, deletion or commit/push is permitted in this work. Preserve source/JAR,
frozen tools and all old evidence. No candidate hash, test PASS or readiness
result is invented before SE/PG completion.

Reuse the existing repository without initialization. Future backup must bind
its exact newly produced snapshot for an isolated restore and current fixture
proof, with independent cleanup. Do not overwrite the source database, delete
old snapshots, prune/forget or infer full disaster recovery. The final reviewed
candidate must define actual bounds and runtime-key/excluded coverage; planning
does not execute it.

Cost preparation is a verification method only: under later authorized access,
identify the actual backend/billing contract/included capacity/current usage;
combine applicable storage, transfer, request and execution charges with bounded
incremental usage, retention period and taxes. Present evidence, conservative
range/capacity limits and the user-approved monetary limit before execution.
Unknown terms mean HOLD, not an invented price, zero-cost claim or guaranteed
billing cap. No provider lookup or purchase occurs now.

MA notes the retained `check --read-data` reads the full existing repository,
not only the new snapshot. Estimate full-repository read plus restore, requests,
retained storage and local copies where billable/capacity-relevant. Retained
upload/download4096 settings are throughput limits, not total byte or cost caps.
Provider plan/account/prices are unchecked. The cost plan proposes a later
read-only confirmation scope, stopping on unknown terms; retention assumptions
never authorize automatic expiry/purge. Preserve existing snapshots/password
references and prepare a local adapter, never replay the first-capture path.

The current grant remains limited to 2026-10-24 KST. A different execution day
requires a separate exact grant-date proposal and explicit change approval;
there is no automatic extension. If approved, resulting changed state must be
covered by the later backup/restore evidence. Audio/data effects remain separate.

## Commands & Outputs

Read the generated handoff and current entry metadata, then relevant headings/
bounded historical statements in retained WI069/WI070/restore-only documents.
Updated only current index/REQ sections and own two-set files. No historical
operation was rerun; no private pointer, credential, provider or server was read.
Validation is restricted to new scopes/links, metadata, approval quote and whitespace.

Initial validation PASS: four owned documents/four new scopes, 22 relative links,
metadata/status/date, whitespace/terminal newline and verbatim Korean approval.
Preparation remains pending and native execution unapproved. No product/runtime
test or broad historical validation was performed.

Final validation PASS: four owned documents, six substantive changed scopes and
23 relative links; metadata/status/date, whitespace/terminal newline and final
candidate/additive-review pins. DocOps read final public README/proposal,
SE/PG evidence and receipts, and hashed only public candidate/receipt bytes.
No product test, native/private/provider access or historical document sweep ran.
Initial pending/cost notes remain historical; final new-snapshot-only scope and
0-KRW unknown-cost HOLD govern the current approval proposal.

## Risks / Rollback

Synthetic local PASS is not current repository, backup, restore or cleanup
acceptance. Old bounded success remains historical and must not be erased or
upgraded. Unknown actual cost/coverage or a changed target means no execution.
Documentation corrections are additive; DocOps performs no operational rollback.

## Follow-ups

The approved local chain is complete; no next native WI is auto-started.
REQ007 remains active. The Korean REQ and final summary separate later actual
cost/target confirmation, new backup/isolated restore and any date change.
Neither this local verdict nor future backup success automatically permits audio.

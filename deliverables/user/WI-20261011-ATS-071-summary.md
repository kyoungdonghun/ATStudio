---
version: 1.17
last_updated: 2026-10-18
project: ATS
owner: PG
category: work-summary
status: stable
dependencies:
  - path: ../agent/WI-20261011-ATS-071-handoff.md
    reason: Approved independent security review
  - path: ../agent/WI-20261011-ATS-071-evidence-pack.md
    reason: Initial design verdict and reproducible local evidence
---

# WI071 Security Review Summary

> Purpose: Track the final candidate review, concrete findings and correction retests.

## TL;DR

**Latest: MA reports native v3 listener-only PASS (352463).** The exact frozen
README proof followed CheckPublic183 (fb3f3b), v2 tools preservation (41dae7)
and v3 public staging (6d76e8). This is attributed MA evidence, not PG execution.
Fresh Release/private producer/app/DB/mail remain NOT_RUN. Human recovery login
and today's availability are pending; no public release or gate bypass is allowed.
No additional audit was performed. PG is on standby until an explicit MA event.

**Current: exact v3 runtime and operator ALLOW with existing gates.** Padding
regression12/12, operator Python54/54, PowerShell27/27, read-only proof6/6 and
CheckPublic183 PASS. No remaining actionable implementation finding. The initial
v3 indentation error was reported immediately, repaired and retested. Unknown
owners/malformed fields remain rejected even when trailing ASCII spaces follow.
[Final exact pins and action decision](../../build/WI071-review/pg-operators-v3-final-20261018.json).

MA may preserve only the fixed v2 tools, stage v3 tools, then run the reviewed
read-only listener proof. Fresh Release/private producer/trial still require
actual listener PASS, backup/restore, fresh target/provider and human readiness.
No automatic retry or public release. PG did not access secrets or the target.
[Incremental evidence and proof recipe](../agent/WI-20261011-ATS-071-evidence-pack.md#listener-padding-continuation-2026-10-18).

**Earlier checkpoint: v3 padding repair under incremental review.** MA's native listener-only
HOLD is independently reproduced locally:16 trailing ASCII display spaces cause
LISTENER_OWNER. The separate MA DNS/DHCP identity PASS is diagnostic only. Prior
v2 review is preserved; it does not establish native listener acceptance. Await
exact v3 bytes and tools-only cold preservation review before further MA action.
[Current continuation](../agent/WI-20261011-ATS-071-evidence-pack.md#listener-padding-continuation-2026-10-18).

## Previous V2 Decision

**2026-10-18: exact v2 operator ALLOW with existing gates; no remaining code blocker.**
Independent focused53 Python+27 PowerShell PASS, PG regressions8+4 PASS, and
documented listener-proof6 PASS. All final freezes/references are stable;
CheckPublic verifies183 files. Runtime17a4 approval is retained.
[Exact pins and action-specific decision](../../build/WI071-review/pg-operators-v2-final-20261018.json).

MA may perform exact cold preservation, then tools-only Public staging, then the
reviewed read-only listener proof. Only after listener PASS and fresh target,
provider/DNS, backup/restore and human/recovery gates may a fresh Release and one
producer invocation proceed. Existing bounded Start/Status/HumanReceipt/Finish
gates remain. No automatic retry, overwrite or public release is authorized.
[Full evidence and proof command](../agent/WI-20261011-ATS-071-evidence-pack.md#final-v2-operator-review-2026-10-18).
MA1ecc89 observations are attributed, not independently observed by PG. No native
or secret operation was performed. SE's100% aggregate coverage report identities
were checked; this turn reran focused tests, not the entire coverage suite.

## Historical Checkpoints

**Current: corrected runtime17a4ca56... ALLOW_NATIVE_EXACT_BYTES.** F6/F7 are repaired:
PG's8 Python and4 PowerShell checks pass. Independent runtime rerun214/214 PASS;
actual15-module coverage1702/1702 lines,314/314 branches,205/205 callable entries.
[Runtime-only ALLOW receipt](../../build/WI071-review/pg-final-listener-v2.json).
Cold/delivery/producer final operator freezes and the pre-Release read-only
listener proof command remain pending. No native supersession or fresh Release
is authorized by this runtime-only result; all existing gates remain.

Historical freeze6c70e477... was held for F6, with cold draft F7 also reproduced.
[Exact HOLD record](../../build/WI071-review/listener-v2-freeze-6c70-hold.json).
Scoped owner
parsing silently omits an extra dotted process name or malformed second PID.
Independent focused checks:6 passed,2 failed; both failures are one parsing finding.
Require complete owner-list parsing and reject unparsed entries, then rerun the
saved PG checks. Cold PC reply validation also accepts array-valued wi/retry;
require scalar strings before enum comparison. Two positive reply checks pass,
two array-rejection checks fail. SE's13 listener/cold/reuse methods independently
pass under native fences. Final freezes and correction retests remain pending.
MA3203f6 confirms expiry and cold state, not review completion or execution. MA reports
backup/restore PASS and the old producer's one-use remote invocation ended HOLD.
Read-only probes and source order identify a listener preflight failure before
private-stage writes; no retry is authorized. The new handoff limits repair to
strict scoped DNS/DHCP identities and fixed cold supersession with old artifacts
preserved. New bytes/freezes must be independently reviewed before MA use.
[Current review scope and attributed evidence](../agent/WI-20261011-ATS-071-evidence-pack.md#listener-v2-continuation-2026-10-17).
Older checkpoint statements below retain their historical scope, including the
formerly pending backup status. PG has not accessed private inputs or the target.

**Private producer: ALLOW_PRIVATE_PRODUCER_EXACT_BYTES; no new code blocker.**
Freeze `9c45df12efa2d045d8a1a126cd8fe25230997263fa7015d0de0fe274f2288dd5`
and ordered bundle `8e3affd51b426e7d733331054df7a13b7447a54cc914e867599a5990493bc134`
match all7 files and11 references. Independent18 Python tests plus8 PowerShell
cases PASS; original runtime/delivery bytes are unchanged.
[Exact verdict](../../build/WI071-review/pg-producer-final-20261017.json) and
[review evidence](../agent/WI-20261011-ATS-071-evidence-pack.md#exact-private-producer-review-2026-10-17).

PC selects only approved mail/profile; the server obtains base after live release
validation and feeds the unchanged stage entry. The producer adds no secret file;
existing stage still retains approved root0600 inputs.json. Use this instead of,
not in addition to, the old complete-input SecretStage call; no automatic retry.
SE's pinned111/111 lines,14/14 branches and7/7 entries were identity-checked, not
remeasured by PG. No SSH, target or protected inputs were accessed.

Actual private-stage remains HOLD until accepted backup AND isolated-restore PASS,
fresh live release and human/action-time gates. MA's6dc58f configuration booleans
and3f3706 profile-preparation update are attributed only; backup final-byte review
remains pending. This verdict grants no Start, Gmail or public-release acceptance.

**MA operational update: public staging PASS; auth remains HOLD.** MA reports
the cold-state precheck,183 staged public files, matching JAR and app count0
(`c29205`, `d32264`, `1f8599`). Human availability and recovery-console login are
reported. Encrypted backup/file restore PASS does not yet close DB
format-equivalence validation under SE069/PG070. Release, SecretStage and Start
remain NOT_RUN. PG recorded this reduced evidence without reading the protected
receipt or accessing the target; prior exact-byte verdicts remain unchanged.

**PC delivery: exact public staging ALLOW, private execution still gated.**
Delivery freeze `42a1743628cb2103eae3a15112c0599691526cd13150dc492e942d9c318afcfa`
passed independent review:34 Python tests and13 PowerShell cases, no failures.
All183 public files and643 current source inputs match; runtime remains unchanged.
[Action-specific verdict](../../build/WI071-review/pg-delivery-final-20261017.json).

MA may stage only the fixed public trees at a freshly verified cold-state boundary,
never concurrently with backup. Human-ready is not needed for that public-only
step. Release, SecretStage and Start retain execution HOLD until actual backup AND
isolated-restore PASS and human/action-time gates. Status/Finish are reviewed for
MA-authorized observation/closeout; no retry, overwrite or fabricated witness is
allowed. Native/SSH/private-input actions were not run by PG.

The sole historical reference drift is MA's approved WI067 handoff continuation;
all16 original component files and the38-file runtime are unchanged. Old hashes
and verifiers were preserved. Full pins, tests and limitations are in the
[PC delivery review](../agent/WI-20261011-ATS-071-evidence-pack.md#exact-pc-delivery-review-2026-10-17).

**2026-10-17: ALLOW_NATIVE_EXACT_BYTES, bounded independent code review.**
SE repaired all five concrete findings: receipt validation, startup resource
monitoring, cross-filesystem retention, independent cleanup dispatch and the
new/old management-IP distinction. PG retests pass. No concrete code blocker
remains in the reviewed state. Exact full-freeze SHA-256:
`e67ca4a80ebee957bc214d59d640e0a1f3bb3f8b003080be7551dec1e3b01de6`.
[Machine-readable verdict](../../build/WI071-review/pg-final-20261017.json).

Initial final-runtime PG rerun:170 tests,170 passed,0 errors/skips. Independent checks confirm
pre-grant monitoring, startup-fault stop and preservation of the original baseline.
The38-file full freeze, original16 components and104 references match before/after
tests. No native/network/credential access.
At12:10:04+09:00 on2026-10-17, PG rechecked WI068's JAR,643 source,336 frontend,
143 SPA files and WI067's artifact projection successfully. This verifies local
bytes only; no current target or mail result is claimed.

Historical DESIGN_ALLOW for local implementation remains valid under its
[conditions](../../build/WI071-review/design-review.md). MA owns fresh IP/SSH and
backup/restore evidence. Human password and actual legal consent remain manual.
The approved sequence is backup/isolated restore before Gmail, with no public
release. Exact-byte review is complete; MA owns operational progression.

### Quality Continuation

PG independently verified the separate quality freeze
`1b3ae69d0fd386f806205e8c9f5bd20d3b3b18e5e86c5c2a3572529f5f1deb34`
and reran208/208 tests successfully at12:33:44+09:00. All15 operational modules
match SE's1666/1666 lines,312/312 branches and204/204 callable-entry metrics, with
no excluded runtime lines. The former68.99% gap is closed for this local scope.
The approved38-file runtime is unchanged; existing exact-byte ALLOW remains valid.
[Independent quality evidence](../../build/WI071-review/quality-20261017-02/review.json).

The added tests make real safety assertions under mocked OS/service boundaries;
100% coverage is not native, exhaustive-condition or race proof. Static inspection
confirms WI067 uses existing `mysql.service` without custom MySQL log/socket/PID/
datadir paths or AppArmor changes. Effective target compatibility remains untested.

MA reports WI069 startup failure on its custom error-log path and repair in
progress. Backup/isolated-restore PASS remains absent, so auth stays blocked.
No remote/secrets access occurred. Any runtime frozen-file change requires a new
freeze and PG recheck; no target/backup/Gmail/public acceptance is granted.

### MySQL Login-File Follow-Up

PG's12:43:16+09:00 targeted review found no additional code blocker. Although
`--no-defaults` alone permits login-file processing, WI067 replaces the entire
child environment and excludes HOME and MYSQL_TEST_LOGIN_FILE. Official MySQL
8.0.46 source selects no login file when both are absent; startup does not restore
HOME. Two supplemental mocked checks PASS, with runtime and quality bytes unchanged.
[Targeted evidence and official references](../agent/WI-20261011-ATS-071-evidence-pack.md#targeted-mysql-login-file-review-2026-10-17).
No unsupported flag, candidate edit or new freeze is needed for this question.
No actual login file, credential or target was accessed. Public-source review and
local mocks are not deployed-binary proof; backup/isolated-restore remains the
required gate before auth.

## Verification

This section preserves the initial2026-10-11 component checkpoint; the current
findings and counts above supersede its implementation-status assumptions.

PG ran82 local synthetic tests:50 existing components,15 binding scaffolds and17
independent tests; all passed with no errors/skips. Before/after reviewed bytes
were stable. The original16 frozen files and104 reference pins remain unchanged.

The new binding directory is not in the original freeze, so the directory-wide
verifier correctly HOLDs. Its native entry is deliberately locked and actual
owned nft/supervision/DB-reader/restoration/resource-monitor adapters remain
unfinished. These are implementation/review gates, not human-password defects.

## Next Gate

MA verifies backup/isolated restore before Gmail, WI068 artifact
pins and current target/access evidence. User-created password and real legal
consent remain personal action-time steps; no automation substitutes for them.
HTTP success cannot establish Gmail delivery because the app absorbs mail errors.

2026-10-11, PG, Windows local source and synthetic review only. No real secrets,
native/network/browser operations, mail/account writes, app/DB start, installation,
provider changes, delegation, commit or push. WI060 historical web PASS is retained;
backup/runtime/mail results and current target state were not independently tested.
This paragraph preserves the historical verification boundary. The final local
review above completes the assigned exact-byte review, not native acceptance or
REQ007. The local coverage gate is now satisfied; operational gates still apply.

## Related Documents

- [Evidence Pack](../agent/WI-20261011-ATS-071-evidence-pack.md)
- [Local test receipt](../../build/WI071-review/local-test-evidence.json)
- [WI071 handoff](../agent/WI-20261011-ATS-071-handoff.md)

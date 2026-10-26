---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: DocOps
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-028-handoff.md
    reason: Approved evidence integration and supplied MA observations
  - path: ../user/REQ-20261011-ATS-003.md
    reason: Active requirement and exhausted one-attempt approval
  - path: WI-20261011-ATS-027-evidence-pack.md
    reason: Exact-bundle conditional review gate
---

# Evidence Pack: WI-20261011-ATS-028

> Purpose: Integrate MA's private startup success, automated cleanup failure and separate postcheck without changing historical evidence.

## Summary and Scope

**First bounded private backend startup VERIFIED; cleanup FAILED; REQ003
approved/active/HOLD.** Native owner MA, observation date2026-10-11 KST.
DocOps document update2026-10-11, Windows/PowerShell, same local main HEAD
58b1011dad796017e47dbcc82a4225ca449bcdf6 with existing dirty work retained.
DocOps performed no native verification or runtime test.

- [x] Latest checkpoint precedes preserved REQ002 history; current REQ not closed.
- [x] Startup/GET result, cleanup failure and later bounded postcheck separated.
- [x] Exact JAR/bundle/preflight pointers and historical selection mistake recorded.
- [x] Original failed attempts, frozen runtime/README/reports and canonical manifest preserved.
- [x] Two deliverable sets and sanitized machine receipt produced; no further approved WI blocked by WI028.

## Drift Report and Fix

| Finding | Impact | Scoped change |
|---|---|---|
| Current entry led with no new-JAR startup | High: obsolete operational judgment | Add dated checkpoint only, index3.21 to3.22 |
| REQ progress/DoD still pending startup | High: conflates verified startup with unresolved cleanup | Incremental Korean progress/DoD, active/HOLD, cleanup unchecked |
| Cleanup generic failure lacks step evidence | High: cannot declare a false positive or full restoration | Preserve FAILED and separate MA postcheck; no diagnosis invented |
| WI026/027 handoffs lacked required metadata | Low: incomplete document envelope | YAML frontmatter only, contract bodies unchanged |

No category document count changes: new outputs are deliverables, linked from
current entry and REQ. No Tier0 reorder, historical report rewrite or duplicate
architecture document. The receipt is machine evidence; the checkpoint is a
projection with pointers, not a second runtime authority.

## Native Evidence and Identity

The [sanitized receipt](WI-20261011-ATS-028-native-result.json) transcribes the
MA handoff and later precision correction. Its exact nativeReceipt object is
preserved:13:20:33.780 KST, SSH exit2/stderr empty, app_start VERIFIED,
status HOLD, cleanup FAILED/CLEANUP_VERIFICATION_FAILED. MA uploaded four
public files13:19:49, staged them13:19:57, created fresh approval13:20:16 and
invoked block3 once. No automatic retry; fresh approval exhausted.

| Identity / evidence | Exact value or source |
|---|---|
| MA source check13:03:30 |641/641 unchanged, dirty main/58b1011dad796017e47dbcc82a4225ca449bcdf6; no rebuild |
| New JAR |70306351 bytes;01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb; [WI020 manifest](WI-20261011-ATS-020-artifact.json) |
| WI026 bundle |7136e749de6b7dd2ae6ecbf2585a3dc1ddb0fd083ef81ab48f96e2f82aa44f05; [freeze](../../build/WI026-private-start/freeze.json) |
| Frozen README |e2305e742db1af5da6a272c31d3b641fc2386e96053f7511fb99035c23c52360; [recipe/block pins](../../build/WI026-private-start/recipe-pins.json) |
| Frozen runtime |c61f590efabae4b94196cfde13b1773c67becb04bdfff8b1e954a022a3f08274 |
| Current preflight |6b1d7ed691b37043af778598decc46bf73e12b7c08f47b7d0f1128fce44de383; [exact helper](../../build/WI014-final-xbind-preflight-80d4a7.py) |

DocOps locally rehashed runtime, README and current preflight; other identities
are attributed to MA/frozen manifests, not a fresh641-input/JAR audit. At13:07
MA mistakenly used the older fbd216-prefix preflight, causing mysql_config false.
No host config was changed to fit it. The already-approved current preflight at
13:20:09 returned23/24 true; retained historical approval made only
new_approval_absent false. This is not24/24 PASS or deletion authority.

WI024's12:56:16 PREPARE/PATH_ANCESTRY HOLD remains: app NOT_RUN, cleanup PASS,
strict rejection of /var/log root:syslog0775. WI026 changed only log location
and fresh names; existing parent permissions unchanged. The
[21-file preservation record](../../build/WI026-private-start/source-preservation.json)
and WI026/027 reports retain that provenance. Owner42+8+3+4 and independent
same suites+6 are overlapping local results, not63 distinct native proofs.
DocOps did not rerun them or edit frozen preparation-time NOT_RUN statements.

## Result Boundaries

On-host proof reported TCP/UDP positive controls2 each, negative deliveries0
and BPF attachment checks. Internet/IPv6 delivery proof NOT_RUN. Readiness and
strict-audit markers preceded the two bounded GETs. Public capabilities confirm
password login/test-user/email-verification/password-reset/socialLogin flags OFF,
not the background master flag or scheduler binding. Background OFF basis is
exact unit/overlay/source review plus no named worker threads; no app children
and unchanged policy/BPF were also observed. Equal43-table before/after counts
(subscriptions6, other tables0) do not prove no UPDATE/transient DML or complete
SQL auditing. No intentional business DB operation was reported by MA.

Immediately afterward, app masked/failed had MainPID0/ControlPID0 and
ExecMainCode1/ExecMainStatus143;13:20:23 start,13:20:32 end. MySQL was
inactive/dead/masked/PID0, original app unit absent, Nginx active/enabled with
same PID11156 and Certbot timer active/enabled. At13:22:09 MA independently
found no java/ffmpeg/mysqld/atstudio processes, pending jobs or listed app/DB/
receiver ports; exact app/MySQL /dev/null masks, unchanged9 listener rows and
three public TLS pins. This bounded recovery-state evidence does not replace
the original cleanup failure or establish full restoration PASS.

PG's supplied read-only analysis: masked/failed is explicitly allowed by cleanup,
so that state alone cannot explain failure. Earlier command return codes,
timeouts, transient checks or final baseline comparison may have failed;
per-step codes were not retained. Root cause NOT_DETERMINED, false positive
NOT_PROVEN. No stop/start repetition to guess. At13:23:05 MA removed the owned
key, observed no agent identities and restored Stopped/Disabled; unlock process
already gone, no other terminal killed. No private logs/env bodies printed.

## Validation and Reproduction

Applied validate-docs and create-wi-evidence-pack skills after confirming the
actual WI028 handoff exists. Validation uses the existing DocValidator, with
only its project-root Markdown enumeration restricted to all current docs/
files plus REQ003, WI026/027/028 handoffs and WI028 evidence/summary. Its
document-index check still scans all docs/. It checks Tier0 existence, file
links, supported IDs and index coverage; it does not validate link anchors or
every historical/build/other deliverable. Baseline226 files passed with0 errors,
0 warnings and228 supported IDs. Final228-file validation also passed with0
errors,0 warnings and228 supported IDs (exit0, receipt b24b4c). No preexisting
unrelated findings occurred in this bounded scan; no whole-repository claim.

Additional direct checks cover seven output paths: required Markdown metadata,
dependency targets, whitespace/EOF and JSON parsing/critical outcome fields.
Scoped git diff --check complements these because untracked deliverables are
not covered by Git diff alone. All7 files/6 Markdown envelopes/10 dependency
targets passed direct checks and JSON outcome assertions (exit0,0abb83).
Scoped git diff --check passed (exit0,67148b); Git emitted only its existing
index CRLF-to-LF normalization notice. No application tests, builds or native
commands. A final rerun after these result lines confirmed the same outcomes.

## Risks, Rollback and Chain

Only docs/index.md, REQ003, two handoff metadata envelopes and three WI028
outputs changed. Withhold/supersede this checkpoint if contrary evidence arrives;
do not rewrite old receipts, delete artifacts or change runtime/canonical flags.
Canonical app_runnable=false and production HOLD remain. No full-site/frontend/
auth/media/load/mail/backup or Internet/IPv6 proof. No source/config edits,
server/SSH/browser/secret access, installation, deletion, commit/push or memory
writes by DocOps.

**WI028 integration complete; Blocks none in current approval. REQ003 remains
active/HOLD.** Cleanup diagnostic hardening or another native attempt requires
new user approval. No successor execution was created or performed.

## Reference Documents

Retained injected Tier0 STD-001/004/002/005, DocOps persona and security policy;
then WI028 task/MA snapshot and precision correction. Relevant WI022-027
handoffs/evidence, REQ003, docs/index entry, WI020 manifest metadata, WI026
freeze/recipe/preservation and frozen helper/README excerpts were read.
Skill gate and injection order retained; no broad architecture reread.

## Related Documents

- [WI028 handoff](WI-20261011-ATS-028-handoff.md): Authority and supplied native observations.
- [REQ003](../user/REQ-20261011-ATS-003.md): Active/HOLD and remaining cleanup criterion.
- [Current entry](../../docs/index.md#current-decision-entry): Superseding checkpoint.
- [User summary](../user/WI-20261011-ATS-028-summary.md): Compact result and approval boundary.
- [WI027](WI-20261011-ATS-027-evidence-pack.md): Conditional exact-byte review.

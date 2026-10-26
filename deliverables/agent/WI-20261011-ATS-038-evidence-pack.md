---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: docops
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-038-handoff.md
    reason: Approved documentation-only boundary
  - path: WI-20261011-ATS-035-evidence-pack.md
    reason: Frozen local candidate and tests
  - path: WI-20261011-ATS-036-evidence-pack.md
    reason: Source/artifact identity and bounded read contract
  - path: WI-20261011-ATS-037-evidence-pack.md
    reason: Independent exact-byte review
  - path: WI-20261011-ATS-034-evidence-pack.md
    reason: Preserved earlier bounded native success
---

# Evidence Pack: WI-20261011-ATS-038

> Purpose: Integrate the actual restricted-web HOLD, access handoff and separately approved repair chain.

## Summary and Scope

**TL;DR: MA's consumed WI035 attempt ended PREPARE / WEB_SERVICE / HOLD,
app NOT_RUN at 2026-10-11 15:31:09 KST. REQ006 stays active/HOLD.**

- [x] Native final receipt preserved exactly; 6 PASS / 9 NOT_APPLICABLE, total15.
- [x] Local identity/tests, conditional PG gate, native outcome and browser NOT_RUN separated.
- [x] Actual access state and new approval recorded without claiming full restoration.
- [x] Prior history preserved; new checkpoint, REQ update and two-set reports/receipt only.
- [x] Follow-up belongs to WI039/040/MA/WI041; no repair or second trial performed here.

DocOps did no source/runtime editing, target access, browser/network/service action,
application tests, installation, deletion, commit/push or memory write. All target,
HTTP and management-console observations below are MA-supplied, not DocOps checks.

## Actual Evidence

The [sanitized receipt](WI-20261011-ATS-038-native-receipt.json) preserves the exact
S5 native object. Supporting observations are reduced from the dispatch/followups;
no private address, environment body, key identity, VNC token or raw log is saved.
Exact seconds are used only when supplied; management-console observations remain
approximate. No durable screenshot file pointer was supplied or invented.

| 2026-10-11 KST | MA observation |
|---|---|
| Approximately15:28 | Provider portal had expired; user reauthenticated. Fresh unchanged management rules and usable root recovery observed. Initial address comparison corrected by verified NAT identity, not a different-host finding. |
| 15:28:35 | Fresh local identity matched641 backend inputs,336 frontend inputs and143 dist files, unchanged JAR/SPA pins. |
| Before15:30:48 | L1 upload153 PASS; S1 root stage10 PASS; S2 new-namespace preflight PASS; S3 fresh approval once. No exact block timestamps supplied. |
| 15:30:48 | S4 dispatched once. Dispatch is not startup acceptance. |
| 15:31:09 | Authoritative S5: HOLD/PREPARE/WEB_SERVICE, app NOT_RUN, cleanup PASS with6 PASS/9 NOT_APPLICABLE. Earlier7/8 aggregation was erroneous. |
| 15:32:12 | Supervisor failed/PID0; MySQL masked/inactive/PID0; new app not-found/PID0; original Nginx active/PID11156. Baseline, web release and temporary MySQL/Nginx overlays absent; original three TLS pins matched. |
| 15:32:49 | With and without `--all`, supervisor omitted ExecReload/ExecStartPre/ExecStop; masked MySQL omitted ExecReload/ExecStart/ExecStartPre/ExecStop and had empty Type. |
| 15:39:42 | Exact pinned old preflight22/23 true; only historical approval-absence false. No app/DB ports, named workers/jobs; MySQL masked/inactive; original TLS/no-proxy, Nginx and Certbot state verified. Not overall preflight PASS. |
| 15:39:43 | HttpClient normal TLS, no redirect/cookies: HTTPS `/ready.txt`200 with exact quarantine body. Original configuration remained in place; no web restoration cycle was exercised. |
| 15:40:09 | Owned key removed using System32 ssh-add; list rc1 confirmed no identities. |
| 15:43:54 / approximately15:44 | Agent Running/Manual observed; restore Start-Process/UAC cancelled by user, process exit1. Not still running and not restored Stopped/Disabled. |
| Approximately15:46 | After user confirmed logout, MA saw root-recovery tty login prompt. Console retained for handoff; no all-provider/browser-session logout claim. |

No baseline or app/web changes occurred before HOLD. DB NOT_STARTED, OPEN absent,
L2/L3 HTTP assertions, application browser navigation and S6/S7 NOT_RUN. Native
containment proof was not reached; the receipt's ON_HOST_ONLY scope is not a PASS.
No business writes, mail/payment calls, public exposure or additional paid product
were reported. Existing server billing continues.

MA's causal comparison identifies Session.service requiring omitted optional Exec
keys before prepare; a separate masked-MySQL Type predicate is a latent blocker.
SE and PG subsequently concurred without edits. `--all` alone is not a fix. This
is distinct from WI037-P2-01's mocked intermediate restoration status defect;
the final native HOLD is not that intermediate receipt and cannot be dismissed.

## Local Identity and Review

| Object | SHA-256 |
|---|---|
| JAR,70306351 bytes | `01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb` |
| SPA143-file set,1194351 bytes | `43a0384c5f48d100e4af6b20d989a2186b34e6900cf4e69c94b010e9671c8647` |
| WI035 ten-file runtime bundle | `35ff347e1d0e2b0cad930ce1431b073d08fea7d2a48631779360766424760ce5` |
| WI03523-file review set | `feb373fe3308a8dd06da2bb7e149bbdc255f527750744a987830fccf8b1de063` |
| runtime.py | `8ca9012729f3f89a74dac8e325321aab3f5418e727efd827a394e53151eebffb` |
| web_session.py | `b7b70dece75a1ceebe9126ee659a68b75129c7cf07a6351294f86d79e0b291b7` |
| README | `91801af1f56c4f67d5be1c11e28ec8b04f1bb91a0db5d4d1d20015fdc6ccd74a` |

The SPA aggregate is a sorted path/TAB/uppercase SHA256/LF file-set hash, not an
archive hash. [WI036 identity](../../build/WI036-web-contract/identity-result.json),
[WI035 freeze](../../build/WI035-restricted-web-check/freeze.json) and
[recipe pins](../../build/WI035-restricted-web-check/recipe-pins.json) retain full
algorithms and pins. No rebuild or new DocOps application/JAR audit.

| Owner/environment | Actual local scope |
|---|---|
| SE, Windows/PowerShell, freeze15:07:31 | Python85/85 PASS,0.347s; PowerShell8/8 synthetic assertions;15 in-memory compile units and4 PowerShell blocks parsed. No target/network/browser execution. |
| QA-INTEG, Windows/PowerShell | PASS_LOCAL_IDENTITY_ONLY;641/336/143 counts and4 exclusions separately checked; six GET paths/17 query sets. No regression suite, build, HTTP or browser test. |
| PG, Windows, final pins15:22:57 | CONDITIONAL ALLOW for exact one REQ006 attempt. Independent85/85 regressions,0.311s;11/11 focused tests,0.167s, including13 fault subcases;8/8 synthetic PowerShell assertions and4 parsed blocks. |

Counts overlap and must not be summed into unique tests or native proofs. See
[SE local receipt](../../build/WI035-restricted-web-check/local-receipt.json) and
[PG test receipt](../../build/WI037-restricted-web-review/test-receipt.json).
PG's earlier no-blocking-defect verdict is preserved as review-time evidence;
the later native parser failure is recorded here, not rewritten into that report.

## Current Approval, Risks and Chain

The user explicitly approved temporary SSH use and supplied unlock/root-recovery
handoffs for this trial. After HOLD the user separately approved repair, review
and one fresh conditional trial. REQ006 records the verbatim approval and narrow
amendment: optional Exec parsing and stage-aware masked-MySQL checks, new namespace,
freeze/tests/PG review, at most30min, unchanged privacy/no-business-write/no-external
boundaries. Original WI035 stays frozen. New attempt NOT_STARTED; current temporary
agent continuation is approved, not final access restoration. MA owns fresh key/
target/recovery checks, all later native work and eventual Stopped/Disabled cleanup.

WI038's consumed-attempt reporting is complete; **WI039 SE -> WI040 PG -> MA
fresh gated attempt -> WI041 DocOps** is the new chain. No additional execution
is authorized by the old WI035 claim. The web goal remains unmet.

F036-01 popular-album request/latest ordering and F036-02 Tag raw-array standard
versus actual dataList contract remain deferred. They are not new runtime
regressions or included repairs. No login, populated catalog, playback/download,
mail/payment, capacity/load, pristine browser, complete traffic capture or public
release acceptance. WI034 BOUNDED_PASS remains dated scoped evidence; WI026's
cleanup FAILED/HOLD and root cause NOT_DETERMINED are unchanged.

## Changes and Documentation Validation

Only `docs/index.md`, REQ006 and these WI038 evidence/summary/receipt files changed.
Historical files and unrelated changes are preserved. Frozen handoff metadata
is outside this write scope; WI038 handoff has no YAML header and is not repaired.
No new category document was added, so only the current index checkpoint changes.

Local DocOps verification on 2026-10-11:

- `python -I -B -`: load the existing validate-docs script via `runpy`; bound
  its root Markdown enumeration to `docs/**/*.md`, REQ005/006, WI034-038 Evidence
  Packs, WI035-038 handoffs and the WI038 summary. Run its unchanged validator:
  234 files,236 supported IDs,zero errors/warnings,exit0 (`129e5b`). No full-repo,
  build-history, external-link or anchor validation is claimed.
- PowerShell direct checks: four Markdown required metadata/dependency sets,
  five whitespace/final-newline checks, exact native-object comparison including
  all15 ordered rows and derived6/9 counts, plus an address/token-locator scan
  of the three new reports: PASS,exit0 (`d00895`). This includes untracked files.
  The locator scan is a bounded reporting check, not general secret-detection proof.
- Scoped `git diff --check --` over the five output paths: exit0. Only the
  existing index CRLF-to-LF normalization notice appeared. An initial inline
  PowerShell validation command had a parenthesis parse error (`42e7c8`), before
  execution; corrected and rerun successfully, with no file/runtime effects.
- Before/after index comparison preserved all previous checkpoints exactly.
  Nine public SHA-256 checks matched: REQ005, WI034 receipt, WI035/036/037
  Evidence Packs, WI035 freeze/recipe-pins/runtime/web_session. No JAR or source
  re-audit, runtime import or test execution was performed by DocOps.

The frozen WI038 handoff's missing YAML header is the separately observed
pre-existing metadata limitation, not covered by the validator's link/ID result;
it remains unchanged outside the allowed five-file patch.

Rollback is a separately authorized reversal of this documentation patch only,
never deletion/rewriting of native records or an operational retry. No operational
rollback was performed by DocOps.

## Reference Documents and Related Documents

Retained ordered Tier0: STD-001 core-principles, STD-004 documentation-standards,
STD-002 development-standards, STD-005 glossary. Tier1: DocOps role and security
reporting/privacy policy. Applied create-wi-evidence-pack and validate-docs skills
to the approved [WI038 handoff](WI-20261011-ATS-038-handoff.md); no subdelegation.

- [REQ006](../user/REQ-20261011-ATS-006.md): active/HOLD and repair amendment.
- [WI035](WI-20261011-ATS-035-evidence-pack.md): preserved owner evidence.
- [WI036](WI-20261011-ATS-036-evidence-pack.md): identity and deferred discrepancies.
- [WI037](WI-20261011-ATS-037-evidence-pack.md): exact conditional gate and limits.
- [WI034](WI-20261011-ATS-034-evidence-pack.md): prior successful bounded native run.
- [WI034 receipt](WI-20261011-ATS-034-native-receipt.json): unchanged historical result.
- [WI038 summary](../user/WI-20261011-ATS-038-summary.md): concise user report.

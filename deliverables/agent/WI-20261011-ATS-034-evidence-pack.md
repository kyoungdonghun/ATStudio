---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: docops
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-034-handoff.md
    reason: Approved documentation-only boundary
  - path: WI-20261011-ATS-032-evidence-pack.md
    reason: Frozen candidate and owner local results
  - path: WI-20261011-ATS-033-evidence-pack.md
    reason: Independent exact-byte review
  - path: WI-20261011-ATS-028-evidence-pack.md
    reason: Preserved historical failure
---

# Evidence Pack: WI-20261011-ATS-034

> Purpose: Integrate the MA-owned bounded diagnostic outcome, separate postcheck and access cleanup.

## Summary and Scope

**TL;DR: REQ005's single native trial returned BOUNDED_PASS at 2026-10-11
14:09:01 KST: app VERIFIED; cleanup 23/23 PASS. Scoped chain complete.**

- [x] New current checkpoint; all previous checkpoints and receipts retained.
- [x] Owner/PG local tests, MA native result, independent postcheck and access cleanup separated.
- [x] Exact native JSON preserved; supporting observations reduced without private addresses or logs.
- [x] REQ005 closeout; REQ003 historical active/HOLD unchanged, old cause NOT_DETERMINED.
- [x] Two-set reports and scoped documentation checks, including untracked outputs.

DocOps made no target calls, ran no application/runtime tests, and changed no
source, candidate, JAR, manifest, old receipt or handoff. No commit/push, deletion,
installation or memory write. No additional native trial is authorized.

## Evidence and Chronology

[Public receipt](WI-20261011-ATS-034-native-receipt.json) preserves the supplied
native result object exactly. Supporting sections are MA-provided projections;
private listener addresses, key paths and raw console contents are omitted.
Verification date is 2026-10-11; times below are KST, not document edit timestamps.

| Time | Owner and evidence |
|---|---|
| 13:58:31 | MA: 641/641 source inputs matched; original 70,306,351-byte JAR unchanged. |
| 14:08:12 | MA preflight `c3e32e`: 22/23, exit2/HOLD; only retained historical approval-absence false. Not blanket PASS. |
| 14:08:20 | MA baseline `a0a4f0`: all nine fresh WI032 targets absent; source JAR pin matched. |
| 14:08:26 / 31 / 41 | MA upload `a14cb4`, root stage `cc84c3`, approval `9431a5`: four public files, then fresh approval. |
| 14:09:01 | MA single runtime `fb37a5`: exit0, empty stderr, BOUNDED_PASS, 23 unique cleanup steps PASS, two GETs. |
| 14:09:35 | MA separate read-only postcheck `9c96dd`: both masks/PID0; no workers/jobs; normalized listener endpoints and three TLS/two JAR pins matched. |
| 14:10:24 | MA `4d3ff7`, `b48ffe`, final `f2d3f4`: owned key removed, no identities, SSH agent Stopped/Disabled. |
| Approximately 14:13 | User confirmed logout; MA screenshot showed Ubuntu tty1 login prompt, no root shell. Owned recovery tab closed; provider main console retained as handoff. No exact seconds supplied. |

Exit metadata `ab1aac`: app ran 14:08:51-14:08:59; MainPID/ControlPID0,
masked/failed, Result exit-code, ExecMainCode1/ExecMainStatus143. Failed is an
accepted stopped state in the cleanup predicate, not proof of graceful shutdown.
MySQL was inactive/dead/masked/PID0. Historical WI026 masked/failed state was retained.
An initial console input malfunction left logout unconfirmed; the final user/MA
confirmation supersedes that pending state. This does not claim all browser
sessions closed. MA's later 12/12 in-memory assertions rechecked these existing
receipts, including normalized endpoint equality excluding PID/fd, without a server call.

## Identities and Local Review

| Artifact | SHA-256 |
|---|---|
| Original JAR, 70,306,351 bytes (MA local/remote checks) | `01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb` |
| WI032 runtime, 49,841 bytes | `189342bef8ac90496b59efe754efbc4f846a582efd409f56e19dc8adaa16857f` |
| Four-file bundle | `11db69c6d82da14083864ea7e14fa239f0823e2665b5d1c3048c70edf9271dc4` |
| Frozen README | `1a70aea2249fd9a4b3783efd68b0165977d307be0b98aa8e4ddd1ebf65c0e284` |
| Preserved WI028 receipt | `4bca13c97a12885e55037c3e81bcefe26ebe6b4bb16473255ec06c1e746bfdfa` |

[Freeze](../../build/WI032-private-diagnostic-start/freeze.json) and
[recipe pins](../../build/WI032-private-diagnostic-start/recipe-pins.json) identify
all bytes and three command blocks; [PG results](../../build/WI033-review/test-results.json)
record their independent checks. No registered identity is replaced.

SE: 88/88 mocks PASS, 0.395s; PG: rerun 88/88 PASS, 0.366s, plus focused 7/7
PASS, 0.032s. Both Windows/Python3.14.3 on 2026-10-11. PG's verdict was
CONDITIONAL_ALLOW_EXACT_ONE_ATTEMPT. The runs overlap and are not 95 native proofs.
SE/PG recorded 51 historical file matches and the original receipt match.
The WI032 owner report is now available and read by DocOps. PG's earlier report
correctly states it was unavailable at that earlier read; that history is unchanged.

## Documentation Changes and Validation

Only five paths changed: `docs/index.md` (new leading checkpoint/version),
`deliverables/user/REQ-20261011-ATS-005.md` (incremental scoped closeout), this
Evidence Pack, the [summary](../user/WI-20261011-ATS-034-summary.md) and public receipt.
Existing handoff metadata needed no repair. Drift resolution is limited to the
new current checkpoint; no historical statement is rewritten as current acceptance.

DocOps validation on 2026-10-11, local PowerShell/Python only:

- `python -I -B -` loaded the existing validate-docs script via `runpy` and
  bounded its root Markdown inventory to `docs/**/*.md` plus REQ003/005,
  WI028/031-034 Evidence Packs, WI032-034 handoffs and the WI034 summary.
  Result: 233 files, 236 supported traceability IDs, zero errors/warnings,
  exit0 (`2f5d9b`). This excludes broad build history, other deliverables,
  external links and anchor validation; it is not a whole-repository audit.
- Direct PowerShell checks: required metadata and dependency paths 4/4,
  whitespace/final newline 5/5, parsed native object exactly equal to the
  supplied result including 23 unique ordered cleanup steps; exit0 (`72a122`).
  These checks include new untracked files, which Git diff alone would omit.
- Scoped `git diff --check --` over the five output paths: exit0 (`bf0aec`).
  Git emitted only its existing `docs/index.md` CRLF-to-LF normalization notice.
- In-memory before/after comparison preserved every earlier index checkpoint.
  SHA-256 rechecks matched unchanged REQ003 (`2716c4bd46d2008ea285e3fa4ca711435dfa39cb078f77fcdc8184d7940ead7d`),
  WI028 receipt, runtime and README. No JAR re-audit or candidate import/execution.
- MA additionally reviewed the saved documents and independently canonical-compared
  saved `native.result` against actual `fb37a5`: exact equality TRUE; console
  logout CONFIRMED. This was local receipt review, not another target execution.

No unrelated finding was returned by this bounded scan. No application tests,
server checks or candidate execution were performed by DocOps.

## Limits, Rollback and Chain

The prior WI026 cleanup FAILED/HOLD and cause NOT_DETERMINED remain intact. The
failure did not reproduce; no causal fix or retrospective PASS is inferred.
REQ003 remains unchanged. This new REQ005 completion covers only its approved
single trial, cleanup and evidence, not production readiness; canonical
app_runnable false/production HOLD are not changed.

Proof is on-host IPv4/BPF only; Internet/IPv6 delivery proof NOT_RUN. The public
capabilities GET verifies authentication flags OFF, not scheduler binding.
Background OFF rests on unit/overlay/source review plus no named worker threads.
Matched table counts are bounded observations, not complete SQL audit or proof
against updates/transient DML; no intentional business DB operation was performed.
No full-site/frontend/auth/media/load/capacity/mail/backup acceptance is claimed.
No new purchase; existing server billing continues.

Documentation rollback would be a separately authorized reversal of this WI's
five-path patch only, preserving all old records. No operational rollback or
additional run was performed by DocOps. Chain: WI032 -> WI033 -> MA single trial
and cleanup -> WI034; no successor within the exhausted approval.

## Reference Documents and Related Documents

Retained Tier0 injection: core-principles STD-001, documentation-standards STD-004,
development-standards STD-002, glossary STD-005; Tier1 security-policy and DocOps
role. Applied Tier2 create-wi-evidence-pack and validate-docs skills to the
approved [WI034 handoff](WI-20261011-ATS-034-handoff.md); no subdelegation.

- [REQ005](../user/REQ-20261011-ATS-005.md): approved scoped closeout.
- [WI032 owner evidence](WI-20261011-ATS-032-evidence-pack.md): current owner report.
- [WI033 PG evidence](WI-20261011-ATS-033-evidence-pack.md): review-time gate and limits.
- [WI028 historical evidence](WI-20261011-ATS-028-evidence-pack.md): preserved failure.
- [WI031 local-only checkpoint](WI-20261011-ATS-031-evidence-pack.md): preceding diagnostics readiness.

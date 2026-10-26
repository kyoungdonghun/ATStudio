---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: evidence-pack
status: stable
related_wi: WI-20261011-ATS-002
dependencies:
  - path: WI-20261011-ATS-002-handoff.md
    reason: Existing skill-generated review assignment and input pointers
  - path: WI-20261011-ATS-001-evidence-pack.md
    reason: Candidate identities and preparation evidence
  - path: ../user/REQ-20261010-ATS-010.md
    reason: Approved scope and separate native consent gates
---

# Evidence Pack: WI-20261011-ATS-002

## Summary

TL;DR: **PASS for the exact local candidate; zero concrete blocking findings.**
Independent PG test run: 35/35 PASS, 0.281s, exit 0. This review releases the
independent-review prerequisite for MA's approved gated sequence, including the
explicit apt confirmation clarification below. Native success, CA consent,
application operation and REQ closure are not granted or asserted.

Review date/environment/owner: 2026-10-11 KST, local Windows/PowerShell,
Python 3.14.3, PG. Time sampled during review: 00:15:01+09:00. Branch `main`, HEAD
`58b1011dad796017e47dbcc82a4225ca449bcdf6`; source candidates are untracked in a
dirty shared checkout. All pre-existing work was preserved. No remote connection,
browser/network request, private configuration read, installation, application/DB
operation, descendant, commit or push. Historical memory was not used as current
evidence. The current decision entry was read as prior chronology, not TLS proof.

## Scope / DoD Check

- [x] Confirmed existing WI002 handoff on disk before evidence processing.
- [x] Confirmed four injected Tier 0 documents, then role/policy/task context.
- [x] Read seven exact implementation files and both WI001 deliverables.
- [x] Independently reran focused hostile-input and hook-failure unittest suite.
- [x] Reviewed isolation, ACL ordering, lineage/reload, package fencing and recovery.
- [x] Recorded exact SHA-256 identities and local/native evidence boundaries.
- [x] Created only WI002 summary and Evidence Pack with `apply_patch`.
- [x] Returned next dependency to MA; no descendant or implementation fix performed.

## Reference Documents (Tier 0-2)

Extracted from the on-disk WI002 handoff INPUT POINTERS; full documents were
injected by MA. The evidence skill was also read from disk and applied directly.

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` (STD-001) | Approval, scope and transparency |
| 0 | `docs/standards/documentation-standards.md` (STD-004) | Metadata and two-set documentation |
| 0 | `docs/standards/development-standards.md` (STD-002) | Independent reproducible checks |
| 0 | `docs/standards/glossary.md` (STD-005) | PG/WI/REQ terminology |
| 1 | `.claude/agents/pg.md` | Independent security review role |
| 1 | `docs/policies/security-policy.md` | Secret, key and evidence boundaries |
| 1 | `docs/policies/access-control-policy.md` | Least privilege and no remote operations |
| 1 | `docs/policies/quality-gates.md` | Bounded evidence and acceptance |
| 2 | `.agents/skills/test/SKILL.md` | Structured results; WI-specific Python unittest scope |
| 2 | `.agents/skills/create-wi-evidence-pack/SKILL.md` | Handoff precondition and this saved output |
| 2 | `docs/standards/evidence-pack-standard.md` | Pointers, reproduction, results and rollback |
| 2 | `docs/templates/eval-report-template.md` | Bounded post-review evaluation |

Context: injected approved REQ010; on-disk WI001/WI002 handoffs, WI001 evidence
and summary, `AGENTS.md` entry rules and `docs/index.md:20-51`; all candidate
files below. `.claude/config/workspace.json` confirms ATS;
`.claude/config/context-injection-rules.json:56` gives PG required tiers `[0, 1]`.
Task types: security/review/testing. No further context injection or delegation.
Reuse: existing WI001 unittest suite and guarded candidates, no new framework.

## Findings / Evidence Pointers

**Blocking findings: none.** No implementation modification requested.

| Reviewed contract | Exact source anchor and result |
|---|---|
| Input and error handling | `scripts/deployment/tls-quarantine.py:19-70`: fixed apex, canonical global IPv4 /32, no arbitrary template path, fixed failure output; no native action. Source approval remains MA's gate. |
| ACME-only HTTP | `deploy/single-vps/tls-quarantine/nginx.conf.template:26-47`: unknown Host denial, anchored token/raw target, isolated root, static `try_files`, GET/HEAD only, no query logging or application include. |
| Restricted HTTPS | `deploy/single-vps/tls-quarantine/https.conf.template:1-35`: default handshake rejection, exact SNI/Host guards, exact marker, `satisfy all` and allow/deny before static content; no early success return. IPv4 literals only. |
| Explicit active config | `deploy/single-vps/tls-quarantine/nginx-service.conf:3-9`: cleared vendor command lists; all four test/start/reload commands select quarantine config. Runbook:102-109 requires full effective-unit inspection. |
| Lineage and key containment | `deploy/single-vps/tls-quarantine/reload-hook.py:27-78`: exact event, root ownership, no writable ancestry/hardlinks, protected live/archive parents, fixed archive link format, same generation, private key group/other denial and size bounds. |
| Native checks/reload failure | Hook:81-139: fixed absolute commands, clean environment, suppressed raw errors, timeout, exact SAN, OpenSSL verify before `nginx -t`, active/reload/active sequence. Earlier guard/command failure prevents reload; successful request is not served-certificate proof. |
| Package/compatibility | Runbook:62-109: exact signed seven-package review, maintainer scripts and schedule inspection, both Certbot runtime masks, MySQL/Nginx masks preserved, installed 2.9 help and actual Nginx features required. No identified source-level incompatibility; target execution remains NOT RUN by PG. |
| Activation and consent | Runbook:113-251: protected render, absent-target installs, no app/config publication, synthetic test before intentional staging registration, separate action-time consent, standard production lineage only. |
| Renewal and recovery | Runbook:253-350: native parsing/pair validation, independent allowed/denied/served checks, dry-run plus separate hook evidence, one scheduler, alert gap and continuing posture explicit; quiesce/remask without deleting retained artifacts. |

Limitations are existing native gates, not newly invented blocking controls.
The hook checks POSIX metadata, not extended ACLs or immutable source hashes on
every future run. The reviewed procedure supplies the protected-path and exact
source/render/install identity checks. The trusted exclusive root change window
remains the stated boundary. Request logs being off is not infrastructure-wide
capture protection. GET also permits HEAD under Nginx `limit_except GET`.

## Current-Window MA Observations and Clarifications

Source: two direct MA updates during this review on 2026-10-11 KST; exact
observation timestamps were not supplied. These are attributed native evidence,
not PG remote measurements and not package installation/application receipts.

- Apt print-uris plan remains seven new packages, zero upgrades/removals;
  reported download 498 kB and additional disk 2433 kB. No change performed.
- Nginx compiled with SSL/v2; access/rewrite not disabled. Root recovery tty1
  session count 1. These do not replace candidate native parsing/phase tests.
- Root/nginx/systemd/varlib ancestry root-owned and not group/other writable;
  root directory 0700, other reported ancestors 0755. Exact path xattr checks
  reported no POSIX ACLs; dedicated targets absent, no Nginx drop-ins, no relevant
  bind mounts and apt locks clear. No private paths or values were copied.
- Firewall form retains only existing management-source SSH inbound. Existing
  outbound TCP 80/443/53 and UDP 53/123, all IPv4, unchanged. Draft forms are not
  applied rules, and do not establish external denial or IPv6 closure.

**APT-CLARIFICATION (accepted, not a blocker):** runbook:83-87 says native
confirmation rather than blind `-y`. MA explicitly requested deterministic
confirmation for noninteractive SSH. Once MA has checked the exact seven
versions, signed origins, hashes, maintainer scripts and masks, with the immediate
final plan still exactly seven new packages and zero upgrades/removals, the
following form is acceptable as deliberate scoped confirmation:

```text
apt-get --assume-yes --no-install-recommends install <exact-version-set>
```

`<exact-version-set>` is notation, not executable input. No unspecified package,
extra dependency, upgrade, removal, repository change or automatic retry is
approved. Plan drift is HOLD. Native interactive input is not required solely
to repeat this already-explicit confirmation. This clarification supersedes only
that confirmation wording for this execution window; candidate bytes remain
unchanged. Exact package hashes/scripts/masks still require MA's actual checks;
the print-uris result alone does not close them.

**ACL-CLARIFICATION (accepted):** no `getfacl` installation is needed. Successful
`os.listxattr` checks on the exact public paths for
`system.posix_acl_access`/`system.posix_acl_default`, with neither present, can
close the existing no-ACL observation gate alongside ownership/mode/ancestry
checks. Errors must not be interpreted as absence. Apply the same existing
checks to newly created relevant paths. Do not read private file bodies.

## Public SHA-256 Identities

PG independently computed all nine hashes from exact local bytes. The eight
hashes published in WI001 match; the ninth identifies WI001 evidence itself.
Only these bytes plus the operational clarification above receive this verdict.
MA must still compare transferred files and privately record rendered/installed
identities; source hashes do not identify a private rendered configuration.

| Reviewed public file | SHA-256 |
|---|---|
| `scripts/deployment/tls-quarantine.py` | `723ed64dbbb48ca1a729dce7684498f6ff74fb6c85f0167e11e046783d761ace` |
| `scripts/deployment/test_tls_quarantine.py` | `b479cec7c47eb4bfee7b5b42f7a3537c3a8ecf968b6aae54f2cf208ba9f65320` |
| `deploy/single-vps/tls-quarantine/nginx.conf.template` | `ebc0ae231780ae2b16e82cf571cd144dd1ae7e2bdd9cb9d4ff564ad0c4a527ea` |
| `deploy/single-vps/tls-quarantine/https.conf.template` | `4fe91f83763303600149c532591b59ac043e28edc25dbdf395bc748ff83e42cb` |
| `deploy/single-vps/tls-quarantine/nginx-service.conf` | `cecaa38ef49f92518e6c5a776cedc7c03cecb54764afd1455b8a404f13161b93` |
| `deploy/single-vps/tls-quarantine/reload-hook.py` | `4c9d7ab410e6fa34597827fa679cc1e434f2cef108a9a7043edd32ab9f9104f0` |
| `deploy/single-vps/runbooks/domain-tls.md` | `ff9405ec71a39fe619780570b49f011773b001ab876b795827e34da00c0f8ab7` |
| `deliverables/user/WI-20261011-ATS-001-summary.md` | `8f9cd035935aff233385a92c2dde9619fad2b17201f4c6ed30abee35cb779866` |
| `deliverables/agent/WI-20261011-ATS-001-evidence-pack.md` | `5679ed91008c833c9f79fceadfb9bd0179a70db8de192bb5d3f934a310d42f52` |

## Commands & Outputs / Tests

```powershell
python -B -m unittest discover -s scripts/deployment -p test_tls_quarantine.py -v
git status --short --branch
git rev-parse HEAD
python --version
Get-FileHash -Algorithm SHA256 -LiteralPath <reviewed-public-file>
```

The test command was actually executed independently by PG, not copied from
WI001: **35 total, 35 passed, 0 failures, 0 errors, 0 skipped, 0.281s, exit 0**.
Suite split: 12 RenderTests and 23 HookTests. Captured terminal output ended:

```text
Ran 35 tests in 0.281s

OK
```

Renderer CLI subprocesses used only synthetic inputs. Native tool execution and
Linux metadata were mocked; no fixture files, sockets, keys or bytecode were
created. Tests cover injection/malformed inputs, private metadata/link rejection,
SAN bounds, trust/native-test/inactive-service failures, fixed output and reload
ordering. String assertions are not Nginx phase execution. No measured coverage
percentage, target-Python validation or native compatibility guarantee is claimed.

NOT RUN by PG: native `nginx -t`, Linux metadata/ACL inspection, systemd commands,
apt/download/install, DNS/firewall or HTTP/TLS probes, OpenSSL certificate/key
verification, Certbot help/registration/issuance/dry-run, timer or boot tests.
No online documentation refresh; version-specific behavior is carried from
WI001's documented research and remains subject to the existing installed-help
and native proof gates. No full application test suite was appropriate here.

## Changes / Completion Checks

Only these new files were written via `apply_patch`:

- `deliverables/user/WI-20261011-ATS-002-summary.md`: verdict and remaining gates.
- `deliverables/agent/WI-20261011-ATS-002-evidence-pack.md`: this skill-formatted review.

Direct final checks verify required metadata, local relative-link targets, ASCII
and LF/final-newline/no-trailing-whitespace in both outputs, and recompare all
nine source hashes. No repository-wide validator or index edit is claimed;
untracked candidates are checked directly rather than inferred from Git diff.

## Risks / Rollback

No runtime change by PG, so withholding/re-reviewing candidates is the only local
rollback. MA's runbook:325-350 recovery preserves SSH/egress, closes only newly
added 80/443 rules, stops window-owned scheduling, quiesces/remasks Nginx and
keeps MySQL masked/app absent. Retain certs/accounts/keys/configs/packages/DNS and
failure receipts; do not delete or restart the distro default. Unconfirmed
quiescence/cleanup remains HOLD. No unrelated Git/file restoration is authorized.

Remaining proof is bounded to existing gates: fresh target identity and masks,
package review, native parser/effective unit/sockets, DNS and exact firewall,
synthetic HTTP, action-time CA consent then staging/production, protected lineage,
allowed/denied/Host/SNI/path and external-source probes, renewal/hook/served TLS,
scheduler/continuing-boot intent, existing independent-alert gap and SSH cleanup.
An active timer or hook request is not future renewal or alert-delivery proof.

## Follow-ups / Evaluation

WI002 review is complete with PASS. It **Blocks WI-20261011-ATS-003**; MA must
immediately trigger its skill-generated handoff/delegation, with actual native
execution and cleanup receipts required before DocOps can complete integration.
PG launches no descendants. If candidate bytes change, route focused fixes through
WI001 and repeat affected review/tests before adoption. REQ010 remains in progress.

Evaluation: existing focused suite and exact-byte identities support the bounded
decision; no concrete blocker or policy/template change is proposed. Keep local
test results separate from native receipts. The scoped apt confirmation and ACL
xattr route avoid unnecessary interactive/tool-install work without expanding
the approved operational scope.

## Related Documents

- [Summary](../user/WI-20261011-ATS-002-summary.md): User-facing verdict.
- [Handoff](WI-20261011-ATS-002-handoff.md): Assignment and input pointers.
- [WI001 Evidence Pack](WI-20261011-ATS-001-evidence-pack.md): Reviewed preparation.
- [Operator runbook](../../deploy/single-vps/runbooks/domain-tls.md): Existing native gates and recovery.
- [Current decision entry](../../docs/index.md#current-decision-entry): Prior host evidence chronology.

## 2026-10-11 Test Isolation Correction Review

### Superseding Verdict and Historical Native Failure

**PASS for the test-only delta; native application HOLD remains pending MA's
fresh-stage Ubuntu/Python 3.12 rerun and receipt review.** The original PASS above
is retained as historical local evidence, not current permission to apply. The
old guard-isolation assurance is withdrawn; no implementation defect is inferred
from this test-isolation failure.

MA reported an exact-hash Ubuntu/Python 3.12 run: 35 total, 34 passed, one error
in `test_any_earlier_failure_stops_reload`. In its lineage subcase, unmocked real
`check_path` raised `FileNotFoundError` for the absent quarantine configuration
before the selected guard. Windows had instead passed on an incidental
`GateError`. Thus both SE and PG's earlier local reruns failed to establish the
intended guard reachability. The original source hash and run results remain
historical evidence. Native failure time was not provided. MA reported protected
public source staging only, with no installation/DNS/firewall/Nginx changes.

The user subsequently approved a test-only fix, retests and independent rereview.
PG reread the same on-disk WI002 handoff and applied the already-loaded test and
create-wi-evidence-pack skills. The original Tier 0 -> role -> task injection,
ownership and no-remote/no-descendant constraints continue. PG changed only these
two WI002 reports by append; no source/implementation or historical text edit.

### Delta Review and Exact Identities

- `scripts/deployment/test_tls_quarantine.py:287-306`: all event/path/lineage/SAN
  guards are mocked; only the selected guard gets `GateError("GUARD")`.
  Assertions check exact error text and target arguments/one call. SAN and native
  execution must not occur. A `Path.lstat` tripwire plus no-call assertion makes
  accidental real filesystem access fail instead of incidentally satisfying the
  exception expectation.
- `scripts/deployment/test_tls_quarantine.py:308-323`: separate missing-filesystem
  regression uses the real path guard with mocked `Path.lstat` raising exact
  `FileNotFoundError("SYNTHETIC_MISSING")`; event/path/first filesystem call are
  asserted and lineage/SAN/native calls must remain absent.
- **No concrete blocking finding in this delta.** In memory only, replacing this
  changed region with the original guard test reproduced original SHA-256
  `b479cec7c47eb4bfee7b5b42f7a3537c3a8ecf968b6aae54f2cf208ba9f65320`.
  This verifies that no other test-file bytes changed; no reconstructed file
  was written. All six non-test hashes in the original table were recomputed
  and remain unchanged. Implementation guards were not weakened.

| Current reviewed correction | SHA-256 |
|---|---|
| `scripts/deployment/test_tls_quarantine.py` | `a9ca1406ef209a9eb67406dbf14c889ca85d5c5eab01274933b1cd78bcf56436` |
| `deliverables/user/WI-20261011-ATS-001-summary.md` | `88bee1121a2ffb28eb466dab2902e8c651ac41214c4453e0d0c2e5f6f7b0659e` |
| `deliverables/agent/WI-20261011-ATS-001-evidence-pack.md` | `19bfda062e99323791d0cb98807b254c04cd31d6a8d1ca39268681f2e1e8b24e` |

The original table's test/WI001 report identities now identify historical
revisions only. Before this append, WI002 summary SHA-256 was
`d8a72824547a23703544e6978afe75ddddd3220665e14b54fbf1c7bf962b937b` and this report
was `820d1694bf6db86968c18a327436c2cee34ed64fee64086f2c9ba9190fab60a6`.

### Independent Retest and Remaining Gate

PG, local Windows/PowerShell/Python 3.14.3, 2026-10-11 KST; time sampled during
rereview 00:21:56+09:00. Branch/HEAD reconfirmed as `main` /
`58b1011dad796017e47dbcc82a4225ca449bcdf6`.

```powershell
python -B -m unittest discover -s scripts/deployment -p test_tls_quarantine.py -v
Get-FileHash -Algorithm SHA256 -LiteralPath scripts/deployment/test_tls_quarantine.py
```

Actual independent result: **36 total, 36 passed, 0 failures, 0 errors, 0 skipped,
0.271s, exit 0** (12 renderer and 24 hook tests). No native tool or real Linux
filesystem operation in the corrected tests. No test/implementation files were
written by PG; no network, private file, install, native operation or delegation.

Native corrected-suite execution is **NOT RUN by PG / pending MA**. Preserve the
original failed stage. MA uses a fresh protected public stage, confirms the new
test hash and unchanged six implementation hashes, reruns the identical command
on Ubuntu/Python 3.12 and supplies the exact 36-test receipt for independent gate
closure. Until then native application remains HOLD. All existing CA consent,
effective configuration, firewall, TLS, renewal, scheduler and cleanup gates are
unchanged; this addendum grants no installation or certificate action by itself.
WI003 integration remains dependent on actual MA execution/cleanup receipts.

Verification of this append: preserve the original WI002 byte prefixes, verify
both report formats/local links, and recheck the current test/report identities.
Rollback remains preservation and HOLD, never treating the old incidental test
PASS as valid evidence or changing production guards to satisfy the test.

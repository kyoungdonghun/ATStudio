---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SA
category: evidence-pack
status: stable
related_wi: WI-20261010-ATS-030
dependencies:
  - path: WI-20261010-ATS-030-handoff.md
    reason: Existing skill-generated assignment and three-path write scope
  - path: ../user/REQ-20261010-ATS-009.md
    reason: Approved local preparation; native sensitive gates remain separate
  - path: WI-20261010-ATS-028-evidence-pack.md
    reason: Latest completed native bootstrap and cleanup evidence
---

# Evidence Pack: WI-20261010-ATS-030

## Summary

TL;DR: Local-only configuration-readiness audit and chronological reconciliation.
One runbook plus two-set deliverables; **production HOLD**. No new architecture,
dependency, private configuration inspection or runtime action. SA verification
date 2026-10-10, Windows/PowerShell shared dirty checkout, `main` HEAD
`58b1011dad796017e47dbcc82a4225ca449bcdf6`. MA owns all cited native observations;
this report is not a native rerun or independent PG approval.

## Scope / DoD Check

- [x] Approved REQ and existing WI handoff checked; Tier 0 fully injected.
- [x] Current entry, WI014/026/028 and public source/templates reconciled.
- [x] Matrix separates applied, local-only, unverified and user-decision states.
- [x] Domain/CORS/callbacks/SMTP, jobs/egress, schema/ADMIN and recovery gates covered.
- [x] Public template audit and local artifact identities checked without execution.
- [x] Final scoped documentation checks and input-preservation recheck.
- [x] Two deliverables, non-destructive rollback and immediate WI031 dependency stated.

Write ownership: only the runbook, WI030 summary and this Evidence Pack.
No change to handoff, indexes, REQ, app, templates, historical deliverables or
Git index. No descendants. No DB/query, SSH/agent, service, Provider, secret,
installation, external price refresh, commit or push action.

## Reference Documents (Tier 0-2)

| Tier | Handoff pointer / actual use |
|---|---|
| 0 | `docs/standards/core-principles.md`, `documentation-standards.md`, `development-standards.md`, `glossary.md`: fully injected STD-001/004/002/005. |
| 1 | `.claude/agents/sa.md` persona and `docs/policies/security-policy.md`: fully injected. `access-control-policy.md`, `quality-gates.md`: read locally. |
| 2 | `.agents/skills/create-wi-evidence-pack/SKILL.md`: handoff-exists precondition, references, direct two-set output; used to author this pack. |
| 2 | `docs/standards/evidence-pack-standard.md`, `docs/templates/eval-report-template.md`: read for reproduction, limits, rollback and bounded evaluation. |
| 2 | `docs/index.md#current-decision-entry`, `deploy/single-vps/CONFIGURATION.md`, `runbooks/deployment.md`: read for chronology, provenance and operation gates. |
| 2 | `scripts/reconstruction/README.md`, `docs/design/db-schema.md`: relevant recovery/keyring and manifest sections read. |
| Context | REQ009, WI030 handoff, WI028 final native/cleanup, WI014 local PG review, WI026 artifact/completion and WI021 accepted MySQL query. |
| Source | `application.yml`, `schema.sql`/`seed.sql`, environment example/contract, service unit, `validate_bundle.py`, `build.gradle`; targeted scheduler/startup and bootstrap-render source reads. |
| Additional | Backup README, resource baseline, acceptance-capacity and production-bootstrap runbooks; `validate-docs/SKILL.md` and its validator implementation. |
| Conditional, not invoked | `test/SKILL.md`, `build-check/SKILL.md`, React best-practices and frontend standards: no build, test suite or frontend implementation in this documentation WI. |

Injection order preserved: Tier0 -> SA -> task context -> snapshot. Workspace
JSON confirms ATS; injection rules require SA tiers [0,1]. Meta architecture
inference and ADR creation excluded by handoff because selected architecture is
reused. Memory was a navigation hint only; current files and dated receipts
supersede its older not-implemented resource note. No memory changes.

## Evidence Pointers and Drift

- `deploy/single-vps/runbooks/production-readiness.md`, sections 1-5: new matrix,
  startup inventory, exact next gates and stop/rollback; no runnable operation.
- `deliverables/user/WI-20261010-ATS-030-summary.md`: decisions and bounded outcome.
- `WI-20261010-ATS-028-evidence-pack.md:276`: final v3 native/cleanup supersedes
  earlier failures. DB/JWT/billing creation and protected metadata only; private
  body not inspected, JDBC NOT_RUN, manifest UNRECORDED, app not runnable.
- `WI-20261010-ATS-021-evidence-pack.md:153`: accepted MA socket query observed
  buffer536870912/max30. Distinguish applied MySQL from unactivated app/proxy.
- `scripts/deployment/production-bootstrap.py:317`: `render_env` replaces DB/key
  fields, retaining domain/SMTP placeholders and preactivation flags by design.
- `deploy/single-vps/CONFIGURATION.md`, current candidate and section 6;
  `runbooks/resource-baseline.md`, section 1: earlier new-build/no-applied-settings
  statements are bounded by later WI026/WI021/WI028 receipts, not literal current
  global status. Preserve them; new runbook provides the reconciliation.
- `deploy/single-vps/runbooks/acceptance-capacity.md`, section 3: old512k/
  heap512MiB/cgroup1536MiB examples remain; current candidate values are explicitly
  distinguished. No capacity conclusion or edits outside ownership.
- `src/main/java/com/atstudio/atstudio/service/SubscriptionScheduler.java:58`,
  `PaymentReconciliationService.java:81`, `WithdrawalBillingCleanupCoordinator.java:31`,
  `storage/StorageMutationRecoveryService.java:35`, `audio/TrackAudioProcessingWorker.java:55`:
  jobs/startup can mutate; no universal scheduler disable inferred.
- `config/AcceptanceStartupGuard.java:68` (same Java root): billing-key validation
  precedes non-acceptance return. Empty Provider credentials are not isolation.

The runbook is new under `deploy/`, not `docs/`; no docs category count changed.
Its runbooks index and current-entry integration are outside the exact write
set: MA/DOCOPS must register the new pointer through the next authorized packet,
without editing old receipts. No whole-repository index-cleanliness claim.

## Commands and Outputs

All commands executed in repository root unless indicated. Tool versions:
PowerShell7.6.5, Python3.14.3, Git2.53.0.windows.1. No Java process was started.

| Check / exact command or procedure | Result |
|---|---|
| `git status --short --branch`; `git rev-parse HEAD` | main at the HEAD above; existing tracked/untracked changes preserved. |
| `python --version`; `git --version`; `$PSVersionTable.PSVersion.ToString()` | Versions above. |
| `python -B scripts/deployment/validate_bundle.py --json` | Exit0; template PASS, errors empty, deployment_ready=false, production HOLD. Fixed public allowlist only; no private/env read. |
| `Get-FileHash -Algorithm SHA256 -LiteralPath <exact public path below>` | Input identities below; no private file hash. |
| JAR SHA comparison with WI026 JSON, then read-only .NET ZIP entry stream SHA256 | Exactly one mysql-connector-j-9.7.0.jar, 2607237 bytes; hash below. No extraction/loading/execution. |
| WI026 `frontendCompletion.artifact.files` vs exact output tree, each file bytes/SHA256 | 143/143 match, exact file count; no rebuild. Paths first constrained beneath `build/WI-20261010-ATS-026-frontend-dist`. |

### Public Input Identities

SHA-256 identifies local bytes, not authorization or remote equality. JAR and
WI026 manifest match the supplied snapshot and WI028; source/template hashes
below match WI014 where listed. Full paths are repository-relative.

| Path | SHA-256 |
|---|---|
| `build/libs/ATStudio-0.0.1-SNAPSHOT.jar` | `517B745479792A7EAE6CE75357C7D1299AB644B50D5BC5B331FEA384CCB10E0A` |
| JAR entry `BOOT-INF/lib/mysql-connector-j-9.7.0.jar` | `0353648EAA1C91E0F4020C959ABF756BC866FFD583DF22AE6B6F6E0CBD43EB44` |
| `deliverables/agent/WI-20261010-ATS-026-artifact.json` | `FBEB74425ADE0FE3882A9DDDC4C97F7B67B75DDA711FFA37ACBD755A02FA4D9C` |
| `src/main/resources/application.yml` | `A493FFE54DA9DA77B56009338438F9C0718A2CA45CAD1C21241548E1052C9709` |
| `src/main/resources/schema.sql` | `6FB7DF1518394371BBFC5513DF5EBE2C97E067E7F77A1F20661AFB1C00B63FB7` |
| `src/main/resources/seed.sql` | `C848E0DA6B9B35EB26563D6AF3666B67FC119430C1AFD888EA7B0246800A579A` |
| `deploy/single-vps/env/atstudio.env.example` | `412E838B532C7249D0BBB9CA44EB114A659F7A78F1E2675BF87A676392FA18B0` |
| `deploy/single-vps/env/contract.json` | `9D02A317E3DE26896DCFB8877B8FB5B51AF7059B1C04117FA9FF170C72EC557D` |
| `deploy/single-vps/systemd/atstudio.service` | `118805E388B24E013148A796AED41B70F596ADA18FF627F8417B6B7B6262E8D1` |
| `deploy/single-vps/mysql/atstudio.cnf` | `008136130093048F395C4F414A937541AD314615E656AF7A64A170E4F376A233` |
| `scripts/deployment/validate_bundle.py` | `19361E515E694718B509E28C6223A3FFC0BF647DDF6BB972F8AA4904CC1CB593` |
| `deliverables/agent/WI-20261010-ATS-030-handoff.md` | `FF3B24EEB863FE3FDC0385AAE7DA03791CD7263C0321065D38F1C8FDF38F4CFC` |
| `deliverables/agent/WI-20261010-ATS-014-evidence-pack.md` | `D83B540E014BF72AC9799D698D22DDE82E45C56A14BAC4AC186021A2E351B5C6` |
| `deliverables/agent/WI-20261010-ATS-028-evidence-pack.md` | `0CA536E68AB6A07F3B761C28D2D3F5FB6AC9209276379C074102D0503C9897E3` |

## Test Evidence

Documentation/static audit only; no Java/SPA build, test suite, coverage or
native syntax run. Historical WI014 reports131 tests plus7 probes; WI030 does
not claim to rerun or extend them. JDBC probe correctness belongs to WI029 and
independent WI031 review, not this ZIP hash check.

Local verification at approximately 22:32 KST, 2026-10-10:

- Scoped skill validator: 3 documents, Tier0 present, no broken file links,
  8 supported traceability IDs, 0 errors/0 warnings, exit0. It checks file
  existence, not remote links or fragment semantics; index audit was not run.
- First invocation without UTF-8 failed with `UnicodeEncodeError` while printing
  a status glyph under cp949, before link checks. The same check with process-local
  `-X utf8` passed. No validator/source/OS setting was changed.
- After embedding reproduction code, the regex link checker interpreted a Python
  dictionary-index-then-call expression as a link to `root`. The document now
  uses equivalent dictionary `get` syntax; no validator rule was bypassed.
- PowerShell checks: all 3 documents have the seven required frontmatter fields,
  ASCII content, no trailing whitespace and a final newline. Bounded private-key,
  JWT and payment-key pattern scan found no matches; this is not generic DLP.
- `git diff --no-index --check -- NUL <owned-path>` ran for each owned file:
  exit1 with no diagnostics (new-file differences); supplementary whitespace
  checks passed. No Git index write or claim of a whole-worktree clean diff.
- Rehashed all16 observed public inputs after writing: zero mismatches, including
  handoff, prior evidence, source/templates and JAR. Concurrent unrelated work
  was not reverted. Scoped `git status --short --untracked-files=all --` lists
  only the three expected new files when filtered to this WI's write set.

Reproduce scoped skill validation from the repository root (no files written):

```powershell
python -X utf8 -B -c "import runpy; from pathlib import Path; from unittest.mock import patch; ns=runpy.run_path('.agents/skills/validate-docs/scripts/validate_docs.py'); root=Path.cwd(); paths=[root/'deploy/single-vps/runbooks/production-readiness.md',root/'deliverables/user/WI-20261010-ATS-030-summary.md',root/'deliverables/agent/WI-20261010-ATS-030-evidence-pack.md']; v=ns.get('DocValidator')(root); checks=[v.validate_tier0_docs()]; scope=patch.object(Path,'rglob',return_value=iter(paths)); scope.start(); checks.append(v.validate_internal_links()); scope.stop(); scope=patch.object(Path,'rglob',return_value=iter(paths)); scope.start(); checks.append(v.validate_traceability_ids()); scope.stop(); print('SCOPED_WI030',len(paths),'FILES',len(v.errors),'ERRORS',len(v.warnings),'WARNINGS'); raise SystemExit(0 if all(checks) and not v.errors and not v.warnings else 1)"
```

Artifact checks use the public WI026 JSON as expected data, never as executable
instructions. Exact read-only JAR/driver command used:

```powershell
$ErrorActionPreference='Stop'
$m=Get-Content -Raw -LiteralPath deliverables/agent/WI-20261010-ATS-026-artifact.json | ConvertFrom-Json
$jar=Get-FileHash -Algorithm SHA256 -LiteralPath $m.backend.artifact.path
if($jar.Hash -ne $m.backend.artifact.sha256){throw 'JAR_IDENTITY_MISMATCH'}
$zip=[IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $m.backend.artifact.path).Path)
try {
$drivers=@($zip.Entries | Where-Object FullName -Match '^BOOT-INF/lib/mysql-connector-j-[0-9.]+\.jar$')
if($drivers.Count -ne 1){throw 'DRIVER_ENTRY_COUNT'}
$stream=$drivers[0].Open()
try { $hash=[Convert]::ToHexString([Security.Cryptography.SHA256]::HashData($stream)) } finally { $stream.Dispose() }
[pscustomobject]@{JarMatch=$true;DriverEntry=$drivers[0].FullName;DriverBytes=$drivers[0].Length;DriverSHA256=$hash;FrontendOutput=$m.frontendCompletion.artifact.outputPath;FrontendCount=$m.frontendCompletion.artifact.fileCount;FrontendSetSHA256=$m.frontendCompletion.artifact.outputSetSha256} | ConvertTo-Json
} finally { $zip.Dispose() }
```

Exact SPA inventory comparison used:

```powershell
$ErrorActionPreference='Stop'
$m=Get-Content -Raw -LiteralPath deliverables/agent/WI-20261010-ATS-026-artifact.json | ConvertFrom-Json
$root=(Get-Location).Path
$base=[IO.Path]::GetFullPath((Join-Path $root 'build/WI-20261010-ATS-026-frontend-dist'))
$expected=@($m.frontendCompletion.artifact.files)
foreach($file in $expected){
$p=[IO.Path]::GetFullPath((Join-Path $root $file.path))
if(-not $p.StartsWith($base+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)){throw 'MANIFEST_PATH_OUT_OF_SCOPE'}
if((Get-Item -LiteralPath $p).Length -ne $file.bytes -or (Get-FileHash -LiteralPath $p -Algorithm SHA256).Hash -ne $file.sha256){throw 'SPA_IDENTITY_MISMATCH'}
}
$actual=@(Get-ChildItem -LiteralPath $base -File -Recurse)
if($actual.Count -ne $expected.Count){throw 'SPA_FILE_COUNT'}
'PASS SPA file inventory: '+$expected.Count+' exact byte-length/SHA256 matches; no rebuild'
```

### Deliverable Identities

`Get-FileHash -Algorithm SHA256 -LiteralPath` for these two files returned:

| Path | SHA-256 |
|---|---|
| `deploy/single-vps/runbooks/production-readiness.md` | `18F49D2433E3B0124542DA725FC1FC91B3A7556F332461AC6592F6900ECEC34A` |
| `deliverables/user/WI-20261010-ATS-030-summary.md` | `BDFF221DCC2A049B9199D0662B22183573BCCD49ADA8252CAD50AD2EC4E1881E` |

This pack has no self-referential hash; consumers should hash the completed file.

## Risks / Rollback / Evaluation

Production HOLD: unknown domain/mail choices, egress and job ownership,
canonical manifest/Hibernate, first ADMIN, secret recovery/backup/restore,
renewal/alerts/reboot and mixed-load acceptance. Bounded prior TEST evidence
is preserved, not promoted to new-host PASS. Monthly budget is not cost control;
no price/provider-term refresh performed or purchase recommended by implication.

Non-destructive rollback: amend only these three owned documents through an
approved correction, retaining dated evidence; withdraw their recommendation
until rechecked. Do not delete files, restore another agent's work, reset Git,
reapply SQL, replace secrets or roll back runtime. No runtime changed here.

Evaluation: reuse of existing architecture/contracts avoids duplicate executable
procedures; source matches current candidate values. Remaining documentation
drift is explicitly localized above. Independent PG review remains pending and
this SA pack cannot close it. No retrospective/policy changes within this WI.

## Follow-ups / WI Chain

**Immediate next WI: WI-20261010-ATS-031 (PG), depends on WI029 + WI030.** MA must
use create-wi-handoff-packet and delegate after both dependency receipts arrive.
PG reviews probe identities, read-only/secret-safe boundaries and this readiness
sequence. Then WI032 (DOCOPS) integrates actual approved MA observations/cleanup
or the exact NOT RUN reason and registers the runbook pointer under explicit
write ownership. Native approval, first start, domain purchase/DNS and external
services are not authorized by local completion. REQ009 stays open under MA;
no descendants were delegated by SA.

## Related Documents

- [Runbook](../../deploy/single-vps/runbooks/production-readiness.md): Readiness matrix and sequence.
- [Summary](../user/WI-20261010-ATS-030-summary.md): Concise decisions for the user.
- [Approved REQ](../user/REQ-20261010-ATS-009.md): Scope and dependency chain.
- [Handoff](WI-20261010-ATS-030-handoff.md): Injected references and exact ownership.

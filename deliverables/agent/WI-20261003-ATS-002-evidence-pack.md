---
version: 1.0
last_updated: 2026-10-03
project: ATS
owner: docops
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261003-ATS-002-handoff.md
    reason: Existing approved WI and file ownership
  - path: ../user/REQ-20261003-ATS-001.md
    reason: Stage A approval and target exclusions
  - path: ../../deploy/single-vps/README.md
    reason: Delivered operator entry
---

# Evidence Pack: WI-20261003-ATS-002

> Purpose: Record scoped operations-document delivery separately from target verification.

## Summary

Implemented single-VPS operator runbooks under the existing approved REQ and
WI handoff using `create-wi-evidence-pack`. No child agents or runtime actions.

| Evidence field | Value |
|---|---|
| Document edit date | 2026-10-03 |
| Verification date | 2026-10-03 |
| Environment | Shared Windows/PowerShell repository; local public source/document inspection only |
| Evidence owner | WI002 docops; MA findings explicitly attributed below |
| Source identity | `main`, HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`; existing dirty documentation preserved; no fetch or runtime identity claim |
| Result | Document implementation complete; bounded checks recorded below; integrated acceptance reserved to WI004/WI005 |
| Unverified boundary | Linux parsing/services, target DB/bootstrap, secret validity, remote access/TLS/renewal, mail, backups/restore, alerts, reboot/rollback and capacity NOT RUN |

## Scope / DoD Check

- [x] Only owned README/runbooks and these two deliverables changed by docops.
- [x] Exact-target/secret/DB/SSH approval gates and first ADMIN pending method
  are explicit; seed/test bootstrap is not a production account provisioner.
- [x] Cold consistent DB/media/config/key capture, visible downtime, retention
  proposals, off-provider copy and local peak-space HOLD are documented.
- [x] Isolated restore rejects imported LIVE/mail configuration and does not
  treat notification/interval flags as scheduler suppression.
- [x] Active users, admitted media transfers, configured rejection and resource
  exhaustion are separated; no unmeasured capacity guarantee.
- [x] Existing dated TEST/source reconstruction evidence preserved.
- [x] Scoped links, anchors, metadata, whitespace and source-value checks passed
  with planned integration dependencies reported separately.
- [ ] Integrated bundle/security/QA acceptance: owned by subsequent WIs.
- [ ] Named Linux target and production release: NOT RUN / HOLD.

## Reference Documents (Tier 0-2)

The four Tier 0 documents and security policy were supplied in full in the
user's ordered context before execution. Remaining pointers were read only as
needed; no claim that the entire repository was loaded.

| Tier | Document | Use |
|---|---|---|
| 0 | [core-principles](../../docs/standards/core-principles.md) / STD-001 | Approval and bounded execution |
| 0 | [documentation-standards](../../docs/standards/documentation-standards.md) / STD-004 | English docs, metadata and links |
| 0 | [development-standards](../../docs/standards/development-standards.md) / STD-002 | Evidence and ownership |
| 0 | [glossary](../../docs/standards/glossary.md) / STD-005 | Public Listening/Official Download and canonical roles |
| 1 | [security-policy](../../docs/policies/security-policy.md), [quality-gates](../../docs/policies/quality-gates.md) | Secrets, protected paths, runtime/verification separation |
| 2 | [current decision entry](../../docs/index.md#current-decision-entry) | Latest bounded evidence and preserved history |
| 2 | [runtime storage](../../docs/design/runtime-storage-operations.md), [SR-93](../../docs/SR/SR-93.md#remaining-production-gates) | Tuple, workers, new-target gates |
| 2 | [evidence skill](../../.agents/skills/create-wi-evidence-pack/SKILL.md), [validation skill](../../.agents/skills/validate-docs/SKILL.md) | Existing handoff prerequisite, report shape and bounded link checking |
| Input | [reconstruction](../../scripts/reconstruction/README.md), [db-schema](../../docs/design/db-schema.md), [database tooling](../../scripts/database/README.md) | Restore isolation, fresh-manifest and remote-helper exclusions |
| Input | [WI028](WI-20260909-ATS-028-evidence-pack.md), [REQ004](../user/REQ-20260909-ATS-004.md), [payment limits](../../docs/payment/known-limits-and-next-steps.md) | Original TEST/phone and pre-WAV reconstruction boundaries |

Injection rule reference: `.claude/config/context-injection-rules.json`, docops
required tiers `[0, 1]`, task type `documentation`; selected Tier 2 pointers
follow the supplied handoff. `.claude/config/workspace.json` confirms tag `ATS`.

## Evidence Pointers

Owned files, all new additions:

1. [deploy/single-vps/README.md](../../deploy/single-vps/README.md).
2. [runbooks/index.md](../../deploy/single-vps/runbooks/index.md).
3. [runbooks/deployment.md](../../deploy/single-vps/runbooks/deployment.md).
4. [runbooks/backup-restore.md](../../deploy/single-vps/runbooks/backup-restore.md).
5. [runbooks/acceptance-capacity.md](../../deploy/single-vps/runbooks/acceptance-capacity.md).
6. [User summary](../user/WI-20261003-ATS-002-summary.md).
7. This Evidence Pack.

Source facts checked without private configuration or execution:

- `frontend/src/api/client.ts`: `baseURL: '/api'`.
- `src/main/java/com/atstudio/atstudio/config/AcceptanceStartupGuard.java`:
  billing keyring validation before non-acceptance return; production rejects
  acceptance/test bootstrap.
- `src/main/resources/seed.sql`: single subscription INSERT, no user/ADMIN seed.
- `src/main/java/com/atstudio/atstudio/AtStudioApplication.java` and
  `service/SubscriptionScheduler.java`: scheduling enabled and active cron jobs.
- `src/main/resources/application.yml`: explicit source environment mappings,
  SQL validate default, storage and audio worker settings, callback defaults;
  these are not effective target configuration.
- WI001 in-progress `nginx/https.conf.template`, `nginx/nginx.conf.template`,
  `systemd/atstudio.service` and `journald/atstudio.conf`: bounded connection/rate,
  memory and log candidates were read, not modified or Linux-validated.

MA-supplied finding: no usable WSL distribution (`--list --quiet` returned
installer help/exit 1), no docker/bash/nginx commands. Docops did not rerun this
or install tools. Linux remains NOT RUN. MA's source findings on keyring/seed/
manifest were corroborated by the public source reads above.

## Commands & Outputs

Read-only commands included `git status --short --branch`, `git rev-parse HEAD`,
bounded `rg` searches and `Get-Content` of the cited public files. Initial file
inventory returned exit 1 because WI001/WI003 directories were not yet created;
that is a parallel-delivery observation, not runtime failure. No DB preflight,
disposable lifecycle, network probe or product test was run.

### Bounded Document Verification

No repository-wide PASS is asserted.
The repository validation skill was read; its stock script scans all Markdown
and does not check anchors. A scoped PowerShell check is used here so unfinished
parallel WI assets are reported separately and no unrelated docs are modified.

| Check, 2026-10-03 / docops / local Windows | Result | Boundary |
|---|---|---|
| Seven exact owned files: relative links and headings | 86 links / 18 anchors checked; 0 unexpected errors; exit 0 | One planned missing WI003 target, reported below; does not prove external links or integrated bundle |
| Frontmatter and dependency paths | All 7 required metadata keys in each file; 15 dependency paths checked | Presence/path check, not a general YAML validator |
| New-file text hygiene | 7/7 ASCII, final newline, no trailing whitespace, Related Documents present | Checks untracked additions omitted by ordinary Git diff |
| Public source/template assertions | 15/15 PASS, exit 0 | Same-origin API, unconditional keyring validation, production bootstrap refusal, seed, schedules, manifest/helper boundary, media/server/rate/heap/cgroup/journal values and WI001 interfaces; presence/value checks, not execution |
| Scoped `git diff --check` | Exit 0 | Owned additions are untracked; separate text checks above provide their whitespace coverage |
| Linux/application/DB/network/load/backup/restore | NOT RUN | No tool install, native syntax validation, product test or runtime effect |

Minimal scoped reproduction from repository root (PowerShell; read-only):

```powershell
$owned = @(
  'deploy/single-vps/README.md',
  'deploy/single-vps/runbooks/index.md',
  'deploy/single-vps/runbooks/deployment.md',
  'deploy/single-vps/runbooks/backup-restore.md',
  'deploy/single-vps/runbooks/acceptance-capacity.md',
  'deliverables/user/WI-20261003-ATS-002-summary.md',
  'deliverables/agent/WI-20261003-ATS-002-evidence-pack.md'
)
$errors = @()
$plannedValidator = Join-Path (Get-Location).Path 'scripts/deployment/validate_bundle.py'
foreach ($path in $owned) {
  $file = (Resolve-Path -LiteralPath $path).Path
  $body = Get-Content -LiteralPath $file -Raw -Encoding utf8
  $front = [regex]::Match($body, '\A---\r?\n([\s\S]*?)\r?\n---\r?\n')
  foreach ($key in @('version','last_updated','project','owner','category','status','dependencies')) {
    if (-not [regex]::IsMatch($front.Groups[1].Value, "(?m)^${key}:")) {
      $errors += "METADATA $path $key"
    }
  }
  if ($body -match '(?m)[ \t]+\r?$|[^\x00-\x7F]' -or -not $body.EndsWith("`n")) {
    $errors += "TEXT $path"
  }
  if ($body -notmatch '(?m)^## Related Documents\r?$') { $errors += "RELATED $path" }
  foreach ($dep in [regex]::Matches($front.Groups[1].Value, '(?m)^  - path: (.+)\r?$')) {
    $target = Join-Path (Split-Path $file) $dep.Groups[1].Value.Trim()
    if (-not (Test-Path -LiteralPath $target)) { $errors += "DEPENDENCY $path" }
  }
  foreach ($match in [regex]::Matches($body, '\[[^\]]+\]\(([^)]+)\)')) {
    $href = $match.Groups[1].Value
    if ($href -match '^https?://') { continue }
    $parts = $href.Split('#', 2)
    $target = [IO.Path]::GetFullPath((Join-Path (Split-Path $file) $parts[0]))
    if (-not (Test-Path -LiteralPath $target)) {
      if ($target -eq $plannedValidator) {
        'PLANNED_MISSING WI003 validator; integrated acceptance pending'
        continue
      }
      $errors += "LINK $path -> $href"
      continue
    }
    if ($parts.Count -eq 2) {
      $text = Get-Content -LiteralPath $target -Raw -Encoding utf8
      $ids = @([regex]::Matches($text, '(?m)^#{1,6} +(.+?)\r?$') | ForEach-Object {
        ($_.Groups[1].Value.ToLowerInvariant() -replace '[^\p{L}\p{N}\s_-]','') -replace ' ','-'
      })
      if ($ids -notcontains $parts[1]) { $errors += "ANCHOR $path -> $href" }
    }
  }
}
$errors
if ($errors.Count) { throw 'Owned document check failed' }
'PASS: seven owned documents; integrated acceptance remains pending'
```

Recheck source facts at the exact pointers above with `Get-Content`/`rg`; values may change
as WI001 completes. Do not treat a stale numeric receipt as later verification.

### Planned Integration Dependencies

- WI001: `deploy/single-vps/CONFIGURATION.md` and `env/contract.json`.
- WI003: `scripts/deployment/validate_bundle.py`.

At the scoped check, WI001 CONFIGURATION and contract files were present and
read for interface alignment. WI003 validator was **not present** and remained
the only planned missing link. The check reports
`SCOPED_PASS_INTEGRATION_PENDING`, not integrated bundle PASS. Later presence
does not establish implementation behavior or review approval. WI004/WI005
must recheck the assembled bundle; this WI does not wait for or execute WI003.

## Risks / Rollback

Runbook settings are candidate values; operator retention, timing, source-IP
distribution, secret setup, target administration and provider cost terms are
not approved by prose. No commands in the runbooks were executed on Linux.

Document rollback is limited to these seven owned additions after checking for
later edits and obtaining any required deletion approval. Do not reset shared
dirty files, remove the whole deployment directory, touch WI001 assets, undo
other WIs, or imply a service/data rollback from reverting documentation.

## Follow-ups

This WI blocks **WI-20261003-ATS-004** together with WI001. MA has supplied that
the independent security-review handoff is ready. Hand back these pointers for
that review; do not spawn agents or modify the REQ here. WI004 and WI003 feed
WI005, then WI006 closes only the Stage A entry. Production remains HOLD.

## Related Documents

- [Runbook index](../../deploy/single-vps/runbooks/index.md).
- [User summary](../user/WI-20261003-ATS-002-summary.md).

---
version: 1.1
last_updated: 2026-10-10
project: ATS
owner: RE
category: guide
status: stable
dependencies:
  - path: ../../deliverables/user/REQ-20261003-ATS-001.md
    reason: Approved offline preparation scope
  - path: ../../deploy/single-vps/env/contract.json
    reason: WI001 schema version 1 template interface
---

# Offline Deployment Template Audit

> Purpose: Detect regression in unrendered single-VPS candidates without applying them.

## 2026-10-10 Implementation

[WI012](../../deliverables/agent/WI-20261010-ATS-012-evidence-pack.md) extends the
original WI003 audit to current source/resource/admission contracts; 39 synthetic
tests passed for SE, MA and PG. [Final PG review](../../deliverables/agent/WI-20261010-ATS-014-evidence-pack.md)
found no new blocking defect within local preparation. This validator remains
offline and never reports deployment readiness. Native Linux checks are NOT RUN.
The old JAR must not receive new env/Hikari10 without a reviewed-source build,
artifact hash and effective admission proof.

Backup orchestration is separate: [backup guide](../../deploy/single-vps/backup/README.md)
and [WI013](../../deliverables/agent/WI-20261010-ATS-013-evidence-pack.md) describe
35 fake tests, default `side_effects:false` / `NOT_CONFIGURED`, default-OFF daily
policy and inactive timer. All host hooks, daily-service integration, independent
alert transport, actual copy and restore remain unimplemented/unconfigured.

## Run

No installation or third-party packages are required. From the repository root
in PowerShell, use the supplied Python runtime:

```powershell
$py = (Get-Command python -ErrorAction Stop).Source
& $py -B scripts/deployment/validate_bundle.py
& $py -B scripts/deployment/validate_bundle.py --root . --json
& $py -B -m unittest discover -s scripts/deployment -p 'test_*.py' -v
```

Python 3.10+ standard library only. `-B` suppresses interpreter bytecode files,
including test imports. The CLI itself opens input files only in binary read
mode and writes results only to stdout/stderr. It creates no rendered directory,
log, cache or deployment file. Tests write synthetic copies only inside
`TemporaryDirectory` and remove only those temporary fixtures.

`--root` names a **local repository root**, or a synthetic bundle with exactly
the same repository-relative layout. It is not the `deploy/single-vps` directory
and is never an external env-file argument. The default root is relative to
the script, independent of the caller's working directory. No directory scan,
private configuration, process environment lookup, shell sourcing/evaluation,
network access, subprocess, DB, cloud or service operation is implemented.

## Fixed Input Boundary

The allowlist is the 11 WI001 paths recorded in
[contract.json](../../deploy/single-vps/env/contract.json), plus the public
[application.yml](../../src/main/resources/application.yml) source-reference
check. Contract paths must equal the independent allowlist before any other
file is opened. Absolute paths, traversal, extra paths (including
`application-local.yml`), duplicates, symlinks and Windows reparse points are
rejected. Error output contains fixed rule IDs and allowlisted relative paths,
never values, arbitrary input keys, root arguments or raw exception messages.

Use a trusted, quiescent local checkout. These guards are not a filesystem
sandbox against concurrent replacement, hardlinks, mounted remote drives or
hostile changes to the validator itself. No real secret files are test inputs.

## Checks And Limits

| Check | Boundaries |
|---|---|
| Bytes | Every input has a 256 KiB read bound; strict UTF-8, terminal LF/CRLF, no lone CR/BOM/control bytes. Templates additionally require ASCII and no trailing whitespace. New untracked files are read directly; `git diff --check` alone is insufficient. |
| Contract | Strict JSON, duplicate-member/nonfinite rejection, exact version/path/key sets, independent safe literals, roots, empty credentials, placeholder ownership and current Nginx/gate fields. Descriptive prose is not an authority to weaken a rule. |
| Environment | Exactly 83 fixed example entries; duplicate/unknown/missing entries fail. A single-line literal subset with optional enclosing quotes is supported. Expansion, escaping, inline comments and multiline systemd syntax are deliberately unsupported. No environment file is sourced. |
| Identity drift | Eight fixed active-line SHA-256 fingerprints for Nginx, systemd, MySQL and journald. Blank/full-comment lines and outer indentation are ignored. These are reviewed-template drift/identity checks, **not semantic configuration validation**. Any active-line change requires owner review; there is no auto-update/accept flag. |
| Focused media regression | The actual extracted media pattern must be one of two known, bounded common regex forms. Nine positive paths cover canonical, `+1`, `0x1`, trailing slash and matrix suffix spellings; seven negative paths cover nonmedia/extra-segment routes. The numeric-only legacy pattern fails. The media block must retain its explicit proxy/rate/concurrency directives. |
| Focused frame regression | The flat SPA location must contain exactly `add_header X-Frame-Options DENY always;`. Omission, comment-only protection or removal of `always` fails. Both findings have tests that reapprove the changed hash yet still fail the independent regression rule. |
| Source mapping | Required `${ENV}` references in public YAML plus eleven exact admission/SMTP/Hikari/spool property paths; rejects wrong parent, duplicate/comment-only/renamed/changed leaves. Boot/JVM/OS bindings remain separately classified. Bounded mapping inspection is not a full YAML/Spring parser or target binding proof; isolated Java binding tests belong to WI011. |
| Structural regression | Bounded Nginx directive tree checks normalized-URI download map, case-insensitive multipart map, explicit child general/multipart/media/download zones, composed API/media rates, trusted identity, protected routes and buffering. JVM/cgroup/MySQL duplicate/value guards operate independently of approved hashes. |

The regex checks use Python's common subset on normalized synthetic paths,
not Nginx PCRE, real URI normalization, location precedence or Spring binding.
Matrix suffix cases are defensive coverage; actual backend acceptance is not
claimed. Unknown regex forms fail closed without execution. Native `nginx -t`,
full rendered includes/overrides, actual response headers, encoded paths and
HTTP behavior still require target testing. The bounded directive check is not a
general Nginx parser. Fingerprints must not be recomputed to dismiss a finding;
review the change and its behavioral regression tests first.

## Results And HOLD Gates

Exit `0` means only **template audit PASS** with unresolved placeholders
intentionally required. Exit `1` means an input/rule failure; exit `2` means
invalid CLI arguments. `--json` adds no output file and reports
`deployment_ready: false` and `production_status: HOLD` even on exit `0`.
Both output formats prominently retain seven target gates:

- HOLD: exact host/domain/change scope and recovery access.
- HOLD: secret provisioning and separately approved fresh DB initialization.
- NOT_RUN: native Linux Nginx/systemd/MySQL/Java/FFmpeg validation.
- NOT_RUN: IPv4/IPv6 firewall, HTTPS/renewal, client identity and private files.
- NOT_RUN: verified egress isolation and one JVM/encoder worker.
- NOT_RUN: selected auth/mail, logging, consistent backup/isolated restore,
  reboot, alerts and measured bounded load.
- HOLD: general release and LIVE payment authorization.

There is no deployment-ready mode or real-secret validator. Fixed resource
values are unmeasured starting settings, not capacity guarantees. Blank Provider
credentials and disabled notifications do not stop schedulers or prove egress
isolation. The validator does not run product Java/frontend suites; WI011 and
MA/PG receipts separately record selected Java tests, not a full build or new JAR.
Earlier Gmail, WAV/128kbps MP3 and phone TEST evidence keeps its original bounds.

## Related Documents

- [Configuration candidates](../../deploy/single-vps/CONFIGURATION.md): Template ownership and later target operations.
- [Runbook index](../../deploy/single-vps/runbooks/index.md): Deployment, restore and acceptance procedures.
- [WI003 handoff](../../deliverables/agent/WI-20261003-ATS-003-handoff.md): Scope and output contract.
- [WI003 evidence](../../deliverables/agent/WI-20261003-ATS-003-evidence-pack.md): Dated local results and limitations.

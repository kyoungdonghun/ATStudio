---
version: 1.0
last_updated: 2026-11-06
project: ATS
owner: EO
category: registry
status: active
dependencies:
  - path: ../index.md
    reason: Current decision entry
  - path: ../../scripts/reconstruction/README.md
    reason: Toolchain and source reconstruction procedure
---
# Reviewed source checkpoint — 2026-11-06

REQ-20261106-ATS-003 authorizes privacy review, selection, local commit and
non-forced publication of reviewed source. WI-20261106-ATS-005 owns privacy
clearance; WI-20261106-ATS-006 owns selection and reconstruction evidence; MA
alone executes Git writes and remote checks. Server work remains deferred.

This document is the public summary of the current local implementation. Its
date is a source/evidence review date, not a deployment or restore date.

## Included capabilities and source groups

| Group | Source | Completed local work |
|---|---|---|
| Application | `src/`, `frontend/`, listening deployment examples | Online capture admission and storage continuity; listening and selling modes; explicit DATABASE catalog publication; protected management thumbnails; exact-attempt publication recovery; selected-content write guards |
| Backup operations | `scripts/deployment/backup/`, backup/systemd examples | Guarded online capture/reconcile/restore, scheduling, bounded existing-service attachment, append-only v2 outer HOLD recovery and causal successor ledger |
| Reconstruction | This registry, source identity manifest, current entry and selected operator guides | Public scope, verification boundaries, source identity and retained private-evidence exclusions |

These groups include their corresponding tests and static example configuration.
No private live configuration, actual approval, data, audio, database dump, secret,
build output or runtime authority is supplied. Templates remain unapplied.

DATABASE catalog publication is explicit and independent of track activation.
Unpublish selected content before editing; validate it before explicitly publishing
again. Unknown writes remain HOLD unless matching committed audit evidence permits
exact-attempt reconciliation. Historical attribution does not replay old writes.

V2 outer HOLD recovery preserves original claims, locks, failures and receipts.
It uses append-only disposition under original config/code/owner bindings and
does not manufacture COMPLETE or restore success. Unsupported v1 custody,
changed boot/cgroup identity and unknown transfer/readers remain HOLD. The
[v2 guide](../../deploy/single-vps/backup/OUTER-HOLD-V2.md) and actual source
define the support boundary. Its original under-review label is a preserved
candidate snapshot; final local acceptance is summarized below.

## Source identity and local verification

[The source manifest](git-checkpoint-20261106-source.json) lists the local reviewed
SHA-256 and the expected repository-filtered Git blob ID for each input. Raw local
CRLF bytes can differ from LF Git blobs under `.gitattributes`; this is explicit,
not a claim that an archive, JAR or native pinned bundle is byte-reproducible from
Git. Rebuild and regenerate native source pins for the exact approved checkout.
Do not replace original runtime pins or reuse old private authority automatically.

EO/docops observed these identities on Windows on 2026-11-06:

| Evidence owner / date | Checkpoint | Identity and verification |
|---|---|---|
| SE/FE/CR, 2026-11-05; EO identity check, 2026-11-06 | Application integration | 1,015 application/build/test inputs unchanged; backend candidate-v2 470 inputs unchanged; frontend-v3 342 inputs unchanged |
| SE/CR, 2026-11-06; EO identity check, 2026-11-06 | Outer recovery candidate-02 | All 41 source/copy hashes unchanged; decisive coherent run 51 PASS, comprising 33 independent and 18 author tests |
| EO/docops, 2026-11-06 | Selected checkpoint syntax | 41 Python files parsed and five JSON examples decoded without error; publication-document links reviewed separately |

Existing backend/frontend build, focused regression and synthetic browser evidence
remains owned by its original WIs; no new application build or complete suite was
run merely to publish unchanged inputs. The final frontend source manifest contains
342 inputs. Earlier failed and passing iterations remain historical; overlapping
test counts are not additive. Source equality is evidence reuse, not a fresh run.

The repository-wide documentation validator previously failed on historical links
and archived/generated copies. Its CLI does not implement `--help` or a path filter.
This checkpoint uses selected-document checks and does not claim global docs are
clean. Standalone Markdown/ruff/jq executables were not installed in this environment;
Python AST/JSON and scoped link/metadata checks are bounded substitutes, not an
equivalent all-rules lint pass.

## Reconstruction checks

Start with the [reconstruction guide](../../scripts/reconstruction/README.md), the
tracked Gradle wrapper and frontend lockfile. Use the reviewed published revision
and record `git rev-parse HEAD` before comparing the manifest. A Git-only check is:

```powershell
@'
import json, subprocess
from pathlib import Path
m = json.loads(Path('docs/registry/git-checkpoint-20261106-source.json').read_text(encoding='utf-8'))
bad = []
for row in m['files']:
    got = subprocess.check_output(['git', 'rev-parse', 'HEAD:' + row['path']], text=True).strip()
    if got != row['git_blob_oid']:
        bad.append(row['path'])
print({'files': len(m['files']), 'mismatches': bad})
raise SystemExit(bool(bad))
'@ | python -
```

The manifest is a dated checkpoint; later approved source changes legitimately
require a new manifest. A clean checkout contains the source tests and examples.
For a local source-only rerun, inspect fixtures first and use an isolated test
directory without private runtime settings. Representative focused commands are:

```powershell
python -B -m unittest discover -s scripts/deployment/backup -p test_outer_recovery.py
python -B -m unittest discover -s scripts/deployment/backup -p test_outer_hold_recovery_independent.py
```

Those commands run synthetic local cases. They do not reproduce the retained
copied-candidate harness or execute native recovery. The original final harness
and raw logs are retained locally. The [listening guide](../../deploy/single-vps/listening/README.md)
defines paired backend/frontend modes and the reserved catalog contract.

## Retained evidence limits

Historical private or ambiguous REQ/WI records, unreviewed provider/client
screenshots and PDFs in the current working set, personal host instructions,
built JAR/SPA artifacts and raw execution logs remain local and are excluded from
this selection. Previously reviewed artifacts already in outgoing history retain
their exact-hash publication decision. The previous October 26 exclusion
decisions were rechecked rather than overridden by a pattern-clear scan. Original
document bytes, candidate manifests and source pins remain intact in local custody.

Public operator-guide copies redirect links to omitted local evidence here. Those
links do not mean this summary contains the original records or grants operational
authority. The original records are not silently rewritten to remove prior failures.
The current public entry retains the committed historical index and adds this safe
summary; the larger uncommitted local chronology is preserved separately.

Source Git history and hash records cannot restore private settings, database rows,
uploaded media, accounts, local conversations or original evidence. Those require
separately managed private backup and approved restore verification. No private
backup or restore was performed by this checkpoint.

## Unverified operational boundaries

Windows H2/mock/synthetic checks do not prove native MySQL collation or concurrency,
Linux/root/systemd/cgroup enforcement, power-loss durability, deployment-wide legacy
exclusion, availability/latency, physical devices, public publication, backup receipts
or current restore. Costs and provider terms were not refreshed. Local code completion
does not close the separately approved native acceptance milestones.

## Revision and publication receipt

Use `git log -1 --format=%H -- docs/registry/git-checkpoint-20261106.md` to identify
the commit containing this reviewed source checkpoint. Its own commit ID is not
self-embedded. PG clearance and exact staged-blob comparison precede publication;
MA retains the actual Git/remote receipt separately in local WI006 evidence.
This source record does not assert a deployment or remote runtime. No server
execution follows automatically.

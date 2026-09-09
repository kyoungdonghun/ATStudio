---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: SE
category: evidence-pack
status: stable
dependencies:
  - path: WI-20260909-ATS-027-handoff.md
    reason: Approved delegated ownership and acceptance criteria
  - path: ../user/REQ-20260909-ATS-006.md
    reason: Approved runtime browser verification scope
  - path: ../../scripts/validation/wav-browser/README.md
    reason: Reproduction commands and probe input contract
---

# Evidence Pack: WI-20260909-ATS-027

## Summary

Prepared 24 actual private WAV fixtures, a read-only original/MP3 probe with nine
passing synthetic tests, and an explicitly requested guarded runtime launcher
that passed CheckOnly before ownership transferred to MA.

## Scope / DoD Check

- [x] Twenty unique stereo/44100Hz/PCM16 sine WAVs, exactly 10MiB each.
- [x] Small valid, deliberately corrupt, near-100MiB and over-100MiB inputs.
- [x] Recorded paths, real payload/frame checks, sizes, durations and SHA-256.
- [x] Pair probe accepts a bounded sanitized MA list; no DB/API access.
- [x] Known portable FFmpeg reused; no installs, sparse fake-size files or uploads.
- [x] New launcher delivered and CheckOnly passed; no backend started/stopped by SE.
- [x] Summary/evidence prepared using create-wi-evidence-pack after handoff check.
- [ ] Actual stored Track pairs: no MA record list provided to this SE run.
- [ ] Browser/DB/public URL/restart acceptance: owned by MA and WI028, not claimed.

The last two items are downstream execution boundaries, not synthetic PASS claims.
The 79 declared baseline dirty files were not edited by SE. MA worked concurrently;
this evidence does not claim a byte-frozen worktree or validate MA's DB operations.

## Reference Documents (Tier 0-2)

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approval, preservation, truthful results |
| 0 | `docs/standards/development-standards.md` | Bounded helper code, tracing, risk-based tests |
| 0 | `docs/standards/documentation-standards.md` | English documents and metadata |
| 0 | `docs/standards/glossary.md` | Official Download versus Public Listening |
| 1 | `docs/policies/security-policy.md` | Secret minimization and private evidence |
| 2 | `docs/design/runtime-storage-operations.md` | Existing storage tuple and full-length MP3 contract |
| 2 | `scripts/database/manual-wi023-audio-processing.sql` | Read-only migration context; not executed |
| 2 | `deliverables/agent/WI-20260909-ATS-023-evidence-pack.md` | Nominal 128000bps, duration tolerance, 100MiB cap |
| 2 | `deliverables/user/REQ-20260909-ATS-006.md` | Approved scope and WI027 -> WI028 chain |

Assignee: SE. MA injected Tier 0 constraints; task-relevant portions were read.
Injection rule source: `.claude/config/context-injection-rules.json`, SE required
Tier 0, with task-specific security/runtime context. Workspace tag ATS confirmed
in `.claude/config/workspace.json`. Skills loaded: create-wi-evidence-pack, test,
validate-docs. No additional agents were spawned.

## Evidence Pointers

New repository files only:

- `scripts/validation/wav-browser/generate_fixtures.py`: deterministic actual PCM generation, complete frame reads, manifest, no overwrite.
- `scripts/validation/wav-browser/probe_pairs.py`: sanitized list validation, hashes, ffprobe metadata and full-decode duration checks.
- `scripts/validation/wav-browser/test_helpers.py`: private synthetic positive/negative cases.
- `scripts/validation/wav-browser/start-backend-wav128.ps1`: user-added launcher scope, now handed to MA without further SE edits.
- `scripts/validation/wav-browser/README.md`: commands, fixture inventory, exact input schema and limitations.
- `deliverables/user/WI-20260909-ATS-027-summary.md` and this evidence pack.

Private root: `%LOCALAPPDATA%/ATStudio/validation/wav-browser-20260909/`.
Actual fixture inventory: `fixture-manifest.json`; batch: `batch20/`; boundary and
failure inputs: `edge/`. Total: 24 files / 419783295 bytes. Each of the twenty batch
files is 10485760 bytes / 59.442834467 seconds with a distinct SHA-256. All exact
hashes and generation arguments are retained in the private manifest.

Passing helper evidence:
`self-test/9761265483c64f30b131b0fa22e3c724/test-result.json` and
`self-test/9761265483c64f30b131b0fa22e3c724/synthetic-pair-result.json`.
The latter is explicitly a synthetic pair, not a runtime Track.

## Commands & Outputs

Run from repository root:

```powershell
python -B scripts/validation/wav-browser/generate_fixtures.py
python -B scripts/validation/wav-browser/test_helpers.py
python -B scripts/validation/wav-browser/probe_pairs.py --help
```

- Generation: exit 0, batch20 ready, 24 files, 20 distinct batch hashes. All fixture
  paths were immediately sent to MA. A repeat invocation exited 1 intentionally:
  `Refusing to overwrite existing WI027 fixtures or manifest.`
- Tests: final exit 0, 9 run, 0 failures, 0 errors; elapsed 2.499 seconds as observed.
- Initial tests: 9 run, 1 failure because FFmpeg's boundary decode end timestamp
  differed from frame duration by about one sample. Exact source frames/hashes
  already agreed. Boundary timestamp assertion now permits two 44100Hz samples;
  production probe tolerance remains the declared 0.25 seconds. Initial failure
  retained at `self-test/fd1d898b44b8477abf5429278cfbf7e8/test-result.json`.
- Probe CLI help: exit 0. No actual runtime list was supplied or probed.

## Launcher Verification And Ownership

The user explicitly added launcher preparation to WI027 after fixture generation.
SE read, never modified, the old external `start-backend-remediation.ps1`. A
temporary string-substitution defect in the new file was corrected with
apply_patch before handoff; final PowerShell AST parse reported zero errors.

MA's first CheckOnly found the historical createDatabaseIfNotExist option. At the
user's request, SE minimally changed only the new launcher to parse the JDBC query
with System.Web.HttpUtility, reject credential keys, and remove the creation key
from the child CLI override. The root ignored local file remained untouched.

SE then executed CheckOnly (not startup) using:

- JAR: `%LOCALAPPDATA%/ATStudio/remote-development-20260908/ATStudio-wav128-20260909.jar`.
- JAR SHA-256: `E7802311EE2F25762A8F601D5F9A9CAA2954DFB817D904DB351B0E0DB7B72784`.
- Live local-config SHA-256: `50680EC3473910CBF333B5586D605D9ECD2FAABC4FD1079532C4F7FADF0F69F4`.
- PublicOrigin: the exact HTTPS Cloudflare origin supplied by MA in this task.
- Result: exit 0, `CHECKS_ONLY_NOT_APPLIED`, 8080 FREE, localhost:3306/atstudio,
  original uploads/private-uploads tuple, portable FFmpeg path, worker true,
  external SMTP keys present and never displayed, RuntimeSafetyAcknowledged false.

MA subsequently confirmed CheckOnly PASS and took exclusive launcher ownership
to start the backend. MA's attempted patch did not apply because SE's change had
already landed; **no MA source intervention actually occurred**. SE made no
launcher edits after ownership transfer. MA's launch or subsequent health is not
independently verified here. Automatic schedulers/recovery remain enabled; the
launcher does not establish business-job safety.

## Tests

Nine focused stdlib unittest cases cover all fixture hashes/sizes/metadata, full
decode of both size-boundary files, corrupt-input rejection, a valid 128kbps pair,
wrong bitrate, shortened MP3, wrong original, input schema/duplicate/readiness
guards and path traversal rejection. No Java/React source changed, so repository
build/test suites were not run by SE. Documentation validation and diff checks
were also performed: `python -B .agents/skills/validate-docs/scripts/validate_docs.py`
exited 0 with Tier 0, internal links, supported traceability IDs and document index
checks passing. Scoped `git diff --check` exited 0 but does not inspect untracked
additions; the new files received a separate trailing-whitespace scan.

MA later reported JPA validate boot success with backend PID 27188, successful
actual admin login and 4/20 public-origin browser uploads underway around 13:56
KST. These are MA-provided progress observations, not independent SE verification
and not evidence of completion of all twenty uploads. MA requested and received
the probe's exact five-field input schema while uploads continued.

## Risks / Rollback

- Pair results trust the MA-provided Track identity/state mapping. The probe does
  not query MySQL, prove Official Download transport, prove UI operation, prove
  per-frame CBR or compare perceptual identity of same-length derivatives.
- Corrupt input should fail initial audio analysis; it alone does not exercise
  worker FAILED/retry behavior. MA must choose an approved separate retry case.
- No stored media, DB rows/schema, global DB manifest, old launchers, existing
  credentials or existing dirty source were changed by SE. No stop/start,
  payment/mail request, browser action, package install, commit or push occurred.
- Retain all new fixtures, failed-test evidence and passing-test evidence. Stop
  using helpers to undo preparation; do not delete old/new media or roll back the
  running JAR without separate MA approval and retained-data review.

## Follow-ups

WI027 unblocks WI-20260909-ATS-028. Return this evidence to MA immediately so MA
can use create-wi-handoff-packet and delegate WI028/qa-integ with its own runtime
and browser evidence. SE does not spawn that agent. REQ006 remains open until MA
and WI028 close the actual acceptance criteria.

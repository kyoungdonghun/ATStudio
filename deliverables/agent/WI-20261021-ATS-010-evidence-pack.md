---
version: 1.0
last_updated: 2026-10-21
project: ATS
owner: SE
category: reference
status: active
---
# Evidence Pack: WI-20261021-ATS-010

## Summary

Prepared frozen local audio-trial components using the retained successful v8r1
tool identities, WI068 JAR and approved synthetic WAV; 50 offline tests pass.
Native lifecycle integration is assigned to WI013, followed by WI011 independent
review. This component evidence is not native-start authorization or target PASS.

## Scope / DoD Check

- [x] Rechecked 643 source/build inputs, retained JAR, selected WAV and all members
  listed by four retained manifests (16 + 42 + 12 + 7 checks; scopes overlap).
- [x] Pinned the manifest bytes themselves before trusting member hashes.
- [x] Implemented finite multipart/READY/activation/MP3/Range/download/denial
  contract, real bounded loopback transport, audio snapshot/delta validators,
  existing-entitlement checks, exact cleanup-step shape validation and tests.
- [x] Default local command is read-only and cannot start services or connect.
- [x] Recorded explicit three-principal data proposal and preserved originals.
- [x] Supplied concrete source-pinned worker delta proposal and native interface.
- [ ] Native lifecycle wiring is outside this component freeze and continues in
  WI013; final joint review is WI011. The partial patch alone is not deployable.
- [ ] Target execution, audible playback, actual conversion and data mutations
  remain unperformed and separately gated.

## Reference Documents (Tier 0–2)

Injected by MA according to the [handoff](WI-20261021-ATS-010-handoff.md):

| Tier | Reference | Application |
|---|---|---|
| 0 | [Core principles](../../docs/standards/core-principles.md) | Scope and approval |
| 0 | [Development standards](../../docs/standards/development-standards.md) | Bounded standard-library tooling |
| 0 | [Documentation standards](../../docs/standards/documentation-standards.md) | Metadata, relative links, history |
| 0 | [Glossary](../../docs/standards/glossary.md) | REQ/WI traceability |
| 1 | [Security policy](../../docs/policies/security-policy.md) | No secrets/remote actions |
| 1 | [Quality gates](../../docs/policies/quality-gates.md) | Independent review |
| 1 | [SE role](../../.claude/agents/se.md) | Implementation ownership |
| 2 | [REQ007](../user/REQ-20261011-ATS-007.md) | Current local approval |
| 2 | [WI009](WI-20261021-ATS-009-evidence-pack.md) | Previous bounded authentication success |
| 2 | [WI068](WI-20261011-ATS-068-evidence-pack.md) | Retained source/JAR identity |
| 2 | [WI063](WI-20261011-ATS-063-evidence-pack.md) | Approved WAV identity |

`create-wi-evidence-pack` was read and applied after confirming the WI handoff
exists. Parent-injected context is distinguished from source files actually
inspected here: TrackController/DTOs, TrackService, DownloadService, Track entity,
worker/encoder/transactions, storage coordinator, security configuration,
subscription repository, and retained native/ledger/network/release code.

## Evidence Pointers

| Output | Purpose |
|---|---|
| [README](../../build/WI010-audio-trial/README.md) | Commands, interfaces, bounds, account/data and native prerequisites |
| [Preflight](../../build/WI010-audio-trial/prepare_local.py) | Manifest/member/source/JAR/WAV verification |
| [Identity receipt](../../build/WI010-audio-trial/identity-receipt.json) | Current local comparison results |
| [Trial contract](../../build/WI010-audio-trial/audio_trial.py) | Bounded one-track flow; no default network |
| [HTTP bridge](../../build/WI010-audio-trial/bridge.py) | Actual loopback transport callable behind a permit callback |
| [Existing login](../../build/WI010-audio-trial/existing_login.py) | Same-window login of three existing approved actors, no stale JWT prerequisite |
| [Audio ledger](../../build/WI010-audio-trial/audio_ledger.py) | Existing-row preservation, four journal rows, USER download evidence |
| [Native validators](../../build/WI010-audio-trial/native_contract.py) | Queue/process/exact digest delta/cleanup shape |
| [Worker proposal](../../build/WI010-audio-trial/audio-worker-proposal.patch) | Partial review patch only, not an installer |
| [Tests](../../build/WI010-audio-trial/tests.txt) | 50 passing tests |
| [Freeze](../../build/WI010-audio-trial/freeze.json) | Fifteen exact local component files |
| [User summary](../user/WI-20261021-ATS-010-summary.md) | Approval-facing boundaries |

Freeze SHA-256: `0d7ae85c4ca3f0425826c2784867ee57169efb96c14cb5d2b315567d8b9bc516`.

JAR SHA-256: `f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81`.

WAV SHA-256: `d7b1011a45c84eb0c0d0308cc744289aa277b8b43ff0a4b97d6c59a1254ffa13`.

## Commands & Outputs

Executed locally on 2026-10-21; evidence owner SE; no SSH/browser/cloud operation:

```powershell
python -B build/WI010-audio-trial/prepare_local.py --emit-review
python -B -m unittest discover -s build/WI010-audio-trial -p test_*.py -v
python -B build/WI010-audio-trial/verify_freeze.py
```

Results: `LOCAL_IDENTITY_PASS_NATIVE_HOLD`, 50 tests PASS,
`LOCAL_FREEZE_PASS_NATIVE_NOT_AUTHORIZED` (15 files).

## Tests

Meaningful cases include WAV tamper before any send, durable-reservation failure,
uncertain upload and replay refusal, terminal processing failure, poll exhaustion,
wrong range/download bytes, response limits, negative-error mismatch, leaked WAV
prefix, deadline/wire exhaustion, CL+TE/duplicate headers/redirect/truncated body,
foreign track-route rejection, unrelated payment mutation, wrong download owner,
initial-WAV four-journal shape, retained-source DELETE intent, derivative journal
before track linkage, unfinished journal and leftover staging file rejection.

Tests inject a fake HTTP connection. They do not create a listener or socket and
do not prove JVM/FFmpeg/native firewall behavior. SQL strings were source-reviewed
and synthetically tested, not submitted to a database.

## Risks / Rollback

- The old auth-only ledger, gateway, principal-absence gate, worker-disabled args,
  one-PID check and Gmail-specific receipts must not be replayed as audio approval.
- An inactive new WAV needs explicit activation after READY. ADMIN download is not
  proof of USER entitlement; positive test uses a separate entitled USER.
- First download writes a license, download history and track counter even if a
  later transport error makes success uncertain. No automatic retry is allowed.
- Initial WAV conversion produces two CREATE plus two DELETE journal intents;
  source references keep the original. A two-journal expectation was corrected
  from actual transaction/coordinator code before freezing.
- Full worker startup can recover any preexisting claimed track. Native preflight
  must prove zero active audio work before JVM release, without deleting old data.
- Rollback: cease use of the new local folder; originals and product code are
  unchanged. Do not delete historical evidence or runtime data automatically.

## Follow-ups

WI013 completes source-pinned native lifecycle integration using these interfaces.
WI011 reviews exact WI010 + WI013 bytes; WI012 consolidates account/data decisions.
Only after local completion/review should MA request separate server resumption.

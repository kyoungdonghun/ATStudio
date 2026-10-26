---
version: 1.0
last_updated: 2026-10-21
project: ATS
owner: PG
category: reference
status: completed
---
# Evidence Pack: WI-20261021-ATS-011

## 2026-10-23 Recovery Review Addendum (Current Candidate)

**LOCAL_ALLOW_API_PREPARATION_ONLY** remains the verdict for current manifest
`3c837f740160d6a54fb3093d75e2148101efe9f67c11b39b615f3086598b48f2`.
This additive section supersedes only the candidate pin and current review status
below; the October 21 receipt and its test history are preserved unchanged.

- [Current dated receipt](../../build/WI011-audio-review/review-receipt-20261023.json)
- [Additional independent checks](../../build/WI011-audio-review/test_resume_independent.py)
- Injected context: Tier 0 principles/documentation/development/glossary and Tier 1
  security boundaries supplied by MA; Tier 2 recovery handoff, current WI013
  implementation/tests/README and previous WI011 evidence read directly.
- Identity freshly verified: all 62 candidate members, all 53 copied dependencies
  against originals and source pins, 643 source inputs, retained JAR, approved WAV,
  retained v8r1 top-level bundle members and WI010 15-file freeze match.
- WI010 freeze remains
  `0d7ae85c4ca3f0425826c2784867ee57169efb96c14cb5d2b315567d8b9bc516`.
- Fresh execution: `python -B build/WI013-audio-native/test_native_audio.py`
  **26 PASS**; `python -B build/WI011-audio-review/test_native_independent.py`
  **4 PASS**; `python -B build/WI011-audio-review/test_resume_independent.py`
  **2 PASS**. The 50 WI010 and 8 contract tests below are historical October 21
  results, not newly rerun results. Identity commands below were freshly rerun.
- Final ledger verifies the derivative path digest against the trial track before
  recording `files-final.json`. Wrong derivative key rejects without a receipt;
  a synthetic receipt write failure prevents the later AUDIO PASS record. The
  saved inventory supplies exact bytes/hash for a later specifically approved
  single-file transfer. Its full inventory is private and is not a transfer asset.
- The README now gives a concrete private-copy/listen/seek operator recipe after
  cleanup; no transfer or playback tool was executed. STREAM HTTP response body
  hash is **not directly compared to the final derivative hash** by this candidate.
  Do not describe HTTP byte checks as proof of that identity or audible playback.
- No new blocking finding for this bounded local API preparation scope. Native
  behavior, account availability, transfer, audible output and browser UI remain
  unverified. A recipe is preparation, not execution or server authorization.

Verification owner: PG recovery independent reviewer; date: 2026-10-23; environment:
local Windows workspace with synthetic test inputs. No implementation edits,
rebuild/reseal, remote contact, credentials, account/DB/service operation or commit.
Rollback: do not use the candidate; preserve prior receipts and trial data.
Next dependency: MA triggers WI012 closeout against this exact current receipt,
then requests separately scoped server/data approval if proceeding.

## Summary

Independent local review of WI010 and WI013: **LOCAL_ALLOW_API_PREPARATION_ONLY**.
This is not a native release receipt, server authorization, human playback acceptance,
or closure of the overall audio goal. Created with create-wi-evidence-pack.

## Scope / DoD Check

- [x] Reviewed source/API, account rights, worker, journal, transport and cleanup contracts.
- [x] Independently verified 643 source-input sizes/hashes, retained JAR and approved WAV.
- [x] Verified final WI010 package and all 62 WI013 manifest members; compared all 53 copied public dependencies with their originals and pins.
- [x] Reproduced 50 WI010 and 25 WI013 tests; added 8 contract and 4 native-adapter adversarial tests, all passing.
- [x] Confirmed local target verbs deny before any host command; altered candidate bytes are rejected.
- [x] Documented account/data proposal and target/human limits.
- [ ] Native execution and audible playback are outside this local approval and remain NOT_RUN.

## Reference Documents (Tier 0-2)

| Tier | Document | Context |
|---|---|---|
| 0 | core-principles, documentation-standards, development-standards, glossary | Injected by MA in handoff/history: approved scope, English documents, bounded implementation and traceability |
| 1 | [Security policy](../../docs/policies/security-policy.md), `.claude/agents/pg.md` | Read directly; secret minimization and access control |
| 1 | quality-gates | Handoff pointer; local synthetic checks must not imply target acceptance |
| 2 | [WI011 handoff](WI-20261021-ATS-011-handoff.md) | Local review ownership and dependency chain |
| 2 | [WI010 handoff](WI-20261021-ATS-010-handoff.md), [WI009 evidence](WI-20261021-ATS-009-evidence-pack.md) | Approved local preparation and previous bounded authentication evidence |
| 2 | [WI010 components](../../build/WI010-audio-trial/README.md), [WI013 integration](../../build/WI013-audio-native/README.md) | Final interfaces, accounts and native limits |

Assignee: PG; task: security/reliability review. Context gaps were resolved by reading
the actual relevant controller, DTO, security, download, audio worker/transactions,
storage mutation coordinator, subscription repository and retained runtime modules.
No claim is made that all referenced policies were freshly read in full by PG.

## Evidence Pointers

- [Final review receipt](../../build/WI011-audio-review/final-review-receipt.json)
- [Independent identity checker](../../build/WI011-audio-review/verify_identity.py)
- [Independent contract tests](../../build/WI011-audio-review/test_independent.py)
- [Independent native-adapter tests](../../build/WI011-audio-review/test_native_independent.py)
- [Earlier component review](../../build/WI011-audio-review/wi010-review-receipt.json): historical intermediate receipt only; superseded by final receipt.

Exact final pins:

| Artifact | SHA-256 |
|---|---|
| WI010 freeze, 15 files | `0d7ae85c4ca3f0425826c2784867ee57169efb96c14cb5d2b315567d8b9bc516` |
| WI013 manifest, 62 files | `5d4b72b6e85993fb50656f3420b1d477589b0aa69d58315b166ee29ed2d73eb4` |
| Retained WI068 JAR | `f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81` |
| Approved WAV | `d7b1011a45c84eb0c0d0308cc744289aa277b8b43ff0a4b97d6c59a1254ffa13` |

## Findings Resolved During Review

1. Retained cleanup uses `cleanup=PASS` and exact ordered `cleanup_steps`, not top-level `status=PASS`. Missing, reordered, duplicate and failed steps now fail local validation; aggregate failure is preserved.
2. Exact delta checks previously compared only changed columns; final checks compare prior/new cell digests. Source manifests/freezes are pinned before trusting members.
3. Initial WAV conversion creates two CREATE and two DELETE journal records. Both DELETE records retain the still-referenced original (`SHARED_REFERENCE_RETAINED`). Intermediate generated PREPARED rows can precede track derivative linkage.
4. Bridge now binds action/method/path/actor and upload track, supports bounded standard chunked responses, rejects ambiguous framing, checks negative error/media leakage, and observes uncertain sends.
5. Existing-account login happens within the same runtime; no stale-token prerequisite, token minting or repeated signup/mail flow. Actor `/me` checks precede upload. Login snapshots compare the hash of the stored refresh digest, matching AuthService plus SQL hashing.
6. Actual worker master flag must be true with audio-only=true and worker=true. Native FFmpeg argv has 36 elements for the pinned fixture; source binding handles the worker racing the upload response. Bounded process re-observation handles transient process disappearance.
7. Worker startup recovery and queue claims are global, so quiet pending/claimed work is required before Java release. Existing payment rows remain compared but are not mistaken for queued audio work.

No open blocking finding remains for the reviewed **local API preparation** scope.

## Commands & Tests

Run from repository root, with Python bytecode writes disabled:

```powershell
python -B build/WI011-audio-review/verify_identity.py
python -B build/WI010-audio-trial/verify_freeze.py
python -B -m unittest discover -s build/WI010-audio-trial -p 'test_*.py'
python -B build/WI013-audio-native/test_native_audio.py
python -B build/WI011-audio-review/test_independent.py
python -B build/WI011-audio-review/test_native_independent.py
```

Results: source 643/643 unchanged; JAR/WAV identical; final suites 50+25+8+4 PASS.
Reviewer tests inject hash changes and synthetic observations without editing
candidate inputs or contacting a server. A test of the in-progress unfrozen tree
correctly returned CANDIDATE_DRIFT; final frozen-tree verification passes.
Verification date: 2026-10-21, local Windows workspace; owner: PG.

## Account / Data Boundary

The implemented flow needs three distinct verified existing principals: ADMIN,
entitled USER and non-entitled USER; anonymous uses no account. Target existence
is unverified. ADMIN download bypass does not prove buyer entitlement. ACTIVE or
CANCELLED subscriptions through their expiry are eligible; a preexisting license
also bypasses the first-download entitlement check.

Future approval must explicitly cover three login refresh-token/updated-at changes,
one inactive upload then activation, source+MP3, four storage journal rows,
one license/download row and download_count+1. No role/profile/entitlement edits,
payment, mail, application data deletion or new account creation is implemented.
If required accounts/entitlement are missing, obtain exact separate approval; the
existing admin correction API does not create a new entitlement.

## Risks / Rollback / Follow-ups

- No SSH/cloud/browser/service/app/DB operation, private input read, rebuild, install,
  deletion or commit was performed. Existing source/JAR/success tools are preserved.
- Target cgroup/nft/BPF, FFmpeg execution, real HTTP/DB/file semantics and cleanup
  remain untested. Local injected tests cannot establish Linux target acceptance.
- The API phase retains the external HTTPS quarantine. MP3/Range bytes do not prove
  audible playback or browser seek. A human playback/delivery route is not implemented;
  it remains a distinct mandatory preparation and acceptance gate.
- Genuine fresh user/PG/target/backup receipts must precede any native operation.
  Root-owned status-shaped JSON is not a substitute for actual approval. This pack
  intentionally is not the target `ALLOW_EXACT_AUDIO_CANDIDATE` receipt.
- Private staged credentials/environment remain root-only retained evidence in the
  inherited lifecycle; future retention/removal needs an explicit operator plan.
- Rollback: do not use the candidate. Preserve existing files/history; no deletion.
- MA should trigger WI012 closeout immediately, preserve the limits above, then obtain
  separate server/data authorization only for a concrete proposed next phase.

---
version: 1.0.0
last_updated: 2026-10-24
project: ATS
owner: se
category: evidence-pack
status: reviewed-local-preparation-complete
---
# Evidence Pack: WI-20261024-ATS-014

## Summary

Prepared a minimal in-memory cold-state and fixed backup-reference probe. It reuses frozen readers and identity checks, exposes only fixed status fields, and has no remote staging, database/service start or cloud operation.

## Scope / DoD Check

- [x] Preserve frozen WI008/WI011 sources and use exact pinned dependencies.
- [x] Enforce the current narrow read-only scope, strict target binding and absolute expiry of at most 600 seconds.
- [x] Inspect six fixed root-private files; compare host credentials/password to their original stage references internally.
- [x] Support optional MA-supplied PC escrow hash through private stdin; publish only match/unchecked status.
- [x] Separate historical repository binding from live identity, cost and current fixture coverage.
- [x] SE6 synthetic checks PASS and final builder creates a valid bounded payload in memory.
- [x] Exact independent PG WI015 review: four checks PASS; no local blockers.
- [ ] Actual server/PC escrow/provider observation: MA only, not performed by SE.

## Reference Documents (Tier 0-2)

Context was injected by MA through the [handoff](WI-20261024-ATS-014-handoff.md).

| Tier | References | Purpose |
|---|---|---|
| 0 | `docs/standards/core-principles.md`, `documentation-standards.md`, `development-standards.md`, `glossary.md` | Approval boundary, privacy, English technical metadata and minimal reuse |
| 1 | `docs/policies/security-policy.md`, `quality-gates.md`, SE role | No secret output; focused verification |
| 2 | `docs/index.md`, REQ-20261011-ATS-007, WI013 final evidence | Current approved decision context |
| 2 | `build/WI20261024-008-readonly-evidence/make_payload.py`, `account_operator.py` | Successful stdin bootstrap and current cold/source/JAR gates |
| 2 | `build/WI20261024-011-backup-preparation/lib/bootstrap.py`, `lib/restore_only.py`, `adapter.py` | Secure readers and established stage/repository/receipt references |

Format follows `.agents/skills/create-wi-evidence-pack/SKILL.md`; context rule source is `.claude/config/context-injection-rules.json`, as injected by MA.

## Evidence Pointers

- [Candidate manifest](../../build/WI20261024-014-backup-preflight/candidate.json): SHA-256 `e7b69cc9e44abbbac971621160285f2c55a22111729420ff3b6b0196a147eb19`, five members.
- Bundle `484de7a31f8fd09b62a75b0eabcbcd99ae760c8fe0be7bd7d76b7460497e66c0`: SHA-256 of the compact sorted JSON filename/hash map.
- [preflight.py](../../build/WI20261024-014-backup-preflight/preflight.py): authority, six fixed reads, secret-free projection and independent final cold observation.
- [make_payload.py](../../build/WI20261024-014-backup-preflight/make_payload.py): four retained dependency pins plus the old builder pin, five exact bootstrap replacements, optional PC-pin stdin and exclusive local payload output.
- [README](../../build/WI20261024-014-backup-preflight/README.md): MA probe/inspect/postprobe procedure, strict retained SSH argv and exact limitations.
- [Tests](../../build/WI20261024-014-backup-preflight/test_preflight.py) and [output](../../build/WI20261024-014-backup-preflight/test-output.txt).

The prior WI008 candidate remains `c4c2162bfb9f368bf98bffa621d798f4d3483dad9756902d5794aa637f574188`; WI011 remains `158d2bb3064d112e625b7b8db914925636fe9b72f68161671da862d84dc19839`. All four reused source bytes and the old builder are checked against fixed hashes during payload preparation. No prior candidate was edited.

## Commands & Tests

Observed local Windows checkout: branch `main`, HEAD `91a16e72693d7cd6ed04e0d36a5bd160af96f6d8`. Existing unrelated tracked changes were preserved. Actual interpreter: `%LOCALAPPDATA%/Python/pythoncore-3.14-64/python.exe`.

```text
<actual-python> -B -m unittest discover -s build/WI20261024-014-backup-preflight -p test_preflight.py -v
```

SE6 PASS: positive fixed-file projection and optional PC comparison; host/repository/receipt/PC mismatch rejection; strict scope/target/expiry; final cold observation on reference failure and drift; exact dependency/bootstrap composition; mandatory inspect target and optional-pin mode restrictions. The final manifest-checked builder also generated and compiled a probe payload in memory under the 256KiB cap. No payload was transmitted and no real input was read. Product builds and historical suites were not run.

PG WI015: four independent checks PASS, including expiry before private reads and preservation of final cold facts after expiry. [Exact review receipt](../../build/WI20261024-015-preflight-review/review-receipt.json), SHA-256 `818f3015fb2bd1d11112e1e32030092420da1b01b8cdb2f371e464a5a02c80d0`, verdict `ALLOW_EXACT_USER_APPROVED_READONLY_PREFLIGHT_ONLY`. PG verified five candidate members, four helper pins, the retained builder, historical reference constants and all 19 prior WI011 members unchanged. SE6 was not rerun by PG. Source/JAR identity evidence is reused from WI012, not a newly executed source/JAR suite; actual target identity is checked by the probe at dispatch.

## Risks / Rollback

A host password hash matching its historical stage, or a supplied PC byte hash, is reference consistency only. It does not establish repository decryption, PC recovery success or full disaster recovery. The historical installation receipt does not claim restic was never executed. Historical repository/result identity is not proof of current cloud contents or current fixture coverage. Provider fees/allowances are not checked by this probe.

The initial probe's boot and machine hash are private transport bindings, not public status fields. Any optional escrow-bearing payload and its whole-payload hash remain private. Errors are fixed codes; raw stderr and metadata bodies must not be published. Expiry prevents further inspection but never suppresses the final read-only cold observation. No service mutation requires cleanup; MA separately restores its approved access.

Rollback is to decline this candidate and retain evidence. No automatic retry, staging deletion or operational recovery is implemented. A HOLD requires a bounded diagnosis before further action.

## Follow-up

WI014 blocked WI015; the frozen candidate was sent immediately and its exact review is complete. MA owns all actual target/provider observations and the next chain. This local completion grants no backup, restore, DB, app, login or audio authority.

---
version: 1.0.0
last_updated: 2026-10-24
project: ATS
owner: SE
category: evidence
status: review
---
# Evidence Pack: WI-20261024-ATS-001

## Summary

Prepared the minimal saved-input, phase-specific approval wrapper for the currently approved 2026-10-24 TEST fixture operation. Implementation and synthetic verification only; MA retains all remote execution authority.

## Scope / DoD Check

- [x] New candidate only; frozen WI005 transaction/native/crypto and WI011 helpers reused without modification.
- [x] Exact external record raw-byte pin, input, wrapper/frozen candidate, target, day, plan 1 quota 5, TEST semantics and effect binding.
- [x] Fresh read-only inspection plus five cleanup PASS gates before one private plan and at most one apply call.
- [x] Exclusive new operation/phase authority paths; prior fixed approval files and evidence preserved; native globals restored in finally.
- [x] Failed inspection, expiry/day rollover, drift or uncertain return prevents further writes. Known ACK/IDs survive later evidence failure.
- [x] Seven focused synthetic tests passed; public output excludes private values/digests/snapshots/errors.
- [x] Runnable builder, exact record schema, strict SSH stdin recipe and independent cold post-probe/restoration instructions supplied before human setup.
- [ ] Independent exact allowance: successor WI-20261024-ATS-002 (PG).
- [ ] Actual native execution/restoration: MA only, after exact allowance and fresh target inspection.

## Reference Documents (Tier 0-2)

Injected context from [the generated handoff](WI-20261024-ATS-001-handoff.md), not an inferred new authorization:

| Tier | Documents | Application |
|---|---|---|
| 0 | `docs/standards/core-principles.md`, `development-standards.md`, `documentation-standards.md`, `glossary.md` | Approved scope, Korean conversation/English documentation, bounded evidence |
| 1 | `docs/policies/security-policy.md`, `quality-gates.md` | No private output; exact review and fail-closed cleanup |
| 2 | `docs/index.md` Current Decision Entry; `deliverables/user/REQ-20261011-ATS-007.md`; `WI-20261023-ATS-013-evidence-pack.md` | Current approval and historical inspection context |
| Code | `build/WI20261023-005-account-fixture/`; `build/WI20261023-011-fixture-inspect/` | Preserved planner, transaction, native lifecycle, secure file and local ACL helpers |

Injection rule source: `.claude/config/context-injection-rules.json`; assignee SE; security/native wrapper task; Tier 0-2 supplied by MA/handoff. Historical native inspection was navigation/context only; this wrapper requires a fresh inspection.

## Evidence Pointers

Candidate: `build/WI20261024-001-fixture-apply/` (five members plus manifest).

- `flow.py`: exact record validation; sanitized result; immutable phase authority binding; inspect/plan/single-apply sequence.
- `remote_apply.py`: verified frozen staging, cold source identity, exclusive root-owned operation evidence, preserved native calls.
- `make_payload.py`: owner-only local input loading, original record-byte pinning, verified executable payload and private exclusive output.
- `test_flow.py`: seven bounded synthetic regressions, including real temporary exclusive phase-file storage and finally restoration.
- `README.md`: required fields, commands, private/public evidence, uncertain-commit handling and remaining gates.

Manifest SHA-256: `f42d9cfe325f8105d908db46f67b5fa93bc72ada6178eb878af8f75a2f5f7719`.

Bundle SHA-256: `5fedfa3187ed83637b31bf5413369ee86a89a6fc8fc45d8c1307704312b3d262`.

Frozen WI005 manifest SHA-256: `3a0f4fd30afc012a8e1a0c46d70b87ed1d2ef5492c2f8143121dc741da35654c`. The synthetic builder verified all 22 members and exact tree. Frozen WI011 manifest SHA-256: `21daba86032674c87844d9a584fee5ec42e3c689db806e53c0a961ebfa407245`; all five members verified, with the prior non-executable `test-output.txt` retained and never imported. MA separately reported current source/JAR/WI014 preservation checks; SE did not inspect actual private files or execute remote commands.

## Commands & Outputs

```powershell
& "$env:LOCALAPPDATA/Python/pythoncore-3.14-64/python.exe" -B -m unittest discover -s build/WI20261024-001-fixture-apply -p test_flow.py -v
git diff --check -- build/WI20261024-001-fixture-apply
```

Observed 2026-10-24 on the local Windows workspace, owner SE: 7 tests PASS; scoped whitespace check returned exit 0. No project build/full auth/audio suite, app launch, network or database operation was executed. PG identified one pre-freeze builder usability defect: the prior WI011 non-executable test output was outside its manifest. The wrapper now allows exactly that existing excluded filename while pinning every imported/executed file; the synthetic builder passed afterward. Final review also restored the prior SSH Python `-I -S -B` isolation options in the README only; the final manifest above supersedes the earlier draft pin.

## Risks / Rollback

Synthetic success does not prove current remote inputs/collisions, actual DML or cleanup. Native receipts and an independent cold post-probe remain required. The approved day is 2026-10-24 KST only; no automatic extension. Existing row drift or any missing gate stops the sequence.

After a possible COMMIT or lost response, preserve private plan/claims/snapshots/commit evidence and stop writes. Do not rerun, delete claims, infer non-application from rollback, or mutate existing rows. Separately authorized read-only reconciliation is the next decision. Runtime cleanup remains independent of date/authority expiry. Before execution, withholding this candidate is the local rollback; no retained source/JAR/core/native edits need reversal.

## Follow-up

WI-20261024-ATS-002 was notified immediately with the exact frozen manifest and SE test result. MA may prepare the externally attested decision record using the README schema; no new human credential collection is needed. Actual native execution is not an SE completion claim.

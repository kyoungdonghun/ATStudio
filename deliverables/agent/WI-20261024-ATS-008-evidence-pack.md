---
version: 1.0.0
last_updated: 2026-10-24
project: ATS
owner: se
category: evidence-pack
status: reviewed-local-preparation-complete
---
# Evidence Pack: WI-20261024-ATS-008

## Summary

Prepared a minimal successor to the successful read-only lifecycle for exact actors 1/2/3, grant metadata and four fixed historical backup records. No native execution or private input access occurred.

## Scope / DoD Check

- [x] Fresh unit/log identity and current explicit read-only scope; absolute expiry and exact target binding.
- [x] SELECT-only bounded account/plan/grant projection without credential/profile fields.
- [x] Four-file backup metadata allowlist, historical proof binding and honest current-coverage limit.
- [x] Existing start/containment/finally cleanup preserved; local focused tests pass.
- [x] Exact candidate and MA commands sent to PG WI009 before human unlock.
- [x] PG exact allowance received; independent 6 tests and SE 8 tests PASS.
- [ ] Actual native observations and cleanup: MA only, after independent review.

## Reference Documents (Tier 0-2)

Injected context follows [handoff](WI-20261024-ATS-008-handoff.md):

| Tier | References | Purpose |
|---|---|---|
| 0 | `docs/standards/core-principles.md`, `development-standards.md`, `documentation-standards.md`, `glossary.md` | Scope, preservation, language and evidence rules |
| 1 | `docs/policies/security-policy.md`, `quality-gates.md`, `.claude/agents/se.md` | SE/security boundaries |
| 2 | `docs/index.md`, REQ-20261011-ATS-007, WI20261024-003/007 Evidence Packs | Current approved context |
| 2 | WI-20261011-ATS-062 Evidence Pack, October 17 completion addendum | Historical successful restore pointer and limits |
| 2 | `build/WI20261023-008-readonly-recheck`, `build/WI069-backup/restore_only.py`, `restore_equivalence.py` | Proven lifecycle and exact metadata contracts |

Role/context source: `.claude/config/context-injection-rules.json`, as injected by MA. Evidence format: `.agents/skills/create-wi-evidence-pack/SKILL.md`.

## Evidence Pointers

- Candidate: [candidate.json](../../build/WI20261024-008-readonly-evidence/candidate.json), SHA-256 `c4c2162bfb9f368bf98bffa621d798f4d3483dad9756902d5794aa637f574188`.
- Seven listed members; bundle `5df8f803c893964a90ac31b1072a926632f5300a20aa67e13baca366ee1f1527`, SHA-256 of canonical sorted compact JSON file-hash map.
- [README](../../build/WI20261024-008-readonly-evidence/README.md): exact scope, four metadata paths, probe/inventory/postprobe recipe, strict SSH argv and cleanup interpretation.
- [account_operator.py](../../build/WI20261024-008-readonly-evidence/account_operator.py): only import, unit, scope and metadata-result attachment differ from successful old operator.
- [inventory.py](../../build/WI20261024-008-readonly-evidence/inventory.py): four SELECTs in a read-only snapshot; explicit actor/grant/plan bounds.
- [backup_evidence.py](../../build/WI20261024-008-readonly-evidence/backup_evidence.py): secure fixed reads and safe historical/current projection. No backup/restore invocation.
- [make_payload.py](../../build/WI20261024-008-readonly-evidence/make_payload.py): six verified source bodies, retained runtime pins and absolute expiry.

## Commands & Tests

Actual local Windows Python: `%LOCALAPPDATA%/Python/pythoncore-3.14-64/python.exe`.

```text
<python> -B -m unittest discover -s build/WI20261024-008-readonly-evidence -p test_readonly.py -v
```

SE result: **8 PASS**, [frozen log](../../build/WI20261024-008-readonly-evidence/test-output.txt). Covers bounded grants/SQL, invalid/private fields, fixed read contract, original proof/restore cleanup binding, same-count nonacceptance, exact lifecycle delta and source/expiry/target guards. The old WI20261023-008 candidate's listed member hashes were rechecked unchanged. No full project suite, JAR build, app, DB, SSH, backup or credential input was run by SE.

PG WI009 verdict: `ALLOW_EXACT_APPROVED_READONLY_ACCOUNT_GRANT_BACKUP_METADATA`. [Review receipt](../../build/WI20261024-009-readonly-review/review-receipt.json), SHA-256 `336c49ed2f65fcb78a1c8dbf2156830c436300b3ec6cc41813df78f7316cef8b`. PG independently passed 6 tests and reran SE's 8. It also verified source643, retained JAR and old candidate5 identities, and matched original backup pins to retained source. All review work was local; actual target results remain pending MA execution.

## Risks / Rollback

Metadata existence or a historical recorded PASS never establishes current fixture backup coverage. Only the four known historical records are checked; no absence claim about other backups is made. Native metadata and SQL compatibility remain MA observations. A metadata HOLD is reported independently from account-inventory success.

No existing tool or product file was changed. Do not deploy this candidate if review fails. After any possible DB start, inherited independent STOP/PID_ZERO/SOCKET_GONE/BASELINE checks remain mandatory, followed by exact-target postprobe and MA access restoration. No automatic retry, log deletion, old-attempt overwrite or restic operation is authorized.

## Follow-up

Blocks WI-20261024-ATS-009. Candidate was handed to PG immediately after freeze; MA owns subsequent approved native execution and dated completion evidence.

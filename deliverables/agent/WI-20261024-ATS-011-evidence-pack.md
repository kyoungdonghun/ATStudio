---
version: 1.1.0
last_updated: 2026-10-24
project: ATS
owner: se
category: evidence-pack
status: reviewed-local-preparation-complete
---
# Evidence Pack: WI-20261024-ATS-011

## Summary

Prepared and independently reviewed an inert-by-default existing-repository capture/isolated-verification adapter. No server, private input, backup, restore or business mutation was performed.

## Scope / DoD Check

- [x] Reuse 11 unchanged successful helper files; preserve all frozen originals and product artifacts.
- [x] Replace first-capture/init and old artifact baseline with exact existing repository plus measured current roots and complete SQL proof.
- [x] Require future exact user decision, target/inventory, existing escrow and independently verified zero-additional-paid-cost evidence.
- [x] Provide working default plan, gated inventory/capture entry and strict SSH-stdin payload builder.
- [x] Preserve independent cleanup and unknown snapshot outcomes without replay.
- [x] Document exact grant-date proposal only; no grant DML or invented product approval.
- [x] SE10 and independent PG6 focused tests PASS; exact local-preparation allowance received.
- [ ] Native execution/current recovery acceptance: not authorized or performed by this WI.

## Reference Documents (Tier 0-2)

Injected by MA through [handoff](WI-20261024-ATS-011-handoff.md):

| Tier | References | Purpose |
|---|---|---|
| 0 | `docs/standards/core-principles.md`, `documentation-standards.md`, `development-standards.md`, `glossary.md` | Approved scope, privacy, language, minimal reuse and traceability |
| 1 | `docs/policies/security-policy.md`, `quality-gates.md`, `.claude/agents/se.md` | SE/security boundaries |
| 2 | `docs/index.md`, REQ-20261011-ATS-007, WI069/WI070/WI20261024-010 Evidence Packs | Current state and accepted historical boundaries |
| 2 | `build/WI069-backup/restore-only-handoff-20261017.md`, retained capture/restore sources | Proven mechanics, exact repository/escrow and strict equivalence |
| 2 | `build/WI20261024-readonly-evidence-native/resume-inventory-600-public.json` | Actual prior safe grant/account observations |

Context rule source: `.claude/config/context-injection-rules.json`, as injected by MA. Evidence format: `.agents/skills/create-wi-evidence-pack/SKILL.md`.

## Evidence Pointers

- [Candidate](../../build/WI20261024-011-backup-preparation/candidate.json): **`158d2bb3064d112e625b7b8db914925636fe9b72f68161671da862d84dc19839`**, 19 members.
- Bundle: `57ff70762fcb531a8ba3669a12fb33f120184931dcf6d4655988681e2bf4680f`, SHA-256 of canonical sorted compact JSON member-hash map. Earlier 738ea716 candidate was superseded before allowance by the grant-date wording correction; no execution occurred.
- The previously reviewed [218733ad manifest](../../build/WI20261024-011-backup-preparation-history/candidate-218733ad0e005b01deff8417aa136d8406417604f63c5c3b7d85d40355900041.json) is preserved outside the candidate. The final change corrects README only: actual column metadata is observed/compared and retained in the private verification workspace, not included in the snapshot payload. The other 18 member hashes are unchanged; no code or suite was rerun.
- [README](../../build/WI20261024-011-backup-preparation/README.md): exact execution path, future record schemas/commands, actual cost-proof method and current limits.
- [backup_plan.py](../../build/WI20261024-011-backup-preparation/backup_plan.py): default-deny authority, raw external decision/cost pins, initial inventory null-pin contract and zero-paid-cost checks.
- [adapter.py](../../build/WI20261024-011-backup-preparation/adapter.py): existing repository anchor, current roots/actors/grant, complete SQL/new-snapshot verification and independent cleanup.
- [entry.py](../../build/WI20261024-011-backup-preparation/entry.py), [make_payload.py](../../build/WI20261024-011-backup-preparation/make_payload.py): inert default and executable exclusive staging/dispatch through the retained strict MA transport.
- [grant-date proposal](../../build/WI20261024-011-backup-preparation/grant-date-proposal.md): preserve start date by default; any exact expiry/audit mutation needs a separate approved implementation. Date fields do not enforce future activation or one-day use.

## Commands & Tests

Actual local Windows Python: `%LOCALAPPDATA%/Python/pythoncore-3.14-64/python.exe`.

```text
<python> -B -m unittest discover -s build/WI20261024-011-backup-preparation -p test_preparation.py -v
```

SE: **10 PASS**, [frozen output](../../build/WI20261024-011-backup-preparation/test-output.txt). One initial test compared a Windows Path with a Linux literal; correcting the test expression to the constructed argv resolved it without changing native behavior. Tests cover positive/negative future contracts, unknown/nonzero/stale cost evidence, no-init/delete/original-restore admission, successful and failing independent cleanup, raw decision preservation and unchanged dependencies. Full historical suites and product builds were not rerun.

PG WI012: **6 independent PASS**, including complete injected stage flow and rejection of a changed SQL cell despite equal counts. [Exact receipt](../../build/WI20261024-012-backup-review/review-receipt.json), SHA-256 **`f138d628e36fbccc6555e43de666047a152cfaa02e8444798cf78c6dda96d013`**. Verdict `ALLOW_EXACT_LOCAL_PREPARATION_ONLY`, native false. PG verified all 19 members, 11 original/copy identities, source643 and retained JAR unchanged. The SE10 were not independently rerun by PG; its six tests are separate.

Final documentation-only rebinding: [additive review receipt](../../build/WI20261024-012-backup-review/review-receipt-final.json), SHA-256 **`c6307fa7a5ccb539480eef388ec765fe2d02419f743078e3cfd6c0189c08f534`**, binds candidate158d2bb and bundle57ff707 with the same local-only verdict. The original receipt remains unchanged. Existing SE10/PG6 results are reused; suite reruns: zero.

## Risks / Rollback

Current fixture recovery remains NOT_ESTABLISHED. This candidate verifies a newly captured snapshot when later authorized; it excludes repository-wide health checking, runtime-secret escrow expansion and full disaster recovery. Source DB startup/shutdown can perform internal engine bookkeeping even with logical read-only flags; only the separately authorized isolated mount receives SQL import.

Additional paid budget defaults to 0 KRW. Actual prices/account allowances/requests/egress/tax and continuing retained-storage cost are unverified. Local rates, byte/time guards and MA record booleans are not provider billing enforcement. Real source-backed bounds must be established later or capture remains HOLD. No provider or cloud call was made now. No automatic deletion at the planning horizon is assumed.

The grant's current observed dates remain October 24; changing them is proposal-only. A separate trial gate must halt outside its approved KST day because `started_at` is not an activation filter. No product correction/consent/payment/mail event was fabricated.

Do not admit the candidate if future gates fail. Preserve any partial stage/run/snapshot. A possibly created snapshot or lost return requires separately authorized read-only reconciliation, not replay/deletion. Inherited finite units plus independent STOP/PID_ZERO/SOCKET_GONE/COLD_BASELINE remain the cleanup boundary; MA must separately re-observe cold state and restore access after any eventual approved run.

## Follow-up

WI011 blocked WI012; the exact candidate was handed off immediately after freeze and PG review is complete. MA owns the next documentation chain and any later concrete approval request. No native operation is triggered by this local completion.

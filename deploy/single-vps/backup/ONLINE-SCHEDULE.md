---
version: 1.1
last_updated: 2026-10-30
project: ATS
owner: SE
category: guide
status: draft
---
# Finite online scheduling connection — locally reviewed, native pending

WI012 adds `scripts/deployment/backup/online_scheduled_dispatch.py`; WI013 independently reviewed the WI014 corrections. It is a separate adapter; the cold dispatcher and disabled systemd templates remain unchanged. No service/timer is installed or enabled. Local independent review is complete; native acceptance remains pending.

## Interface

Default `python -B online_scheduled_dispatch.py` returns DISABLED without reading inputs or executing capture. The Linux/root-only execution interface accepts `--execute --config <private-json> --approval <private-json> --policy <private-json> --pins <private-json> --state <private-directory>`.

This interface is documentation, not an instruction to execute it now. Native state must be exactly `<config.state_dir>/online-scheduled-ledger`, owned by the invoking root and mode0700. Input references must pass existing private/trusted-path checks. Native pins are the exact absolute paths and SHA256s of Python, this adapter, `scheduled_dispatch.py`, `online_capture.py`, `backup_runtime.py`, and `backup_protocol.py`.

Policy fields:

| Field | Meaning |
|---|---|
| version / enabled / operation | `1`, explicit `true`, `daily-online-dispatch`; absent approval remains inert |
| policy_id | Stable identifier, 1–64 alphanumeric/underscore/hyphen characters |
| timezone / start_hhmm | Asia/Seoul and approved HH:MM; no default production schedule |
| start_grace_seconds / window_seconds | 1–60 second start grace; grace < window <=1800 and <= capture limit |
| not_before / expires_at | Explicit finite timestamps, maximum366days; window must fit one KST day |
| config_subject_sha256 | Existing online `config_subject(config)`; only proof references are excluded |
| pins_sha256 | `digest(encoded(pins))`; changes do not reset prior consumption |
| budget | Integer max_attempts, per_attempt_bytes, per_attempt_cost_krw, existing_bytes, reserve_bytes, capacity_bytes, cost_cap_krw |

The expected run ID is `digest(encoded([policy_id, KST-date, "daily-online-dispatch"]))[:32]`. Exact online approval must already carry this ID, the current complete config hash, the same window and release authorization. The adapter consumes it; it does not generate it. Each attempt still requires freshly truthful target/repository/recovery/cost proofs under `online_capture.execute`, including prior-state binding and the shared operations lock. No timestamps, evidence, budget observations or authorizations are automatically renewed.

## Accounting and outcomes

Before claiming, reserve existing+reserve+(consumed+1)*full-per-attempt bytes and (consumed+1)*per-attempt cost. This deliberately preserves conservative accounting; it is not a new low-cost monthly policy or an issuer of current provider/storage/cost evidence. The online executor's separate current cost proof remains mandatory. Current tariff/accounting refresh and storage costs continuing after captures stop remain operator responsibilities.

A persistent policy-bound ledger rejects duplicates, concurrent locks, policy changes and exhausted bounds. An exception or invalid receipt after a claim retains claim+lock and blocks subsequent days. Never remove the lock, recreate the ledger, change policy identity or reset a claim merely to retry. Reconciliation is outside this adapter and needs actual outcome evidence.

WI014 hardening: every prior claim must have exactly identity/run/config-hash/approval-hash fields, valid lowercase hash formats and the current policy identity. Every prior result must exactly bind COMPLETE and false restore verification to the canonical capture UUID derived from that claim's run ID. Corrupt or incomplete history is rejected before any successor is admitted; historical records are never repaired or migrated automatically.

Only the fixed online executor is used by native CLI. Its COMPLETE/capture UUID/restore NOT_RUN response is accepted; the cold receipt is rejected. Dispatcher success remains `restore_verified=false`. The online executor retains actual snapshot evidence, last-good handling and resource/signal cleanup. The dispatcher does not create a second supervisor, mail reporter, snapshot or restore operation.

WI014 binds the exact already-validated approval across the executor's file reread: the dispatcher passes its canonical approval SHA256, and `online_capture.execute` checks the reread object against it before authority validation or host access. A changed same-run window cannot silently widen the scheduler's authority. Existing standalone calls remain compatible through an optional keyword argument; their original exact online approval validation remains in force.

WI014 also rejects import/source ambiguity before native input validation and immediately before invoking the executor. The adapter and its four project dependencies must originate from the exact reviewed `.py` files. Existing `__cached__` paths (including a separate Python cache prefix) or adjacent legacy `.pyc` files produce HOLD; caches are not deleted automatically. Python `-B` prevents new bytecode writes, not reads of existing bytecode. Use a reviewed, root-owned, immutable deployment bundle without caches; any supervised cleanup/redeployment is a separate operation. Imports must remain side-effect-free. This post-import guard is not a malicious-code sandbox or protection for hot replacement of already loaded source.

## Evidence and next work

Windows author tests after WI014:20 dispatcher/handoff/cache cases PASS; the preceding R1/R2 revision also passed33 existing cold-dispatch/online-capture tests. New tests verify finite wiring and failure preservation with a fake executor. Handoff cases use actual approval checking with mocked path/identity boundaries and stop before host access; cache unit tests use synthetic module metadata. They do not prove native Linux/root execution or live DB/systemd/cloud operations. Existing online proof tests exercise real validation with protected-path mocks and synthetic JSON. WI013 owns independent acceptance and the real stale-bytecode subprocess regression; see its evidence before claiming review completion.

WI013 completed local independent review with60unique tests passing (20author,33related,7independent), including a real local stale-bytecode subprocess reproduction and no native server effects. Next is the separately scoped result reporter. An input assembler/current-observation source and finite native trial remain separately required. Do not enable unattended scheduling merely because this local suite passes. See [WI013 evidence: retained local evidence](../../../docs/registry/git-checkpoint-20261106.md#retained-evidence-limits) and historical [WI012 evidence: retained local evidence](../../../docs/registry/git-checkpoint-20261106.md#retained-evidence-limits).

## Public checkpoint evidence boundary

This public copy redirects unavailable local evidence links to the shared
checkpoint boundary. The original document bytes and detailed REQ/WI evidence
remain in private local custody. No runtime contract or source code changed.

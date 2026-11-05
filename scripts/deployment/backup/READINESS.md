# Offline readiness bridge (WI-20261026-ATS-013)

`python -B scripts/deployment/backup/readiness_plan.py` is inert. With `--stdin`,
it reads at most 128 KiB of declaration-only JSON and prints fixed diagnostics
and aggregate bounds. Exit 2 means activation is held. It never opens a referenced
file, sends an alert, issues approval, installs a timer, or calls a native tool.
Use the synthetic fixture in `test_readiness_plan.py` as the input schema example;
never put passwords, keys, email addresses or provider identifiers in this input.

All input references and timestamps are **unverified declarations**, including
caller-supplied claims of observation. There is deliberately no activation-ready
output. Actual receipts need independent validation outside this planner. Missing,
future and stale declarations are reported early, but passing those checks does
not authenticate receipt contents. The empty-state call to `status_report` is a
protocol illustration, not an assertion that WI012 lacks a successful snapshot.

## Reused protocols and bounds

- Optional `schedule` contains existing config/policy, `planned_start`, and integer
  `retry_allowance`. It uses `validate_config` and `recurring_approval` at that
  planned time, discards the derived approval, and requires expiry within budget.
- WI016 updated generic config to accept known numeric zero projected/approved
  costs; null, negative, boolean and string costs remain rejected. This is protocol
  compatibility, not verification of actual tariffs. Its persistent dispatcher is
  documented in deploy/single-vps/backup/SCHEDULE.md. Successful bounded WI012
  tools remain separate and unchanged.
- Capacity is existing bytes + reserve + maximum attempts * per-attempt new bytes.
  Attempts include retries and failures. No deletion, deduplication or compression
  is assumed. Per-attempt bound must cover selected full recovery sources and any
  independently retained copies; it cannot reuse the narrower WI012 bound blindly.
- Storage, request, retrieval, transfer and tax KRW fields are **total horizon
  upper bounds**, not rates. All must be known nonnegative integers, backed by a
  cost evidence reference. Round fractional KRW upper bounds upward, never down;
  this checker is not a tariff calculator. Included allowance is not a hard billing stop.
- Budget horizon is finite, at most 366 days. Daily slot count is conservative;
  this is a plan bound, not an enforced persistent attempt ledger.

## Real dependencies still held

WI012 already verified one current-state backup and exact-snapshot isolated
restore (43 tables/25 rows). This does not establish full configuration/key
recovery, recurring operation or independent alert delivery.

1. Inventory required runtime settings, current Gmail/Toss integration references,
   DB access, JWT material and **all retained billing encryption keys**, not merely
   the active key. Verify recoverability using protected references without public
   contents. NCP is not active. DPAPI tied to the original PC is not sole portable
   recovery. The planner's six category references are a nonexhaustive inventory
   checklist, not proof of key completeness or successful decryption. Actual full
   recovery also needs selected TLS, proxy, systemd/service, mail/OAuth and Toss
   configuration plus key-retention metadata as applicable to the deployed system.
2. Reuse existing repository/password escrow where latest evidence supports it.
   Verify independent credential custody and a new isolated recovery destination.
   The client's Google Drive is a candidate only: owner consent, free quota,
   credential recovery and transfer/request costs remain unverified. Independently
   held repository passwords and OAuth recovery on a fresh client, including
   effective scopes, need verification; a folder ID is not an access-security
   boundary. No Drive
   access, connector, install or upload was performed or authorized by this check.
3. Establish refreshed full-source and aggregate horizon bounds, actual schedule,
   expiry, retry ledger and fail-closed admission before each capture. The earlier
   91,570,225-byte single-run bound would be 2,747,106,750 bytes for 30 attempts
   before existing storage/reserve; it is not current billing or a full-DR bound.
4. Implement/verify real host-specific hooks and independent alert transport.
   Reuse `existing-ID1-mailbox` and current Gmail; no new recipient/account request
   is needed. An independent monitor must detect host outage and missed schedules,
   deliver a failure notification and retain acknowledgement evidence. Local status
   output and an acknowledgement reference alone do not prove delivery.
5. Independently validate real receipts and protected credential restoration on
   an exact isolated target with outbound access denied and schedulers stopped.
   Keep source DB untouched. Any new cost requires user approval before execution.

No generic timer/calendar was activated, and no successful capture executor changed.

# Persistent scheduled dispatch (WI-20261026-ATS-016)

## October28 local v2 integration (WI-20261028-ATS-003)

The disabled service template now calls `scheduled_dispatch.py`, not the guarded
runner directly. It names protected config/policy/budget/pins/manifest references
and the fixed `config.state_dir/scheduled-ledger`. Its start deadline is1080s and
owned stop allowance35s, matching the prior900s capture envelope; these template
values need target review before installation. The timer still has no calendar or
installation section. No service/timer was installed or run.

V1 fixed-config input remains supported. V2 is opt-in with `policy.version=2`:
- `policy_id`: stable1–64 alphanumeric/underscore/hyphen identifier;
- `config_subject_sha256`: canonical config hash excluding only
  `cold_host.evidence`; requires cold-preserve. Boot, target, roots, credential
  references, code hashes, limits and state paths remain in the fixed subject;
- `code_pins`: exact path/hash map for Python, guarded executor, protocol/runtime
  and dispatcher. Existing timing/expiry/daily-cold-capture fields still apply.

The v2 budget replaces v1 config/argv/pins digests with `policy_id`,
`config_subject_sha256` and exact `policy_sha256`. All finite budget/window fields
remain unchanged. The ledger continues to pin the entire budget hash. New proof
references do not change that identity. Policy/boot/budget changes HOLD against
the existing ledger; no migration/reset/automatic carryover is implemented.
Reconciliation must preserve consumed allowances and historical claims. A new
directory is not a permitted workaround for an exhausted or uncertain ledger.

Each protected `--manifest` has exactly these fields:
`version:1`, `policy_id`, `run_id`, `config_sha256`, `policy_sha256`,
`argv_sha256`, `pins_sha256`. Digests use canonical `backup_protocol.encoded`.
The run ID derives from stable policy ID and KST date; renewing a proof on the
same day cannot mint another attempt. The approval still binds the exact current
config digest. Every supplied pin is checked, mandatory runtime code pins must be
inside fixed policy authority, and the manifest binds the complete current argv
and pin set. Native argv is constructed by the CLI, never supplied as an arbitrary
command in the manifest. Changed config outside proof references, old-day manifests,
tampered code, duplicate slots and expired/late windows HOLD before dispatch.

This is an input contract, not an observation issuer. The custodian must supply
fresh protected actual host/repository/source/cost evidence before each run; signing
or regenerating a manifest does not establish those facts. The unchanged host
adapter checks protected proof hashes, source inventory, identity and at most6h
validity before effects. No automated provider observation, live stop/resume,
remote transport or authorization to update credentials is implied.

For a failed/unknown run, retain `dispatch.lock`, `identity.json` and all attempt
claims/results. Read the fixed `ledger_failure_code` alongside original capture
state/receipt and owned-unit evidence. Only the existing bounded owned cleanup
may stop the attempt's claimed DB. Never remove the lock or reset the budget based
on an exit code alone. Native reboot/power-loss/service-containment proof remains
required during separately authorized deployment.

Local implementation exists; nothing is installed or activated. The prior bounded
WI012 paired executor remains unchanged and is not implicitly schedulable.
The generic guarded runner still requires a real verified host adapter.

## Integration interface

Default: `python -B scripts/deployment/backup/scheduled_dispatch.py` prints DISABLED
and performs no state changes. Explicit Linux/root entry:

```text
python3 -B /opt/atstudio-backup/scheduled_dispatch.py --execute --config CONFIG --policy POLICY --budget BUDGET --pins PINS --state STATE
```

This is an unrendered interface, not an install command. No calendar/service was
changed. Use exact approved absolute protected paths. STATE must equal the existing
config `state_dir` plus `/scheduled-ledger`, precreated private mode0700, never a
fresh directory to reset accounting. Preserve it across failed/successful runs,
config changes, redeployments and policy expiry. A changed policy/budget requires
explicit reconciliation of retained consumption, not directory deletion/renaming.

PINS maps absolute file paths to SHA256: resolved Python executable, this dispatcher,
guarded_backup.py, backup_protocol.py, backup_runtime.py, CONFIG and POLICY. The
fixed command identity tuple is resolved Python, `-B`, guarded_backup.py, `scheduled`, `--config`,
CONFIG, `--approval`, POLICY. The native entry validates this tuple, then calls pinned guarded_backup.execute
in-process. It does not launch a second supervising process. No shell or arbitrary
command template is accepted. Root-owned nonwritable ancestors/private inputs and code pins
must be established before invocation. Trusted root itself is not an adversary
these file checks can defend against.

BUDGET contains canonical-JSON SHA256 `config_sha256`, `policy_sha256`,
`argv_sha256`, `pins_sha256`; timezone-aware `not_before`, `expires_at` (at most
366 days); integers `max_attempts`, `per_attempt_bytes`, `per_attempt_cost_krw`,
`existing_bytes`, `reserve_bytes`, `capacity_bytes`, `cost_cap_krw`, and
`max_output_bytes` (at most1MiB). All costs are known nonnegative integers, rounded
up for fractional upper bounds. Per-attempt cost includes conservative amortized
storage, requests, transfer/retrieval and tax over the entire finite horizon;
do not treat unmeasured components as zero. Per-attempt byte growth must be at
least configured source_bytes + dump_bytes + metadata_bytes; this conservative
sanity floor is not an encoded remote size proof. Fresh external overhead/cost
evidence must justify the full bound, and source limits remain enforced by the
existing capture monitor. Include non-attempt retained cost in
that conservative allocation. `cost_cap_krw` cannot exceed the config authority,
and per-attempt cost cannot understate its projection. Actual tariff evidence is
an external prerequisite, not inferred from numeric zero.

## Durable behavior and recovery

The dispatcher reuses validate_config and recurring_approval, including exact
config, short daily start window, expiry and no catch-up. The fixed policy/budget
identity is durably recorded under an exclusive mkdir lock. Each daily run ID
gets an exclusive attempt directory and fsynced claim **before** dispatch. Every
claim consumes an attempt and conservative byte/cost allowance, including failure.
Same-day replay cannot dispatch again. Counts never rely on success alone.

Any nonzero result is uncertain and retains the lock; it is not proof that no
snapshot was created. Exit0 additionally requires the exact
guarded runner COMPLETE/restoreNOT_RUN/copyNOT_RUN receipt; it does not claim
isolated restoration or full recovery. Unknown outcomes, crash, timeout, excessive
output, malformed success and after-claim errors preserve the lock and claim.
No automatic unlock, claim deletion, catch-up or uncertain retry exists. Diagnose
bounded original host/child/receipt evidence first; if the outcome cannot be proven,
keep HOLD. Any manually authorized recovery must retain consumed budgets and
historical claims. There is no built-in reset/reconcile shortcut.

The existing LinuxRunner bounds runtime through LinuxMonitor's deadline, streams
bounded output, rejects surviving descendants and terminates owned process groups.
The in-process guarded executor is the sole owner of those groups and its
SIGINT/SIGTERM cleanup; there is no nested supervisor.  SIGKILL/power loss require the service's cgroup
containment (`KillMode=control-group`) and later reconciliation. Native Linux
permissions, durability under power loss, process groups and actual host hooks
were **not exercised** by Windows fake tests. No shell/native child ran in those
tests. The symlink rejection test injects the filesystem condition; it does not
certify Windows or Linux filesystem race behavior.

## Alert connection

This ledger is not guarded_backup's `state.json`. Keep original attempt/last_good
receipts for `status_report`. A persistent dispatch lock or missing result after
claim is DISPATCH_UNCERTAIN; historical retained FAILED result is DISPATCH_FAILED. The new dispatcher emits
no confirmed FAILED result from an ambiguous nonzero exit. Use read-only
`ledger_failure_code(root)` for these fixed signals; None does not prove freshness. Feed these
as dispatcher failure signals alongside original capture receipts to the independent
alert consumer. A successful dispatch alone does not establish notification delivery.

Existing successful capture artifacts were not regenerated. Known numeric zero is
now accepted by the shared config protocol; null/bool/negative/string costs remain
rejected. This supersedes READINESS.md's historical zero-cost incompatibility note.

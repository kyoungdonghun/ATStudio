---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20261011-ATS-047-evidence-pack.md
    reason: Exact independent gate, tests and MA conditions
  - path: REQ-20261011-ATS-006.md
    reason: Approved original-HTTPS-only recovery
---

# WI047 Original HTTPS Recovery Review

> Purpose: Report the exact conditional recovery gate without claiming a native outcome.

## Verdict

**TL;DR: CONDITIONAL_ALLOW for MA's pinned L1 and L2, each once, only to restore original quarantine HTTPS.** No app/MySQL start or unmask, retry, trial continuation, secret access or historical mutation is covered. PG performed no native operation.

F01 is resolved: receipt-write/fsync failure now invokes the guarded stop and all remaining safety checks, without retrying a previous stop. Disk receipts are explicitly provisional. Previous failed tests and records remain unchanged.

Independent tests: **13/13 PASS**; owner regressions independently rerun: **23/23 PASS**. Nine manifest files plus separate freeze, both recipe hashes, seven selected preservation pins and two PG failure records match. Two PowerShell bodies and four Python syntax units pass without native execution.

## Exact Identity

Recovery script: `7ab8be7b2b1b94df4eeabbc8be5c03620d0ad219749ce571014d587a92ad1d94`

Freeze: `90b446e6aa4af89ac7aee184d7b0dc74b258a7d92427e3017d169bbb31bf418b`

Bundle: `8a249e7b2628bc7c8bc66628e90e79b820713cb4b7d26909820de4ff1b7e30e4`

## Conditions and Limits

MA must freshly confirm host/boot, recovery access, strict System32 OpenSSH transport, management-only restrictions, exact bytes, app/MySQL masked and PID0, and Nginx stopped. Any collision, partial or ambiguous response is HOLD with no retry.

Only exit0 plus stdout `RECOVERED_ORIGINAL_HTTPS`, `receipt_durability=PASS` and `trial_status=HOLD` establishes the tool's outcome. A disk PENDING_DURABILITY receipt is not success evidence. Failed safety stop requires read-only inspection, not automatic repair.

Local mocks do not prove actual restoration or filesystem durability. The old trial remains HOLD; POST_STATE_CHANGED is still undetermined. MA performs the once-only recovery and access cleanup; WI045 integrates those actual results separately.

## Related Documents

- [WI047 Evidence Pack](../agent/WI-20261011-ATS-047-evidence-pack.md): Every executable/recipe pin and full conditions.
- [Exact Recovery README](../../build/WI046-quarantine-recovery/README.md): Reviewed L1/L2.
- [Independent Receipt](../../build/WI047-quarantine-review/final-receipt.json): Actual local results.

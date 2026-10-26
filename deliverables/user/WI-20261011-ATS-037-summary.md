---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: work-summary
status: confirmed
dependencies:
  - path: REQ-20261011-ATS-006.md
    reason: Approved single management-only trial
  - path: ../agent/WI-20261011-ATS-037-evidence-pack.md
    reason: Exact identity, findings, tests and execution conditions
---

# WI037 Restricted Web Review

> Purpose: Report the exact-byte security gate and remaining MA obligations.

**TL;DR: CONDITIONAL ALLOW for one approved REQ006 attempt. No confirmed execution-blocking defect. PG local review complete; native/browser NOT_RUN by PG.**

## Verified

- Independent owner regression85/85, additional review11/11 and PowerShell payload8/8 PASS; four PowerShell blocks parsed.
- At 2026-10-11 15:22:57 KST, all23 frozen files, all11 recipe blocks,17 preservation objects and the external QA helper matched.
- Runtime bundle: `35ff347e1d0e2b0cad930ce1431b073d08fea7d2a48631779360766424760ce5`.
- README: `91801af1f56c4f67d5be1c11e28ec8b04f1bb91a0db5d4d1d20015fdc6ccd74a`.
- External WI036 identity helper: `6fcd7568785a8e25d9391dce09f5a698f7adda13d9ce9fcdbe66c8f679a4ad6f`.

## Finding and Conditions

P2: `web_session.py:378` reports cleanup FAILED while the supervisor is merely `deactivating` and no final result exists. This is a reproduced intermediate-status defect, not evidence of actual native cleanup failure. Preserve every receipt. **Existing S5 already permits bounded read-only observation after finish without a code change:** at most60 checks,5-second intervals. Require authoritative final result and S7 independent postcheck. Never ignore a true final FAIL/HOLD, missing result or failed diagnostic; no start retry or repair.

MA must re-pin exact bytes and the external helper before L1; bind the current strict pinned SSH array, verify bare ssh resolves to System32 OpenSSH, and capture/redact raw stderr. S1-S7 remain unchanged UTF-8/LF buffers through `/bin/sh -s` with authentic SSH_CONNECTION. Fresh host/boot/peer, unique targets, provider management-only policy and usable root recovery are required before S3. Portal expiry alone is not a blocker; re-login before S3 if necessary to confirm a required current gate.

The accepted browser scope uses retained context, not a pristine profile. Stop on retained identity or excluded behavior; no storage inspection/clearing. Separate PC HTTP checks do not establish all browser network behavior. Actual runtime limits, BPF, Nginx worker removal/restoration, app/DB stop and scoped SSH cleanup remain MA obligations. One attempt only; preserve all old/new evidence.

## Next Step

MA may perform the exact single trial only when every condition holds, then route actual results to WI038. REQ006 remains active. No implementation or server change was made by PG; no exec session remains running.

## Related Documents

- [Evidence Pack](../agent/WI-20261011-ATS-037-evidence-pack.md): all pins, exact block identities, P2 reproduction and conditions.
- [Test receipt](../../build/WI037-restricted-web-review/test-receipt.json): independently executed tests and final pin check.
- [Unchanged recipe](../../build/WI035-restricted-web-check/README.md): MA execution sequence.

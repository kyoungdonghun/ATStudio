---
version: 1.0
last_updated: 2026-10-25
project: ATS
owner: PG
category: work-summary
status: accepted
---
# WI-20261025-ATS-002: Independent Recovery Repair Review

**Local review PASS.** The exact new candidate
`480834a7fadc69d542dd9113deb0af877d67a570170aa1819c682feec81efe2c`
may enter MA's previously approved execution sequence after its fresh native
gates and required access handoff. This is not an applied result or server PASS.
The old `3fbbec...` candidate and WI027 HOLD remain preserved.

- F1 resolved: every read-only reconciliation has a fresh `reconcile_attempt`
  for its service/log and attempt receipts. The original apply nonce, claim and
  before/expected-after evidence remain unchanged. A failed start after actual
  exclusive log creation was followed by successful fresh-attempt reconciliation;
  replay of the same attempt was refused. Cleanup and no-DML checks passed.
- F2 resolved: after November 24, only fresh read-only reconciliation is admitted.
  Inspect/apply remain blocked. Expired authority, write permission, invalid
  target/candidate and invalid attempt identity remain rejected.
- Approved mutation/recovery/audit functions are unchanged. ID1 promotion,
  ID2/3 preservation, grant1 expiry through November 24 inclusive, the two
  updated_at values and two truthful audits remain the complete intended delta.

Three focused recovery tests passed in an independent local run. Additional
checks rejected three wrong original-claim variants and six invalid authority
variants, verified actual MySQL argument builders, and compiled all three
payload modes. Existing eleven/seven test suites and stage one were not replayed.
Windows filesystem exclusivity is real; Linux ownership/signals/processes,
systemd and MySQL boundaries are synthetic. No server/provider/private-input
access, DB/app start, tool edit, cost or payment occurred in this review.

MA must verify the new candidate and current target/source/runtime identities,
then fresh cold probe -> inspect with five cleanup PASS results -> exact-before
apply once -> original-apply reconciliation -> independent cold check -> access
restoration. Use a fresh 32-hex `reconcile_attempt` for every reconciliation,
including a retry in the same second. Preserve all original claims and logs.
Unknown write outcomes require reconciliation before any further write; drift,
cleanup failure or partial change remains HOLD. Do not weaken pins on drift.

Native syntax/current schema/host containment remain to be observed. The four
saved values are bounded correction evidence, not a complete current backup.
Buyer3's old password remains forbidden and unnecessary for this DB-only step.
Mail/PG and overall operating readiness remain separate from account/grant
application; no extra cost or actual payment is authorized by this review.

[Evidence Pack](../agent/WI-20261025-ATS-002-evidence-pack.md) and
[hash-bound allowance](../../build/WI20261025-002-stage-two-review/allow.json).

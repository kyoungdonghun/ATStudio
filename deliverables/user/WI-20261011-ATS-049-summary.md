---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20261011-ATS-049-evidence-pack.md
    reason: Exact conditional verdict and tests
  - path: ../../build/WI048-quarantine-continuation/freeze-v2.json
    reason: Reviewed corrected revision
---

# WI049: Original HTTPS Continuation Review

> Purpose: Summarize the exact v2 recovery gate without implying native success.

## Decision

**TL;DR: CONDITIONAL_ALLOW for MA's fresh-gated README-v2 L1/L2, each once. PG native NOT_RUN.**

The v1 local-path blocker is reproduced and preserved. v2 resolves the actual candidate path; the Python source and L2 remain unchanged. No remaining blocking finding was identified in this bounded review.

Independent 9/9 and owner 21/21 tests passed in PG reruns. The real local L1 path/read/hash check, two PowerShell syntax blocks, all 14 frozen file pins, ordered bundle, two recipe pins and seven selected prior inputs passed.

Freeze SHA256: `26c47507668074aea79c5f96fd5d0a99d7dd7d1c86259edb3898baf170240b2e`.
Bundle SHA256: `b3befcd2a2310ef8c7461800ce35f75f2aad918eb479de12f480433c55fcacfe`.

## Conditions and Limits

MA must recheck exact bytes, current host/boot, management-only transport/firewall, root recovery, stopped app/DB masks/PID0, stopped Nginx, old evidence and fresh paths/claim. Only original Nginx may start; no app/DB start, unmask, detach, secret access, repair or retry. Use v2 only.

Require exit0 plus stdout recovery success and receipt durability PASS; disk bytes alone are provisional. Any failure or ambiguous result remains HOLD. Preserve all history and perform only approved read-only checks afterward.

Local mocks do not prove native recovery. Prior WI046 HOLD and WI042 POST_STATE_CHANGED cause NOT_DETERMINED remain unchanged. MA's actual continuation outcome and access cleanup flow to WI045.

## Related Documents

- [Evidence Pack](../agent/WI-20261011-ATS-049-evidence-pack.md): Full pins, conditions and reproduction.
- [Final Receipt](../../build/WI049-continuation-review/final-receipt.json): Machine-readable verdict.
- [Active Recipe](../../build/WI048-quarantine-continuation/README-v2.md): Exact reviewed procedure.

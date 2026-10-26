---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20261011-ATS-013-evidence-pack.md
    reason: Exact operator pins, mocked tests and future MA procedure
---

# WI-20261011-ATS-013 Summary

> Purpose: Hand the exact v5 lifecycle operator to WI014 independent review.

TL;DR: local operator preparation complete;19/19 synthetic tests PASS;
native/app/DB execution NOT_RUN. No local implementation blocker remains.

New file: `build/WI013-native-v5/ma-native-operator-v5.py`.
SHA-256: `64dc16d9b74315647497b9d335e5e309e27f544e03a5f76df35cb66fe0288136`.

The delta from preserved rev3 is five v5 path/hash/review replacements and one
explicit exit-code statement: PASS=0, HOLD=2. Candidate, wrapper, boot pin,
historical identity and lifecycle/TLS guards remain unchanged. The operator
binds WI012 candidate approval; WI014 separately reviews this new procedure.

Tests cover normal/rejected/timed-out probes, independent stop/mask after
failure, nonzero HOLD, TERM/KILL/wait paths and bounded safe reporting. These
are AST mocks, not Linux signals/services or native database evidence.

MA still needs fresh target/access/capture checks and WI014 PASS before using
the separately pinned operator. No old file was overwritten; no remote action,
secret access, startup, schema change or site-live claim. Recovery and exact
key/session/agent cleanup remain MA responsibilities.

## Related Documents

- [Evidence Pack](../agent/WI-20261011-ATS-013-evidence-pack.md): Exact staging/invocation, tests and limitations.
- [WI012 candidate review](../agent/WI-20261011-ATS-012-evidence-pack.md): Preserved v5 code approval.

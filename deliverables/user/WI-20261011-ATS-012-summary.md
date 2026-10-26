---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: work-summary
status: confirmed
dependencies:
  - path: REQ-20261011-ATS-001.md
    reason: Approved local correction and review
  - path: ../agent/WI-20261011-ATS-012-evidence-pack.md
    reason: Independent identities, tests and limitations
---

# WI012 Environment Contract Review

> Purpose: Report independent review of exact v5 and the full public bootstrap fixture.

## Verdict

**TL;DR: scoped PASS for v5 code/candidate; native NOT_RUN. No operator or remote retry approval.**

Independent focused tests passed 26/26 against the existing compiled v5, without rebuilding the candidate. The actual public renderer supplies all 83 keys with synthetic secrets. Pinned v4 rejects that fixture with ENV_KEYS; v5 accepts only the added exact `SPRING_CONFIG_LOCATION="classpath:/"` line. Other locations, mapping overrides, duplicates and malformed inputs still fail closed.

The prior review's missed public-contract integration is now regression-covered. This does not establish which private remote line caused the historical MA ENV_KEYS diagnostic.

## Exact Candidate

- Path: `build/WI011-schema-v5`.
- Bundle SHA-256: `463c95667ee51e5fe2f01303a43ccebe0c0547375a374730c511c924212c7e54`.
- All 171 generated files match v4; runtime safeguards remain pinned and unchanged. Approval now requires WI012 and the exact new bundle.
- Real-validator matching/missing-table/missing-column regression and offline mapping passed without a real connection.

## Boundary and Handoff

Old operators/approvals are not authorized for v5. Future native use needs new explicit approval, fresh target gates and a separately reviewed exact lifecycle. No private config, remote, DB or service was accessed or changed during this review. Application readiness, canonical fresh schema and production deployment remain unproven.

MA proceeds immediately to existing WI008 integration with this local result. Historical evidence remains intact; REQ remains active/HOLD for target execution.

## Related Documents

- [Evidence Pack](../agent/WI-20261011-ATS-012-evidence-pack.md): Exact source pins, test log and limits.
- [Handoff](../agent/WI-20261011-ATS-012-handoff.md): Approved review scope.

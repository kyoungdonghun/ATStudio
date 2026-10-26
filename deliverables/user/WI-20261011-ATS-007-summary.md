---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: work-summary
status: confirmed
dependencies:
  - path: REQ-20261011-ATS-001.md
    reason: Approved independent review
  - path: ../agent/WI-20261011-ATS-007-evidence-pack.md
    reason: Reproduction, candidate hashes and procedure conditions
---

# WI-20261011-ATS-007: Independent Probe Review

> Purpose: State the bounded decision for the existing v3 candidate.

## Decision

**TL;DR: HOLD. Do not run the v3 candidate against the target.**

One blocking defect: RuntimeSchemaProbe.java:59-60 supplies Integer database
version settings, while the bundled Hibernate validator expects String values.
The real validator throws ClassCastException before its metadata connection;
the normal probe would perform its first read-only snapshot and then return
SCHEMA_REJECTED even for a compatible schema. This is a probe defect, not proof
of a target-schema mismatch. Do not change the database to address it.

Candidate: `build/WI006-schema-v3`, SHA-256
`f06f7f2de2a891a482515f92abac8dfe75b4dfdddd1718a91f9c9592958a642b`.

## Verified Locally

- Exact backend638, frontend336 and SPA143 recorded inputs and existing JAR match.
- Existing48 focused tests pass; exact-candidate offline mapping smoke passes.
- Added bounded actual-validator reproduction fails on the unchanged candidate.
- Synthetic string-typed control passes matching metadata and rejects missing
  table/column cases. It is not an implementation fix or a replacement release.
- No network, SSH, DB, private configuration, browser or production execution.
  No implementation edit, source rebuild, dependency change or descendant.

PG verified on2026-10-11, Windows/PowerShell/Java17.0.12. MA's host/capture reports
are separately attributed, not independently verified target evidence.

## Procedure and Next Step

Retained WI025-v3 `load_bundle + observe` before/after is acceptable read-only
reuse after exact helper/dependency/input and private provenance checks. Compare
against the verified bootstrap receipt observation, never apply/mutate or reuse
the obsolete Nginx-off host guard. The old JDBC approval provides historical
identity only, not current authority.

SE must correct the setting types, add a real-validator regression and produce
a fresh candidate for independent review. Native remains HOLD until that PASS.
The native envelope also needs signal/timeout-aware child termination and reap,
independent bounded stop/mask attempts and recovery-console cleanup. Preserve
restricted TLS/timer and verify final DB/process/session/agent closure.

No full application, canonical fresh-schema, capacity or production-ready claim.
REQ remains active; MA owns WI008 integration and any subsequent delegation.

## Related Documents

- [Evidence Pack](../agent/WI-20261011-ATS-007-evidence-pack.md): Findings, tests, exact hashes and operator conditions.
- [SE candidate](../agent/WI-20261011-ATS-006-evidence-pack.md): Original candidate and exclusions.

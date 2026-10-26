---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SE
category: work-summary
status: stable
dependencies:
  - path: REQ-20261010-ATS-009.md
    reason: Approved local preparation and separate native authority
  - path: ../agent/WI-20261010-ATS-029-evidence-pack.md
    reason: Reproducible evidence and exact identities
---

# WI029: Read-Only JDBC Probe Preparation

TL;DR: **Local preparation PASS; native JDBC NOT RUN by SE; production HOLD.**

The standalone Java17 probe and stdlib Python wrapper reuse only Connector/J
9.7.0 from the exact approved WI026 JAR. No application startup, new dependency,
private setting read, SQL execution, service operation, commit or push occurred.

- Compilation PASS; actual pinned-driver class loading PASS without connecting.
- Synthetic suite:29 tests PASS,0 failures/errors/skips,59.130s. Java tests use
  an in-memory fake driver, including transaction closure and failure paths.
- Fixed target and strict generated-env parsing; password confined to memory and
  private framed stdin. No TLS/authentication weakening or automatic retry.
- Root-path/approval checks, bounded execution and fixed output codes are ready
  for independent review. Privileged memory/capture risks require MA guards.

Output: `build/WI029-jdbc-v1`. Plan SHA-256:
`6c1ad23065f89bc0776cb628322941b166dc82dbf4f56ee15c052aa2a1860199`.

Next: MA supplies WI029 and WI030 to WI031 PG review, then performs only the
explicitly approved native check and cleanup. WI032 integrates actual receipts.
JDBC success will not establish Hibernate, canonical fresh manifest, application
readiness, SMTP, public release, backup/restore or capacity.

## Related Documents

- [Evidence Pack](../agent/WI-20261010-ATS-029-evidence-pack.md): Commands, hashes, limits and rollback.
- [Runbook](../../deploy/single-vps/runbooks/jdbc-readiness.md): MA-only guards and exact invocation.

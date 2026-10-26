---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: work-summary
status: confirmed
dependencies:
  - path: REQ-20261011-ATS-001.md
    reason: Approved correction and independent re-review
  - path: ../agent/WI-20261011-ATS-009-evidence-pack.md
    reason: Exact new candidate and regression evidence
---

# WI009 Validator Correction

> Purpose: Return the corrected candidate for WI010 independent review.

TL;DR: PG-007-01 corrected locally; native NOT_RUN, PG010 pending.

- Changed only the two Jakarta database version settings from Integer to String.
  Real Hibernate validation now passes matching synthetic metadata and rejects
  missing tables/columns; preserved v3 still reproduces the original type failure.
- Focused suite21/21 plus reused JDBC suite29/29:50/50 PASS. Exact v4 also passes
  mapping smoke and a separate real-validator synthetic run, without Connector/J.
- Candidate: `build/WI009-schema-v4`; bundle SHA-256:
  `f2e59839caa096fff83b427ad47563be8f7b96c58bdd2825bda9216e109eaeec`.
- Wrapper requires WI010 and `pg_wi010_exact_bundle_pass`; WI007 is rejected.
  Runbook requires PG review of MA's signal-aware group cleanup, independent
  bounded stop/mask and recovery-console path while retaining restricted TLS.
- Existing source/JAR/SPA baseline still matches. WI006/v3 and WI007 evidence
  remain historical, unchanged; no application/configuration or production action.

## Next WI

WI009 immediately unblocks WI-20261011-ATS-010. MA owns delegation and native
lifecycle review; WI008 waits for PG plus actual MA outcome/cleanup. Synthetic
metadata is not canonical fresh DDL, target compatibility or a live site.

## Related Documents

- [WI009 evidence](../agent/WI-20261011-ATS-009-evidence-pack.md): Hashes and exact commands.
- [WI007 HOLD](../agent/WI-20261011-ATS-007-evidence-pack.md): Preserved diagnosis.
- [Runbook](../../deploy/single-vps/runbooks/release-readiness.md): Gated payload and cleanup.

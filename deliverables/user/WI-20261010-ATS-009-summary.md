---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: PG
category: work-summary
status: stable
dependencies:
  - path: REQ-20261010-ATS-005.md
    reason: Approved document-only restic selection
  - path: ../../deploy/single-vps/runbooks/restic-backup-plan.md
    reason: Operational design and execution gates
  - path: ../agent/WI-20261010-ATS-009-evidence-pack.md
    reason: Evidence, scope and verification boundaries
---

# WI-20261010-ATS-009 Summary

## Result

Completed the document-only [restic recovery plan](../../deploy/single-vps/runbooks/restic-backup-plan.md).
Private S3-compatible storage is the encrypted backup candidate, not primary audio storage.
Direct file backup avoids whole-media archive staging; a bounded logical DB dump and cache/temp still need local space.

## Decisions

- Capture the DB, both full storage roots, exact release, settings and retained keyring in one writer-free interval; resume only after verified complete capture.
- Finish the first full baseline and isolated recovery rehearsal before valuable writes. A50GB upload alone takes39.7min at20MiB/s or397.4min at2MiB/s; actual speed and downtime remain unmeasured.
- Match WI008's whole-helper1GiB cap,768MiB pressure threshold, one-CPU-equivalent budget and cold-only first operation; no helper/encoder/ingest overlap.
- Keep `max(20% filesystem,10GiB)` operating reserve plus dump/cache/temp peaks. Fail and escalate if the initial ceilings cannot fit; no silent capacity purchase or data deletion.
- Propose daily and prechange captures with7daily/4weekly/3monthly/2prechange retention selections plus protected points. No automatic forget/prune or object-lifecycle expiration is approved.
- Use an independently encrypted external-disk repository. Weekly plus prechange copying is a proposed7day provider-loss RPO, distinct from the daily provider-copy objective; owner/cadence require execution approval.
- Budget external retrieval separately: the MA's dated audit implies3,300KRW inclVAT for50GB externally retrieved in the first band, in addition to storage/server/API and other charges.54,120KRW is not a complete backup bill.

## Unverified Gates

Actual endpoint/region/TLS/private permissions, restic compatibility, tool installation, performance, costs, first backup and restore are **NOT RUN**.
The provider's key-based S3 documentation supports the candidate; it does not validate a fictional endpoint or default region.
Credentials, external disk/paths, owner/receiver and allowed downtime remain unresolved execution inputs.
Restored JVM startup also requires proven scheduler/local-write containment; blocking outbound traffic alone is insufficient.

## Scope and Handoff

Only this summary, its Evidence Pack and the new runbook were added. No existing source/config/template changes, secrets, purchase, DB access, transfer or deletion.
Historical Gmail/WAV/phone TEST successes remain valid within their original scope.
WI009 is design-complete, not deployment-ready; WI008 plus WI009 immediately feed MA's WI010 integration.
Validation results are recorded in the [Evidence Pack](../agent/WI-20261010-ATS-009-evidence-pack.md).

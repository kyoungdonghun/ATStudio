---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SE
category: work-summary
status: stable
dependencies:
  - path: REQ-20261010-ATS-006.md
    reason: Approved local-only implementation
  - path: ../agent/WI-20261010-ATS-011-evidence-pack.md
    reason: Exact changes, reproduction, hashes and limitations
---

# WI-20261010-ATS-011: Storage Admission And Timeout Bindings

## Result

Local implementation is ready for WI014 independent review. **105 focused tests
passed**, including 57 new tests, with no failures/errors/skips. Verification owner:
SE; 2026-10-10 Windows local workspace, final run at 17:49 KST. Production remains
HOLD; target performance and operational acceptance were not run.

## Changes

- Storage-writing HTTP requests in Track, Album, Playlist, Notice, Question and
  CompanyCertification share global2/IP1 admission and receive immediate HTTP429
  with Retry-After2 when full, regardless of multipart/JSON/form Content-Type.
- The guard is registered before servlet form-body parsing, not merely before the
  controller. Existing strict firewall and authentication rules remain in place.
- Permits remain held through transaction cleanup and async continuation; timeout
  or error notification alone never releases them. Final completion releases once.
- Client counters contain salted fingerprints, not raw IP values, and remove entries
  on release. Existing trusted-proxy identity resolution is reused.
- Explicit Hikari10/min2/5000ms, SMTP5000/10000/10000ms, multipart0B and admission
  environment bindings were verified using actual property binding targets.

## Verification Boundary

Tests used synthetic requests, Boot's registration collector, an isolated security
context, mock JDBC connections/storage/repositories, and local JavaMail Session
properties. No full application, database, scheduler, real storage, FFmpeg, SMTP
connection, listening server or external environment configuration was loaded.
No deployment files, remote services, secrets, payments, backups or Git state were
changed. Source/test compilation passed; full build/coverage was not run.

Pool10 does not reserve connection partitions or prove deadlock freedom. Other
payment/audit/background consumers remain outside storage admission; their source
inventory and target verification limits are recorded in the Evidence Pack.

## Next Step

WI011 blocks WI014. MA owns independent review after the WI012/WI013 inputs are
available. This does not close REQ006 or authorize target installation or operation.

## Related Documents

- [Evidence Pack](../agent/WI-20261010-ATS-011-evidence-pack.md)
- [Approved REQ006](REQ-20261010-ATS-006.md)

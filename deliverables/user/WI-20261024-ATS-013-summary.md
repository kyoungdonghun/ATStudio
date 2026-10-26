---
version: 1.1.0
last_updated: 2026-10-24
project: ATS
owner: DocOps
category: summary
status: stable
related_wi: WI-20261024-ATS-013
---

# WI-20261024-ATS-013 Summary

## Current: Local Preparation and Exact Review Complete

WI013 is complete for local preparation only. The exact 19-member candidate
passed SE10 + independent PG6 focused checks, with 11 unchanged helpers and
Source643/JAR preserved. PG verdict: ALLOW_EXACT_LOCAL_PREPARATION_ONLY,
native_authorized=false, no local blocker. MA's final candidate/member/receipt
pin check passed. No server/provider/private-input access or native operation
occurred. See [exact pins and evidence](../agent/WI-20261024-ATS-013-evidence-pack.md).
The final README-only storage clarification preserves the other 18 members;
the existing 16 focused results are reused without a test rerun.

The final adapter reuses the existing repository without init or replaying the
first-capture path. It prepares one current backup and isolated restore of that
**new snapshot only**, with all-file/owner and complete SQL strict comparison.
It does not run repository-wide check --read-data; the initial broader cost
draft below is superseded. Old snapshots/evidence are preserved. Runtime keys
remain excluded, and full disaster recovery is not claimed.

The next approval proposal separates these gates:

1. Separately approved read-only provider billing/allowance pages, without
   purchases or settings changes. Establish request cost before potentially
   billed repository metadata/API calls or transfer; read-only S3 can cost money.
   Unknown fees stop the step. Then perform approved bounded metadata and exact
   target/repository/escrow confirmation. Initial inventory uses no DB/cloud.
2. Actual zero-additional-paid-cost evidence: **current budget 0 KRW; actual
   cost unknown**. Include metadata/list/upload, exact new-snapshot restore,
   requests/egress/tax and continuing retained storage/local capacity. Unknown,
   stale or positive costs mean HOLD. Throughput 4096 is not a cost cap. No
   automatic deletion/retention expiry, purchase or free-usage guarantee.
3. Separately approved new backup/isolated restore with current fixture/target
   pins, maximum 1800-second authority and cost evidence within 600 seconds covering
   that window; separate STOP/PID_ZERO/SOCKET_GONE/COLD_BASELINE, post-observation
   and access restoration. Lost outcomes require preserved evidence and separately
   approved read-only reconciliation, without retry or source-DB overwrite.
4. Keep the current grant on 2026-10-24. If day D is needed, separately propose
   preserving started_at and changing expires_at only to explicit D, plus
   updated_at/truthful audit and exact transaction safeguards. This extends
   entitlement through D, not activation only on D; a D-only trial gate is separate.
   Neither date writes nor that gate is implemented/approved here. A backup must
   cover the chosen pre/post-correction recovery point accurately.

Current fixture recovery remains NOT_ESTABLISHED. New backup/restore,
app/login/audio and data changes remain unapproved. REQ007 stays active, and no
native WI starts automatically. See the [Korean next-approval plan](REQ-20261011-ATS-007.md).

## Historical Initial Approval and Cost Outline

The user approved minimal local backup-tool preparation and independent review,
including existing-repository reuse and a method to confirm additional cost.
SE011 -> PG012 -> Doc013 is active; exact candidate/tests/review are pending.
No server/provider/private-input access, backup or restore occurs now.

The later execution proposal will cover:

- Reusing the exact existing repository without init, preserving snapshots and
  evidence; no prune/forget, deletion or source-database overwrite.
- A separately approved current-fixture backup and isolated restore of that
  exact new snapshot, with reviewed time/capacity/transport limits and independent
  cleanup. Excluded runtime-key/recovery scope remains explicit; no full disaster
  recovery claim follows from local tests.
- Verifying actual billing terms, included capacity/current usage and bounded
  additional billable storage/transfer/requests/execution before quoting a cost
  range and obtaining a monetary limit. Retained `check --read-data` reads the
  full existing repository, so include that read plus restore, requests,
  retained storage and local copies. Upload/download4096 is a throughput limit,
  not a byte/cost cap. Provider plan/account/prices remain unchecked. A later
  read-only confirmation must resolve these inputs. Unknown costs mean HOLD; no price,
  zero-cost promise or guaranteed billing cap is invented. No provider access or
  purchase is performed during preparation. Cost retention assumptions do not
  authorize automatic expiry/purge; existing snapshots/password references stay.
- Keeping the grant on 2026-10-24 KST. Any different date requires a separate
  exact change proposal/approval; no automatic extension. A later approved
  change must be included in the state covered by backup/restore evidence.
- Leaving app/login/audio, payment and other data effects outside this approval;
  uncertain results stop further operations while preserving evidence.

Local audio preparation and WI010 read-only restoration remain complete.
Current fixture recovery coverage remains NOT_ESTABLISHED. WI013 and REQ007
stay active until the exact local candidate/review is integrated; native execution
requires later approval even after local readiness.

See [Korean approval and next-scope draft](REQ-20261011-ATS-007.md),
[governing handoff](../agent/WI-20261024-ATS-013-handoff.md) and
[initial evidence](../agent/WI-20261024-ATS-013-evidence-pack.md).

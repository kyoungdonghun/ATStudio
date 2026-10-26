---
version: 1.0
last_updated: 2026-10-26
project: ATS
owner: SE
category: reference
status: draft
dependencies:
  - path: ../../deliverables/agent/WI-20261026-ATS-001-handoff.md
    reason: Approved local-only inventory boundary
  - path: ../../deliverables/agent/WI-20261024-ATS-018-evidence-pack.md
    reason: Final actual stage-one evidence
---

# Operating safeguards: evidence and remaining work

Observed locally on 2026-10-26 by SE. This is source/retained-receipt inspection, not a new host observation. No network, server, provider, private configuration, credential, DB, application, backup or restore operation ran. Costs and provider terms were not refreshed. The existing repository is the reuse target; neither a new repository nor paid capacity is proposed as a prerequisite.

## 1. Evidence matrix

| Safeguard | Confirmed dated evidence and owner | Current local preparation | Remaining completion evidence |
|---|---|---|---|
| Bounded capture and isolated verification | MA actual 2026-10-24 `capture2-result.json`: `CURRENT_CAPTURE_ISOLATED_VERIFY_PASS`, COMPLETE, 43 tables/15 rows, identified snapshot, four cleanup PASS. PG accepted the final interpretation. | WI011 primitives plus WI017 allowlist/SQL-context repairs exist. Historical failures remain separate. | A newly reviewed candidate and fresh observation are required for the changed account/grant state; do not rerun the old command unchanged. |
| Coverage freshness | That capture preceded audio, reset and 2026-10-25 account/grant application. MA's later audio/reset and whole-43-table account delta are retained. | Public/private/temp roots, pinned releases and logical DB are included by the bounded capture design. | New complete snapshot covering the later track/source/MP3/license/download/session/reset records, ID1 ADMIN and Nov24 grant, with source-to-restored comparison. Do not call the old snapshot current. |
| Recoverable secrets/configuration | WI016 records host password-reference and optional PC escrow comparison; this is historical reference matching. | WI011 explicitly excludes runtime secrets. General runbook lists complete billing key ring, JWT, settings and independent recovery access. | Approved protected recovery inventory, independently recoverable access and actual recovery proof without relying on the original VPS/PC. The bounded snapshot is not full disaster recovery. |
| Off-provider copy | No successful independent copy receipt is established by the inspected current chain. | `copy-plan` is a marker, not a copier; existing external-disk topology is a design. | Exact existing destination identity/capacity and separate recovery access, cost bound, copied exact snapshot IDs and independently verified restore. No disk purchase or deletion inferred. |
| Recurring backup | One-shot native capture succeeded. It does not establish daily unattended capture. | Generic runner exists; host hooks remain unconfigured; timer has no `OnCalendar` or install section. | Reuse reviewed native primitives in a named-host adapter, shared operation lock, rendered expiring schedule, bounded success/failure/missed-run receipts and actual unattended operation. |
| Backup/host failure notification | No actual operational alert delivery proof found in this chain. ID1 is the selected operations destination. | Receipt status checker exists; signal proposals exist. Checker schedule/sender is not installed by the generic tooling. | Independent monitoring execution and real synthetic alert/acknowledgment/clear proof. Gmail auth/reset delivery does not prove backup alerts. |
| TLS and renewal | MA 2026-10-11: trusted restricted TLS, renewal dry-run/hook PASS, single Certbot timer enabled/waiting. | Existing quarantine hook/runbook should be reused. | Read current public expiry and timer/last run; verify unattended renewal evidence and alert delivery. Do not issue/recreate a certificate solely because an old runbook says candidate. |
| Restart/reboot | MA 2026-10-10 OS reboot/postboot PASS before production application availability. | `atstudio.service` has `Restart=on-failure`, control-group kill and resource limits. | Matched DB/storage/proxy/application boot order, one JVM/scheduler, interrupted synthetic audio recovery, restricted health and rollback under a separately bounded runtime trial. OS reboot alone does not prove application recovery. |
| Release/configuration rollback | Historical tested JAR and later NCP candidate are separately identified. | WI005 11 source pins and historical JAR freshly match locally. Effective-config provenance and exact pointer rollback recipe exist. | Compare actual unit/drop-ins/argv/settings origins privately on target; compatibility-aware start/rollback proof. Do not rewind DB or replace billing keys to undo configuration changes. |

## 2. Concrete reuse blocker found before another server session

The successful bounded backup tool is tied to its old account state:

- `build/WI20261024-011-backup-preparation/lib/inventory.py:107` fixes roles to ID1 USER, ID2 ADMIN, ID3 USER; `adapter.py:142` requires that match. The retained 2026-10-25 completion establishes ID1 ADMIN, so an unchanged new capture would reject the approved state.
- `backup_plan.py:55-58` accepts only 1 or 2 `admin_operation_audit_logs` rows. The later completion records two additional audit inserts. The exact current count must be read and bound; it must not be fabricated as 1/2 merely to pass the old guard.
- Grant dates are already validated supplied inputs (`backup_plan.py:59-66`), not write instructions. Preserve start 2026-10-24, end 2026-11-24 inclusive KST, ID3/plan1, MONTHLY/CANCELLED, no pending change and nonbilling state. No further date mutation is needed for this review.

The minimal follow-up is a **new derived and pinned candidate**, preserving WI011/WI017 bytes and successful evidence. Reuse SQL capture, strict file inventory, cost calculation, exact-new-snapshot restore, equivalence checks, one-use records and cleanup. Bind the new allowed role tuple and exact audit baseline explicitly; do not weaken checks to allow arbitrary users, roles or audit counts.

Before implementation approval is considered complete, the concrete candidate contract should require:

1. Exactly actors 1/2/3 with ADMIN/ADMIN/USER, not deleted and verified; six exact current products unchanged, plan1 individual/daily5/active.
2. Exactly the intended nonbilling grant projection, including dates and absence of pending billing changes; record expected counts from fresh read-only observation.
3. All 43 tables captured and compared, preserving all existing rows/cells/audits. Include audio and session/reset state in the new baseline; do not compare against the older 15-row capture as an expected current total.
4. Negative cases: wrong roles/actors, wrong quota/product/date/pending state, unexpected table/count/cell drift, unfinished audio/storage work, missing/extra/hash-changed files, stale/wrong target or cost evidence, uncertain previous outcome. These must reject rather than broaden the allowlist or replay a capture.
5. Focused local tests and independent review for only the changed contract; reuse unchanged prior evidence. Native validation occurs once after the real current gates pass, not as a way to discover the known mismatch.

No tool or product code was changed in this WI. This is an actionable next implementation scope, not a prepared executable replacement.

## 3. Limits that must stay explicit

- `capture2` performed exact-new-snapshot restore and isolated DB verification, not a repository-wide data check, app startup or external-provider-loss recovery. The accepted SQL proof allows exactly 209 redundant MySQL charset insertions, with equal column metadata and all other bytes equal; raw dump SHA values are not equal.
- Runtime configuration/secret capture remains excluded from that bounded snapshot. Password escrow matching does not prove all application encryption keys, portable custody or recovery without the original machine.
- The generic daily runner's ordinary acceptance retrieves dump/manifest and checks metadata; it does not download all media. Do not confuse that path with WI011's exact snapshot restore.
- `APP_BACKGROUND_TASKS_ENABLED`/audio-only controls now exist. Older runbook claims that no scheduler suppression exists are historical. Current WI005 source and mode checks govern preparation, while actual restored-runtime suppression remains unverified. Audio-only is not a payment kill switch: synchronous payment/withdrawal paths may still have effects.
- The daily runner needs its operations lock connected to deployment and other writers. A lock path declaration is not proof all operations participate.
- `Restart=on-failure` and `After=mysql.service` are source settings, not target restart/boot proof or DB readiness guarantees. Starting full mode can run renewal, expiry, reconciliation, withdrawal cleanup and storage recovery.
- TLS dry-run success and a waiting timer do not prove an actual changed certificate or an expiry notification. The existing runbooks differ between a 30-day expiry check and a 21-day alert proposal; choose and record a single operational threshold before activation.
- No cleanup/prune/forget/unlock or retention deletion is authorized. No RTO/RPO, capacity or zero-cost guarantee follows from this local inspection.

## 4. Source and receipt pointers

| Topic | Exact pointer |
|---|---|
| Actual final capture and accepted equivalence | `deliverables/agent/WI-20261024-ATS-018-evidence-pack.md:112-114` and `build/WI20261024-stage-one-native/capture2-result.json` |
| Snapshot predates audio/reset; final media proof | Same EP `:128`, `:132-140`; `post-reset-media-result.json` records 2 files / 1,925,002 bytes unchanged at that observation |
| Later account/grant delta and no new backup | `deliverables/agent/WI-20261025-ATS-003-evidence-pack.md:48-52` |
| Snapshot roots and secrets boundary | `build/WI20261024-011-backup-preparation/adapter.py:52-59,152-169`; README `:7-16`; `lib/cold_capture.py:27-32,234-235` |
| Cost/recovery-reference historical boundary | `deliverables/agent/WI-20261024-ATS-016-evidence-pack.md:60-78,92-99`; WI018 `:106-114` supersedes its earlier capture-not-run state |
| Generic host hooks and shared lock | `deploy/single-vps/backup/README.md:41-80`; `host_hooks_unconfigured.py:1-8`; `guarded_backup.py:23-34` |
| Recurring gate / monitor / copy limits | Backup README `:93-108,168-186`; `guarded_backup.py:294-308`; `atstudio-backup.timer:1-13` |
| TLS actual acceptance | `docs/index.md:2307-2339`, originating `deliverables/agent/WI-20261011-ATS-005-evidence-pack.md` |
| TLS check/notification separation | `deploy/single-vps/runbooks/domain-tls.md:281-321` |
| OS-only reboot acceptance | `docs/index.md:2546-2565,2603-2623`, originating `deliverables/agent/WI-20261010-ATS-024-evidence-pack.md` |
| App service/recovery | `deploy/single-vps/systemd/atstudio.service:3,12-30`; `runbooks/deployment.md:227-246`; WI005 README operating modes and rollback |
| Signal/notification proposal | `deploy/single-vps/runbooks/acceptance-capacity.md:33-79` |

## 5. Fresh local checks

`source-pin-check.json`: 2026-10-26 18:55 KST, inspected read-only WI005 checker, exit 0, 11 public paths and historical JAR match; target NOT_OBSERVED. Original receipt untouched. This does not assert source-to-historical-JAR equivalence or NCP deployment.

`hook-marker-check.json`: same time, inspected marker, exit 2 with `configured=false / HOST_HOOK_NOT_CONFIGURED`, as designed. This is proof of its fail-closed local behavior, not a failed native backup.

The next ordered work is in [next-runtime-checklist.md](next-runtime-checklist.md). Overall stage two remains open and stage three is not complete.

---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SA
category: work-summary
status: stable
dependencies:
  - path: REQ-20261010-ATS-005.md
    reason: Approved document-only settings decision
  - path: ../../deploy/single-vps/runbooks/resource-baseline.md
    reason: Selected baseline and implementation/test gates
  - path: ../agent/WI-20261010-ATS-008-evidence-pack.md
    reason: Evidence, preservation and verification limits
---

# WI-20261010-ATS-008: Resource Settings Decision

## Result

Completed the [initial baseline](../../deploy/single-vps/runbooks/resource-baseline.md)
for the2vCPU/8GB candidate. Values are selected for a later approved target test,
**not applied, installed or performance-validated**. No executable template or
product code changed; no application/DB command or provider research was run.

| Area | Selected starting decision |
|---|---|
| JVM / application group | Heap256MiB-1GiB; metaspace256MiB; cgroup pressure2GiB, hard3GiB including encoder |
| MySQL / Hikari | Buffer pool512MiB, MySQL max30 connections; Hikari max10/min2, acquisition timeout5s |
| Request processing | Tomcat40 threads/128 connections/20 accept backlog unchanged; one audio worker/encoder,5s poll,300s native encode timeout |
| Storage mutations | New implementation required: global2 and1/IP, no waiting queue; reject excess429 before transaction/multipart allocation |
| Media | Preserve shared media4/IP,16/global; original downloads2 global,1/IP; stream256KiB/s and original2MiB/s |
| Email | Explicit future mapping of connect5s/read10s/write10s JavaMail timeouts, with phase-specific tests |
| Backup budget | One helper group:1GiB maximum,768MiB pressure level, one-CPU-equivalent starting quota; cold capture, no initial load overlap |
| Host reserve | At least1GiB available memory; disk operating reserve plus admitted-work/backup peak; existing bounded journal logging |

## Important Qualification

**Pool size10 alone does not solve `REQUIRES_NEW` starvation.** The plan requires
global storage-mutation admission before the outer transaction, plus an inventory
and test of other competing background/payment/HTTP nested callers. Those controls
are not implemented now. The residual risk is an explicit pre-invitation gate,
not a paid-server upgrade finding. UI sequential uploads are insufficient.

50MB at2MiB/s is23.84s of ideal payload transfer, compared with95.37s at the
old512KiB/s. The selected usability test is<=45s for each of two concurrent
50MB downloads while listening; actual success remains NOT RUN. Faster shaped
responses and concurrency limits still do not create a monetary cap.

MA/PG should reconcile WI009's restic process/cache/retention design with this
1GiB helper envelope. Effective host RAM, image/document peaks, pool latency,
mixed-load performance and restore remain unmeasured. The runbook supplies
named tests, explicit failure thresholds and a future implementation map.

## Completion Boundary

Verification2026-10-10, local Windows/source/templates, SA. Reused WI004 source
audit and WI007 integration; focused recheck only. Current HEAD remains
`58b1011dad796017e47dbcc82a4225ca449bcdf6`; unrelated dirty work preserved.
Documentation checks and seven executable-template preservation hashes are
recorded in the Evidence Pack. No purchase/install/backup execution or release
approval. WI008 complete; MA coordinates WI010 when WI009 is also complete.

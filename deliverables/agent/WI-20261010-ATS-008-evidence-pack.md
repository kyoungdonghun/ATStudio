---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SA
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261010-ATS-008-handoff.md
    reason: Approved exact write scope
  - path: ../user/REQ-20261010-ATS-005.md
    reason: Settings and restic design approval, no installation
  - path: ../user/WI-20261010-ATS-008-summary.md
    reason: Decision digest
  - path: ../../deploy/single-vps/runbooks/resource-baseline.md
    reason: Selected candidate values, resource envelope and unresolved gate
---

# Evidence Pack: WI-20261010-ATS-008

## Summary

Selected concrete initial resource, admission, transfer and timeout values in a
document-only plan, preserving current executable templates. Nested transaction
starvation remains an explicit implementation/acceptance gate, not declared fixed.

## Scope / DoD Check

- [x] Reused WI004 audit; focused recheck of current values and nested storage paths.
- [x] Current versus selected settings, application/native/MySQL/backup/OS budget.
- [x] Concrete mutation/download admission design and honest residual pool risk.
- [x] Protected media, finite SMTP phases, exposure/timezone/disk/log boundaries.
- [x] Named mixed-work tests, stop thresholds and later implementation map.
- [x] Only the three assigned new documents created with `apply_patch`.
- [ ] Runtime validation/install/DB/backup execution: NOT RUN and not authorized.

## Reference Documents (Tier 0-2)

MA injected Tier0/Tier1 excerpts and explicit pointers; retained WI004 context
was reused. Handoff exists and was read before writing; create-wi-evidence-pack
skill used. RoleSA, task architecture/settings decision.

| Tier | Reference | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Bounded approval, no execution by implication |
| 0 | `docs/standards/development-standards.md` | Current source, layered transaction context |
| 0 | `docs/standards/documentation-standards.md` | Metadata/pointers/new deliverables |
| 0 | `docs/standards/glossary.md` | Listening versus entitled original download |
| 1 | `docs/policies/security-policy.md` | No secrets/private configuration or log disclosure |
| 2 | `deploy/single-vps/CONFIGURATION.md` | Current caps, exposure and single worker |
| 2 | Capacity and backup runbooks under `deploy/single-vps/runbooks/` | Existing finite test/space/restore gates |
| Context | WI004 summary/EP and WI007 summary | Reused sizing evidence and integrated limitations |
| Context | REQ005 / WI008 handoff | Three-path write scope, downstream WI010 |

## Evidence Pointers

Java paths are under `src/main/java/com/atstudio/atstudio/`.

| Pointer | Evidence / decision use |
|---|---|
| `deploy/single-vps/systemd/atstudio.service:20,28-30` | Existing512MiB heap,192MiB metaspace,1/1.5GiB app cgroup and256 tasks |
| `deploy/single-vps/mysql/atstudio.cnf:10-11`; env example9-12 | Existing30 connections/256MiB buffer, Tomcat40/128/20 and Hikari5 |
| `deploy/single-vps/nginx/https.conf.template:51-74`; nginx global/proxy templates | Existing shared zones,512KiB/s, protected path rules and no media cache/buffering |
| `service/TrackService.java:65-74` | Outer transaction queries user before storage processing |
| `service/storage/StorageMutationCoordinator.java:56-94,110-130,174-196` | Nested journal prepare for writes/deletes; generated audio explicitly outside business transaction |
| `service/storage/StorageMutationJournalService.java:38-63` | REQUIRES_NEW prepare/transition |
| `service/audio/TrackAudioProcessingTransactions.java:24-69` | Single worker's short claim/finalization phases, not a globally reserved DB pool |
| `service/storage/StorageMutationRecoveryService.java:35-43,144-151` | Startup/scheduled recovery competes for DB resources |
| Coordinator reference inventory | Track, Album, Playlist, Notice, Question, CompanyCertification, audio worker/transactions; not a proof that every application nested transaction was inventoried |
| WI004 EP / source pointers | Bounded audio, whole image/PDF allocation, synchronous transfer and historical TEST evidence reused |
| `src/main/resources/application.yml:23-37`; env example mail section | Finite SMTP properties not currently configured; multipart limits already exist |
| `deploy/single-vps/journald/atstudio.conf` | Existing256MiB/64MiB/7day and1GiB reserve values retained in plan |

## Commands / Results

Read-only commands: `Get-Content` on REQ/handoff/settings/WI007 and relevant
transaction code; scoped `rg -n` and `rg -l StorageMutationCoordinator`;
`git status --short --branch`, `git rev-parse HEAD`; `Get-FileHash` on seven
templates. HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`, branch main;
prior dirty/untracked work preserved. No provider/browser/network calls, secrets,
application/DB/build/test processes, installations or paid actions.

Arithmetic:50MB/2MiB-per-second=23.8419s;100MiB/2MiB-per-second=50s;
14*256KiB/s+2*2MiB/s=7.5MiB/s=62.91456Mbps=679.477248GB/day.
Planning memory3+1.25+1+0.75=6GiB plus1GiB available reserve; actual host units
and process behavior still require measurement. These are budgets, not benchmarks.

## Executable Asset Preservation

SHA-256 values captured before document edits; post-write recheck matched 7/7.
All paths below relative to `deploy/single-vps/`. No historical hashes changed.

| Path | SHA-256 |
|---|---|
| `systemd/atstudio.service` | `F3E72F2C1B5F2B616693C98856E024063396336BB6E47527C895B9B365C4AB14` |
| `mysql/atstudio.cnf` | `78047E4EE940EDD339FCDFEEC91DAF159C1527907965F971EB69BC3C4D37A57A` |
| `env/atstudio.env.example` | `EB08966DAC9570D94356354530606EFE3E975A9A7B7881735D3C1DD36B7DDB6B` |
| `nginx/nginx.conf.template` | `060903FF487D99D410425EB11D61F6422BFBAFDEB085F543CE2E84F200961A50` |
| `nginx/https.conf.template` | `F69CAD94887742E4F220D8E8FD198A9E89A5EA32637121DC1F1A4D8FD74DC2CA` |
| `nginx/proxy.conf.template` | `D84E2EA90710BE3E9ECA3ADBA66887F34365F5D56D540F5A854A89272B9F9A80` |
| `journald/atstudio.conf` | `229473C1D88E2CF30DAC6E9F4ED34327A39A66A74653DC72465E42239B2FC963` |

## Validation / Limits

Scoped PowerShell documentation checks passed (exit 0): all three frontmatter
blocks present, zero missing linked files and zero trailing-whitespace lines.
The new runbook is 192 lines, within the preferred 230-line bound. Link checking
verified target-file existence, not anchors. Seven before/after executable
template hashes matched (exit 0). No new application measurement or test result.
MA/WI010 owns integrated links/index
and repository-wide documentation validation. Target cgroups/admission/SMTP/
performance/backup remain NOT RUN. Partial caller inspection is not a formal
proof that nesting is bounded; the plan deliberately retains that open gate.

## Risks / Rollback / Follow-ups

- Raising pool/heap without admission is not this selected safe-test baseline.
- Mutations need admission before transactions/body allocation; routes/background
  paths and exception release need implementation tests. No code changed now.
- Restic budget must align with PG/WI009; cache/dump growth, helper memory and
  runtime length can invalidate estimates. No automatic cap increase/deletion.
- Rollback is ignoring this unapplied plan, not reverting templates or other
  contributors' files. No installed state was changed.
- WI008 complete within document scope; Blocks WI-20261010-ATS-010. MA triggers
  integration when WI009 is also ready; no purchase or implementation implied.

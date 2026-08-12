# WI-20260809-ATS-019 Evidence Pack

## Status

- Result: PASS for the bounded declaration inventory.
- Product code changed: No.
- Existing current-state documentation changed: No.
- Runtime or DB changed: No.
- Baseline: `e343c20` on `codex/v1-release-rehearsal-fixes`.

Two initially delegated workers were stopped after remaining in `running` state
without writing outputs. The MA recovered the bounded WI from reproducible
source scans. No worker-authored patch was integrated.

## Deliverables

- `deliverables/agent/WI-20260809-ATS-019-handoff.md`
- `deliverables/agent/WI-20260809-ATS-019-inventory.md`
- `deliverables/agent/WI-20260809-ATS-019-evidence-pack.md`
- `deliverables/user/WI-20260809-ATS-019-summary.md`

## Evidence Sources

| Evidence                         | Pointer                                                                                                                          |
| -------------------------------- | -------------------------------------------------------------------------------------------------------------------------------- |
| Active route graph and guards    | `frontend/src/router/index.tsx:110-241`                                                                                          |
| Public/USER/ADMIN header entries | `frontend/src/layouts/Header.tsx:14-33`                                                                                          |
| ADMIN sidebar entries            | `frontend/src/layouts/AdminLayout.tsx:12-28`                                                                                     |
| Frontend API modules             | `frontend/src/api/*.ts`, excluding tests                                                                                         |
| Backend HTTP mappings            | `src/main/java/com/atstudio/atstudio/controller/*.java`                                                                          |
| Scheduled operations             | `PaymentReconciliationService`, `SubscriptionScheduler`, `WithdrawalBillingCleanupCoordinator`, `StorageMutationRecoveryService` |
| Current API count contract       | `docs/design/api-spec.md:75-103`                                                                                                 |
| Current UI count contract        | `docs/ui/atstudio-front-list.md:21-46`                                                                                           |
| Registry counts                  | `docs/registry/project-registry.md:41-43`                                                                                        |

## Reproduction Commands

```powershell
git status --short --branch
git rev-parse --short HEAD

rg -n 'createBrowserRouter|path:|children:|element:' frontend/src/router/index.tsx

Get-ChildItem frontend/src/api -File -Filter '*.ts' |
  Where-Object { $_.Name -notmatch '\.test\.ts$' }

rg -n '@(Get|Post|Put|Patch|Delete)Mapping' `
  src/main/java/com/atstudio/atstudio/controller

rg -n '@Scheduled' src/main/java/com/atstudio/atstudio
```

Route counts were independently obtained with the installed TypeScript parser
by walking object literals with `path` or `index` properties and declarations
initialized by `lazyPage(...)`. Controller counts were grouped per Java file by
method-level mapping annotation.

## Verified Counts

| Metric                         |                     Count |
| ------------------------------ | ------------------------: |
| Path-bearing React routes      |                        56 |
| Index redirects                |                         1 |
| Routable declarations          |                        57 |
| Lazy/distinct page UIs         |                        53 |
| Header navigation arrays       | 5 public, 5 USER, 2 ADMIN |
| ADMIN sidebar entries          |                        15 |
| Frontend API source modules    |                        19 |
| Controller files with mappings |                        25 |
| Method-level HTTP mappings     |                       144 |
| Scheduled methods              |                         6 |

## Findings and Limits

1. Existing UI and API count documents match current declarations.
2. The API total includes one SPA forwarding mapping; this is the current
   documented method-mapping counting contract, not 144 business REST actions.
3. Four ADMIN visual routes are contextual rather than sidebar entries: Track
   edit, Album create/edit, and Notice edit.
4. Three checkout paths reuse one payment page and must be tested as different
   callback states despite sharing a component.
5. Presence of an API module or mapping does not prove an active UI consumer.
   Function-level consumer and state mapping is a WI-020 requirement.
6. Schema counts, SecurityConfig behavior, modal inventory, API response
   semantics, visual state, runtime behavior, and DB persistence were out of the
   bounded recovery scope and remain open.
7. Historical/promotion branch wording is not changed during the frozen audit;
   the current audit branch is not automatically the documented official branch.

## Validation

The WI is documentation-only. The following checks passed on 2026-08-09:

- Targeted Prettier check: PASS for all five REQ/WI documents.
- Repository documentation validation: PASS with no broken links, unsupported
  traceability IDs, missing Tier 0 documents, or unindexed documents.
- Worktree review: only the five REQ/WI documents and the pre-existing,
  intentionally untracked client demo ZIP were present as untracked files.

Reproduction commands:

```powershell
Set-Location frontend
npm.cmd exec -- prettier --check `
  ../deliverables/user/REQ-20260809-ATS-001.md `
  ../deliverables/agent/WI-20260809-ATS-019-handoff.md `
  ../deliverables/agent/WI-20260809-ATS-019-inventory.md `
  ../deliverables/agent/WI-20260809-ATS-019-evidence-pack.md `
  ../deliverables/user/WI-20260809-ATS-019-summary.md

Set-Location ..
python .agents/skills/validate-docs/scripts/validate_docs.py
git status --short
```

## Rollback

Remove the four WI-019 files and the parent REQ only if the entire approved
audit is abandoned. No product/runtime rollback is required because this WI did
not change them.

## Next WI

WI-20260809-ATS-020 must transform this declaration inventory into the master
role/state/data/viewport/interruption matrix. It must not begin browser mutation
testing until every inventory row has an expected outcome and side-effect
classification.

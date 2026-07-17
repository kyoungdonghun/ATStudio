# Evidence Pack: WI-20260717-ATS-003

## Summary

Removed approved React SPA residuals, canonicalized active page/route identities, and replaced ten native confirmations with controlled single-submit dialogs without changing V1 product behavior.

## Scope / DoD Check

- [x] Obsolete server play-history and download-queue frontend clients/types are removed; local history and active download history remain.
- [x] Canonical `/downloads`, `DownloadHistoryPage`, and `PaymentOperationsPage` identities are active with no compatibility redirects.
- [x] Unused `DataTable`, approved API exports, playlist-create adapter, and owned source placeholders are removed.
- [x] Ten production payment/whitelist `window.confirm` calls are replaced by accessible, pending-aware `ConfirmDialog` flows.
- [x] Stale one-time payment routes/handling and `mock*` checkout CSS identities are removed; recurring Toss checkout remains.
- [x] Exact negative searches, focused regressions, all frontend quality gates, and the production build pass.
- [x] No new dependency, package metadata change, backend/config/doc/runtime/Git-ref mutation, or generated repository output was introduced.

## Reference Documents (Tier 0-2)

**Injected Context** from `deliverables/agent/WI-20260717-ATS-003-handoff.md`:

| Tier     | Document                                                  | Reason                                          |
| -------- | --------------------------------------------------------- | ----------------------------------------------- |
| 0        | `docs/standards/core-principles.md`                       | System and ATStudio invariants                  |
| 0        | `docs/standards/development-standards.md`                 | Software Engineer implementation standards      |
| 1        | `docs/policies/quality-gates.md`                          | Required frontend verification gates            |
| 2        | `docs/standards/frontend-standards.md`                    | React UI, accessibility, and route conventions  |
| 2        | `.agents/skills/react-best-practices/AGENTS.md`           | React performance and state guidance            |
| 2        | `docs/design/api-spec.md`                                 | Current V1 frontend/API contract boundaries     |
| 2        | `docs/ui/`                                                | Current UI patterns and visual references       |
| Decision | `deliverables/user/REQ-20260716-ATS-004.md`               | Approved cleanup scope and constraints          |
| Decision | `deliverables/agent/WI-20260717-ATS-001-evidence-pack.md` | Approved disposition ledger and KEEP invariants |
| Decision | `deliverables/agent/WI-20260716-ATS-038-evidence-pack.md` | Residual inventory and proof expectations       |
| Decision | `deliverables/agent/WI-20260716-ATS-035-evidence-pack.md` | Prior frontend behavior evidence                |

**Injection Rules Applied**:

- Rule sources: `AGENTS.md` and the WI-003 handoff packet.
- Assignee: `se`.
- Task type: bounded React frontend cleanup.
- All injected documents and UI assets were read before editing.

## Decision Traceability

| Decision                                  | Evidence                                                                                                                                                                                                                         |
| ----------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `INT-R01`                                 | Deleted `frontend/src/api/playHistory.ts`; removed `PlayHistory` from `frontend/src/types/index.ts`; retained local history functions and pages.                                                                                 |
| `INT-R02`                                 | Deleted `frontend/src/api/downloadQueue.ts`; removed `DownloadQueueItem`; retained `frontend/src/api/downloads.ts` and active history behavior.                                                                                  |
| `INT-R08`                                 | Deleted `frontend/src/components/ui/DataTable.tsx` and `DataTable.module.css`.                                                                                                                                                   |
| `INT-R09`                                 | Removed `fetchUser`, `addTracksToPlaylistBatch`, `fetchSubscriptionPlanDetail`, `fetchAdminUserSubscriptionDetail`, `cancelMyBillingAgreement`, and `PaymentProvider`.                                                           |
| `INT-R10`                                 | Deleted `PlaylistCreatePage.tsx` and removed `/playlists/new`.                                                                                                                                                                   |
| `INT-R11`                                 | Deleted `frontend/src/features/.gitkeep` and `frontend/src/hooks/.gitkeep`; `frontend/public/.gitkeep` remains outside the user-authorized source boundary.                                                                      |
| `INT-P06`                                 | Renamed the three `DownloadQueuePage*` files to `DownloadHistoryPage*`; route, header, profile, and tests now use `/downloads`.                                                                                                  |
| `INT-P07`                                 | Renamed the three `PaymentReadOnlyPage*` files to `PaymentOperationsPage*`; admin route remains `/admin/payments`.                                                                                                               |
| `INT-P08`                                 | `PaymentOperationsPage.tsx:405-686`, `WhitelistChannelManagePage.tsx:127-219`, and `WhitelistChannelPage.tsx:250-277` implement controlled confirmations; `ConfirmDialog.tsx:28-45` suppresses repeat confirm/cancel while busy. |
| `INT-P09`                                 | `SubscriptionPaymentPage.tsx:306` and its stylesheet use provider-neutral identities.                                                                                                                                            |
| `INT-V01`-`INT-V05` frontend consequences | `router/index.tsx:172-183` retains only current download/recurring routes; `SubscriptionPaymentPage.tsx:31-32` recognizes only canonical recurring callbacks.                                                                    |

## Evidence Pointers

**Renamed page units**:

- `frontend/src/pages/subscriber/DownloadHistoryPage.tsx`, `.test.tsx`, `.module.css`
- `frontend/src/pages/admin/PaymentOperationsPage.tsx`, `.test.tsx`, `.module.css`

**Changed production files**:

- `frontend/src/api/admin.ts`
- `frontend/src/api/payments.ts`
- `frontend/src/api/playlists.ts`
- `frontend/src/api/subscriptions.ts`
- `frontend/src/api/userSubscriptions.ts`
- `frontend/src/components/ui/ConfirmDialog.tsx`
- `frontend/src/layouts/Header.tsx`
- `frontend/src/pages/admin/WhitelistChannelManagePage.tsx`
- `frontend/src/pages/subscriber/ProfilePage.tsx`
- `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx`
- `frontend/src/pages/subscriber/SubscriptionPaymentPage.module.css`
- `frontend/src/pages/subscriber/WhitelistChannelPage.tsx`
- `frontend/src/router/index.tsx`
- `frontend/src/types/index.ts`

**Changed or added tests**:

- `frontend/src/components/ui/ConfirmDialog.test.tsx:7`
- `frontend/src/pages/admin/PaymentOperationsPage.test.tsx:409`
- `frontend/src/pages/admin/WhitelistChannelManagePage.render.test.tsx:147`
- `frontend/src/pages/subscriber/DownloadHistoryPage.test.tsx`
- `frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx`
- `frontend/src/pages/subscriber/WhitelistChannelPage.test.tsx:178`
- `frontend/src/router/index.test.tsx`

**Deleted files**:

- `frontend/src/api/downloadQueue.ts`
- `frontend/src/api/playHistory.ts`
- `frontend/src/components/ui/DataTable.tsx`
- `frontend/src/components/ui/DataTable.module.css`
- `frontend/src/pages/subscriber/PlaylistCreatePage.tsx`
- `frontend/src/features/.gitkeep`
- `frontend/src/hooks/.gitkeep`
- Old `DownloadQueuePage*` and `PaymentReadOnlyPage*` paths, replaced by the renamed page units above.

## Commands & Outputs

- Exact negative-search script over production `frontend/src`:
  - Pass: server play-history client/type.
  - Pass: legacy download queue.
  - Pass: unused component/API exports.
  - Pass: playlist-create adapter.
  - Pass: one-time payment aliases.
  - Pass: old page identities.
  - Pass: production native confirms.
  - Pass: mock payment CSS identities.
  - Pass: both owned `.gitkeep` placeholders removed.
- `git diff --check` -> pass; only line-ending notices from the dirty shared worktree were emitted.
- `Get-FileHash frontend/tsconfig.tsbuildinfo` -> `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A` before build and after byte restoration.

## Tests

- `npm run typecheck` -> initial run found one stale `ApiResponse` import after export removal; the import was removed and the exact rerun passed.
- `npm run lint` -> pass, zero warnings.
- `npm run format` -> pass, all matched files formatted.
- WI-003 deliverable Prettier check -> pass for both Markdown outputs.
- Focused WI command over router, dialog, payment, whitelist, download, and recurring checkout -> 7 files, 39 tests passed.
- KEEP regression command over `PlayerBar`, player store, route guards, and public track pages -> 6 files, 50 tests passed.
- `npm test` -> 45 files, 255 tests passed.
- `npm run build -- --outDir <TEMP>` -> pass, Vite 6.4.3, 266 modules transformed. Temporary output was removed; no repository `dist` mutation was made.

## Behavior Evidence

- Confirmation opens before any mutation API call.
- Confirm and cancel/close/Escape are suppressed while pending; repeat clicks invoke the mutation once.
- Dialog remains visible with a busy control until mutation and refresh complete.
- Cancelled admin CSV export does not call its API.
- Payment settlement failure retains the current view without an extra refresh.
- Request-generation and refresh-queue tests remain green for payment, download, and whitelist surfaces.
- Canonical recurring Toss success/failure callbacks and route guards remain green.

## Risks / Rollback

**Risks**:

- Consumers using removed private SPA aliases will receive the normal not-found flow; no legacy redirects were authorized.
- `frontend/public/.gitkeep` remains for WI-005 because this WI was restricted to `frontend/src` plus package metadata if needed.
- The typed `window.prompt` guard for emergency refund/correction execution remains intentionally unchanged; WI-003 targeted the approved ten `window.confirm` calls and must preserve emergency operations.
- `frontend/tsconfig.tsbuildinfo` remains shown as modified because that was the user's pre-task working-tree state; its bytes were not changed by this WI.

**Rollback**:

- Restore the deleted clients/components/types and old page paths from the pre-WI revision.
- Restore old router/header/profile producers and subscription callback handling as one route batch.
- Revert `ConfirmDialog` and the three payment/whitelist page changes together with their tests.
- Remove only the two WI-003 deliverables when rolling back this WI; do not touch concurrent deliverables or the pre-existing `tsconfig.tsbuildinfo` change.

## Follow-ups

- `WI-20260717-ATS-005`: active-document, generated-artifact, ignore-policy, and remaining placeholder ownership.
- `WI-20260717-ATS-006`: independent full residual-reference and runtime/API/UI verification.

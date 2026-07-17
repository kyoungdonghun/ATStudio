# WI-20260717-ATS-003 Summary

## Status

Completed the approved React frontend cleanup within `frontend/src/`. No backend, schema/config, active documentation, runtime, Git ref, secret, package metadata, generated repository output, or unrelated artifact was changed.

## Implemented Cleanup

| Decision                                           | Result                                                                                                                                                    |
| -------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `INT-R01`                                          | Removed the obsolete server play-history API client and server response type while preserving browser-local play history.                                 |
| `INT-R02`                                          | Removed the obsolete download-queue API client and queue item type while preserving the active download-history implementation.                           |
| `INT-R08`                                          | Removed the unused `DataTable` component and stylesheet.                                                                                                  |
| `INT-R09`                                          | Removed the approved unused API exports and `PaymentProvider` type.                                                                                       |
| `INT-R10`                                          | Removed the `/playlists/new` route and `PlaylistCreatePage` adapter.                                                                                      |
| `INT-R11`                                          | Removed `.gitkeep` placeholders under the owned `frontend/src/features` and `frontend/src/hooks` paths.                                                   |
| `INT-P06`                                          | Renamed the active page identity to `DownloadHistoryPage` and canonicalized its route and producers to `/downloads`.                                      |
| `INT-P07`                                          | Renamed the admin page identity to `PaymentOperationsPage` while preserving `/admin/payments`.                                                            |
| `INT-P08`                                          | Replaced all ten production `window.confirm` calls in payment and whitelist surfaces with accessible, pending-aware, single-submit `ConfirmDialog` flows. |
| `INT-P09`                                          | Renamed checkout CSS identities from `mock*` to provider-neutral `provider*` names.                                                                       |
| Resolved `INT-V01`-`INT-V05` frontend consequences | Removed stale one-time payment aliases/callback handling and retained only recurring checkout routes.                                                     |

The active recurring routes are `/subscriptions/checkout`, `/subscriptions/checkout/success`, and `/subscriptions/checkout/fail`. Existing OAuth/PKCE, route guards, request-generation fences, browser-local play history, public playback, recurring checkout, download history, whitelist operations, and emergency typed admin operations were preserved.

## Validation

| Check                   | Result                                                                                |
| ----------------------- | ------------------------------------------------------------------------------------- |
| Exact negative searches | Pass: eight old route/symbol groups and two owned placeholders report zero residuals. |
| TypeScript              | `npm run typecheck` passed.                                                           |
| ESLint                  | `npm run lint` passed with zero warnings.                                             |
| Prettier                | `npm run format` passed.                                                              |
| Focused WI tests        | 7 files, 39 tests passed.                                                             |
| KEEP regression tests   | 6 files, 50 tests passed.                                                             |
| Full Vitest             | 45 files, 255 tests passed.                                                           |
| Production build        | Passed; 266 modules transformed.                                                      |
| Diff integrity          | `git diff --check` passed.                                                            |

The production build wrote to a temporary directory outside the repository. `frontend/tsconfig.tsbuildinfo` was restored to its pre-task bytes; its pre-build and post-restore SHA-256 remained `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A`.

## Scope Notes

- `frontend/public/.gitkeep` was not changed because the approved workspace boundary for this WI was `frontend/src/`; generated-artifact and ignore-policy ownership remains with WI-005.
- No frontend package metadata change was required.
- Pre-existing concurrent backend changes, untracked deliverables, logs, attachments, and runtime artifacts were left untouched.
- WI-003 now unblocks its frontend dependency for WI-005 and WI-006.

# B1 screen and endpoint inventory (WI-20261123-ATS-004)

Owner QA integration. Static inspection on 2026-11-23 against Git 199f86d, the retained November 21 nginx site and the dated November 21–23 release records. The [handoff](WI-20261123-ATS-004-handoff.md) lists Tier 0–2 inputs. [User report](../user/WI-20261123-ATS-004-summary.md). Work produced only these reports. No authenticated probe, privilege test, runtime change or fresh deployment was performed in this WI.

## Reproducible counts

In `frontend/src/router/index.tsx`, count `createLazyPage` declarations (53). Split at `const sellingRoutes:` and `const listeningRoutes:` before counting literal path-bearing declarations: 56 and 14, with one index redirect each. In B1 the 14 include the root redirect, admin shell and two catch-alls sharing the NotFound page. The remaining ten are the three public/login, three personal and four admin pages listed in the report. In plain listening mode the three pilot personal routes are omitted. See [inventory counting contract](../../docs/ui/atstudio-front-list.md).

Run `Get-ChildItem src/main/java/com/atstudio/atstudio/controller -Filter *.java | Select-String '^\s*@(Get|Post|Put|Patch|Delete)Mapping\b' | Group-Object Pattern` for the total; enumerate the files separately for controller counts. Current source totals 161 across 28 controllers, GET 83, POST 43, PUT 21, DELETE 14, PATCH 0. `@RequestMapping` prefixes, handlers outside this directory and implicit methods do not count. The [API reference](../../docs/design/api-spec.md#current-contract) reports a prior 155 across 26; its operation descriptions remain useful, but its total needs a date.

| Controller family | Source mappings | B1 surface |
|---|---:|---|
| Track | 13 | Public list, detail, stream and batch hydration; pilot download; ADMIN upload, edit, list/detail, thumbnail and conversion status/retry. Delete closed. |
| Tag | 7 | Public tag list and available tags; ADMIN tag list, create and update. Delete and deletion-impact closed. |
| ListeningCatalog; ListeningThumbnail | 3 + 1 | ADMIN catalog state, revisioned update and reconciliation; visitor published-track thumbnail. |
| Auth; User; Util | 7 + 9 + 6 | Login, refresh and logout; current user; public capabilities and personal daily quota. Other account onboarding, changes, lookups and utilities are outside the filter. |
| Download; License | 2 + 4 | Owner history, owner license list/detail. `/history/track-ids` and both ADMIN user-scoped license lookups closed. |
| Album; Playlist; Like | 8 + 9 + 6 | Closed. A player queue, playback or locally retained client state alone does not establish a server-side playlist/like feature. |
| Notice; Question; Setting | 7 + 7 + 1 | Closed. |
| CompanyCertification; WhitelistChannel | 7 + 6 | Closed. |
| Payment; Subscription; UserSubscription | 6 + 3 + 5 | Closed. A pilot grant permits scoped delivery and does not authorize checkout. |
| AdminPayment; AdminUserSubscriptionCorrection | 27 + 7 | Closed. Payment operations include settlement/correction tooling with its own side effects. |
| AdminSetting; AdminStats; AdminStorageIntegrity; AdminTrackAudioAnalysis; AdminWhitelistChannel | 1 + 1 + 1 + 1 + 5 | Closed under these controller families. Track conversion status/retry uses TrackController and remains available to operators. |
| SpaForward | 1 | Local fallback mapping. Deployed SPA shell delivery also uses nginx and the React router. |

## Permission and visibility cross-check

- [ListeningAccessFilter](../../src/main/java/com/atstudio/atstudio/security/ListeningAccessFilter.java) partitions by path and HTTP verb. [ListeningSecurityConfiguration](../../src/main/java/com/atstudio/atstudio/config/ListeningSecurityConfiguration.java) applies ADMIN/session rules after the first filter. SPA shell visibility is not authority over the API.
- [PilotAccess](../../src/main/java/com/atstudio/atstudio/service/PilotAccess.java) requires a specifically enumerated verified, non-deleted USER, a date-valid nonrenewing grant in Seoul time, positive active plan quota, no pending change or commercial order/agreement/payment. Publication and original-file eligibility also apply on delivery; past UUIDs do not grant unconditional access after withdrawal or unpublication.
- [nginx pilot include](../../deploy/single-vps/nginx/listening-pilot-locations.conf.template) adds personal pages and five API operations. The local retained deployment site `build/WI-20261121-ATS-009/active-site.conf` includes listening/public/operator locations and ends in a 404 catch-all. [Deployment evidence](WI-20261121-ATS-009-evidence-pack.md) and [two-user API acceptance](WI-20261121-ATS-010-evidence-pack.md) record distinct kinds of verification.
- [Admin layout](../../frontend/src/layouts/AdminLayout.tsx) filters its navigation. [TrackManagePage](../../frontend/src/pages/admin/TrackManagePage.tsx), [TrackEditPage](../../frontend/src/pages/creator/TrackEditPage.tsx) and [TagManagePage](../../frontend/src/pages/admin/TagManagePage.tsx) specialize publication gates and hide destructive actions. Track form reuses the shared edit form behind the listening gate.
- [LicenseManagePage](../../frontend/src/pages/admin/LicenseManagePage.tsx) and LicenseController implement operator lookup by user only in the full mode. The pilot's owner routes demand a USER; an ADMIN is not implicitly a participant. [Whitelist CSV](../../frontend/src/pages/admin/WhitelistChannelManagePage.tsx) is an operational export that can transition pending records; [payment operations](../../frontend/src/pages/admin/PaymentOperationsPage.tsx) handles settlement imports. No member directory CSV was identified in the user management controller/page inventory.
- Mobile file receipt, third-party registration, PG LIVE, external mail delivery and support resolution cannot be inferred from route counts. [November 23 account evidence](WI-20261123-ATS-001-evidence-pack.md) covers the new invite roster and states its live-login rate-limit boundary. Historical results remain dated. Read-only audit; reversal would remove these two reports only.

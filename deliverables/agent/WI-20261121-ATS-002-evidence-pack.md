---
version: 1.0
last_updated: 2026-11-21
project: ATS
owner: DocOps
category: evidence-pack
status: completed
---
# Pilot publication and personal records · WI-20261121-ATS-002

The [approved request](../user/REQ-20261121-ATS-001.md) and [handoff](WI-20261121-ATS-002-handoff.md) update the [canonical plan](../user/REQ-20261104-ATS-002.md#stage-b-rollout-2026-11-21), [current entry](../../docs/index.md#current-decision-entry) and [work summary](../user/WI-20261121-ATS-002-summary.md). The [earlier evidence review](WI-20261121-ATS-001-evidence-pack.md) remains a dated record.

## Local source evidence

| Capability | Source and implementation boundary |
|---|---|
| Publication and original file | [Publication transaction](../../src/main/java/com/atstudio/atstudio/service/ListeningCatalogService.java) checks readiness and activity before recording the selected catalog. [Public list/detail/stream](../../src/main/java/com/atstudio/atstudio/service/TrackService.java) uses catalog membership and active status. [Actual file service](../../src/main/java/com/atstudio/atstudio/service/DownloadService.java) checks active state and skips first-download grant checks for an existing license. Pilot work must apply current publication and user/grant eligibility to every file request and test concurrent publication change. |
| Read ownership | [History controller](../../src/main/java/com/atstudio/atstudio/controller/DownloadController.java) takes the authenticated principal. [History repository](../../src/main/java/com/atstudio/atstudio/repository/TrackDownloadRepository.java) filters by user. [License controller](../../src/main/java/com/atstudio/atstudio/controller/LicenseController.java) exposes personal list/detail alongside separate admin routes. [License service](../../src/main/java/com/atstudio/atstudio/service/LicenseService.java) ties a detail ID to the principal and the [detail DTO](../../src/main/java/com/atstudio/atstudio/dto/license/LicenseResponse.java) includes the issued code. Preserve owner history with stable code and avoid disclosing newly edited private metadata through old records. |
| Frontend and edge | [Router](../../frontend/src/router/index.tsx) uses authenticated license views and a subscriber-only selling history view. [Subscriber guard](../../frontend/src/router/SubscriberRoute.tsx) can redirect to subscription sales. [History](../../frontend/src/pages/subscriber/DownloadHistoryPage.tsx) calls history and daily count and also has optional bulk actions. Pilot should bind only necessary reads. [License list](../../frontend/src/pages/subscriber/LicenseListPage.tsx) already has individual re-download, and [detail](../../frontend/src/pages/subscriber/LicenseDetailPage.tsx) renders the issued code. The [HTTP client](../../frontend/src/api/client.ts) drops auth for listening visitor requests. Align protected requests from the public list with narrow [proxy](../../deploy/single-vps/nginx/listening-authenticated-admin-https.conf.template) and [Spring filter](../../src/main/java/com/atstudio/atstudio/security/ListeningAccessFilter.java) allowances. |
| Reuse and acceptance | [September TEST](../../docs/payment/acceptance-test-checklist.md#2026-09-08-acceptance-record) and [October bounded server trial](WI-20261024-ATS-019-evidence-pack.md) document first/repeat and authorized/denied cases in their respective environments. Future coverage includes two identities, stable issue code, concurrent actions, unpublishing, expiry, revocation and existing A and commercial behavior. No pilot runtime acceptance is claimed. |

## Validation and continuation

MA independently confirmed the scoped results and hash-identical earlier roadmap/index sections. The separate full-repository validator returned 5,748 broken links and four orphan warnings across the repository, with all 1,144 traceability IDs passing. The newly referenced targets and fragments were verified in the scoped check.

The scoped check passed for 68 relative link occurrences, including 13 heading fragments, across the two current entries and four requirement/WI documents. All four Tier 0 sources and the earlier roadmap entries were present. The [handoff](WI-20261121-ATS-002-handoff.md) lists the injected standards, security and quality policy and source paths. To reproduce the source review, inspect the mappings, owner queries, web guards and download branch in the linked files. The approved result is a local plan. Application tests, account creation, deployment and provider operations are future execution work. For a documentation rollback, restore the prior WI001 guidance in the two current entries and retain the historical records. No next WI is authorized within this planning scope.

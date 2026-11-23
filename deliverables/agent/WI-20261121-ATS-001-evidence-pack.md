---
version: 1.0
last_updated: 2026-11-21
project: ATS
owner: DocOps
category: evidence-pack
status: completed
---
# Evidence Pack · WI-20261121-ATS-001

## Scope and outputs

The approved [request](../user/REQ-20261121-ATS-001.md) and [handoff](WI-20261121-ATS-001-handoff.md) cover the local roadmap update. The [November 21 decision](../user/REQ-20261104-ATS-002.md#stage-b-rollout-2026-11-21) leads the canonical roadmap and the [current decision entry](../../docs/index.md#current-decision-entry). Both retain the November 17 entries and the historical six workstreams and five backup follow-ups. The [user summary](../user/WI-20261121-ATS-001-summary.md) identifies the next owner and action.

## Source reconciliation

| Source and evidence owner | Finding used for the decision |
|---|---|
| [September TEST acceptance](../../docs/payment/acceptance-test-checklist.md#2026-09-08-acceptance-record), MA and user | A fresh track created a download and license. A repeat added no quota, history or license. Real Gmail delivery and Toss TEST first payment, renewal retry, refund and a separate entitlement correction were observed in that bounded environment. |
| [October audio and reset closeout](WI-20261024-ATS-019-evidence-pack.md), MA and user | A temporary server trial exercised the authorized download, denial paths and direct media blocking. Separate human observations cover sound, seek and the reset/login/logout sequence. |
| [October grant reconciliation](../../docs/index.md#2026-10-25-stage-two-accountgrant-applied-access-restored-provider-work-open), MA | Applied and reconciled the existing ID 3 nonbilling daily-five grant through November 24 KST. Those dates do not configure a new cohort. |
| [November public listening deployment](../user/WI-20261107-ATS-019-summary.md) and [taxonomy verification](../user/WI-20261117-ATS-009-summary.md), MA and reviewers | Public listening, administrator publishing, and responsive views have bounded deployment evidence. Acceptance of the friends workflow on real devices is open. |
| [Login](../../frontend/src/pages/auth/LoginPage.tsx), [routing](../../frontend/src/router/index.tsx), [edge](../../deploy/single-vps/nginx/listening-authenticated-admin-https.conf.template) and [server](../../src/main/java/com/atstudio/atstudio/security/ListeningAccessFilter.java), local source | Current listening surfaces constrain member access. Prepare aligned changes to the login/UI and server-side file path while preserving existing public/admin surfaces. |
| [Download service](../../src/main/java/com/atstudio/atstudio/service/DownloadService.java), [unit tests](../../src/test/java/com/atstudio/atstudio/service/DownloadServiceTest.java), [concurrency contracts](../../src/test/java/com/atstudio/atstudio/service/DownloadConcurrencyContractTest.java), local source | Active tracks pass the initial track check. Existing licenses skip first-download subscription and quota checks, and ADMIN bypasses them. B1 must check participant validity and file eligibility before either path. |
| [Operating and privacy baseline](../user/WI-20261026-ATS-003-summary.md), [bounded restore](../user/WI-20261030-ATS-007-summary.md) and [schedule review](../user/WI-20261030-ATS-013-summary.md), operating and review records | Pilot terms, current recovery point, transfer capacity, incident response and pilot closeout remain launch work. The historical restore covers its dated snapshot. |

## Validation and continuation

The final scoped check passed for 48 relative link occurrences, including 13 heading fragments across the touched sections and four WI/REQ documents. All four Tier 0 inputs were present. MA independently compared the roadmap with the dated evidence, current code and link targets. The full repository validator still reported 5,748 broken links and four orphan warnings across the repository, including retained reconstruction copies. Its Tier 0 and 1,143 traceability-ID checks passed. This WI validates the added roadmap material only.

For reproduction, resolve each Markdown link in the new dated entries, REQ, WI summary, handoff and evidence pack against the containing file, then match each fragment to its Markdown heading. `python .agents/skills/validate-docs/scripts/validate_docs.py` runs the separate repository-wide check. The handoff records the injected standards and security policy. Source inspection used `rg -n` for the evidence dates, selection rules and first/repeat paths. The work used existing test reports. Runtime application tests and provider checks were outside this documentation task.

For a documentation rollback, remove this dated addendum and its index entry and restore the previous current pointer. For a future B1 rollout, verify the pilot can be turned off independently while public listening and administrator work continue. This documentation WI has no successor within the approved scope.

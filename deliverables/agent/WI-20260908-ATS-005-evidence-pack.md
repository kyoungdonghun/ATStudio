---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: docops
category: evidence-pack
status: stable
dependencies:
  - path: WI-20260908-ATS-005-handoff.md
    reason: Approved docs-only ownership
  - path: WI-20260908-ATS-004-evidence-pack.md
    reason: Completed independent review and frozen source identity
  - path: ../user/REQ-20260908-ATS-001.md
    reason: Approved development closeout and WI chain
---

# Evidence Pack: WI-20260908-ATS-005

> Purpose: Reconcile the approved development closeout with dated acceptance, source verification and remaining target-production decisions.

## Summary (one-liner)

PASS: bounded English current-state documentation and Korean REQ closeout complete;
documentation validation and scoped checks passed. Development completion is not release approval.

## Scope / DoD Check

- [x] Read existing WI005 handoff and WI001-004 evidence; WI004 completed with no actionable findings across 16 files.
- [x] Centralize checkout/source-versus-runtime state in the payment index; preserve historical evidence and generic unchecked acceptance rows.
- [x] Separate actual user/Toss TEST/Gmail acceptance, H2/fake-provider tests, real-browser API fixtures and read-only operations observations.
- [x] Preserve annual paid-period policy, financial outcome authority, historical records and explicit correction approval boundaries.
- [x] Separate completed fixes, unapproved maintenance candidates and target-production gates.
- [x] Documentation validator and scoped diff checks completed; approved development REQ closed after those checks.

## Reference Documents (Tier 0-2)

| Tier | Document | Use |
|---|---|---|
| Entry | WI005 handoff; AGENTS.md; approved REQ | Explicit delegated docs-only scope; no agents or commits |
| 0 | core-principles.md (STD-001), documentation-standards.md (STD-004), development-standards.md (STD-002), glossary.md (STD-005), all under docs/standards/ | MA-injected Tier 0; truncated metadata/development text supplemented from current files; truthful evidence and canonical terms |
| 1 | docs/policies/security-policy.md; docs/policies/quality-gates.md | Secret-safe records and bounded quality checks |
| 2 | WI001-004 Evidence Packs | Actual focused counts, independent review and source freeze |
| 2 | docs/payment/index.md, acceptance-test-checklist.md, known-limits-and-next-steps.md, user-flows.md, admin-operations-guide.md | Current guide layer and historical acceptance |
| 2 | docs/SR/SR-93.md; docs/design/payment-integration-design.md | Remaining production decisions and unchanged payment contract |
| 2 | docs/design/runtime-storage-operations.md | Read-only reference for the DB/public/private storage tuple; not edited |

Skills: `create-wi-evidence-pack`, `validate-docs`. Existing handoff verified before
writing. Rule source `.claude/config/context-injection-rules.json`: docops requires
tiers `[0, 1]` and core/documentation/glossary. ATS tag confirmed in
`.claude/config/workspace.json`; both configuration files were read only.

## Evidence Pointers

| Changed document | Owned update |
|---|---|
| [Payment index](../../docs/payment/index.md#2026-09-08-source-and-runtime) | Central current branch/client/cached-ref and built-versus-running identity |
| [Acceptance checklist](../../docs/payment/acceptance-test-checklist.md#2026-09-08-acceptance-record) | Compact dated actual/isolated results; historical entries and generic boxes preserved |
| [Known limits](../../docs/payment/known-limits-and-next-steps.md#2026-09-08-maintenance-candidates) | Maintenance candidates, stale branch claim removal, critical-defect boundary |
| [User flows](../../docs/payment/user-flows.md) | Sections 2-4: reported hint, charge history and explicit retained-period confirmation |
| [Admin guide](../../docs/payment/admin-operations-guide.md) | Sections 7 and 11: original receipt evidence, entered expiry, target summaries and absent edit/cancel API |
| [Payment integration design](../../docs/design/payment-integration-design.md) | Registration charge-history ownership, upgrade confirmation and callback/ADMIN safeguards |
| [SR-93](../../docs/SR/SR-93.md#remaining-production-gates) | Dated development closeout and named target-dependent gates |
| [REQ](../user/REQ-20260908-ATS-001.md) | Korean WI progress and development-only closure |
| This Evidence Pack; [WI005 summary](../user/WI-20260908-ATS-005-summary.md) | Detailed pointers and bounded user-facing outcome |

### Evidence Attribution And Artifact Root

MA supplied aggregate executions, actual acceptance, final runtime/DB observations
and visual inspection. Docops read WI001-004 evidence and the final preflight log;
it did not rerun application suites, financial flows, DB queries or browser checks.

All artifact basenames below resolve under:

`C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908`

They are repo-external, machine-local generated evidence, not committed artifacts.
WI004's [reviewed source snapshot](WI-20260908-ATS-004-evidence-pack.md#reviewed-source-snapshot)
identifies all 16 dirty-worktree files. MA reported no subsequent product/test changes.

### Aggregate And Focused Verification

| Owner / check | Result | Pointer |
|---|---|---|
| MA isolated backend build | Exit 0, 3m27s; 1,700 total, 1,681 passed, 19 skipped, 0 failures, 0 errors | closeout-backend-build.log; build-closeout/test-results/test/ |
| MA full JaCoCo | Lines 87.67%, methods 85.40%, branches 73.03%; threshold PASS | build-closeout/reports/jacoco/test/jacocoTestReport.xml |
| MA full frontend | 112 files, 1,487 tests passed, 0 skipped, 53.32s; statements 90.22%, lines 92.8%, functions 91.18%, branches 82.74% | closeout-frontend-coverage.log |
| MA frontend build / full Prettier | PASS | closeout-frontend-build.log; closeout-frontend-format.log |
| MA final frozen-source typecheck / ESLint | Both exit 0 | closeout-frontend-typecheck.log; closeout-frontend-lint.log |
| WI001 focused JUnit | 18 passed: DownloadServiceTest 10, RecurringRenewalCommandIntegrationTest 8; 0 failures/errors/skips | [WI001 tests](WI-20260908-ATS-001-evidence-pack.md#tests) |
| WI002 focused Vitest | 301 passed: SubscriptionPaymentPage 111, SubscriptionManagePage 80, PaymentOperationsPage 110; 0 failures/skips; owner typecheck/ESLint/format PASS | [WI002 tests](WI-20260908-ATS-002-evidence-pack.md#tests) |
| WI003 focused JUnit | 83 passed across 10 suites, including WI001's 18; 0 failures/errors/skips | [WI003 tests](WI-20260908-ATS-003-evidence-pack.md#tests) |
| WI004 independent review | 16 files, 0 actionable findings; existing 83-test XML independently parsed, not rerun | [WI004 findings and boundaries](WI-20260908-ATS-004-evidence-pack.md) |

The full backend skips comprise 18 gated MySQL cases plus one platform-dependent
LocalStorageServiceTest. Focused counts overlap each other and the full suites;
none is added to the aggregate totals. H2/fake-provider proof does not establish
operating MySQL concurrency, real midnight timers or multi-server scheduling.

The MA backend command uses the authorized external init script to isolate output:

```powershell
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-17'
.\gradlew.bat -I 'C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908/closeout-build.gradle' build --offline --no-daemon --console=plain
```

The original `build/libs/ATStudio-0.0.1-SNAPSHOT.jar` SHA-256 remains
`94A3324A6D1F4E2F414A84363AF5554D810FAA5627F6A7D4558790D71B4DFC60`.
The new artifact is isolated under `build-closeout`; the source fixes were not
deployed to the public backend and no old timestamps were repaired. The
[central snapshot](../../docs/payment/index.md#2026-09-08-source-and-runtime)
owns the live-versus-source distinction for the guide pack.

### Real Browser With Synthetic APIs

MA ran real Playwright Edge against Vite in fresh isolated contexts at 1440x900
and 390x900, then viewed the images and closed every context.

| Scenario | Observed result | Screenshots |
|---|---|---|
| Paid upgrade | Explicit 100,000 KRW today, retained YEARLY 2026-09-08 to 2027-09-08, next MONTHLY 19,900; cancel made no charge | upgrade-confirmation-1440.png; upgrade-confirmation-390.png |
| Card failure | Allowlisted INVALID_CARD_NUMBER hint visible, raw message absent, status check retained | card-failure-1440.png; card-failure-390.png |
| ADMIN correction | Expiry initially empty/required; blank blocked preview. Explicit 2027-09-08, user/subscription/refund IDs and DELUXE/YEARLY/CANCELLED appeared in request confirmation; cancel only | correction-confirmation-1440.png; correction-confirmation-390.png |

All API requests were intercepted and external access blocked. Zero actual writes,
page errors or overflow were observed. One stubbed POST preview per ADMIN viewport
is not real API or correction execution proof. Request confirmation was browser
checked; approval/execute target summaries have component-test/static evidence.
The initial ADMIN exact-label selector failed because its wrapped select label
included option text; changing the automation selector passed without a product fix.

### Earlier Joint User Acceptance

These events preceded the no-external-financial-call source closeout on the same
date. They are MA/user-reported real Toss TEST/Gmail evidence, not Docops execution.
Times are KST; amounts are KRW. No email address or provider key is retained here.

| Event | Exact result |
|---|---|
| Mail | User confirmed real Gmail email verification and password reset. |
| Initial annual purchase | Order 6 DONE, STANDARD YEARLY 99,000. |
| Upgrade | Order 7 DONE, DELUXE 100,000 difference. Annual paid period retained through 2027-09-08; MONTHLY selected for next renewal only. |
| Reservations and access | Pending downgrade/cadence reservation and cancellation passed; cancellation/reactivation preserved paid access. |
| Re-registration, 16:39:45 | Order 8 DONE, amount 0, period unchanged. Authorized local SUSPENDED fixture; not provider-real removed-key detection or proof of the later timestamp fix in the running JAR. |
| Renewal retry | Controlled REJECT_CARD_PAYMENT then logical-next-day actual success for 199,000, order 9 with two attempts. No clock change or elapsed-midnight test. Failure email reached Gmail spam in English. |
| Refund | Refund 2 SUCCEEDED for only the extra 199,000 renewal; provider CANCELED, balance 0, cancel count 1. Original 99,000 + 100,000 charges untouched; order 9 DONE and receipt ISSUED remain distinct original-charge evidence alongside the refund ledger. |
| Wrong correction disposition | Correction 1 targeted 2026-09-08 and was approved but never executed; user explicitly authorized scoped manual CANCELLED disposition. No edit/cancel API exists and this is not repeatable standing authorization. |
| Correct correction, 17:26:28 | Correction 2, exact CANCELLED expiry 2027-09-08, explicitly user-approved and executed successfully. |
| Downloads, 17:43:47 | Track 4 repeat added no quota/history/License. Track 5 created download 5 and License 5; daily total 2/20, remaining 18. |

### Read-Only Operations Closeout

| Evidence | MA result / exact pointer |
|---|---|
| Reachability | Final local/public frontend and API HTTP 200. Current TEST tunnel: https://generation-types-foam-ambient.trycloudflare.com (dated observation, not a stable production address). |
| Direct-backend CORS | OPTIONS /api/users/me with trusted public Origin and authorization,content-type returned 200 with exact Allow-Origin and allowed headers; untrusted Origin returned 403. |
| Public proxy preflight | Tunnel/Vite OPTIONS returned 204 without Allow-Origin for both origins. This is not cross-origin allowance; the current browser uses same-origin /api proxy and needs no cross-origin preflight. No product defect or production proxy/CORS proof inferred. |
| Capabilities | Remote SMTP enabled; password login/auth/reset true; Google/Kakao/Naver false; QA bootstrap false. No active OAuth flow to test. |
| Process continuity | Frontend 19932, backend 19376, tunnel 2372 unchanged; one application JVM observed, not midnight timer proof. |
| Script guards | closeout-environment-guards.log PASS 10; closeout-dry-run.log PASS 12; closeout-db-guards.log PASS 19. PSScriptAnalyzer not installed. |
| No-DB preflight | closeout-db-preflight-final.log: PASS, 43 CREATE TABLE statements, manifest expectation RECORDED. Docops read this log directly. No DDL execution, DB creation, backup or restore this turn. |
| Existing DB shape | atstudio: 43 tables, 511 columns, 175 distinct indexes, 91 FK columns. This is read-only shape evidence, not a new canonical manifest or restore proof. |
| Final account state | User 11 / Subscription 5 DELUXE YEARLY CANCELLED, paid expiry 2027-09-08, pending NULL. Agreement 2 CANCELLED, retry NULL, failure count 0, nextBilling 2027-09-08. |
| Historical ledgers | Orders DONE 4 / IN_PROGRESS 2 (historical invalid-card attempts); refund 2 SUCCEEDED 199,000; correction 1 CANCELLED, wrong 2026-09-08 target never executed; correction 2 SUCCEEDED, target CANCELLED 2027-09-08. Downloads today 2. State unchanged in closeout. |
| Media | Existing 10 historical missing references unchanged. Strict production tuple needs proper fresh media or an approved retained recovery decision. |

Final preflight **normalized-text** SHA-256 values (not raw-file or DB-manifest hashes):

- schema.sql: `c599d2e84f1ea9d21720d54f86e98ded9399856cdf39831bf2cca5e811fa5198`.
- seed.sql: `c848e0da6b9b35eb26563d6af3666b67fc119430c1afd888ea7b0246800a579a`.

The schema edit changed only a stale expiresAt + 1 day COMMENT sentence. Earlier
dated raw-file and disposable-manifest hashes remain valid for their stated units.

## Commands & Outputs

Docops used read-only `Get-Content`, `rg`, `git status`, and scoped diff inspection,
then `apply_patch` for the ten owned Markdown paths. An initial multi-file patch
failed on an unmatched SR paragraph and applied no changes; the corrected scoped
patch succeeded. One optional storage-guide lookup used a nonexistent guides/
path; the actual design/ path was located and read without a file change.

Final documentation-only checks executed by Docops:

- `python .agents/skills/validate-docs/scripts/validate_docs.py`: exit 0; Tier 0, internal links and index PASS; 671 traceability IDs matched supported formats.
- `git diff --check --` with the ten owned paths above: exit 0; tracked documentation hunks clean. Scoped diff was read and reviewed.
- Read-only PowerShell trailing-whitespace scan: all 10 owned Markdown files, including untracked REQ/deliverables, zero issues.
- `Get-FileHash -Algorithm SHA256` comparison against WI004's source table: all 16 source/test files matched, zero deltas.

MA independently supplied repository-wide docs validation exit 0 with the same
671-ID/link/Tier0/index result (`closeout-docs-validation.log` under the artifact
root), final full-code `git diff --check` exit 0 (CRLF notices only), and final
frozen-source frontend typecheck/ESLint exit 0. No application build or suite was
rerun by Docops; historical pages received no whole-file formatter rewrite.

## Risks / Rollback

Runtime artifacts and tunnel observations are dated local evidence and may be
replaced or expire. Existing accounts, historical media and the client worktree
were not repaired or modified. Rollback is a reviewed reverse patch of WI005's
documentation hunks only, preserving historical text and all other contributors'
files; no reset, checkout, deletion, DB or runtime action is appropriate.

## Follow-ups

WI005 is complete and blocks no subsequent WI. All five planned WIs are complete;
the approved development REQ is closed. Documentation is frozen for MA's final
validator/diff check and report.
Production remains OPEN under [SR-93](../../docs/SR/SR-93.md#remaining-production-gates);
maintenance candidates require their own approved scope. No agent, commit,
feature expansion or memory update was created by this WI.

## Related Documents

- [WI005 handoff](WI-20260908-ATS-005-handoff.md)
- [WI005 summary](../user/WI-20260908-ATS-005-summary.md)
- [Approved REQ](../user/REQ-20260908-ATS-001.md)
- [WI001 evidence](WI-20260908-ATS-001-evidence-pack.md)
- [WI002 evidence](WI-20260908-ATS-002-evidence-pack.md)
- [WI003 evidence](WI-20260908-ATS-003-evidence-pack.md)
- [WI004 evidence](WI-20260908-ATS-004-evidence-pack.md)

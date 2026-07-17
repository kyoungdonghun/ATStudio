---
version: 1.0
last_updated: 2026-07-17
project: ATS
owner: qa-integ / cr
category: audit
status: blocked
dependencies:
  - path: ../WI-20260717-ATS-006-handoff.md
    reason: Review scope and output contract
  - path: ../WI-20260717-ATS-001-evidence-pack.md
    reason: Approved 56-item execution ledger
  - path: ../WI-20260716-ATS-038-evidence-pack.md
    reason: Authoritative integrated source table
---

# WI-20260717-ATS-006 Integration Review

## Findings

### P1

No P1 findings.

### P2

#### P2-01 - The frontend recurring-payment contract still uses the removed provider identity

The backend serializes `PaymentProviderType.TOSS`, and the current API specification requires
`TOSS`, but the active frontend contract still declares `provider: 'TOSS_BILLING'` in all three
billing-agreement responses. It also retains `type: 'MOCK' | string` for checkout data. Current
frontend tests construct the same legacy values, so typecheck and Vitest can pass without testing
the real backend contract. This leaves `INT-V07` incomplete and violates the required absence of
legacy provider meanings from active paths.

- Frontend: `frontend/src/api/payments.ts:14`, `:31`, `:52`, `:59`
- Backend: `src/main/java/com/atstudio/atstudio/entity/enums/PaymentProviderType.java:3-5`
- Backend DTOs: `BillingAgreementPrepareResponse.java:13`,
  `BillingAgreementConfirmResponse.java:13`, `BillingAgreementResponse.java:11`
- Current contract: `docs/design/api-spec.md:53-56`, `:234-235`
- Representative stale fixture: `frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx:75`

Reproduce:

```powershell
rg -n "provider: 'TOSS_BILLING'|type: 'MOCK'" frontend/src
rg -n "PaymentProviderType provider" src/main/java/com/atstudio/atstudio/dto/payment
Get-Content src/main/java/com/atstudio/atstudio/entity/enums/PaymentProviderType.java
```

Repair and reset: change the frontend response contract and fixtures to `TOSS`, remove the `MOCK`
checkout meaning, then rerun frontend typecheck, full Vitest, build, provider negative searches,
and the backend/frontend API-contract gate.

#### P2-02 - Four active documents still promise the removed `/playlists/new` route

The router now has only `/playlists`, `/:playlistId`, and `/:playlistId/edit`; the approved
`PlaylistCreatePage` adapter was deleted. Four stable/current documents still tell users that
direct navigation to `/playlists/new` opens the creation modal, and `modal-list.md` still counts it
as a lazy route component. Client acceptance following these documents will exercise a route that
does not exist. `INT-R10` and `INT-P11` are therefore not closed.

- `docs/ui/modal-list.md:43-45`
- `docs/client/1-quick-checklist.md:51`
- `docs/client/2-full-feature-checklist.md:62`
- `docs/design/usecase/sound-playlist.md:21`
- Actual routes: `frontend/src/router/index.tsx:164-166`

Reproduce:

```powershell
rg -n "/playlists/new|PlaylistCreatePage" docs frontend/src/router/index.tsx
```

Repair and reset: remove the compatibility statements or approve and implement a real redirect;
then rerun route tests, client checklist review, route/count probes, and `validate-docs`.

#### P2-03 - Active policy/design paths still instruct operators to use deleted manual patches

All nine `src/main/resources/db/manual/*.sql` files are deleted and the current V1 contract is a
fresh-only `schema.sql`. Nevertheless, active stable documents still state that retained/current
databases require a dated manual patch or explicitly instruct a copied-database patch rehearsal.
These are operational instructions, not merely archived history. The archived
`p1-payment-db-integrity-design.md` references are correctly historical and are not part of this
finding. `INT-R12` and `INT-P11` remain incomplete.

- `docs/policies/security-policy.md:215`
- `docs/design/usecase/company-certification.md:210`
- `docs/design/p1-payment-integrity-remediation-design.md:621-627`
- Current baseline statement: `docs/design/db-schema.md:23-26`, `:167-176`

Reproduce:

```powershell
rg -n -i "manual patch|db/manual|20260(615|618|714|715|716)_" `
  docs/policies/security-policy.md `
  docs/design/usecase/company-certification.md `
  docs/design/p1-payment-integrity-remediation-design.md
Get-ChildItem src/main/resources/db/manual -File -ErrorAction SilentlyContinue
```

Repair and reset: make all active documents describe only the fresh-only V1 baseline and route any
future retained-data migration to a separate approved design. Rerun the active-document negative
search and `validate-docs`.

#### P2-04 - JWT startup guidance still implies that creating the ignored local file is sufficient

Automatic import of root `application-local.yml` was removed. The example and current DB design
correctly require an explicit additional location, but `JwtConfig` still tells an operator only to
create or update the file. Following the thrown startup message without an explicit load leaves the
secret unresolved and startup fails again. This is the old automatic-import meaning in an active
runtime path, so `INT-V08` is only partially complete.

- Misleading runtime message: `src/main/java/com/atstudio/atstudio/config/JwtConfig.java:23-36`
- Correct explicit-load instruction: `application-local.example.yml:1-2`
- Correct current contract: `docs/design/db-schema.md:169-176`

Reproduce:

```powershell
rg -n "application-local\.yml|SPRING_CONFIG_ADDITIONAL_LOCATION" `
  src/main/java/com/atstudio/atstudio/config/JwtConfig.java `
  application-local.example.yml docs/design/db-schema.md
```

Repair and reset: make the exception guidance explicitly require the additional-location mechanism,
then rerun configuration contract tests, startup-negative tests, and local explicit-config smoke.

### P3

#### P3-01 - Approved removals still have non-runtime references in active standards/configuration

The product symbols are gone, but the active frontend standard still presents deleted
`api/playHistory.ts` and `DataTable` as current examples, calls `features/` and `hooks/`
placeholders, and later repeats the deleted API file. `features/` no longer exists and `hooks/`
contains real code. The base YAML also retains a standalone Thymeleaf-settings comment after the
settings were removed. These do not create a runtime regression, but fail the requested zero-active-
reference check for `INT-R01`, `INT-R06`, `INT-R08`, and `INT-R11`.

- `docs/standards/frontend-standards.md:57`, `:60-62`, `:91`, `:218`
- `src/main/resources/application.yml:23`

Reproduce:

```powershell
rg -n "playHistory|DataTable|features/|hooks/" docs/standards/frontend-standards.md
rg -n -i "thymeleaf" src/main/resources/application.yml
```

#### P3-02 - The exact 1,128-file historical hash assertion is not independently reproducible

WI-005 reports a 1,128-file historical baseline and zero changes, but provides no saved manifest or
per-file hash artifact. The current Git index independently shows 1,127 tracked files in the stated
historical zones and zero tracked modifications/deletions; new WI/REQ records are untracked and
intentionally additive. This is an evidence gap, not evidence that history was rewritten.

Reproduce:

```powershell
$zones = @('deliverables/user','deliverables/agent','docs/SR','docs/audit','docs/retrospective')
(git ls-files -- $zones).Count
git diff --name-status HEAD -- $zones
git status --porcelain=v1 -- $zones
```

Repair recommendation: preserve a path/hash manifest when an exact historical-file count is used as
a release gate, or state which untracked pre-existing record accounts for the difference.

## Verdict

**FAIL / BLOCKED.** There are no P1 findings, but four P2 findings remain. WI-006 cannot satisfy
"no unresolved P1/P2", and WI-007 branch/worktree deletion must not run until repairs and affected
gate reruns complete. The financial, authorization, acceptance-ingress, full-playback, private-
document, tracked-client-asset, and historical-preservation KEEP boundaries showed no mechanical
weakening in this integration track.

## 56-Item Traceability

Status totals: **43 PASS, 9 PARTIAL/BLOCKED, 4 DEFERRED = 56**.

| INT | Status | Independent current evidence |
|---|---|---|
| `INT-K01` | PASS | Claim/fence/lease, reconciliation, audit, refund, lock, and storage-recovery sources remain; WI-002 focused fence/recovery evidence and WI-004 full/MySQL race evidence were read. |
| `INT-K02` | PASS | Startup guard, acceptance properties/public URL validation, host filter, CORS/ingress sources, production refusal, and disabled defaults remain. |
| `INT-K03` | PASS | `oauthAttempt.ts` and its consumers/tests remain; WI-003 KEEP regression suite reports pass. |
| `INT-K04` | PASS | `safeStorage`, cancellation/request-generation fences, and fallback paths remain; WI-003 KEEP regression suite reports pass. |
| `INT-K05` | PASS | `ProtectedRoute`, `SubscriberRoute`, and `usePublicCapabilities` remain; current route/security tests remain present. |
| `INT-K06` | PASS | `/subscriptions/checkout`, billing-agreement endpoints, recurring provider, renewal, reactivation, and cancellation paths remain. |
| `INT-K07` | PASS | Certification/private document, whitelist transition/audit/export/limit paths and `document_path` remain. |
| `INT-K08` | PASS | Browser-local `playHistory`, Player store, full-track stream, waveform/progress, and active history page remain; only stale standards prose is flagged under `INT-R01`. |
| `INT-K09` | PASS | Base/test YAML, JPA, MySQL, and H2 stack remain; schema/entity comparison is 39/39. |
| `INT-K10` | PASS | 1,127 tracked historical files have zero tracked diff; only additive untracked REQ/WI records exist. Exact WI-005 count reproducibility is P3-02. |
| `INT-K11` | PASS | Client PDF and manifest remain tracked with expected SHA-256 hashes. |
| `INT-K12` | PASS | Current branch is `codex/p1-acceptance-hardening`; both pre-consolidation tags exist and are ancestors of it. |
| `INT-K13` | PASS | `seed-client-demo.mjs` remains explicit and is not invoked by schema/seed baseline; PowerShell wrapper requires an explicit credential path outside dry-run. |
| `INT-R01` | PARTIAL | Backend/server client/table/tests are absent and local history remains; active frontend standard still names deleted `api/playHistory.ts` (P3-01). |
| `INT-R02` | PASS | Queue backend/client/model/table are absent; `/downloads`, history APIs, License/quota/accounting paths remain. |
| `INT-R03` | PASS | Four-argument `finalizeUpgrade` overload is absent; three-argument fenced finalizer remains. |
| `INT-R04` | PASS | `previewFile`/`preview_file` runtime/schema consumers are absent; full `audioFile` streaming remains. Negative contract-test strings are valid guards. |
| `INT-R05` | PASS | User-ID/nickname export snapshots are absent; email/channel/plan/order replay evidence remains. |
| `INT-R06` | PARTIAL | Thymeleaf properties/dependency/runtime are absent, but `application.yml:23` retains the removed-settings comment (P3-01). |
| `INT-R07` | PASS | Stale `PUT /api/settings/*` matcher is absent; public GET and `/api/admin/**` boundary remain. |
| `INT-R08` | PARTIAL | DataTable source/imports are absent; the active frontend standard still names it (P3-01). |
| `INT-R09` | PASS | Six approved frontend-only exports/types are absent; allowed backend recurring/admin operations remain. |
| `INT-R10` | PARTIAL | Route/page source are absent, but four current documents still promise the route (P2-02). |
| `INT-R11` | PARTIAL | All three `.gitkeep` files are deleted; active standard still describes placeholder directories (P3-01). |
| `INT-R12` | PARTIAL | Manual SQL file count is zero, but active policy/design instructions retain patch semantics (P2-03). |
| `INT-R13` | PASS | Demo/temp/attachment/expanded-capture paths are absent and narrowly ignored; retained ZIP hash/52 entries pass. |
| `INT-R14` | DEFERRED | 35 registrations are prunable and all 35 matching Claude branches are merged; WI-007 owns the actual prune/delete. |
| `INT-R15` | DEFERRED | `codex/p0-release-blockers`, `codex/payment-integration-clean`, and `dev/kyoung` have zero unique commits versus the official branch; WI-007 owns deletion. |
| `INT-R16` | DEFERRED | Both auxiliary worktrees are clean, unlocked, fully merged, and ports 5173/8080 have no listener; WI-007 remains blocked by P2 findings. |
| `INT-P01` | PASS | V1 crypto/property paths are absent; V2 key-ID AES-GCM/key-ring validation and negative behavior remain. |
| `INT-P02` | PASS | Fresh-only fail-closed schema is current; source comparison is 39 tables/39 entities. WI-004 records first apply pass and second apply failure. |
| `INT-P03` | PASS | `seed.sql` has one insert and exactly six plan rows; acceptance runner validates instead of duplicating baseline data. |
| `INT-P04` | PASS | Base automatic local import is absent; acceptance/local behavior uses `validate` and guarded V2 config. Runtime guidance defect is tracked under `INT-V08`. |
| `INT-P05` | PASS | Guarded V1 MySQL proof manager/script and final WI-004 logs/evidence remain; no proof command was rerun by this integration-only track. |
| `INT-P06` | PASS | `/downloads` and `DownloadHistoryPage` are active; old queue route/page/client are absent. |
| `INT-P07` | PASS | `PaymentOperationsPage` is active on unchanged `/admin/payments`; old source identity is absent. |
| `INT-P08` | PASS | Ten production native-confirm replacements are absent from current scan; controlled dialog busy/double-submit guards and focused tests remain. |
| `INT-P09` | PASS | Production recurring checkout CSS/comments use provider-neutral presentation names. |
| `INT-P10` | PASS | `tsconfig.tsbuildinfo` is absent from the index, present locally, and narrowly ignored; its generated local hash may change without status noise. |
| `INT-P11` | BLOCKED | Main counts and inventories agree, but P2-02/P2-03/P2-04 and P3-01 show active SoT is not fully reconciled. |
| `INT-P12` | PASS | Demo wrapper default credential path is empty, dry-run is secret-free, and non-dry-run requires an explicit path. |
| `INT-A01` | PASS | Both selected designs are archived in place with date/reason/replacement and indexed archived status. |
| `INT-A02` | PASS | Historical MySQL helper/log evidence remains in historical zones and is not used by current schema tooling; exact hash manifest gap is P3-02. |
| `INT-A03` | PASS | Screenshot ZIP remains unignored with 52 entries and expected SHA-256. |
| `INT-V01` | PASS | One-time payment controller/service/provider/DTO/frontend aliases/routes are absent; recurring path remains. |
| `INT-V02` | PASS | Direct subscription POST/request/error path is absent; current recurring and management paths remain. |
| `INT-V03` | PASS | `/api/utils/subscription-status` controller/service/DTO path is absent. |
| `INT-V04` | PASS | `/api/utils/user-type` controller/service/DTO path is absent. |
| `INT-V05` | PASS | ADMIN detail GET and frontend wrapper are absent; list/update/cancel emergency operations remain. |
| `INT-V06` | PASS | QA bootstrap is off by default, acceptance-gated, non-production guarded, and baseline-validating. |
| `INT-V07` | PARTIAL | Backend enum/schema/provider selection is `TOSS` and extension interfaces remain, but frontend response types/fixtures retain `TOSS_BILLING`/`MOCK` (P2-01). |
| `INT-V08` | PARTIAL | Automatic import is absent and the ignored local file was not read; runtime error guidance still implies file creation alone is sufficient (P2-04). |
| `INT-V09` | PASS | `package.json`, lockfile root, and lockfile package version all equal `0.1.0`. |
| `INT-V10` | DEFERRED | Three unique branch tips have exact archive tags; local branch deletion remains WI-007 work. |
| `INT-V11` | PASS | Four stopped runtime logs are absent and narrowly ignored; no 5173/8080 listener remains. |
| `INT-V12` | PASS | ADMIN subscription update/cancel code, security, UI callers, and current emergency-operation documentation remain. |

## Mechanical Verification

### Repository and diff

- Branch/HEAD at review: `codex/p1-acceptance-hardening` /
  `a96d2e0c5d249723bbf449b6834299a04cf2ad30`.
- Working diff from `HEAD`: 198 tracked files, 1,615 insertions, 17,553 deletions,
  plus approved untracked replacement/tests/deliverables.
- `git diff --check HEAD`: pass; line-ending advisories only.
- `git diff --cached --check`: pass.
- Staging was not modified. The sole staged change remained deletion of
  `frontend/tsconfig.tsbuildinfo`.

### Documentation, API, schema, routes, and counts

- `python .agents/skills/validate-docs/scripts/validate_docs.py`: pass.
  - Tier 0 present.
  - No broken internal links.
  - 433 supported traceability IDs.
  - Document index complete.
- Controller-derived mappings: 137 across 23 files; GET 65, POST 36, PUT 21,
  DELETE 15, PATCH 0.
- Expanded API-spec route set: 137; controller route set: 137. The only parser
  special case was the SPA regex route, manually confirmed at
  `SpaForwardController.java:9` and `api-spec.md:225`.
- Schema/entity names: exact 39/39 set equality.
- Seed baseline: one insert, six plan rows, no demo fixture statements.
- Router: 53 lazy screen symbols, 56 path entries, one index redirect.
- Main count documents agree with these source-derived values. Satellite route
  and migration semantics fail under P2-02 and P2-03.

### Removal and legacy-meaning searches

- Product/runtime searches are clean for removed server Play History, Download
  Queue, preview field, export snapshots, one-time payment APIs/providers,
  direct subscription creation, dormant utility/admin detail APIs, old page
  identities, playlist adapter, native production confirms, and manual SQL
  files.
- Negative contract-test strings and explicit current statements such as "no
  server Play History" were classified as safeguards, not active consumers.
- Active residual references are fully enumerated in P2-01 through P3-01.

### Generated artifacts and preservation

- `frontend/tsconfig.tsbuildinfo`: local file exists, index entries `0`, ignored
  by `frontend/.gitignore:5`; current local SHA-256 is
  `8F9D7AF00AA9D5580F21014F24D27ED2BAC9FC8B8680414716BA6E4FF3346A35`.
  This differs from WI-005 after later typecheck/build activity and is expected
  for an ignored generated cache.
- Generated/temp/runtime paths in `INT-R13`/`INT-V11`: absent.
- Screenshot ZIP: 52 entries, SHA-256
  `6C56C9139616A7936BA596DED6A2F35A8A29D6762DC210497EECDAE3437B09E8`.
- Client PDF SHA-256:
  `AFBA32CCE2460D5D38B80F4A88278E31D1F7344A2258E240BFD61DF74F4C6095`.
- Client PDF manifest SHA-256:
  `11A1C91AF1EBF77FBB5CE6B913D3EB197B3AC68D29F2E62B31231C553E0E398D`.

### Secret and historical scans

- High-confidence added-line scan: staged candidates `0`, unstaged candidates
  `0`.
- Non-ignored untracked text scan: 42 files, candidates `0` before this report.
- The ignored `application-local.yml` was not read. Binary screenshot content
  was not treated as text-diff scan coverage.
- Historical tracked zones: 1,127 tracked files, zero tracked modifications or
  deletions. New REQ/WI records are additive; see P3-02 for the exact-count gap.

### Cleanup preconditions

- `git worktree prune --dry-run`: 35 prunable registrations, no mutation.
- All 35 corresponding `claude/*` branches are merged with zero unique commits.
- Three other ordinary cleanup branches are merged with zero unique commits.
- `codex/acceptance-preview` and `codex/client-demo-stable` worktrees are clean,
  unlocked, and fully merged; no listener owns 5173 or 8080.
- Both rollback tags exist and are ancestors of the official branch.
- The 10/3/3 unique branch tips each have an exact `archive/pre-v1-*` tag.
- Preconditions are structurally sound, but deletion readiness is **false**
  while this working diff is uncommitted and P2 findings remain open.

## Evidence Limits and Required Reruns

- This integration track reran documentation validation, structural/count/set
  comparisons, negative searches, diff checks, secret scans, preservation
  checks, and Git/worktree dry-run/read-only checks.
- It read the final WI-002 through WI-005 test evidence but did not claim those
  historical test results as fresh execution. Backend/full MySQL/runtime smoke
  and frontend full gates belong to the other WI-006 tracks.
- After any repair, rerun the focused gate named in the finding and the final
  backend, frontend, docs, runtime/API/UI, diff/secret, and residual-reference
  gates. Do not start WI-007 until all P1/P2 findings are closed.

## Injected and Read Inputs

- Tier 0: `core-principles.md`, `development-standards.md`,
  `documentation-standards.md`, `glossary.md`.
- Tier 1: quality, security, access-control, versioning, and archive policies.
- Tier 2/current SoT: API spec, DB schema, UI/client/payment/docs indexes and
  active implementation paths needed for each mechanical comparison.
- Decision/evidence: WI-006 handoff, WI-001 56-item ledger, WI-038 source table,
  approved REQ, and final WI-002 through WI-005 evidence packs.

## Rollback

This review changed only this report. Rollback is deletion of
`deliverables/agent/WI-20260717-ATS-006/integration-review.md`; no product,
documentation, configuration, database, staging, branch, tag, or worktree
rollback is applicable.

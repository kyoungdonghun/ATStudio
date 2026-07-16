---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: qa-integ
category: audit
status: complete
related_wi: WI-20260716-ATS-027
---

# WI-20260716-ATS-027 Summary

## Decision

**NEEDS WI-028 REMEDIATION.** No P0 issue was found, but one P1 operator-safety defect, three P2 UI/API defects, and four P3 consistency or commit-hygiene defects remain. The cumulative REQ-20260716-ATS-002 worktree is not commit-ready. WI-028 must resolve or explicitly dispose them and incorporate the pending WI-025 review plus the completed WI-026 review.

## Findings

### [P1] F-026-01 - Admin lists can render records from an older filter or page

The whitelist and company-certification admin loaders commit every completion without a request generation or abort boundary. A slow response for an old filter/page can overwrite the newer result while the controls continue to show the new scope, allowing an administrator to review or mutate the wrong visible record set. WI-026 reported this while WI-027 was in progress; WI-027 independently confirmed it against the current source and UI contract.

- Whitelist loader and filter transition: `frontend/src/pages/admin/WhitelistChannelManagePage.tsx:71-96`, `frontend/src/pages/admin/WhitelistChannelManagePage.tsx:207-212`
- Certification loader: `frontend/src/pages/admin/CompanyCertManagePage.tsx:97-113`
- Required latest-request-wins contract: `docs/ui/screen-flow.md:62-68`
- Concurrent review evidence: `deliverables/user/WI-20260716-ATS-026-summary.md`

### [P2] F-026-02 - Concurrent whitelist mutations can drop the final refresh

The subscriber whitelist loader returns immediately while another load is active, but each successful mutation depends on `await load()` and different row actions can overlap. A second successful mutation can therefore skip its refresh and leave pre-mutation status, capacity, or primary-channel state visible until manual reload.

- Load guard: `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:80-117`
- Mutation refresh calls: `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:174-185`, `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:201-250`
- Concurrent review evidence: `deliverables/user/WI-20260716-ATS-026-summary.md`

### [P2] F-026-03 - Certification detail cannot close while its request is pending

Closing the certification detail modal does not invalidate the request or clear `detailLoading`, while modal openness is derived from that loading flag. Escape and the close button therefore cannot dismiss a pending detail view, and a late completion can repopulate it after the attempted close.

- Detail request and close state: `frontend/src/pages/admin/CompanyCertManagePage.tsx:115-135`
- Modal open condition: `frontend/src/pages/admin/CompanyCertManagePage.tsx:284-290`
- Shared close callbacks: `frontend/src/components/ui/Modal.tsx:26-32`, `frontend/src/components/ui/Modal.tsx:130-138`
- Concurrent review evidence: `deliverables/user/WI-20260716-ATS-026-summary.md`

### [P2] F-027-01 - Cross-origin whitelist export cannot reliably expose replay metadata

The export endpoint sends `Content-Disposition` and `X-Whitelist-Export-Batch-Id`, but the CORS configuration does not expose either response header. The frontend reads both and converts the batch header with `Number(...)`, so an allowed separately hosted frontend receives an unreadable header and can render/store `NaN` instead of a replayable batch ID.

- Backend headers: `src/main/java/com/atstudio/atstudio/controller/AdminWhitelistChannelController.java:77-85`
- CORS configuration: `src/main/java/com/atstudio/atstudio/config/CorsConfig.java:41-46`
- Frontend adapter: `frontend/src/api/admin.ts:203-216`
- Admin UI state and validation: `frontend/src/pages/admin/WhitelistChannelManagePage.tsx:149-167`
- Required API contract: `docs/design/api-spec.md:3276-3280`, `docs/design/api-spec.md:3294-3300`
- Coverage gap: `src/test/java/com/atstudio/atstudio/config/CorsConfigTest.java:12-35`, `frontend/src/api/adminWhitelistChannels.test.ts:23-63`

**Boundary:** this does not affect same-origin deployment or the current Vite proxy path. It affects a frontend origin that is allowed by CORS but differs from the API origin. WI-028 should expose both headers and add a CORS visibility plus adapter fallback/validation test.

### [P3] F-027-02 - Company-certification examples differ from the implemented wire contract

The API examples show raw objects although the controller returns the standard response envelope. The download section promises `application/pdf`, while the controller always returns `application/octet-stream` and the accepted evidence may also be JPEG. The React adapters currently follow the implementation, so the in-repo UI is not broken; external clients generated from the examples can parse the response or media type incorrectly.

- Standard envelope: `docs/design/api-spec.md:322-330`
- Divergent examples: `docs/design/api-spec.md:3317-3337`, `docs/design/api-spec.md:3390-3412`, `docs/design/api-spec.md:3447-3450`, `docs/design/api-spec.md:3472-3479`
- Controller behavior: `src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java:41-45`, `src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java:67-70`, `src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java:112-120`, `src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java:133-136`
- Frontend behavior: `frontend/src/api/companyCerts.ts:29-62`, `frontend/src/api/admin.ts:67-124`

### [P3] F-027-03 - Service-enabled subscription wording is stale

Runtime access correctly includes an unexpired `CANCELLED` subscription during its grace period, but one glossary definition, one use-case cross-reference, and a route comment still describe `ACTIVE` only. This is documentation/comment drift rather than a current authorization defect.

- Backend rule: `src/main/java/com/atstudio/atstudio/repository/UserSubscriptionRepository.java:23-35`
- Route behavior/comment: `frontend/src/router/SubscriberRoute.tsx:13-18`, `frontend/src/router/SubscriberRoute.tsx:45-50`
- Stale references: `docs/standards/glossary.md:93`, `docs/design/usecase/sound-track.md:208-224`

### [P3] F-027-04 - Reconciliation examples and operations guidance omit currency fields

The on-demand reconciliation DTO distinguishes local and provider currency, but the API example and safe diagnostic-field list show amounts without those currencies. There is no current frontend consumer for the on-demand response; this is an API/operations interpretation risk, especially for non-KRW provider data.

- Implemented response: `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentReconciliationResponse.java:70-106`
- Incomplete API example: `docs/design/api-spec.md:2232-2247`
- Incomplete operations field list: `docs/design/payment-operations-runbook.md:76-89`
- Frontend boundary: `frontend/src/api/admin.ts:494-559`

### [P3] F-027-05 - A generated TypeScript build-info file remains tracked and modified

`frontend/.gitignore` now ignores `*.tsbuildinfo`, but `frontend/tsconfig.tsbuildinfo` is already tracked and modified. A broad stage operation would include generated state. WI-028 must exclude it and either restore its content or intentionally remove it from tracking as a separately reviewed repository-hygiene change.

- Ignore rule: `frontend/.gitignore:1-8`
- Generated tracked artifact: `frontend/tsconfig.tsbuildinfo`

## Verified Consistency

- Source and published inventory counts agree: 149 REST mappings, 41 schema tables, 41 JPA entities with exact table-name parity, 53 visual pages, 13 agents, 92 SR entries, and 193 managed Markdown documents.
- The four product invariants remain aligned in source and documentation: public full-track listening, subscriber-only download limits, recurring billing-key payments, and a single application server.
- Payment support references are masked, and the implemented backend/frontend/admin presentation is aligned after WI-021.
- Documentation validation, staged-text hygiene (`git diff --check`), PDF verification, and PDF manifest/source hashes passed in this read-only audit.

## Environment-Conditional Holds

- Fresh disposable MySQL migration, retained-database concurrency, and `EXPLAIN` evidence remain required; current static schema parity does not close them.
- Live Toss billing/refund/callback behavior remains provider-conditional.
- Trusted-proxy, separate-origin CORS, public callback, secret injection, canonical-path, and symlink-host behavior remain deployment-conditional.
- The frozen client branch was not mutated or revalidated in this WI. WI-022's supplied evidence is historical input, not a current branch assertion.
- WI-023/WI-024 client-demo evidence belongs to REQ-20260716-ATS-003 and does not close REQ-20260716-ATS-002 production gates.

## Worktree Classification

At the final audit snapshot, the shared worktree contained 292 tracked modifications and 296 untracked files, including these two WI-027 deliverables and the concurrently completed WI-026 outputs.

- **REQ-002 commit candidates:** 291 tracked files after excluding `frontend/tsconfig.tsbuildinfo`; 52 untracked product/test/manual-SQL files; 3 PDF tooling/manifest files; and 66 REQ-002 WI/design deliverables.
- **Separate REQ-003 work:** 7 REQ/WI deliverables and 2 demo scripts.
- **Generated output:** 73 demo-seed assets, 53 screenshot/archive artifacts, 35 temporary PDF render files, and the tracked TypeScript build-info file.
- **Runtime/local/private:** 4 untracked Vite/Cloudflare logs, `.codex-remote-attachments/`, and ignored local secrets, build outputs, dependencies, uploads, IDE files, and server logs.

No file was staged, committed, deleted, reverted, or mutated outside the two authorized WI-027 deliverables.

## WI-028 Gate

WI-028 should ingest the pending WI-025 review, completed WI-026 review, and this WI; disposition F-026-01 through F-026-03 and F-027-01 through F-027-05; select only the classified REQ-002 artifacts; then run the backend, frontend, documentation, PDF, database, provider, deployment, and staged-diff checks prescribed in the Evidence Pack.

---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: se
category: implementation
status: complete
related_wi: WI-20260716-ATS-029
---

# WI-20260716-ATS-029 Summary

## Decision

**IMPLEMENTED WITH A WI-028 INTEGRATION HOLD.** The WI-029-owned frontend races, certification
detail close behavior, separate-origin export contract, certification examples, subscription
terminology, and generated-file staging guard are remediated with focused coverage. The API side of
F-027-04 is aligned, but the currency wording in `docs/design/payment-operations-runbook.md` remains
owned by concurrent WI-028 and was not edited. The focused frontend and backend checks pass.

## Finding Disposition

| Finding | Result | Evidence |
|---|---|---|
| F-026-01 | Closed | Both admin lists fence success, failure, and loading completion by request generation; reverse-order tests cover current rows, pagination/edit state, stale failure, and latest loading ownership. |
| F-026-02 | Closed | Subscriber whitelist loads coalesce overlapping refresh requests and perform a final server-state refresh after concurrent mutations. |
| F-026-03 | Closed | Certification detail uses explicit open state; Escape/button close invalidates the request and late success/failure cannot reopen the dialog. |
| F-027-01 | Closed in owned files | CORS exposes both export headers; the adapter rejects invalid initial metadata, safely falls back to the requested replay ID when metadata is filtered, and rejects a mismatched valid replay ID. |
| F-027-02 | Closed | Certification examples use the standard response envelope and document the implemented generic binary media contract. |
| F-027-03 | Closed | Route comment, glossary, and track use-case use the service-enabled definition: `ACTIVE`, or `CANCELLED` before period end. |
| F-027-04 | API side closed; WI-028 hold | The on-demand example includes local/provider currencies and distinguishes its shape from persisted Incidents. The operations runbook remains a WI-028-owned file. |
| F-027-05 | Closed for commit hygiene | `frontend/tsconfig.tsbuildinfo` stayed byte-identical and must be omitted from any later staging allowlist. |

## Changed Files

- `frontend/src/pages/admin/WhitelistChannelManagePage.tsx`
- `frontend/src/pages/admin/WhitelistChannelManagePage.render.test.tsx`
- `frontend/src/pages/admin/CompanyCertManagePage.tsx`
- `frontend/src/pages/admin/CompanyCertManagePage.test.tsx`
- `frontend/src/pages/subscriber/WhitelistChannelPage.tsx`
- `frontend/src/pages/subscriber/WhitelistChannelPage.test.tsx`
- `frontend/src/api/admin.ts`
- `frontend/src/api/adminWhitelistChannels.test.ts`
- `frontend/src/router/SubscriberRoute.tsx`
- `src/main/java/com/atstudio/atstudio/config/CorsConfig.java`
- `src/test/java/com/atstudio/atstudio/config/CorsConfigTest.java`
- `docs/design/api-spec.md`
- `docs/standards/glossary.md`
- `docs/design/usecase/sound-track.md`
- `deliverables/user/WI-20260716-ATS-029-summary.md`
- `deliverables/agent/WI-20260716-ATS-029-evidence-pack.md`

## Verification

- PASS: focused Vitest, 4 files and 25 tests.
- PASS: focused backend `CorsConfigTest` after concurrent WI-028 restored test compilation.
- PASS: frontend TypeScript typecheck.
- PASS: targeted ESLint, targeted Prettier, and full frontend `npm run format`.
- PASS: follow-up `CompanyCertManagePage.test.tsx`, 1 file and 7 tests after applying Prettier to
  `CompanyCertManagePage.tsx` only.
- PASS: documentation validation, targeted diff whitespace check, and Java main-source compilation.
- PRESERVED: `frontend/tsconfig.tsbuildinfo`, 5,421 bytes, SHA-256
  `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A` before and after all checks.

## Commit Boundary

No file was staged, committed, pushed, deleted, untracked, or reverted. A later commit must use an
explicit path allowlist and omit `frontend/tsconfig.tsbuildinfo`, runtime logs, generated output,
client worktree content, and WI-028-owned files until their owning work is complete.

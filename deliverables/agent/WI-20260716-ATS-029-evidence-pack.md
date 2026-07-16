---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: se
category: evidence
status: complete
related_wi: WI-20260716-ATS-029
---

# Evidence Pack: WI-20260716-ATS-029

## Summary

- Implemented WI-029-owned remediation for F-026-01 through F-026-03 and F-027-01 through
  F-027-05 without staging, committing, deleting, untracking, or touching WI-028-owned source.
- Verdict: `IMPLEMENTED_WITH_WI_028_INTEGRATION_HOLD`.
- The remaining integration hold is the WI-028-owned runbook currency wording.

## Scope / DoD Check

- [x] Admin whitelist and certification lists use latest-request-wins ownership for success, failure,
  pagination/edit state, and loading completion.
- [x] Overlapping subscriber whitelist mutations queue and converge on a final refresh.
- [x] Certification detail closes while loading and ignores late success/failure.
- [x] CORS exposes `Content-Disposition` and `X-Whitelist-Export-Batch-Id`.
- [x] Export adapter validates initial/replay batch metadata and uses a bounded replay fallback.
- [x] Company-certification examples match the response envelope and binary media contract.
- [x] Service-enabled subscription wording matches runtime grace-period behavior.
- [x] On-demand reconciliation docs include local/provider currencies and distinguish persisted
  Incidents.
- [ ] WI-028-owned operations runbook currency wording is not yet present; ownership was preserved.
- [x] Focused frontend tests, typecheck, lint, code formatting, docs validation, diff check, and Java
  source compilation passed.
- [x] Focused backend CORS test passed after concurrent WI-028 restored test compilation.
- [x] `frontend/tsconfig.tsbuildinfo` remained byte-identical and is explicitly excluded from staging.
- [x] Both required WI-029 deliverables were created.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Product invariants and execution boundaries |
| 0 | `docs/standards/development-standards.md` | Frontend/backend implementation and testing rules |
| 0 | `docs/standards/documentation-standards.md` | Deliverable and documentation rules |
| 0 | `docs/standards/glossary.md` | Canonical subscription terminology |
| 1 | `docs/policies/quality-gates.md` | Required verification and evidence |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | React data-flow and effect guidance |
| 2 | `docs/standards/frontend-standards.md` | SPA async, route, and accessibility standards |
| 2 | `docs/ui/screen-flow.md` | Latest-request-wins list contract |
| 2 | `docs/design/api-spec.md` | Export, reconciliation, and certification contracts |
| 2 | `docs/design/usecase/sound-track.md` | Subscriber playlist cross-reference |
| Context | `deliverables/user/REQ-20260716-ATS-002.md` | Approved cumulative remediation scope |
| Context | `deliverables/user/WI-20260716-ATS-026-summary.md` | Frontend findings |
| Context | `deliverables/agent/WI-20260716-ATS-026-evidence-pack.md` | Reproduction and coverage requirements |
| Context | `deliverables/user/WI-20260716-ATS-027-summary.md` | Integration findings |
| Context | `deliverables/agent/WI-20260716-ATS-027-evidence-pack.md` | Three-way contract evidence |

**Injection rules applied**

- Source: `deliverables/agent/WI-20260716-ATS-029-handoff.md`
- Assignee: `se`
- Task type: frontend/CORS/document remediation

## State-Machine Reasoning

### Admin list ownership

`WhitelistChannelManagePage` and `CompanyCertManagePage` increment a request generation before each
list request. Only the current generation may commit success, failure, or `finally` state. Effect
cleanup invalidates an in-flight generation before a new filter/page scope or unmount can accept its
completion.

### Subscriber final refresh

`WhitelistChannelPage` marks every load request as queued. One active loader drains the queue in a
loop. If another mutation finishes while a refresh is active, it sets the queue flag rather than
dropping the request; the loader performs another complete channel/subscription read before releasing
loading ownership.

### Certification close authority

Certification detail openness is explicit and separate from loading/content/error state. Opening
captures a detail request generation. Escape/button close increments that generation, closes the
dialog immediately, and clears loading state. Late success, failure, and `finally` handlers fail the
generation check and cannot reopen or repopulate the dialog.

### Export metadata validation

Initial exports require a positive safe-integer batch header. Replay responses use the already
validated requested ID only when the browser filters or returns invalid batch metadata; a valid but
different response ID is rejected. This prevents `NaN`, unsafe numeric values, and cross-batch replay
confusion.

## Evidence Pointers

### F-026-01

- `frontend/src/pages/admin/WhitelistChannelManagePage.tsx:70-108`
- `frontend/src/pages/admin/WhitelistChannelManagePage.render.test.tsx:78-144`
- `frontend/src/pages/admin/CompanyCertManagePage.tsx:84-129`
- `frontend/src/pages/admin/CompanyCertManagePage.test.tsx:141-181`

### F-026-02

- `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:80-134`
- `frontend/src/pages/subscriber/WhitelistChannelPage.test.tsx:129-178`

### F-026-03

- `frontend/src/pages/admin/CompanyCertManagePage.tsx:86-166`
- `frontend/src/pages/admin/CompanyCertManagePage.tsx:316-321`
- `frontend/src/pages/admin/CompanyCertManagePage.test.tsx:183-212`

### F-027-01

- `src/main/java/com/atstudio/atstudio/config/CorsConfig.java:41-48`
- `src/test/java/com/atstudio/atstudio/config/CorsConfigTest.java:38-48`
- `frontend/src/api/admin.ts:185-239`
- `frontend/src/api/adminWhitelistChannels.test.ts:23-96`
- `docs/design/api-spec.md:3279-3289`

### F-027-02

- `docs/design/api-spec.md:3325-3350`
- `docs/design/api-spec.md:3372-3391`
- `docs/design/api-spec.md:3404-3430`
- `docs/design/api-spec.md:3463-3472`
- `docs/design/api-spec.md:3492-3504`

### F-027-03

- `frontend/src/router/SubscriberRoute.tsx:12-15`
- `docs/standards/glossary.md:93`
- `docs/design/usecase/sound-track.md:208-224`

### F-027-04

- `docs/design/api-spec.md:2198-2210`
- `docs/design/api-spec.md:2242-2251`
- External hold: `docs/design/payment-operations-runbook.md:80-89` remains WI-028-owned and still
  lists local/provider amount without both currencies at this snapshot.

### F-027-05

- Preserved path: `frontend/tsconfig.tsbuildinfo`
- Length: 5,421 bytes
- SHA-256 before/after:
  `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A`
- Later staging rule: use an explicit path allowlist and omit this tracked generated file.

## Verification Commands and Results

All frontend commands ran from `frontend/`; other commands ran from the repository root.

```powershell
npx vitest run src/pages/admin/WhitelistChannelManagePage.render.test.tsx src/pages/admin/CompanyCertManagePage.test.tsx src/pages/subscriber/WhitelistChannelPage.test.tsx src/api/adminWhitelistChannels.test.ts
```

- Final PASS: 4 files, 25 tests.
- An earlier iteration passed 23 of 24 tests; one new certification race test used a detached filter
  after the page entered loading state. The test was corrected to use React StrictMode's overlapping
  effect generations, then the final command passed.

```powershell
npm run typecheck
```

- PASS: `tsc --noEmit`.
- `frontend/tsconfig.tsbuildinfo` hash was identical before and after.

```powershell
npx eslint src/pages/admin/WhitelistChannelManagePage.tsx src/pages/admin/WhitelistChannelManagePage.render.test.tsx src/pages/admin/CompanyCertManagePage.tsx src/pages/admin/CompanyCertManagePage.test.tsx src/pages/subscriber/WhitelistChannelPage.tsx src/pages/subscriber/WhitelistChannelPage.test.tsx src/api/admin.ts src/api/adminWhitelistChannels.test.ts src/router/SubscriberRoute.tsx --max-warnings 0
```

- PASS: 0 errors, 0 warnings.

```powershell
npx prettier --check src/pages/admin/WhitelistChannelManagePage.tsx src/pages/admin/WhitelistChannelManagePage.render.test.tsx src/pages/admin/CompanyCertManagePage.test.tsx src/pages/subscriber/WhitelistChannelPage.tsx src/pages/subscriber/WhitelistChannelPage.test.tsx src/api/admin.ts src/api/adminWhitelistChannels.test.ts src/router/SubscriberRoute.tsx
```

- PASS for the WI-029-formatted code/test set.

### Formatting Follow-up

The integrated full gate later identified `CompanyCertManagePage.tsx` as the only remaining frontend
Prettier failure. The follow-up changed that owned file and the two WI-029 deliverables only.

```powershell
npx prettier --write src/pages/admin/CompanyCertManagePage.tsx
npx prettier --check src/pages/admin/CompanyCertManagePage.tsx
npm run format
npx vitest run src/pages/admin/CompanyCertManagePage.test.tsx
```

- PASS: targeted Prettier check.
- PASS: full frontend Prettier check through `npm run format`.
- PASS: focused CompanyCert test, 1 file and 7 tests.
- PRESERVED: `frontend/tsconfig.tsbuildinfo`, 5,421 bytes, SHA-256
  `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A`.

```powershell
python .agents\skills\validate-docs\scripts\validate_docs.py
git diff --check -- <WI-029 owned code/test/doc paths>
.\gradlew.bat compileJava
```

- PASS: all Tier 0 documents, internal links, 417 supported traceability IDs, and document index.
- PASS: no diff whitespace errors; output contained LF-to-CRLF warnings only.
- PASS: Java main-source compilation.

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.config.CorsConfigTest"
```

- Final PASS: `compileTestJava` and `CorsConfigTest` completed successfully.
- Two earlier retries were temporarily blocked by concurrent WI-028 constructor updates (first two
  errors, then one). WI-029 did not modify those files; the owning work completed before the final
  retry.

## Changed Files and Traceability

| File | Finding(s) | Change |
|---|---|---|
| `frontend/src/pages/admin/WhitelistChannelManagePage.tsx` | F-026-01 | List request generation fence |
| `frontend/src/pages/admin/WhitelistChannelManagePage.render.test.tsx` | F-026-01 | Reverse completion, stale failure, pagination/edit/loading checks |
| `frontend/src/pages/admin/CompanyCertManagePage.tsx` | F-026-01, F-026-03 | List/detail generations, explicit detail open state, and follow-up Prettier alignment |
| `frontend/src/pages/admin/CompanyCertManagePage.test.tsx` | F-026-01, F-026-03 | Stale list completion and close-during-load tests |
| `frontend/src/pages/subscriber/WhitelistChannelPage.tsx` | F-026-02 | Coalesced final refresh loop |
| `frontend/src/pages/subscriber/WhitelistChannelPage.test.tsx` | F-026-02 | Overlapping mutation/final-state test |
| `frontend/src/api/admin.ts` | F-027-01 | Positive safe-integer parsing, replay fallback, mismatch rejection |
| `frontend/src/api/adminWhitelistChannels.test.ts` | F-027-01 | Missing/invalid/mismatched header coverage |
| `src/main/java/com/atstudio/atstudio/config/CorsConfig.java` | F-027-01 | Exposed response headers |
| `src/test/java/com/atstudio/atstudio/config/CorsConfigTest.java` | F-027-01 | CORS contract assertion |
| `docs/design/api-spec.md` | F-027-01, F-027-02, F-027-04 | CORS, envelope/media, and currency contract examples |
| `frontend/src/router/SubscriberRoute.tsx` | F-027-03 | Service-enabled access comment |
| `docs/standards/glossary.md` | F-027-03 | Canonical grace-period definition |
| `docs/design/usecase/sound-track.md` | F-027-03 | Playlist precondition alignment |
| `deliverables/user/WI-20260716-ATS-029-summary.md` | F-027-05, all | User-facing outcome and staging exclusion |
| `deliverables/agent/WI-20260716-ATS-029-evidence-pack.md` | F-027-05, all | Reproducibility, mapping, rollback, and hold evidence |

## Risks / Rollback

**Risks**

- F-027-04 is not fully closed until WI-028 adds local/provider currency guidance to its owned
  operations runbook.
- Same-origin tests do not replace a future deployed separate-origin smoke check.

**Rollback**

- Remove only the WI-029 request-generation, queue, explicit-open, adapter, CORS, terminology, and
  API-example hunks listed above; do not restore whole files because they contain concurrent work.
- Remove the two WI-029 deliverables if the WI is withdrawn.
- Do not reset, checkout, delete, untrack, or broadly stage any shared worktree path.

## Follow-up Gate

- WI-028 must finish its owned runbook wording.
- After WI-028 completion, rerun relevant payment tests, docs validation, and a final explicit-path
  diff review before WI-030.

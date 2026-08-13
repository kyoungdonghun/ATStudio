---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: qa-fe
category: audit
status: complete
dependencies:
  - path: WI-20260809-ATS-054-qa-handoff.md
    reason: Independent QA scope, acceptance criteria, and constraints
  - path: WI-20260809-ATS-054-handoff.md
    reason: Approved functional and quality contract
---

# QA Frontend Result: WI-20260809-ATS-054

## Findings

### QA-FE-054-001 [P1] A background target switch can retire an accepted correction mutation without recovery

- `frontend/src/pages/admin/UserSubscriptionManagePage.tsx:172-178` leaves every
  row's correction action able to replace `correctionTarget` while the current
  workflow owns a mutation.
- On that target change,
  `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:286-312`
  aborts the current controller, advances the request generation, clears
  `busy`, confirmation, errors, and correction state, and starts the new
  target's open lookup.
- The retired request then fails the current-generation check at `:513-525`.
  Consequently, an accepted request, approval, or execution whose server result
  is now unknown does not enter the required bounded reconciliation path.
  Its `finally` path at `:597-598`, `:633-638`, or `:683-688` can also release
  the shared pending ref after the client-side abort even though the original
  server mutation may already have committed.
- The pending-execute test at
  `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:654-693`
  exercises Escape, backdrop, header close, cancel, and duplicate click, but it
  does not activate another row during the pending mutation. The target-switch
  test at `:423-478` covers only a read-only open lookup.
- Impact: a local entitlement or billing-agreement mutation can commit for
  target A without visible recovery while target B becomes actionable. This
  breaks immutable target/generation ownership, unknown-outcome preservation,
  and the at-most-one-mutation safety contract.
- Required remediation: keep the parent target immutable for the full accepted
  mutation/reconciliation lifetime, or carry a target-scoped pending owner that
  makes any allowed retarget read-only until A reaches a known result. Do not
  abort or discard accepted mutation recovery on target replacement. Add
  deferred request/approval/execution retarget tests with exact invocation and
  recovery-read counts.

### QA-FE-054-002 [P2] Company review retarget detaches the pending result and skips authoritative list refresh

- `frontend/src/pages/admin/CompanyCertManagePage.tsx:174-183` allows
  `openDetail` to replace the selected detail and close the review dialog even
  while `reviewPendingRef` owns a submitted review.
- The old operation is generation-fenced, but `confirmReview` at `:216-244`
  returns immediately when the original owner no longer matches. A late
  success therefore skips both the exact-detail refresh and `loadCerts()`, and
  a late failure is suppressed without target-specific feedback.
- The retarget tests at
  `frontend/src/pages/admin/CompanyCertManagePage.test.tsx:362-489` confirm that
  background retarget is accepted while pending and that the old result is
  discarded; they do not require the committed A row to converge through an
  authoritative list reload.
- Impact: the server may durably review certification A while the ADMIN list
  remains stale and the operation is no longer visibly attached to A. Although
  B stays read-only until settlement, the submitted mutation is still detached
  from canonical UI convergence.
- Required remediation: block retarget while review ownership is pending, or
  retain an immutable A operation owner and independently reload the canonical
  list after A succeeds without writing into B's detail state. Assert list
  convergence and target-specific failure handling for late A outcomes.

### QA-FE-054-003 [P3] Required current-behavior documentation was not updated

- The scoped diff contains no documentation change although WI-054 explicitly
  requires current ADMIN correction and modal documentation to match the
  implementation.
- `docs/ui/screen-flow.md:302-303` and
  `docs/payment/admin-operations-guide.md:270-271` still describe typed
  confirmation as work that "remains assigned" to WI-054 rather than current
  behavior. The exact normalized execution phrase and the five owner-flow
  pending ownership rules are not recorded as the completed contract.
- Required remediation: update the current modal/screen-flow/correction
  documentation in English, including exact phrase normalization, execute-only
  scope, immutable pending ownership, and approval's ordinary confirmation.

### QA-FE-054-004 [P3] Scoped Prettier quality gate fails

- `npx prettier --check` failed for five changed files:
  `UserManagePage.tsx`, `TagManagePage.test.tsx`,
  `CompanyCertManagePage.tsx`, `UserSubscriptionCorrectionModal.tsx`, and
  `UserSubscriptionManagePage.test.tsx`.
- Required remediation: format the listed files and rerun the same scoped check.

No P0 finding was identified. One P1, one P2, and two P3 findings remain open.

## Verdict

**FAIL**

PASS requires no open P0-P3 finding. The pending correction retarget defect,
company-review convergence defect, documentation gap, and failed formatting
gate prevent closure.

## Verification Results

| Check | Result |
| --- | --- |
| Focused owner suite | PASS: 6 files, 78/78 tests, 5.71 s |
| Changed coverage suite | PASS: 1 file, 24/24 tests, 5.98 s |
| TypeScript | PASS: `npm run typecheck` |
| Scoped ESLint | PASS: exit 0, zero warnings allowed |
| Scoped Prettier | FAIL: 5 changed files require formatting |
| Diff whitespace | PASS: scoped `git diff --check` |

Focused owner command:

```text
npm test -- src/components/ui/ConfirmDialog.test.tsx src/pages/admin/UserManagePage.test.tsx src/pages/admin/TagManagePage.test.tsx src/pages/admin/TrackManagePage.test.tsx src/pages/admin/CompanyCertManagePage.test.tsx src/pages/admin/UserSubscriptionManagePage.test.tsx
```

Changed coverage command:

```text
npm test -- src/test/coverage/adminSubscriberGaps.coverage.test.tsx
```

The passing tests independently confirm shared `ConfirmDialog` busy forwarding,
normal pre-submit cancellation, exact trimmed typed confirmation, duplicate
execute prevention, and the tested close paths for User, Tag, Track, Company,
and subscription correction owners. Passing counts do not cover the two
retarget/convergence defects above.

## Residual Risk

- Per the stop instruction, no additional full frontend suite, production
  build, backend ADMIN/H2 test, coverage report, documentation validation, or
  live-browser check was run.
- No deployed environment, retained data, Provider, mail, export/download, or
  other external effect was used.
- Protected output paths and ignored secret/local environment values were not
  opened, inspected, hashed, modified, staged, or deleted.

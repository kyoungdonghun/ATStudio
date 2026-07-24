---
version: 1.0
last_updated: 2026-07-24
project: ATS
owner: qa-integ
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20260724-ATS-024-handoff.md
    reason: Approved Work Item scope and output contract
  - path: WI-20260724-ATS-014-evidence-pack.md
    reason: Original runtime defect and representative regression baseline
  - path: WI-20260724-ATS-023-evidence-pack.md
    reason: Corrective implementation evidence
---

# Evidence Pack: WI-20260724-ATS-024

## Summary

- **Verdict: PASS**
- Independently verified the minimal Company Certification authorization fix in
  source, tests, and the disposable acceptance runtime.
- `WI014-DEFECT-01` is corrected. WI-015 and WI-016 are no longer blocked by
  this defect.
- No product code was changed during this Work Item.

## Scope / DoD Check

- [x] Corrective diff is minimal and matches the BUSINESS-only policy.
- [x] Focused and related backend gates pass.
- [x] The corrected backend process runs commit `677c378`.
- [x] PERSONAL, ADMIN, and BUSINESS runtime behavior passes.
- [x] Representative WI-014 API, UI, media, and ADMIN payment-read regression
  passes.
- [x] Logs contain no restricted value or credential and show no Toss or mail
  call.
- [x] Backend, frontend, restricted bundle, and disposable database remain
  available for WI-015 and WI-016.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approval, traceability, and sustainable verification |
| 0 | `docs/standards/development-standards.md` | Java, testing, and evidence standards |
| 0 | `docs/standards/documentation-standards.md` | Deliverable structure and links |
| 0 | `docs/standards/glossary.md` | Canonical Work Item and domain terminology |
| 1 | `docs/policies/security-policy.md` | Secrets and BUSINESS-only certification policy |
| 1 | `docs/policies/access-control-policy.md` | Least privilege and separation of duties |
| 1 | `docs/policies/quality-gates.md` | Independent verification gates |
| 2 | `docs/design/api-spec.md` | API and media contracts |
| 2 | `docs/client/2-full-feature-checklist.md` | Representative client coverage |
| Context | `deliverables/user/REQ-20260724-ATS-002.md` | Approved release rehearsal |
| Context | `deliverables/agent/WI-20260724-ATS-014-evidence-pack.md` | Failed baseline and reusable harness |
| Context | `deliverables/agent/WI-20260724-ATS-023-evidence-pack.md` | Corrective diff and tests |

## Evidence Pointers

### Source and Test Evidence

```text
C:\Users\jm991\AppData\Local\ATStudio\
release-rehearsal-runtime-3147873-20260724\
wi024-20260724T072635Z-5bfa78f8
```

| Evidence | Purpose |
|---|---|
| `source-verification.json` | Commit, remote, clean clone, diff, and gate summary |
| `focused-backend-tests-executed.log` | Forced focused gate execution |
| `related-backend-tests-executed.log` | Forced 118-test related gate execution |
| `focused-backend-tests.log` | First wrapper diagnostic; not used as a test verdict |
| `focused-backend-tests-rerun.log` | Cached diagnostic; not used as execution evidence |

### Runtime Evidence

```text
C:\Users\jm991\AppData\Local\ATStudio\
release-rehearsal-runtime-3147873-20260724\
wi024-20260724T072637Z-2a22e58c
```

| Evidence | Purpose |
|---|---|
| `process-tree.json` | Corrected commit and process ancestry |
| `runtime-ownership-verification.json` | Live listener, clone, commit, and process cross-check |
| `final-runtime-handoff.json` | Final listener and preservation state for later Work Items |
| `backend.log` | Running acceptance backend log |
| `certification-role-and-static-boundary-matrix.json` | PERSONAL/ADMIN/BUSINESS and authenticated static denial |
| `api-matrix.json` | Raw 52-case WI-014 API/media rerun |
| `api-matrix-adjudication.json` | Five established expectation adjustments and final PASS |
| `admin-payment-read-safety.json` | Nine read-only ADMIN payment responses |
| `ui-smoke.json` | Eight routes, guards, playback, images, and console result |
| `ui-track-detail-playing.png` | Full Track playback visual evidence |
| `ui-company-certification-guard.png` | Unauthenticated Company Certification guard |
| `secret-and-external-call-scan.json` | Restricted values, credentials, provider, mail, and connection scan |
| `validate-docs.log` | Final documentation-integrity gate |

The restricted WI-013 bundle remains outside both evidence directories. No
bundle value, password, or bearer token is stored in these artifacts.

## Corrective Diff Review

Comparison:

```text
2a7484a976486440012295f7542da8a4500f7de1
  -> 677c3780f997f55b3e6f380e5e6c70113116b25c
```

Product-code scope:

- `src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java:157`
- Four added lines, zero removed lines.
- `getMyStatus` resolves the current user, rejects a non-BUSINESS member, and
  only then calls the certification repository.
- No schema, DTO, controller route, frontend, Provider, payment, or mail
  behavior changed.

Direct regression pointers:

- `src/test/java/com/atstudio/atstudio/service/CompanyCertificationServiceTest.java:508`
- `src/test/java/com/atstudio/atstudio/service/CompanyCertificationServiceTest.java:523`
- `src/test/java/com/atstudio/atstudio/service/CompanyCertificationSecurityVerificationTest.java:161`
- `src/test/java/com/atstudio/atstudio/controller/CompanyCertificationControllerTest.java:151`

The corrective commit also carries its approved WI handoff and deliverable
updates. Product behavior remains limited to one service guard plus three
directly related test files.

## Backend Gates

### Focused Gate

```powershell
.\gradlew.bat --no-daemon test --rerun-tasks `
  --tests "com.atstudio.atstudio.service.CompanyCertificationServiceTest" `
  --tests "com.atstudio.atstudio.service.CompanyCertificationSecurityVerificationTest" `
  --tests "com.atstudio.atstudio.controller.CompanyCertificationControllerTest"
```

Result:

- `BUILD SUCCESSFUL`
- Five actionable tasks executed.
- Gradle exit code `0`.

### Related Gate

The WI-023 ten-class certification, bootstrap, entity, billing, payment-fence,
and storage-reference slice was rerun with `--rerun-tasks`.

Result:

- 118 tests passed.
- 0 failures, 0 errors, 0 skipped.
- Main and test compilation executed successfully.
- Gradle exit code `0`.

Diagnostics:

1. The first PowerShell wrapper treated a JVM stderr warning as a terminating
   `NativeCommandError`; it produced no Gradle verdict.
2. A second non-forced command returned `UP-TO-DATE`; it was not accepted as
   independent execution evidence.
3. Final focused and related commands forced task execution and are the
   authoritative results.

## Runtime Ownership

| Process | Listener PID | Address |
|---|---:|---|
| Backend | `16792` | `127.0.0.1:8080` |
| Frontend | `27872` | `127.0.0.1:15173` |

Cross-checks:

- Clone HEAD: `677c3780f997f55b3e6f380e5e6c70113116b25c`
- Remote-tracking ref: same commit.
- Clone worktree: clean.
- `process-tree.json` commit: same commit.
- Backend command line resolves to the corrected clone without recording the
  command line itself.
- Acceptance profile and successful application-start markers are present.
- Both listeners remain loopback-only.

## Certification Runtime Matrix

| Actor | Runtime role/type | Result | Expected |
|---|---|---:|---:|
| PERSONAL fixture | `USER` / `INDIVIDUAL` | `403` | `403` |
| ADMIN | `ADMIN` / `INDIVIDUAL` | `403` | `403` |
| BUSINESS | `USER` / `BUSINESS` | `200` | `200` or `404` |

The same six-case matrix also proved authenticated `403` denial for Track
static audio, private Company Certification documents, and private Question
documents. Repository non-invocation is established by the reviewed ordering
and the focused Mockito regressions.

## WI-014 Regression

### API and Media

| Result | Count |
|---|---:|
| Raw cases | 52 |
| Raw passes | 47 |
| Established non-defect expectation adjustments | 5 |
| Effective passes | 52 |
| Defects | 0 |

The five adjustments are unchanged from WI-014:

1. Three unauthenticated static-resource requests return `401` through the
   authentication entry point; fresh authenticated probes return `403`.
2. The unsubscribed PERSONAL fixture correctly receives `403` for Subscription
   state and Playlist access; subscriber equivalents pass.

`individual certification policy`, the former sixth raw failure, now returns
the required `403`.

Media:

| Check | Result |
|---|---|
| Track | ID `1`, duration `36` seconds |
| Source, full Public Listening, Official Download bytes | `576,044` each |
| Full Public Listening SHA-256 equals source | PASS |
| Official Download SHA-256 equals source | PASS |
| Byte-range response | `206`, 1,000 bytes |
| Browser playback | Advanced to at least 5 seconds of 36 |

### UI

- Routes: 8/8 passed.
- Public: Albums, Subscriptions, Notices, and Login.
- Guards: Playlist, Whitelist Channel, Company Certification Status, and ADMIN
  Payment routes reached Login.
- Broken images: 0.
- Console errors: 0.
- Console warnings: 9 known React Router future-flag warnings.

The initial probe used undefined `/company-certification` and correctly reached
the 404 page. Source inspection identified the defined guarded routes as
`/company-certification/apply` and `/company-certification/status`; the final
representative check used `/company-certification/status`.

### ADMIN Payment Read Safety

Nine non-mutating ADMIN list endpoints passed `200`:

- Payment Orders
- Billing Agreements
- Subscription Payments
- Payment Receipts
- Payment Operation Audit Logs
- Payment Settlements
- Refunds
- Entitlement Corrections
- Reconciliation Incidents

Forbidden Provider identifier field matches: 0.

`GET /api/admin/payments/reconciliation` was not called because it may enter
Provider reconciliation behavior.

## Secret and External-Side-Effect Evidence

Final shared-read log and evidence scan:

| Check | Matches |
|---|---:|
| Exact restricted bundle values | 0 |
| Bearer JWTs | 0 |
| Raw Toss secret prefixes | 0 |
| Raw Toss client prefixes | 0 |
| Raw Billing Key values | 0 |
| Raw Auth Key values | 0 |
| Email delivery events | 0 |
| Toss Provider HTTP evidence | 0 |
| Backend established non-loopback connections | 0 |

No Toss mutation, mail delivery, Cloudflare launch, or Provider reconciliation
request was performed. The protected database was not connected to or queried
directly.

## Final Quality Gates

```powershell
python .agents/skills/validate-docs/scripts/validate_docs.py
git diff --check
```

Results:

- Tier 0 documents: present.
- Broken internal links: 0.
- Supported traceability ID matches: 471.
- Document index omissions: 0.
- `git diff --check`: passed.
- Both new untracked documents also passed an explicit no-index whitespace
  check.

## Risks / Rollback

### Residual Risks

- The disposable database has no Provider payment rows. Non-empty masked
  support-reference mapping remains covered by backend tests.
- Browser automation intentionally did not receive restricted credentials.
- React Router future-flag warnings remain a pre-production migration item.
- Log and connection checks prove the observed release-rehearsal window, not a
  general-purpose historical packet capture.

### Runtime Preservation

Do not roll back or clean up after this PASS. WI-015 and WI-016 depend on the
current processes, restricted bundle, and disposable database.

Later cleanup must:

1. Re-read the latest process tree and listener ownership.
2. Stop only the recorded owned process trees.
3. Use the WI-013 regex-guarded disposable database cleanup contract.
4. Remove the restricted bundle only after the later Work Items complete.

## Follow-ups

- Resume WI-015 and WI-016 from the corrected runtime.
- Preserve this runtime until their evidence is complete.
- WI-017 remains the cleanup owner.

## Related Documents

- [WI-024 Handoff](WI-20260724-ATS-024-handoff.md)
- [WI-014 Evidence Pack](WI-20260724-ATS-014-evidence-pack.md)
- [WI-023 Evidence Pack](WI-20260724-ATS-023-evidence-pack.md)
- [WI-024 User Summary](../user/WI-20260724-ATS-024-summary.md)

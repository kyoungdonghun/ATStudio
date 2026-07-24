---
version: 1.0
last_updated: 2026-07-24
project: ATS
owner: qa-integ
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20260724-ATS-014-handoff.md
    reason: Approved Work Item scope and output contract
  - path: WI-20260724-ATS-013-evidence-pack.md
    reason: Disposable MySQL runtime and cleanup contract
  - path: WI-20260724-ATS-020-evidence-pack.md
    reason: Independent clean-clone prerequisite
---

# Evidence Pack: WI-20260724-ATS-014

## Summary

- **Verdict: FAIL**
- Started an owned loopback-only acceptance runtime from the remote fresh clone,
  populated representative demo data, and verified API, media, UI, payment-read,
  and secret-handling behavior.
- `WI014-DEFECT-01` blocks WI-015 and WI-016: Company Certification status
  reads do not enforce the BUSINESS member type before repository access.

## Scope / DoD Check

- [x] Fresh clone HEAD equals
  `2a7484a976486440012295f7542da8a4500f7de1`.
- [x] Ports `8080` and `15173` were free before launch.
- [x] Backend started on loopback with `acceptance`, the WI-013 disposable DB,
  and `ddl-auto=validate`.
- [x] Frontend started on loopback with Vite port override `15173`.
- [x] Cloudflare was not started.
- [x] The supported demo seed CLI created 36 Tracks, 36 Tags, and 9 Playlists.
- [x] Public Listening and Official Download file boundaries passed.
- [x] ADMIN payment read responses contained no forbidden Provider identifier
  fields.
- [x] Final logs and evidence contain no restricted bundle values, bearer
  tokens, raw Toss keys, Billing Keys, Auth Keys, or email-delivery events.
- [x] Toss Provider calls and email deliveries remained at zero.
- [ ] All role authorization expectations pass.
  `WI014-DEFECT-01` remains open.
- [ ] Positive authenticated role screens are browser-verified.
  Restricted credentials were intentionally not copied into browser automation;
  positive roles were verified by API instead.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approval, sustainability, and traceability |
| 0 | `docs/standards/development-standards.md` | Runtime and QA standards |
| 0 | `docs/standards/documentation-standards.md` | Evidence structure |
| 0 | `docs/standards/glossary.md` | Canonical domain terms |
| 1 | `docs/policies/security-policy.md` | Secrets, private files, and BUSINESS-only certification policy |
| 1 | `docs/policies/quality-gates.md` | Verification gates |
| 2 | `docs/design/api-spec.md` | API contract |
| 2 | `docs/ui/atstudio-front-list.md` | SPA route inventory |
| 2 | `docs/client/2-full-feature-checklist.md` | Client feature coverage |
| 2 | `docs/client/3-admin-checklist.md` | ADMIN coverage |
| Context | `deliverables/user/REQ-20260724-ATS-002.md` | Approved release rehearsal |
| Context | `deliverables/agent/WI-20260724-ATS-013-evidence-pack.md` | Disposable runtime pointer |

## Evidence Pointers

### Repository Deliverables

- `deliverables/user/WI-20260724-ATS-014-summary.md`
- `deliverables/agent/WI-20260724-ATS-014-evidence-pack.md`

### Repo-External Runtime

Runtime directory:

```text
C:\Users\jm991\AppData\Local\ATStudio\
release-rehearsal-runtime-3147873-20260724\
wi014-20260724T064049Z-b2d3df60
```

| Evidence | Purpose |
|---|---|
| `handoff-manifest.json` | Health, ownership, boundaries, and later-WI handoff |
| `process-ownership.json` | Launch attempts and root-process ownership |
| `process-tree.json` | Exact owned process trees and listeners |
| `demo-seed.log` | Supported demo fixture creation and verification |
| `demo-seed/manifest.json` | Fixture and storage ownership |
| `api-matrix.json` | Raw 52-case role/API result |
| `api-matrix-adjudication.json` | Five expectation corrections and one defect |
| `static-resource-auth-boundary.json` | Authenticated static-path denial proof |
| `admin-payment-read-safety.json` | Nine non-mutating ADMIN payment reads |
| `ui-smoke.json` | Public routes, protected redirects, playback, and console results |
| `ui-tracks.png` | Track catalog visual evidence |
| `ui-track-detail-playing.png` | Player progress and full-duration visual evidence |
| `ui-subscriptions.png` | Subscription plan visual evidence |
| `ui-login.png` | Acceptance login capability visual evidence |
| `secret-scan.json` | Exact-value and credential-pattern scan |
| `backend-final.stdout.log` | Secret-safe final backend runtime log |
| `backend-final.stderr.log` | Final backend error stream |

The restricted bundle remains outside this directory. Its values are not copied
into any evidence file.

## Runtime Ownership

| Process | Root PID | Listener PID | Address |
|---|---:|---:|---|
| Backend | `24992` | `22744` | `127.0.0.1:8080` |
| Frontend | `9004` | `27872` | `127.0.0.1:15173` |

The final browser handoff URL is
`http://127.0.0.1:15173/tracks/1`.

WI-017 must re-read `process-tree.json`, prove the listener belongs to the
recorded tree, and stop only that owned tree.

## API Matrix

### Raw and Adjudicated Results

| Result | Count |
|---|---:|
| Raw cases | 52 |
| Raw passes | 46 |
| Non-defect expectation adjustments | 5 |
| Effective passes | 51 |
| Defects | 1 |

The five non-defect adjustments are:

1. Three unauthenticated static-resource requests returned `401`; the
   authentication entry point correctly precedes `denyAll`. Authenticated
   probes returned `403` for Track audio, Company Certification files, and
   Question files.
2. The unsubscribed QA member correctly received `403` for Subscription state
   and Playlist access. Subscriber equivalents returned `200`.

### Blocking Defect

`WI014-DEFECT-01`

- Request: `GET /api/company-certifications/me`
- Actor: authenticated `INDIVIDUAL` member
- Required result: `403`
- Actual result: `404`
- Code:
  `src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java:157`
- Cause: `getMyStatus` loads the member and repository state without the
  BUSINESS type check used by `apply` and `resubmit`.
- Risk: a member changed from BUSINESS to INDIVIDUAL could retain status-read
  access to an earlier certification record.
- Required correction: enforce BUSINESS before certification repository access
  and add service/controller regression tests.

## Media and Download Evidence

| Check | Result |
|---|---|
| Track ID | `1` |
| API duration | 36 seconds |
| Generated duration | 36 seconds |
| Source bytes | 576,044 |
| Full public stream bytes | 576,044 |
| Subscriber download bytes | 576,044 |
| Byte-range response | `206`, 1,000 bytes |
| Full stream SHA-256 equals source | PASS |
| Official Download SHA-256 equals source | PASS |
| Public Official Download | `401` |
| Unsubscribed member Official Download | `403` |
| Subscriber Official Download | `200` |

Browser playback advanced to `15.501049` seconds and exposed a 36-second slider
maximum. This proves Public Listening uses the complete Track rather than a
preview segment.

## UI Evidence

- Routes: 8/8 passed.
- Public: Albums, Subscriptions, Notices, and Login.
- Guards: Playlist, Whitelist Channel, Company Certification, and ADMIN Payment
  routes redirected unauthenticated access to Login.
- Broken images: 0.
- Console errors: 0.
- Console warnings: 10 repeated React Router v7 future-flag warnings.
- Post-restart Track detail heading: present.

Positive authenticated UI was not automated because doing so would require
copying a restricted bundle credential into the browser control channel.
Positive ADMIN, member, subscriber, grace-period, and business behavior was
covered through bearer-token API calls held only in process memory.

## ADMIN Payment Read Safety

The following read-only endpoints passed `200` and produced zero forbidden
identifier-field matches:

- Payment Orders
- Billing Agreements
- Subscription Payments
- Payment Receipts
- Payment Operation Audit Logs
- Payment Settlements
- Refunds
- Entitlement Corrections
- Reconciliation Incidents

`GET /api/admin/payments/reconciliation` was intentionally not called because
it can enter Provider reconciliation logic. The fresh fixture has no payment
rows, so non-empty support-reference mapping remains proven by automated tests,
not this runtime.

## Secret and External-Side-Effect Evidence

Final `secret-scan.json`:

| Check | Matches |
|---|---:|
| Exact restricted bundle values | 0 |
| Bearer JWTs | 0 |
| Raw Toss secret/client prefixes | 0 |
| Raw Billing Key values | 0 |
| Raw Auth Key values | 0 |
| Email delivery events | 0 |
| Toss Provider HTTP evidence | 0 |

Hibernate initially logged the full JDBC URL and disposable database name at
INFO. The owned backend was stopped, affected logs were sanitized in place, and
the backend was restarted with
`org.hibernate.orm.connections.pooling=WARN`. The final runtime log contains no
JDBC URL or disposable database name.

## Commands and Reproduction

Commands below are sanitized contracts. Bundle values were injected only in
process and never printed.

```powershell
# Preflight
git -C <fresh-clone> rev-parse HEAD
Get-NetTCPConnection -State Listen -LocalPort 8080,15173

# Backend
$env:SPRING_PROFILES_ACTIVE = 'acceptance'
$env:SERVER_PORT = '8080'
$env:SPRING_JPA_HIBERNATE_DDL_AUTO = 'validate'
$env:LOGGING_LEVEL_ORG_HIBERNATE_ORM_CONNECTIONS_POOLING = 'warn'
.\gradlew.bat --no-daemon bootRun

# Frontend
npm.cmd run dev -- --host 127.0.0.1 --port 15173 --strictPort

# Demo fixture
.\scripts\demo\seed-client-demo.ps1 `
  -Mode Seed `
  -ApiBase 'http://127.0.0.1:8080' `
  -RuntimeCredentialsPath '<restricted-WI-013-bundle>' `
  -WorkDirectory '<repo-external-WI-014-directory>\demo-seed'
```

## Diagnostics

1. The first backend launch was refused because the WI-013 bundle does not
   contain `APP_PUBLIC_BASE_URL`. No Cloudflare or external callback was used.
2. A non-secret HTTPS loopback origin was supplied through
   `SPRING_APPLICATION_JSON` for callback-consistency validation.
3. The raw API harness initially needed PowerShell 5.1 accommodations for
   `-OutFile -PassThru` and byte-range requests. The final 52-case evidence is
   complete.
4. The Hibernate JDBC INFO disclosure was remediated at runtime as described
   above. No repository code was changed.

## Risks / Rollback

### Risks

- WI-015 and WI-016 must not start until `WI014-DEFECT-01` is corrected and
  WI-014 authorization checks are rerun.
- The React Router advisory migration remains required before production.
- The running processes and disposable database are temporary release-rehearsal
  assets, not production services.

### Rollback and Cleanup

1. Read `handoff-manifest.json` and `process-tree.json`.
2. Verify ports `8080` and `15173` are still owned by the recorded descendants.
3. Stop only those owned process trees.
4. Use the WI-013 cleanup contract to drop only the regex-guarded disposable DB.
5. Remove the restricted bundle and repo-external runtime only after DB absence
   and protected-database metadata checks pass.

## Follow-ups

- Create a corrective WI for `WI014-DEFECT-01`.
- Rerun the certification authorization slice and final secret scan.
- Resume WI-015 and WI-016 only after WI-014 passes.

## Related Documents

- [WI-014 Handoff](WI-20260724-ATS-014-handoff.md)
- [WI-013 Evidence Pack](WI-20260724-ATS-013-evidence-pack.md)
- [WI-020 Evidence Pack](WI-20260724-ATS-020-evidence-pack.md)
- [WI-014 User Summary](../user/WI-20260724-ATS-014-summary.md)

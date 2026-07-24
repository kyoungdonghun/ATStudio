---
version: 1.0
last_updated: 2026-07-24
project: ATS
owner: qa-integ
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20260724-ATS-014-handoff.md
    reason: Approved Work Item scope
  - path: ../agent/WI-20260724-ATS-013-evidence-pack.md
    reason: Disposable MySQL runtime and restricted environment source
---

# WI-20260724-ATS-014 Runtime Verification Summary

> Purpose: Report the isolated acceptance runtime result before any external payment or mail verification.

## Verdict

**FAIL.** The runtime, media, API, and public UI checks are healthy, but one
Company Certification authorization defect blocks WI-015 and WI-016.

## Verified Results

- Fresh clone commit: `2a7484a976486440012295f7542da8a4500f7de1`
- Backend: `127.0.0.1:8080`, acceptance profile, disposable MySQL,
  `ddl-auto=validate`
- Frontend: `127.0.0.1:15173`, Vite CLI port override
- Demo fixtures: 36 Tracks, 36 Tags, and 9 Playlists
- API matrix: 52 cases, 51 effective passes, 1 defect
- Additional ADMIN payment reads: 9/9 passes with zero forbidden identifier
  fields
- UI routes: 8/8 passes with zero console errors
- Public Listening: complete 36-second WAV and byte-range delivery verified
- Official Download: public `401`, unsubscribed member `403`, subscriber `200`;
  downloaded bytes match the source SHA-256
- Restricted-value and credential scan: PASS
- Toss Provider calls: 0
- Email deliveries: 0
- Cloudflare: not started

## Blocking Defect

`WI014-DEFECT-01`: `GET /api/company-certifications/me` does not enforce
`UserType.BUSINESS` before reading certification state.

The individual QA member received `404` instead of the required `403`. The
empty fixture disclosed no certification data, but an account changed from
BUSINESS to INDIVIDUAL could retain read access to a prior certification
record.

Code pointer:
`src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java:157`

## Runtime Handoff

The owned backend and frontend processes remain running for verification:

- UI: `http://127.0.0.1:15173/tracks/1`
- Backend root PID: `24992`; listener PID: `22744`
- Frontend root PID: `9004`; listener PID: `27872`
- Repo-external evidence:
  `C:\Users\jm991\AppData\Local\ATStudio\release-rehearsal-runtime-3147873-20260724\wi014-20260724T064049Z-b2d3df60`

Stop only the process trees recorded in `process-tree.json`. The disposable
database and restricted bundle remain owned by WI-017 cleanup.

## Limitations

- Positive authenticated browser screens were not opened because restricted
  bundle values were not copied into browser automation. Positive role
  behavior was verified through the API matrix; public and unauthenticated
  route guards were verified in the browser.
- The fresh database contains no Provider payment rows. ADMIN payment response
  contracts were scanned, but non-empty masked support-reference mapping
  remains covered by automated tests rather than this runtime fixture.
- Ten React Router v7 future-flag warnings were observed. They are the known
  pre-production migration item; no browser console errors occurred.

## Related Documents

- [WI-014 Handoff](../agent/WI-20260724-ATS-014-handoff.md)
- [WI-013 Evidence Pack](../agent/WI-20260724-ATS-013-evidence-pack.md)
- [WI-014 Evidence Pack](../agent/WI-20260724-ATS-014-evidence-pack.md)

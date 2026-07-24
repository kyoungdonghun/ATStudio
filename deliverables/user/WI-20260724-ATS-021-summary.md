---
version: 1.0
last_updated: 2026-07-24
project: ATS
owner: PG
category: wi-summary
status: stable
related_wi: WI-20260724-ATS-021
related_req: REQ-20260724-ATS-002
---

# WI-20260724-ATS-021 Summary

## Verdict

**CONDITIONAL PASS for the current non-production rehearsal; remediation is
required before production.**

The locked frontend installs `react-router-dom@6.30.4` and
`react-router@6.30.4`. The current npm advisory database reports two moderate
vulnerable packages containing three advisory records:

1. The open-redirect/XSS family:
   - `GHSA-wrjc-x8rr-h8h6` / `CVE-2026-53669`
   - `GHSA-jjmj-jmhj-qwj2` / `CVE-2026-53668`
2. The SSR hydration constructor-injection advisory:
   - `GHSA-337j-9hxr-rhxg` / `CVE-2026-53666`

## ATStudio Risk Assessment

| Advisory family | Installed code affected | Current ATStudio reachability | Disposition |
|---|---|---|---|
| Open redirect / XSS | Yes | No exploitable call site was found. Whole-string query/session return targets are validated, and all other dynamic targets retain a fixed same-origin path prefix or use `URLSearchParams`. | Low current application exploitability, but moderate residual dependency risk remains. |
| SSR hydration constructor injection | Yes | Not reachable. ATStudio uses client-only `createRoot`, `createBrowserRouter`, and `RouterProvider`; it does not perform SSR or manual hydration. | Not applicable to the current runtime architecture. Reassess if SSR/hydration is introduced. |

The review covered all production `Link`, `NavLink`, `navigate`,
`router.navigate`, `Navigate`, `redirect`, and SSR/hydration references under
`frontend/src`.

## Recommendation

Do **not** run `npm audit fix --force`. The complete upstream remediation
requires a React Router 7 major upgrade.

Recommended path:

1. Keep the current application-level internal-route validation in place.
2. Add advisory-specific regression tests in a separately approved corrective
   WI, especially raw, mixed, and percent-encoded backslash targets.
3. Approve a controlled upgrade to `react-router-dom@7.18.1` and its matching
   `react-router` dependency before production, with router migration review
   and the full frontend gate suite.

The current V1 rehearsal may continue only as a non-production exercise if the
moderate residual dependency risk is explicitly accepted. This review does not
justify suppressing or ignoring `npm audit`.

## Approval Point

Approval is required for one of these dispositions:

- **Recommended:** authorize a separate React Router 7.18.1 migration WI before
  production.
- **Temporary exception:** explicitly accept the residual moderate dependency
  risk for the non-production rehearsal, require the targeted mitigation tests,
  and keep the production gate blocked until the major upgrade is complete.

WI-20260724-ATS-020 should record the selected disposition.

## Scope Integrity

- Product code changed: **No**
- Dependencies or lockfiles changed: **No**
- `npm audit fix` run: **No**
- Required deliverables added: **This summary and the WI-021 evidence pack only**

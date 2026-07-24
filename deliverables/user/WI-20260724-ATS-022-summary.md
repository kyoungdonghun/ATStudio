---
version: 1.0
last_updated: 2026-07-24
project: ATS
owner: DocOps
category: work-summary
status: stable
related_wi: WI-20260724-ATS-022
related_req: REQ-20260724-ATS-002
---

# WI-20260724-ATS-022 Summary

## Verdict

**PASS**

The three authorized current-state documents no longer claim that the current
frontend dependency audit has zero findings.

## Corrected Current State

- The locked frontend resolves `react-router-dom@6.30.4` with
  `react-router@6.30.4`.
- The current npm advisory database reports two moderate vulnerable package
  records containing three advisory records.
- WI-20260724-ATS-021 found no exploitable open-redirect/XSS call site in the
  current ATStudio route graph.
- WI-20260724-ATS-021 found the SSR hydration constructor-injection advisory
  unreachable in the current client-only architecture.
- Those call-path conclusions do not remove the vulnerable package status.
- Public acceptance remains conditional and limited to non-production use.

## Remaining Production Decision

Production readiness remains open. A controlled migration to
`react-router-dom@7.18.1` and its matching `react-router` dependency requires a
separately approved WI and the full frontend gate suite. `npm audit fix --force`
was not run.

## Changed Current-State Documents

- `docs/SR/SR-42.md:7`
- `docs/SR/SR-93.md:33`
- `docs/design/remaining-remediation-design-20260716.md:48`

Historical WI and Evidence Pack text was preserved.

## Verification

- Document validation: PASS
- Stale current-state zero-advisory claim search: PASS, zero matches
- `git diff --check`: PASS

Detailed commands and source evidence are recorded in
`deliverables/agent/WI-20260724-ATS-022-evidence-pack.md`.

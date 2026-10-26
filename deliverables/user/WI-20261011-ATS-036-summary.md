---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: QA-INTEG
category: work-summary
status: confirmed
dependencies:
  - path: REQ-20261011-ATS-006.md
    reason: Approved read-only web trial scope
  - path: ../agent/WI-20261011-ATS-036-evidence-pack.md
    reason: Independent local evidence
---

# WI036 Integration Summary

> Purpose: Identify the exact local release and smallest anonymous trial surface.

**TL;DR: local identity PASS; browser/server acceptance NOT RUN.** Backend641 inputs and JAR, frontend336 inputs and143 output files, plus four separately excluded historical coverage tests match the pinned manifests. Branch main, HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`; existing dirty worktree preserved.

## Result

- JAR: 70,306,351 bytes; SHA-256 `01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb`.
- SPA: 143 files / 1,194,351 bytes; set SHA-256 `43a0384c5f48d100e4af6b20d989a2186b34e6900cf4e69c94b010e9671c8647`.
- **Six exact GET paths, 17 query sets:** tracks, albums, tags, **tags/available without query**, subscriptions, public-capabilities. Home requires available-tags; no broad tags wildcard is justified.
- Six SPA routes: `/`, `/tracks`, `/albums`, `/albums/list`, `/subscriptions`, `/login`; exact search/sort query variants are in the machine-readable contract.
- Browser plan covers real-origin HTTPS, empty lists/search, reload/navigation, six expected seeded plan rows, disabled login display and reviewed edge denials. No fixture creation or successful playback/download/login claim.

## Findings and Gates

| Severity / gate | Result |
|---|---|
| P2 existing semantic mismatch | Home requests popular albums; AlbumService falls back to newest-first. Empty-data trial cannot certify popular ordering. No source fix attempted. |
| P3 documentation discrepancy | Injected development standard calls tags a raw array; current spec, backend and frontend agree on a dataList envelope. Use the actual envelope; later documentation reconciliation required. |
| Context limitation | MA reports no supported fresh/incognito context API; a new tab shares the existing browser. Pristine-profile acceptance is HOLD. A reviewed existing-context alternative requires edge credential rejection and no hidden-storage inspection/clearing; stop on signed-in UI or auth/member/batch/media attempts. |
| Before navigation | Pinned HTML loads an external jsDelivr font and preconnect. WI035/WI037 must prevent browser external traffic before parsing or HOLD. Server egress rules alone do not cover it. |

Verification owner QA-INTEG; local Windows/PowerShell on2026-10-11, initial hash result14:30:22 KST and archive-resource check14:32:58 KST. Final rerun time is in the receipt. These are local observations, not the document date or MA's earlier native evidence.

No build/install, app/browser/DB/network/service operation, private environment read, source edit, deletion, commit/push or other-chat action occurred. Only approved WI036 outputs were written with apply_patch. **WI036 now feeds WI037 together with WI035; operational review and actual trial remain pending.**

## Related Documents

- [Evidence Pack](../agent/WI-20261011-ATS-036-evidence-pack.md): exact pins, paired FE/BE evidence and limitations.
- [Allowlist](../../build/WI036-web-contract/allowlist.json): exact raw API queries and SPA paths.
- [Browser plan](../../build/WI036-web-contract/browser-plan.md): unexecuted acceptance/denial matrix.
- [Identity receipt](../../build/WI036-web-contract/identity-result.json): current local check results.

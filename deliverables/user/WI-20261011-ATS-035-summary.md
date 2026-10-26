---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: work-summary
status: confirmed
dependencies:
  - path: REQ-20261011-ATS-006.md
    reason: Approved restricted web check
  - path: ../agent/WI-20261011-ATS-035-evidence-pack.md
    reason: Frozen candidate, local tests and review boundaries
---

# WI035 Restricted Web Check Summary

> Purpose: Report the completed local candidate and mandatory next review.

**TL;DR: Local preparation complete. Python85/85 and PowerShell8/8 PASS.
Native NOT_RUN; PG037 final exact-byte review is still required.**

## Completed

- Separate WI035 candidate with existing startup containment, fresh one-use identity,
  management-only exact GET/SPA paths and reviewed143-file static manifest.
- Independently bounded app/DB lifetime and systemd-owned restoration, explicit
  start/status/finish handoff, original23 plus13 web diagnostic outcomes.
- Exact upload, staging, preflight, approval, start, status, HTTP, finish and postcheck
  recipe. All eleven executable blocks and23 review files have SHA256 pins.
- Management-PC checker verifies seventeen allowed API cases and seventeen synthetic
  denial cases only after the pinned fence check. Browser UI evidence remains separate.
- Original sources/JAR/candidates preserved; seventeen named pinned objects MATCH.
  No remote, secret, service, DB, actual HTTP/browser or package operations performed.

## Frozen Identity

- Runtime bundle: `35ff347e1d0e2b0cad930ce1431b073d08fea7d2a48631779360766424760ce5`
- README: `91801af1f56c4f67d5be1c11e28ec8b04f1bb91a0db5d4d1d20015fdc6ccd74a`
- Review set: `feb373fe3308a8dd06da2bb7e149bbdc255f527750744a987830fccf8b1de063`
- All individual/block pins: [freeze](../../build/WI035-restricted-web-check/freeze.json)
  and [recipe pins](../../build/WI035-restricted-web-check/recipe-pins.json).

## Limits and Next Step

MA must obtain PG037 final review before executing the frozen recipe through its
current pinned SSH transport. Runtime checks match the live client peer to the
existing nginx management allow; no public server-address assumption is introduced.

Local tests do not establish native timing, BPF, HTTPS/UI or restoration success.
Existing browser context is not a fresh profile. DOM/screenshots/dev.logs cannot
prove all request statuses or absence of extra traffic; the separate bounded PC
checker supplies API evidence. Expected external-font CSS blocking is not app failure.
No independent non-management proof, populated catalog, playback, login, payment,
production release or historical root-cause conclusion is claimed.

Cleanup failure or missing result remains HOLD. Withhold the candidate to roll back
before execution; retain old/new evidence and use only the reviewed finish procedure
during an approved attempt. No automatic retry or additional start.

**WI035 complete locally; hand the frozen bytes to the already-started WI037 review now.
REQ006 remains active. MA alone owns native execution and temporary access cleanup.**

## Related Documents

- [Exact recipe](../../build/WI035-restricted-web-check/README.md): review-gated executable instructions.
- [Evidence Pack](../agent/WI-20261011-ATS-035-evidence-pack.md): tests, preservation and limitations.
- [Local receipt](../../build/WI035-restricted-web-check/local-receipt.json): reproducible target-free results.
- [WI036 browser plan](../../build/WI036-web-contract/browser-plan.md): accepted UI scope and observability limits.

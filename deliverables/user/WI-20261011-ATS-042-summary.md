---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20261011-ATS-042-handoff.md
    reason: Approved local-only repair
  - path: ../agent/WI-20261011-ATS-042-evidence-pack.md
    reason: Exact test, freeze and preservation evidence
---

# WI042 Ordered Service Command Repair

> Purpose: Report the local parser repair and the remaining independent review gate.

**TL;DR:** LOCAL READY ONLY. Python106/106 and PowerShell8/8 PASS. Native execution is NOT AUTHORIZED and was NOT_RUN.

## Delivered

Repeated Exec command records now remain ordered JSON lists. Nginx's two reload commands are retained, including their order and content. Required scalar/JSON duplicates still reject, optional omissions remain bounded, and loaded MySQL still requires exactly one approved command.

The prepared S2 parses existing Nginx and masked MySQL through the same read-only production methods used by prepare, before any new approval/claim. It does not call prepare or write state. Full prepare/restore mock tests verify JSON roundtrip, changed command/order rejection and continued fail-closed cleanup.

Existing WI035/WI039 consumed claims and failed supervisors are never reset, removed or reused. All79 preservation hashes match. App source, frontend, JAR, security boundaries and the known intermediate-status limitation are unchanged.

## Frozen Delivery

- [Recipe](../../build/WI042-restricted-web-check/README.md): LOCAL READY ONLY; remote blocks require new user execution authority.
- [Freeze](../../build/WI042-restricted-web-check/freeze.json): frozen2026-10-11 16:35:35KST;27 review files.
- [Command hashes](../../build/WI042-restricted-web-check/recipe-pins.json):11 executable blocks.
- [Evidence Pack](../agent/WI-20261011-ATS-042-evidence-pack.md):106 Python tests,8 PowerShell assertions,17 compilation units and79 unchanged identities.

Bundle: `78ce821757111b6f4bd440a79680ed12e4e40b6592616293e4d768ddd6dbe00c`.

Freeze: `b92cbe9cf2b24da99953b9f6c496028205a1a5daab754a915dce30690537f6bc`.

## Limits and Next Step

WI043 exact-byte independent review is next. No native success is claimed; fixtures derive command strings from the repository and reported native record counts, not a raw target capture. The WI039 HOLD and earlier failures remain historical evidence.

Rollback means withholding the candidate. No SSH, upload, service/DB operation, retry or public exposure is authorized by this local completion. MA must obtain new explicit execution authority before any future remote block.

## Related Documents

- [WI042 handoff](../agent/WI-20261011-ATS-042-handoff.md): scope and authority.
- [REQ006](REQ-20261011-ATS-006.md): requirement history.

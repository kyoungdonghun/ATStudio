---
version: 1.1
last_updated: 2026-10-25
project: ATS
owner: DOCOPS
category: evidence-pack
status: completed
---
# Evidence Pack: WI-20261025-ATS-003

## Summary
Recorded approved account/grant application, independent reconciliation/cold
checks and final access restoration, preserving the old candidate's WI027 HOLD
and stage-one/pause history. Provider/operating work remains open.

## Scope / DoD Check
- [x] Current entry/REQ reflect the user's resumed approval without broadening scope.
- [x] Old candidate remains held until a new pinned candidate passes independent review.
- [x] Actual new review/native/restoration evidence integrated from MA receipts.

## Reference Documents (Tier 0-2)
MA-injected principles: approved in-scope recovery is automatic, evidence must
separate prepared/applied states, English docs/Korean REQ, preserve history and
private bodies. Tier0/1 pointers supplied by [handoff](WI-20261025-ATS-003-handoff.md):
core-principles, development-standards, documentation-standards, glossary,
security-policy and AGENTS. No independent full-body reread claimed. Actually
read current index, approved REQ and handoff; prior turn read and applied
create-wi-evidence-pack skill. Handoff exists before edits. DOCOPS makes no
private-reference/server/provider accesses.

## Evidence Pointers
- [WI002 review](WI-20261025-ATS-002-evidence-pack.md) allows exact candidate
  `480834a7fadc69d542dd9113deb0af877d67a570170aa1819c682feec81efe2c`.
- Native MA-owned evidence under `build/WI20261025-stage-two-native/`:
  [inspect](../../build/WI20261025-stage-two-native/inspect.json) INSPECTION_RECORDED,
  [apply](../../build/WI20261025-stage-two-native/apply.json) APPLIED/COMMIT ACKNOWLEDGED,
  [reconcile](../../build/WI20261025-stage-two-native/reconcile.json) APPLIED_CONFIRMED;
  five cleanup checks PASS in each phase.
- [probe-before](../../build/WI20261025-stage-two-native/probe-before.json) and
  [probe-after](../../build/WI20261025-stage-two-native/probe-after.json): independent
  cold checks PASS, not new backup/repository/cost/DR verification.
- [access-restoration](../../build/WI20261025-stage-two-native/access-restoration.json)
  at 20:17:41 KST and [SSH cleanup](../../build/WI20261025-stage-two-native/ssh-cleanup.json):
  original TCP22 restored, TCP80/443/outbound preserved, provider logout,
  task key removed and empty agent before Stopped/Disabled.
  SSH cleanup observed at 20:17:09 KST. MA reports all agent-created browser tabs
  closed; provider-logout.png retains the logout observation.
- MA reports the exact whole-43-table delta: ID1 role/updated_at, grant1
  expires_at/updated_at plus two truthful offline audit inserts only; ID2/3,
  start date, daily-five nonbilling plan, six plans and other cells preserved.
  DOCOPS read public receipts only, not private snapshots or secret bodies.
  No app/login/new backup/payment/paid cloud operation. Initial direct-fragment
  firewall attempt did not persist; normal-console recovery needed no additional
  user UI assistance. Native and browser observations remain MA-owned.
- [Current entry](../../docs/index.md#current-decision-entry): concise new checkpoint.
- [REQ](../user/REQ-20261024-ATS-001.md): resume status and WI chain.
- [Summary](../user/WI-20261025-ATS-003-summary.md): result boundaries.
- [WI027 review](WI-20261024-ATS-027-evidence-pack.md): retained defects/HOLD.
- [Prior pause receipt](../../build/WI20261024-stage-two-native/pause-cleanup-result.json): historical cleanup only.

## Commands / Tests
`git diff --check -- docs/index.md deliverables/user/REQ-20261024-ATS-001.md`
passed (CRLF normalization warning only). Python local-target checks passed for
all links in the three owned deliverables and new index checkpoint. No code test
or remote trial is required for this documentation checkpoint.
Final integration repeated these focused checks: 47 local links PASS and four
owned-path diff whitespace checks PASS (CRLF normalization warning only).

## Risks / Rollback
Do not interpret resumed approval or local tests as a native success. Revert only
the newly added checkpoint/own deliverables if necessary; preserve unrelated
worktree and historical evidence. MA owns actual server/access results.

## Follow-ups
WI003 documentation is complete; Blocks: none. REQ remains open. NCP account
absent and Toss contract not applied require account/pricing/contract preparation;
ID1 mailbox is selected. Reuse stored references and request only missing input.
Operating configuration/deployment/scheduler work and provider activation remain
incomplete. New costs and real payments require prior confirmation; no fresh
full backup or operational PG readiness is asserted.

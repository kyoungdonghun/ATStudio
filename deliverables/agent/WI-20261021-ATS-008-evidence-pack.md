---
version: 1.0
last_updated: 2026-10-21
project: ATS
owner: SE
category: reference
status: active
---
# Evidence Pack: WI-20261021-ATS-008

## Summary
Minimal local cold-preservation derivative prepared; exact failed-state vector
is still required from MA before final tests, freeze and PG review.

## Scope / DoD Check
- Implemented current metadata 7d1351 shapes and receipt pins.
- Implemented absent app mask, supervisor retirement and v7 tools moved last.
- Preserved original operator; no remote or secret access.
- Pending exact attempt/result semantic vectors, final semantic regression and freeze.

## Reference Documents (Tier 0-2)
Injected Tier0 core-principles, development-standards, documentation-standards,
glossary and security-policy retained from SE handoff. Current scope:
[WI008 handoff](WI-20261021-ATS-008-handoff.md), REQ-20261011-ATS-007.
Evidence-pack skill applied. Prior operator: build/WI067-profile-retry-20261021.

## Evidence Pointers
- build/WI067-dns-preserve-20261021/supersede.py: existing primitives reused;
  fixed cold gate, private shape validation, callback retirement, tools last.
- prepare.py: initial mechanical derivative provenance, not a rerun entrypoint.
- invoke.ps1: original pinned delivery/ACL/SSH transport with new receipt prefix.
- test_cold.py, test_metadata.py, test_pc.ps1: bounded synthetic checks.
- freeze.py explicitly refuses pending semantic placeholder.

## Commands and Tests
- Python selected ColdTests excluding pending semantic test: 11 PASS.
- Python test_metadata.py: 2 PASS.
- pwsh test_pc.ps1: 5 PASS, no native or secret reads.
- One preliminary -k orchestration invocation selected zero tests; not counted.
- No final freeze, PG approval or native success claimed.

## Risks / Rollback
Exact current result steps cannot be inferred from byte length. Pending MA data:
attempt keys and fixed enum values; result keys and ordered steps step/status.
Operator intentionally holds until these are bound. Final failed result remains
verbatim. No mutation has run; partial native execution would require phase-based
inspection, not automatic retry or restoring live callbacks blindly.

## Follow-up
Bind MA semantic vector, finish regression, freeze, return exact hash to WI007 PG.
Only MA may execute after independent review. Native proof d59e49 is MA-reported,
not rerun by SE.

## Completion Addendum: Native Metadata 47b9f2
MA supplied exact public attempt/result vectors. They are now bound in
failed_state: PREFLIGHT/HOLD/NOT_RUN, attempt cleanup NOT_RUN; result cleanup
FAILED with the exact 13 ordered steps, including eight FAILED outcomes.
No historical failed result was relabeled. Grant must remain absent.

- Python all local tests: 14 PASS (0.118s), including per-step status flips,
  order/extra-key rejection, expiry, shape, cold gates and retirement ordering.
- PowerShell transport tests: 5 PASS.
- Freeze independently rehashed: all 8 members EXACT.
- Freeze: build/WI067-dns-preserve-20261021/freeze.json
- SHA256: f5e03832a77ceb118f931b012421b46f37bbfb290237f38a47c04801855efbb9
- Candidate is ready for independent WI007 PG review; MA execution remains pending.
- Native execution, installation, app/DB start, mail and secrets access: NOT_RUN.

## MA Native Preservation Addendum (2026-10-21)

Evidence owner: MA; SE appended the reported observations without remote access.
The earlier NOT_RUN entry describes SE's local candidate checkpoint, not this
later MA execution.

- 81add4: COLD_PRESERVED; receipt
  wi067-dns-preserve-d043cfe0e7184fe8bc423acd7baecc8f.
- ddfcdd: independent preservation verification, JAR pin and cold-state checks PASS,
  as reported by MA. The preserved failed result remains historical and unchanged.
- Later v8r1 transition evidence is recorded in the WI006 native continuation
  addendum: Public/Release/producer staged, live preflight PASS, then OPEN.

Cold-state success applies to the preservation checkpoint only. MA subsequently
reported app and DB ACTIVE for the bounded signup trial, with the browser signup
form loaded and a password/consent participation request sent. Actual signup,
mail receipt and final cleanup remain PENDING. This addendum neither claims whole
workflow completion nor authorizes any additional native action.

## Final MA Continuation Addendum (2026-10-21)

MA reports the later v8r1 trial completed its bounded authentication flow:
a16753/642988 BOUNDED_AUTH_FLOW_OBSERVED, cleanup PASS, historical JAVA_RELEASED.
Independent checkpoint 6cbdc6 verified restoration and pins, app/DB PID0 and
recovery-console logout. Checkpoint 3d99f4 confirmed SSH key removal and agent
Stopped/Disabled. These supersede the earlier ACTIVE/PENDING continuation state;
they do not alter the failed result preserved by WI008.

Flow evidence 469fc6, user reset-email receipt confirmation, HumanReceipt STAGED
2d1926 and Finish d8876f are detailed in the WI006 final addendum. Reset link was
not opened and no password was changed. No general public-release or unrelated
workflow completion is claimed. SE performed no remote operation. WI009 DocOps
owns current-entry/REQ updates; this change is an evidence addendum only.

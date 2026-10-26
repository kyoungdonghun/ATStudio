---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: guide
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-006.md
    reason: Approved local diagnostic implementation and review
  - path: WI-20261011-ATS-052-handoff.md
    reason: Candidate implementation boundary and exact user approval
---

# WI-20261011-ATS-053 Handoff

> Purpose: Independently review diagnostic-only changes for unchanged security decisions and bounded evidence export.

[WI HEADER]
WI ID: WI-20261011-ATS-053
REQ: REQ-20261011-ATS-006 local diagnostic amendment
Agent: pg
Depends On: WI052 completed local candidate and pinned evidence
Blocks: WI054 DocOps final integration

[WI SUMMARY]
Scope: Independently inspect the exact WI052 semantic delta from preserved WI042 and run isolated local regressions/adversarial probes. Never execute native entrypoints, app, services, sockets, SQL, secrets or remote commands. No production/old artifact/source changes.
Approval: Exact latest user quote is in WI052; only local implementation, tests and review are approved. No native acceptance or historic cause certainty may be inferred.
DoD: Findings first, exact reviewed hashes, reproducible tests and LOCAL_DIAGNOSTICS_READY or HOLD verdict. Ready means a local review candidate only, not deployable trial readiness. Existing cleanup/HTTP/recovery defects remain out of scope.

[ACCEPTANCE CRITERIA]
- [ ] Compare baseline capture, prestart, pre-view, post-view, reload events and exception paths against original evaluation order. Same comparisons and original fail-closed error categories must remain; no extra reads after first failure, no acceptance of changed BPF IDs or reset baseline.
- [ ] Recorded phases, ordering and observed-at times are bounded, internally consistent, and explicitly distinguish NOT_RUN from PASS. Missing, truncated or malformed diagnostics cannot create a PASS; clock failures cannot become leaked raw exceptions.
- [ ] Export whitelist rejects unknown fields/strings, raw secrets/config/DB content/private hashes/BPF IDs, booleans as numbers, nonfinite/out-of-range times, duplicate/oversized rows and invalid status/error combinations. Fixed allowlisted field labels are acceptable; never arbitrary property/table names.
- [ ] Primary failure survives diagnostic failure and cleanup. Test actual attempt JSON persistence -> Session.restore -> public_result, strict final projection, output bounds and hostile sentinels using mocked native boundaries.
- [ ] Test policy-only, BPF-only, DB-only mismatches/errors, successful comparisons and skipped calls. Ensure same-rules/new-ID and policy order-only differences still HOLD; do not silently repair unrelated known issues.
- [ ] Local CLI execution rejects LOCAL_ONLY before side effects even if OS/root guards are spoofed; methods are exercised only with strict native mocks. Review README says NOT_NATIVE_READY, no remote recipes/new claims and future integration needs approval.
- [ ] Pin tested new files and verify old WI042/native-receipt inputs unchanged. Distinguish independent cases from owner reruns and mocks from native proof.

[INPUT POINTERS]
Tier 0 (MA injected): core-principles, documentation-standards, development-standards, glossary.
Tier 1: security-policy; PG role; default-deny/PII/secret export constraints.
Tier 2: WI052 handoff/evidence and build/WI052-state-diagnostics/; WI050 confirmed diagnosis; preserved WI042 runtime/session/tests and WI045 redacted native receipt; REQ006 and current index.
Skills: create-wi-handoff-packet (MA); test for isolated Python tools; create-wi-evidence-pack.

[OUTPUT CONTRACT]
Allowed writes only: build/WI053-state-diagnostics-review/ independent tests/logs/review manifest; deliverables/agent/WI-20261011-ATS-053-evidence-pack.md; deliverables/user/WI-20261011-ATS-053-summary.md. Apply_patch for manual edits. Do not edit SE candidate or historical artifacts. Send actionable bounded findings to MA; current-scope diagnostic defects may be returned to SE for local repair/review, not native retry.

[TRACEABILITY REQUIREMENTS]
Record exact reviewed candidate hashes, actual local commands/counts, preserved original pins and residual limitations. Final verdict must explicitly withhold server execution, capacity acceptance, original cause resolution and public opening. On completion trigger WI054, not an operational trial.

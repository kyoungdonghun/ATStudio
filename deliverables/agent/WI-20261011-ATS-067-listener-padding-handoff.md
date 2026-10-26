---
version: 1.0
last_updated: 2026-10-18
project: ATS
owner: MA
category: reference
status: active
dependencies:
  - path: WI-20261011-ATS-067-listener-v2-handoff.md
    reason: Existing approved scoped listener correction
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Renewed autonomous bounded defect resolution
---
# WI067 Native Listener Padding Continuation

[WI HEADER]
WI: WI-20261011-ATS-067; REQ: REQ-20261011-ATS-007; Agent: se.
Depends On: Exact v2 review and native read-only diagnostic.
Blocks: PG071 incremental review, native listener acceptance, bounded Gmail trial.

[WI SUMMARY]
Why: Native ss pads the scoped TCP owner field with ASCII spaces. Full-match validation rejects this legitimate display padding.
Scope: Preserve immutable v1/v2 candidates and native history. Produce the smallest corrected sibling/runtime and bound delivery/producer identity updates. Add a fixed reviewed cold replacement of only v2 tools; no new release/private stage exists. Reuse verified JAR/SPA. New paths build/WI067-auth-trial-v3, build/WI067-pc-delivery-v3, build/WI067-private-producer-v3 and build/WI067-padding-supersede, with create-new evidence siblings, are owned by SE067.
DoD: Accept only observed harmless trailing ASCII display spaces while fully consuming every owner entry. Preserve rejection of unknown owners, malformed PID/fd, extra data, controls and unexpected scope. PG exact-byte review precedes MA native use.
Forbidden: No agent remote/secret/browser operations; no app or DB starts, mail, public exposure, new packages, deletion/overwrite of history, relaxed service identity/BPF/firewall checks, generic rewrite framework, retry after ambiguous mutation.

[ACCEPTANCE CRITERIA]
- [ ] Native TCP padded fixture passes and UDP unchanged; malformed/unknown/trailing payload fixtures fail.
- [ ] No other runtime behavior changes; original tests and focused PG regressions pass.
- [ ] Fixed cold tool preservation verifies v2 tree, cold source state, absent release/evidence/control/unit paths, exact JAR/SPA and no-replace archive; no daemon reload/service change.
- [ ] New exact runtime/delivery/producer pins, reviewed listener-only proof, complete finite command recipe and concise two-set evidence.

[INPUT POINTERS]
Tier0: core-principles, documentation-standards, development-standards, glossary (MA injected/retained).
Tier1: security-policy, access-control-policy, quality-gates.
Tier2: create-wi-handoff-packet, test, create-wi-evidence-pack skills; WI067/071 existing handoffs.
Native evidence: 9f95b2 COLD_PRESERVED v1; 41a113 PUBLIC_STAGED v2; 1a7bf1 LISTENER_ONLY HOLD; d38ca5 LISTENER_OWNER at native.py:157. bf6f8d shows TCP127.0.0.53%lo:53 owner string users:(("systemd-resolve",pid=556,fd=15)) followed by16 ASCII spaces; UDP DNS/DHCP unpadded. No fresh v2 Release/private stage or app operation occurred. Exact v2 runtime17a4ca56f025bf1809c015dea1bda94fc9dab0854d6042ac16605b5e9701e30d.

[OUTPUT CONTRACT]
SE owns new candidates, WI067 evidence/summary; PG071 owns independent incremental review and exact-byte receipts. Send ready source to PG promptly, then finalize pins. MA executes native operations only after review. Do not re-audit unrelated historical code or rebuild unchanged JAR.

[TRACEABILITY]
Skill-generated continuation under existing approved REQ. Preserve all earlier results as history. A listener-only PASS does not prove actual app, mail, consent or cleanup completion.

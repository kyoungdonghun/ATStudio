# WI-20261011-ATS-039 Handoff Packet

[WI HEADER]
WI ID: WI-20261011-ATS-039
REQ: REQ-20261011-ATS-006 (approved repair amendment)
Agent: se
Depends On: WI038 native HOLD diagnosis and explicit user repair approval
Blocks: WI040
Session ID: current

[WI SUMMARY]
Why: The once-only WI035 run stopped at PREPARE/WEB_SERVICE before app, DB or web changes because the checker required omitted empty Exec properties.
Scope (in): A new isolated WI039 candidate with narrow service-parser and staged masked-MySQL baseline handling, exact target-shape regressions, new namespace/claims, pinned recipes and preservation proof.
Scope (out): No old frozen-file or claim edits; no remote/browser/network/agent actions, actual services, secrets, database access, app/domain/frontend changes, extra purchase, commit/push.
Approval: User explicitly answered "수정·재검토 후 새 1회 웹 검증 승인" after the exact two defects and bounded one-retry scope were explained.
DoD: New frozen candidate, focused diff, target-free tests, immutable old pins, complete executable recipe ready for independent PG. Native execution remains MA-only after PG.
Constraints: Preserve max30min total, existing approved read-only route contract, management-only HTTPS, blocked mail/payment/OAuth/jobs, existing secret handling, fail-closed cleanup and every security predicate. No arbitrary defaults for missing required security properties. Do not fix unrelated P2 intermediate-state reporting or catalog sorting.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Reproduce omitted supervisor ExecReload/ExecStartPre/ExecStop and masked MySQL omitted ExecReload/ExecStart/ExecStartPre/ExecStop plus Type="" from actual systemd output.
- [ ] Only legitimate optional empty Exec keys may normalize to empty. Duplicate/unknown/malformed keys and any missing required scalar security property still HOLD.
- [ ] No claim that --all repairs this; native comparison showed the same omission both ways.
- [ ] Masked baseline requires exact masked/inactive/dead/PID0 and mask-link evidence; nonempty/conflicting execution state must reject. Unmask/start stage still verifies real loaded type/executable/drop-ins/timeouts/network restrictions.
- [ ] Maintain exact approved allowlist (6 GET paths,17 query combinations), no cookies/auth forwarding or mutating probes.
- [ ] Fresh WI039 control/tools/release/upload/unit/claim paths; do not touch consumed WI035 artifacts or earlier pins. New recipe can observe retained failed WI035 supervisor without restarting/resetting/deleting it.
- [ ] Failures at each stage leave truthful final cleanup and no unauthorized retry.
Performance:
- [ ] Existing timers and total bound remain <=30min; cleanup remains independently bounded.
Quality:
- [ ] Use test skill; rerun prior Python and PowerShell regression suites against the new candidate.
- [ ] Add real-output-shaped positive and negative fixtures, staged baseline/unmask tests and regression for all required missing scalar fields.
- [ ] Hash old candidate before/after; freeze new files, recipes and public candidate contents. Explain every semantic diff and namespace-only change.
- [ ] No mocks count as native acceptance.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
- docs/policies/access-control-policy.md
Tier 2:
- docs/index.md (Current Decision Entry)
- deliverables/user/REQ-20261011-ATS-006.md
- deliverables/agent/WI-20261011-ATS-035-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-036-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-037-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-038-evidence-pack.md
- build/WI035-restricted-web-check/
- build/WI036-web-contract/
- build/WI037-restricted-web-review/

Role: .claude/agents/se.md
Target observations: MA sanitized WI038 receipt and service-shape diagnosis injected at dispatch.
React source is not edited; framework guidance does not authorize UI changes.

[OUTPUT CONTRACT]
Candidate -> build/WI039-restricted-web-check/
Agent-facing -> deliverables/agent/WI-20261011-ATS-039-evidence-pack.md
User-facing -> deliverables/user/WI-20261011-ATS-039-summary.md
Handoff -> this file.
Use create-wi-evidence-pack. Edit only new candidate and own deliverables. No REQ/index edits (DocOps owns).

[TRACEABILITY REQUIREMENTS]
Exact source inputs, prior preservation hashes, new file/recipe hashes, commands/tests/limits, differences from WI035, rollback and stop conditions. Blocks WI040 immediately on completion.


# WI-20261011-ATS-042 Handoff Packet

[WI HEADER]
WI ID: WI-20261011-ATS-042
REQ: REQ-20261011-ATS-006 (approved local-only second repair amendment)
Agent: se
Depends On: WI039 native HOLD diagnosis; explicit local-only repair approval
Blocks: WI043
Session ID: current

[WI SUMMARY]
Why: Normal Nginx has two ExecReload commands. Generic unique() rejected its repeated property as DUPLICATE_JSON in PREPARE before any app/DB start.
Scope (in): Create only a fresh local WI042 candidate, parser repair, realistic regression fixtures, strengthened pre-claim read-only preflight, exact freeze/recipes and preservation evidence.
Scope (out): No server/SSH/agent/browser/network/service/DB/secret access, app or frontend changes, purchases, historical edits, commit/push or memory updates.
Approval: User answered "검사 도구 수정·검토 준비 승인" to local-only repair, realistic tests, independent review and pre-execution-check preparation. No new native attempt is approved.
DoD: Bounded local preparation with truthful tests, exact evidence and preserved old files.

[ACCEPTANCE CRITERIA]
- [ ] Preserve ordered repeated Exec command records without overwriting, joining ambiguously, sorting or losing commands; scalar and true JSON duplicate keys remain rejected. Optional omissions and strict masked/loaded MySQL predicates remain.
- [ ] Adapt all service/restore/baseline comparisons and loaded MySQL exact single-command checks to the new representation; prove JSON baseline roundtrip retains order/content.
- [ ] Reproduce repository Nginx two-command reload configuration, normal missing optional keys, command ordering/content changes, scalar duplicates, missing security fields and malformed/unknown fields.
- [ ] Exercise complete prepare and restoration service parsing, not just a mocked unique() unit test. Preserve fail-closed final checks and known P2 intermediate status limitation.
- [ ] Prepared S2 read-only preflight must parse actual existing nginx and masked mysql through the same production path BEFORE approval/claim; it may not mutate units or invoke prepare/start.
- [ ] New WI042 namespace/claims/ports; account for both consumed WI035 and WI039 failed supervisors/claims without deleting/resetting/reusing them. Runtime bounds, six GET paths/17queries, secret handling, CSP/egress, cleanup remain unchanged.
- [ ] Freeze all new runtime/recipe/review files; old035/039/040 and earlier registered pins unchanged; recipe explicitly LOCAL READY ONLY and future remote use needs new approval.
- [ ] Prior owner regressions plus targeted additions and local Python/PowerShell syntax tests PASS; no Gradle/npm rebuild or remote acceptance claim.

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
- deliverables/agent/WI-20261011-ATS-039-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-040-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-041-handoff.md
- build/WI039-restricted-web-check/
- build/WI040-restricted-web-review/
- build/WI036-web-contract/
- deploy/single-vps/tls-quarantine/nginx-service.conf

Role: .claude/agents/se.md
Framework source is out of scope; no React change is authorized.

[OUTPUT CONTRACT]
Candidate -> build/WI042-restricted-web-check/
Agent-facing -> deliverables/agent/WI-20261011-ATS-042-evidence-pack.md
User-facing -> deliverables/user/WI-20261011-ATS-042-summary.md
Handoff -> this file
Skills: test where applicable; create-wi-evidence-pack; validate-docs for DocOps.
Write only the assigned candidate/review area and own deliverables, except DocOps current entries explicitly listed.

[TRACEABILITY REQUIREMENTS]
Full exact source/freeze/recipe pins; actual local commands/counts/failures; target fixtures versus observed evidence; no native success inference. Trigger Blocks immediately on completion.


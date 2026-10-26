# WI-20261011-ATS-043 Handoff Packet

[WI HEADER]
WI ID: WI-20261011-ATS-043
REQ: REQ-20261011-ATS-006 (approved local-only second repair amendment)
Agent: pg
Depends On: WI042 exact final frozen candidate
Blocks: WI044
Session ID: current

[WI SUMMARY]
Why: The previous independent review missed valid multiple ExecReload entries. Independently review exact repair and pre-claim coverage, not repeat the same mock assumption.
Scope (in): Local-only exact-byte review and independent adversarial tests; provide readiness verdict with no remote execution authority.
Scope (out): No server/SSH/agent/browser/network/service/DB/secret access, app or frontend changes, purchases, historical edits, commit/push or memory updates.
Approval: User answered "검사 도구 수정·검토 준비 승인" to local-only repair, realistic tests, independent review and pre-execution-check preparation. No new native attempt is approved.
DoD: Bounded local preparation with truthful tests, exact evidence and preserved old files.

[ACCEPTANCE CRITERIA]
- [ ] Review ordered multi-Exec properties, every consumer/baseline/restore comparison and strict scalar/JSON rejection.
- [ ] Independently construct valid two-command Nginx and malicious/malformed variants, including dropped/reordered/changed records and baseline serialization; exercise genuine prepare/restore paths with mocked effects.
- [ ] Confirm prepared preflight invokes the actual parser on existing nginx/mysql before approval/claim and cannot mutate services or read/expose secrets beyond existing shape design.
- [ ] Confirm preserved routes/identity/lifetime/egress/secret/cleanup controls; no reused old claim or silent relaxations.
- [ ] Verify full new pins and every recipe, preservation proof, Python/PowerShell regression counts, and provenance limits.
- [ ] Native/remote/browser/DB/app all NOT_RUN; verdict is local preparation only, future MA execution separately approved. No candidate edits; HOLD findings go to MA.

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

Role: .claude/agents/pg.md
Framework source is out of scope; no React change is authorized.

[OUTPUT CONTRACT]
Review helpers -> build/WI043-restricted-web-review/
Agent-facing -> deliverables/agent/WI-20261011-ATS-043-evidence-pack.md
User-facing -> deliverables/user/WI-20261011-ATS-043-summary.md
Handoff -> this file
Skills: test where applicable; create-wi-evidence-pack; validate-docs for DocOps.
Write only the assigned candidate/review area and own deliverables, except DocOps current entries explicitly listed.

[TRACEABILITY REQUIREMENTS]
Full exact source/freeze/recipe pins; actual local commands/counts/failures; target fixtures versus observed evidence; no native success inference. Trigger Blocks immediately on completion.


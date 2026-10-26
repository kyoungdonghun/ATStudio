---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: handoff
status: active
dependencies:
  - path: ../user/REQ-20261010-ATS-010.md
    reason: Approved restricted TLS scope and latest explicit unresolved-NS exception
  - path: WI-20261011-ATS-002-evidence-pack.md
    reason: Prior exact-candidate independent security review
---

# WI-20261011-ATS-004: Restricted TLS Resume Review

[WI HEADER]
WI ID: WI-20261011-ATS-004
REQ: REQ-20261010-ATS-010
Agent: pg
Depends On: WI-20261011-ATS-002, WI-20261011-ATS-003, latest user exception approval
Blocks: WI-20261011-ATS-005 final receipt integration

[WI SUMMARY]
Why: User now explicitly permits recording the unresolved child-apex NS inconsistency and trying restricted HTTPS. Previous hold and cleanup remain historical facts.
Scope: Independently review the bounded resumption and exact Certbot2.9 no-email staging invocation against prior reviewed implementation; write review deliverables only.
DoD: Clear PASS/HOLD with command preconditions, unchanged app/DB denial, CA registration boundary, rollback/cleanup, and unresolved DNS implications.
Constraints: No remote access, browser mutation, key/secret reads, install, CA request, billing, public opening, code/config edits, commit/push. Only MA performs approved operational actions after fresh gates. New temporary SSH-agent approval is currently pending.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Distinguish NS anomaly from its unknown cause and downstream impact; HTTPS success does not resolve DNS.
- [ ] Validate native2.9-compatible no-email register and certonly --dry-run staging sequence, explicit endpoint/webroot/apex and existing-account handling.
- [ ] Separate staging success from production issuance/TLS/renewal acceptance and preserve action-time scope.
- [ ] Define current masks/service/source/identity/recovery checks before reusing retained artifacts; no reinstall/recreate/overwrite.
Performance:
- [ ] Bound DNS/HTTP probes and CA attempts; no continuous polling or blind retry.
Quality:
- [ ] Fresh MA target checks required; local review is not remote acceptance.
- [ ] Use create-wi-evidence-pack skill and report scoped validation.

[INPUT POINTERS]
Tier0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
- docs/standards/evidence-pack-standard.md
Tier1:
- .claude/agents/pg.md
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
Tier2:
- .agents/skills/create-wi-evidence-pack/SKILL.md
- docs/templates/eval-report-template.md
- deploy/single-vps/runbooks/domain-tls.md
Context:
- deliverables/user/REQ-20261010-ATS-010.md
- deliverables/agent/WI-20261011-ATS-002-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-003-evidence-pack.md
- scripts/deployment/tls-quarantine.py
- deploy/single-vps/tls-quarantine/
- docs/index.md

[OUTPUT CONTRACT]
User-facing: deliverables/user/WI-20261011-ATS-004-summary.md
Agent-facing: deliverables/agent/WI-20261011-ATS-004-evidence-pack.md
Write scope: Those two new files only, using apply_patch.
Return concise review verdict before final receipts; no runtime success claims.

[TRACEABILITY REQUIREMENTS]
Latest user: "NS 문제는 미해결로 기록해두고, 제한된 HTTPS 시험부터 진행해보자. 앱이나 DB는 아직 공개하지 말고, 추가 비용이나 내가 직접 해야 할 건 먼저 알려줘."
Prior consent: LE v1.8 no-email staging/production/renewal approved. Compare current agreement before actions.
MA latest local observation: SSH-agent Stopped/Disabled. No remote actions in this resumed window yet.
Historical closure: provider SSH-only; Nginx/MySQL masked/inactive; Certbot runtime-masked, no timer enablement; no accounts/certificates. Must refresh, not assume.
MA latest read-only DNS checks: parent referral correct; both authority apex A correct, NS AA NOERROR/NODATA with SOA2026101101; both DoH providers confirm NS NODATA. UI host registration is optional own-nameserver glue, not missing required setup. No exact provider cause confirmed.
Rollback: No runtime writes by PG; preserve earlier evidence and unresolved risks.


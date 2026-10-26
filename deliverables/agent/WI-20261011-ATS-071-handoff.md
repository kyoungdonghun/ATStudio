---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: reference
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Approved bounded Gmail functional validation
  - path: WI-20261011-ATS-067-handoff.md
    reason: Candidate implementation and exact native boundaries
---
# WI-20261011-ATS-071
[WI HEADER]
WI ID: WI-20261011-ATS-071
REQ: REQ-20261011-ATS-007
Agent: pg
Depends On: WI067 local components and network proposal
Blocks: WI067 complete native binding; later MA functional trial

[WI SUMMARY]
Why: Review the minimal app-only SMTP egress and bounded account mutation design now, then review completed exact implementation.
Scope: Own build/WI071-review/ and named reports. Read WI067 source, actual app endpoints, reviewed WI060 patterns. Review proposed UID-scoped nft port intersection with unchanged exact BPF comparisons, fixed management ingress, private credential projection, mutation ledger and independent cleanup. Initial design verdict should unblock local binding implementation promptly; it is not native release.
DoD: Concrete findings, risk-based independent tests, DESIGN_ALLOW or HOLD with precise remedies, then exact-byte final review when implementation arrives. Distinguish account/human prerequisites from technical defects.
Forbidden: No native/server/cloud/browser calls, real secrets/private files, mail/account writes, app or DB start, provider rules, new dependencies, candidate edits, subdelegation, commit/push. No invented consent or bypass of human credential creation.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Only designated Gmail SMTP, exact loopback DB/DNS and management ingress can reach app; reject other egress.
- [ ] Failures close admission and independently observe/clean app, DB, owned rules and original quarantine.
- [ ] Bound routes, principal, attempts, duration, request sizes and expected DB changes; no payment/media/admin/reset submission.
Quality:
- [ ] Review exact candidate/source identities, independent synthetic tests, findings-first evidence.
- [ ] No local preparation promoted to runtime or actual mail delivery PASS.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md; docs/standards/documentation-standards.md; docs/standards/development-standards.md; docs/standards/glossary.md
Tier1: docs/policies/security-policy.md; docs/policies/access-control-policy.md; docs/policies/quality-gates.md; docs/standards/evidence-pack-standard.md
Tier2: .agents/skills/test/SKILL.md; .agents/skills/create-wi-evidence-pack/SKILL.md
Context: deliverables/user/REQ-20261011-ATS-007.md; deliverables/agent/WI-20261011-ATS-067-handoff.md; build/WI067-auth-trial/README.md; build/WI060-restricted-web/; src/main/java/com/atstudio/atstudio/service/EmailService.java

[OUTPUT CONTRACT]
User: deliverables/user/WI-20261011-ATS-071-summary.md
Agent: deliverables/agent/WI-20261011-ATS-071-evidence-pack.md
Review workspace: build/WI071-review/
Handoff: this skill-generated packet; all code changes remain SE-owned.

[TRACEABILITY REQUIREMENTS]
First design result promptly, then tested exact-byte final verdict. Preserve historical native outcomes. Current backup pending and human password/consent requirements remain explicit, not generic blockers for local implementation.

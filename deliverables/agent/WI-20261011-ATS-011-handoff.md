---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: handoff
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-001.md
    reason: Direct user approval for bounded validator compatibility correction
  - path: WI-20261011-ATS-010-evidence-pack.md
    reason: Historical scoped review superseded by MA native ENV_KEYS hold
---

# WI-20261011-ATS-011: Public Environment Contract Compatibility

[WI HEADER]
WI ID: WI-20261011-ATS-011
REQ: REQ-20261011-ATS-001
Agent: se
Depends On: WI009/WI010 history and user approval on 2026-10-11
Blocks: WI-20261011-ATS-012 independent review

[WI SUMMARY]
Why: Native v4 stopped at APPROVED_ENV_VALIDATION; sanitized diagnosis returned ENV_KEYS. Public bootstrap requires classpath:/ but schema_password rejects that key.
Scope: Exact safe-value acceptance, full public bootstrap environment regression, fresh versioned local candidate and bounded runbook update.
DoD: Required public configuration accepted, external/unknown mapping configuration still rejected, earlier real-validator and JDBC regressions preserved, exact new candidate pinned.
Constraints: Local only. No remote, browser, DB, private files, SSH agent, install, dependencies, app startup, source/JAR rebuild, purchases, deletion, commit, push or descendants. Never change production environment to bypass validation. Preserve old candidate/operator/evidence bytes and unrelated dirty work.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Accept only the exact existing SPRING_CONFIG_LOCATION="classpath:/" line in the restricted mapping-key guard.
- [ ] Continue rejecting other locations, additional-location/import settings, unknown overrides, duplicate keys and malformed environment input.
- [ ] Regression covers the complete public bootstrap-rendered environment using synthetic secrets only; no handpicked partial fixture alone.
- [ ] Demonstrate old v4 rejection and corrected acceptance locally without network or private material.
- [ ] Run actual Hibernate matching/missing-table/missing-column and existing strict JDBC regressions; attribute tests honestly.
- [ ] Fresh build/WI011-schema-v5 candidate binds approval review WI012 and retains exact pinned application artifact identity.
Quality:
- [ ] No weakened guard, broad prefix exemption or private output.
- [ ] Focused tests, no-connect smoke, direct whitespace checks and evidence-pack complete.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/documentation-standards.md; docs/standards/development-standards.md; docs/standards/glossary.md.
Tier 1: .claude/agents/se.md; docs/policies/security-policy.md.
Tier 2: .agents/skills/test/SKILL.md; .agents/skills/build-check/SKILL.md; .agents/skills/create-wi-evidence-pack/SKILL.md; approved REQ; WI009/WI010 evidence; deploy/single-vps/CONFIGURATION.md; deploy/single-vps/env/contract.json; public env example; production-bootstrap.py render_env and validate_bundle.py contract; current release helper/tests/runbook.
No frontend code work: React-specific guidance is not operative for this Python verification-only correction.

[OUTPUT CONTRACT]
Write only scripts/deployment/release-readiness.py, scripts/deployment/test_release_readiness.py, deploy/single-vps/runbooks/release-readiness.md, own WI011 summary/evidence, and fresh unique build outputs. No changes to Java helpers, shared REQ/index, old evidence or prior candidate directories. Ask MA before expanding scope.

[TRACEABILITY REQUIREMENTS]
Created using create-wi-handoff-packet after approved REQ, current WI sequence and workspace/context rules verification. Existing full Tier0/security injection retained and new context supplied by MA. Use create-wi-evidence-pack. User approval: "검증 도구 수정·회귀시험·재검토 진행". Native schema remains NOT_RUN; MA cleaned MySQL and SSH access. Immediately hand results to MA for WI012; no target execution in this WI.

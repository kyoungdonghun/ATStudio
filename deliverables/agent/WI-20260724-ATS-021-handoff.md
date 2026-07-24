[WI HEADER]
WI ID: WI-20260724-ATS-021
REQ: REQ-20260724-ATS-002
Agent: pg
Depends On: WI-20260724-ATS-011
Blocks: WI-20260724-ATS-020

[WI SUMMARY]
Why: The live npm advisory database now reports two moderate React Router findings against the locked `react-router-dom`/`react-router` 6.30.4 baseline, although the prior V1 audit reported zero.
Scope (in/out): Independently establish the advisory details, affected runtime surfaces, ATStudio call-site exploitability, and available remediation paths. Determine whether a safe application-level mitigation exists without a major router migration. This WI is review-only and must not upgrade dependencies or modify product code.
DoD: Each advisory has a source/range, reachable or non-reachable ATStudio usage assessment, concrete evidence, recommended disposition, and a clearly identified approval point if a major dependency upgrade is required.
Constraints/Forbidden: Do not run `npm audit fix`, change lockfiles, upgrade to React Router 7, suppress audit output, or claim non-applicability merely because ATStudio is an SPA. Do not browse non-primary technical sources when external verification is needed.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Record the exact installed versions and current advisory identifiers/ranges.
- [ ] Inventory every `Link`, `NavLink`, `navigate`, redirect, and hydration/SSR-related call site that can receive user-, query-, hash-, or server-controlled values.
- [ ] Assess open-redirect/XSS and constructor-injection reachability separately.
- [ ] Compare major-upgrade, application mitigation, and documented residual-risk options.
Performance:
- [ ] Review-only; no runtime impact.
Quality:
- [ ] Findings cite local call sites and primary advisory/package evidence.
- [ ] No repository or lockfile change outside the two WI-021 deliverables.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
Tier 1 (Policies - Inferred):
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
Tier 2 (Task Context):
- docs/standards/evidence-pack-standard.md
- docs/standards/frontend-standards.md
REQ/Context Docs:
- deliverables/user/REQ-20260724-ATS-002.md
- deliverables/agent/WI-20260724-ATS-011-evidence-pack.md
Files:
- frontend/package.json
- frontend/package-lock.json
- frontend/src/

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260724-ATS-021-summary.md :
- Advisory disposition, risk level, recommendation, approval point
Agent-facing -> deliverables/agent/WI-20260724-ATS-021-evidence-pack.md :
- Advisory evidence, call-site inventory, reachability analysis, repro commands
Handoff Packet -> deliverables/agent/WI-20260724-ATS-021-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Record audit and static call-site commands; no mutation
Rollback: Review-only; no code rollback

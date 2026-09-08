---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: MA
category: reference
status: confirmed
---

# WI-20260908-ATS-015: Authentication, Identity and PII Boundaries

[WI HEADER]
WI ID: WI-20260908-ATS-015
REQ: REQ-20260908-ATS-004
Agent: pg
Depends On: none
Blocks: WI-20260908-ATS-018

[WI SUMMARY]
Why: Bounded release-readiness audit of main 8161f0a, not feature development.
Scope: AuthService, OAuth2Service, JwtAuthenticationFilter/Provider, CustomUserDetailsService, SecurityConfig, AuthRateLimitFilter, user request/response DTOs, frontend auth store/API/interceptors. Check registration role/type/certification fields, refresh/logout/revocation, password reset/email verification, OAuth identity verification, role/ownership enforcement, error/log PII, XSS/open redirect and trust in browser state. Existing tests and policy are evidence; do not hypothesize a finding without a concrete path.
DoD: Map important requirements to concrete implementation and existing negative tests; identify only actionable findings with exact file/line, trigger, impact, counterevidence and severity; report limits and hand back bounded verification commands.
Forbidden: Product/config/runtime/test modifications; Git writes; live financial/mail/data mutations; credential-file reads; full-repo dumps; Gradle/npm execution (MA owns it). You may write ONLY this WI's evidence-pack and user summary with apply_patch.

[ACCEPTANCE CRITERIA]
- [x] Inspect relevant docs and primary code paths rather than assume old audit results are current.
- [x] Every finding has a reachable path and existing defenses considered; distinguish confirmed, conditional and unverified.
- [x] Separate release blocker, maintenance and optional cleanup; say clearly when no blocker is found in this scope.
- [x] Provide specific existing test classes/files MA can run without external side effects; note mock-only blind spots.
- [x] Keep evidence concise (target <=150 lines) and user summary <=40 lines; no secret values.

MA closeout: review accepted into WI018; two P1 and four P2 findings, not implemented. Reviewer closed after report verification. Blocks dependency consumed by WI018.

[INPUT POINTERS]
Tier 0 (ordered injected pointers):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/index.md#current-v1-baseline
- docs/design/api-spec.md
- docs/SR/SR-93.md (current open gates, preserve historical evidence)
- deliverables/user/REQ-20260908-ATS-004.md
- Related domain design/requirements and current src/main, src/test, frontend/src discovered by bounded rg.

[OUTPUT CONTRACT]
Agent-facing: deliverables/agent/WI-20260908-ATS-015-evidence-pack.md
User-facing: deliverables/user/WI-20260908-ATS-015-summary.md
Use create-wi-evidence-pack skill and this handoff. Do not create other files or wait for MA. Complete independent review and notify MA immediately if a credible P0/P1 requires containment, without exploiting or mutating live data.

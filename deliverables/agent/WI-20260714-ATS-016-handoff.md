[WI HEADER]
WI ID: WI-20260714-ATS-016
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260714-ATS-003, WI-20260714-ATS-015
Blocks: WI-20260714-ATS-017, WI-20260714-ATS-020, WI-20260714-ATS-022, WI-20260714-ATS-024

[WI SUMMARY]
Why: Preserve per-client abuse controls through Cloudflare and Vite without trusting spoofed forwarding headers or wildcard Hosts.
Scope: Exact Vite Host allowlist, proxy header sanitization/internal client identity, loopback trust enforcement, rate-limit key integration, CORS tightening, and tests.
Out: Starting Cloudflare, adding dependencies, changing general rate limits, or trusting arbitrary proxy ranges.
DoD: Only loopback Vite may assert one validated client IP; direct spoofing is ignored/rejected; unknown Hosts fail; same-origin public traffic needs no wildcard CORS.
Constraints: Preserve relative `/api` and `/uploads`. Do not edit acceptance secret/public-URL contract from WI-015 except consuming it.

[ACCEPTANCE CRITERIA]
- [ ] `allowedHosts: true` is removed and local plus injected public Host are explicit.
- [ ] Vite overwrites/removes inbound forwarding headers before proxying.
- [ ] Spring trusts only the internal header from configured loopback peer and validates one IP literal.
- [ ] Rate-limit tests prove direct spoof rejection and separate effective identities.
- [ ] Typecheck/lint/focused backend/frontend tests and diff check pass.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/development-standards.md
Tier 1: docs/policies/security-policy.md; docs/policies/access-control-policy.md
Tier 2: docs/standards/frontend-standards.md
Context: deliverables/user/REQ-20260714-ATS-001.md; docs/design/p1-security-acceptance-hardening-design.md; docs/SR/SR-42.md
Files: frontend/vite.config.ts; AuthRateLimitFilter/Properties; SecurityConfig/CorsConfig if needed; new bounded client-IP resolver; focused tests

[OUTPUT CONTRACT]
User summary: deliverables/user/WI-20260714-ATS-016-summary.md (Korean)
Evidence Pack: deliverables/agent/WI-20260714-ATS-016-evidence-pack.md
Implementation ownership: Vite Host/proxy and backend trusted-client-identity path plus tests.

[TRACEABILITY REQUIREMENTS]
Evidence/commands/tests/rollback required; public topology remains WI-022 evidence.

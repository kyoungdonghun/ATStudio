[WI HEADER]
WI ID: WI-20260724-ATS-014
REQ: REQ-20260724-ATS-002
Agent: qa-integ
Depends On: WI-20260724-ATS-013, WI-20260724-ATS-020
Blocks: WI-20260724-ATS-015, WI-20260724-ATS-016

[WI SUMMARY]
Why: Verify the fresh clone and fresh DB as a running application before any external Provider mutation.
Scope (in/out): Start backend and frontend on isolated loopback ports from the fresh clone and runtime DB. Exercise public, authentication, subscriber, business, admin, media, whitelist, certification, and non-mutating payment/admin reads through API and browser smoke. No Toss mutation or real email.
DoD: Readiness, representative role/API/UI paths, proxying, file access boundaries, and secret-safe output pass.
Constraints/Forbidden: Do not use Cloudflare or fixed client ports in this phase. Do not use current DB, existing server processes, destructive admin mutations, or external Provider calls.

[ACCEPTANCE CRITERIA]
- [ ] Isolated backend/frontend ports become ready.
- [ ] Public and role-protected API expectations pass.
- [ ] Representative SPA routes render without console/network failures.
- [ ] Streaming works and official download authorization remains enforced.
- [ ] Payment/admin reads expose only support-safe identifiers.
- [ ] Logs contain no secrets, raw billing keys, auth keys, or card data.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/design/api-spec.md
- docs/ui/atstudio-front-list.md
- docs/client/2-full-feature-checklist.md
- docs/client/3-admin-checklist.md
REQ/Context:
- deliverables/user/REQ-20260724-ATS-002.md
- deliverables/agent/WI-20260724-ATS-013-evidence-pack.md

[OUTPUT CONTRACT]
User-facing -> `deliverables/user/WI-20260724-ATS-014-summary.md`
Agent-facing -> `deliverables/agent/WI-20260724-ATS-014-evidence-pack.md`

[TRACEABILITY REQUIREMENTS]
Record ports, process ownership, redacted environment source, API matrix, browser screenshots/console results, log scans, and processes handed to later WIs.

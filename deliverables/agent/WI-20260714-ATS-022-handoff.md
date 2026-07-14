[WI HEADER]
WI ID: WI-20260714-ATS-022
REQ: REQ-20260714-ATS-001
Agent: qa-integ
Depends On: WI-20260714-ATS-009, WI-20260714-ATS-010, WI-20260714-ATS-011, WI-20260714-ATS-014, WI-20260714-ATS-015, WI-20260714-ATS-016, WI-20260714-ATS-017, WI-20260714-ATS-020, WI-20260714-ATS-021, WI-20260714-ATS-035
Blocks: WI-20260714-ATS-024, WI-20260714-ATS-025, WI-20260714-ATS-027, WI-20260714-ATS-034

[WI SUMMARY]
Why: Prove the hardened acceptance topology through an ephemeral Cloudflare HTTPS origin before deciding whether it is ready to share with the client.
Scope: Preflight external secrets/test credentials, lifecycle start/status/stop, local/public frontend/API/media checks, Host/header/rate-limit identity probes, login/admin smoke, certification public denial, and non-monetary Toss test callback readiness where safely possible.
Out: Sharing the URL with the client, real payment, live Toss credentials, live SMTP, production DB, data deletion, or persistent tunnel installation.
DoD: One ephemeral public origin passes the approved smoke matrix and teardown proves owned processes/ports/public URL are closed; failures produce no URL-ready verdict.
Constraints: Do not print or persist secrets. Use only the acceptance profile and disposable/rehearsed schema-compatible test environment. Stop and report if required external values are absent rather than inventing them.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Start emits a URL only after local/public frontend and API 2xx/3xx readiness.
- [ ] `/api` and `/uploads` remain same-origin through Vite; source audio and company documents remain denied.
- [ ] Unknown Host and spoofed forwarding headers do not change trusted identity; two external clients are separated where observable.
- [ ] Login, session termination, representative subscriber/admin navigation, media, and upload boundaries pass without real payment.
- [ ] Stop is idempotent and closes owned tunnel/frontend/backend in order.
Quality:
- [ ] Evidence redacts public secrets/tokens and records only the ephemeral URL for MA review, not client delivery.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/SR/SR-42.md
- deliverables/agent/WI-20260714-ATS-009-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-010-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-011-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-014-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-015-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-016-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-017-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-020-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-021-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-035-evidence-pack.md
Files:
- scripts/acceptance/
- frontend/vite.config.ts
- acceptance/security configuration and focused tests

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-022-summary.md (Korean)
Agent-facing -> deliverables/agent/WI-20260714-ATS-022-evidence-pack.md
Handoff -> deliverables/agent/WI-20260714-ATS-022-handoff.md

[TRACEABILITY REQUIREMENTS]
Preflight, local/public status matrix, redacted headers, lifecycle ownership/teardown, limitations, rollback, and explicit client-sharing decision are required.

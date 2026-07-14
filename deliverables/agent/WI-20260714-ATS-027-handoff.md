[WI HEADER]
WI ID: WI-20260714-ATS-027
REQ: REQ-20260714-ATS-001
Agent: docops
Depends On: WI-20260714-ATS-022, WI-20260714-ATS-024, WI-20260714-ATS-025
Blocks: WI-20260714-ATS-028, WI-20260714-ATS-029, WI-20260714-ATS-030, WI-20260714-ATS-031, WI-20260714-ATS-032, WI-20260714-ATS-033, WI-20260714-ATS-034

[WI SUMMARY]
Why: Give the client and operator a simple, accurate acceptance checklist and repeatable environment procedure without exposing internal secrets.
Scope: Update Korean client checklist for user-visible flows and an English operator procedure for acceptance startup/status/stop, preflight, safe test data, expected failures, teardown, and issue reporting.
Out: Sharing a public URL, revealing test credentials, production deployment, real payment, or internal implementation-heavy client prose.
DoD: A non-technical client can follow the checklist; an operator can safely reproduce and close the environment; each item maps to current routes/evidence.
Constraints: Client prose must be easy Korean. Do not embed passwords, keys, ephemeral URLs, private paths, raw headers, or dangerous payment/admin steps.

[ACCEPTANCE CRITERIA]
- [ ] Checklist covers login/logout, music/media, subscriptions without real charge, whitelist, company certification, and admin screens at a client-understandable level.
- [ ] Destructive/refund/real-payment actions are excluded or clearly marked operator-only.
- [ ] Operator procedure documents acceptance profile, external env values, disposable DB, Cloudflare topology, ownership status, stop/cleanup, and client URL gate.
- [ ] Expected result and issue-report fields are explicit for every client section.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/SR/SR-42.md
- docs/client/
- deliverables/agent/WI-20260714-ATS-022-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-024-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-025-evidence-pack.md
- scripts/acceptance/

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-027-summary.md (Korean)
Agent-facing -> deliverables/agent/WI-20260714-ATS-027-evidence-pack.md
Handoff -> deliverables/agent/WI-20260714-ATS-027-handoff.md

[TRACEABILITY REQUIREMENTS]
Checklist inventory, route/evidence mapping, excluded-dangerous-actions list, operator lifecycle proof, rollback, and remaining client-test limitations are required.

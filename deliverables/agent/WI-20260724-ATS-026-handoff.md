[WI HEADER]
WI ID: WI-20260724-ATS-026
REQ: REQ-20260724-ATS-002
Agent: qa-integ
Depends On: WI-20260724-ATS-015
Blocks: WI-20260724-ATS-017

[WI SUMMARY]
Why: WI-015 passed the Toss test-key gate and reached the test card form, but the prepared HTTPS loopback callback was not served and the automation surface could not observe the post-submit result.
Scope (in/out): Expose only the isolated Vite frontend through a temporary owned Cloudflare quick tunnel, restart only the owned disposable backend with that exact HTTPS origin, prove local/public readiness, then resume the Toss test-only recurring-payment acceptance flow from a fresh prepared order. No live keys, real money, production DB, client branch, external mail, or production deployment.
DoD: Test-key classification remains fail-closed; callback, CORS, and public origin match; an operator-controlled browser completes the supported Toss test billing-auth flow; every executed charge/refund transition has local/Provider parity or is explicitly blocked with evidence; all owned tunnel/runtime resources remain traceable for WI-017 cleanup.
Constraints/Forbidden: Expose only the frontend port and rely on its `/api` proxy. Never expose MySQL or backend directly. Never print or persist keys, auth/customer/billing keys, raw card data, bearer tokens, QA passwords, or exact Provider identifiers. Refund only a payment created in this resumed rehearsal. Do not infer success from browser navigation alone.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Cloudflare quick tunnel is owned, recorded, and serves the isolated frontend over HTTPS.
- [ ] Backend acceptance callbacks and CORS use the exact public HTTPS origin.
- [ ] Local and public `/api` readiness pass before Provider mutation.
- [ ] Toss client and secret keys classify as test-only without value disclosure.
- [ ] A fresh billing-auth order completes through backend confirmation and first recurring charge.
- [ ] Representative upgrade or pending plan/cycle change passes.
- [ ] Cancellation and reactivation pass.
- [ ] Refund request, approval, and execution use only the rehearsal payment.
- [ ] Receipt, audit, reconciliation, and local/Provider parity are support-safe.
Performance:
- [ ] Tunnel and callback checks use bounded timeouts and no unbounded retries.
Quality:
- [ ] Unknown/live keys fail closed.
- [ ] No secret or raw Provider identifier appears in logs or evidence.
- [ ] Existing incomplete orders are handled through supported expiration/state transitions, not ad hoc DB deletion.
- [ ] Documentation validation and git diff checks pass.

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
- docs/standards/evidence-pack-standard.md
- docs/payment/acceptance-test-checklist.md
- docs/payment/user-flows.md
- docs/payment/admin-operations-guide.md
- docs/design/payment-operations-runbook.md
- docs/SR/SR-42.md
- docs/SR/SR-93.md
REQ/Context Docs:
- deliverables/user/REQ-20260724-ATS-002.md
- deliverables/agent/WI-20260724-ATS-015-handoff.md
- deliverables/agent/WI-20260724-ATS-015-evidence-pack.md
- deliverables/agent/WI-20260724-ATS-024-evidence-pack.md
Files:
- src/main/resources/application-acceptance.yml
- frontend/vite.config.ts
- scripts/acceptance/AcceptanceLifecycle.psm1
Repro/Logs:
- C:/Users/jm991/AppData/Local/ATStudio/release-rehearsal-runtime-3147873-20260724/wi015-20260724T142846Z-16f958ec

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260724-ATS-026-summary.md
Agent-facing -> deliverables/agent/WI-20260724-ATS-026-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260724-ATS-026-handoff.md

[TRACEABILITY REQUIREMENTS]
Record public-origin ownership, exact process trees without secrets, test-key gate, executed/blocked payment matrix, support-safe state transitions, cleanup ownership, commands, and rollback.

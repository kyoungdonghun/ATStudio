[WI HEADER]
WI ID: WI-20260809-ATS-060
REQ: REQ-20260809-ATS-001 (approved 2026-08-09 user single-gate approval)
Agent: pg
Depends On: - (USER-approved decision input is complete)
Blocks: WI-042 auth/account correction, WI-072 auth/Profile documentation synchronization, WI-077 authenticated Profile and social-OAuth acceptance evidence, and any implementation that relies on these identity/session rules

[WI SUMMARY]
Why: Record the USER-approved identity, consent, session, return-target, and logout policy decisions as a decision record before dependent product implementation or verification proceeds.

Scope (in):
- Record the four approved decisions below exactly as binding policy decisions, including their stated limits and failure semantics.
- Produce decision-record-only traceability and the required two-set future outputs.

Scope (out):
- No frontend, backend, API, authentication-flow, routing, database/schema, migration, test-fixture, deployment, or product-behavior write.
- No implementation, verification against a running application, or remediation of any finding.

Approved decisions to encode exactly:
1. Signup requires separate Terms of Service and Privacy Collection/Use consent; marketing is optional and independent. Persist the authenticated user identity, policy type/version, and agreement timestamp; do not invent additional retention such as IP/device fingerprint.
2. An unverified account cannot receive or persist an authenticated application session. A correct credential result must lead only to email-verification guidance/resend flow; no protected/member capability.
3. Login return targets retain only same-application pathname and query; reject external/protocol-relative/backslash/control/API/uploads/auth-flow/hash values. Apply current role/user-type access rules after login; unsupported or inaccessible values fall back to slash.
4. Logout follows hybrid server-first: attempt POST /api/auth/logout; 204 and confirmed invalid/revoked session are success; any settled network/server failure still clears local session and navigates away with an explicit server-revocation-could-not-be-confirmed notice. Callers await settlement, prevent duplicates, and do not claim confirmed server revocation on failure.

DoD:
- A decision-only record faithfully encodes all four USER-approved decisions without expansion, reinterpretation, or unapproved retention/control requirements.
- The record distinguishes confirmed server revocation from a local-only logout completion after settled failure.
- Traceability links the approved REQ and consolidated findings, states downstream dependency/block relationships, and records that no product or external state changed.

Constraints/Forbidden:
- Do not inspect protected output artifacts or ignored secrets; do not expose, copy, log, or infer secret values.
- Do not inspect output artifacts beyond the approved input pointers needed for this decision-record task.
- Do not call APIs, browse protected pages, send email, invoke a provider, access a database, or cause any external effect.
- Do not modify product code, frontend, backend, API specifications, runtime configuration, database/schema, migrations, fixtures, tests, or product documentation.
- Do not stage, commit, push, branch, merge, reset, or otherwise mutate Git state.
- Do not create any file other than the two future output-contract deliverables when the delegated WI is executed; this handoff packet itself is the sole file created by this invocation.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] The decision record contains all four approved decisions verbatim, including separate consent, no authenticated session before verification, bounded return target, and hybrid server-first logout semantics.
- [ ] The decision record includes no invented retention, such as IP address or device fingerprint, and no unapproved implementation detail.
- [ ] The logout decision distinguishes confirmed server revocation from settled failure and requires local-session clearing, navigation away, explicit notice, awaited settlement, and duplicate prevention.
- [ ] Dependencies and blocked follow-up WIs are traceable without assigning or performing implementation.

Performance:
- [ ] Not applicable: this is an offline decision-record task with no runtime path or external operation.

Quality:
- [ ] No product write, database access, external action, secret/output-artifact inspection, or Git mutation occurred.
- [ ] Output paths, source pointers, and USER approval status are internally consistent.
- [ ] Evidence Pack explicitly records the no-change/no-external-action result and any remaining implementation follow-up as blocked by this decision record.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md - approval gate, two-set deliverables, and escalation constraints.

Tier 1 (Policies - Required/inferred):
- docs/policies/security-policy.md - privacy, secret-handling, authentication/session, and protected-resource policy.
- docs/policies/quality-gates.md - evidence and quality-gate requirements for a decision-only WI.
- docs/policies/access-control-policy.md - current role/user-type access rules relevant to post-login return-target authorization.

Tier 2 (Current auth/navigation context):
- docs/design/api-spec.md - current auth endpoint and contract context, including the logout route.
- docs/design/usecase/user-info.md - current registration, identity, and Profile flow context.
- docs/ui/screen-flow.md - current same-application navigation and auth-flow context.

Tier 3 (Findings context):
- deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md - consolidated identity, privacy, session, navigation, and logout findings; pointer only, do not inspect protected output artifacts.

REQ/Context:
- deliverables/user/REQ-20260809-ATS-001.md - approved parent REQ, execution strategy, quality gates, and plan traceability.

Repro/Logs:
- No command, runtime, API, database, browser, provider, or external-action log is permitted for this WI. Record a no-action attestation in the Evidence Pack instead.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-060-summary.md:
- Decision-record summary of the four USER-approved rules, downstream blocks, no-change status, and any approval/escalation boundary.

Agent-facing -> deliverables/agent/WI-20260809-ATS-060-evidence-pack.md:
- Source pointers to the approved REQ and consolidated findings; verbatim decision transcription check; no-product-write/no-external-action/no-Git-mutation attestation; dependency and follow-up pointers.

Handoff Packet -> deliverables/agent/WI-20260809-ATS-060-handoff.md:
- This packet, created solely as the required pre-delegation handoff-generation step.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required. Use only the input pointers in this packet and an explicit no-action attestation; do not capture secrets, protected-output contents, or runtime data.
Tests: Not applicable. Do not run tests, builds, browser sessions, API calls, database queries, provider actions, or external-effect checks for this decision-only WI.
Rollback: No product state changes are allowed. If a decision record needs correction before implementation, supersede it through the approved REQ/WI process; do not alter runtime state.

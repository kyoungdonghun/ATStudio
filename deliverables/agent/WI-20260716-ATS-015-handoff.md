[WI HEADER]
WI ID: WI-20260716-ATS-015
REQ: REQ-20260716-ATS-002
Agent: qa-integ
Depends On: WI-20260716-ATS-013, WI-20260716-ATS-014
Blocks: WI-20260716-ATS-017

[WI SUMMARY]
Why: Perform the final independent three-way contract review across approved intent, backend/frontend implementation, and current design/operational/client documentation before any release-readiness fixes are applied.

Scope (in):
- Reconcile every material WI-005 through WI-014 behavior across controller/DTO/service/entity/schema, frontend API/types/routes/pages/state, and live docs/client checklists.
- Verify request/response shapes, enum/status transitions, route roles, query/path/body names, zero-amount billing-agreement re-registration, payment/settlement/refund/entitlement workflows, whitelist exports, company certification, auth/OAuth, downloads/licenses, playlists/catalog, and public full-track listening.
- Reproduce the published API/DB/router/page/modal/agent/SR/document counts and check that the counting unit is stable.
- Inspect WI-013 and WI-014 residual findings for cross-layer consequences and discover additional mismatches, stale documentation, missing error-state contracts, or UI/API behavior gaps.
- Review the current PDF/client material for technically accurate, non-misleading acceptance instructions.
- Produce severity-ranked findings and required WI-017 dispositions. Verification-only except summary/evidence.

Scope (out):
- Product/test/code/doc repair, live provider calls, DB migration/data mutation, secret/proxy/runtime changes, client-demo branch changes, stage/commit/push.
- Inventing social-only withdrawal or other unapproved policy.

DoD:
- A traceable three-way matrix covers all high-risk domains and every WI-013/WI-014 residual.
- Every mismatch has exact code/API/doc pointers, user impact, severity, and proposed WI-017 action.
- A clean area is explicitly marked verified rather than inferred from test success.
- Required summary and Evidence Pack are created.

Constraints/Forbidden:
- Work only in C:/Users/jm991/Desktop/project/ATStudio on codex/p1-acceptance-hardening.
- You are not alone. Preserve all current changes and remain verification-only except WI-015 deliverables.
- Do not modify/restart the frozen client-demo branch/runtime.
- Product invariants are fixed: public full-track listening; subscription/quota/license-gated official download; recurring billing-key card payment; single-server topology.
- Do not treat structural docs validation, unit tests, or code comments alone as semantic truth.
- No DB/provider/secret access, destructive operation, stage, commit, or push.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Backend and frontend contracts match for all high-risk endpoints/workflows.
- [ ] Role/route/UI availability matches security and controller behavior.
- [ ] Payment, whitelist, certification, auth/OAuth, download/license, catalog/playlist, and playback contracts have explicit three-way evidence.
- [ ] API/DB/UI/doc counts reproduce and use consistent units.
- [ ] WI-013/WI-014 findings are carried forward, refined, or rejected with evidence.

Performance:
- [ ] Review uses source/report inspection only and does not start application/runtime processes.

Quality:
- [ ] Findings lead, ordered P0 through P3, with exact file/line pointers.
- [ ] Documentation and client acceptance wording is checked for false certainty and missing environment boundaries.
- [ ] git diff --check and tsbuildinfo baseline remain unchanged.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2:
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/remaining-remediation-design-20260716.md
- docs/design/usecase/
- docs/ui/
- docs/payment/
- docs/client/
- docs/SR/
- docs/registry/

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-002.md
- deliverables/agent/WI-20260716-ATS-005-evidence-pack.md through WI-20260716-ATS-014-evidence-pack.md
- deliverables/user/WI-20260716-ATS-013-summary.md
- deliverables/user/WI-20260716-ATS-014-summary.md

Implementation Evidence:
- src/main/java/com/atstudio/atstudio/
- src/main/resources/application.yml
- src/main/resources/schema.sql
- src/main/resources/db/manual/
- src/test/java/com/atstudio/atstudio/
- frontend/src/
- frontend/package.json
- frontend/vite.config.ts
- build/reports/tests/test/
- build/reports/jacoco/test/
- frontend/coverage/

Repro/Logs:
- source-derived endpoint/entity/router/page/modal/count commands
- API/DTO/type field and enum comparisons
- role/route mapping comparisons
- git diff --check and tsbuildinfo SHA-256

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-015-summary.md:
- Plain-language three-way review outcome, findings, verified areas, and exact WI-017 actions.

Agent-facing -> deliverables/agent/WI-20260716-ATS-015-evidence-pack.md:
- Full contract matrix, count reproduction, findings with pointers/severity, environment limits, and no-change rollback statement.

Handoff Packet -> deliverables/agent/WI-20260716-ATS-015-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
- Every finding must include approved intent, implementation evidence, current-document evidence, and user impact.
- Distinguish historical records from live current-state documents.
- Do not claim real provider/retained-DB/browser/production proof from source inspection.
- Map each actionable finding to WI-017 and each environment/policy boundary to a separately gated follow-up.

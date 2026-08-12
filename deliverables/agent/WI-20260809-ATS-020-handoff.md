[WI HEADER]
WI ID: WI-20260809-ATS-020
REQ: REQ-20260809-ATS-001
Agent: qa-fe
Depends On: WI-20260809-ATS-019
Blocks: WI-20260809-ATS-021

[WI SUMMARY]
Why: Convert the verified active-surface inventory into an executable acceptance matrix before browser mutation begins, so every route, role, state, entry-point variant, viewport, API boundary, and external side effect has an explicit expected result and evidence requirement.
Scope (in/out): Build a master matrix for all 53 distinct page UIs, shared dialogs and player surfaces, contextual routes, background/admin operations, and complex cross-layer flows identified by WI-019. Map authorized and denied roles, primary reads and mutations, normal/empty/loading/validation/error/unknown states, desktop/mobile/keyboard checks, API and persistence evidence, interruption/retry scenarios, adjacent regression scope, and side-effect class. Do not execute browser mutations, change product code or current-state docs, mutate DB/runtime data, access secrets, or touch the intentional demo ZIP.
DoD: Every active route and non-navigation operation from WI-019 has a matrix row or an explicit parent/variant reference; no row lacks a role, state coverage, expected outcome, evidence source, side-effect class, and downstream browser WI assignment; high-risk state machines and same-behavior/different-entry-point families have dedicated scenario tables; open policy questions are separated from executable checks.
Constraints/Forbidden: Use baseline `e343c20` on `codex/v1-release-rehearsal-fixes`. React SPA is the only active UI. Preserve code freeze. Do not inspect ignored secret files or print environment values. Do not infer product policy from generic UX convention when project documents or code disagree. Do not perform real Toss charge/refund/cancel, external email, destructive DB work, branch operations, dependency changes, or runtime restarts.

[ACCEPTANCE CRITERIA]
Functional:

- [ ] Cover all 53 distinct page UIs and all 57 routable declarations, including three checkout callback paths and the admin index redirect.
- [ ] Cover shared Header, PlayerBar, queue/history/playlist dialogs, confirmation dialogs, toast/error surfaces, and every current modal occurrence by owning workflow.
- [ ] Assign anonymous, USER consumer/creator/business variants, subscribed/unsubscribed states, and ADMIN allow/deny expectations where applicable.
- [ ] Assign loading, normal, empty, validation, authorization, not-found, infrastructure failure, duplicate-submit, stale-response, interruption/reload, and unknown-outcome checks where applicable.
- [ ] Assign desktop, narrow/tablet, mobile, keyboard/focus, overflow, and fixed-player overlap checks to all visual families.
- [ ] Map expected frontend request, backend boundary, persistence/browser-local owner, reload proof, and external-side-effect class to each critical mutation.
- [ ] Give every same-behavior/different-entry-point family separate rows and one shared invariant.
- [ ] Give Subscription/Payment, admin payment operations/corrections, Whitelist, Company Certification, upload/media, CSV/export, and ordered membership dedicated state-machine scenarios.
- [ ] Assign every executable row to WI-021 through WI-030 and identify prerequisites/test accounts/data fixtures.
      Performance:

- [ ] Keep the matrix pointer-based and grouped so one browser WI can load only its bounded rows and source pointers.
- [ ] Avoid duplicate scenarios by using shared invariant IDs plus explicit entry-point variants.
      Quality:

- [ ] No inventory item remains unassigned or silently marked covered by another screen.
- [ ] Expected behavior is sourced from current documents/code; generic convention is labeled as an audit heuristic, not product policy.
- [ ] Side-effect classes distinguish read-only, reversible test mutation, local persistent mutation, external test-provider action, and separately approved real external action.
- [ ] Prettier, documentation validation, and whitespace checks pass for WI-020 deliverables.

[INPUT POINTERS]
Tier 0:

- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:

- docs/policies/quality-gates.md
- docs/policies/access-control-policy.md
- docs/policies/security-policy.md

Tier 2:

- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
- docs/ui/atstudio-front-list.md
- docs/ui/modal-list.md
- docs/ui/screen-flow.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/usecase/index.md
- docs/payment/index.md
- docs/client/\_internal-feature-map.md

REQ/Context:

- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-019-inventory.md
- deliverables/agent/WI-20260809-ATS-019-evidence-pack.md
- AGENTS.md

Primary code entry points:

- frontend/src/router/index.tsx
- frontend/src/pages/
- frontend/src/components/
- frontend/src/layouts/
- frontend/src/api/
- frontend/src/store/
- frontend/src/types/index.ts
- src/main/java/com/atstudio/atstudio/controller/
- src/main/java/com/atstudio/atstudio/service/
- src/main/java/com/atstudio/atstudio/entity/
- src/main/resources/schema.sql

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-020-summary.md:

- Matrix scope, coverage counts, role/data prerequisites, high-risk scenario families, side-effect boundaries, unresolved policy decisions, and readiness for browser WIs.

Agent-facing -> deliverables/agent/WI-20260809-ATS-020-evidence-pack.md:

- Source pointers, matrix coverage reconciliation, validation commands/results, explicit limits, rollback, and WI-021 trigger.

Matrix -> deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:

- Coverage taxonomy, global assertions, route/action rows, shared-surface rows, high-risk state machines, cross-entry invariants, role/data/viewport fixtures, evidence schema, side-effect classes, and WI assignments.

Handoff Packet -> deliverables/agent/WI-20260809-ATS-020-handoff.md:

- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required for each scenario group and each critical expected behavior; include document contract and current code owner where available.
Tests: This is a planning/audit WI. Validate route/inventory reconciliation, table completeness, Prettier, repository documentation integrity, and whitespace. Do not execute product mutations.
Rollback: Removing the WI-020 handoff, matrix, evidence pack, and summary reverts this documentation-only work. No runtime or product rollback may be necessary.

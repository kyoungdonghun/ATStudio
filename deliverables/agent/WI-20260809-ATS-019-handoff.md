[WI HEADER]
WI ID: WI-20260809-ATS-019
REQ: REQ-20260809-ATS-001
Agent: qa-integ
Depends On: -
Blocks: WI-20260809-ATS-020

[WI SUMMARY]
Why: Establish a complete, evidence-backed inventory before any browser acceptance pass or product-code modification, so active routes, actions, APIs, roles, data states, background operations, and documentation contracts cannot silently fall outside the audit.
Scope (in/out): Read-only repository and current-runtime reconnaissance. Inventory active React routes, route guards, menus, modals, frontend API wrappers, backend controllers, scheduled/operational surfaces, relevant schema entities, and current design/UI/API documentation. Produce a document-to-screen-to-action-to-API-to-data traceability baseline and a gap list. Do not execute browser mutations or change product code, schema, runtime data, current-state docs, branches, secrets, or the intentional ZIP.
DoD: Every discovered active route and externally reachable backend operation is assigned to a feature group, roles/guards and user actions are identified where determinable, frontend/backend contracts are linked, documentation coverage is noted, duplicate/dead/uncertain surfaces are explicitly classified, and WI-020 receives a bounded inventory from which a master acceptance matrix can be generated.
Constraints/Forbidden: Use baseline `e343c20`. Treat React SPA as the only active UI. Do not read or print ignored secret files or environment values. Do not infer a route or API as active solely because a historical document names it. Do not modify product code or existing documentation. Do not stage, commit, push, stop the acceptance runtime, mutate DB data, or touch `output/client-demo-screenshots-20260716-140514.zip`.

[ACCEPTANCE CRITERIA]
Functional:

- [ ] Enumerate every active React route, guard, layout, major modal/drawer, and navigation entry from current code.
- [ ] Enumerate frontend API wrappers and backend controller endpoints, including scheduled/admin/file-export surfaces that are not reachable from primary navigation.
- [ ] Connect each active feature group to roles, primary actions, API contracts, persistence/state owner, and current documentation pointers where evidence exists.
- [ ] Identify same-behavior/different-entry-point families such as Track playback from list, detail, Album, Playlist, Likes, History, and queue.
- [ ] Identify high-risk state-machine families such as Subscription/Payment, Company Certification, Whitelist, admin corrections/refunds/reconciliation, upload processing, and CSV export.
- [ ] Record documentation-only, code-only, uncertain, duplicate, and apparently unreachable items without deleting or changing them.
- [ ] Provide a complete input contract for WI-020, including counts and reproducible discovery commands.
      Performance:
- [ ] Use targeted `rg`, route/API parsing, and repository indexes rather than loading the entire repository content into one output.
      Quality:
- [ ] Every inventory row has at least one concrete code or document pointer.
- [ ] Unknowns and assumptions are labeled; no unsupported current-state claim is presented as fact.
- [ ] `git diff --check` passes for the three WI-019 deliverables.

[INPUT POINTERS]
Tier 0:

- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:

- docs/policies/quality-gates.md
- docs/policies/security-policy.md

Tier 2:

- docs/index.md
- docs/design/index.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/usecase/index.md
- docs/ui/index.md
- docs/ui/atstudio-front-list.md
- docs/ui/modal-list.md
- docs/ui/screen-flow.md
- docs/payment/index.md
- docs/client/\_internal-feature-map.md

REQ/Context:

- deliverables/user/REQ-20260809-ATS-001.md
- AGENTS.md

Primary code entry points:

- frontend/src/router/
- frontend/src/api/
- frontend/src/layouts/
- frontend/src/pages/
- frontend/src/components/
- frontend/src/store/
- src/main/java/com/atstudio/atstudio/controller/
- src/main/java/com/atstudio/atstudio/service/
- src/main/java/com/atstudio/atstudio/entity/
- src/main/resources/schema.sql
- scripts/acceptance/

Baseline evidence:

- `git rev-parse HEAD`
- `git status --short --branch`
- Current expected baseline: `e343c20` on `codex/v1-release-rehearsal-fixes`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-019-summary.md:

- Scope and baseline, inventory counts, high-risk feature families, material doc/code gaps, unknowns, and WI-020 readiness.
  Agent-facing -> deliverables/agent/WI-20260809-ATS-019-evidence-pack.md:
- Reproducible commands, file/line pointers, count methodology, coverage limits, rollback statement, and next-WI inputs.
  Inventory -> deliverables/agent/WI-20260809-ATS-019-inventory.md:
- Structured tables for routes/screens/actions/roles, frontend APIs, backend endpoints, state/data owners, operational surfaces, documentation coverage, cross-entry-point families, and unresolved classifications.
  Handoff Packet -> deliverables/agent/WI-20260809-ATS-019-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required for every inventory section; use current file paths and symbols, with line references where stable.
Tests: This is a read-only audit WI; record inventory validation commands and `git diff --check` instead of executing full product test suites.
Rollback: Deleting the three WI-019 deliverables reverts this documentation-only audit output; no runtime or product behavior may change.

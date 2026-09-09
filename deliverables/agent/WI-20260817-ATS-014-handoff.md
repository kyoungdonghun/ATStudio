[WI HEADER]
WI ID: WI-20260817-ATS-014
REQ: REQ-20260817-ATS-009
Agent: se
Depends On: WI-20260817-ATS-013
Blocks: WI-20260817-ATS-015, WI-20260817-ATS-016

[WI SUMMARY]
Why: Remove the current production dependency audit findings for React Router through a controlled V6-to-V7 update while preserving AT.M's React SPA routing, protected-route, loader, and navigation behavior.

Scope (in/out):
- In: `frontend/package.json`, lockfile, and only the frontend router imports/configuration/tests required by React Router 7.18.2.
- Out: React/ReactDOM major upgrade, Vite migration, React Router framework-mode conversion, API/backend changes, DB/config/secret changes, feature/UI redesign, documentation changes.

DoD:
- Production dependencies resolve `react-router` and `react-router-dom` at 7.18.2 or a later audited secure V7 patch selected from the npm registry at execution time.
- All imports and current data-router behavior compile under V7; no obsolete future prop or removed API remains in active code.
- Current public, authentication, subscriber, administrator, lazy-route, and pending-navigation test coverage passes.
- `npm audit --omit=dev` has no remaining React Router production vulnerabilities.

Constraints/Forbidden:
- Keep the existing Vite React SPA and current URL/API contracts.
- Do not convert the app to framework mode or rewrite all imports merely for V8 preparation.
- Preserve all already-approved test-only fixes from WI-013.
- Before selecting any version, verify its peer requirements; execution host has Node 24.14.0 and React 18.3.1, and React Router 7.18.2 requires Node >=20 and React/ReactDOM >=18.
- Do not alter production credentials, external callback URLs, or output artifacts.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Browser router initialization and `RouterProvider` are valid under the selected V7 version.
- [ ] Existing route guards, redirects, query navigation, lazy routes, and `useBlocker` behavior retain their tested contracts.
- [ ] No React Router V6 deprecation warning is emitted by the project test suite because of active configuration.
Quality:
- [ ] `npm install` completes with an intentional lockfile change only.
- [ ] `npm audit --omit=dev` confirms the targeted findings are removed.
- [ ] `npm run typecheck`, `npm run lint`, `npm run format`, `npm test -- --run`, and `npm run build` pass.
- [ ] `git diff --check` passes.

[INPUT POINTERS]
Tier 0 (Constitution - Required):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2 (Frontend):
- .agents/skills/react-best-practices/AGENTS.md
- docs/design/p1-security-acceptance-hardening-design.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-009.md
- deliverables/agent/WI-20260817-ATS-013-evidence-pack.md
- docs/SR/SR-93.md

Files:
- frontend/package.json
- frontend/package-lock.json
- frontend/src/App.tsx
- frontend/src/router/index.tsx
- frontend/src/router/LazyRoute.test.tsx
- frontend/src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx
- frontend/src/test/coverage/publicAuthShell.coverage.test.tsx
- frontend/src/hooks/usePendingMutationGuard.ts

External primary references:
- https://reactrouter.com/de/main/start/changelog (V7 minimum versions, package restructuring, future-flag behavior)
- https://api.reactrouter.com/v7/modules/react-router-dom.html (V7 compatibility re-export)

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-014-summary.md:
- Selected version, behavioral compatibility outcome, audit result, risks.
Agent-facing -> deliverables/agent/WI-20260817-ATS-014-evidence-pack.md:
- Exact dependency diff, source compatibility changes, commands/results, rollback.
Handoff Packet -> deliverables/agent/WI-20260817-ATS-014-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Record audit before/after and each frontend quality gate.
Rollback: Restore package.json/package-lock and any narrow V7 compatibility edits as one atomic commit; no DB/data rollback.

[WI HEADER]
WI ID: WI-20260716-ATS-019
REQ: REQ-20260716-ATS-002
Agent: se
Depends On: WI-20260716-ATS-018
Blocks: Final development-branch release-readiness verification

[WI SUMMARY]
Why: Close the remaining customer-visible legacy service-name examples while preserving the stable ATStudio repository and runtime identifiers.
Scope (in):
- Treat `AT.M` as the customer-facing service display name.
- Replace remaining user-visible `ATStudio`/`atstudio` examples in active UI and acceptance seed display data.
- Use a neutral YouTube example handle/link instead of inventing an official AT.M handle.
- Document the boundary between the `AT.M` display brand and the internal `ATStudio` project identifier in current documentation.
- Add or update focused tests for the affected UI/bootstrap contract where useful.
Scope (out):
- Package names, Java main class, database/schema name, repository paths, filenames, route paths, API URLs, callback URLs, environment-variable names, internal headers, mail domains, historical WI/REQ evidence, or Git branch names.
- Product behavior, data migrations, runtime configuration, external provider calls, or client-demo propagation.
DoD:
- Active customer-facing UI contains no stale ATStudio service-name label or `@atstudio` example.
- Acceptance seed company display name uses AT.M without changing account identifiers.
- Current docs clearly state that AT.M is the display brand and ATStudio remains the internal project identifier.
- Focused frontend/backend tests and formatting/diff checks pass.
- User summary and Evidence Pack are created.
Constraints/Forbidden:
- Work only in `C:/Users/jm991/Desktop/project/ATStudio` on `codex/p1-acceptance-hardening`.
- Do not modify `C:/Users/jm991/Desktop/project/ATStudio-client-demo-stable` or its runtime.
- Do not stage, commit, push, delete files, mutate DB/provider state, restart servers, or change URLs/redirects/callback behavior.
- Do not perform broad mechanical replacement of `ATStudio`/`atstudio`.
- Preserve `frontend/tsconfig.tsbuildinfo` exact bytes and do not revert unrelated changes.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Whitelist validation and placeholders use a neutral YouTube handle/link example.
- [ ] Acceptance business seed display name is `AT.M QA Biz` while acceptance email/account identifiers remain unchanged.
- [ ] Active frontend title/header/admin display remains `AT.M`.
- [ ] No route, API, callback, database, package, or deployment identifier is renamed.
Performance:
- [ ] No runtime or bundle-size behavior is materially changed.
Quality:
- [ ] Focused affected tests pass.
- [ ] Frontend typecheck/lint/Prettier checks for changed files pass.
- [ ] Documentation validation and `git diff --check` pass for the WI scope.
- [ ] Client worktree HEAD/status remain unchanged.

[INPUT POINTERS]
Tier 0 (Constitution and standards):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 (Policies):
- docs/policies/quality-gates.md

Tier 2 (Current UI and project records):
- docs/client/0-site-policy.md
- docs/ui/index.md
- docs/registry/project-registry.md

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-002.md
- deliverables/agent/WI-20260716-ATS-018-evidence-pack.md

Files:
- frontend/index.html
- frontend/src/layouts/Header.tsx
- frontend/src/layouts/AdminLayout.tsx
- frontend/src/pages/subscriber/WhitelistChannelPage.tsx
- frontend/src/pages/subscriber/WhitelistChannelPage.test.tsx
- src/main/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunner.java
- src/test/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunnerTest.java
- docs/standards/glossary.md
- docs/registry/project-registry.md

Repro/Inspection:
- `rg -n --glob '!*.test.*' 'ATStudio|ATstudio|atstudio' frontend/src frontend/index.html`
- `rg -n 'ATStudio QA Biz|AT.M QA Biz' src/main/java src/test/java`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-019-summary.md:
- Display-name changes, preserved technical identifiers, test results, and residual boundary.
Agent-facing -> deliverables/agent/WI-20260716-ATS-019-evidence-pack.md:
- Exact file pointers, commands/results, no-propagation evidence, rollback, and follow-up status.
Handoff Packet -> deliverables/agent/WI-20260716-ATS-019-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Record exact focused commands and results.
Rollback: Revert only the WI-019 display strings/tests/docs as a coherent group.

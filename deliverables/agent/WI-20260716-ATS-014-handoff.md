[WI HEADER]
WI ID: WI-20260716-ATS-014
REQ: REQ-20260716-ATS-002
Agent: qa-fe
Depends On: WI-20260716-ATS-010, WI-20260716-ATS-011, WI-20260716-ATS-012
Blocks: WI-20260716-ATS-015, WI-20260716-ATS-016

[WI SUMMARY]
Why: Independently verify the final React/Vite remediation set across dependency security, formatting, types, lint, tests, coverage, build output, route policy, asynchronous state, and accessibility before cross-layer review.

Scope (in):
- Review all frontend production, test, package, Vite, ESLint, Prettier, and TypeScript changes accumulated by WI-010 through WI-012.
- Run unfiltered and production-only npm audits, TypeScript typecheck, ESLint, full Vitest, V8 coverage, production build, and full-tree Prettier check.
- Confirm exact resolved versions and that no override/force/major-upgrade workaround was introduced.
- Inspect route aliases, protected/subscriber/admin access, safe redirects, loading/error/empty states, stale-request defenses, modal focus restoration, keyboard behavior, toast/pagination semantics, and player/full-track policy regressions.
- Check that public full-track playback remains unrestricted while downloads retain subscription/quota/license gates.
- Review coverage by risk area without inventing a release threshold and identify precise frontend residual findings for WI-017.
- Preserve the pre-existing tsbuildinfo file byte-for-byte after all commands.
- Create the required user summary and evidence pack. Do not repair application code in this WI.

Scope (out):
- Backend regression, live provider/payment execution, retained DB, client-demo branch modifications, or production deployment claims.
- Stage, commit, push, destructive operations, or propagation to codex/client-demo-stable.

DoD:
- All required frontend audit/quality/test/coverage/build/format commands run against the current development worktree.
- Exact audit totals, resolved versions, test totals, coverage metrics, build result, formatting result, and tsbuildinfo before/after hashes are recorded.
- Route/state/a11y/product-policy review findings are severity-ranked with file/test evidence.
- User summary and Evidence Pack are created.

Constraints/Forbidden:
- Work only in C:/Users/jm991/Desktop/project/ATStudio on codex/p1-acceptance-hardening.
- You are not alone in the codebase. Do not revert, auto-fix, reformat, or overwrite existing changes; verification-only except required deliverables.
- Do not modify or restart the frozen client-demo worktree/runtime.
- No npm audit fix, package update, DB/provider call, secret access, stage, commit, or push.
- Record the SHA-256 of frontend/tsconfig.tsbuildinfo before commands and restore/check the exact original bytes if a tool rewrites it.
- Coverage is observational; no invented threshold.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Production and unfiltered dependency audits are reproduced.
- [ ] Typecheck, ESLint, full Vitest, V8 coverage, production build, and full-tree Prettier check complete.
- [ ] Public full-track listening and gated-download policy show no frontend regression.
- [ ] Route guards, safe return paths, stale-request defenses, and major accessibility contracts are reviewed against current tests/code.

Performance:
- [ ] Build output and unusually large chunks or command resource failures are recorded without unrelated optimization.

Quality:
- [ ] Exact test totals and V8 statements/branches/functions/lines are recorded.
- [ ] Declared and resolved dependency versions are recorded; no force/override workaround is hidden.
- [ ] tsconfig.tsbuildinfo before/after SHA-256 is identical.
- [ ] git diff --check remains clean apart from non-failing line-ending warnings.

[INPUT POINTERS]
Tier 0 (Constitution and development standards):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies):
- docs/policies/quality-gates.md
- docs/policies/security-policy.md

Tier 2 (React guidance and current UI contracts):
- .agents/skills/react-best-practices/AGENTS.md
- docs/ui/
- docs/design/api-spec.md
- docs/client/

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-002.md
- docs/design/remaining-remediation-design-20260716.md
- deliverables/agent/WI-20260716-ATS-010-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-011-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-012-evidence-pack.md

Implementation Evidence:
- frontend/package.json
- frontend/package-lock.json
- frontend/vite.config.ts
- frontend/tsconfig.json
- frontend/.eslintrc.cjs
- frontend/.prettierrc
- frontend/src/
- frontend/coverage/
- frontend/dist/

Repro/Logs:
- npm audit --omit=dev --json
- npm audit --json
- npm run typecheck
- npm run lint
- npm test -- --run
- npm run test:coverage
- npm run build
- npx prettier --check . --ignore-unknown
- SHA-256 checks and git diff --check

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-014-summary.md:
- Plain-language frontend quality result, exact audit/test/coverage/build outcomes, residual risks, and whether cross-layer review may proceed.

Agent-facing -> deliverables/agent/WI-20260716-ATS-014-evidence-pack.md:
- Commands, exact metrics, route/state/a11y/product-policy review matrix, findings with severity/evidence, tsbuildinfo hash proof, environment limits, and rollback/no-change statement.

Handoff Packet -> deliverables/agent/WI-20260716-ATS-014-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
- Distinguish development-branch evidence from the frozen client-demo branch state.
- Cite exact command output/report paths for audit, tests, coverage, build, and formatting.
- Record every residual finding with file/test evidence and a proposed WI-017 disposition.
- Do not claim real browser/provider/production proof from jsdom or Vite build success.
- Rollback must state that this WI is verification-only except its summary/evidence deliverables.

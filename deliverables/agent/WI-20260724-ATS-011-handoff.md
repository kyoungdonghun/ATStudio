[WI HEADER]
WI ID: WI-20260724-ATS-011
REQ: REQ-20260724-ATS-002
Agent: qa-fe
Depends On: WI-20260724-ATS-010
Blocks: WI-20260724-ATS-014

[WI SUMMARY]
Why: Prove the pushed frontend from a lockfile-based clean install.
Scope (in/out): In the fresh clone only, run `npm ci`, full Vitest coverage, typecheck, ESLint, Prettier, and production build. No UI behavior change.
DoD: All frontend gates pass from the clean clone and resolved dependency/build evidence is recorded.
Constraints/Forbidden: Do not use the original repo `node_modules`; do not modify source, commit, push, or include secrets.

[ACCEPTANCE CRITERIA]
- [ ] `npm ci` succeeds from `package-lock.json`.
- [ ] Tests and configured coverage thresholds pass.
- [ ] typecheck, ESLint, Prettier, and build pass.
- [ ] Build warnings and clone status are recorded.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
Tier 2:
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
REQ/Context:
- deliverables/user/REQ-20260724-ATS-002.md
- deliverables/agent/WI-20260724-ATS-010-evidence-pack.md
Files:
- frontend/package.json
- frontend/package-lock.json

[OUTPUT CONTRACT]
User-facing -> `deliverables/user/WI-20260724-ATS-011-summary.md`
Agent-facing -> `deliverables/agent/WI-20260724-ATS-011-evidence-pack.md`

[TRACEABILITY REQUIREMENTS]
Record exact commands, resolved versions, test/coverage counts, build output summary, warnings, and clone diff.

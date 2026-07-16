[WI HEADER]
WI ID: WI-20260716-ATS-011
REQ: REQ-20260716-ATS-002
Agent: se
Depends On: WI-20260716-ATS-005, WI-20260716-ATS-006, WI-20260716-ATS-010
Blocks: WI-20260716-ATS-012, WI-20260716-ATS-013, WI-20260716-ATS-014

[WI SUMMARY]
Why: Restore reproducible full-tree frontend formatting verification and introduce measurable backend/frontend coverage reporting so the remaining remediation can be evaluated with evidence instead of assumptions.

Scope (in):
- Audit frontend production dependencies and remediate compatible known vulnerabilities, including direct and transitive React Router and Axios paths where applicable.
- Re-run focused navigation, redirect, HTTP cancellation/error, and existing frontend regression tests after dependency updates; document any vulnerability that cannot be removed without an incompatible major change.
- Add Gradle JaCoCo reporting for the backend with HTML and XML output.
- Add Vitest V8 coverage reporting for the frontend with a reproducible npm script and committed dependency lock changes.
- Record coverage as an observational baseline. Do not add or infer arbitrary coverage thresholds.
- Inventory the current full frontend Prettier failures, then mechanically format the supported frontend source/config/test surface until the repository-defined full check passes.
- Make generated coverage/build artifacts explicitly untracked where existing ignore rules are insufficient.
- Keep quality commands and documentation consistent with the implemented configuration.
- Create the required user summary and agent evidence pack, including exact commands, baseline metrics, changed-file inventory, risks, and rollback.

Scope (out):
- Product behavior, authorization, payment, playback, download, whitelist, company-certification, or database behavior changes.
- Tests added only to inflate a percentage, minimum coverage thresholds, or release claims based on coverage alone.
- Broad semantic refactors mixed into mechanical Prettier changes.
- Unreviewed `npm audit fix --force`, framework major upgrades, or lockfile churn unrelated to a documented vulnerability/coverage dependency.
- Generated coverage reports, runtime logs, PID files, build outputs, or `frontend/tsconfig.tsbuildinfo` in deliverables.
- Any change to `codex/client-demo-stable`, its worktree, public Cloudflare runtime, or client demo processes.

DoD:
- Production dependency audit results are recorded before and after remediation; compatible fixes are applied and every residual advisory has impact and deferral rationale.
- Backend tests can generate JaCoCo HTML/XML reports with one documented Gradle command.
- Frontend tests can generate V8 coverage reports with one documented npm command.
- Actual backend/frontend baseline metrics are extracted and recorded without enforcing a threshold.
- The repository-defined full frontend Prettier check passes; all resulting formatting-only changes are identified separately from configuration changes.
- Existing frontend typecheck, ESLint, Vitest, and build still pass after dependency/configuration changes.
- Generated reports remain ignored and `git status` contains no coverage artifacts.
- Documentation validation and `git diff --check` pass.
- User-facing summary and agent-facing evidence pack are written.

Constraints/Forbidden:
- Work only in `C:/Users/jm991/Desktop/project/ATStudio` on `codex/p1-acceptance-hardening`.
- Do not edit or restart `C:/Users/jm991/Desktop/project/ATStudio-client-demo-stable` or the public demo runtime.
- The worktree contains changes from prior WIs. You are not alone in the codebase: do not revert, overwrite, stage, or commit other agents' or user changes.
- Preserve the product invariants: full public track listening, gated downloads, recurring card billing, and single-server deployment.
- Use versions compatible with the repository's current Gradle, Java, Vite, and Vitest versions. Update lockfiles reproducibly.
- Do not force the 80/70/80/80 values mentioned by the generic coverage skill; the approved REQ explicitly requires baseline measurement with no arbitrary threshold.
- Do not use coverage exclusions to make the result look better. Only exclude conventional non-runtime/bootstrap/type/declaration/test/config artifacts and document every exclusion.
- Do not modify or add real secrets, retained DB data, schema DDL, or external services.
- Preserve the pre-existing `frontend/tsconfig.tsbuildinfo` state; do not stage or intentionally rewrite it.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Frontend production dependency audit has no compatible unaddressed high/critical advisory; any residual advisory is identified with dependency path, runtime impact, and deferral rationale.
- [ ] Existing navigation/redirect and Axios request cancellation/error tests pass after dependency changes.
- [ ] `gradlew.bat test jacocoTestReport` succeeds and creates backend HTML/XML coverage reports.
- [ ] A committed frontend script such as `npm run test:coverage` succeeds and creates Vitest V8 coverage output.
- [ ] Backend and frontend baseline line/branch/function or method/instruction metrics are recorded from generated machine-readable reports.
- [ ] Generated coverage artifacts are ignored and absent from tracked/untracked deliverables.
- [ ] The full repository-defined frontend Prettier check succeeds after an initial failure inventory and mechanical formatting pass.

Performance:
- [ ] Coverage collection does not alter application runtime behavior or production bundles.
- [ ] No long-running watch mode is introduced into verification scripts.

Quality:
- [ ] `gradlew.bat test jacocoTestReport` passes.
- [ ] `npm run typecheck` passes without intentionally changing `frontend/tsconfig.tsbuildinfo`.
- [ ] `npm run lint` passes.
- [ ] `npm test -- --run` (or the equivalent existing non-watch command) passes.
- [ ] `npm run test:coverage` passes.
- [ ] `npm run build` passes.
- [ ] The full frontend Prettier check passes.
- [ ] `python .agents/skills/validate-docs/scripts/validate_docs.py` passes.
- [ ] `git diff --check` passes apart from non-failing line-ending warnings already present in the worktree.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Quality and execution):
- docs/policies/quality-gates.md
- docs/policies/execution-policy.md
- docs/standards/evidence-pack-standard.md

Tier 2 (Frontend and implementation context):
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
- .agents/skills/test-coverage/SKILL.md
- .agents/skills/prettier/SKILL.md
- docs/design/remaining-remediation-design-20260716.md

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-002.md
- deliverables/agent/WI-20260716-ATS-010-evidence-pack.md
- deliverables/user/WI-20260716-ATS-010-summary.md

Files:
- build.gradle
- settings.gradle
- .gitignore
- frontend/package.json
- frontend/package-lock.json
- frontend/vite.config.ts
- frontend/.gitignore
- frontend/src/**

Repro/Logs:
- `npm audit --omit=dev` and a machine-readable audit result before/after dependency remediation
- focused React Router navigation/redirect and Axios request/cancellation/error test commands
- `gradlew.bat test jacocoTestReport`
- `npm run test:coverage` from `frontend/`
- repository-defined frontend Prettier check from `frontend/`
- `npm run typecheck`, `npm run lint`, `npm test -- --run`, `npm run build` from `frontend/`
- `python .agents/skills/validate-docs/scripts/validate_docs.py`
- `git diff --check`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-011-summary.md:
- Plain-language summary of tooling changes, measured baselines, risks, and follow-up work.

Agent-facing -> deliverables/agent/WI-20260716-ATS-011-evidence-pack.md:
- Exact file pointers, dependency/config changes, initial and final Prettier evidence, coverage report pointers and metrics, all verification commands/results, rollback, and follow-up WI.

Handoff Packet -> deliverables/agent/WI-20260716-ATS-011-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
- Evidence pointers: Required for every configuration, dependency, script, ignore, and documentation change.
- Dependencies: Record direct/transitive versions, advisory paths, before/after audit results, compatibility judgment, and regression evidence. Never claim `npm audit` dev-only findings are production runtime vulnerabilities.
- Tests: Record command, exit status, test count where available, and report output path.
- Coverage: Record raw report path and exact baseline values; distinguish measurement from acceptance thresholds.
- Prettier: Record the initial failure count/list, the final full-tree command, and PASS result. Separate formatting-only files from semantic/configuration edits.
- Rollback: Document how to remove JaCoCo/Vitest coverage configuration and revert only WI-011 formatting/config changes without touching earlier WIs.
- Branch isolation: Record dev branch name and verify the client branch/worktree was not touched.

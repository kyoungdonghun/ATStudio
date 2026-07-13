# Evidence Pack: WI-20260713-ATS-015

## Summary (one-liner)

- Verified successful backend and frontend production builds, then removed the only tracked generated-file change produced by the build.

## Scope / DoD Check

- DoD items:
  - [x] `.\gradlew.bat build` exited with code 0.
  - [x] `npm run build` exited with code 0.
  - [x] No generated tracked file remains modified.
  - [x] No deployment or live external-system call was made.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | System constitution and evidence traceability |
| 0 | `docs/standards/development-standards.md` | Backend/frontend build and QA standards |
| 0 | `docs/standards/documentation-standards.md` | Required deliverable structure and documentation quality |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/quality-gates.md` | Release quality-gate criteria |
| 2 | `docs/standards/frontend-standards.md` | React/TypeScript/Vite build context |

**REQ / Context**:

| Document | Reason |
|----------|--------|
| `deliverables/user/REQ-20260713-ATS-001.md` | Approved P0 remediation scope and G6 quality gate |
| `deliverables/agent/WI-20260713-ATS-015-handoff.md` | WI scope, acceptance criteria, constraints, and output contract |
| `.agents/skills/build-check/SKILL.md` | Build verification workflow and reporting contract |
| `.agents/skills/create-wi-evidence-pack/SKILL.md` | Evidence Pack structure and reproducibility requirements |

**Injection Rules Applied**:

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `qa`
- Task type: production build verification
- Loaded tiers: Tier 0, Tier 1, Tier 2, and approved REQ context

## Evidence Pointers (required)

- Files changed by this WI:
  - `deliverables/user/WI-20260713-ATS-015-summary.md` - user-facing build result.
  - `deliverables/agent/WI-20260713-ATS-015-evidence-pack.md` - reproducible QA evidence.
- Product, test, current-state documentation, and other WI files changed by this WI: none.
- Generated-file evidence:
  - Path: `frontend/tsconfig.tsbuildinfo`
  - Tracked by Git: yes.
  - Pre-build SHA-256: `432D8E84A411B0C34B1DA11800B68EDC2F1A8FEDF925D4D6290F544785E40F90`
  - Post-build SHA-256: `66047ECF66C2A3B54C29C83E7BAFDF46E044E2F529075D556C122437E0EDEBB7`
  - Cleanup command: `git restore -- frontend/tsconfig.tsbuildinfo`
  - Restore exit: 0.
  - Restored SHA-256: `432D8E84A411B0C34B1DA11800B68EDC2F1A8FEDF925D4D6290F544785E40F90`
  - Final scoped Git status for the file: clean.

## Commands & Outputs

Commands were run from `C:\Users\jm991\Desktop\project\ATStudio` unless another directory is stated.

| Command | Working directory | Exit | Duration | Result |
|---------|-------------------|------|----------|--------|
| `.\gradlew.bat build` | repository root | 0 | 2.840 s | `BUILD SUCCESSFUL`; 8 tasks up-to-date |
| `npm run build` | `frontend/` | 0 | 7.966 s | `tsc -b` and Vite production build succeeded; 259 modules transformed |
| `git restore -- frontend/tsconfig.tsbuildinfo` | repository root | 0 | not separately timed | Restored the generated tracked metadata only |
| `git diff --check` | repository root | 0 | not separately timed | No whitespace error; unrelated LF-to-CRLF notices only |
| Owned-file trailing-whitespace scan | repository root | 0 | not separately timed | Zero affected lines in both WI-015 deliverables |

Notable build output:

- Gradle: compile, test, check, JAR, and Boot JAR stages completed successfully.
- Vite: production bundle completed in 2.28 s with no warning or error output.
- Build outputs under `build/` and `frontend/dist/` did not appear as tracked Git changes.

## Tests

- Gradle `build` included the configured test/check lifecycle and completed successfully.
- Frontend `npm run build` included TypeScript project compilation through `tsc -b` before Vite bundling.
- Dedicated test counts and static-quality evidence are owned by WI-013 and WI-014 respectively.
- No live Toss, SMTP, external database, deployment, or other network-dependent validation was performed.

## Risks / Rollback

- Risks:
  - Successful local production builds do not prove deployment configuration or live provider behavior.
  - Gradle tasks were up-to-date in this invocation; WI-013 separately records the forced full backend test execution.
- Rollback:
  - No product rollback is applicable because this WI made no product changes.
  - Remove only the two WI-015 deliverable files if this verification record must be regenerated.

## Follow-ups

- WI-016: verify documentation integrity.
- WI-017: consume WI-013 through WI-016 evidence for the final P0 closure decision.

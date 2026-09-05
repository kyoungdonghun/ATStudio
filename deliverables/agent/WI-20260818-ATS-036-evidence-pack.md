---
version: 1.0
last_updated: 2026-08-18
project: ATS
owner: se
category: evidence-pack
status: draft
related_wi: WI-20260818-ATS-036
---

# Evidence Pack: WI-20260818-ATS-036

## Summary

- Replaced the three active HomePage audience strings with the approved Korean `창작자` copy and added focused positive and negative assertions.

## Scope / DoD Check

- [x] Hero title renders `창작자를 위한` before `최고의 음악`.
- [x] Hero subtitle renders `창작자를 위한 고품질 라이선스 음악.`.
- [x] Footer renders `창작자를 위한` before `음악 라이선스 플랫폼`.
- [x] TrackDetail's distinct `쇼츠, 브이로그` phrase remains unchanged.
- [x] Inactive HTML mockups, layout, styles, routes, APIs, and backend remain unchanged.
- [x] TypeScript typecheck, ESLint, changed-code Prettier, and `git diff --check` pass.
- [ ] Focused HomePage Vitest execution is environment-blocked before test collection by `spawn EPERM` from Vite child-process startup.

## Reference Documents

| Tier    | Document                                            | Reason                                                 |
| ------- | --------------------------------------------------- | ------------------------------------------------------ |
| 0       | `docs/standards/core-principles.md`                 | Constitution and approved-WI execution boundary        |
| 0       | `docs/standards/development-standards.md`           | Implementation, testing, and two-set deliverable rules |
| 1       | `docs/policies/quality-gates.md`                    | LOW-impact quality-gate checklist                      |
| 1       | `docs/standards/evidence-pack-standard.md`          | Evidence structure and reproducibility requirements    |
| 2       | `.agents/skills/react-best-practices/AGENTS.md`     | React performance impact review                        |
| 2       | `docs/standards/frontend-standards.md`              | Active React SPA and test conventions                  |
| Context | `deliverables/user/REQ-20260818-ATS-002.md`         | Approved copy-change scope                             |
| Context | `deliverables/agent/WI-20260818-ATS-036-handoff.md` | Exact DoD, constraints, and output contract            |

## Evidence Pointers

- `frontend/src/pages/public/HomePage.tsx:298-304`: approved hero title and subtitle copy.
- `frontend/src/pages/public/HomePage.tsx:508-514`: approved footer audience copy.
- `frontend/src/pages/public/HomePage.test.tsx:68-81`: focused positive and legacy-copy absence assertions.
- `frontend/src/pages/public/TrackDetailPage.tsx:300`: semantically distinct `쇼츠, 브이로그` phrase preserved.
- `docs/ui/mockup/main.html:237-238,316`: inactive mockup text preserved.

## Commands & Results

| Command                                                                                                                 | Result                                                                                                  |
| ----------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| `npm test -- src/pages/public/HomePage.test.tsx`                                                                        | BLOCKED before config load/test collection: `Error: spawn EPERM` while Vite attempted to start esbuild. |
| `npx vitest run src/pages/public/HomePage.test.tsx --configLoader runner`                                               | BLOCKED before test collection: `Error: spawn EPERM` while Vite attempted a Windows child process.      |
| `npm run typecheck`                                                                                                     | PASS, exit code 0.                                                                                      |
| `npm run lint`                                                                                                          | PASS, exit code 0 with zero warnings allowed.                                                           |
| `npx prettier --check "src/pages/public/HomePage.tsx" "src/pages/public/HomePage.test.tsx"`                             | PASS; both files match Prettier style.                                                                  |
| `git diff --check`                                                                                                      | PASS, exit code 0.                                                                                      |
| `git diff --name-only`                                                                                                  | Only `HomePage.tsx` and `HomePage.test.tsx` are tracked modifications.                                  |
| `rg -n "쇼츠\|크리에이터\|창작자" frontend/src/pages/public/HomePage.tsx frontend/src/pages/public/TrackDetailPage.tsx` | Confirms three active HomePage replacements and preserved TrackDetail wording.                          |

## Risks / Rollback

- Risk: The focused test has not executed in this restricted environment; static checks and exact diff inspection pass, but an unrestricted runner must close the test-execution gap.
- Performance: Text-only replacements add no render work, request, dependency, state, or bundle behavior.
- Rollback: Restore the three original HomePage strings and remove or restore the corresponding focused assertions only.

## Follow-ups

- Re-run `npm test -- src/pages/public/HomePage.test.tsx` in a Windows environment that permits Vite/esbuild child-process startup.

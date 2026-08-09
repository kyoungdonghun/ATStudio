# Evidence Pack: WI-20260809-ATS-014

## Summary

- Aligned the Playlist editor reorder payload with the backend's zero-based
  contiguous order contract and added a focused page-level regression.

## Scope / DoD Check

- [x] A reordered non-empty Playlist submits Track orders `0..n-1`.
- [x] The focused page-level test asserts the exact `reorderTracks` payload.
- [x] The existing related page test expects the repaired contract.
- [x] The affected API current-state document explicitly records zero-based
      ordering.
- [x] The use-case current-state document was checked and already records
      orders from 0 through n-1.
- [x] Focused tests, typecheck, lint, scoped Prettier, component/route audit,
      and scoped diff check completed.
- [x] Existing dirty changes were preserved.

## Reference Documents

| Tier    | Document                                            | Reason                                                      |
| ------- | --------------------------------------------------- | ----------------------------------------------------------- |
| 0       | `docs/standards/core-principles.md`                 | Constitution, language, approval, and transparency boundary |
| 0       | `docs/standards/development-standards.md`           | Frontend testing and evidence expectations                  |
| 0       | `docs/standards/documentation-standards.md`         | English documentation and current-state update rules        |
| 0       | `docs/standards/glossary.md`                        | Canonical Playlist and Track terms                          |
| Persona | `.claude/agents/qa-fe.md`                           | Frontend quality gates and route/component audit            |
| Context | `deliverables/agent/WI-20260809-ATS-014-handoff.md` | Authoritative scope, constraints, DoD, and output contract  |
| Context | `deliverables/user/WI-20260808-ATS-029-summary.md`  | MAJOR-001 evidence and required repair                      |
| Skill   | `.agents/skills/test/SKILL.md`                      | Focused Vitest execution contract                           |
| Skill   | `.agents/skills/typecheck/SKILL.md`                 | TypeScript verification contract                            |
| Skill   | `.agents/skills/eslint/SKILL.md`                    | ESLint verification contract                                |
| Skill   | `.agents/skills/prettier/SKILL.md`                  | Scoped formatting verification contract                     |
| Skill   | `.agents/skills/create-wi-evidence-pack/SKILL.md`   | Evidence Pack structure and reproducibility                 |
| Skill   | `.agents/skills/react-best-practices/SKILL.md`      | React implementation review                                 |

## Contract Evidence

| Position after reorder | Track ID | Submitted `trackOrder` |
| ---------------------- | -------- | ---------------------- |
| First                  | 102      | 0                      |
| Second                 | 101      | 1                      |
| Third                  | 103      | 2                      |

The exact mock call is:

```typescript
reorderTracks(41, [
  { trackId: 102, trackOrder: 0 },
  { trackId: 101, trackOrder: 1 },
  { trackId: 103, trackOrder: 2 },
]);
```

## Evidence Pointers

### Product

- `frontend/src/pages/subscriber/PlaylistEditPage.tsx:121-125`
  - Maps each reordered Track directly to its zero-based array index.

### Tests

- `frontend/src/pages/subscriber/PlaylistEditPage.test.tsx:25-99`
  - Renders the route, loads three Tracks, moves the first Track down, submits,
    and asserts the exact zero-based payload and completed navigation.
- `frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx:951-978`
  - Retains the broader metadata-plus-reorder page workflow with repaired
    `0, 1` expectations.

### Current-State Documentation

- `docs/design/api-spec.md:278-281`
  - Records visible active Track membership and zero-based `0..n-1` reorder
    semantics.
- `docs/design/usecase/sound-playlist.md:133-140`
  - Already records unique contiguous orders from 0 through n-1; no edit was
    required.

### Route / Component Audit

- `frontend/src/router/index.tsx:164-166`
  - The editor remains under the `subscriberOnly` route boundary.
- The implementation changes no Hook, effect, state, timer, listener, inline
  style, `any`, or `@ts-ignore` behavior.
- The page-level test exercises rendered reorder controls and submission rather
  than relying on API-wrapper forwarding coverage.

## Commands And Outputs

1. Worktree inspection: `git status --short --branch`
   - Branch: `codex/v1-release-rehearsal-fixes`, ahead by one commit.
   - Existing tracked and untracked dirty work was identified and preserved.
2. RED focused test:
   `npm test -- src/pages/subscriber/PlaylistEditPage.test.tsx`
   - Result: 1 file failed; 1 test failed.
   - Received `trackOrder` values were `1, 2, 3`; expected values were
     `0, 1, 2`.
3. GREEN focused test:
   `npm test -- src/pages/subscriber/PlaylistEditPage.test.tsx`
   - Result: 1 file passed; 1 test passed in 3.19 seconds.
4. Existing related page test:
   `npx vitest run src/test/coverage/adminSubscriberPages.coverage.test.tsx -t "saves playlist metadata and reordered tracks after meaningful edits"`
   - Result: 1 file passed; 1 test passed and 32 unrelated tests skipped.
5. TypeScript: `npm run typecheck`
   - Passed with zero errors.
6. ESLint: `npm run lint`
   - Passed with `--max-warnings 0` and zero warnings.
7. Scoped frontend formatting:
   `npx prettier --check "src/pages/subscriber/PlaylistEditPage.tsx" "src/pages/subscriber/PlaylistEditPage.test.tsx" "src/test/coverage/adminSubscriberPages.coverage.test.tsx"`
   - Passed for all three affected TS/TSX files.
8. Current-state document formatting probe:
   `npx prettier --check "src/pages/subscriber/PlaylistEditPage.tsx" "src/pages/subscriber/PlaylistEditPage.test.tsx" "src/test/coverage/adminSubscriberPages.coverage.test.tsx" "../docs/design/api-spec.md"`
   - The three TS/TSX files matched; only the shared dirty API document reported
     pre-existing whole-file Markdown format differences and was not globally
     rewritten.
9. Scoped tracked-file diff check:
   `git diff --check -- frontend/src/pages/subscriber/PlaylistEditPage.tsx frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx docs/design/api-spec.md`
   - Passed with no whitespace errors.
10. Final scoped implementation, test, and deliverable formatting:
    `npx prettier --check "src/pages/subscriber/PlaylistEditPage.tsx" "src/pages/subscriber/PlaylistEditPage.test.tsx" "src/test/coverage/adminSubscriberPages.coverage.test.tsx" "../deliverables/user/WI-20260809-ATS-014-summary.md" "../deliverables/agent/WI-20260809-ATS-014-evidence-pack.md"`
    - Passed for all five scoped files after applying Markdown-only formatting
      corrections to the new deliverables.

## Constraints And External Effects

- No backend, schema, data, dependency, or external API change.
- No real external call, secret access, or ZIP access.
- No full suite, coverage, build, commit, or push.
- `frontend/src/api/playlists.ts` and
  `docs/design/usecase/sound-playlist.md` were inspected but not changed by this
  WI.

## Risks / Rollback

- Risk: mocked page-level API boundaries do not prove deployed browser/backend
  integration behavior.
- Formatting residual: the co-located dirty `docs/design/api-spec.md` retains
  its pre-existing whole-file Prettier differences; the scoped diff is clean.
- Rollback: inverse only the WI-014 hunks identified above and remove the two
  WI-014 deliverables. Preserve all unrelated dirty and untracked work.

## WI-20260808-ATS-029 Status

- WI-014 repairs and focused-tests MAJOR-001.
- The implementation-side blocker is ready for an independent reviewer rerun
  and final disposition.
- This Evidence Pack does not mark WI-029 approved or complete.

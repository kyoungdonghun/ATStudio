# WI-20260711-ATS-010 Frontend Regression Test Summary

## Verdict

- **PASS**
- The repository-configured non-watch frontend suite completed successfully.
- All 14 test files passed, and all 51 tests passed.
- Failed tests: **0**
- Skipped tests: **0**
- No timeout or error excerpt was produced.

## Execution

| Item | Result |
|---|---|
| Working directory | `frontend/` |
| Command | `npm test` |
| Resolved script | `vitest run` |
| Exit code | `0` |
| Test files | 14 passed / 14 total |
| Tests | 51 passed / 51 total |
| Failed | 0 |
| Skipped | 0 |
| Vitest duration | 10.27 seconds |
| Wall-clock elapsed | 11.957 seconds |
| Timeout | None; 300-second command limit was not reached |

## Integrity Check

- No source or snapshot file was modified.
- The only frontend status entries after the run were the same pre-existing untracked `frontend/vite.err.log` and `frontend/vite.out.log` files seen before the run.
- Detailed reproduction evidence is recorded in `deliverables/agent/WI-20260711-ATS-010-evidence-pack.md`.

## Next Consumer

- This result unblocks the frontend/UX review input for `WI-20260711-ATS-018`.

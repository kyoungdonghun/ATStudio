# WI-20260809-ATS-026 Summary

## Outcome

WI-026 is closed as an evidence/documentation deliverable against baseline `e343c20` on branch `codex/v1-release-rehearsal-fixes`.

All six in-scope rows are classified `FAIL` because each has at least one confirmed defect. This does not mean every behavior in each row failed.

| Row      | Result | Confirmed basis                                                                                    |
| -------- | ------ | -------------------------------------------------------------------------------------------------- |
| `G-BUS`  | `FAIL` | F10 application-form gate defect; F11 missing retry on status load failure.                        |
| `MEM-12` | `FAIL` | F01-F06 Whitelist delete/primary/URL/requeue contract defects.                                     |
| `MEM-13` | `FAIL` | F10 leaves the certification application form active after a non-403/non-404 status-check failure. |
| `MEM-14` | `FAIL` | F11 leaves the certification status flow without retry after load failure.                         |
| `ADM-06` | `FAIL` | F11 missing retry for admin list/detail load errors; F12 minor copy issues.                        |
| `ADM-11` | `FAIL` | F07-F09 export scope, stale reload rows, and note-length contract defects.                         |

Finding count: 12 total, `P1=2`, `P2=8`, `P3=2`. See the agent-facing findings file for bounded evidence and four-lane classification.

## Guard Results

Anonymous read-only guard checks passed after 500ms async settlement for exactly these routes:

| Route                                      | Redirect                                                           |
| ------------------------------------------ | ------------------------------------------------------------------ |
| `/whitelist-channels?from=audit`           | `/login?returnTo=%2Fwhitelist-channels%3Ffrom%3Daudit`             |
| `/company-certification/apply?from=audit`  | `/login?returnTo=%2Fcompany-certification%2Fapply%3Ffrom%3Daudit`  |
| `/company-certification/status?from=audit` | `/login?returnTo=%2Fcompany-certification%2Fstatus%3Ffrom%3Daudit` |
| `/admin/company-certifications?from=audit` | `/login?returnTo=%2Fadmin%2Fcompany-certifications%3Ffrom%3Daudit` |
| `/admin/whitelist-channels?from=audit`     | `/login?returnTo=%2Fadmin%2Fwhitelist-channels%3Ffrom%3Daudit`     |

Individual authenticated USER, BUSINESS, and ADMIN variants are `BLOCKED` because no authorized sessions or safe fixtures were available.

## Quality Evidence

The following results were supplied and were not rerun:

- Frontend targeted tests: 11 files / 57 tests passed in 4.20s.
- Backend targeted tests: 27 XML suites / 176 tests; failures, errors, and skipped tests `0`; `BUILD SUCCESSFUL` in 34s.
- `npm run typecheck`: exit code `0`.
- Targeted ESLint: exit code `0`.

These results do not establish authenticated runtime behavior or durable DB/storage/export state.

## Browser and Change Boundary

- Browser restored to `http://127.0.0.1:5173/`, viewport `1280x720`, scroll `0`, with `0` dialogs and `0` file inputs.
- Screenshot inventory: `NONE`.
- No tracked product diff, runtime mutation, upload/download, DB/storage/provider/mail/payment operation, secret inspection, stage, or commit.
- Intentional ZIP was preserved and uninspected.

## Deliverables

- Agent evidence: `deliverables/agent/WI-20260809-ATS-026-evidence-pack.md`
- Findings: `deliverables/agent/WI-20260809-ATS-026-findings.md`
- User summary: `deliverables/user/WI-20260809-ATS-026-summary.md`

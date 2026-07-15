---
version: 1.0
last_updated: 2026-07-15
project: ATS
owner: qa-integ
category: audit
status: stable
dependencies:
  - path: ../agent/WI-20260715-ATS-016-handoff.md
    reason: Approved acceptance-preview refresh scope
  - path: ../agent/WI-20260715-ATS-016-evidence-pack.md
    reason: Detailed sanitized execution evidence
---

# WI-20260715-ATS-016 Summary

## Verdict

**FAIL**

The isolated acceptance-preview branch was safely fast-forwarded from
`b2172346f9c8202abe56ec44b458cd0a493fa232` to
`64db91c4a216336e52ea2cabdfa9445c6a657e9b`, but the refreshed backend could not
start against the existing acceptance environment. There is no active client
URL.

## Completed Work

- Stopped only lifecycle-owned tunnel, frontend, and backend services.
- Verified ports 5173 and 8080 were released.
- Ran `git merge --ff-only codex/p1-acceptance-hardening` successfully.
- Verified `codex/acceptance-preview` is clean at exact commit `64db91c`.
- Used only the repository acceptance lifecycle and the existing repo-external environment artifact.
- Stopped after the same sanitized startup blocker repeated three times.
- Left the failed lifecycle fully cleaned up with no owned services or listeners.
- Did not change the development branch, product files, schema, data, providers, or four development runtime logs.

## Startup Blocker

All three attempts failed before backend readiness with the same safe exception
signature led by `org.hibernate.tool.schema.spi.SchemaManagementException` and
`jakarta.persistence.PersistenceException`. This identifies a JPA/Hibernate
schema-management compatibility blocker without inspecting or exposing any
credential, database/JDBC identity, schema object name, or log message value.

| Attempt | Command result | Lifecycle result |
|---|---:|---|
| 1 | Exit 1 after 189.7 seconds | Local `/api/tracks` readiness failed |
| 2 | Exit 1 after 67.1 seconds | Same blocker; bounded 60-second diagnostic timeout |
| 3 | Exit 1 after 68.6 seconds | Same blocker; retries stopped |

## Availability

- Active URL: **None**.
- Last issued URL, now inactive:
  `https://import-sides-remaining-nights.trycloudflare.com`.
- Final lifecycle status: `failed`.
- Final owned services: none.
- Final listeners on 5173/8080: none.

| Surface | `/` | `/api/tracks` | `/admin/dashboard` | Protected Question attachment |
|---|---:|---:|---:|---:|
| Local | No response (`000`, curl 7) | No response (`000`, curl 7) | No response (`000`, curl 7) | No response; 401/403 not verified |
| Public | 530 | 530 | 530 | 530; 401/403 not verified |

Response bodies were neither displayed nor retained.

## Required Next Action

Keep `WI-20260715-ATS-017` blocked. Create a separately approved
acceptance-schema compatibility WI to perform a sanitized schema-drift review
and authorize either a fresh disposable acceptance database at the `64db91c`
contract or a backed-up, rehearsed migration of the existing acceptance
database. Then rerun the lifecycle and all local/public/protected probes.

Returning the preview branch to `b217234` is not a fast-forward and requires a
separately approved branch update. No rollback was performed.

## Changed Paths

- `deliverables/agent/WI-20260715-ATS-016-evidence-pack.md`
- `deliverables/user/WI-20260715-ATS-016-summary.md`
- Repo-external acceptance lifecycle manifest and three timestamped run directories

## Output Validation

- Documentation validation: PASS, exit `0`.
- `git diff --check`: PASS, exit `0`.
- WI-016 trailing-whitespace, EOF-newline, and sensitive-assignment pattern checks: PASS.
- No lifecycle restart was attempted after the third failed attempt.

## Related Documents

- [WI-016 Evidence Pack](../agent/WI-20260715-ATS-016-evidence-pack.md): Exact commands, exits, statuses, and sanitized blocker evidence
- [WI-016 Handoff](../agent/WI-20260715-ATS-016-handoff.md): Approved scope and constraints

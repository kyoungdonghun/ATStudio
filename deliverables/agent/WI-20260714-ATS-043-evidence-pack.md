---
version: 1.0
last_updated: 2026-07-15
project: ATS
owner: qa-integ
category: evidence-pack
status: stable
dependencies:
  - path: WI-20260714-ATS-043-handoff.md
    reason: Approved frozen acceptance preview scope
  - path: WI-20260714-ATS-041-evidence-pack.md
    reason: Independent lifecycle and checkpoint audit baseline
---

# Evidence Pack: WI-20260714-ATS-043

## Summary (one-liner)

- Confirmed that the frozen acceptance worktree remains clean at the approved checkpoint and that its owned local and Cloudflare preview services still serve the required shell and API paths while protected Question attachments remain inaccessible.

## Verdict

**PASS**

## Scope / DoD Check

- [x] Preview worktree is on `codex/acceptance-preview` at checkpoint `b217234` and has no Git changes.
- [x] Acceptance lifecycle status reports `running` with owned tunnel, backend, and frontend services.
- [x] Local SPA, API, and admin shell return HTTP 200.
- [x] Public SPA, API, and admin shell return HTTP 200 through the same temporary Cloudflare origin.
- [x] Direct access to a Question attachment probe returns HTTP 401 locally and publicly.
- [x] Repository-external bootstrap credential file exists as a regular file with protected ACL inheritance.
- [x] Runtime services and the disposable database were left running; no restart or teardown occurred.
- [x] No credential, database name, JDBC URL, token, key, password, or response body was read into evidence.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Security, approval, and traceability baseline |
| 0 | `docs/standards/development-standards.md` | Verification and evidence requirements |
| 0 | `docs/standards/documentation-standards.md` | Deliverable structure |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| REQ | `deliverables/user/REQ-20260714-ATS-001.md` | Approved acceptance-hardening scope |
| WI | `deliverables/agent/WI-20260714-ATS-043-handoff.md` | WI-043 scope and output contract |

## Verification Results

### Frozen checkpoint

| Check | Result |
|---|---|
| Worktree | Dedicated acceptance preview worktree |
| Branch | `codex/acceptance-preview` |
| Commit | `b217234` |
| Git status | Clean |

### Runtime ownership

`scripts/acceptance/status.ps1` reported lifecycle state `running`. The runtime manifest identified one owned process for each role: tunnel, backend, and frontend. Local ports 5173 and 8080 were listening. Wrapper and child listener process identifiers were intentionally omitted.

### Header-only HTTP probes

The probes used GET with response-headers-only completion. Response bodies were not read or recorded.

| Surface | Path category | HTTP status | Result |
|---|---|---:|---|
| Local | SPA root | 200 | PASS |
| Local | Track API | 200 | PASS |
| Local | Admin SPA shell | 200 | PASS |
| Local | Question attachment direct access | 401 | PASS - denied |
| Public | SPA root | 200 | PASS |
| Public | Track API | 200 | PASS |
| Public | Admin SPA shell | 200 | PASS |
| Public | Question attachment direct access | 401 | PASS - denied |

### External credential artifact

- The credential artifact exists under the repo-external acceptance runtime root.
- It is a regular file with ACL inheritance disabled, an owner, and two access rules.
- Only file metadata and the path were checked. The file body was not opened, parsed, hashed, or copied.
- The exact path is retained outside repository evidence and may be disclosed directly to the operator.

## State Safety

- No server, tunnel, frontend, backend, or database process was stopped or restarted.
- No provider, payment, refund, billing-key, OAuth, or email mutation was invoked.
- No long-running authentication flow, build, test suite, schema migration, or data import was rerun.
- No runtime log, secret bundle, credential body, database cleanup body, or application-local configuration was read.
- The development worktree remained on `codex/p1-acceptance-hardening`; its pre-existing runtime logs were untouched.

## Deferred Coverage / Limitations

- This close-out revalidated availability and access boundaries only. It did not repeat the earlier ADMIN/subscriber authenticated journeys or logout replay test.
- The Cloudflare Quick Tunnel uses a temporary randomized origin and has no availability SLA; the URL stops working when the owned tunnel process exits.
- A successful API response proves the application can currently use its disposable database, but this close-out did not inspect the database name, credentials, or retained records.
- Known payment exceptional-integrity findings tracked outside WI-043 remain development-branch work and were not changed in the frozen preview.

## Rollback / Shutdown

- Use the acceptance lifecycle stop script with the same repo-external runtime root when the client review ends.
- Drop only the disposable preview database using its repo-external cleanup metadata after services are stopped.
- Do not remove or modify the development worktree's unrelated runtime logs.

## Related Documents

- [WI-043 User Summary](../user/WI-20260714-ATS-043-summary.md): Operator-facing outcome and limitations
- [WI-043 Handoff](WI-20260714-ATS-043-handoff.md): Approved scope and acceptance criteria

---
version: 1.0
last_updated: 2026-07-15
project: ATS
owner: qa-integ
category: evidence-pack
status: stable
dependencies:
  - path: WI-20260715-ATS-017-handoff.md
    reason: Approved isolated fresh-database recovery scope and output contract
  - path: WI-20260715-ATS-016-evidence-pack.md
    reason: Failed old-database startup attempt at the current checkpoint
  - path: WI-20260714-ATS-043-evidence-pack.md
    reason: Prior frozen-preview availability and protected-resource baseline
---

# Evidence Pack: WI-20260715-ATS-017

## Summary (one-liner)

- Reopened the clean `64db91c` acceptance preview with an isolated repo-external runtime root and one fresh disposable database, passed the bounded local/public smoke and protected-attachment checks, and retained the new preview runtime and database for client review.

## Verdict

**PASS**

## Scope / DoD Check

- [x] Verified the acceptance preview is clean on `codex/acceptance-preview` at exact commit `64db91c`.
- [x] Used a new checkpoint-specific repo-external runtime root without altering the previous runtime or its metadata.
- [x] Preserved the previous disposable database and its external metadata unchanged.
- [x] Created one newly approved disposable MySQL database from the current canonical schema and retained it for the running preview.
- [x] Reached lifecycle state `running` with one owned tunnel, frontend, and backend.
- [x] Received HTTP 200 for the required local and public SPA, Track API, and admin-shell paths.
- [x] Received HTTP 401 for the required local and public anonymous Question attachment probes.
- [x] Left the new runtime services and new disposable database running after PASS.
- [x] Kept evidence redacted and made no product, handoff, branch, or runtime change during evidence close-out.
- [x] Recorded that no full audit or deep acceptance journey was run.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approval, language, security, and traceability baseline |
| 0 | `docs/standards/development-standards.md` | Evidence-first and integration-verification requirements |
| 0 | `docs/standards/documentation-standards.md` | Repository-required deliverable structure baseline |
| 0 | `docs/standards/glossary.md` | Repository-required canonical terminology baseline |
| 1 | `docs/policies/security-policy.md` | Secret, credential, database, and logging restrictions |
| 1 | `docs/policies/quality-gates.md` | DoD, validation, traceability, and rollback requirements |
| WI | `deliverables/agent/WI-20260715-ATS-017-handoff.md` | Approved scope, acceptance criteria, and output contract |
| Evidence | `deliverables/agent/WI-20260715-ATS-016-evidence-pack.md` | Current-checkpoint failure against the preserved older database |
| Evidence | `deliverables/agent/WI-20260714-ATS-043-evidence-pack.md` | Earlier frozen-preview smoke and access-boundary baseline |

**Injection rules applied:**

- Assignee: `qa-integ`
- Task type: integration verification and evidence close-out
- Required context tiers: Tier 0 and task-relevant Tier 1 policies
- Evidence style: pointers and status-only results; no sensitive values or response bodies

## Evidence Pointers

- Approved work contract: `deliverables/agent/WI-20260715-ATS-017-handoff.md`
- Failed predecessor attempt: `deliverables/agent/WI-20260715-ATS-016-evidence-pack.md`
- Prior passing preview baseline: `deliverables/agent/WI-20260714-ATS-043-evidence-pack.md`
- Canonical schema source: acceptance preview worktree `src/main/resources/schema.sql`
- Lifecycle source: acceptance preview worktree `scripts/acceptance/`
- New runtime state: checkpoint-specific repo-external runtime root; exact path intentionally omitted
- Close-out files:
  - `deliverables/agent/WI-20260715-ATS-017-evidence-pack.md`
  - `deliverables/user/WI-20260715-ATS-017-summary.md`

## Recovery Continuity

| WI | Checkpoint | Database/runtime context | Outcome |
|---|---|---|---|
| WI-043 | `b217234` | Earlier frozen acceptance runtime and disposable database | PASS baseline |
| WI-016 | `64db91c` | Preserved older runtime/database context | FAIL in JPA/Hibernate schema-management startup |
| WI-017 | `64db91c` | Separate runtime root and one fresh disposable database from the current canonical schema | PASS; running |

WI-017 isolates the recovery from WI-016. The previous runtime, previous disposable database, and their external metadata remain available and untouched; no migration or in-place repair was performed against them.

## Verification Results

### Checkpoint and lifecycle

| Check | Result |
|---|---|
| Preview branch | `codex/acceptance-preview` |
| Preview commit | `64db91c` |
| Preview Git state | Clean |
| Runtime root | New, separate, repo-external, and checkpoint-specific; exact path omitted |
| Database | One newly approved disposable MySQL database created from the current canonical schema; identifier omitted |
| Previous state | Previous runtime, database, and external metadata preserved unchanged |
| Lifecycle | `running` |
| Owned roles | One tunnel, one frontend, and one backend; process identifiers omitted |
| Local listeners | Frontend 5173 and backend 8080 |
| Active public origin | `https://specials-pro-obtained-nuclear.trycloudflare.com` |
| Retention | New runtime services and new disposable database intentionally left running |

The running backend against the fresh canonical schema closes the WI-016 startup blocker for this isolated preview. No database identity, connection value, credential, or schema response was recorded.

### Status-only HTTP probes

All probes recorded only the HTTP status. Response bodies were not displayed or retained.

| Surface | Path | HTTP status | Result |
|---|---|---:|---|
| Local | `/` | 200 | PASS |
| Local | `/api/tracks` | 200 | PASS |
| Local | `/admin/dashboard` | 200 | PASS |
| Local | `/uploads/questions/acceptance-probe.txt` | 401 | PASS - anonymous access denied |
| Public | `/` | 200 | PASS |
| Public | `/api/tracks` | 200 | PASS |
| Public | `/admin/dashboard` | 200 | PASS |
| Public | `/uploads/questions/acceptance-probe.txt` | 401 | PASS - anonymous access denied |

## Commands & Outputs

The operational commands were completed before this evidence close-out. They are recorded by category to preserve reproducibility without disclosing sensitive arguments:

1. Read-only Git branch, commit, and cleanliness verification in the acceptance preview worktree.
2. Creation and validation of a separate repo-external runtime root while preserving the previous runtime and database metadata.
3. Approved creation of one constrained disposable MySQL database and application of the current canonical `schema.sql`.
4. Acceptance lifecycle startup and status verification through the repository `start.ps1` and `status.ps1` scripts.
5. Local and public status-only GET probes for the SPA root, Track API, admin shell, and anonymous Question attachment path.
6. Close-out-only whitespace validation with `git diff --check` restricted to the two WI-017 deliverables.

No operational command was replayed by the evidence closer. In particular, no lifecycle stop, database drop, branch update, or runtime inspection was performed during documentation close-out.

## Security / Non-Disclosure Check

- No exact database name, JDBC URL, username, password, credential, token, provider key, process identifier, or response body appears in either WI-017 deliverable.
- No sensitive runtime, environment, credential, or cleanup path appears beyond the generic repo-external runtime-root pointer.
- Secret-bearing environment and cleanup artifacts remain outside repositories.
- The active Cloudflare Quick Tunnel URL is included because it is the approved client access endpoint; it is temporary and stops working when the owned tunnel exits.
- No live payment, SMTP, OAuth, data import, migration, or retained-database mutation was performed as part of this close-out.

## Tests / Coverage Boundary

- Checkpoint cleanliness: PASS.
- Fresh-schema backend startup and owned lifecycle readiness: PASS.
- Six local/public availability probes: PASS with HTTP 200.
- Two local/public protected-attachment probes: PASS with HTTP 401.
- Full audit: not run.
- Deep authenticated acceptance journey: not run.
- Build suite, broad regression suite, payment/provider mutation, email, OAuth, data import, and migration testing: not run and outside scope.

## Final State

- The client preview remains available at `https://specials-pro-obtained-nuclear.trycloudflare.com`.
- The lifecycle remains `running` with the new runtime root's owned tunnel, frontend, and backend.
- The newly created disposable database remains retained for the running preview.
- The previous runtime, previous disposable database, and their metadata remain untouched.
- The Cloudflare URL is temporary and has no durable availability guarantee.

## Risks / Rollback / Shutdown

### Residual risks and limitations

- The Quick Tunnel origin is temporary and becomes unavailable when the owned tunnel or host session ends.
- The verification is a bounded availability and anonymous-access smoke, not a full audit or deep acceptance journey.
- Authenticated role journeys, logout replay, uploads beyond the protected probe, media behavior, payments, provider callbacks, email, and OAuth were not exercised.

### Conceptual shutdown and database cleanup

1. After client review, invoke the acceptance lifecycle stop procedure against only the new checkpoint-specific runtime root.
2. Verify only that runtime root's owned tunnel, frontend, and backend stop and that its listeners are released.
3. If database cleanup is separately approved, resolve and validate the target from the new runtime's external cleanup metadata, then drop only the newly created disposable database.
4. Do not stop, alter, migrate, delete, or drop the previous runtime, previous disposable database, or their metadata.

Shutdown and database cleanup were intentionally not executed. The preview and its new disposable database remain running and retained.

## Output Validation

- `git diff --check -- deliverables/agent/WI-20260715-ATS-017-evidence-pack.md deliverables/user/WI-20260715-ATS-017-summary.md` -> PASS, exit `0`.
- Because both files are untracked, supplemental `git -c core.autocrlf=false diff --no-index --check` checks compared each file with an empty input. Both produced zero whitespace diagnostics; exit `1` is the expected no-index result for non-empty content.

## Related Documents

- [WI-017 User Summary](../user/WI-20260715-ATS-017-summary.md): Korean operator-facing result and temporary preview URL
- [WI-017 Handoff](WI-20260715-ATS-017-handoff.md): Approved scope and acceptance criteria
- [WI-016 Evidence Pack](WI-20260715-ATS-016-evidence-pack.md): Failed preserved-database attempt at `64db91c`
- [WI-043 Evidence Pack](WI-20260714-ATS-043-evidence-pack.md): Earlier passing frozen-preview baseline

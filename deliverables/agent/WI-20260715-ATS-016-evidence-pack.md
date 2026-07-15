---
version: 1.0
last_updated: 2026-07-15
project: ATS
owner: qa-integ
category: evidence-pack
status: stable
dependencies:
  - path: WI-20260715-ATS-016-handoff.md
    reason: Approved acceptance-preview refresh scope and constraints
  - path: WI-20260715-ATS-015-evidence-pack.md
    reason: Verified development checkpoint and prior preview baseline
---

# Evidence Pack: WI-20260715-ATS-016

## Verdict

**FAIL**

The acceptance-preview branch fast-forwarded cleanly from `b217234` to
`64db91c`, but the backend failed to start in all three lifecycle attempts with
the same sanitized JPA/Hibernate schema-management exception signature. The
lifecycle cleaned up every owned process after each failure. There is no active
public URL.

## Scope / DoD Check

- [x] Recorded the development and preview pre-state without reading the four development runtime logs.
- [x] Stopped only lifecycle-owned acceptance services and verified ports 5173 and 8080 were released.
- [x] Fast-forwarded `codex/acceptance-preview` with `--ff-only` to exact commit `64db91c`.
- [x] Restarted only through `scripts/acceptance/start.ps1` with the existing repo-external environment artifact and installed cloudflared.
- [x] Stopped retries after the same safe startup blocker repeated three times.
- [x] Performed final local, public, admin-shell, and protected-attachment probes without retaining response bodies.
- [x] Preserved the development branch, product files, schema, data, providers, and four development runtime logs.
- [ ] Lifecycle is not ready/running; final state is `failed` with no owned services.
- [ ] Local SPA, Track API, admin shell, and protected attachment boundary did not return required statuses.
- [ ] Public SPA, Track API, admin shell, and protected attachment boundary did not return required statuses.
- [ ] No active client URL is available.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approval, recovery, and traceability baseline |
| 0 | `docs/standards/development-standards.md` | Verification and evidence requirements |
| 0 | `docs/standards/documentation-standards.md` | Deliverable structure and link rules |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/security-policy.md` | Sensitive-data and protected-resource boundary |
| 1 | `docs/policies/quality-gates.md` | FAIL criteria and evidence requirements |
| REQ | `deliverables/user/REQ-20260714-ATS-001.md` | Approved P1 and acceptance scope |
| Evidence | `deliverables/agent/WI-20260714-ATS-043-evidence-pack.md` | Frozen preview runtime and protected-probe baseline |
| Evidence | `deliverables/agent/WI-20260715-ATS-014-evidence-pack.md` | Development and frozen-preview audit baseline |
| Evidence | `deliverables/agent/WI-20260715-ATS-015-evidence-pack.md` | Verified development checkpoint and residual limits |
| Context | `docs/SR/SR-42.md` | Single frontend tunnel and same-origin proxy pattern |
| Context | `docs/client/testing-guide.md` | Client acceptance and live-provider safety boundary |
| WI | `deliverables/agent/WI-20260715-ATS-016-handoff.md` | Scope, commands, acceptance criteria, and output contract |

The acceptance scripts `start.ps1`, `stop.ps1`, `status.ps1`, and their imported
`AcceptanceLifecycle.psm1` were read from the dedicated preview worktree. The
repo-external environment artifact was checked only as an existing regular
file. It was not opened, parsed, copied, hashed, or printed.

## Git And Runtime Pre-State

| Check | Result |
|---|---|
| Development branch | `codex/p1-acceptance-hardening` |
| Development HEAD | `64db91c4a216336e52ea2cabdfa9445c6a657e9b` |
| Development tracked state | Clean |
| Preserved development untracked paths | Four runtime logs plus the pre-existing WI-016 handoff |
| Preview branch | `codex/acceptance-preview` |
| Preview HEAD | `b2172346f9c8202abe56ec44b458cd0a493fa232` |
| Preview state | Clean |
| Fast-forward target | `64db91c4a216336e52ea2cabdfa9445c6a657e9b` |
| Fast-forward ancestry check | PASS |
| Pre-start lifecycle status command | Exit `0`; `running` |
| Pre-start owned roles | tunnel, frontend, backend |
| Previous public URL | `https://sara-edit-seeker-receiving.trycloudflare.com` |
| Repo-external environment artifact | Exists; regular file; content not inspected |
| cloudflared | Installed; provider/configuration unchanged |

## Lifecycle And Branch Update

Commands were run from
`C:\Users\jm991\Desktop\project\ATStudio-acceptance-preview`.

```powershell
$runtimeRoot = Join-Path $env:LOCALAPPDATA 'ATStudio\acceptance-preview'
$backendEnvironmentPath = Join-Path $runtimeRoot 'backend-environment-credentials.json'
$cloudflared = Get-Command cloudflared.exe

.\scripts\acceptance\status.ps1 -RuntimeRoot $runtimeRoot
.\scripts\acceptance\stop.ps1 -RuntimeRoot $runtimeRoot
git merge --ff-only codex/p1-acceptance-hardening
.\scripts\acceptance\start.ps1 `
  -RuntimeRoot $runtimeRoot `
  -CloudflaredPath $cloudflared.Source `
  -BackendEnvironmentPath $backendEnvironmentPath
```

| Operation | Exit | Result |
|---|---:|---|
| Pre-state `status.ps1` | 0 | `running`; all three roles owned |
| `stop.ps1` | 0 | `stopped`; all three roles stopped; ports closed; previous URL unreachable |
| Listener verification | 0 | Zero listeners on 5173/8080 |
| `git merge --ff-only codex/p1-acceptance-hardening` | 0 | Fast-forward `b217234..64db91c` |
| Post-merge Git verification | 0 | Exact target, correct branch, clean worktree |
| Start attempt 1, default readiness timeout | 1 | Local `/api/tracks` readiness failed after 189.7 seconds |
| Start attempt 2, 60-second bounded diagnostic timeout | 1 | Same local `/api/tracks` readiness failure after 67.1 seconds |
| Start attempt 3, 60-second bounded diagnostic timeout | 1 | Same local `/api/tracks` readiness failure after 68.6 seconds |
| Final `status.ps1` | 0 | `failed`; tunnel/frontend/backend all unowned |
| Final listener verification | 0 | Zero listeners on 5173/8080 |

Attempts 2 and 3 used only the lifecycle script's existing
`-ReadinessTimeoutSeconds 60` parameter. No environment value, schema, data, or
provider setting was changed.

## Sanitized Startup Blocker

Each of the three backend runs produced the same exception-class signature.
Only class names and counts were extracted; no log line, message value,
credential, database/JDBC identity, or schema object name was read or recorded.

| Exception class | Count per attempt |
|---|---:|
| `jakarta.persistence.PersistenceException` | 1 |
| `org.hibernate.tool.schema.spi.SchemaManagementException` | 4 |
| `org.springframework.beans.factory.BeanCreationException` | 3 |
| `org.springframework.beans.factory.UnsatisfiedDependencyException` | 3 |
| `org.springframework.boot.web.server.WebServerException` | 1 |
| `org.springframework.context.ApplicationContextException` | 2 |

Safe blocker statement: backend startup aborts in the JPA/Hibernate
schema-management path before the web server becomes ready. The exact database
identity and schema object mismatch remain intentionally unidentified under the
WI constraints. Port binding, network connection, and credential-authentication
marker counts were zero in the sanitized category check.

## Final Smoke Results

All probes used GET, discarded response content to `NUL`, and recorded only curl
exit and HTTP status output.

```powershell
curl.exe --silent --location --max-time 15 --output NUL --write-out '%{http_code}' <URL>
```

| Surface | Path | Curl exit | HTTP output | Required | Result |
|---|---|---:|---:|---:|---|
| Local | `/` | 7 | `000` (no HTTP response) | 200 | FAIL |
| Local | `/api/tracks` | 7 | `000` (no HTTP response) | 200 | FAIL |
| Local | `/admin/dashboard` | 7 | `000` (no HTTP response) | 200 | FAIL |
| Local | `/api/questions/1/attachments/1` | 7 | `000` (no HTTP response) | 401 or 403 | FAIL / unverified boundary |
| Public | `/` | 0 | 530 | 200 | FAIL |
| Public | `/api/tracks` | 0 | 530 | 200 | FAIL |
| Public | `/admin/dashboard` | 0 | 530 | 200 | FAIL |
| Public | `/api/questions/1/attachments/1` | 0 | 530 | 401 or 403 | FAIL / unverified boundary |

- Active URL: **None**.
- Last issued but inactive URL:
  `https://import-sides-remaining-nights.trycloudflare.com`.
- Response bodies were neither displayed nor retained.

## Final State And Changed Paths

| Item | Final state |
|---|---|
| Preview branch/HEAD | `codex/acceptance-preview` at `64db91c4a216336e52ea2cabdfa9445c6a657e9b` |
| Preview Git state | Clean |
| Lifecycle | `failed`; no owned tunnel/frontend/backend process |
| Ports | No listeners on 5173/8080 |
| Development branch/HEAD | Unchanged at `codex/p1-acceptance-hardening` / `64db91c4a216336e52ea2cabdfa9445c6a657e9b` |
| Development product/schema/data | Unchanged |
| Live providers | Not called or changed |

WI-016-created repository paths:

- `deliverables/agent/WI-20260715-ATS-016-evidence-pack.md`
- `deliverables/user/WI-20260715-ATS-016-summary.md`

Authorized runtime state changed only under the repo-external acceptance root:

- lifecycle manifest state and three new timestamped run directories
- no repo-external file content is reproduced in this evidence

The pre-existing `deliverables/agent/WI-20260715-ATS-016-handoff.md` and four
development runtime logs were not modified. No file was staged, committed, or
pushed.

## Output Validation

| Check | Exit / result |
|---|---|
| `python .agents\skills\validate-docs\scripts\validate_docs.py` | Exit `0`; Tier 0, internal links, 376 traceability IDs, and document index passed |
| `git diff --check` | Exit `0` |
| WI-016 output trailing-whitespace scan | 0 lines in both files |
| WI-016 output EOF-newline check | PASS for both files |
| WI-016 sensitive-assignment pattern scan | 0 matches in both files |

No lifecycle restart was attempted after attempt 3 or after the explicit stop
instruction. Final status was observed read-only.

## Risks / Rollback / Shutdown

### Residual limitations

- The client acceptance preview is unavailable and has no active URL.
- SPA, API, admin shell, and protected attachment acceptance criteria are not met.
- The precise schema mismatch was not inspected because this WI prohibits database identity disclosure and schema/data work.
- Authenticated journeys, uploads/media, payment callbacks, provider behavior, email, OAuth, and retained data were not exercised.
- The Cloudflare Quick Tunnel URL is temporary and is already inactive after lifecycle cleanup.

### Next WI recommendation

Keep `WI-20260715-ATS-017` blocked. Create a separately approved
acceptance-schema compatibility WI before any further preview restart. That WI
should authorize a sanitized schema-drift comparison and choose one controlled
path: prepare a fresh disposable acceptance database at the `64db91c` contract,
or approve and rehearse the required migration against the existing acceptance
database with backup and rollback evidence. After that WI passes, rerun the
acceptance lifecycle and all eight WI-016 probes.

### Rollback and shutdown

- Returning the preview branch to `b2172346f9c8202abe56ec44b458cd0a493fa232`
  requires a separately approved branch update because it is not a fast-forward;
  no reset, force update, or rollback was performed here.
- Current shutdown command, if future owned services are started:

```powershell
.\scripts\acceptance\stop.ps1 -RuntimeRoot $runtimeRoot
```

## Related Documents

- [WI-016 User Summary](../user/WI-20260715-ATS-016-summary.md): Operator-facing verdict and next action
- [WI-016 Handoff](WI-20260715-ATS-016-handoff.md): Approved scope and acceptance criteria
- [WI-015 Evidence Pack](WI-20260715-ATS-015-evidence-pack.md): Development checkpoint baseline

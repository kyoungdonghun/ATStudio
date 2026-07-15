# Evidence Pack: WI-20260715-ATS-023

## Summary (one-liner)

- Committed the verified complete-listening checkpoint, froze it at one clean stable branch/worktree, replaced only the owned acceptance processes, and passed local/public status and retained full-resource stream probes.

## Scope / DoD Check

- [x] Committed only the 39 authorized product, current-document, test, REQ, and WI paths.
- [x] Kept runtime logs, PID files, credentials, runtime state, ignored dependencies, and unrelated paths out of the commit.
- [x] Created `codex/client-demo-stable` and the exact requested worktree at the development commit.
- [x] Verified the stable worktree is clean at `109112809523c0820aab2a408a355faa21b7833e`.
- [x] Preserved the existing acceptance environment bundle, database connection, and uploaded media.
- [x] Stopped only the manifest-owned old tunnel, frontend, and backend process trees.
- [x] Started the stable worktree through `scripts/acceptance/start.ps1` after installing its locked frontend dependencies.
- [x] Verified local and public SPA, Track API, complete Track stream, and Range behavior.
- [x] Verified the no-Range length equals exactly one retained stored full audio resource.
- [x] Verified anonymous direct static original access remains denied.
- [x] Left exactly one owned tunnel, frontend, and backend running.
- [x] Made no database/schema/data mutation and no live Provider, email, or OAuth request.
- [x] Recorded exact checkpoint, lifecycle, URL, probes, risks, and stop/rollback procedure.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Approval, security, language, and traceability baseline |
| 0 | `docs/standards/development-standards.md` | QA evidence and test standards |
| 0 | `docs/standards/documentation-standards.md` | Repository-required documentation rules |
| 0 | `docs/standards/glossary.md` | Canonical Public Listening and Official Download terms |
| 1 | `docs/policies/security-policy.md` | Protected Track media and secret-handling boundary |
| 1 | `docs/policies/quality-gates.md` | Git, validation, rollback, and evidence gates |
| 2 | `deliverables/user/REQ-20260715-ATS-001.md` | Approved complete-listening and stable-demo scope |
| 2 | `deliverables/agent/WI-20260715-ATS-022-evidence-pack.md` | Reused full backend/frontend/document quality gates |
| 2 | `deliverables/agent/WI-20260715-ATS-017-evidence-pack.md` | Retained acceptance database/runtime baseline |
| 2 | `docs/SR/SR-42.md` | Single frontend tunnel and Vite proxy topology |
| 2 | `scripts/acceptance/start.ps1` | Owned stable-runtime startup |
| 2 | `scripts/acceptance/status.ps1` | Owned process status |
| 2 | `scripts/acceptance/stop.ps1` | Owned process shutdown |

**Injection rules applied:** Handoff `deliverables/agent/WI-20260715-ATS-023-handoff.md`; assignee `qa`; deployment QA task; read order Tier 0 -> Tier 1 -> Tier 2 -> lifecycle implementation.

## Git Freeze Evidence

### Exact Commit

| Check | Result |
|------|--------|
| Development branch | `codex/p1-acceptance-hardening` |
| Parent checkpoint | `64db91c4a216336e52ea2cabdfa9445c6a657e9b` |
| New commit | `109112809523c0820aab2a408a355faa21b7833e` |
| Commit subject | `WI-20260715-ATS-023: complete-listening verified checkpoint freeze` (committed in Korean) |
| Commit size | 39 files, 2,995 insertions, 405 deletions |
| Staged-path comparison | 39 expected, 39 actual, 0 missing, 0 extra |
| `git diff --cached --check` | PASS, exit 0 |
| Forbidden staged runtime paths | 0 |
| Representative secret signatures | 0 matches |

The Korean commit subject is `WI-20260715-ATS-023: \uc804\uccb4 \uac10\uc0c1 \uac80\uc99d \uccb4\ud06c\ud3ec\uc778\ud2b8 \uace0\uc815`. Its Korean body records the goal, reused acceptance lifecycle/environment bundle, and authorized impact boundary.

### Authorized Staged Paths

- Current backend and tests: `src/main/java/com/atstudio/atstudio/controller/TrackController.java`, `src/main/java/com/atstudio/atstudio/service/TrackService.java`, `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java`, `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java`.
- Current frontend and tests: `frontend/src/layouts/PlayerBar.tsx`, `frontend/src/layouts/PlayerBar.test.tsx`, `frontend/src/store/playerStore.ts`, `frontend/src/store/playerStore.test.ts`.
- Current documents: `docs/audit/p0-release-blocker-closure-20260713.md`, `docs/design/api-spec.md`, `docs/design/db-schema.md`, `docs/design/p0-release-blocker-remediation-design.md`, `docs/design/usecase/index.md`, `docs/design/usecase/sound-track.md`, `docs/policies/security-policy.md`, `docs/standards/glossary.md`.
- User deliverables: `deliverables/user/REQ-20260715-ATS-001.md` and `deliverables/user/WI-20260715-ATS-016-summary.md` through `WI-20260715-ATS-022-summary.md`.
- Agent deliverables: WI-016 through WI-022 Evidence Packs and handoffs, plus `deliverables/agent/WI-20260715-ATS-023-handoff.md`.

Root `cloudflared.out.log`, `cloudflared.err.log`, `frontend/vite.out.log`, and `frontend/vite.err.log` remained untracked. The WI-023 summary and this Evidence Pack were created after the commit in the development workspace, as required, and are not part of the frozen checkpoint.

### Stable Branch and Worktree

| Check | Result |
|------|--------|
| Branch | `codex/client-demo-stable` |
| Branch HEAD | `109112809523c0820aab2a408a355faa21b7833e` |
| Worktree | `C:/Users/jm991/Desktop/project/ATStudio-client-demo-stable` |
| Worktree HEAD | `109112809523c0820aab2a408a355faa21b7833e` |
| Worktree status | Clean |
| Manifest `repoRoot` | Exact stable worktree path |
| Push | Not performed |

`frontend/node_modules` was absent in the new worktree. `npm.cmd ci` completed with 309 packages added from the committed lockfile. It did not modify tracked files; npm reported 13 existing audit findings.

## Lifecycle Evidence

### Old Owned Runtime Stop

- Runtime root: `C:/Users/jm991/AppData/Local/ATStudio/acceptance-preview-64db91c`.
- Initial `status.ps1`: state `running`; tunnel PID 14936, frontend PID 21160, backend PID 12760; all `owned=true`.
- `stop.ps1 -TimeoutSeconds 60`: all three roles returned `stopped=true` and `reason=owned-process-tree-stopped`.
- Stop result: `portsClosed=true` and `publicUrlUnreachable=true` for the previous tunnel URL.
- No other branch, worktree, process root, database, bundle, or upload path was stopped or removed.

### Stable Runtime Start and Final Status

- Start source: `C:/Users/jm991/Desktop/project/ATStudio-client-demo-stable/scripts/acceptance/start.ps1`.
- Reused the same runtime root and the existing repo-external backend environment bundle.
- Final lifecycle state: `running`.
- Public base URL: `https://roland-perry-spatial-feat.trycloudflare.com`.
- Owned services: tunnel PID 34956, frontend PID 17184, backend PID 32976; all `owned=true`.
- Local listeners: frontend `127.0.0.1:5173`, backend `127.0.0.1:8080`.

## Preservation Evidence

| Check | Before | After | Result |
|------|-------:|------:|--------|
| Environment bundle SHA-256 | `060cfac1b238f13c209c30870d47e75b8bf16872c691180a3b555858c25c338b` | Same | PASS |
| Environment bundle length | 1,452 bytes | 1,452 bytes | PASS |
| Public storage | 3 files / 26,165,861 bytes | 3 files / 26,165,861 bytes | PASS |
| Private storage | 0 files / 0 bytes | 0 files / 0 bytes | PASS |
| Retained active Track | Track `2` | Track `2` streamed | PASS |

No environment value, database identifier, credential, original storage key, or response body is recorded. Database retention is demonstrated by the unchanged bundle plus the retained Track list and successful stored-resource stream; no SQL or database mutation command was run.

## HTTP Probe Evidence

### Status-only SPA and API

| Surface | Path | Status |
|---------|------|-------:|
| Local frontend | `/` | 200 |
| Local backend | `/api/tracks?size=100` | 200 |
| Local frontend proxy | `/api/tracks?size=100` | 200 |
| Public | `/` | 200 |
| Public | `/api/tracks?size=100` | 200 |

Only the local Track response was parsed in memory to select Track `2`; response bodies were not displayed or retained.

### Complete no-Range Stream

| Surface | Status | Content-Length | Content-Range | Accept-Ranges | Content-Type |
|---------|-------:|---------------:|---------------|---------------|--------------|
| Local backend | 200 | 17,863,782 | `bytes 0-17863781/17863782` | `bytes` | `audio/mpeg` |
| Local frontend proxy | 200 | 17,863,782 | `bytes 0-17863781/17863782` | `bytes` | `audio/mpeg` |
| Public | 200 | 17,863,782 | `bytes 0-17863781/17863782` | `bytes` | `audio/mpeg` |

The retained storage contained three audio candidates. Exactly one candidate had length 17,863,782, proving the no-Range response length matches the stored full resource without exposing its storage key.

### Range Stream

Request: `Range: bytes=0-1023`. Expected full-resource denominator: 17,863,782.

| Surface | Status | Content-Length | Content-Range values | Result |
|---------|-------:|---------------:|----------------------|--------|
| Local backend | 206 | 1,024 | Two identical `bytes 0-1023/17863782` field lines | PASS against WI length/status criteria |
| Local frontend proxy | 206 | 1,024 | One merged line containing the same value twice | PASS against WI length/status criteria |
| Public | 206 | 1,024 | One merged line containing the same value twice | PASS against WI length/status criteria |

All emitted `Content-Range` values use the retained full-resource length. Duplicate emission is documented under Risks and was not changed because product changes are outside WI-023.

### Anonymous Static Original Denial

| Surface | Status | Result |
|---------|-------:|--------|
| Local backend | 401 | PASS |
| Local frontend proxy | 401 | PASS |
| Public | 401 | PASS |

The direct static path was resolved internally from retained storage only for this status probe. The original storage key was not printed or recorded.

## Commands & Outputs

- `git add -- <39 explicit authorized paths>` -> 39 expected and staged; no extra path.
- `git diff --cached --check` -> exit 0.
- `git commit ...` -> created `109112809523c0820aab2a408a355faa21b7833e` on `codex/p1-acceptance-hardening`.
- `git worktree add -b codex/client-demo-stable C:\Users\jm991\Desktop\project\ATStudio-client-demo-stable <commit>` -> exact branch/worktree created and clean.
- `npm.cmd ci` in stable `frontend/` -> exit 0; 309 packages installed; tracked status remained clean.
- Old `scripts/acceptance/status.ps1` -> one owned service for each role.
- Old `scripts/acceptance/stop.ps1` -> all owned process trees stopped; ports closed.
- Stable `scripts/acceptance/start.ps1` -> exit 0; readiness completed.
- Stable `scripts/acceptance/status.ps1` -> running with one owned service for each role.
- Status-only `Invoke-WebRequest` probes -> all required SPA/API statuses 200; static original statuses 401.
- `curl.exe -sS -D - -o NUL` full and Range probes -> full 200/17,863,782 and Range 206/1,024 on local backend, local frontend proxy, and public surfaces.

### Non-mutating Probe Retries

- An initial PowerShell preservation summary used an unsupported `??` operator and failed during parsing before execution; the PowerShell 5-compatible retry passed.
- The first stream precondition expected one stored audio candidate and stopped after observing three; no stream or storage mutation occurred.
- A strict public Range check initially rejected the merged duplicate `Content-Range`. Raw backend, Vite proxy, and public headers were then inspected; the final probe preserved all values and verified every value against the same full length.

## Tests

- Reused WI-022 full-gate PASS: backend 981 tests with 0 failures and 9 skipped; frontend typecheck, ESLint, 79 Vitest tests, build, changed-file Prettier, documentation validation, and diff integrity all passed.
- WI-023 ran deployment/API smoke only, as required by the handoff.
- No live Provider, payment, email, OAuth, Official Download, or database-mutation probe was run.

## Exact Decision

- Functional acceptance: **PASS**.
- Stable checkpoint/worktree acceptance: **PASS**.
- Lifecycle ownership and preservation acceptance: **PASS**.
- Local/public smoke and full-resource stream acceptance: **PASS**.
- Overall WI-20260715-ATS-023: **PASS with recorded residual risks**.
- WI chain: no blocked successor is listed in the handoff.

## Risks / Rollback

### Risks

- The explicit Range response contains duplicate identical `Content-Range` values. Curl accepts the response and all WI status/length checks pass, but HTTP field duplication should be removed in a separately approved product WI and covered by a multi-value header assertion.
- `npm ci` reported 13 existing dependency audit findings: 1 low, 6 moderate, and 6 high. No lockfile remediation was authorized in this deployment WI.
- The Cloudflare Quick Tunnel URL is temporary and becomes unavailable when the owned tunnel exits.
- Full quality gates were reused from WI-022 rather than rerun; WI-023 was intentionally limited to deployment/API smoke.

### Stop and Rollback

1. Stop only the stable manifest-owned runtime:
   `C:\Users\jm991\Desktop\project\ATStudio-client-demo-stable\scripts\acceptance\stop.ps1 -RuntimeRoot 'C:\Users\jm991\AppData\Local\ATStudio\acceptance-preview-64db91c'`.
2. Verify `status.ps1` reports no owned running service and ports 5173/8080 are closed.
3. Preserve the runtime root, external environment bundle, retained database, and upload storage; do not run database cleanup.
4. If separately approved, remove only `C:/Users/jm991/Desktop/project/ATStudio-client-demo-stable` with `git worktree remove`, then delete only `codex/client-demo-stable` after verifying its exact checkpoint.
5. The development commit can be reverted only through a separately approved non-destructive Git revert; do not reset shared branches.

## Follow-ups

- Create a separate product/test WI to stop manually setting `Content-Range` when the `ResourceRegion` converter already emits it, and assert the header has exactly one value through the real HTTP stack.
- Triage the existing frontend dependency audit findings separately without changing this frozen demo checkpoint.

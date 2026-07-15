# WI-20260715-ATS-023 Stable Client Demo Summary

## Decision

**PASS** - The verified complete Public Listening checkpoint is committed, frozen in a clean stable worktree, and running through the retained acceptance database and upload storage.

## Frozen Checkpoint

| Item | Result |
|------|--------|
| Development branch | `codex/p1-acceptance-hardening` |
| Commit | `109112809523c0820aab2a408a355faa21b7833e` |
| Stable branch | `codex/client-demo-stable` |
| Stable worktree | `C:/Users/jm991/Desktop/project/ATStudio-client-demo-stable` |
| Stable worktree state | Clean at the exact commit |
| Public URL | `https://roland-perry-spatial-feat.trycloudflare.com` |

The commit contains exactly the 39 authorized backend, frontend, test, current-document, REQ, and WI paths. Runtime logs, PID files, credentials, dependencies, and runtime state were not staged or committed.

## Runtime Verification

- The previous owned tunnel, frontend, and backend were stopped through `scripts/acceptance/stop.ps1`; both application ports closed and the previous public URL became unreachable.
- The stable worktree was started through `scripts/acceptance/start.ps1` with the retained repo-external environment bundle and runtime root.
- Final lifecycle state is `running` with exactly one owned tunnel, one owned frontend, and one owned backend.
- The retained environment bundle SHA-256 is unchanged before and after restart.
- Retained storage is unchanged: public storage has 3 files and 26,165,861 bytes; private storage has 0 files and 0 bytes.
- The retained active Track remains available as Track `2`.

## HTTP Results

| Probe | Local | Public |
|------|------:|-------:|
| `/` | 200 | 200 |
| `/api/tracks` | 200 | 200 |
| Full Track stream | 200 / 17,863,782 bytes | 200 / 17,863,782 bytes |
| `Range: bytes=0-1023` | 206 / 1,024 bytes | 206 / 1,024 bytes |
| Anonymous direct static original | 401 | 401 |

The no-Range stream length equals exactly one retained stored audio resource. Every Range `Content-Range` value is `bytes 0-1023/17863782`.

## Residual Risks

- The backend emits the same valid `Content-Range` value twice for a Range response. Vite and Cloudflare merge the duplicate values into one comma-separated header line. Status, body length, and full-resource denominator pass the WI criteria, but header duplication should be removed in a separate product WI.
- `npm ci` reproduced the locked frontend dependencies and reported 13 existing audit findings: 1 low, 6 moderate, and 6 high. No dependency or lockfile change was made in this deployment WI.
- The Cloudflare Quick Tunnel URL is temporary and remains available only while the owned tunnel process is running.

## Stop Procedure

Run the following from the stable worktree to stop only this retained runtime root:

```powershell
.\scripts\acceptance\stop.ps1 `
  -RuntimeRoot 'C:\Users\jm991\AppData\Local\ATStudio\acceptance-preview-64db91c'
```

Do not delete the runtime root, environment bundle, database, or upload storage. Worktree or branch removal requires a separate approved Git operation after the runtime is stopped.

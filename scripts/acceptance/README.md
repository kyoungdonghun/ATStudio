# Acceptance Environment Operator Guide

## Purpose

This directory starts and owns the client-acceptance runtime for the current
ATStudio clone. It exposes only the Vite frontend through a Cloudflare quick
tunnel. Vite proxies `/api` and `/uploads` to the loopback Spring Boot backend.

The generated URL is temporary acceptance access, not production deployment or
release evidence.

## Topology and Fixed Ports

```text
Client
  -> HTTPS Cloudflare quick tunnel
  -> http://127.0.0.1:5173 (Vite)
       -> /api and /uploads proxy
       -> http://127.0.0.1:8080 (Spring Boot)
            -> loopback MySQL
```

The lifecycle currently owns fixed ports `5173` and `8080`. Confirm that both
are free before `start.ps1`. Do not start a second acceptance runtime against
the same ports.

## Prerequisites

- Java 17 or later
- Node.js and npm dependencies installed in `frontend/`
- A current backend build and a validated fresh V1 database
- `cloudflared` on `PATH`, or its exact path supplied with
  `-CloudflaredPath`
- A repo-external runtime root
- A repo-external, ACL-restricted flat JSON backend environment bundle
- Test accounts, test data, and Toss test keys only

The database can be prepared with the guarded
[disposable MySQL bootstrap](../database/README.md). Never point acceptance at
the protected development database merely to bypass setup.

## Backend Environment Bundle

The file must be a flat JSON object with nonblank string values. It must be a
regular non-reparse file outside the repository. Unknown, nested, blank, or
non-string values are rejected.

### Required keys

| Key | Contract |
|---|---|
| `SPRING_DATASOURCE_URL` | Loopback JDBC URL for the prepared acceptance database |
| `SPRING_DATASOURCE_USERNAME` | Acceptance database username |
| `SPRING_DATASOURCE_PASSWORD` | Acceptance database password |
| `JWT_SECRET` | Acceptance-only JWT signing secret |
| `APP_BOOTSTRAP_TEST_USERS_ENABLED` | Must be the exact string `true` |
| `APP_BOOTSTRAP_TEST_USERS_DEFAULT_PASSWORD` | Password for opt-in QA accounts |

### Optional allowlisted keys

Only the following optional names are accepted:

- `TOSS_CLIENT_KEY`
- `TOSS_SECRET_KEY`
- `TOSS_CANCEL_URL`
- `TOSS_CONNECT_TIMEOUT_MILLIS`
- `TOSS_READ_TIMEOUT_MILLIS`
- `PAYMENT_BILLING_KEY_ACTIVE_KEY_ID`
- `PAYMENT_BILLING_KEY_0_ID`
- `PAYMENT_BILLING_KEY_0_SECRET`
- `APP_PAYMENT_SCHEDULER_ZONE`
- `TOSS_BILLING_ISSUE_URL`
- `TOSS_BILLING_CHARGE_URL`
- `TOSS_BILLING_DELETE_URL`
- `TOSS_PAYMENT_LOOKUP_BY_ORDER_ID_URL`
- `TOSS_BILLING_CONNECT_TIMEOUT_MILLIS`
- `TOSS_BILLING_READ_TIMEOUT_MILLIS`
- `PAYMENT_RECONCILIATION_NOTIFICATION_ENABLED`
- `PAYMENT_OPERATIONS_OPERATOR_EMAIL`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `KAKAO_CLIENT_ID`
- `KAKAO_CLIENT_SECRET`
- `NAVER_CLIENT_ID`
- `NAVER_CLIENT_SECRET`
- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MAIL_SMTP_AUTH`
- `MAIL_SMTP_STARTTLS`
- `MAIL_FROM`
- `APP_STORAGE_PUBLIC_PATH`
- `APP_STORAGE_PRIVATE_PATH`
- `APP_STORAGE_RECOVERY_BATCH_SIZE`
- `APP_STORAGE_RECOVERY_MAX_ATTEMPTS`
- `APP_STORAGE_RECOVERY_STALE_SECONDS`
- `APP_STORAGE_RECOVERY_CLAIM_SECONDS`
- `APP_STORAGE_RECOVERY_INTERVAL_MS`

The launcher rejects obsolete names instead of translating them. In
particular, do not add:

- `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET`
- `APP_PAYMENT_PROVIDER`
- `TOSS_CONFIRM_URL`

The launcher creates these runtime values itself after discovering the public
URL; they do not belong in the bundle:

- `SPRING_PROFILES_ACTIVE=acceptance`
- `APP_ACCEPTANCE_ENABLED=true`
- `APP_PUBLIC_BASE_URL=<generated HTTPS quick-tunnel origin>`
- `APP_SECURITY_TRUSTED_CLIENT_IDENTITY_ENABLED=true`
- `APP_SECURITY_TRUSTED_PROXY_ADDRESSES=127.0.0.1,::1`

### Secret-file ACL

Create the bundle under a user-owned directory such as
`$env:LOCALAPPDATA\ATStudio`, not under the clone. On Windows, remove inherited
access and grant only the current account before use:

```powershell
$bundle = "$env:LOCALAPPDATA\ATStudio\acceptance-backend-environment.json"
icacls $bundle /inheritance:r
icacls $bundle /grant:r "$env:USERDOMAIN\$env:USERNAME:(F)"
icacls $bundle
```

Review the final ACL output. Never print the file contents, pass secret values
as command-line arguments, copy the file into an evidence pack, or commit it.
Treat the runtime root and logs as operator-sensitive even though the launcher
does not copy bundle values into its manifest.

## Preflight

Run the launcher contract tests:

```powershell
.\scripts\acceptance\test-dry-run.ps1
.\scripts\acceptance\test-backend-environment.ps1
```

Confirm ports are free:

```powershell
Get-NetTCPConnection -State Listen -LocalPort 5173,8080 -ErrorAction SilentlyContinue
```

Inspect the secret-free plan. `-DryRun` does not load the backend bundle, start
processes, create a manifest, or make a tunnel:

```powershell
$runtime = "$env:LOCALAPPDATA\ATStudio\acceptance-v1"
.\scripts\acceptance\start.ps1 `
  -RuntimeRoot $runtime `
  -DryRun
```

## Start, Status, and Stop

Start only after the database has passed its V1 manifest check:

```powershell
$runtime = "$env:LOCALAPPDATA\ATStudio\acceptance-v1"
$bundle = "$env:LOCALAPPDATA\ATStudio\acceptance-backend-environment.json"

.\scripts\acceptance\start.ps1 `
  -RuntimeRoot $runtime `
  -BackendEnvironmentPath $bundle
```

When `cloudflared` is not on `PATH`, add
`-CloudflaredPath "C:\path\to\cloudflared.exe"`.

The lifecycle starts the tunnel first, validates exactly one
`https://*.trycloudflare.com` origin, then loads the backend bundle and starts
backend and frontend. The bundle is injected only into the backend process.
Readiness requires all four targets to return HTTP `2xx` or `3xx`:

- `http://127.0.0.1:5173`
- `http://127.0.0.1:8080/api/tracks`
- the generated public origin
- the generated public `/api/tracks`

Check owned-process state without reading secrets:

```powershell
.\scripts\acceptance\status.ps1 -RuntimeRoot $runtime
```

Stop the runtime at the end of the acceptance window:

```powershell
.\scripts\acceptance\stop.ps1 -RuntimeRoot $runtime
```

Stop validates process ownership before terminating the tunnel, frontend, and
backend process trees. It then verifies ports `5173` and `8080` are closed and
records the stopped state. A repeated stop is safe.

## Cleanup and Failure Handling

- On startup failure, the launcher performs best-effort cleanup of only the
  process records it created.
- The public URL should become unreachable after stop. If it remains reachable,
  inspect the runtime manifest and process ownership before taking any manual
  action.
- Runtime manifests and logs remain outside the repository. Remove their exact
  runtime directory only after processes are stopped and evidence no longer
  needs to be retained.
- Drop only the exact disposable MySQL database used for the rehearsal through
  `scripts/database/bootstrap-disposable-mysql.ps1 -Action Drop`.
- Never delete or recreate `atstudio`, a system schema, an unrelated database,
  or an unrelated process as acceptance cleanup.

## Acceptance Boundaries

- Use Toss test credentials only. A key that cannot be proven to be a test key
  blocks payment mutation tests.
- Do not send mail to real users. Use an isolated SMTP sink or an explicitly
  approved test inbox.
- The Cloudflare URL is ephemeral and may change on every run.
- OAuth provider consoles must separately allow the generated callbacks before
  social login can be claimed as tested.
- Passing this lifecycle proves a controlled acceptance environment. Production
  secret management, retained-data migration, deployment, monitoring, and
  release approval remain separate gates in `docs/SR/SR-93.md`.

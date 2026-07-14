---
version: 1.0
last_updated: 2026-07-15
project: ATS
owner: se
category: evidence-pack
status: stable
related_wi: WI-20260714-ATS-040
---

# Evidence Pack: WI-20260714-ATS-040

## Summary (one-liner)

- Added a required repo-external backend environment bundle, fail-closed flat-JSON validation, and spawn-scoped backend injection that suppresses inherited backend variables from tunnel and frontend children.

## Scope / DoD Check

- [x] Non-dry-run start refuses a missing backend bundle path before any spawn.
- [x] Bundle loading accepts only a regular, non-reparse file outside the repository.
- [x] Malformed, non-object, nested, unknown-name, blank-value, missing-required, and disabled-bootstrap bundles fail closed.
- [x] Tunnel spawn occurs before bundle loading.
- [x] Only backend spawn receives bundle names and values; launcher environment is restored before frontend spawn.
- [x] Tunnel and frontend explicitly suppress all 42 backend allowlist names inherited from the parent launcher.
- [x] Manifest, status, dry-run, launch arguments, and validation errors contain no bundle path, body, or values.
- [x] Existing lifecycle cleanup and dry-run contracts remain passing.
- [x] Temporary synthetic fixtures were created outside the repository and removed.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approved execution, security, and traceability baseline |
| 0 | `docs/standards/development-standards.md` | Script implementation and test evidence requirements |
| 1 | `docs/policies/security-policy.md` | External secret storage and logging minimization policy |
| 1 | `docs/policies/quality-gates.md` | Regression, rollback, and evidence gate |
| REQ | `deliverables/user/REQ-20260714-ATS-001.md` | Approved acceptance-hardening scope |
| WI | `deliverables/agent/WI-20260714-ATS-015-evidence-pack.md` | Acceptance profile and required external configuration |
| WI | `deliverables/agent/WI-20260714-ATS-017-evidence-pack.md` | Lifecycle ownership, cleanup, and dry-run contracts |
| WI | `deliverables/agent/WI-20260714-ATS-022-evidence-pack.md` | Prior acceptance runtime findings and redaction boundary |
| WI | `deliverables/agent/WI-20260714-ATS-039-evidence-pack.md` | Immediate dependency and bounded QA result |
| Context | `src/main/resources/application-acceptance.yml` | Required acceptance placeholders |
| Context | `src/main/java/com/atstudio/atstudio/config/AcceptanceStartupGuard.java` | Backend startup refusal contract |

**Injection Rules Applied**:

- Rule source: `AGENTS.md` and `deliverables/agent/WI-20260714-ATS-040-handoff.md`
- Assignee: `se`
- Task type: acceptance lifecycle security implementation
- Ownership: acceptance PowerShell files and WI-040 deliverables only

## Environment Variable Contract

Required names (6):

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`
- `APP_BOOTSTRAP_TEST_USERS_ENABLED`
- `APP_BOOTSTRAP_TEST_USERS_DEFAULT_PASSWORD`

Optional payment/Toss names (16):

- `APP_PAYMENT_PROVIDER`
- `TOSS_CLIENT_KEY`
- `TOSS_SECRET_KEY`
- `TOSS_CONFIRM_URL`
- `TOSS_CANCEL_URL`
- `TOSS_CONNECT_TIMEOUT_MILLIS`
- `TOSS_READ_TIMEOUT_MILLIS`
- `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET`
- `TOSS_BILLING_ISSUE_URL`
- `TOSS_BILLING_CHARGE_URL`
- `TOSS_BILLING_DELETE_URL`
- `TOSS_PAYMENT_LOOKUP_BY_ORDER_ID_URL`
- `TOSS_BILLING_CONNECT_TIMEOUT_MILLIS`
- `TOSS_BILLING_READ_TIMEOUT_MILLIS`
- `PAYMENT_RECONCILIATION_NOTIFICATION_ENABLED`
- `PAYMENT_OPERATIONS_OPERATOR_EMAIL`

Optional OAuth names (6):

- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `KAKAO_CLIENT_ID`
- `KAKAO_CLIENT_SECRET`
- `NAVER_CLIENT_ID`
- `NAVER_CLIENT_SECRET`

Optional mail names (7):

- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MAIL_SMTP_AUTH`
- `MAIL_SMTP_STARTTLS`
- `MAIL_FROM`

Optional storage names (7):

- `APP_STORAGE_PUBLIC_PATH`
- `APP_STORAGE_PRIVATE_PATH`
- `APP_STORAGE_RECOVERY_BATCH_SIZE`
- `APP_STORAGE_RECOVERY_MAX_ATTEMPTS`
- `APP_STORAGE_RECOVERY_STALE_SECONDS`
- `APP_STORAGE_RECOVERY_CLAIM_SECONDS`
- `APP_STORAGE_RECOVERY_INTERVAL_MS`

Contract totals: required 6, optional 36, allowlisted 42, unique 42. Values must be nonblank JSON strings. `APP_BOOTSTRAP_TEST_USERS_ENABLED` must equal `true`.

## Evidence Pointers

- `scripts/acceptance/start.ps1:5` - accepts the external bundle path; lines 26-27 refuse a missing path before non-dry-run startup.
- `scripts/acceptance/AcceptanceLifecycle.psm1:9` - exact required and optional allowlists.
- `scripts/acceptance/AcceptanceLifecycle.psm1:107` - external regular-file, repository-boundary, JSON shape, allowlist, value, and required-name validation.
- `scripts/acceptance/AcceptanceLifecycle.psm1:394` - child spawn suppresses inherited backend names, applies role-specific values, and restores the launcher environment in `finally`.
- `scripts/acceptance/AcceptanceLifecycle.psm1:767` - dry-run reports counts and external-source policy without a path or names.
- `scripts/acceptance/AcceptanceLifecycle.psm1:923` - tunnel suppression precedes bundle loading at line 937; backend-only merge and spawn are at lines 941-954; frontend suppression is at line 967.
- `scripts/acceptance/test-dry-run.ps1:160` - synthetic dry-run path non-disclosure assertion.
- `scripts/acceptance/test-backend-environment.ps1:71` - safe failure helper; refusal matrix starts at line 134; spawn isolation and ordering checks follow.
- `deliverables/user/WI-20260714-ATS-040-summary.md` - user-facing result.
- `deliverables/agent/WI-20260714-ATS-040-evidence-pack.md` - this reproducibility record.

## Load and Spawn Order

1. Suppress all 42 backend names and spawn tunnel.
2. Wait for and validate the quick-tunnel public base URL; persist only the existing redacted manifest fields.
3. Read and validate the external bundle.
4. Merge common acceptance variables plus bundle values in memory.
5. Suppress inherited backend names, apply the merged environment, spawn backend, and restore the launcher environment in `finally`.
6. Suppress all backend names again and spawn frontend with common non-secret acceptance variables only.

The bundle path is not included in child arguments or environments. Bundle names and values are not added to the manifest, status result, dry-run result, or process service records.

## Commands & Outputs

- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\acceptance\test-dry-run.ps1`
  - PASS; 10 lifecycle/parser/dry-run checks.
  - PSScriptAnalyzer: `not-installed`.
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\acceptance\test-backend-environment.ps1`
  - PASS; 7 reported focused check groups.
- Module allowlist inventory command
  - PASS: required 6, allowed 42, unique 42.
- `git diff --check -- scripts/acceptance/start.ps1 scripts/acceptance/AcceptanceLifecycle.psm1 scripts/acceptance/test-dry-run.ps1 scripts/acceptance/test-backend-environment.ps1`
  - PASS: exit 0, no output.
- `Select-String -LiteralPath <owned PowerShell paths> -Pattern '[ \t]+$'`
  - PASS: 0 trailing-whitespace matches, including untracked files.
- Temporary-directory inventory under the system temp root
  - PASS: 0 `atstudio-wi040-*` or `atstudio-acceptance-dry-run-tests-*` directories remained.
- `Get-Process -Name cloudflared -ErrorAction SilentlyContinue`
  - Observed count: 0 after verification.

## Tests

- Bundle validation covered: valid external flat JSON, missing path/file, repository-internal path, directory path, malformed JSON, nested value, unknown key, blank value, missing required name, and disabled bootstrap.
- Isolation covered parent pre-existing backend values, tunnel suppression, backend-only value injection, launcher restoration, frontend suppression, path non-forwarding, and exact tunnel -> bundle load -> backend -> frontend order.
- All fixture values were generated at runtime from synthetic GUID-based markers in a unique system-temp directory. Tests emitted no fixture values and removed the directory in `finally`.
- No real child process was started: `Start-Process` and lifecycle spawn dependencies were mocked for non-dry-run path coverage.

## State Safety

- No Spring Boot, Vite, Cloudflare tunnel, DB, Toss/provider, payment, OAuth, mail, or storage operation was started or called.
- No real secret, secret file, JDBC URL, bootstrap password, token, or provider credential was created, printed, persisted, or copied into repository evidence.
- No unrelated shared-worktree file was edited, reverted, cleaned, staged, or committed.
- No staging or commit operation occurred.

## Risks / Rollback

- Risks:
  - The focused suite proves process-environment behavior through PowerShell mocks; a later separately approved live acceptance run remains responsible for end-to-end OS child inheritance and application startup.
  - PSScriptAnalyzer was unavailable; parser coverage and focused behavioral tests passed.
- Rollback:
  - Revert only the WI-040 edits in `scripts/acceptance/start.ps1`, `scripts/acceptance/AcceptanceLifecycle.psm1`, and `scripts/acceptance/test-dry-run.ps1`.
  - Remove `scripts/acceptance/test-backend-environment.ps1` and the two WI-040 deliverables.
  - Preserve all unrelated concurrent application, frontend, documentation, runtime-log, and deliverable changes.

## Follow-ups

- Chain trigger: WI-040 blocks WI-20260714-ATS-041; MA should create its handoff packet and delegate it immediately under the approved REQ plan.

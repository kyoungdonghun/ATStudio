
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A058: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a058). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# Evidence Pack: WI-20260817-ATS-030

## Summary

- Created and retained one guarded loopback rehearsal database from the current schema/seed baseline, booted Spring with `ddl-auto=validate`, performed safe live API checks, and shut down only owned rehearsal processes.

## Scope / DoD Check

- [x] Confirmed the required branch, clean tracked worktree state before this WI, and pre-existing client listeners on `5173` and `8080` without sending a request to them.
- [x] Reserved and used distinct rehearsal-only ports `18080`, `18081`, and `18082`; each was unoccupied before start and closed after cleanup.
- [x] Guarded a fresh loopback disposable database name before credential loading or connection.
- [x] Created and independently validated one current `schema.sql -> seed.sql` rehearsal baseline with the 43-table recorded manifest.
- [x] Booted Spring on the rehearsal backend port with `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`; application-ready was reached after JPA initialization.
- [x] Confined storage recovery configuration to the newly created DB and storage root. Scheduler infrastructure initialized within that process only.
- [x] Kept provider and SMTP endpoints fail-closed on unbound loopback values; no provider/payment/refund/renewal/settlement/SMTP action was requested.
- [x] Performed live public catalogue, tags, public capabilities, unauthenticated session, and repeated protected mutation API checks against the rehearsal backend only.
- [x] Stopped owned rehearsal backend/static-proxy/headless-browser processes and retained the rehearsal DB/storage.
- [ ] Browser DOM checks: BLOCKED by local Chrome DevTools WebSocket HTTP 500.
- [ ] Live playable-track detail/waveform: BLOCKED because the current schema/seed clone has no track records.
- [ ] Full authenticated-session and UI cancellation/retry: NOT EXECUTED; password login and test-user bootstrap remained disabled by design.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and approved-WI execution boundary |
| 0 | `docs/standards/development-standards.md` | Two-set deliverables and evidence-pointer requirements |
| 1 | `docs/policies/security-policy.md` | Secret/PII handling and external configuration rules |
| 1 | `docs/policies/quality-gates.md` | Runtime and quality evidence expectations |
| Context | `deliverables/user/REQ-20260817-ATS-010.md` | Approved rehearsal context |
| Context | `deliverables/user/WI-20260817-ATS-029-summary.md` | Prior live-boot blocker and regression baseline |
| Context | `deliverables/agent/WI-20260817-ATS-030-handoff.md` | Exact scope, forbidden actions, and DoD |

## Evidence Pointers

- `scripts/database/bootstrap-disposable-mysql.ps1:1-27,170-283`: supported guarded bootstrap entry point; preflight occurs before credentials/connector use and `Create` validates the resulting target.
- `scripts/database/DisposableMysqlBootstrap.java:29-54,109-145,220-275`: loopback/name guards, current 43-table expectation, schema/seed application order, and recorded manifest checks.
- `src/main/resources/application.yml:13-21,82-98,103-131`: Hibernate validate, isolated storage environment overrides, and provider/mail configuration inputs.
- `src/main/java/com/atstudio/atstudio/service/storage/StorageMutationRecoveryService.java:35-48`: startup recovery and scheduled recovery implementation evaluated only inside the clone boundary.
- `src/main/java/com/atstudio/atstudio/service/SubscriptionScheduler.java:44-60`: payment jobs are cron-based; the bounded run was outside its scheduled time and provider endpoints were fail-closed.
- `src/main/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunner.java:35-39`: test-user bootstrap is opt-in and was disabled for this run.
- `frontend/vite.config.ts:68-93`: development proxy is fixed to the client backend port; the owned runtime static proxy was used instead so no request reached that boundary.
- `frontend/src/router/ProtectedRoute.tsx:28-59` and `frontend/src/utils/loginReturn.ts:57-67`: source contract for protected-route login return behavior. Browser runtime validation remains blocked.
- `%LOCALAPPDATA%\ATStudio\release-rehearsal-WI-20260817-ATS-030\runtime-manifest.json`: secret-free owned-runtime manifest; final state is `stopped`. Do not publish environment bundle contents.

## Commands and Safe Results

| Command / operation | Result |
|---|---|
| `git branch --show-current`, `git rev-parse --short HEAD`, safe listener query | Correct rehearsal branch confirmed; existing client ports were present, rehearsal ports were initially free. |
| `bootstrap-disposable-mysql.ps1 -Action Preflight` | Final intended target passed guarded name, loopback, schema-order, 43-table source statement count, and recorded-manifest checks. An earlier invalid-name preflight was refused before a DB connection. |
| `bootstrap-disposable-mysql.ps1 -Action Create` | PASS: schema then seed applied; manifest matched 43 tables, 511 columns, 175 indexes, 91 foreign keys, 6 plans, and 6 plan keys. |
| `bootstrap-disposable-mysql.ps1 -Action Validate` | PASS: repeated independent manifest validation matched the same baseline. |
| Isolated `gradlew.bat bootRun` with external process environment | PASS: Spring reached ready on `127.0.0.1:18080` after Hibernate/JPA initialization with `ddl-auto=validate`. Sensitive values and JDBC value are intentionally omitted. |
| `GET` catalogue keyword, tags, public capabilities | PASS: each returned HTTP 200 from the rehearsal backend. |
| `GET` catalogue through owned static proxy | PASS: HTTP 200; static proxy target was loopback rehearsal backend only. |
| `GET` current session without credentials | PASS: HTTP 401; password capability reported disabled as configured. |
| Two unauthenticated `POST` requests to a protected playlist mutation | PASS: both returned HTTP 401 before mutation; no replay/login claim is made. |
| Headless Chrome DevTools WebSocket connection | BLOCKED: Chrome returned HTTP 500 both before and after an owned-browser restart. No browser DOM result was used. |
| Rehearsal listener cleanup | PASS: only manifest-owned backend/static-proxy and rehearsal-profile Chrome processes were stopped. Ports `18080`, `18081`, and `18082` were closed; DB/storage retained. |

## Evidence by Layer

| Layer | Verified | Not claimed |
|---|---|---|
| UI | Static SPA proxy was listening on an isolated port. | Browser rendering, navigation, and protected-route return behavior; Chrome CDP was blocked. |
| API | Public catalogue/tag/capabilities returned 200; unauthenticated session and protected mutation returned 401. | Successful user login, refresh/replay, or a completed UI mutation. |
| Persistence / runtime | Manifest baseline passed and Spring booted with Hibernate validate on the clone. | Any claim about the protected development DB, client DB, or live playable-track records. |
| Provider | No provider/payment/refund/renewal/settlement request was made; process configuration was loopback fail-closed. | A provider response, provider availability, or external-effect rehearsal. |

## Risks / Rollback

- The retained schema/seed clone has no track data, so the valid schema proof does not prove playback response availability.
- Chrome DevTools HTTP 500 is a local tool/runtime blocker, not an application defect determination.
- No external-effect stage is authorized from this evidence.
- Rollback completed for runtime processes only. Do not delete the retained rehearsal DB, runtime root, or storage without a new explicit approval.

## Follow-ups

- Provision an approved read-only clone containing representative non-sensitive track rows and isolated storage fixtures, then rerun track detail/duration/waveform checks.
- Repair or replace the local browser automation boundary and rerun actual SPA catalogue/protected-route checks through the isolated static proxy.
- Enable a clone-only test user only under a new approved scope if successful login and protected mutation cancellation/retry must be demonstrated.

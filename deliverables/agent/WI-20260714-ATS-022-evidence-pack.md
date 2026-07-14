# Evidence Pack: WI-20260714-ATS-022

## Summary (one-liner)

- Exercised the approved local/public Cloudflare acceptance matrix against a disposable MySQL database, proved teardown, and retained a NO-SHARE decision because two-client separation and an active-subscriber success path remain unverified.

## Scope / DoD Check

- DoD items:
  - [x] Started the application only with the explicit `acceptance` profile, external child-process configuration, and a WI-021-owned disposable MySQL database.
  - [x] Proved local/public frontend and `/api/tracks` readiness.
  - [x] Proved same-origin `/api` and `/uploads`, a public Playlist thumbnail, and safe media headers.
  - [x] Proved unknown Host refusal and that alternating spoofed forwarding headers did not bypass the login rate-limit bucket.
  - [x] Proved anonymous and authenticated denial for source Track audio and Company Certification static paths.
  - [x] Proved ADMIN login, representative role API/SPA shell navigation, logout, and refresh-token replay refusal.
  - [x] Proved safe Toss success/fail callback shell readiness without payment or provider mutation.
  - [x] Proved ordered/idempotent stop, closed ports, no owned processes, unreachable public origin, disposable DB removal, and helper-class cleanup.
  - [ ] Two real external clients were not available to prove distinct effective identities.
  - [ ] Active-subscriber navigation could not pass because the fresh schema contains no subscription plan seed for bootstrap alignment.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Governance, approval, security, and traceability baseline |
| 0 | `docs/standards/development-standards.md` | Integration test and evidence requirements |
| 0 | `docs/standards/documentation-standards.md` | Two-set deliverable format |
| 0 | `docs/standards/glossary.md` | Canonical Track, subscriber, and WI terms |
| 1 | `docs/policies/security-policy.md` | Secret, protected media, and auth handling |
| 1 | `docs/policies/quality-gates.md` | Blocked-check and teardown evidence rules |
| 2 | `deliverables/user/REQ-20260714-ATS-001.md` | Approved acceptance and destructive-action boundary |
| 2 | `docs/SR/SR-42.md` | Single frontend tunnel and same-origin proxy topology |
| WI | `deliverables/agent/WI-20260714-ATS-009-evidence-pack.md` | Canonical Playlist thumbnail and response-header contract |
| WI | `deliverables/agent/WI-20260714-ATS-010-evidence-pack.md` | Company Certification quarantine and static denial contract |
| WI | `deliverables/agent/WI-20260714-ATS-011-evidence-pack.md` | Logout and refresh revocation contract |
| WI | `deliverables/agent/WI-20260714-ATS-014-evidence-pack.md` | Social callback ordering boundary |
| WI | `deliverables/agent/WI-20260714-ATS-015-evidence-pack.md` | Acceptance profile, public-base, callback, and secret guard |
| WI | `deliverables/agent/WI-20260714-ATS-016-evidence-pack.md` | Host, proxy header, client identity, and rate-limit boundary |
| WI | `deliverables/agent/WI-20260714-ATS-017-evidence-pack.md` | Acceptance lifecycle ownership and cleanup contract |
| WI | `deliverables/agent/WI-20260714-ATS-020-evidence-pack.md` | Frontend route/proxy prerequisites |
| WI | `deliverables/agent/WI-20260714-ATS-021-evidence-pack.md` | Approved disposable MySQL runner and schema ordering |
| WI | `deliverables/agent/WI-20260714-ATS-035-evidence-pack.md` | Closed `tracks.waveform_data` Hibernate validation blocker |

**Injection Rules Applied**:

- Rule source: `AGENTS.md` and `deliverables/agent/WI-20260714-ATS-022-handoff.md`
- Assignee: `qa-integ`
- Task type: public acceptance integration and teardown
- Required context: Tier 0, security/quality policies, approved REQ, all handoff evidence pointers, acceptance scripts, Vite proxy, acceptance configuration, and focused security tests

## Evidence Pointers

### Files created

- `deliverables/user/WI-20260714-ATS-022-summary.md` - Korean user-facing result and NO-SHARE decision.
- `deliverables/agent/WI-20260714-ATS-022-evidence-pack.md` - this redacted reproducibility record.

### Runtime contracts exercised

- `scripts/acceptance/start.ps1` - one allowed successful retry after an initial QA-wrapper JDBC URL assembly error.
- `scripts/acceptance/status.ps1` - ownership checks without exposing the public origin.
- `scripts/acceptance/stop.ps1` - ordered and idempotent teardown.
- `scripts/acceptance/AcceptanceLifecycle.psm1` - readiness and process ownership implementation.
- `frontend/vite.config.ts` - exact Host allowlist, same-origin proxies, and forwarding-header rewrite.
- `src/main/resources/application-acceptance.yml` - `validate` schema mode, loopback binding, external secrets, and derived callbacks.
- `src/main/java/com/atstudio/atstudio/security/AcceptanceHostFilter.java` - backend unknown Host refusal.
- `src/main/java/com/atstudio/atstudio/security/TrustedClientIdentityResolver.java` - bounded loopback-proxy identity assertion.
- `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java` - source audio and Company Certification static denial.

### Redacted run identity

- Ephemeral public host SHA-256 prefix: `1f404a07d9f4`.
- Disposable database SHA-256 prefix: `e231c50f01fc`; name matched `^ats_wi021_\d{8}_[a-z0-9]{8}$`.
- Exact public/JDBC URLs, database name, credentials, bootstrap password, JWT, Toss keys, tokens, client IP, PIDs, and runtime logs were not copied into repository artifacts.
- Repo-external lifecycle logs remain under the launcher-owned `%LOCALAPPDATA%` runtime and were not modified or copied.

## Commands & Outputs

### Preflight and startup

- Approved runner: `run-disposable-mysql-rehearsal.ps1 -KeepDatabase`.
  - MySQL 8.0.45; current `schema.sql` plus WI-021 manual-patch order applied.
  - Contract result: `PASS`; drop deferred for acceptance.
- Initial start attempt:
  - The external QA wrapper incorrectly supplied only the disposable database name as `SPRING_DATASOURCE_URL`.
  - Backend readiness failed before acceptance; launcher cleanup left owned services, `5173`, `8080`, and `cloudflared` at zero.
  - No product failure or URL-ready claim is attributed to this attempt.
- Single allowed retry:
  - Derived a complete loopback JDBC URL by replacing only the local URL database path while preserving host, port, and query; the value was never printed or persisted.
  - `start.ps1` passed MySQL connection, Hibernate `validate`, acceptance startup guard, bootstrap, and four readiness URLs.
  - The parent orchestration cell returned non-zero after launcher success, but the manifest, three owned services, bound ports, and runtime probes independently proved the acceptance environment was ready.

### Local/public status matrix

| Check | Result |
|------|--------|
| Local frontend | 200 |
| Public frontend | 200 |
| Local backend `/api/tracks` | 200 |
| Public same-origin `/api/tracks` | 200 |
| Local/public Playlist thumbnail | 200 / 200; body lengths equal |
| Thumbnail headers | `image/jpeg`, `nosniff`, `Cross-Origin-Resource-Policy: same-origin` |
| Anonymous source audio | 401 |
| Anonymous Company Certification static path | 401 |
| ADMIN source audio | 403 |
| ADMIN Company Certification static path | 403 |
| Unknown backend Host | 400 |
| Unknown Vite Host | 403 |
| Unknown public Host | 403 |

### Auth, navigation, identity, and Toss

| Check | Result |
|------|--------|
| ADMIN login and `/api/users/me` role | 200-equivalent success / `ADMIN` |
| ADMIN `/api/admin/stats` | 200 |
| ADMIN SPA route shell | 200 |
| ADMIN logout / refresh replay | 204 / 401 |
| Subscriber fixture login and role | 200-equivalent success / `USER` |
| Subscriber SPA route shell | 200 |
| Subscriber Playlist/subscription APIs | 403 / 403; no seeded active plan |
| Subscriber logout / refresh replay | 204 / 401 |
| Alternating spoofed forwarding headers | No bucket bypass; continuation reached 429 after 16 non-429 probes |
| Two distinct external egress clients | BLOCKED: only one external egress was available |
| Toss checkout success/fail SPA shells | 200 / 200 |
| Toss provider/payment mutation | Not executed by scope |

### Teardown

- `stop.ps1` first owned teardown:
  - three owned process trees stopped;
  - `portsClosed=true`;
  - `publicUrlUnreachable=true`.
- Repeated `stop.ps1` calls:
  - state `stopped`;
  - no owned process tree remained;
  - idempotency passed.
- Independent final state:
  - listeners on `5173`: 0;
  - listeners on `8080`: 0;
  - `cloudflared` processes: 0;
  - manifest-owned services: 0.
- Approved runner drop-only cleanup:
  - `drop.database: OK`;
  - `cleanup.database.exists: 0`;
  - `RESULT: PASS`.
- Generated helper cleanup:
  - removed `DisposableMysqlRehearsal*.class`: 3;
  - remaining: 0.

## Tests

- Live acceptance smoke: PASS for the completed matrix above.
- Teardown and disposable DB cleanup: PASS.
- Required limitations:
  - two-external-client identity separation: BLOCKED by single egress;
  - active-subscriber successful navigation: BLOCKED by absent subscription plan seed;
  - authenticated browser interaction: not run; role APIs and SPA shells only;
  - Toss SDK, provider callbacks, billing auth, payment, email, and live provider mutation: intentionally not run.

## Client-Sharing Decision

- **NO-SHARE**.
- The ephemeral URL was not delivered to the client and is now unreachable.
- WI-022 must not be treated as URL-ready until the required blocked checks are completed in a separately safe acceptance run.

## Risks / Rollback

- Risks:
  - A single-egress test cannot prove external-client separation even though spoofed forwarding headers shared one rate-limit bucket.
  - Fresh-schema bootstrap users do not provide subscriber success coverage without plan seed data.
  - SPA shell 200 does not replace authenticated visual navigation assertions.
  - Callback shell readiness does not prove provider registration or payment behavior.
- Rollback:
  - No application code, existing database, existing runtime log, provider state, payment, or email state changed.
  - Runtime rollback is complete: tunnel/processes/ports are closed and the disposable database is absent.
  - Remove only the two WI-022 deliverables if this documentation must be withdrawn.

## Follow-ups

- Keep WI-022 dependents blocked from a client-ready verdict.
- A future approved acceptance run needs two independent egress clients and an explicitly seeded disposable subscriber plan fixture.
- Preserve the same no-payment/no-provider-mutation boundary for that follow-up.

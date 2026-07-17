# Evidence Pack: WI-20260717-ATS-018

## Summary (one-liner)
- Completed the V1 acceptance lifecycle and read-only role/UI smoke run, leaving the manifest-owned backend, frontend, and tunnel running after the WI-019 launcher correction.

## Scope / DoD Check
- DoD items:
  - [x] Both acceptance lifecycle contract tests passed.
  - [x] Startup blockers were identified in sequence: a stale external database, then a fail-closed V2 environment allowlist mismatch.
  - [x] Final startup succeeded after WI-019 corrected the launcher allowlist.
  - [x] Local frontend, local API, public frontend, and public proxied API returned HTTP 200.
  - [x] Public, basic, subscriber, grace, business, and admin read-only authorization paths were exercised.
  - [x] Home, tracks empty-state, and six-plan subscription UI states rendered without a browser console error.
  - [x] Startup-log review found one application start, one QA bootstrap ready marker, and no backend, schema-validation, or frontend error marker.
  - [x] Provider, billing, refund, and admin mutations were deliberately not executed.
  - [x] Current runtime ownership, runtime root, public URL, and leave-running state are recorded.
  - [x] This evidence contains no secret, JDBC credential, password, or token value.

## Reference Documents (Tier 0-2)

**Injected Context** (declared by the WI Handoff Packet):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution and ATStudio domain baseline |
| 0 | `docs/standards/development-standards.md` | Development and verification standards |
| 0 | `docs/standards/documentation-standards.md` | Evidence and documentation standards |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/quality-gates.md` | Acceptance and quality gates |
| 1 | `docs/policies/security-policy.md` | Secret-safe runtime evidence |
| 1 | `docs/policies/access-control-policy.md` | Role and authorization boundaries |
| 1 | `docs/policies/execution-policy.md` | Runtime execution and ownership constraints |
| 2 | `docs/design/api-spec.md` | Representative API contracts |
| 2 | `docs/design/db-schema.md` | V1 schema baseline |
| 2 | `docs/client/testing-guide.md` | Client-facing smoke-test flows |
| 2 | `docs/payment/acceptance-test-checklist.md` | Payment acceptance limits and checks |
| 2 | `docs/SR/SR-42.md` | Acceptance runtime topology |
| 2 | `docs/SR/SR-93.md` | Consolidated acceptance scope |

**Injection Rules Applied**:
- Rule source: `AGENTS.md` and `deliverables/agent/WI-20260717-ATS-018-handoff.md`.
- Assignee: `qa-integ`.
- Task type: acceptance lifecycle, cross-layer integration, authorization, and UI smoke verification.
- WI-018 changed only this Evidence Pack and its user-facing summary.

## Startup Findings and Resolution
1. The first startup used a stale external database and failed because `company_certification_audit_logs` was missing.
2. After switching to the current `atstudio` database, startup failed closed because the acceptance launcher did not allowlist the current V2 environment contract.
3. WI-019 corrected the launcher allowlist while retaining application fail-closed behavior.
4. The final startup succeeded with the current database and corrected launcher contract.

Classification:
- The first failure was stale external database state, not an unexplained application startup regression.
- The second failure was acceptance launcher contract drift and was resolved by WI-019.
- The final runtime showed no startup, schema-validation, or frontend error marker in the reviewed logs.

## Evidence Pointers (required)
- WI scope and output contract:
  - `deliverables/agent/WI-20260717-ATS-018-handoff.md`.
- Launcher correction and focused verification:
  - `deliverables/agent/WI-20260717-ATS-019-evidence-pack.md`.
- WI-018 deliverables:
  - `deliverables/agent/WI-20260717-ATS-018-evidence-pack.md`.
  - `deliverables/user/WI-20260717-ATS-018-summary.md`.
- Current runtime root:
  - `C:\Users\jm991\AppData\Local\ATStudio\acceptance-v1-final-20260717`.
- Current public URL:
  - `https://protected-humor-michel-careful.trycloudflare.com`.
- Runtime ownership/state:
  - The current manifest owns the running backend, frontend, and tunnel processes.
  - Leave-running decision: running for continued client acceptance access.
- Source, schema, branch, Git refs/index, and repo-external runtime data were not changed by WI-018.

## Commands & Outputs
- `powershell -ExecutionPolicy Bypass -File scripts/acceptance/test-dry-run.ps1`
  - Result: PASS.
- `powershell -ExecutionPolicy Bypass -File scripts/acceptance/test-backend-environment.ps1`
  - Result: PASS.
- Acceptance startup lifecycle:
  - First attempt: failed against a stale external database because `company_certification_audit_logs` was absent.
  - Current-database attempt: failed closed on the missing V2 launcher allowlist contract.
  - Final attempt after WI-019: succeeded; manifest-owned backend, frontend, and tunnel remain running.
- HTTP readiness:

| Surface | Target | Result |
|---------|--------|--------|
| Local frontend | Application root | 200 |
| Local API | Representative tracks API | 200 |
| Public frontend | Current tunnel root | 200 |
| Public API | Proxied representative tracks API | 200 |

- Startup-log review:

| Marker | Count |
|--------|------:|
| `Started` | 1 |
| QA bootstrap ready | 1 |
| `ERROR` | 0 |
| Schema validation error | 0 |
| Frontend error | 0 |

- `git diff --check`
  - Result: PASS, exit code 0; no whitespace error.
  - Git emitted line-ending normalization warnings for pre-existing working-tree changes; WI-018 did not rewrite those files.

## Role / API Matrix

| Role | Read-only GET surface | Result |
|------|-----------------------|--------|
| Public | Tracks | 200 |
| Public | Subscriptions | 200 |
| Basic | Users/me | 200 |
| Basic | User-subscriptions/me | 403 |
| Basic | Admin stats | 403 |
| Subscriber | User-subscriptions/me | 200 |
| Subscriber | Licenses/me | 200 |
| Subscriber | Whitelist channels | 200 |
| Grace | User-subscriptions/me | 200 |
| Business | Company-certifications/me | 200 |
| Business | Whitelist channels | 200 |
| Admin | Stats | 200 |
| Admin | Payment orders | 200 |
| Admin | Admin whitelist | 200 |
| Admin | Company certifications | 200 |

## Browser Smoke Results
- AT.M home rendered normally.
- Tracks rendered the empty-state normally.
- Subscription page rendered all six plans normally.
- Browser console errors: 0.
- Non-error console observation: React Router future warning only.

## Tests
- Acceptance lifecycle contract tests: 2/2 passed.
- Final process readiness: backend, frontend, and tunnel running under the current manifest.
- Local/public frontend and API readiness: 4/4 returned HTTP 200.
- Role/API checks matched expected allow/deny boundaries, including basic-role 403 responses for subscriber and admin surfaces.
- Provider, billing, refund, and admin mutations: not executed by design.

## Risks / Rollback
- Risks:
  - The Cloudflare quick-tunnel URL is tied to the current local runtime and is not a production endpoint.
  - The run proves representative read-only acceptance behavior only; payment-provider and mutation paths remain unexecuted.
  - Successful acceptance does not establish production readiness or authorize production deployment.
  - React Router emitted a future warning; it was non-fatal and produced no console error.
- Rollback / cleanup:
  - Use the acceptance lifecycle stop operation against only the manifest under `C:\Users\jm991\AppData\Local\ATStudio\acceptance-v1-final-20260717`.
  - Stop only the backend, frontend, and tunnel processes owned by that manifest.
  - Confirm stopped status through the lifecycle status operation; do not kill unrelated processes or delete external credentials/data.

## Follow-ups
- The next approval point is whether to keep the current acceptance runtime available or stop its manifest-owned processes after client verification.
- Any provider, billing, refund, admin mutation, production deployment, or production-readiness decision requires separate scope and approval.

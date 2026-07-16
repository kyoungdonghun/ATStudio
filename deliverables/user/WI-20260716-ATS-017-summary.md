---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: se
category: implementation
status: complete
dependencies:
  - path: REQ-20260716-ATS-002.md
    reason: Approved development-branch remediation scope
  - path: ../agent/WI-20260716-ATS-017-handoff.md
    reason: Implementation and verification contract
  - path: ../agent/WI-20260716-ATS-017-evidence-pack.md
    reason: Reproducible closure evidence
---

# WI-20260716-ATS-017 Summary

## Outcome

All accepted WI-017 code findings are closed on the development branch. The work blocks unsafe whitelist links, fences financial and catalog races, extends bounded provider reconciliation, separates frontend inactive/error/stale states, tests the existing admin payment operations, preserves safe OAuth return targets, masks provider identifiers in admin/client surfaces, and synchronizes the live documentation and client PDF.

No product invariant changed. Public tracks remain fully playable, downloads remain subscription/quota/license controlled, payment remains recurring billing-key card payment, and deployment remains single-server. The frozen client branch/runtime, retained DB/data, providers, secrets, and Git index/remotes were not changed.

## Finding Closure

| Finding | Final status | Result |
|---|---|---|
| F-016-01 | CLOSED | Backend accepts only normalized HTTPS YouTube hosts without credentials or non-standard ports; unsafe retained values are text-only in subscriber/admin UI. |
| F-016-02 | CLOSED | Entitlement correction creation is serialized, duplicate non-terminal requests are rejected, stale execution is blocked, and terminal retry is idempotent. |
| F-016-03 | CLOSED / ENVIRONMENT-CONDITIONAL | Recent DONE reconciliation is bounded and refund-aware; live provider proof remains an environment gate. |
| F-016-04 | CLOSED / ENVIRONMENT-CONDITIONAL | Album/playlist mutations share pessimistic lock paths and order; real retained-MySQL race proof remains an environment gate. |
| F-015-01 | CLOSED | Certification self-service requires USER plus BUSINESS; BUSINESS ADMIN cannot use subscriber self-service. |
| F-016-05 | CLOSED | Player subscription loading, active, inactive, and service-error states are distinct and latest-request fenced. |
| F-016-06 | CLOSED | Track detail, user, user-subscription, and download loaders ignore cancellation and reject stale success/failure. |
| F-016-07 / F-015-05 | CLOSED | Admin settlement/refund/entitlement operations have frontend interaction and backend controller/read contract tests. |
| WI-013 storage risk | CLOSED | Reachable journal completion, compensation retry, and idempotency behavior have focused tests. |
| F-015-02 | CLOSED | Raw provider keys remain server-held; admin APIs/UI/client material use deterministic masked `REF-` support references. |
| F-016-08 | CLOSED | OAuth return targets are internal-only, per-attempt, ten-minute, one-time values preserved through profile completion. |
| F-016-09 / F-015-P3-03 | CLOSED | Router and frontend standards point to the live screen-count contract; managed docs remain 193. |
| F-015-P3-02 | CLOSED | Playlist delete documentation now distinguishes membership physical deletion from parent soft deletion. |

Social-only withdrawal remains `POLICY-PENDING` and was intentionally not implemented.

## Full Verification

- Backend: clean test plus JaCoCo passed with 1,079 tests, 0 failures, 0 errors, and 9 conditional skips. JaCoCo recorded 77.47% line and 59.85% branch coverage. The full Gradle build passed.
- Frontend: production-only and unfiltered audits both reported 0 vulnerabilities; typecheck, ESLint, 43-file/230-test Vitest, V8 coverage, Vite build, and full-tree Prettier all passed.
- Frontend coverage: statements 38.64%, branches 38.64%, functions 32.35%, lines 39.90%.
- Client guide: deterministic 12-page PDF verified with SHA-256 `dfdfc587168aaa45786dc15e2fbf4eb9afb4c07f8d371e2a1a73679ad56e8369`; all pages were rendered and visually inspected.
- Generated TypeScript baseline: `frontend/tsconfig.tsbuildinfo` was restored byte-for-byte to 5,421 bytes and SHA-256 `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A`.

## Release Judgment

The development branch is ready for controlled environment acceptance after the named environment gates are supplied. It is not yet an unconditional production/client-branch release: retained MySQL migration/concurrency/EXPLAIN evidence, live provider/proxy/secret/symlink checks, and the frozen client branch's separate dependency state still require their own environment evidence or promotion decision.

No stage, commit, push, merge, DB/provider call, or client-runtime action was performed.

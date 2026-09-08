---
version: 2.7
last_updated: 2026-09-08
project: ATS
owner: docops
category: reference
status: stable
dependencies:
  - path: ../design/api-spec.md
    reason: Current API count and DTO contracts
  - path: ../design/db-schema.md
    reason: Current DB/entity count
  - path: ../ui/atstudio-front-list.md
    reason: Current screen-count unit
---

# Internal Client Feature Map

This file is excluded from the client PDF.

## Historical Counts

Pre-WI014 recorded snapshot (not recounted). These values are preserved, not
promoted to current counts by WI014. Prior metadata does not establish a
measurement date. Later source/runtime evidence is linked below.

| Surface | Snapshot count | Unit |
|---|---:|---|
| REST API | 149 | Method-level mappings across 25 controller classes |
| Database | 42 | `CREATE TABLE` declarations; also 42 JPA entities |
| Frontend screens | 53 | Distinct visual page UIs, including 2 error screens |
| Router declarations | 57 | 56 path routes plus 1 index redirect |
| Lazy page components | 53 | Every current `createLazyPage(...)` declaration |
| Modal occurrences | 22 | `<Modal>` renders across 17 non-test TSX files |
| SR items | 100 | 82 DONE, 15 OPEN, 2 NOT CONFIRMED, 1 DROPPED |

## Historical Verified V1 Quality Baseline (2026-07-17)

| Gate | Final verified result |
|---|---|
| Backend tests | 1,208 tests, 0 failures/errors, 9 environment-dependent skips |
| Backend JaCoCo | Instruction 85.673%, branch 71.682%, line 85.726%, method 82.931% |
| Frontend tests | 468 tests, 0 failures |
| Frontend coverage | Statements 86.73%, branches 76.98%, functions 85.41%, lines 88.75% |
| Frontend static/build gates | Typecheck, ESLint, Prettier, and build PASS |

## Semantic Hotspots

| Client topic | Current source |
|---|---|
| Billing re-registration amount 0 and flat DTOs | Java billing DTOs, `frontend/src/api/payments.ts`, API spec 6.3.4-6.3.7 |
| Browser-local play history | `playerStore.ts`, `PlayHistoryPage.tsx`; no server Play History contract |
| Shared playback hydration | `POST /api/tracks/batch`, `PlayableTrackService`, ID-only `playerState`/`playHistory` persistence |
| Tag discovery | Repeated Genre/Mood/Instrument/Usage parameters, AND semantics, Usage-first Home fallback |
| ADMIN role safety | Deterministic active-ADMIN lock, role audit, `/users/me` resynchronization |
| ADMIN local subscription correction | Separate `admin_subscription_corrections` workflow; no provider charge/refund |
| Dashboard stats | `GET /api/admin/stats`, `DashboardPage.tsx` |
| Site settings | `COMPANY_CERT_GUIDE`, public read + admin upsert |
| Screen count | `frontend/src/router/index.tsx`, `docs/ui/atstudio-front-list.md` |
| Current tracking SoT | `deliverables/user/` and `deliverables/agent/` |

## Dependency And Environment Boundary

- Official V1 source baseline: `main` in the root checkout; see the [current V1 baseline](../index.md#current-v1-baseline). The retired client worktree and its temporary thumbnail policy are historical, not a second maintained baseline. Vite 6.4.3 is an earlier dependency observation, not a fresh install or audit result.
- The verified coverage values above are observations, not release thresholds.
- The earlier WI-014~021 focused-only checkpoint is historical. Subsequent
  aggregate suites, coverage, static/build gates and bounded desktop checks
  are recorded in the [2026-09-08 development closeout](../SR/SR-93.md#2026-09-08-development-closeout)
  and [dated source/runtime evidence](../payment/index.md#2026-09-08-source-and-runtime).
  No tests were rerun for WI014; those results do not establish complete
  browser acceptance, production deployment or security approval.
- Public runtime evidence is valid only for the current operator-controlled acceptance lifecycle and its newly verified URL. Historical captures are reference-only.
- Retained DB rehearsal, live provider/secrets, production proxy/CORS/monitoring, and final client acceptance remain open environment gates.

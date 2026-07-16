---
version: 2.0
last_updated: 2026-07-16
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

## Exact Current Counts

| Surface | Current count | Unit |
|---|---:|---|
| REST API | 149 | Method-level mappings across 24 REST controllers |
| Database | 41 | `CREATE TABLE` declarations; also 41 JPA entities |
| Frontend screens | 53 | Distinct visual page UIs, including 2 error screens |
| Router declarations | 63 | 62 path routes plus 1 index redirect |
| Lazy page components | 54 | Includes `/playlists/new` modal adapter |
| Modal occurrences | 23 | `<Modal>` renders across 17 non-test TSX files |
| SR items | 92 | 82 DONE, 7 OPEN, 2 NOT CONFIRMED, 1 DROPPED |

## Semantic Hotspots

| Client topic | Current source |
|---|---|
| Billing re-registration amount 0 and flat DTOs | Java billing DTOs, `frontend/src/api/payments.ts`, API spec 6.3.4-6.3.7 |
| Browser-local play history | `playerStore.ts`, `PlayHistoryPage.tsx`; server API retained separately |
| Dashboard stats | `GET /api/admin/stats`, `DashboardPage.tsx` |
| Site settings | `COMPANY_CERT_GUIDE`, public read + admin upsert |
| Screen count | `frontend/src/router/index.tsx`, `docs/ui/atstudio-front-list.md` |
| Current tracking SoT | `deliverables/user/` and `deliverables/agent/` |

## Dependency And Environment Boundary

- Development branch: Vite 6.4.3; production audit 0; unfiltered audit 0.
- Coverage is a low observed baseline, not a threshold: backend branch 59.05%; frontend statements 34.49%, branches 34.00%, functions 27.82%, lines 35.43%.
- Frozen client-demo branch: Vite 6.4.1; production audit 5; unfiltered audit 13 (8 are additional development-toolchain paths). The branch was inspected read-only and not modified.
- Retained DB rehearsal, live provider/secrets, production proxy/CORS/monitoring, and final client acceptance remain open environment gates.

# Project Retrospective — Lessons for Next Project

> **Source**: ATStudio v1 backend completion (2026-03-07)
> **Purpose**: Capture reusable patterns, anti-patterns, and standards worth carrying forward.
> **Audience**: Starting a new project — read before designing architecture or workflow.

---

## Files

| File | Contents |
|------|----------|
| [engineering.md](engineering.md) | Spring Boot / JPA / Security / Testing patterns |
| [process.md](process.md) | Multi-agent workflow, REQ→WI process, memory management |
| [domain-design.md](domain-design.md) | Domain modeling, API design, soft delete, status machines |

---

## Top 5 "Wish We Knew Earlier"

1. **Spring Security rule ordering** — specific rules must precede wildcards. `/api/users/me` before `/api/users/*`. A single ordering mistake silently breaks auth for all users.

2. **WI chain rule** — after completing any WI, immediately check which WI it unblocks and trigger it. Easy to forget mid-session when writing deliverables or committing.

3. **re = pure verifier** — `re` agent only runs and reports. `se` writes the tests. Separating these roles reduces test duplication and confusion over who owns test authorship.

4. **Domain vocabulary lock-in early** — "Playlist" vs "Album" confusion persisted for multiple sessions because the naming wasn't locked in the first REQ. Define domain glossary terms in REQ-001.

5. **API response envelope consistency** — decide on `{ "dataList": [...] }` vs raw array at project start. Retrofitting this across 15+ endpoints mid-project is expensive.

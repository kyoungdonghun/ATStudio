# Key Paths

- `AGENTS.md`: primary operational entry for Codex sessions
- `CLAUDE.md`: primary operational entry for Claude sessions
- `docs/index.md`: live documentation index
- `docs/client/`: client-facing SR intake/testing guides
- `docs/design/`: API, DB, and use case source of truth
- `frontend/src/router/index.tsx`: route map and lazy loading baseline
- `frontend/src/api/client.ts`: auth token/refresh handling
- `frontend/src/pages/auth/`: login/signup/social auth UI
- `frontend/src/pages/subscriber/`: subscriber UX including profile, playlists, downloads
- `src/main/java/com/atstudio/atstudio/service/`: backend business rules

# Current Baseline

- Codex and Claude operate in a dual-entry baseline.
- Live docs, skills, and validation flows are aligned and `validate-docs` passes.
- Client SR intake docs are usable again and no longer depend on fake fixed credentials.
- Auth flows are hardened across backend contracts, frontend UI, and regression tests.
- Public capability flags drive login/signup/reset availability by environment.
- Playlist and subscription flows now use tier-based limits and consistent subscriber-only rules.

# Recent Decisions

- Historical `docs/SR/*` files are treated as records and should not be rewritten for current behavior.
- `SR-70` to `SR-78` are historical records to be tracked as-is when committed.
- Playlist creation has no admin-specific bypass; it follows the same active subscription rules as other subscriber features.
- `download-queue` remains a legacy compatibility surface while user-facing copy stays on download history terminology.
- Server-side validation is the source of truth for profile invariants such as phone uniqueness and required fields by user type.

# Validation Baseline

- Frontend gates expected to pass: `npm test`, `npm run typecheck`, `npm run lint`, `npm run build`
- Backend gate expected to pass: `./gradlew test`
- Documentation gate expected to pass: `python .agents/skills/validate-docs/scripts/validate_docs.py`

# Notes

- Run Gradle tests sequentially only; avoid overlapping runs because Gradle test result files can contend.
- `frontend/tsconfig.tsbuildinfo` is generated noise and should not be committed as a meaningful change.
- If future work removes reusable process lessons from memory, move those lessons to `docs/retrospective/kick.md` first.

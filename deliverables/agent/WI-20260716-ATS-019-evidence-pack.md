---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: se
category: evidence
status: complete
related_wi: WI-20260716-ATS-019
---

# Evidence Pack: WI-20260716-ATS-019

## Summary

- Replaced the remaining customer-facing legacy display examples with `AT.M` or the neutral YouTube example `@your_channel`, preserving internal ATStudio identifiers.

## Scope / DoD Check

- [x] Whitelist validation and placeholders use a neutral YouTube handle/link example.
- [x] Acceptance business seed display name is `AT.M QA Biz`; email/account identifiers remain unchanged.
- [x] Active frontend title/header/admin display remains `AT.M`.
- [x] No route, API, callback, database, package, or deployment identifier was renamed.
- [x] Focused tests and frontend formatting/type/lint checks pass.
- [x] Documentation validation and `git diff --check` pass.
- [x] Client worktree HEAD/status remain unchanged.

## Reference Documents

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution and language/safety rules |
| 0 | `docs/standards/development-standards.md` | Implementation and traceability standards |
| 0 | `docs/standards/documentation-standards.md` | Documentation format and integrity rules |
| 0 | `docs/standards/glossary.md` | Canonical terminology; updated for the AT.M boundary |
| 1 | `docs/policies/quality-gates.md` | Verification gates |
| 2 | `docs/client/0-site-policy.md` | Current customer-facing service policy |
| 2 | `docs/ui/index.md` | Active UI document entry point |
| 2 | `docs/registry/project-registry.md` | Current project identity; updated for the display/internal boundary |
| REQ | `deliverables/user/REQ-20260716-ATS-002.md` | Approved scope and acceptance criteria |
| WI context | `deliverables/agent/WI-20260716-ATS-018-evidence-pack.md` | Previous WI boundary and worktree context |

## Evidence Pointers

- `frontend/index.html:11` retains the customer-facing document title `AT.M`.
- `frontend/src/layouts/Header.tsx:131` retains the public header logo `AT.M`.
- `frontend/src/layouts/AdminLayout.tsx:67` retains the admin display logo `AT.M`.
- `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:145` uses `https://www.youtube.com/@your_channel` in validation guidance.
- `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:330,338` uses neutral handle/link placeholders.
- `frontend/src/pages/subscriber/WhitelistChannelPage.test.tsx:72-75` asserts both neutral placeholders render.
- `src/main/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunner.java:131` changes only the BUSINESS fixture company display name to `AT.M QA Biz`; `src/main/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunner.java:124` preserves `qa.business@atstudio.local`.
- `src/test/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunnerTest.java:92-95` asserts the display name and preserved account email together.
- `docs/standards/glossary.md:65-67` defines the `AT.M` display-brand and `ATStudio` internal-identifier boundary.
- `docs/registry/project-registry.md:29-43` preserves the `ATStudio` project registry identity and records the display-brand boundary.

## Commands and Results

- `npm run test -- --run src/pages/subscriber/WhitelistChannelPage.test.tsx` from `frontend/` -> PASS; 1 file, 4 tests.
- `npm run typecheck` from `frontend/` -> PASS.
- `npx eslint src/pages/subscriber/WhitelistChannelPage.tsx src/pages/subscriber/WhitelistChannelPage.test.tsx --max-warnings 0` from `frontend/` -> PASS.
- `npx prettier --check src/pages/subscriber/WhitelistChannelPage.tsx src/pages/subscriber/WhitelistChannelPage.test.tsx` from `frontend/` -> PASS.
- `./gradlew.bat test --tests "com.atstudio.atstudio.bootstrap.TestUserBootstrapRunnerTest"` -> PASS, exit 0.
- `python .agents/skills/validate-docs/scripts/validate_docs.py` -> PASS; Tier 0, links, traceability IDs, and document index all passed.
- `git diff --check` -> PASS; existing line-ending warnings only.
- `rg -n --glob '!*.test.*' 'ATStudio|ATstudio|atstudio' frontend/src frontend/index.html` -> only preserved technical references in `frontend/src/router/index.tsx:139` and `frontend/src/types/index.ts:2`; no customer-facing stale display label or `@atstudio` example.
- `rg -n 'ATStudio QA Biz|AT.M QA Biz' src/main/java src/test/java` -> only `AT.M QA Biz` in the bootstrap and its focused assertion.
- `Get-FileHash frontend/tsconfig.tsbuildinfo -Algorithm SHA256` before/after -> unchanged SHA-256 `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A`.
- `git -C C:\Users\jm991\Desktop\project\ATStudio-client-demo-stable status --short` -> no output; `rev-parse HEAD` -> unchanged `cd876fcf84b3cb2490c27420c6c53a87a35b982d`.

## Risks / Rollback

- Risk: `ATStudio` remains visible in technical comments/types, documentation, package paths, account mail domains, and repository identifiers by design. These are outside customer-facing display cleanup.
- Rollback: revert only the WI-019 edits in the two frontend whitelist files, the bootstrap implementation/test, the two current documentation files, and these two WI-019 deliverables as one coherent group. Do not revert unrelated worktree changes.

## Follow-up

- Final development-branch release-readiness verification remains the next blocked-by-WI-019 activity.

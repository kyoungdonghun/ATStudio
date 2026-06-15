# Evidence Pack: WI-20260603-ATS-003

## Summary (one-liner)
- Updated whitelist API/DB/usecase/UI/client documentation to match the implemented workflow.

## Scope / DoD Check
- [x] API spec includes user/admin whitelist endpoints and CSV export.
- [x] DB schema includes expanded whitelist fields and export ledger tables.
- [x] `schema.sql` table count matches the DB schema table count after adding the missing `payment_settlements` DDL.
- [x] Use cases include save/request/primary/delete-removal/admin/export flows.
- [x] Use cases/API spec document revision re-request slot handling and primary replacement promotion.
- [x] UI/client docs reflect user/admin screens and non-subscriber behavior.
- [x] Docs validation passed.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution |
| 0 | docs/standards/documentation-standards.md | Documentation rules |
| 0 | docs/standards/glossary.md | Terminology |
| 1 | docs/policies/security-policy.md | Information exposure boundary |
| 2 | docs/design/api-spec.md | API SoT |
| 2 | docs/design/db-schema.md | DB SoT |
| 2 | docs/design/usecase/whitelist.md | Usecase SoT |
| 2 | docs/ui/screen-flow.md | Screen flow |
| 2 | docs/client/1-scenarios.md | Client acceptance scenarios |

## Evidence Pointers
- `docs/design/api-spec.md:9` — v16 to v17 change history.
- `docs/design/api-spec.md:2835` — Section 12 whitelist APIs.
- `docs/design/api-spec.md:3657` — full API summary 145.
- `docs/design/db-schema.md:13` — v12 whitelist schema change history.
- `docs/design/db-schema.md:702` — expanded whitelist channel table.
- `docs/design/db-schema.md:728` — export batch table.
- `docs/design/db-schema.md:740` — export item table.
- `docs/design/db-schema.md:1046` — complete table list 38.
- `src/main/resources/schema.sql:521` — `payment_settlements` DDL present, bringing `CREATE TABLE` count to 38.
- `docs/design/usecase/whitelist.md:1` — rewritten whitelist use cases.
- `docs/design/usecase/whitelist.md:94` — processed-channel updates must pass the same reprocessing gate.
- `docs/design/usecase/whitelist.md:124` — delete flow promotes a replacement primary channel when possible.
- `docs/design/usecase/whitelist.md:154` — request flow excludes a counted correction channel's own slot before limit comparison.
- `docs/design/usecase/index.md:25` — whitelist UC count 8.
- `docs/ui/atstudio-front-list.md:99` — H-1 screen update.
- `docs/ui/atstudio-front-list.md:146` — K-11 admin screen entry.
- `docs/ui/screen-flow.md:269` — user whitelist flow.
- `docs/ui/screen-flow.md:357` — admin whitelist flow.
- `docs/client/0-site-policy.md:55` — non-subscriber save/request boundary.
- `docs/client/1-scenarios.md:484` — admin whitelist acceptance scenario.
- `docs/standards/glossary.md:79` — whitelist-channel terminology update.

## Commands & Outputs
- `python .agents/skills/validate-docs/scripts/validate_docs.py` → PASS.
- `git diff --check` → PASS.

## Risks / Rollback
- Risk: `docs/client/testing-guide-friendly.html` is a static guide and may need regeneration if there is a separate source pipeline.
- Risk: Historical deliverables still mention older 12.1~12.4 whitelist scope by design; they are not active SoT.
- Rollback: Revert documentation updates associated with REQ-20260603-ATS-001.

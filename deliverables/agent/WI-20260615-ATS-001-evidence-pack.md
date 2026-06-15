# Evidence Pack: WI-20260615-ATS-001

## Summary (one-liner)
- Completed a current-state alignment audit and patch for payment, usage tag, whitelist-channel, schema, and documentation consistency.

## Scope / DoD Check
- [x] Current code/docs/schema reviewed for payment, whitelist-channel, and usage tag changes.
- [x] Existing DB migration gap documented for `ddl-auto=validate`.
- [x] Manual SQL patch added for existing MySQL DB alignment.
- [x] Whitelist CSV export frontend/API/documentation mismatch corrected.
- [x] Backend, frontend, docs, and diff checks passed.

## Reference Documents (Tier 0-2)

**Injected Context**

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Project operating principles |
| 0 | docs/standards/development-standards.md | Java/Spring and implementation standards |
| 0 | docs/standards/documentation-standards.md | Documentation consistency |
| 0 | docs/standards/glossary.md | Domain terminology |
| 2 | docs/design/db-schema.md | DB schema source of truth |
| 2 | docs/design/api-spec.md | API source of truth |
| 2 | docs/design/usecase/whitelist.md | Whitelist workflow source of truth |
| 2 | docs/payment/index.md | Payment documentation index |
| 2 | docs/payment/system-overview.md | Payment system overview |
| 2 | docs/SR/SR-93.md | Payment operations readiness checklist |

## Evidence Pointers
- `src/main/resources/schema.sql`: updated schema header/runtime notes and total table count.
- `src/main/resources/db/manual/20260615_align_payment_whitelist_schema.sql`: added manual SQL patch for existing MySQL DBs.
- `docs/design/db-schema.md`: documented runtime DB application notes and manual migration path.
- `docs/payment/index.md`, `docs/payment/system-overview.md`, `docs/SR/SR-93.md`: documented DB patch responsibility for payment/whitelist operations.
- `docs/design/api-spec.md`, `docs/design/usecase/whitelist.md`: clarified whitelist export as status-based; keyword filter is list-only.
- `frontend/src/pages/admin/WhitelistChannelManagePage.tsx`: removed keyword-as-export-note call.
- `frontend/src/api/whitelistChannels.ts`: clarified whitelist channel API comments.
- `docs/index.md`, `docs/design/index.md`, `docs/registry/project-registry.md`: updated current verification metadata.
- `deliverables/user/REQ-20260615-ATS-001.md`: approved scope record.
- `deliverables/user/WI-20260615-ATS-001-summary.md`: user-facing summary.

## Commands & Outputs
- `.\gradlew.bat test`: passed.
- `npm run typecheck`: passed.
- `npm run lint`: passed.
- `npm run build`: passed.
- `python .agents\skills\validate-docs\scripts\validate_docs.py`: passed.
- `git diff --check`: passed.
- API count check: 145 controller mappings excluding `SpaForwardController`.
- Schema table count check: 38 `CREATE TABLE` statements.
- Docs index count check: 184 Markdown documents across indexed categories.

## Risks / Rollback
- Risks:
  - The manual SQL patch has not been executed against local or production DBs.
  - Older databases may need earlier payment-operation migrations before this latest alignment patch.
  - Historical deliverables may still contain old counts; active SoT docs were prioritized.
- Rollback:
  - Revert this WI's changed files with Git if the documentation/schema alignment is not desired.
  - Do not execute the manual SQL patch unless the target DB has been backed up and reviewed.

## Follow-ups
- Before starting backend with `ddl-auto=validate` on an existing DB, review and apply `src/main/resources/db/manual/20260615_align_payment_whitelist_schema.sql` on a copied local/staging DB first.
- Commit the full whitelist feature and current-state alignment changes together only after the user confirms the final diff scope.

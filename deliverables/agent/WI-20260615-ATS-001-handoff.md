[WI HEADER]
WI ID: WI-20260615-ATS-001
REQ: REQ-20260615-ATS-001
Agent: MA
Depends On: -
Blocks: -

[WI SUMMARY]
Why: Align code, DB schema references, design docs, UI docs, and deliverables after payment, usage tag, and whitelist-channel feature work.
Scope (in/out):
- In: current-state audit, documentation corrections, schema/manual migration guidance, lightweight code/document mismatch fixes, verification.
- Out: applying DDL to local/production DB, deleting data, adding new payment providers, implementing YouTube automatic whitelist registration.
DoD:
- Active SoT docs describe the same feature scope as the current code.
- `schema.sql` and `docs/design/db-schema.md` state that existing DBs require manual migration under `ddl-auto=validate`.
- Manual SQL patch exists for the latest known payment/whitelist schema delta.
- Backend, frontend, docs, and diff checks pass.
Constraints/Forbidden:
- Do not execute destructive DB or data operations without explicit user approval.
- Do not revert unrelated existing working tree changes.
- Keep historical deliverables as historical artifacts unless active SoT docs depend on them.

[ACCEPTANCE CRITERIA]
Functional:
- [x] Manual DB patch is available for existing MySQL DB alignment.
- [x] Whitelist export behavior is represented consistently across frontend and docs.
- [x] Payment and schema docs include current runtime DB application notes.
Quality:
- [x] Backend tests pass.
- [x] Frontend typecheck/lint/build pass.
- [x] Documentation validation passes.
- [x] Diff whitespace check passes.

[INPUT POINTERS]
Tier 0 (Constitution - Required):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1/Tier 2 (Task Context):
- docs/design/db-schema.md
- docs/design/api-spec.md
- docs/design/usecase/whitelist.md
- docs/payment/index.md
- docs/payment/system-overview.md
- docs/SR/SR-93.md
- src/main/resources/schema.sql
- frontend/src/pages/admin/WhitelistChannelManagePage.tsx
- frontend/src/api/whitelistChannels.ts

[OUTPUT CONTRACT]
- User-facing summary: deliverables/user/WI-20260615-ATS-001-summary.md
- Agent evidence pack: deliverables/agent/WI-20260615-ATS-001-evidence-pack.md
- Code/doc changes must remain reviewable by `git diff`.

[TRACEABILITY]
- Related REQ: deliverables/user/REQ-20260615-ATS-001.md
- Manual DB patch: src/main/resources/db/manual/20260615_align_payment_whitelist_schema.sql

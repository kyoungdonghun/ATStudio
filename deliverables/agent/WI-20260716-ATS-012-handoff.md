[WI HEADER]
WI ID: WI-20260716-ATS-012
REQ: REQ-20260716-ATS-002
Agent: docops
Depends On: WI-20260716-ATS-005, WI-20260716-ATS-006, WI-20260716-ATS-007, WI-20260716-ATS-008, WI-20260716-ATS-009, WI-20260716-ATS-010, WI-20260716-ATS-011
Blocks: WI-20260716-ATS-013, WI-20260716-ATS-014

[WI SUMMARY]
Why: Reconcile design, current implementation, operational guidance, client acceptance material, and traceability after the remediation WIs so the repository describes one current system and produces a reproducible client PDF.

Scope (in):
- Re-run a semantic three-way review: approved design/planning intent vs current backend/frontend/schema/tooling implementation vs current design/current-state/operations/client documents.
- Reconcile the canonical billing-agreement DTO/API examples, including zero-amount payment-method re-registration and flat/nested response shapes.
- Declare the active SPA localStorage play-history UX as the current user-visible SoT and clearly separate the retained server play-history API/entity as legacy compatibility; do not invent synchronization.
- Define and apply a stable screen-count contract that separately records route entries, unique route-level page screens/components, reused aliases/workflows, and modal/overlay inventory. Publish only a number whose unit and derivation are explicit.
- Correct the admin dashboard and site-settings screen/API inventory from current router/controller/API evidence.
- Reconcile documentation metadata vocabulary before targeted remediation; do not blindly rewrite every historical document. Repair live standards/index/registry/workboard/SR/deliverable traceability and state whether CTX/workboard paths are authoritative, advisory, or deprecated.
- Repair SR index tail/status/confirmation lifecycle from existing SR files and history without deleting historical SR evidence.
- Update Phase 2 lifecycle labels, freshness dates, API/DB/UI/document counts, and index entries using the repository's documented counting rules and current files.
- Add explicit deprecation/replacement/removal criteria for legacy one-time subscription endpoints/routes and other confirmed legacy compatibility surfaces; do not remove code in this WI.
- Reconcile all WI-005 through WI-011 security, payment, whitelist, company certification, OAuth/catalog/download, frontend state/a11y, dependency, formatting, and coverage facts across design, policy, operations, UI, SR, client, and audit-facing current-state documents.
- Update acceptance/public-exposure guidance to state that an internet-exposed Vite dev server must have compatible security patches or a controlled-access alternative. Record current dev-toolchain residual advisories without mislabeling them as production bundle vulnerabilities.
- Update the seven-source client testing bundle and regenerate `output/pdf/atstudio-client-testing-guide.pdf` from a committed deterministic generator.
- Add a machine-readable or human-readable PDF provenance manifest containing generator path/version/runtime, ordered source list, source SHA-256 hashes, output hash, creation command, and deliberate exclusions.
- Ensure the generated PDF has a valid Unicode Korean title, current AT.M display branding where applicable, matching source body, and visually verified nonblank/unclipped pages.
- Create the required user summary and agent evidence pack.

Scope (out):
- Backend/frontend product logic, database DDL/data, provider calls, secret/proxy/deployment changes, or removal of legacy endpoints.
- Inventing a social-only withdrawal policy, real-provider evidence, retained-DB evidence, or environment evidence not present in the repository.
- Changing, merging, restarting, or committing to `codex/client-demo-stable` or its worktree/public runtime.
- Broad metadata insertion into historical/read-only audit records merely to improve a count.
- Treating structural `validate_docs.py` PASS as semantic correctness.
- Hiding low coverage or dev-only advisories behind wording changes.

DoD:
- Current Java DTOs/controllers/entities/schema and React types/routes/screens are the explicit evidence source for observed contracts; approved product invariants remain the policy source.
- P2-12 through P2-15 and P3-01 through P3-02 in the remaining-remediation design are either closed with direct evidence or retained with an honest status and reason.
- Billing API examples match the current Java and TypeScript shapes and payment-method re-registration amount semantics.
- Play-history documentation consistently distinguishes active local SPA history from legacy server persistence APIs.
- Screen/project counts are derived by documented commands/rules and match all live index/registry/UI documents.
- SR/index/registry/workboard/metadata/freshness/Phase-2 statements are internally consistent and point to current paths.
- Legacy one-time subscription surfaces have deprecation, replacement, compatibility, and removal criteria.
- The client Markdown bundle reflects the current tested behavior and contains a clear public dev-server security prerequisite.
- PDF generation is reproducible from committed assets; manifest hashes match; Unicode title and all-page visual/text checks pass.
- Document validation, index synchronization/count checks, PDF checks, and `git diff --check` pass.

Constraints/Forbidden:
- Work only in `C:/Users/jm991/Desktop/project/ATStudio` on `codex/p1-acceptance-hardening`.
- You are not alone in the codebase. Preserve every code and document change from WI-005 through WI-011; do not revert or reformat unrelated files.
- Documentation language is English except the existing Korean REQ and client-facing Korean guidance, which should remain easy for a non-technical client.
- Product invariants: public full-track listening; downloads gated by subscription/quota/license; recurring card billing through billing keys; single-server topology.
- Current implementation documents observed behavior, but it does not silently override explicit approved product policy. Flag conflicts instead of disguising them.
- No destructive operation, DB mutation, real payment/provider call, secret access, client worktree modification, stage, commit, or push.
- Historical audit/evidence documents remain historical. Add closure/current-state pointers rather than rewriting evidence that was true at the time.
- Use ASCII in code/scripts where practical; Korean client content and Unicode PDF metadata are intentional exceptions.
- Use a deterministic source order for the PDF: `testing-guide.md`, `1-quick-checklist.md`, `2-full-feature-checklist.md`, `3-admin-checklist.md`, `4-sr-format.md`, `5-ai-prompt.md`, `0-site-policy.md`. Explicitly exclude `index.md` and `_internal-feature-map.md` from the PDF body.
- Follow the PDF skill workflow: use repository/bundled dependencies, render every page, inspect representative pages, and do not accept blank, clipped, overlapping, or broken-Hangul output.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Billing prepare/confirm/my/cancel examples match current Java DTO and TypeScript contracts, including `BILLING_AGREEMENT` amount `0` re-registration.
- [ ] Active SPA play history and legacy server play-history APIs are explicitly separated in API/use-case/UI/client documents.
- [ ] Screen count contract and exact current counts are documented and consistent across root index, registry, UI inventory, router commentary where applicable, and client material.
- [ ] Admin stats and site settings appear with their real API/controller/route evidence.
- [ ] SR index tail, SR-C aliases/statuses, confirmation-directory claim, registry/workboard paths, and deliverables source-of-truth are corrected.
- [ ] Phase 2 is consistently described as active where implemented; live-document freshness/status metadata is current.
- [ ] Legacy one-time subscription endpoints/routes have replacement and removal criteria without code removal.
- [ ] Current coverage baselines, production dependency audit result, and dev-only residual advisories are accurately documented.
- [ ] Client acceptance checklists reflect current payment, whitelist, company certification, auth/download, frontend retry/error/a11y behavior and remain easy to follow.
- [ ] PDF and manifest regenerate deterministically from the seven ordered source files; manifest source/output hashes verify.
- [ ] PDF Title metadata preserves Korean Unicode and visual/text comparison proves the final bundle is readable and synchronized.

Performance:
- [ ] Documentation/PDF tooling does not run application servers or modify application runtime state.
- [ ] PDF generation finishes as a bounded one-shot command and writes intermediates only under `tmp/pdfs/`.

Quality:
- [ ] `python .agents/skills/validate-docs/scripts/validate_docs.py` passes.
- [ ] The docs index sync/count check passes under the documented direct/recursive category rules.
- [ ] No U+FFFD replacement characters or broken internal Markdown links remain in live documents.
- [ ] PDF metadata, page count, ordered-source hashes, output hash, text presence, page rendering, edge/clipping signal, and representative visual inspection are recorded.
- [ ] Current API/DB/screen/agent/SR counts are reproduced from source commands and agree across live documents.
- [ ] `git diff --check` passes apart from pre-existing non-failing line-ending warnings.
- [ ] Client branch/worktree HEAD and clean status remain unchanged.

[INPUT POINTERS]
Tier 0 (Constitution and documentation standards):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
- docs/standards/development-standards.md

Tier 1 (Policies and architecture):
- docs/policies/quality-gates.md
- docs/policies/security-policy.md
- docs/policies/archive-policy.md
- docs/architecture/system-design.md

Tier 2 (Design, UI, operations, and client sources):
- docs/design/remaining-remediation-design-20260716.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/usecase/
- docs/ui/
- docs/payment/
- docs/SR/index.md
- docs/SR/SR-42.md
- docs/SR/SR-92.md
- docs/SR/SR-93.md
- docs/registry/
- docs/client/
- docs/index.md
- scripts/acceptance/
- output/pdf/atstudio-client-testing-guide.pdf

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-002.md
- deliverables/agent/WI-20260716-ATS-005-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-006-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-007-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-008-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-009-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-010-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-011-evidence-pack.md
- docs/audit/full-system-audit-20260713.md
- deliverables/agent/WI-20260711-ATS-001-evidence-pack.md
- deliverables/agent/WI-20260711-ATS-006-evidence-pack.md
- deliverables/agent/WI-20260711-ATS-019-evidence-pack.md

Implementation Evidence:
- src/main/java/com/atstudio/atstudio/controller/
- src/main/java/com/atstudio/atstudio/dto/
- src/main/java/com/atstudio/atstudio/entity/
- src/main/resources/schema.sql
- frontend/src/api/
- frontend/src/router/index.tsx
- frontend/src/pages/
- frontend/src/store/playerStore.ts
- frontend/package.json
- frontend/vite.config.ts
- build.gradle

Repro/Logs:
- `python .agents/skills/validate-docs/scripts/validate_docs.py`
- `.agents/skills/sync-docs-index/SKILL.md` counting rules and corresponding count commands
- `rg`/PowerShell source inventories for request mappings, `CREATE TABLE`, `@Entity`, route/page/modal, SR, and agent counts
- deterministic client PDF generator command created by this WI
- SHA-256 verification for every PDF source and output
- PDF metadata/text/page render inspection using the bundled PDF runtime
- `git diff --check`
- read-only `git status --short --branch` in both development and client worktrees

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-012-summary.md:
- Plain-language description of corrected documents, authoritative behavior, exact current counts, PDF result, remaining environment/policy boundaries, and next QA steps.

Agent-facing -> deliverables/agent/WI-20260716-ATS-012-evidence-pack.md:
- Full three-way matrix, changed-file pointers, count commands/results, metadata/registry/SR decisions, legacy deprecation evidence, PDF generator/manifest/render evidence, validation outputs, risks, and rollback.

Handoff Packet -> deliverables/agent/WI-20260716-ATS-012-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
- Every corrected claim must cite design/intent, implementation evidence, and final current-state/client document where applicable.
- Distinguish current authoritative documents from historical audit/evidence records.
- Record every exact count with its unit, inclusion/exclusion rule, and reproduction command.
- Record every PDF source path/hash in order, generator identity/command, output hash, Unicode metadata inspection, all-page render result, and representative visual-review pages.
- Record structural validation limits; do not claim semantic closure from the validator alone.
- Rollback must identify only WI-012 documents/tooling/PDF/manifest and preserve code plus earlier WI evidence.
- Explicitly list unresolved policy/environment items such as social-only withdrawal, retained-DB rehearsal, real providers/secrets/proxy evidence, dev-toolchain advisory remediation, and the frozen public client branch.

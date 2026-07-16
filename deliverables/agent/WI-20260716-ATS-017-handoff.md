[WI HEADER]
WI ID: WI-20260716-ATS-017
REQ: REQ-20260716-ATS-002
Agent: se
Depends On: WI-20260716-ATS-015, WI-20260716-ATS-016
Blocks: -

[WI SUMMARY]
Why: Apply the smallest coherent fixes for every accepted WI-013 through WI-016 finding, synchronize current documentation/client PDF, and rerun the complete backend/frontend/document quality gates until release readiness can be honestly re-evaluated.

Scope (in) - Priority 1 security/financial/concurrency:
- F-016-01: whitelist channel URLs must accept only normalized HTTPS YouTube hosts, reject credentials and unsafe/lookalike schemes/hosts, and never render an unsafe persisted value as an anchor in subscriber/admin UI. Add backend and frontend regression tests. Do not mutate retained rows; document the operator audit requirement.
- F-016-02: serialize entitlement-correction creation on the user subscription, reject an existing non-terminal correction for the same refund/subscription, and compare current subscription state with the captured before-state immediately before execution. Add race/stale/retry tests without introducing a schema migration unless existing locking cannot provide the guarantee.
- F-016-03: add a bounded/cursor-based provider-reconciliation path for eligible recent DONE provider orders. Distinguish locally recorded refunds/cancellations, provider not-found/non-DONE/amount mismatch/lookup failure, preserve incident deduplication, and avoid unbounded provider calls. Add focused tests and update runbook/design/config docs.
- F-016-04: use a consistent pessimistic lock path and lock order for all album and playlist mutations, including metadata update/delete and membership/reorder. Add update/delete and concurrent-mutation contract tests; retain MySQL proof as environment-conditional where it cannot run locally.
- F-015-01: restrict company-certification self-service endpoints to ROLE_USER plus BUSINESS qualification; add BUSINESS ADMIN denial tests while preserving admin review endpoints.

Scope (in) - Priority 2 frontend/state/verification/privacy:
- F-016-05: replace PlayerBar's boolean subscription collapse with loading/active/inactive/error semantics; only the structured no-active-subscription domain response means inactive. Add AbortController/generation fencing, retry-safe UI behavior, and active/inactive/5xx/offline/auth-change/out-of-order tests.
- F-016-06: add cancellation plus generation fences to TrackDetailPage, UserManagePage, UserSubscriptionManagePage, and DownloadQueuePage. Ignore cancellation as an error and add old-success/old-failure-after-current tests.
- F-016-07/F-015-05: add focused admin payment UI tests for settlement/refund/entitlement mutation payload/ID wiring, confirmation, busy single-submit, error feedback, and one current-view refresh. Add backend AdminPaymentController/read-service contract tests where current HTTP/read wiring is uncovered.
- WI-013 risk finding: add focused tests for reachable StorageMutationJournalService compensation/retry/idempotency behavior and other directly confirmed high-risk gaps; do not chase coverage percentage or test retained legacy internals without a reachable risk.
- F-015-02: make provider identifiers follow one safe contract. Raw provider payment keys remain server-held for provider operations and must not appear by default in client-facing/admin UI or client capture guidance. Prefer masked/support references in response/display where exact raw values are not required; keep mutations keyed by local IDs and server-side lookup. Align code, security/payment docs, client guide, and generated PDF. Do not remove evidence needed for refunds/reconciliation.

Scope (in) - Priority 3 navigation/docs/maintenance:
- F-016-08: preserve the already validated internal return target across an OAuth attempt using per-attempt session state, consume it once after callback/profile completion, and reject external/protocol-relative/missing/stale targets. Add tests.
- F-016-09/F-015-P3-03: replace stale fixed router/frontend-standard counts with the current documented counting contract or pointer; synchronize managed-document count after adding WI deliverables.
- F-015-P3-02: correct playlist-delete documentation to relationship physical deletion plus playlist soft deletion.
- Record MySQL/symlink/live-provider/proxy/secret boundaries as environment-conditional; do not fake evidence. Identify the unchecked Java warning source if practical and document it without unrelated refactoring.
- Update design/API/UI/security/payment/SR/registry/client/current-remediation documents only where the final code changes alter or close a finding.
- Regenerate and verify the deterministic client PDF/manifest if any included source changes.
- Create the required WI-017 summary and Evidence Pack with a finding-by-finding closure matrix.

Scope (out):
- Social-only withdrawal policy or implementation, multi-server scheduler locks, new payment provider, cash receipts, tax invoices, unrelated feature additions, broad refactoring, live Toss/OAuth calls, retained/production DB mutation, client-demo branch/runtime changes, stage/commit/push.
- Applying manual migrations or deleting temporary/runtime/data files.

DoD:
- Every F-016-01 through F-016-09 and F-015-01/F-015-02/F-015-P3-02/P3-03 has explicit CLOSED, ENVIRONMENT-CONDITIONAL, POLICY-PENDING, or retained-with-reason disposition and direct evidence.
- P1 unsafe URL path is blocked at backend storage and frontend rendering boundaries.
- Financial/concurrency fixes are bounded, idempotent, tested, and do not change approved payment/subscription policy.
- Frontend stale/error behavior and high-risk admin mutations have focused tests.
- Current code, design, operations, client Markdown, and generated PDF describe one system.
- Full backend build/test/JaCoCo and frontend audit/typecheck/lint/full Vitest/coverage/build/full-tree Prettier all pass after fixes.
- Docs validation, index count sync, PDF verification/render QA, git diff --check, and tsbuildinfo hash checks pass.

Constraints/Forbidden:
- Work only in C:/Users/jm991/Desktop/project/ATStudio on codex/p1-acceptance-hardening.
- You are not alone. Preserve all prior WI changes and existing unrelated dirty files; make targeted edits only.
- Do not modify/restart/merge the frozen codex/client-demo-stable worktree/runtime.
- Product invariants are immutable: public full-track listening; subscription/quota/license-gated official download; recurring billing-key card payment; single-server topology.
- Do not invent social-only withdrawal behavior or claim environment evidence not run.
- No destructive operation, DB/data mutation, real provider/secret access, stage, commit, or push.
- Use apply_patch for manual edits. Do not erase prior evidence or rewrite historical audit records.
- Preserve frontend/tsconfig.tsbuildinfo exact baseline SHA-256 B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A after all frontend commands.
- Test changes should prove reachable risk; coverage remains observational, not an invented threshold.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Unsafe whitelist URL schemes/credentials/lookalikes are rejected and unsafe retained values render as text, not links.
- [ ] Entitlement corrections reject duplicate non-terminal/stale execution and remain idempotent.
- [ ] Eligible recent DONE provider orders are reconciled in bounded pages without false incidents for recorded refunds/cancellations.
- [ ] Album/playlist update/delete/membership mutations share a consistent lock order.
- [ ] Certification self-service rejects ADMIN even when BUSINESS.
- [ ] Player and four loaders distinguish active/inactive/error/cancel/stale outcomes correctly.
- [ ] Admin financial mutation/read contracts have focused frontend/backend tests.
- [ ] Raw provider payment keys are not exposed by default in UI/client material while server-side operations retain required evidence.
- [ ] OAuth safe return survives valid attempts and rejects unsafe/stale targets.
- [ ] Current docs/counts/PDF match final behavior.

Performance:
- [ ] DONE reconciliation is bounded by explicit page/age/run caps and cannot scan/call providers without limit.
- [ ] Frontend cancellation/fencing does not create duplicate requests or mutation submissions.
- [ ] Full verification fits documented heap/timeout expectations; deviations are recorded.

Quality:
- [ ] Focused tests for every code fix pass before full suites.
- [ ] Backend clean full suite/build/JaCoCo passes with exact totals recorded.
- [ ] Frontend production/unfiltered audit, typecheck, lint, full Vitest, V8 coverage, build, and full-tree Prettier pass.
- [ ] validate-docs, index count sync, PDF hash/text/title/all-page render QA, and git diff --check pass.
- [ ] tsconfig.tsbuildinfo final hash equals the baseline.
- [ ] Client branch HEAD/status remain unchanged.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2:
- docs/design/remaining-remediation-design-20260716.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/payment-integration-design.md
- docs/design/payment-operations-runbook.md
- docs/design/usecase/
- docs/ui/
- docs/payment/
- docs/client/
- docs/SR/
- docs/registry/
- scripts/docs/

REQ/Review Evidence:
- deliverables/user/REQ-20260716-ATS-002.md
- deliverables/user/WI-20260716-ATS-013-summary.md
- deliverables/user/WI-20260716-ATS-014-summary.md
- deliverables/user/WI-20260716-ATS-015-summary.md
- deliverables/user/WI-20260716-ATS-016-summary.md
- deliverables/agent/WI-20260716-ATS-013-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-014-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-015-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-016-evidence-pack.md

Implementation Evidence:
- src/main/java/com/atstudio/atstudio/
- src/main/resources/application.yml
- src/main/resources/schema.sql
- src/test/java/com/atstudio/atstudio/
- frontend/src/
- frontend/package.json
- frontend/vite.config.ts
- build/reports/
- frontend/coverage/
- output/pdf/atstudio-client-testing-guide.pdf
- output/pdf/atstudio-client-testing-guide.manifest.json

Repro/Logs:
- focused Gradle/Vitest commands selected by changed area
- gradlew.bat clean test jacocoTestReport --console=plain
- gradlew.bat build --console=plain
- npm audit --omit=dev --json; npm audit --json
- npm run typecheck; npm run lint; npm test -- --run; npm run test:coverage; npm run build
- npx prettier --check . --ignore-unknown
- Python docs/PDF validators and source/count/hash/render checks
- git diff --check; tsbuildinfo SHA-256; client worktree status/HEAD

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-017-summary.md:
- Plain-language fixes, finding closure table, exact full verification results, remaining policy/environment boundaries, and release-readiness judgment.

Agent-facing -> deliverables/agent/WI-20260716-ATS-017-evidence-pack.md:
- Patch inventory, finding-by-finding evidence, focused/full commands and results, coverage observations, docs/PDF provenance, environment limits, client-branch integrity, and rollback guidance.

Handoff Packet -> deliverables/agent/WI-20260716-ATS-017-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
- Map every edit/test to one accepted finding or current-document synchronization need.
- For each finding, record before risk, changed contract, code/test/doc pointers, and final status.
- Separate code closure from retained MySQL/live-provider/proxy/secret/symlink evidence gaps.
- Preserve historical evidence; update only current-state documents and add closure pointers where needed.
- Rollback must be targeted by finding and must not revert earlier WI work.
- Do not mark release-ready while any accepted P1/P2 code finding remains open.

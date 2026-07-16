[WI HEADER]
WI ID: WI-20260716-ATS-029
REQ: REQ-20260716-ATS-002
Agent: se
Depends On: WI-20260716-ATS-026, WI-20260716-ATS-027
Blocks: WI-20260716-ATS-030

[WI SUMMARY]
Why: Close the final frontend request-race/accessibility, separate-origin export, and code/document consistency findings before commit.
Scope (in): F-026-01 through F-026-03 and F-027-01 through F-027-05, except payment backend implementation owned by WI-028. Add latest-request fences, queued/final refresh behavior, authoritative detail close, exposed/validated export headers, focused tests, API/glossary/use-case/runbook examples, and explicit generated-file commit exclusion.
Scope (out): Payment service/renewal/correction implementation, schema changes, client branch/runtime, generated output deletion, tracked tsbuild deletion/untracking, new UX or product policy.
DoD: Admin list responses are latest-request-wins; whitelist mutations converge to the final server state; certification detail closes immediately and ignores late completions; separate-origin export exposes and validates replay headers; docs/comments match wire/runtime contracts; `frontend/tsconfig.tsbuildinfo` remains byte-identical and explicitly excluded from later staging.
Constraints/Forbidden: Work only on the development branch. Do not stage, commit, push, restart, mutate DB/provider, touch the client worktree, delete/untrack files, or change product invariants. You are not alone in the codebase; do not revert WI-028 or other concurrent edits.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Admin whitelist and company-certification list loaders ignore stale success/failure/finally completions.
- [ ] Subscriber whitelist overlapping mutations schedule/converge on a final refresh.
- [ ] Certification detail closes by Escape/button while loading and late success/failure cannot reopen it.
- [ ] CORS exposes `Content-Disposition` and `X-Whitelist-Export-Batch-Id`; the adapter rejects/falls back safely for absent or invalid batch IDs.
- [ ] Company-certification examples use the standard envelope and implemented binary media contract.
- [ ] Grace-period, reconciliation currency, and operation guidance wording match current code.
Quality:
- [ ] Focused Vitest and backend CORS tests cover every behavioral finding.
- [ ] Typecheck, targeted ESLint, targeted Prettier, docs validation, and diff check pass.
- [ ] `frontend/tsconfig.tsbuildinfo` hash remains unchanged and no generated/runtime output is deleted.
- [ ] Both deliverables are produced.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/quality-gates.md
Tier 2:
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md
- docs/ui/screen-flow.md
- docs/design/api-spec.md
- docs/design/usecase/sound-track.md
REQ/Findings:
- deliverables/user/REQ-20260716-ATS-002.md
- deliverables/user/WI-20260716-ATS-026-summary.md
- deliverables/agent/WI-20260716-ATS-026-evidence-pack.md
- deliverables/user/WI-20260716-ATS-027-summary.md
- deliverables/agent/WI-20260716-ATS-027-evidence-pack.md
Owned files:
- frontend/src/pages/admin/WhitelistChannelManagePage.tsx and focused tests
- frontend/src/pages/admin/CompanyCertManagePage.tsx and focused tests
- frontend/src/pages/subscriber/WhitelistChannelPage.tsx and focused tests
- frontend/src/api/admin.ts and whitelist export adapter tests
- src/main/java/com/atstudio/atstudio/config/CorsConfig.java
- src/test/java/com/atstudio/atstudio/config/CorsConfigTest.java
- docs/design/api-spec.md
- docs/standards/glossary.md
- docs/design/usecase/sound-track.md
- frontend/src/router/SubscriberRoute.tsx comment only if required
- directly affected UI/operations documentation excluding WI-028-owned payment files
Do not edit:
- src/main/java/com/atstudio/atstudio/service/payment/**
- src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java
- src/main/java/com/atstudio/atstudio/service/AdminPayment*.java
- docs/design/payment-integration-design.md
- docs/design/payment-operations-runbook.md
- docs/policies/security-policy.md
- frontend/tsconfig.tsbuildinfo

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-029-summary.md
Agent-facing -> deliverables/agent/WI-20260716-ATS-029-evidence-pack.md
Handoff -> deliverables/agent/WI-20260716-ATS-029-handoff.md

[TRACEABILITY REQUIREMENTS]
- Map every changed file/test/doc to F-026-01..03 or F-027-01..05.
- Record exact commands, results, state-machine reasoning, rollback, and residual environment boundaries.
- Preserve unrelated and concurrent worktree edits.

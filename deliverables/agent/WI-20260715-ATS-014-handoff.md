[WI HEADER]
WI ID: WI-20260715-ATS-014
REQ: REQ-20260714-ATS-001
Agent: qa
Depends On: WI-20260715-ATS-013
Blocks: final scope audit and completion report

[WI SUMMARY]
Why: Independently verify the complete current development branch after payment-integrity remediation and documentation alignment, while confirming the frozen client-acceptance preview remains reachable.
Scope (in): Full backend clean build/test; frontend typecheck, lint, Prettier check, test, and build; documentation validation; Git whitespace/scope checks; frozen preview commit and public SPA/API smoke; review of authoritative WI-007 MySQL 7/7 evidence and WI-012 independent closure.
Scope (out): Product/docs/schema edits, live Toss, real-money movement, retained/production DB changes, client data mutation, preview branch mutation, tunnel restart, new feature work, and non-payment remediation.
DoD: Produce one reproducible PASS/FAIL verdict with exact commands, durations/counts where available, failures/warnings, preview commit/status, changed-artifact audit, and remaining production/client risks. PASS requires every executable gate in this WI to pass and no new P0/P1 within the verified payment scope.
Constraints/Forbidden: Read-only except WI-014 summary/evidence outputs. Do not use live keys, real payment, SMTP, production or retained DB, or destructive cleanup. Do not modify the preview worktree at `C:\Users\jm991\Desktop\project\ATStudio-acceptance-preview`. Do not stage or commit. Preserve the four untracked Cloudflare/Vite runtime logs. You are not alone in the repository; do not revert user/concurrent changes. If a command generates tracked build metadata, report it and restore only the generated metadata after confirming it was clean before execution.

[ACCEPTANCE CRITERIA]
Backend:
- [ ] `gradlew.bat clean build` passes, including the complete JUnit suite.
- [ ] Test report counts and any skips/warnings are recorded.
Frontend:
- [ ] `npm run typecheck` passes.
- [ ] `npm run lint` passes with zero warnings.
- [ ] `npm run format` passes.
- [ ] `npm run test` passes and test/file counts are recorded.
- [ ] `npm run build` passes.
Documentation and scope:
- [ ] `python .agents/skills/validate-docs/scripts/validate_docs.py` passes without warnings.
- [ ] `git diff --check` passes.
- [ ] Working tree after verification contains only the four expected untracked runtime logs plus WI-014 outputs; no generated tracked metadata remains.
Independent evidence:
- [ ] WI-007 authoritative final MySQL summary remains schema PASS, Hibernate validate PASS, seven races PASS, drop PASS, cleanup count 0.
- [ ] WI-012 remains PASS with no P0/P1 in its reviewed follow-up scope; its single P3 log-appender test gap is reported as non-blocking.
Preview:
- [ ] Preview worktree remains commit `b217234` on `codex/acceptance-preview`.
- [ ] `https://sara-edit-seeker-receiving.trycloudflare.com/` and `/api/tracks` both return HTTP 200 without mutating data.
- [ ] Quick Tunnel limitations are reported: temporary URL and dependency on local processes/PC.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies):
- docs/policies/quality-gates.md
- docs/policies/security-policy.md

REQ/Context Docs:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/audit/p1-payment-integrity-closure-20260715.md
- docs/design/p1-payment-integrity-remediation-design.md
- docs/SR/SR-93.md
- docs/payment/acceptance-test-checklist.md
- deliverables/agent/WI-20260715-ATS-007-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-012-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-013-evidence-pack.md

Verification Skills:
- .agents/skills/build-check/SKILL.md
- .agents/skills/test/SKILL.md
- .agents/skills/typecheck/SKILL.md
- .agents/skills/eslint/SKILL.md
- .agents/skills/prettier/SKILL.md
- .agents/skills/validate-docs/SKILL.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260715-ATS-014-summary.md
Agent-facing -> deliverables/agent/WI-20260715-ATS-014-evidence-pack.md
Handoff -> deliverables/agent/WI-20260715-ATS-014-handoff.md

[TRACEABILITY REQUIREMENTS]
Record exact command lines, exit codes, durations, backend/frontend test counts, preview commit/branch, HTTP status codes, Git status before and after, and all intentionally unverified production boundaries. Distinguish a test/build PASS from production readiness and client acceptance. Include rollback guidance for WI outputs only.

[WI HEADER]
WI ID: WI-20260717-ATS-003
REQ: REQ-20260716-ATS-004
Agent: se
Depends On: WI-20260717-ATS-001
Blocks: WI-20260717-ATS-005, WI-20260717-ATS-006

[WI SUMMARY]
Why: Remove approved frontend residual code and consolidate active React SPA routes, names, API clients, and destructive confirmations around the V1 contracts.
Scope (in/out): Implement frontend portions of INT-R01, R02, R08, R09, R10, R11 and INT-P06, P07, P08, P09 plus resolved V01 through V05 frontend consequences. Remove server play-history and obsolete download-queue clients/types; remove unused DataTable and API exports; remove the obsolete playlist-create adapter; rename download history and payment operations identities; replace production window.confirm calls in payment/whitelist surfaces with controlled ConfirmDialog behavior; neutralize mock CSS naming; remove stale one-time payment aliases/callback routes. Out of scope: backend, schema/config/provider enum, tracked tsbuildinfo/ignore policy (WI-005), active docs, generated artifacts, and product redesign.
DoD: Active SPA has only current V1 routes/contracts; approved old symbols/routes have zero references; recurring checkout, playback, downloads, whitelist and admin payment behavior remain; frontend full quality gates for this WI pass.
Constraints/Forbidden: Do not edit backend, schema/config, docs, Git refs, runtime processes, secrets, frontend/tsconfig.tsbuildinfo, or unrelated artifacts. Preserve OAuth/PKCE, storage/error fallbacks, request-generation fences, route guards, public full-track playback/progress/waveform, current recurring checkout and emergency admin operations. Do not revert other changes.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Remove obsolete play-history/download-queue clients and aliases without changing browser-local history or active download history.
- [ ] Canonicalize route/page names without adding legacy redirects.
- [ ] Remove unused component/API exports and placeholder files in owned frontend paths.
- [ ] Replace ten production confirm calls in payment/whitelist pages with accessible, single-submit confirmation dialogs.
- [ ] Remove stale one-time payment callback/alias surfaces and provider-specific mock naming while preserving recurring Toss checkout.
Performance:
- [ ] No avoidable new bundle dependency or rerender regression.
Quality:
- [ ] Typecheck, ESLint, Prettier, Vitest, and production build pass for the resulting frontend.
- [ ] Targeted navigation, dialog, payment, whitelist, player, and download tests pass.
- [ ] Exact negative reference searches for approved old routes/symbols pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/quality-gates.md

Tier 2:
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
- docs/design/api-spec.md
- docs/ui/

REQ / Decision Sources:
- deliverables/user/REQ-20260716-ATS-004.md
- deliverables/agent/WI-20260717-ATS-001-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-038-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-035-evidence-pack.md

Files:
- frontend/src/
- frontend/package.json

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260717-ATS-003-summary.md
Agent-facing -> deliverables/agent/WI-20260717-ATS-003-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260717-ATS-003-handoff.md

[TRACEABILITY REQUIREMENTS]
Map every edit to an INT ID. Record routes/symbols removed or renamed, dialog behavior, commands/results, visual or component evidence, residual references, risks, and rollback. Use create-wi-evidence-pack after implementation.

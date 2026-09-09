[WI HEADER]
WI ID: WI-20260817-ATS-015
REQ: REQ-20260817-ATS-009
Agent: docops
Depends On: WI-20260817-ATS-014
Blocks: WI-20260817-ATS-016, WI-20260817-ATS-017

[WI SUMMARY]
Why: Make current operating documents truthful after the 43-table source update and the completed React Router security remediation, without rewriting historical evidence.

Scope (in/out):
- In: Current-state sections of payment guides and active SR addenda that still claim a 39-table baseline or an unresolved React Router 6 production audit.
- Out: Historical REQ/WI/evidence/audit records, archived designs, runtime code, dependencies, DB actions, and any claim that the unrecorded 43-table MySQL manifest has already been proven.

DoD:
- Current documentation says the source schema contains 43 tables/entities and that its actual MySQL manifest remains `UNRECORDED` pending WI-016.
- Current documentation records that React Router 7.18.2 removed the two prior moderate production audit findings, while retaining any non-router production readiness gates.
- Current acceptance guidance remains explicit that a public tunnel is not production deployment.
- Historical 39/41/42-table evidence is still labelled historical and unmodified.

Constraints/Forbidden:
- Do not edit code, lockfiles, scripts, databases, output artifacts, or historical deliverables.
- Do not close SR-93: database proof, real deployment, provider, backup, monitoring, and release approval remain open.
- Do not describe `npm audit` as a permanent claim; state the observed command/date/version precisely.
- Preserve English documentation language and metadata standards.

[ACCEPTANCE CRITERIA]
Documentation:
- [ ] `docs/payment/known-limits-and-next-steps.md` no longer says current V1 uses a 39-table schema.
- [ ] Active SR-42/SR-93 safety addenda no longer state the resolved Router 6 finding as current.
- [ ] No current-state document incorrectly states the 43-table MySQL manifest is recorded.
Quality:
- [ ] `python .agents/skills/validate-docs/scripts/validate_docs.py` passes.
- [ ] `git diff --check` passes.
- [ ] Focused stale-claim searches distinguish active current-state docs from historical records.

[INPUT POINTERS]
Tier 0 (Constitution - Required):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 (Policies):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2 (Current-state documents):
- docs/payment/known-limits-and-next-steps.md
- docs/payment/system-overview.md
- docs/payment/feature-inventory.md
- docs/SR/SR-42.md
- docs/SR/SR-93.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-009.md
- deliverables/agent/WI-20260817-ATS-014-evidence-pack.md
- scripts/database/README.md

Evidence:
- `npm audit --omit=dev --json` after WI-014: 0 vulnerabilities.
- `frontend/package.json`: pinned `react-router-dom` 7.18.2.
- `src/main/resources/schema.sql`: 43 derived `CREATE TABLE` statements.
- `scripts/database/DisposableMysqlBootstrap.java`: current manifest expectation `UNRECORDED`.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-015-summary.md:
- Updated documents, exact claims corrected, remaining gates.
Agent-facing -> deliverables/agent/WI-20260817-ATS-015-evidence-pack.md:
- File/line pointers, search evidence, validation output, rollback.
Handoff Packet -> deliverables/agent/WI-20260817-ATS-015-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Record document validation, diff check, and claim-search results.
Rollback: Revert only the current-document update commit; historical evidence remains untouched.

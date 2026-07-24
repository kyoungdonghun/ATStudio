[WI HEADER]
WI ID: WI-20260724-ATS-019
REQ: REQ-20260724-ATS-002
Agent: se
Depends On: WI-20260724-ATS-013
Blocks: WI-20260724-ATS-020

[WI SUMMARY]
Why: The fresh-install rehearsal currently relies on a historical helper that references retired migration files, while the current acceptance launcher and external environment-bundle contract are not documented as a reproducible operator procedure.
Scope (in/out): Add a guarded, repo-supported disposable MySQL bootstrap path that applies only current `schema.sql` then `seed.sql`; document its safety contract and the current acceptance lifecycle, Cloudflare prerequisite, external JSON allowlist, and secret-handling rules. Do not revive retired migrations or alter product schema.
DoD: An operator can create/verify/drop an approved disposable DB from a fresh clone without touching protected DB names, and can understand how to prepare/start/status/stop the acceptance environment without discovering obsolete environment keys by trial and error.
Constraints/Forbidden: Work only in a new active DB bootstrap script/test area, `scripts/acceptance/` documentation, and directly related operator docs. The tool must default to refusal, allow loopback MySQL only, require a disposable-name regex, refuse protected names including `atstudio`, never print credentials, and never enumerate/drop unrelated databases. Do not execute against production or the current protected DB.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Bootstrap applies only `src/main/resources/schema.sql` then `src/main/resources/seed.sql`.
- [ ] Create/validate/drop actions require an exact disposable database name and loopback host.
- [ ] Protected and malformed database names are rejected before any connection or mutation.
- [ ] Acceptance documentation lists current required/optional JSON keys and explicitly rejects obsolete keys.
- [ ] Cloudflared, ports, lifecycle commands, secret-file ACL, and cleanup expectations are documented.
Performance:
- [ ] Bootstrap completes within normal local MySQL setup time and does not scan unrelated databases.
Quality:
- [ ] Guard tests pass without needing MySQL.
- [ ] A disposable MySQL proof run passes when WI-013 credentials are available.
- [ ] Documentation validation and `git diff --check` pass.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1 (Policies - Inferred):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2 (Task Context):
- docs/standards/evidence-pack-standard.md
- docs/SR/SR-42.md
- docs/SR/SR-93.md
REQ/Context Docs:
- deliverables/user/REQ-20260724-ATS-002.md
- deliverables/agent/WI-20260724-ATS-013-handoff.md
Files:
- src/main/resources/schema.sql
- src/main/resources/seed.sql
- scripts/acceptance/acceptance-lifecycle.ps1
- scripts/acceptance/
- src/main/resources/application-acceptance.yml

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260724-ATS-019-summary.md :
- Summary, operator workflow, safety decisions, residual risks
Agent-facing -> deliverables/agent/WI-20260724-ATS-019-evidence-pack.md :
- Exact files/lines, commands, refusal cases, proof results, rollback
Handoff Packet -> deliverables/agent/WI-20260724-ATS-019-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Record guard tests, disposable DB proof if available, docs validation, and diff check
Rollback: Document removal of the new helper/docs without touching any database

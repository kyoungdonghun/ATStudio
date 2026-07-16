[WI HEADER]
WI ID: WI-20260716-ATS-027
REQ: REQ-20260716-ATS-002
Agent: qa-integ
Depends On: WI-20260716-ATS-022
Blocks: WI-20260716-ATS-028

[WI SUMMARY]
Why: Reconcile the cumulative code, schema, API, UI, operational, client, and WI evidence before the development-only commit.
Scope (in): Changed docs and deliverables, API/DTO/TypeScript contracts, schema/entity parity, route/screen/count claims, environment-conditional wording, generated PDF provenance, and worktree hygiene classification.
Scope (out): Historical evidence rewriting, client-demo propagation, database/provider/runtime mutation, new feature design.
DoD: 3-way mismatches and stale claims are reported with exact pointers; tracked/untracked files are classified as commit, generated/ignored, runtime, or unrelated; final validation commands are prescribed.
Constraints/Forbidden: Read-only review. Do not edit, delete, stage, commit, push, restart, or touch the client worktree/runtime. Do not expose secrets or PII from logs/config/data.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Compare current Java mappings/entities/DTOs and TypeScript contracts with authoritative design/UI/operations/client documents.
- [ ] Recheck counts, statuses, deprecation boundaries, payment provider references, public-listening/download policy, and acceptance-environment claims.
- [ ] Classify all untracked categories, including WI artifacts, demo seed scripts/output, logs, temporary files, attachments, generated reports, and tests.
Quality:
- [ ] Every mismatch includes severity and code/doc/evidence pointers.
- [ ] Environment-conditional claims are not marked closed without named evidence.
- [ ] Produce both required deliverables.

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
- docs/index.md
- docs/design/index.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/remaining-remediation-design-20260716.md
- docs/ui/index.md
- docs/registry/project-registry.md
- docs/client/index.md
REQ/Context:
- deliverables/user/REQ-20260716-ATS-002.md
- deliverables/user/WI-20260716-ATS-012-summary.md
- deliverables/user/WI-20260716-ATS-022-summary.md
Files/Repro:
- git diff -- docs deliverables src/main frontend/src
- git status --short --branch

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-027-summary.md
Agent-facing -> deliverables/agent/WI-20260716-ATS-027-evidence-pack.md
Handoff -> deliverables/agent/WI-20260716-ATS-027-handoff.md

[TRACEABILITY REQUIREMENTS]
- Use file:line pointers, command results, and a concise 3-way mismatch matrix.
- Include commit/include/exclude classification for worktree artifacts without deleting anything.
- This WI is review-only; remediation belongs to WI-028.

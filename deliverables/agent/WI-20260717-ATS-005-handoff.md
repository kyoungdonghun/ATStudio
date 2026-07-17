[WI HEADER]
WI ID: WI-20260717-ATS-005
REQ: REQ-20260716-ATS-004
Agent: docops
Depends On: WI-20260717-ATS-002, WI-20260717-ATS-003, WI-20260717-ATS-004
Blocks: WI-20260717-ATS-006

[WI SUMMARY]
Why: Make active documentation, repository tooling, ignore rules, and generated artifacts describe and support the single verified V1 baseline.
Scope (in/out): Implement INT-P10, P11, P12, INT-A01 through A03, remaining INT-R06/R11/R13 consequences, resolved INT-V11, and active-document consequences of WI-002/WI-003/WI-004. Update only current SoT documents for the 39-table fresh-only schema, current API/UI/routes, TOSS recurring-only V1 with multi-PG interfaces, explicit local config loading, six-plan seed ownership, removed manual SQL, and emergency admin operations. Archive the two completed design docs in place. Keep historical REQ/WI/SR/audit/retrospective records unchanged. Fix demo seed PowerShell credential-path behavior. Stop tracking tsconfig.tsbuildinfo and add narrow ignores while preserving tracked PDF assets and the screenshot ZIP as historical evidence. Delete approved generated copies/logs/expanded screenshots/demo output/tmp/attachment copies after source/hash/ownership verification; remove remaining frontend/public/.gitkeep if empty. Out of scope: product behavior, DB mutation, branch/worktree deletion, push, historical record normalization.
DoD: Active docs match code/schema/config; completed designs are archived in place with replacement pointers; current counts and indexes are accurate; manual SQL is described as retired with no active filename instructions; demo script has no hard-coded acceptance-preview credential path; generated artifacts are narrowly ignored/removed; tracked PDF and historical screenshot ZIP remain; docs validation and relevant tooling dry-runs pass; no product source is changed.
Constraints/Forbidden: Do not edit backend/frontend product source or tests except scripts/demo/seed-client-demo.ps1, .gitignore, and generated-cache tracking mechanics explicitly owned here. Do not modify application-local.yml or expose secrets. Do not alter historical deliverables, SR, audit, retrospective, or dated addenda. Do not delete tracked client PDF/manifest/generator/verifier or screenshot ZIP. Do not use broad output/ ignores. Do not modify Git branches/worktrees/tags/remotes. Do not revert concurrent work.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Active API/DB/UI/current-state docs match the verified V1 code and 39-table schema.
- [ ] Active docs state TOSS card recurring-only V1 and preserved multi-PG interface boundary without speculative active providers.
- [ ] Active docs state explicit local config loading, no automatic ignored-local inheritance, single six-plan seed owner, and retired manual SQL.
- [ ] Removed APIs/routes/tables/columns/symbols are absent from current SoT but historical records remain unchanged.
- [ ] Two completed design docs are archived in place with date, reason, replacement path, and index status.
- [ ] Demo seed PowerShell has no hard-coded acceptance-preview credential path and remains explicit/non-baseline.
- [ ] tsconfig.tsbuildinfo is no longer tracked and is narrowly ignored; generated logs/artifacts are cleaned and narrowly ignored.
- [ ] Tracked client PDF and screenshot ZIP remain intact; expanded/duplicate/generated copies are removed after verification.
Performance:
- [ ] Not applicable; tooling changes do not add runtime overhead.
Quality:
- [ ] validate-docs, link/index/traceability checks, demo-script dry-run/contract tests, git diff --check, and secret scan pass.
- [ ] Current API/table/screen counts are derived and internally consistent.
- [ ] Negative searches for stale branch/manual-SQL/provider/route/version wording pass in active docs.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1:
- docs/policies/versioning-policy.md
- docs/policies/archive-policy.md
- docs/policies/quality-gates.md
- docs/policies/security-policy.md

Tier 2:
- docs/index.md
- docs/design/index.md
- docs/registry/
- docs/guides/
- docs/client/

REQ / Decision Sources:
- deliverables/user/REQ-20260716-ATS-004.md
- deliverables/agent/WI-20260717-ATS-001-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-038-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-037-evidence-pack.md
- deliverables/agent/WI-20260717-ATS-002-evidence-pack.md
- deliverables/agent/WI-20260717-ATS-003-evidence-pack.md
- deliverables/agent/WI-20260717-ATS-004-evidence-pack.md

Files / Artifacts:
- docs/ (active SoT only; preserve historical categories)
- scripts/demo/seed-client-demo.ps1
- scripts/demo/seed-client-demo.mjs
- .gitignore
- frontend/tsconfig.tsbuildinfo
- frontend/public/.gitkeep
- cloudflared.err.log
- cloudflared.out.log
- frontend/vite.err.log
- frontend/vite.out.log
- .codex-remote-attachments/
- output/client-demo-screenshots-20260716-140514.zip
- output/client-demo-screenshots-20260716-140514/
- output/demo-seed/
- output/pdf/
- tmp/

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260717-ATS-005-summary.md
Agent-facing -> deliverables/agent/WI-20260717-ATS-005-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260717-ATS-005-handoff.md

[TRACEABILITY REQUIREMENTS]
Map each edit/deletion to INT-P10/P11/P12, INT-A01/A02/A03, INT-R06/R11/R13, or INT-V11. Record active-vs-historical classification, derived counts, file hashes/counts before deletion, preserved assets, ignore checks, commands/results, stale-reference searches, risks, and rollback. Use create-wi-evidence-pack after implementation.

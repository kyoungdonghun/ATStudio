# WI-20261121-ATS-008

[WI HEADER] REQ-20261121-ATS-003 approved. Agent QA integration. Depends on WI003–005 local candidate. Parallel independent review.

[WI SUMMARY] Independent cross-layer review and fresh local integration tests. Check nginx template and default config, API contracts, user isolation first/repeat including unpublished/republished, grant change, security boundaries and cache headers. Own integration test changes and edge fixes. Coordinate backend/frontend findings with owners; retest final candidate.

[ACCEPTANCE CRITERIA] Distinguish confirmed defects and missing evidence, identify corrected behaviors, verify exact final candidate with focused checks. Use defensive code inspection and ordinary application workflows with synthetic local users. Preserve production, other worktree edits, and prior evidence. No deployments or external systems.

[INPUT POINTERS] Tier 0 docs/standards/core-principles.md, development-standards.md, documentation-standards.md and glossary.md. Tier 1 docs/policies/security-policy.md and quality-gates.md. Tier 2 B1 roadmap in deliverables/user/REQ-20261104-ATS-002.md, current docs/index.md entry, WI003–005 evidence and changed source and tests. Reuse existing test tools. Report exact fresh results.

[OUTPUT CONTRACT] English WI summary and evidence pack; findings with pointers, tests, unresolved limitations and local rollback. Report to MA.

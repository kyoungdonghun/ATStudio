---
version: 1.0
last_updated: 2026-11-06
project: ATS
owner: MA
status: delegated
---
# WI-20261106-ATS-005 — Commit candidate privacy review

[WI HEADER]
WI ID: WI-20261106-ATS-005
REQ: REQ-20261106-ATS-003
Agent: pg
Depends On: approved REQ
Blocks: WI006 final EO shareability / MA Git execution

[WI SUMMARY]
Why: Preserve completed work without leaking private operational evidence or identities.
Scope: All visible changed/untracked paths triage, then exact selected candidate blob review. Reuse prior scanner/decisions; record hashes, exclude unsafe originals, classify fixture matches without echoing values.
DoD: Explicit safe include/exclude lists with reasons, per-finding disposition and final exact-hash PG clearance; not a pattern-only claim.
Forbidden: Network/server/cloud/provider/production/service/DB/Git writes. Do not read ignored/private credential bodies; no raw matched values in output. Do not modify implementation or historical evidence. Only write WI005 deliverables and local redacted scan/manifests under build/WI-20261106-ATS-005. Coordinate redaction/export needs with WI006; preserve originals.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Check token/key/password/PII/internal endpoint/private reference issues, binary unknowns, sensitive documents/screenshots.
- [ ] Establish prior exclusion reasons; do not blindly add all untracked historical evidence.
- [ ] Exact file/hash reviews and synthetic fixture reasons; provide candidate list or per-file dispositions to EO.
- [ ] Final selected staged/committed content scan before push, with no secret echo.
Performance:
- [ ] Visible candidate files only; bounded reads, no ignored/private tree traversal.
Quality:
- [ ] Reuse scripts/reconstruction/scan_commit_candidates_wi004.py; understand its limits and fixed old WI label.
- [ ] Evidence Pack and user summary, counts and risks clear. Do not broaden tools/installations.

[INPUT POINTERS]
Tier 0: AGENTS.md; docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md
Tier 1: docs/policies/security-policy.md; docs/policies/quality-gates.md; docs/policies/execution-policy.md
Tier 2: deliverables/user/REQ-20261106-ATS-003.md; docs/index.md Current Decision Entry; scripts/reconstruction/scan_commit_candidates_wi004.py; .gitignore; prior security/commit deliverables from 2026-10-26; latest WI-20261106-ATS-003/004 and WI-20261105-ATS-007 evidence.
Snapshot: branch main, HEAD f0c73d449e4f2773ad91cb2fb34093ddd57cd741, 56 tracked changes +423 untracked before new REQ/WIs, no staged files. Last commit 2026-10-26. HTTPS GitHub origin; no .github directory locally.

[OUTPUT CONTRACT]
deliverables/agent/WI-20261106-ATS-005-evidence-pack.md and deliverables/user/WI-20261106-ATS-005-summary.md via evidence skill; build/WI-20261106-ATS-005 sanitized scans and exact dispositions. Communicate to MA and checkpoint_prepare. Final clearance must bind selected hashes; initial broad triage is not final approval.

[TRACEABILITY REQUIREMENTS]
Record scanner invocation, exact selected scope, redacted category/line findings and justification. Original private evidence untouched. Separate cleared source from intentionally retained local docs/binaries. Report any real credential finding privately as file/category only; no rotation or history mutation authority.

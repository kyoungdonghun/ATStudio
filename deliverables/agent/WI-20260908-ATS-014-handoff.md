---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: MA
category: reference
status: active
---

# WI-20260908-ATS-014: Main Baseline Promotion and Preserved Cleanup

[WI HEADER]
WI ID: WI-20260908-ATS-014
REQ: REQ-20260908-ATS-003
Agent: docops (current documentation); MA (critical Git operations)
Depends On: WI-20260908-ATS-013
Blocks: none

[WI SUMMARY]
Why: The user approved one actual branch named main, after remote preservation.
Scope: Archive exact old tips and client dirty files, promote verified 5328482 code to main, remove approved obsolete refs/client worktree, align current documentation.
DoD: Remote archives verified before deletion; local/remote main agree; current docs distinguish Git baseline from deployment approval; unrelated data and runtime preserved.
Forbidden: Product changes, legacy policy merges, server restart, DB/media/secret modifications, external mail/payment actions, blanket clean/reset, security-audit claims.

[ACCEPTANCE CRITERIA]
Functional:
- [x] All obsolete branch tips and client HomePage dirty edits recoverable from remote tags.
- [x] main is the only local/remote work branch and remote default.
- [x] Only the root worktree remains; ignored client content and process ownership checked before removal.
- [x] Current docs name main and retain dated evidence and outstanding production gates.
Performance:
- [x] No build or runtime interruption required for Git/document-only changes.
Quality:
- [x] validate-docs and git diff --check pass.
- [x] Product tree matches 5328482; pre-existing unrelated untracked files remain byte-identical.
- [x] Local/public HTTP and original process identities checked without mutation.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/versioning-policy.md
- docs/policies/security-policy.md
Context:
- deliverables/user/REQ-20260908-ATS-003.md
- deliverables/agent/WI-20260908-ATS-013-branch-decision-register.md
- .claude/config/workspace.json
DocOps exclusive write scope:
- docs/index.md
- docs/client/testing-guide.md
- docs/client/_internal-feature-map.md
- docs/payment/feature-inventory.md
- docs/payment/index.md
- docs/SR/SR-93.md
- deliverables/agent/WI-20260908-ATS-014-doc-review.md
MA scope:
- REQ003 and WI014 handoff/evidence/summary, WI013 historical decision-register addendum.
- Git preservation, publication, exact-ref cleanup, read-only runtime verification.

[OUTPUT CONTRACT]
User-facing: deliverables/user/WI-20260908-ATS-014-summary.md (MA integration).
Agent-facing: deliverables/agent/WI-20260908-ATS-014-evidence-pack.md (MA integration).
DocOps review: changed paths, historical versus current boundary, validation results; no Git writes.
Handoff: this file, generated using create-wi-handoff-packet skill structure.

[EXECUTION NOTES]
- main already is GitHub default, but points to independent README-only 736fdc4. Preserve it first; use exact-SHA lease, not unrelated-history merge.
- Client worktree has two dirty HomePage files; archive their snapshot without importing its temporary thumbnail policy into main.
- Snapshot counts/tips in WI013 remain historical, not a description of the post-promotion state.
- DocOps may edit approved target-state documentation while MA completes Git operations; MA must verify actual promotion before committing those claims. No waiting loop.

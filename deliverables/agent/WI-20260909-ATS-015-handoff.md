# WI-20260909-ATS-015

[WI HEADER]
REQ: REQ-20260909-ATS-002 (approved)
Agent: re
Depends On: WI-20260909-ATS-014
Blocks: none

[WI SUMMARY]
Why: Prepare a narrow, reproducible test-runtime application of the verified remediation.
Scope: A repo-external launcher based on the observed pinned Gmail launcher, plus this WI's evidence and summary. MA owns Git, read-only DB preflight, process stop/start and HTTP checks.
DoD: Launcher inspected/parse-tested, no secrets embedded, old runtime preserved; later supplied MA execution evidence recorded truthfully.
Forbidden: No source fixes, no commit/push, no executing launchers, no stopping processes, no real Provider/SMTP requests, no DB/data/secret changes, no unrelated artifact edits.

[ACCEPTANCE CRITERIA]
- Preserve existing runtime tuple and all current arguments, including audit-on-startup/non-strict historical media handling.
- New launcher takes explicit absolute verified JAR path and SHA256, expected public HTTPS origin; validates ownership/prerequisites, port availability and environment. Use existing secrets in memory only.
- Preserve scheduler safety; report any existing enabled scheduler before proposing startup.
- Start-Process uses WindowStyle Hidden, logs in the same runtime, correct working directory. Never overwrite old launcher/JAR.
- Pending external/user acceptance is not called PASS.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
Tier 2:
- docs/design/payment-operations-runbook.md#source-bound-command-rollout-2026-09-09
- docs/design/runtime-storage-operations.md
Context:
- deliverables/user/REQ-20260909-ATS-002.md
- deliverables/user/WI-20260909-ATS-014-summary.md
- output/release-remediation-20260909/runtime-final-readonly.json
- C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908/start-backend-gmail.ps1

[OUTPUT CONTRACT]
- C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908/start-backend-remediation.ps1 (apply_patch only)
- deliverables/agent/WI-20260909-ATS-015-evidence-pack.md
- deliverables/user/WI-20260909-ATS-015-summary.md
- Send launcher readiness first; wait for MA evidence before finalizing deployment claims.

[TRACEABILITY REQUIREMENTS]
- Follow create-wi-evidence-pack skill. Reference Tier inputs, commands, exact observed outcomes and boundaries.
- Rollback requires monetary admission closure and new/old unfinished-command review; do not blindly restart the old JAR after new monetary activity.

## Approved Documentation Follow-up / 2026-09-09

[WI HEADER]
REQ: REQ-20260909-ATS-002 (documentation follow-up approved)
Agent: docops (follow-up only; original runtime task remains re-owned)
Depends On: completed WI015 runtime application and supplied user screenshots
Blocks: none

[WI SUMMARY]
Why: Close the requested TEST-runtime user check and remove stale current-state wording, without rewriting historical evidence.
Scope: Current source/runtime entry points, limited acceptance record, WI015 evidence/summary and REQ002 follow-up completion. MA separately inventories the unchanged untracked files and reads cleanup history.
DoD: User screenshots are recorded as supplied UI evidence, not fresh DB/provider verification; source main and applied product revision are distinct; production SR-93 HOLD is preserved; dated historical checkpoints remain intact.
Forbidden: No source edits, new files, delete/move/ignore changes, Git execution, server actions, external requests, DB/media/secret changes, or broad audits. Do not claim 205 files were cleaned or archived.

[ACCEPTANCE CRITERIA]
- Record logged-in UI, DELUXE YEARLY paid access through 2027-09-08 with renewal cancelled, and download-complete/19 of 20 remaining as user screenshot evidence only.
- Point current state to product revision 2d47504 applied to TEST runtime and document checkpoint 1c467d1; do not imply production deployment or a new full suite.
- Correct stale currently worded not-deployed, re-login-pending or untested-mail claims using dated superseding updates. Preserve historical sections and test counts.
- Receive MA's untracked-file/cleanup findings for a concise inventory section, not new artifacts.
- Documentation validation and diff check must pass; MA performs final validation and exact-path commit/push.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md
Tier 1: docs/policies/archive-policy.md
Tier 2: .agents/skills/create-wi-evidence-pack/SKILL.md; .agents/skills/validate-docs/SKILL.md
Context: deliverables/user/REQ-20260909-ATS-002.md; WI015 existing evidence/summary; MA-supplied screenshot observations and Git inventory.

[OUTPUT CONTRACT]
Allowed existing documents only:
- docs/index.md
- docs/SR/SR-93.md
- docs/payment/index.md
- docs/payment/acceptance-test-checklist.md
- docs/payment/known-limits-and-next-steps.md
- deliverables/user/REQ-20260909-ATS-002.md
- deliverables/user/WI-20260909-ATS-015-summary.md
- deliverables/agent/WI-20260909-ATS-015-evidence-pack.md
Report exact edited paths. Reuse these two evidence/summary deliverables; no new WI, REQ or log files.

[TRACEABILITY REQUIREMENTS]
Follow create-wi-evidence-pack format in the existing evidence document. Separate supplied user UI observations, prior MA runtime evidence and current MA Git inspection. MA adds actual final checks and commit identifiers after verification. Rollback is a documentation-only revert of exact changed paths; no runtime rollback.

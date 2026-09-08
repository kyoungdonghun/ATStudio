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

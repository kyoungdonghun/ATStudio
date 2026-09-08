---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: docops
category: reference
status: active
dependencies:
  - path: ../user/REQ-20260908-ATS-002.md
    reason: Approved backend Gmail restoration and one website email
---

# WI-20260908-ATS-012: Backend Gmail runtime restoration

[WI HEADER]
REQ: REQ-20260908-ATS-002, latest WI012 approval.
Agent: docops; MA owns runtime helper, restart and browser verification.
Depends On: WI-20260908-ATS-011
Blocks: -

[WI SUMMARY]
Record recipient evidence for WI011 and MA-supplied runtime results for WI012. This is not a production release or a repeat of financial tests.
No product or credential source changes. Backend-only restart, preserve JAR/DB/storage/public origin/frontend/Tunnel. One normal forgot-password submission to the already authorized test recipient; no password reset submission or financial operations.

[ACCEPTANCE CRITERIA]
- Record the three supplied smartphone images: Korean templates readable, test numbers 1/3, 2/3, 3/3 and Inbox labels; do not generalize deliverability.
- Record explicit external SMTP import and configuration precedence, process ownership and unchanged protected runtime surfaces.
- Distinguish actual backend delivery log from generic HTTP/UI success and recipient receipt.
- Validate affected docs and preserve all historical evidence and production OPEN gates.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md, documentation-standards.md, development-standards.md, glossary.md.
Tier 1: docs/policies/security-policy.md, .claude/agents/docops.md.
Tier 2: REQ002, WI011 evidence/summary, src/main/java/com/atstudio/atstudio/service/EmailService.java, src/main/java/com/atstudio/atstudio/controller/AuthController.java.
Skills: create-wi-handoff-packet, create-wi-evidence-pack, validate-docs. Project/routing JSON checked by MA.

[WRITE OWNERSHIP]
WI011 evidence-pack and summary receipt status only; WI012 evidence-pack and user-summary; REQ002 WI011 receipt and WI012 current status only. Preserve earlier unrelated changes. No runtime/config/script/Git actions, external calls, sensitive values, or nested agents. MA creates the generated packet and runtime-only helper outside the repo.

[OUTPUT CONTRACT]
deliverables/agent/WI-20260908-ATS-012-evidence-pack.md and deliverables/user/WI-20260908-ATS-012-summary.md. Use the evidence-pack skill. Wait for MA facts before finalizing; no re-verification of SMTP/network by agent. Final output <=10 lines.

[TRACEABILITY]
Include inspected pointers, MA-supplied timestamps, no-secret config key names and remaining user confirmation; no recipient address or auth tokens. Rollback is backend-only configuration re-launch, never DB rollback. No next WI is blocked.

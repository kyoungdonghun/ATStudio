---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: docops
category: reference
status: active
dependencies:
  - path: ../user/REQ-20260908-ATS-002.md
    reason: Explicit approval for three SMTP test messages
---

# WI-20260908-ATS-011: Korean SMTP receipt verification

[WI HEADER]
REQ: REQ-20260908-ATS-002, latest three-message approval.
Agent: docops; MA owns isolated runtime invocation and SMTP.
Depends On: WI-20260908-ATS-010
Blocks: -

[WI SUMMARY]
Scope: Record the actual three-message send result supplied by MA, preserving the original source/runtime evidence. User receipt and Spam/Inbox placement remain pending until user confirmation.
DoD: Exact source-artifact identity, accepted/failed/unknown send count, test-only marker, absence of DB/provider operations and outstanding recipient confirmation are recorded truthfully.
Forbidden: Agent SMTP/network/runtime/Git actions, credentials or recipient PII in documents, product code, new APIs, DB, process restart, nested agents, automatic resend or claims of scheduler/payment/production verification.

[ACCEPTANCE CRITERIA]
- [ ] Record three authorized scenarios using actual EmailService templates with clearly synthetic message inputs.
- [ ] Distinguish SMTP acceptance from actual recipient receipt and Inbox placement.
- [ ] Preserve previous evidence and production gates; validate only changed documentation.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md, documentation-standards.md, development-standards.md, glossary.md.
Tier 1: docs/policies/security-policy.md, .claude/agents/docops.md.
Tier 2: REQ002, WI006/008/010 evidence, src/main/java/com/atstudio/atstudio/service/EmailService.java, docs/payment/acceptance-test-checklist.md.
Skill chain: create-wi-handoff-packet, create-wi-evidence-pack, validate-docs. Tag/routing checked in .claude/config/workspace.json and context-injection-rules.json.

[WRITE OWNERSHIP]
Only this WI's evidence-pack and user-summary, plus REQ002 latest WI011 approval/status. MA creates this generated packet. No other files may be edited.

[OUTPUT CONTRACT]
deliverables/agent/WI-20260908-ATS-011-evidence-pack.md and deliverables/user/WI-20260908-ATS-011-summary.md. Keep brief; do not finalize until MA supplies results. Follow-up recipient confirmation does not require another mail send.

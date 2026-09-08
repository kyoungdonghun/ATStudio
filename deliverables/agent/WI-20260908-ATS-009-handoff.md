---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: docops
category: reference
status: active
dependencies:
  - path: ../user/REQ-20260908-ATS-002.md
    reason: Approved copy work and subsequent runtime adoption approval
---

# WI-20260908-ATS-009: Restart and verify copy delivery

[WI HEADER]
REQ: REQ-20260908-ATS-002, runtime follow-up approved by the user's subsequent request to restart, verify, and finish these changes.
Depends On: WI-20260908-ATS-008
Blocks: -
Agent: docops; MA owns process control and verification.

[SUMMARY / DoD]
Record the newly approved runtime adoption separately from prior source-only evidence. MA will verify the owned development backend/frontend, preserve the current tunnel, restart the tested build with the same DB/storage/SMTP/callback environment, and perform read-only HTTP and isolated UI checks. No real mail/payment/refund, DB/schema mutation, client worktree, commit or production GO. Startup storage recovery is an existing runtime behavior; MA checks its pending rows before restart.

[CONTEXT]
Tier 0: injected core-principles, development-standards, documentation-standards, glossary.
Tier 1: security-policy, .claude/agents/docops.md.
Tier 2: REQ002, WI008 evidence, docs/payment/index.md, docs/design/runtime-storage-operations.md.
Generated using create-wi-handoff-packet; config sources .claude/config/workspace.json and context-injection-rules.json. Skills: create-wi-evidence-pack, validate-docs.

[WRITE OWNERSHIP]
Only REQ002 approval addendum/progress, a concise later runtime note in docs/payment/index.md and docs/payment/acceptance-test-checklist.md, own WI009 evidence-pack and user summary. Preserve earlier dated evidence and unrelated dirty work. Do not change product code, runtime settings, secrets, existing runtime manifest, or the client worktree. Use apply_patch for manual edits; no nested agents.

[OUTPUT]
Wait for MA's exact restart/HTTP/UI evidence before marking runtime verification passed. Evidence must identify new PIDs, actual artifact hash/path, preserved environment and limits. No fresh SMTP receipt or provider success claim. Source tests were already completed in WI008 and are not rerun merely for counts. Complete this terminal WI after docs validation.

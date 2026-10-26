---
version: 1.0
last_updated: 2026-10-17
project: ATS
owner: MA
category: reference
status: active
dependencies:
  - path: WI-20261011-ATS-067-handoff.md
    reason: Existing approved local implementation and ownership
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Approved bounded Gmail validation continuation
---
# WI067 Private Producer Continuation

[WI HEADER]
WI: WI-20261011-ATS-067; REQ: REQ-20261011-ATS-007; Agent: se.
Depends On: Existing exact runtime and delivery freezes; native use requires accepted backup/restore and live release.
Blocks: PG071 producer review and MA private staging.

[WI SUMMARY]
Why: Close the final operator-input gap without copying existing server DB/JWT/keyring secrets to the PC.
Scope: Own new build/WI067-private-producer and create-new sibling evidence directories, plus existing WI067 reports. Preserve all existing runtime/delivery freezes and handoffs. Reuse pinned reviewed ACL/SSH/framing helpers and native original_env. Implement only a fixed, bounded mail/profile producer and operator adapter.
DoD: Local synthetic tests, exact freeze, narrow PG071 review, executable MA procedure. PC reads only the three approved Gmail fields; remote fixed producer validates runtime/release, obtains nine base fields in memory, frames original input to unchanged entry.py --stage through an owned 0600 pipe. No new secret file or credential on argv/output. Inert default, strict host/key, fixed target, deadlines, no replay, fixed sanitized outcomes.
Forbidden: Agent private-file reads or remote/native operations; new account password/consent; changes to original runtime or delivery; arbitrary destinations; purchases; app startup; Gmail sends; public release; manufactured evidence.

[ACCEPTANCE CRITERIA]
- [ ] Read only approved MAIL_USERNAME/MAIL_PASSWORD/MAIL_FROM from owner-only PC JSON, map to username/password/sender, validate duplicate keys/types/bounds without exposing values.
- [ ] Send only mail/profile to the server; original_env reads server base in memory under original gates. Preserve original stage parser and all security gates.
- [ ] Test framing, duplicate/wrong fields, target/pins, timeouts, uncertain side effects, pipe ownership and sanitization with synthetic values.
- [ ] Pass exact-byte independent review before MA runs; report measured coverage scope honestly.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; documentation-standards.md; development-standards.md; glossary.md, already injected in full.
Tier 1: docs/policies/security-policy.md; quality-gates.md; access-control-policy.md.
Tier 2: .agents/skills/create-wi-handoff-packet/SKILL.md; test/SKILL.md; create-wi-evidence-pack/SKILL.md.
Context: Existing WI067 and WI071 handoffs; build/WI067-pc-delivery/README.md; build/WI067-auth-trial/binding/native.py, transport.py, entry.py; build/WI069-backup/invoke-bootstrap.ps1.
Reduced MA observations: 758e9f/3791ba confirm protected PC JSON is a string-valued object; MAIL_USERNAME, MAIL_PASSWORD and MAIL_FROM exist, have no outer quote wrapper and no CR/LF. Values were not output. Public staging completed d32264; target JAR/tool/SPA counts match 1f8599. No private staging or services started.

[OUTPUT CONTRACT]
Append evidence and summary to existing WI067 two-set reports. Return new producer freeze, test receipts and exact MA commands, then immediately send PG071 the narrow new surface. Do not delay on unrelated historical review.

[TRACEABILITY]
Skill-generated continuation, not new architecture or expanded native authority. Existing approved secret-use scope remains MA-only. Prior files, hashes and failures remain immutable. Backup/restore PASS, live release and actual human readiness are separate mandatory gates.

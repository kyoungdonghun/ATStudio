---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: guide
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Approved autonomous deployment scope
  - path: WI-20261011-ATS-055-handoff.md
    reason: Candidate under independent review
---

# WI-20261011-ATS-056 Handoff

[WI HEADER]
WI ID: WI-20261011-ATS-056
REQ: REQ-20261011-ATS-007 (approved)
Agent: pg
Depends On: WI055 candidate (early review allowed; final freeze required for verdict)
Blocks: WI058 MA native trial

[WI SUMMARY]
Why: Independently verify the combined diagnostics, HTTP result contract and durable cleanup before native use.
Scope: Read WI055 working candidate while SE finishes tests; provide exact findings early and review final pinned bytes. Create independent synthetic regressions in your own folder. No server action.
DoD: Verdict tied to final bundle/recipes; independent negative tests of actual candidate behavior; preservation verification and precise native preconditions.
Forbidden: Remote/UI/SSH/DB/cloud/secret access; edits to SE candidate or historical artifacts; installs/purchases; weakening comparators; native execution; commits/push.

[ACCEPTANCE CRITERIA]
- [ ] Scope matches REQ007: management line only, read-only trial, max30min, existing secrets internal, no mail/payment/jobs, app/DB stop and original HTTPS restoration.
- [ ] Audit actual original-to-new diff; fresh names, no stale imports/claims/pins. Preserve complete diagnostic projection and failure precedence.
- [ ] HTTP child-process exit/result contract fails closed on mismatched success/exit, null/stale LASTEXITCODE, partial/malformed result, transport errors. Review exact byte UTF8/LF transport, not a source-only promise.
- [ ] Cross-filesystem exact-byte durable preservation/no-overwrite/no-link checks, source recheck and same-directory detach, parent fsync ordering; safe independent cleanup continues on failure without false success or unsafe start.
- [ ] Security comparisons including BPF identity remain unchanged. Do not hide POST_STATE_CHANGED. Distinguish benign ordering already approved for Nginx restoration from original policy comparison.
- [ ] Run independent synthetic cases and relevant inherited tests, classify platform limits, final hash verification. No final ALLOW against a mutable/unfrozen candidate.

[INPUT POINTERS]
Tier0 MA injected projection: docs/standards/core-principles.md; docs/standards/documentation-standards.md; docs/standards/development-standards.md; docs/standards/glossary.md.
Tier1: docs/policies/security-policy.md; docs/policies/access-control-policy.md; docs/policies/quality-gates.md; .claude/agents/pg.md.
Tier2: REQ007; WI055 handoff; WI045 actual failure/receipt; WI048/049 restoration evidence; WI052/v2 and WI053 final local diagnostic review. New build/WI055-restricted-web/** only for candidate.
Skills: test; create-wi-evidence-pack.

[OUTPUT CONTRACT]
Only build/WI056-restricted-web-review/**, deliverables/agent/WI-20261011-ATS-056-evidence-pack.md, deliverables/user/WI-20261011-ATS-056-summary.md. apply_patch manual edits. Coordinate findings via MA, no direct SE edits or subdelegation. Send specific findings early, final two-set evidence after frozen review.

[TRACEABILITY REQUIREMENTS]
Report source lines, actual failure reproduction, test counts with overlap/OS, exact pins and native limits. MA owns access/current host/boot/firewall/recovery verification and future actual execution.

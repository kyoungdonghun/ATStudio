---
version: 1.0
last_updated: 2026-10-21
project: ATS
owner: MA
category: reference
status: active
---
# WI007 Independent SMTP Repair Review

[WI HEADER]
REQ: REQ-20261011-ATS-007, current user-approved bounded repair/trial continuation.
Agent: PG. Depends on WI006. Blocks MA native mutation and trial.
Created using create-wi-handoff-packet.

[WI SUMMARY]
Review WI006 design before implementation and exact frozen candidate afterwards. Ensure DNS rotation is handled by a bounded consistent app-resolution/network-policy contract, not relaxed destination checks. Check actual Java17 behavior, TLS identity/STARTTLS, immutable mapping ownership, phase-specific fail-closed cleanup, secret-safe diagnostics and preserved historical evidence. Avoid a new general orchestration framework.

[ACCEPTANCE CRITERIA]
Independent regression and actual-runtime probe review; reject wildcard SMTP access, disabled TLS validation, extra recipients, secret logging and unapproved public routes. No server/user unlock is requested until ready for a single meaningful native proof. Report exact hashes, findings, allowed invocation and limitations. Require actual native preflight/probe before human signup readiness.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md, docs/standards/documentation-standards.md, docs/standards/development-standards.md, docs/standards/glossary.md. Tier1: docs/policies/security-policy.md. Tier2: WI006 handoff, WI004 evidence, REQ007, docs/index.md; v7 and new v8 candidate directories.

[OUTPUT CONTRACT]
Own new build/WI071-review/pg-dns-v8-* review files, WI007 evidence-pack and user summary. No remote access, private input reads, implementation edits, destructive operations or purchases. Preserve old reviews. Use create-wi-evidence-pack. Immediately return blockers to MA/SE; distinguish local acceptance from target acceptance.

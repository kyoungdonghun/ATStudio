---
version: 1.0
last_updated: 2026-10-21
project: ATS
owner: MA
category: reference
status: active
---
# WI006 Bounded SMTP Resolution Repair

[WI HEADER]
REQ: REQ-20261011-ATS-007, renewed by the user's October21 request to repair Gmail IP handling without relaxing security and continue the bounded signup trial.
Agent: SE. Depends on WI004 native failure evidence. Blocks WI007 independent PG review and MA native execution.
Created using create-wi-handoff-packet.

[WI SUMMARY]
Diagnose and repair the mismatch between repeated dynamic DNS observations and the application's restricted SMTP destination set. Prefer one fresh per-trial resolution shared by the JVM and existing nft/BPF restrictions; preserve hostname TLS verification and reject unknown destinations. Do not merely remove DNS comparison or allow all port587 destinations. First provide a concise design/delta for PG assessment, then implement the smallest reviewed direction in new v8 candidate directories. Preserve every v7 freeze and historical record.

[ACCEPTANCE CRITERIA]
- Reproduce rotated answers locally with a real Java17 name-resolution probe, not only Python mocks.
- Same bounded IP set is used by app resolution and nft/BPF. Keep port, UID, time, principal, mail budget and public-access limits.
- Missing/tampered mapping, unexpected hosts, stale release, IPv6 and private destinations fail closed; TLS hostname verification and STARTTLS remain enforced.
- Add fixed-code phase diagnostics without requests, tokens, secrets or private profile values; address preflight-only cleanup behavior only as needed to preserve the original quarantine safely.
- Freeze exact runtime/transport/producer candidates and provide focused regression results, hashes, native preflight/probe and preservation plan. No new user unlock until local preparation/review is complete.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md, docs/standards/documentation-standards.md, docs/standards/development-standards.md, docs/standards/glossary.md. Tier1: docs/policies/security-policy.md.
Tier2: docs/index.md; REQ007; WI004 evidence; build/WI067-auth-trial-v7, build/WI067-pc-delivery-v7, build/WI067-private-producer-v7; frozen profile-retry preservation.
MA evidence: 4520a5 restored exact original quarantine and read-only preflight reproduced DNS_CHANGED; 59e066 confirms DNS set mismatch. Original controller only records PREFLIGHT HOLD/NOT_RUN, so exact historical exception is not proven. c5b491 app/MySQL PID0, quarantine Nginx active, root console logged out. 10e511 SSH agent Stopped/Disabled. Original quarantine marker retains old IP and is separately fail-closed behind provider current-IP restriction; do not widen it.

[OUTPUT CONTRACT]
Own new build/WI067-auth-trial-v8, build/WI067-pc-delivery-v8, build/WI067-private-producer-v8 and narrowly named v8 supporting local candidate/probe folders, plus WI006 evidence-pack and user summary. No remote access, private credential reads, source/JAR changes, purchases, actual mail, commits or public exposure. Report remaining native proof explicitly. Use create-wi-evidence-pack. PG review blocks execution.

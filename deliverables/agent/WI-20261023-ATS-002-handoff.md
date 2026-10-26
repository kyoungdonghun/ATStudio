---
version: 1.0
last_updated: 2026-10-23
project: ATS
owner: MA
category: reference
status: active
---
# WI-20261023-ATS-002

Generated using create-wi-handoff-packet after explicit user approval on 2026-10-23 under approved active REQ007.

[WI HEADER]
WI ID: WI-20261023-ATS-002
REQ: REQ-20261011-ATS-007
Agent: SE operations
Depends On: -
Blocks: WI-20261023-ATS-003

[WI SUMMARY]
Why: Prepare a minimal phase-1 existing-account read-only inventory and DB-only lifecycle procedure.
Scope/DoD/Constraints: Own build/WI20261023-002-account-preflight/ and own evidence/user summary only. No remote access by agent. Reuse successful build/WI067-ledger-diagnostic-v2 and existing transport/helper lifecycle patterns, inspect before designing. Prepare exact executable/operator recipe for MA: connect approved target, verify cold app/worker/quarantine and target/JAR identity, temporarily start only local-only MySQL if needed, SELECT-only read-only transaction, stop and restore original DB mask/state in finally independently even failures. Never start/unmask app or run audio/auth entry. Query existing approved test-account candidates and aggregate eligibility with minimum nonsecret output; no password/token values or hashes, personal names/emails, payment bodies. Resolve first-admin and latest designated auth-test via existing sanitized receipt IDs if possible; no broad personal data dump or infer ownership from arbitrary users. Distinguish identity candidate from actual approved fixture. Source-backed eligibility for ADMIN + entitled USER with daily quota + unentitled USER; ACTIVE or CANCELLED inclusive expiry; local password/email verification eligibility without reading password hash. Existing pending/claimed audio/journals must be quiet before future phase2. Report role/account/entitlement proposals only. Local transport can preserve existing access constraints; no new libraries, architecture, broad framework. Native system/log effects allowed under user scope but no business writes. Add narrow command-order/failure cleanup and query read-only tests, genuine exact candidate receipt. Need operational readiness and explicit unresolved access prerequisites before human unlock.

[ACCEPTANCE CRITERIA]
- Functional: complete scoped implementation or evidence, no implied phase2 authority.
- Performance: bounded queries/operations; no full trial replay or unnecessary broad suite.
- Quality: focused tests and exact source/candidate identity; independent review before MA remote mutation.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/documentation-standards.md; docs/standards/development-standards.md; docs/standards/glossary.md.
Tier 1: docs/policies/security-policy.md; docs/policies/quality-gates.md.
Tier 2: deliverables/user/REQ-20261011-ATS-007.md; docs/index.md current entry; deliverables/agent/WI-20261021-ATS-009-evidence-pack.md; deliverables/user/WI-20261021-ATS-012-summary.md.
Injected principles: Korean conversation; English evidence/docs except Korean REQ; approved bounded work only; preserve unrelated work and historical evidence; no secrets/PII in outputs; pointers and measured evidence; source/JAR identity first. Exact user approval on 2026-10-23 supersedes prior local-only restriction only for MA's phase-1 server access and temporary DB start/read/stop. No app, login, business-data mutation, payment, publication, installation, deletion, commit or push. Subagents remain local only.
[TRACEABILITY REQUIREMENTS]
Use create-wi-evidence-pack. Record precise hashes, test commands/results, current versus historical evidence, limits and preservation rollback. No fabrication of operational approval or runtime PASS.

Task pointers: build/WI013-audio-native; build/WI010-audio-trial; build/WI011-audio-review/review-receipt-20261023.json; build/WI067-auth-trial-v8r1; build/WI067-pc-delivery-v8r1; build/WI067-ledger-diagnostic-v2; build/WI068-release-run-02/source-before.json.

[OUTPUT CONTRACT]
Agent-facing: deliverables/agent/WI-20261023-ATS-002-evidence-pack.md.
User-facing: deliverables/user/WI-20261023-ATS-002-summary.md.
Implementation ownership as scoped above. Completion immediately triggers blocked WI; MA owns remote execution.


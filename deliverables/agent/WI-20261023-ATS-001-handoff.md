---
version: 1.0
last_updated: 2026-10-23
project: ATS
owner: MA
category: reference
status: active
---
# WI-20261023-ATS-001

Generated using create-wi-handoff-packet after explicit user approval on 2026-10-23 under approved active REQ007.

[WI HEADER]
WI ID: WI-20261023-ATS-001
REQ: REQ-20261011-ATS-007
Agent: SE
Depends On: -
Blocks: WI-20261023-ATS-003

[WI SUMMARY]
Why: Implement streamed-body versus derivative-file byte count/SHA-256 linkage in a new preserved-success audio candidate.
Scope/DoD/Constraints: Own build/WI20261023-001-audio-hash/ and own evidence/user summary only. Reuse exact WI013 manifest3c837f740160d6a54fb3093d75e2148101efe9f67c11b39b615f3086598b48f2 and WI010/v8r1; preserve original trees unchanged. Inspect builder/provenance assumptions: new overlay changes must be honestly represented, copied-source verification must remain valid. Compare whole HTTP stream body hash and bytes with the same trial track's derivative in final file snapshot before any aggregate AUDIO PASS. Original WAV download hash remains. Handle missing/invalid/mismatched/cross-track receipts and write failures fail closed. Minimal implementation and scoped synthetic tests, standard library, Python -B, no application source/JAR rebuild. New manifest and reproducible source provenance; old reviewer receipt not applied to changed bytes. Future audio runtime still NOT_RUN and unapproved. Deliver concrete code, no checklist-only result.

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
Agent-facing: deliverables/agent/WI-20261023-ATS-001-evidence-pack.md.
User-facing: deliverables/user/WI-20261023-ATS-001-summary.md.
Implementation ownership as scoped above. Completion immediately triggers blocked WI; MA owns remote execution.


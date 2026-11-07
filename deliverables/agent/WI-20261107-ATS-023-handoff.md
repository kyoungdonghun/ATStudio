[WI HEADER]
WI ID: WI-20261107-ATS-023
REQ: REQ-20261107-ATS-006
Agent: pg
Depends On: WI022 final selection
Blocks: WI024/MA commit

[WI SUMMARY]
Independently scan pending files and selected/staged bytes for credentials, private endpoints, personal identifiers, raw execution payloads, certificates, encoded secrets and suspicious binaries. Reuse existing scanner when applicable; inspect its limitations. Emit paths/categories/line numbers only, never matched secret values. Inspect outgoing history range before push and repository workflows for server side effects. No network/server/account actions.

[ACCEPTANCE CRITERIA]
No confirmed secret in selected snapshot; classify false positives by evidence. Any fix sent to WI022 owner. Do not alter historical artifacts. Produce machine-readable findings in ignored build location and final redacted report. Recheck actual staged tree after MA stages.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md
Tier1: docs/policies/security-policy.md; AGENTS.md
Context: deliverables/user/REQ-20261107-ATS-006.md; scripts/reconstruction/scan_commit_candidates_wi004.py; WI022 selection

[OUTPUT CONTRACT]
build/WI-20261107-ATS-023/ scan reports; standard evidence pack/summary using skill. No Git index/commit/push mutation. Do not expose real email/IP/token matches in tool or chat.

[TRACEABILITY]
Distinguish scan coverage from guarantees; record exact reviewed tree/path hashes and exclusions.

[WI HEADER]
WI ID: WI-20261107-ATS-022
REQ: REQ-20261107-ATS-006
Agent: docops
Depends On: approved user request
Blocks: WI023/WI024 final candidate review

[WI SUMMARY]
Inventory pending work; select necessary source/docs and preserve excluded historical/binary/private files. Prepare explicit path manifest, minimal truthful current-state docs, no blanket add. Preserve evidence integrity; do not silently redact registered originals. If private historical evidence cannot be shared, create concise sanitized reconstruction notes and point to local custody, rather than publishing it. Inspect prior reconstruction/commit workflow for reuse.

[ACCEPTANCE CRITERIA]
Selection rationale and explicit UTF-8 paths; source/test dependency closure; safe docs with no private accounts/IPs/keys; ignored tools needed for reproduction considered. Do not copy large ignored build trees blindly. No server/Git index/commit/push actions. Author own files only; coordinate privacy reviewer findings before edits.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md
Tier1: docs/policies/security-policy.md; AGENTS.md
Context: docs/index.md; deliverables/user/REQ-20261107-ATS-006.md; prior REQ/WI20261106-006; scripts/reconstruction/scan_commit_candidates_wi004.py

[OUTPUT CONTRACT]
build/WI-20261107-ATS-022/selection.json (selected/excluded reasons, no sensitive content); standard evidence pack + summary. Reviewers write separate files. Use create-wi-evidence-pack skill. Provide actual existing test evidence identity and omitted artifacts. MA handles git mutations.

[TRACEABILITY]
Record counts/hash-based manifests and local checks; preserve unrelated files. Do not claim server changes or whole-suite rerun.

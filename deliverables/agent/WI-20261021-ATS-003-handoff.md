---
version: 1.0
last_updated: 2026-10-21
project: ATS
owner: MA
category: handoff
status: active
---
# WI-20261021-ATS-003 Handoff

[WI HEADER]
WI ID: WI-20261021-ATS-003
REQ: REQ-20261021-ATS-001
Agent: docops
Depends On: approved request; final WI001/002 outcomes
Blocks: final report

[WI SUMMARY]
Prepare a short privacy-safe local resume guide for evening Gmail signup testing and Git cleanup evidence. No app/runtime/code changes or new test framework. Do not repeat historical narratives or rewrite old registered evidence.

[ACCEPTANCE CRITERIA]
- State v7 actual OPEN/signup UI, original policy/BPF/ledger PASS, human flow deferred, overall cleanup not PASS, verified app/DB stopped/quarantine restored, own SSH key unloaded and console logged out; agent service stop deferred.
- Distinguish Gmail sender from Naver recipient; paid Naver cloud mail not integrated. No private email, IP, username or token-bearing URLs in new documents.
- Resume requires current local/remote artifact/config/IP checks and fresh bounded one-use operation; do not replay consumed Release/Start commands. Keep access for one work session, not repeated unnecessary unlocks. Human password/consent/signup, receipt, verification, login/logout and reset-email receipt only; never request actual reset-link use in this trial.
- Protect current reviewed JAR/SPA hashes; after commits, historical build HEAD remains unchanged and is mapped by content, not silently rewritten.
- Record selective local commits and deferred files honestly; no clean-worktree or test-success fabrication.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md; docs/standards/documentation-standards.md; docs/standards/development-standards.md; docs/standards/glossary.md.
Tier1: docs/policies/security-policy.md.
Context: deliverables/user/REQ-20261021-ATS-001.md; docs/index.md; deliverables/agent/WI-20261011-ATS-062-evidence-pack.md; WI001/002 new evidence.

[OUTPUT CONTRACT]
Only create docs/guides/evening-auth-trial-resume.md, deliverables/agent/WI-20261021-ATS-003-evidence-pack.md and deliverables/user/WI-20261021-ATS-003-summary.md. Update only new REQ status when evidence warrants. Do not change docs/index.md (existing operational identifiers keep it outside safe commit scope). Use apply_patch and create-wi-evidence-pack.

[TRACEABILITY REQUIREMENTS]
Relative source links, exact public artifact hashes, current date and bounded evidence owner. Link/format checks focused on owned docs; report outstanding runtime work separately.

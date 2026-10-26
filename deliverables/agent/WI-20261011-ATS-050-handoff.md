---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: guide
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-006.md
    reason: Approved web work and explicit diagnostic-only follow-up
  - path: WI-20261011-ATS-045-evidence-pack.md
    reason: Latest actual trial and recovery boundaries
---

# WI-20261011-ATS-050 Handoff

> Purpose: Diagnose POST_STATE_CHANGED from preserved local evidence without another app or database run.

[WI HEADER]
WI ID: WI-20261011-ATS-050
REQ: REQ-20261011-ATS-006
Agent: pg
Depends On: WI045 recovery and access closeout
Blocks: WI051 documentation integration; no native action

[WI SUMMARY]
User follow-up authorizes diagnosis only: "오케이 그럼 원래 웹 연결 시험에서 나온 ‘상태 변경 감지’ 원인부터 확인해줘. 앱 재실행이나 외부 공개는 아직 하지 말고, 추가 비용이나 내가 직접 입력해야 하는 건 먼저 알려줘."
Scope: Inspect pinned WI042 runtime/session code, local tests and redacted WI045 receipts. Identify the exact failing comparison site, possible branches, persistence gaps and the minimum evidence needed to resolve remaining uncertainty. Research primary upstream sources only where needed. Reports may be created; no implementation repair.
DoD: Explain proven facts versus hypotheses with exact source lines and reproduction limits. Examine policy comparison, BPF attachment identity across daemon-reload, and DB aggregate comparison/short-circuit behavior independently. Do not conflate WI046 After comparison or cleanup failures with the earlier primary failure.
Forbidden: SSH/agent/UI/cloud access, app/DB/service start or changes, SQL, private environment/log reads, secret use, external disclosure of project material, purchases/installations, source/candidate/freeze edits, commits/push, deleting records. No native command recipes are to be executed. Any need for target evidence is a proposal for MA and user, not execution authority.

[ACCEPTANCE CRITERIA]
- [ ] Pin actual runtime/session bytes to the consumed attempt.
- [ ] Determine whether first or post-view comparison failed from preserved result fields.
- [ ] Identify which unequal branches are logically possible, including distinct thrown error codes and short-circuit effects.
- [ ] Identify what snapshot data is actually persisted and whether live reads now could reconstruct past state.
- [ ] Distinguish deterministic code defects, upstream-supported possibilities and native-proven causes; no guessed certainty.
- [ ] Provide bounded next diagnostic/repair options without weakening security tests or authorizing another trial.

[INPUT POINTERS]
Tier0 retained from same agent context: docs/standards/core-principles.md, documentation-standards.md, development-standards.md, glossary.md.
Tier1 retained: docs/policies/security-policy.md; .claude/agents/pg.md.
Tier2: docs/index.md current entry; REQ006; WI045 evidence and native-receipt; build/WI042-restricted-web-check/runtime.py, web_session.py, existing local tests and manifest; WI048/049 preserved recovery evidence.
Skills: create-wi-evidence-pack; test only if running isolated pure/mocked diagnostic probes. No source edits or full application test runs.

[OUTPUT CONTRACT]
Only deliverables/agent/WI-20261011-ATS-050-evidence-pack.md and deliverables/user/WI-20261011-ATS-050-summary.md may be created, using apply_patch. Return early findings to MA promptly. Keep reports concise; document actual observations and no-native boundary. In-memory pure probes are allowed, never invoke run/main/session.view with real effects.

[TRACEABILITY REQUIREMENTS]
Runtime SHA256 ad3e8a20ddb17fd1e3e4878ac2096bfa74a63561b9b1e8572b6bc2a7cda7a7b6; session SHA256 3092ae8c5598d9b97e1dedc6e0bb74ca9c0d3a8989493465114229d45727257a. Current branch main/HEAD 58b1011dad796017e47dbcc82a4225ca449bcdf6; unrelated dirty files preserved. No live service state is inferred from old receipts. Original trial remains HOLD until evidence supports a narrower conclusion. Native exact cause may remain NOT_DETERMINED. WI051 integrates only this authorized follow-up and findings into current entry; no additional execution chain.

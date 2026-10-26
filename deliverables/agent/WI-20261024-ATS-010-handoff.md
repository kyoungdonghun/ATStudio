---
version: 1.0.0
last_updated: 2026-10-24
project: ATS
owner: MA
category: handoff
status: active
---
# WI-20261024-ATS-010
[WI HEADER]
WI ID: WI-20261024-ATS-010
REQ: REQ-20261011-ATS-007
Agent: docops
Depends On: WI-20261024-ATS-008, WI-20261024-ATS-009
Blocks: Final report

[WI SUMMARY]
Why: Current user explicitly approves server contact, temporary DB start/stop and read-only account/grant/backup-evidence check, followed by DB stop and access restoration.
Scope: Record current explicit user read-only scope and track SE008->PG009->MA boundedquery/DBstop/accessrestoration->Doc010. Own REQ007/index and own docs only. No privateinputs. Final results only after MA actual receipt; historicalbackup vscurrentfixturecoverage separate. Local/audio previousprep remains done, audio/app/nativewrites notauthorized.
DoD: Exact reviewed bounded tool, actual safe evidence and independent cleanup classification.
Constraints: MA alone accesses server. No app execution, login, business-data/schema mutation, new backup/restore, audio, payment, mail, deletion, commit/push. No credential lookup/input by agents. Preserve frozen tools/evidence. No auto full reruns. Human unlock only after technical readiness.

[ACCEPTANCE CRITERIA]
Functional: Return bounded nonsecret evidence sufficient for actual1/2/3 roles, grant and existing backup scope; no fabricated current backup acceptance.
Performance: Reuse 300-second independent DB lifecycle/900-second approval expiry, bounded SELECT/files.
Quality: Minimal local regression and exact PG review before MA use.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md.
Tier1: docs/policies/security-policy.md; docs/policies/quality-gates.md; .claude/agents/docops.md.
Tier2: docs/index.md current; deliverables/user/REQ-20261011-ATS-007.md; deliverables/agent/WI-20261024-ATS-003-evidence-pack.md; deliverables/agent/WI-20261024-ATS-007-evidence-pack.md; deliverables/agent/WI-20261011-ATS-062-evidence-pack.md backup addenda; build/WI20261023-008-readonly-recheck/README.md.
Prior bounded backup source tools are locally available; never print embedded payload/base64 or private addresses/records.
Latest user: 서버 접속·DB 임시 시작/종료·계정과 이용권 및 백업 근거의 읽기 전용 확인까지만 승인. 앱 실행·로그인·데이터 변경·새 백업 실행·음원 시험 금지. 조회 후 DB 종료·접근 원복 확인.

[OUTPUT CONTRACT]
deliverables/user/WI-20261024-ATS-010-summary.md
deliverables/agent/WI-20261024-ATS-010-evidence-pack.md
Use create-wi-evidence-pack; English technical docs/Korean REQ.
[TRACEABILITY REQUIREMENTS]
Record candidate/review hashes, changed files/tests, real observation owner/date, native cleanup and unverified limits separately. No old attempt overwrite.


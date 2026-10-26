---
version: 1.0.0
last_updated: 2026-10-24
project: ATS
owner: MA
category: handoff
status: active
---
# WI-20261024-ATS-015
[WI HEADER]
WI ID: WI-20261024-ATS-015
REQ: REQ-20261011-ATS-007
Agent: pg
Depends On: WI-20261024-ATS-014
Blocks: WI-20261024-ATS-016
[WI SUMMARY]
Scope: Review exact minimal read-only probe scope and bounded projections/drytests. No DBstart, remote staging, restic/cloud/API, businessdata, writes, privatevalues, subprocess except proven coldstate read commands. No passwordbody/hash output. Exact allowance restricted to newly userapproved inspection, not capture; previoustoolsreuse/hashes avoid broadrerun. Own build/WI20261024-015-preflight-review+EP/summary. Report defects directlySE.
DoD: Exact bounded read-only tool reused/reviewed; factual server/cost/access results later.
Constraints: User approves only read-only server/existingrepository/passwordstoragereference/additionalcostprecheck. No potentiallybillablequery beforecostverified. No backup/isolatedrestore/grantdate/app/login/audio, no deletion/purchase/configchange except MA separately approved temporaryaccessrestore. No SE/PG/DocOpsserver/provider/private access.
[ACCEPTANCE CRITERIA]
Functional: Failclosed exactreadonlyscope; old evidence preserved; freshactual observations separated from historical.
Performance: Minimalreuse and boundedcommands/files; no whole-suite rerun.
Quality: Independentexactreview before MA SSH/humanunlock.
[INPUT POINTERS]
Tier0: docs/standards/core-principles.md; docs/standards/documentation-standards.md; docs/standards/development-standards.md; docs/standards/glossary.md.
Tier1: docs/policies/security-policy.md; docs/policies/quality-gates.md; .claude/agents role.
Tier2: docs/index.md; deliverables/user/REQ-20261011-ATS-007.md; WI-20261024-ATS-013 final EP/summary; build/WI20261024-011-backup-preparation/README.md; build/WI20261024-008-readonly-evidence/make_payload.py; current final candidate158d2bb3064d112e625b7b8db914925636fe9b72f68161671da862d84dc19839.

[OUTPUT CONTRACT]
deliverables/user/WI-20261024-ATS-015-summary.md
deliverables/agent/WI-20261024-ATS-015-evidence-pack.md
Use create-wi-evidence-pack. Englishtechnical/KoreanREQ.
[TRACEABILITY REQUIREMENTS]
Hashes, scopedtests, actualowner/date/limits; chain immediate; no inferrednativecaptureauthority.


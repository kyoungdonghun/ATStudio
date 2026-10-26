---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: guide
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-006.md
    reason: Approved bounded web trial and renewed user authority
  - path: WI-20261011-ATS-043-evidence-pack.md
    reason: Exact reviewed candidate and execution prerequisites
---

# WI-20261011-ATS-045 Handoff

> Purpose: Record renewed authority and the actual once-only WI042 target trial without rewriting history.

[WI HEADER]
WI ID: WI-20261011-ATS-045
REQ: REQ-20261011-ATS-006
Agent: docops
Depends On: WI-20261011-ATS-044; MA actual receipts for final closeout
Blocks: none within this once-only trial

[WI SUMMARY]
Why: User now authorizes the reviewed repair's real-server preflight and conditional management-only web trial.
Scope: Record the new approval in REQ006 first; later integrate MA receipts into this WI's two reports and a new docs/index checkpoint. MA alone performs SSH/browser and reviewed native operations. No implementation changes.
DoD: Actual stages, timing, outcomes, identity, tests and cleanup are separately attributed; native failure or missing evidence remains HOLD; prior failure records remain byte-preserved.
Constraints: No agent remote/network/secret access or service commands. Do not edit frozen candidates/recipes, JAR, SPA, old evidence or other files. No Git mutation, purchases, packages, data writes, retries, public access or memory writes.

[APPROVAL SNAPSHOT]
Latest user: "오케이 그럼 수정본으로 실서버 사전검사하고, 통과하면 관리 회선에서만 웹 연결 시험 진행해줘. 추가 비용이나 내가 암호 입력해야 하는 건 먼저 알려주고, 외부 공개는 아직 하지 말고."
Separate security confirmation: "이번 시험에 한해 승인" for temporary Windows SSH agent/existing key, existing server DB/JWT/encryption secret internal use without output/change, max30min app/DB trial followed by stop/TLS restoration/key removal/agent disabled. No additional paid products. Exact approval time not supplied; do not invent it.
Preserved native WI039 failed before app/DB start. WI042 bundle 78ce821757111b6f4bd440a79680ed12e4e40b6592616293e4d768ddd6dbe00c reviewed by WI043 LOCAL_READY. New authority permits one conditional target trial only after fresh prerequisites, not a prior-native success claim or indefinite/repeated operation.

[ACCEPTANCE CRITERIA]
- [ ] Append Korean approval amendment to REQ006 and update current status without changing historical checkpoints.
- [ ] Await MA final receipts before reporting target completion; running/preflight-only is not success.
- [ ] Preserve finite read-only route contract, disabled external integrations/jobs and max30min scope.
- [ ] State actual app/DB/web activation and restoration plus SSH/recovery cleanup; report gaps precisely.
- [ ] Scoped documentation and whitespace checks pass; do not claim global audit.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md; docs/standards/documentation-standards.md; docs/standards/development-standards.md; docs/standards/glossary.md.
Tier1: docs/policies/security-policy.md.
Context: docs/index.md; deliverables/user/REQ-20261011-ATS-006.md; WI041, WI042, WI043, WI044 Evidence Packs.
Files: build/WI042-restricted-web-check/README.md, freeze.json, recipe-pins.json; build/WI043-restricted-web-review/final-receipt.json. Read-only pointers only.
Skills: .agents/skills/create-wi-evidence-pack/SKILL.md; .agents/skills/validate-docs/SKILL.md.

[OUTPUT CONTRACT]
Allowed writes only: deliverables/user/REQ-20261011-ATS-006.md; docs/index.md; deliverables/agent/WI-20261011-ATS-045-evidence-pack.md; deliverables/user/WI-20261011-ATS-045-summary.md; optionally deliverables/agent/WI-20261011-ATS-045-native-receipt.json with strictly redacted MA receipts.
First return an approval-recorded checkpoint; wait for MA's final evidence before finalizing. Current source/context already retained from WI044.

[TRACEABILITY REQUIREMENTS]
Apply create-wi-evidence-pack. Cite exact public pins and observed timestamps, no private IPs/tokens/log bodies. No guesses or assumed success. Rollback is withholding/reverting only this authorized documentation patch; MA operational cleanup is separately evidenced. No successor WI is implied after completion.

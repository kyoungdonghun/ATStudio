---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: guide
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-006.md
    reason: Approved HTTPS-only follow-up recovery
  - path: WI-20261011-ATS-046-evidence-pack.md
    reason: Preserved predecessor and reusable pinned helpers
---

# WI-20261011-ATS-048 Handoff

> Purpose: Prepare a minimal new HTTPS-only continuation after the consumed recovery stopped on unordered dependency serialization.

[WI HEADER]
WI ID: WI-20261011-ATS-048
REQ: REQ-20261011-ATS-006
Agent: se
Depends On: WI046 consumed HOLD and new user approval
Blocks: WI049 PG -> MA one-time original HTTPS continuation -> WI045 integration

[WI SUMMARY]
Why: WI046 durably preserved and detached BOTH overlays and reloaded systemd, then failed BEFORE Nginx start. MA read-only comparison found ONLY nginx.After serialization differs; all other Nginx/MySQL service keys match. At17:32:55 the sorted token lists matched exactly with seven tokens. Nginx remains inactive; app/DB remain masked/stopped.
Scope: New small continuation script/recipe/tests under build/WI048-quarantine-continuation, plus two reports. Reuse hash-verified pure/pinned WI046/WI042 helpers where useful. Do not copy/rebuild the entire trial framework. Correct ONLY unordered nginx.After comparison; preserve exact Exec command order and all other semantics. No new overlay moves or rerun of WI046.
DoD: Exact fresh target/boot, original TLS bytes, consumed WI046 receipt, durable copies and detached originals checked; original baseline comparison with After token-multiset equivalence only; nginx -t; at most one original nginx start; postcheck and bounded stop-on-failure including receipt durability. App/DB never start/unmask. New exclusive record and fixed safe stdout, preserve old evidence.
Constraints: Local only for SE. No SSH/UI/network, secrets/env reads, DB/data changes, installs, firewall/domain/cert changes, deletions/overwrites, source/JAR/SPA changes, commit/push. Use apply_patch. No broad helper refactor. Native MA only after PG.

[APPROVAL SNAPSHOT]
User: "검사 수정·재검토 후 HTTPS 복구만 계속" in response to correcting meaningless dependency-list output ordering, regression/independent review, then a NEW recovery that leaves already-detached temporary settings alone and starts only existing HTTPS. Keep previous failed records; no app/DB start, public opening or extra purchase. Existing temporary SSH may remain through this recovery and is removed afterward.

[ACTUAL EVIDENCE]
WI046 L1 STAGED exit0 (19c3b3). L2 consumed (0c3198), status HOLD/stage ORIGINAL_SERVICES/code BASELINE; checks STOPPED,NO_NGINX,TLS_BASELINE,CLAIM,PRESERVE_0,PRESERVE_1,DETACH_0,DETACH_1,DAEMON_RELOAD. nginx_start NOT_RUN, receipt_durability PASS, safety_stop NOT_NEEDED, trial_status HOLD. Original trial POST_STATE_CHANGED cause remains NOT_DETERMINED.
17:32:23 comparison: only nginx.After differs; expected/actual string length121; all other service keys equal.17:32:55 token multiset equality true/count7, nginx inactive, recovery receipt present. Do not expose baseline/config/address bodies.
WI046 candidate7ab8be7b2b1b94df4eeabbc8be5c03620d0ad219749ce571014d587a92ad1d94; freeze90b446e6aa4af89ac7aee184d7b0dc74b258a7d92427e3017d169bbb31bf418b. Original host/boot/transport unchanged. Both original /run *.conf files now absent, *.conf.retained-wi046 originals retained in same directory, durable private copies in /var/lib/atstudio/wi046-quarantine-recovery. Read/verify exact files only.

[ACCEPTANCE CRITERIA]
- [ ] Reordered identical After tokens pass; added/removed/duplicated tokens, changed other properties or reordered Exec commands fail.
- [ ] No old record/config/overlay modification or any move/delete operation; verify completed detach state and exact bytes/metadata.
- [ ] App/DB masked/PID0, no jobs/workers, original TLS byte equality before and after start; normal original Nginx process/config/cgroup/listeners after start.
- [ ] Exclusive new claim, one start, bounded stop on uncertainty including final receipt failure; no retry or secret reads/output.
- [ ] Focused regressions, exact immutable script/helper/recipe pins, honest native NOT_RUN, minimal two-set reports.

[INPUT POINTERS]
Tier0 retained: docs/standards/core-principles.md; documentation-standards.md; development-standards.md; glossary.md.
Tier1 retained: docs/policies/security-policy.md.
Tier2: approved REQ006/new approval above; WI046/WI047 EP and final-receipt; build/WI046-quarantine-recovery/recover.py, README, freeze, tests; immutable WI042 helpers. Skills: test; create-wi-evidence-pack. No React source work.

[OUTPUT CONTRACT]
Only build/WI048-quarantine-continuation/**, deliverables/agent/WI-20261011-ATS-048-evidence-pack.md, deliverables/user/WI-20261011-ATS-048-summary.md. Exact minimal MA recipe, fresh namespace, freeze and tests. DocOps owns REQ/index. Return frozen candidate promptly for immediate PG049 chain; no native execution.

[TRACEABILITY REQUIREMENTS]
Retain prior failure/approval evidence. Distinguish After-only false-positive now observed from undetermined original POST_STATE_CHANGED. Local tests are not recovery proof. No unlimited retry, normalization of ordered commands, dynamic private-value output or new security weakening.

---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: guide
status: active
dependencies:
  - path: WI-20261011-ATS-046-handoff.md
    reason: Approved HTTPS-only recovery tool and tests
  - path: ../user/REQ-20261011-ATS-006.md
    reason: User recovery approval and unchanged containment boundaries
---

# WI-20261011-ATS-047 Handoff

> Purpose: Independently review the exact narrow recovery before MA execution.

[WI HEADER]
WI ID: WI-20261011-ATS-047
REQ: REQ-20261011-ATS-006
Agent: pg
Depends On: WI046 exact freeze for final verdict
Blocks: MA once-only original HTTPS recovery -> WI045 final integration

[WI SUMMARY]
Why: WI042 app/DB stopped/masked successfully, but cross-filesystem os.rename left two trial overlays and Nginx stopped. User explicitly approved only recovery of previous TLS quarantine after tests/review, continuing current SSH temporarily until cleanup.
Scope: Independently review preservation/detachment, filesystem boundaries, exact byte/service validation and once-only original Nginx start in new WI046 tool. Prepare independent local regression cases while SE implements; final exact-byte verdict after freeze. No repair/weakening of old trial predicates.
DoD: Clear CONDITIONAL_ALLOW or HOLD tied to every executable/recipe hash, native prerequisites and cleanup limits; no app/MySQL start/DB/secret access. Report findings immediately.
Constraints: Local only, mocked effects; no browser/SSH/service/package/network access or actual operations. No edits to SE/frozen/history/app/config. No new model/runtime choice. No public exposure, purchase, delete, overwrite, git or memory mutation.

[ACCEPTANCE CRITERIA]
- [ ] Exact two overlay path/pin/type/owner/mode/link/ancestry checks; preserve durable bytes plus same-filesystem retained originals; safe collisions/partials, no overwrites/deletions.
- [ ] Distinct /run and /var handling tested explicitly; no cross-device rename reliance or silent shutil move/delete.
- [ ] App/MySQL masked and PID0, Nginx stopped/no workers before detachment/start; known original pins/service baseline and nginx -t before single start.
- [ ] Failures cannot start restricted config or app/DB, bypass drift or retry consumed claim. Original result.json/claims/evidence unchanged.
- [ ] Safe outputs only; no environment/DB secrets or raw configs/private logs/addresses. Existing root-owned helpers imported only when pinned.
- [ ] Independent tests for wrong source/pins, service changes, collision, partial stages, cross-device baseline and failed start; exact executable pins and final scope boundaries.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md; documentation-standards.md; development-standards.md; glossary.md.
Tier1: docs/policies/security-policy.md.
Context: WI046 handoff actual17:02 state and approval; WI043/044 evidence; REQ006 renewed amendments.
Files: build/WI046-quarantine-recovery/** once created/frozen; preserved build/WI042-restricted-web-check/web_session.py and runtime.py, original TLS pins, same-disk baseline order constraints. No implementation write access.
Skills: .agents/skills/create-wi-evidence-pack/SKILL.md; .agents/skills/test/SKILL.md.
Full Tier0/security/PG context retained from WI043; public current policy unchanged.

[OUTPUT CONTRACT]
Only build/WI047-quarantine-review/**, deliverables/agent/WI-20261011-ATS-047-evidence-pack.md, deliverables/user/WI-20261011-ATS-047-summary.md. Independent helper tests with native effects mocked, exact candidate recipe/pins. Do not wait to report a blocker, but withhold final verdict until SE046 freeze.

[TRACEABILITY REQUIREMENTS]
Preserve WI042 failure: HOLD/READ_ONLY_CHECKS/POST_STATE_CHANGED plus cleanupFAILED; app verified and17HTTP200 cases do not prove full web acceptance. L2 wrapper failed, L3/browserNOT_RUN. Recovery success must be separate from failed trial. Unknown post-state predicate remains unknown; no DB restart to diagnose it. MA handles evidence, SSH and actual recovery.

---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: handoff
status: approved
dependencies:
  - path: ../user/REQ-20261011-ATS-006.md
    reason: Approved current identity and read-only contract check
---
# WI-20261011-ATS-036 Handoff

[WI HEADER]
WI ID: WI-20261011-ATS-036
REQ: REQ-20261011-ATS-006
Agent: qa-integ
Depends On: approved REQ006
Blocks: WI037

[WI SUMMARY]
Why: Pin current SPA/JAR identity and exact nonmutating browser/API surface before management-only web trial.
Scope: Read current public code/manifests, check all artifact+source hashes and reviewed exclusions, derive actual frontend routes/API requests. Write evidence and small public allowlist/QA plan only.
DoD: Artifact identity PASS or specific drift; exact API/method/query and page test list for SE; no actual backend/browser/network startup.
Forbidden: Server/DB/secrets/private env, modifying app/build/config/source, installing/rebuilding, fixtures, deletion, commit/push, subdelegation.
Do not assume every GET is read-only; download/view-count etc may write. First approved pass uses anonymous empty production DB; skip login/stream/download success. Only home/catalog/search/plans and deny checks.

[ACCEPTANCE CRITERIA]
- [ ] Current branch/HEAD/dirty files and641 backend inputs + JAR matched or drift.
- [ ] Frontend143 output files and336 reviewed source inputs/exclusions matched or drift. Do not import private env.
- [ ] Inspect actual home/track/album/plans/router/layout requests and service effects; provide minimal exact allowlist (path/method/parameters), reject unnecessary detail/write/PII endpoints.
- [ ] Browser plan verifies real origin HTTPS, SPA deep link/reload, empty-state/query, real six plan data if expected, blocked paths/methods and existing off-login state; no fictitious successful media claim.
- [ ] Static source/API contract attribution, no current server or UI acceptance claimed.
- [ ] Send early concise allowlist/identity result to MA so implementation is not blocked on full report.
[INPUT POINTERS]
Tier0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier1:
- docs/policies/security-policy.md
Context:
- deliverables/user/REQ-20261011-ATS-006.md
- deliverables/agent/WI-20261011-ATS-020-artifact.json
- deliverables/agent/WI-20261010-ATS-026-artifact.json
- deliverables/agent/WI-20261011-ATS-020-evidence-pack.md
- frontend/src/router/index.tsx
- frontend/src/pages/public/
- frontend/src/api/
- src/main/java/com/atstudio/atstudio/controller/
- src/main/java/com/atstudio/atstudio/service/
- build/WI-20261010-ATS-026-frontend-dist/
[OUTPUT CONTRACT]
Write only build/WI036-web-contract/**, deliverables/agent/WI-20261011-ATS-036-evidence-pack.md, deliverables/user/WI-20261011-ATS-036-summary.md.
Use apply_patch and create-wi-evidence-pack skill; short early result and final exact pins.
[TRACEABILITY REQUIREMENTS]
Current local hash verification is not native/browser proof. Preserve source, old manifest and all unrelated dirty files.

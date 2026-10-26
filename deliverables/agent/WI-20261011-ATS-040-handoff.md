# WI-20261011-ATS-040 Handoff Packet

[WI HEADER]
WI ID: WI-20261011-ATS-040
REQ: REQ-20261011-ATS-006 (approved repair amendment)
Agent: pg
Depends On: WI039 frozen candidate
Blocks: MA new once-only trial, then WI041
Session ID: current

[WI SUMMARY]
Why: Independently verify the repaired checker does not broaden access or convert invalid service states into valid ones.
Scope (in): Read new exact frozen bytes, narrow semantic diff, old preservation, target-free independent tests, operational recipe review, explicit ALLOW/HOLD.
Scope (out): No production/remote/browser/network/secret/system-service actions, candidate edits, new purchase, git writes.
DoD: Exact byte-bound verdict and reproducible tests; no inherited ALLOW from WI037.
Constraints: Same REQ006 boundaries; no general missing-property tolerance, no shortcuts around masked-stage versus actual running-stage checks.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] New namespace and consumed WI035 preservation confirmed.
- [ ] Exact target output fixtures accepted only in the appropriate stage; required scalars, malformed/duplicate/unknown output and unsafe states rejected.
- [ ] Review loaded/unmasked MySQL gates and all post-unmask cleanup failure paths independently.
- [ ] All network, timing, privilege, route, secret and cleanup controls remain effective and pinned.
- [ ] Fresh source/artifact checks, SSH/recovery/firewall preconditions and honest browser limits remain in the recipe.
- [ ] Existing P2 transient cleanup reporting caveat is preserved without hiding final failures.
Performance:
- [ ] App/DB bound and cleanup fit approved <=30min total.
Quality:
- [ ] Independently rerun relevant Python/PowerShell tests and add adversarial fixtures in own review directory.
- [ ] Exact reviewed set/runtimebundle/recipe pins and explicit native-execution conditions in verdict.
- [ ] Native trial remains NOT_RUN until MA actually runs it.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
- docs/policies/access-control-policy.md
Tier 2:
- docs/index.md (Current Decision Entry)
- deliverables/user/REQ-20261011-ATS-006.md
- deliverables/agent/WI-20261011-ATS-035-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-036-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-037-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-038-evidence-pack.md
- build/WI035-restricted-web-check/
- build/WI036-web-contract/
- build/WI037-restricted-web-review/

Role: .claude/agents/pg.md
New inputs:
- deliverables/agent/WI-20261011-ATS-039-evidence-pack.md
- build/WI039-restricted-web-check/

[OUTPUT CONTRACT]
Review-only helpers -> build/WI040-restricted-web-review/
Agent-facing -> deliverables/agent/WI-20261011-ATS-040-evidence-pack.md
User-facing -> deliverables/user/WI-20261011-ATS-040-summary.md
Use test and create-wi-evidence-pack skills. Do not edit SE or historical artifacts.

[TRACEABILITY REQUIREMENTS]
Exact hashes, findings with lines, attack/negative cases, test commands/results and bounded operational conditions. On completion MA checks chain and executes only if fresh guards and verdict allow.


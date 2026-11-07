[WI HEADER]
WI ID: WI-20261107-ATS-024
REQ: REQ-20261107-ATS-006
Agent: cr/qa/eo
Depends On: WI022 selection and WI023 privacy review
Blocks: MA commit/push

[WI SUMMARY]
Independently review source/test changes and chosen commit coherence. Reuse current test/build evidence with identity checks; run proportional local checks when needed. Inspect hooks and CI/push effects for server-unchanged condition. No server access. Final staged snapshot review after peers finish. Do not expand functionality.

[ACCEPTANCE CRITERIA]
Necessary source dependencies included; validation accurately reported; no privileged broadening beyond approved completed implementation; docs/current-state consistent. Existing unrelated changes preserved. Final EO sharing decision based on PG report and exact staged tree.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md
Tier1: docs/policies/security-policy.md; docs/policies/quality-gates.md; AGENTS.md
Context: deliverables/user/REQ-20261107-ATS-006.md; WI019/020/021 evidence; WI022/023 results

[OUTPUT CONTRACT]
Independent review under build/WI-20261107-ATS-024 plus standard evidence/summary. Use relevant test/lint skills. No staging/commit/push; MA owns Git mutations. Do not repeat whole suite without justification.

[TRACEABILITY]
Record source hashes/current test evidence and exact reviewed staged tree. Unresolved findings block commit, not routine review checkpoints.

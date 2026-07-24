[WI HEADER]
WI ID: WI-20260724-ATS-003
REQ: REQ-20260724-ATS-001
Agent: se
Depends On: -
Blocks: WI-20260724-ATS-006

[WI SUMMARY]
Why: Make client testing PDF provenance accurate and portable across machines.
Scope (in/out): Edit PDF generator/verifier and generated PDF/manifest only when required by deterministic verification. Do not change client guide content except generated provenance.
DoD: No user-specific tool path is hardcoded; generated manifest records truthful portable command/tool data; verifier passes.
Constraints/Forbidden: Preserve deterministic PDF content contract and Korean rendering. Do not add machine-specific paths elsewhere.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Generator source contains no `C:/Users/jm991` path.
- [ ] Manifest records executable/tool provenance based on actual arguments/runtime or a portable command identity.
- [ ] PDF verification passes and deterministic hashes are intentionally updated if needed.
Quality:
- [ ] Generator and verifier pass.
- [ ] `git diff --check` passes for owned files.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/standards/documentation-standards.md
REQ/Context:
- deliverables/user/REQ-20260724-ATS-001.md
- docs/design/remaining-remediation-design-20260716.md
Files:
- scripts/docs/generate_client_testing_pdf.py
- scripts/docs/verify_client_testing_pdf.py
- output/pdf/atstudio-client-testing-guide.pdf
- output/pdf/atstudio-client-testing-guide.manifest.json

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260724-ATS-003-summary.md
Agent-facing -> deliverables/agent/WI-20260724-ATS-003-evidence-pack.md

[TRACEABILITY REQUIREMENTS]
Record source hashes, commands, generated artifact changes, verifier result, and rollback.

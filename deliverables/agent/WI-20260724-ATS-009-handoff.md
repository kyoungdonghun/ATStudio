[WI HEADER]
WI ID: WI-20260724-ATS-009
REQ: REQ-20260724-ATS-001
Agent: se
Depends On: WI-20260724-ATS-007
Blocks: WI-20260724-ATS-008

[WI SUMMARY]
Why: Close the three bounded P2 findings from the final V1 audit before independent review and remote publication.
Scope (in/out): Correct PDF replay provenance, remove six retired availability rate-limit aliases, and align the local DB example with the canonical datasource password variable. Update only directly affected tests and documentation. Product behavior, payment policy, DB schema, external secrets, and unrelated formatting are out.
DoD:
- PDF provenance no longer derives a misleading runtime command from `Path.stem`; the committed documentation provides a portable dependency/bootstrap and replay contract, and the exact documented replay path is verified in a fresh process.
- `application.yml` accepts only the canonical availability `*_CLIENT_LIMIT` and `*_CLIENT_WINDOW_SECONDS` names; baseline tests assert the six aliases are absent.
- `application-local.example.yml` references `SPRING_DATASOURCE_PASSWORD`, and its contract test expects that canonical name.
- Generated PDF and manifest are regenerated and verified when provenance changes.
Constraints/Forbidden:
- Do not record a personal absolute Python/Poppler path in tracked artifacts.
- Do not read or modify ignored secret files or external acceptance bundles.
- Do not alter rate-limit values, product behavior, API contracts, or DB schema.
- Do not hide an unreproducible command behind a generic executable name. Prefer an explicit portable runtime/dependency contract.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] The PDF replay recipe succeeds from its documented dependency/runtime setup and the verifier passes.
- [ ] All six retired availability aliases have zero active runtime references.
- [ ] Local bootstrap uses the canonical datasource password variable.
Performance:
- [ ] No runtime hot-path or bundle impact.
Quality:
- [ ] Focused Java baseline contract passes.
- [ ] PDF generation and verification pass.
- [ ] Documentation validation and `git diff --check` pass.
- [ ] Repository scans distinguish current code from historical WI evidence.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2 (Task Context):
- docs/client/index.md

REQ/Context Docs:
- deliverables/user/REQ-20260724-ATS-001.md
- deliverables/user/WI-20260724-ATS-007-summary.md
- deliverables/agent/WI-20260724-ATS-007-evidence-pack.md

Files:
- scripts/docs/generate_client_testing_pdf.py
- scripts/docs/verify_client_testing_pdf.py
- output/pdf/atstudio-client-testing-guide.pdf
- output/pdf/atstudio-client-testing-guide.manifest.json
- src/main/resources/application.yml
- application-local.example.yml
- src/test/java/com/atstudio/atstudio/config/V1BackendBaselineContractTest.java

Repro/Logs:
- Use the configured workspace Python runtime for initial inspection, but leave a repository-portable bootstrap/replay contract.
- Run the exact replay recipe that the final documentation records.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260724-ATS-009-summary.md :
- Corrections, behavior boundary, verification, and remaining gates.
Agent-facing -> deliverables/agent/WI-20260724-ATS-009-evidence-pack.md :
- Exact patches, scans, commands/results, artifact hashes, rollback, and WI-008 readiness.
Handoff Packet -> deliverables/agent/WI-20260724-ATS-009-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Include exact focused commands and results
Rollback: Document how to revert generated artifacts and configuration/test changes

[WI HEADER]
WI ID: WI-20260724-ATS-018
REQ: REQ-20260724-ATS-002
Agent: se
Depends On: WI-20260724-ATS-011, WI-20260724-ATS-012
Blocks: WI-20260724-ATS-020

[WI SUMMARY]
Why: A Windows fresh clone with `core.autocrlf=true` fails the frontend Prettier gate and changes the client-PDF provenance manifest even though the committed content is logically unchanged.
Scope (in/out): Define an explicit repository line-ending policy, make client-PDF source hashing use the documented normalized-text contract, and add deterministic font preflight evidence. Add focused automated tests for the new contracts. Do not change product behavior, client document contents, generated PDF layout, or unrelated formatting.
DoD: A fresh Windows checkout preserves frontend formatting, client-PDF replay produces a clean manifest/PDF result, missing or unexpected font inputs fail with an actionable message, and focused tests cover the contracts.
Constraints/Forbidden: Work only on `.gitattributes`, the Python cache rules in `.gitignore`, `scripts/docs/`, and directly related tests. Do not bulk-renormalize existing tracked files in this WI. Do not modify generated client content merely to make hashes pass. Do not access secrets, databases, or external Providers.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Repository text files use a deterministic LF checkout policy, with explicit exceptions for Windows batch files and binary assets.
- [ ] Every text-source hash recorded and verified by the client-PDF manifest uses UTF-8 text normalized to LF.
- [ ] Replay preflight validates the required Malgun Gothic font inputs without embedding or copying the font.
- [ ] Focused tests demonstrate CRLF/LF-equivalent manifest source hashes.
Performance:
- [ ] No material increase in PDF replay time.
Quality:
- [ ] Focused Python tests pass.
- [ ] Existing PDF replay and verification pass without changing the generated PDF SHA.
- [ ] `git diff --check` passes.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1 (Policies - Inferred):
- docs/policies/quality-gates.md
Tier 2 (Task Context):
- docs/standards/evidence-pack-standard.md
- docs/client/index.md
REQ/Context Docs:
- deliverables/user/REQ-20260724-ATS-002.md
- deliverables/agent/WI-20260724-ATS-011-evidence-pack.md
- deliverables/agent/WI-20260724-ATS-012-evidence-pack.md
Files:
- .gitattributes
- scripts/docs/generate_client_testing_pdf.py
- scripts/docs/verify_client_testing_pdf.py
- scripts/docs/replay-client-testing-pdf.ps1
- scripts/docs/

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260724-ATS-018-summary.md :
- Summary, corrected contracts, verification results, residual risks
Agent-facing -> deliverables/agent/WI-20260724-ATS-018-evidence-pack.md :
- Exact files/lines, commands, hashes, tests, rollback
Handoff Packet -> deliverables/agent/WI-20260724-ATS-018-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Record focused tests, PDF replay/verify, Prettier, and `git diff --check`
Rollback: Document how to revert the policy and hashing changes together

[WI HEADER]
WI ID: WI-20260714-ATS-010
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260714-ATS-003, WI-20260714-ATS-012
Blocks: WI-20260714-ATS-019, WI-20260714-ATS-021, WI-20260714-ATS-022, WI-20260714-ATS-024

[WI SUMMARY]
Why: Ensure administrators never open extension-trusted enterprise certification content from the public upload tree.
Scope: Approved PDF/JPEG/PNG signature/format validation, image canonicalization, private quarantine root, generated key, safe ADMIN attachment download, aggregate size bound, response DTO redaction, public-route exclusion, and tests.
Out: HWP/HWPX/DOC/DOCX support, malware-clean verdict, inline preview, live legacy migration/backfill, and document-body inspection outside tests.
DoD: New documents are verified and stored only in private root; client MIME/path never defines trust; download is authenticated attachment-only with no-store/nosniff/sandbox controls.
Constraints: User approved PDF/JPEG/PNG-only baseline. Use WI-012 coordinator. Existing legacy rows/files are not auto-migrated/deleted; fail closed or mark operator follow-up without exposing public paths.

[ACCEPTANCE CRITERIA]
- [ ] PDF begins `%PDF-`, ends `%%EOF` after whitespace, and has no leading/trailing payload; images use canonical pipeline.
- [ ] Extension/MIME mismatch, polyglot, path-like filename, per-file/count/aggregate overflow fail before DB mutation.
- [ ] Private root cannot equal/nest under public root and is not mapped by WebConfig.
- [ ] ADMIN download enforces parent/child ownership and safe attachment headers; API response omits storage path.
- [ ] Focused service/controller security tests, backend tests, compile, and diff check pass.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/development-standards.md
Tier 1: docs/policies/security-policy.md; docs/policies/access-control-policy.md
Context: deliverables/user/REQ-20260714-ATS-001.md; docs/design/p1-security-acceptance-hardening-design.md; WI-012/WI-009 evidence
Files: CompanyCertificationService/Controller/DTO/entity; WebConfig; storage coordinator/private root; focused tests

[OUTPUT CONTRACT]
User summary: deliverables/user/WI-20260714-ATS-010-summary.md (Korean)
Evidence Pack: deliverables/agent/WI-20260714-ATS-010-evidence-pack.md
Implementation ownership: certification validation/quarantine/download/API redaction and focused tests.

[TRACEABILITY REQUIREMENTS]
Record legacy compatibility gap and separate backfill approval; no automatic data change.

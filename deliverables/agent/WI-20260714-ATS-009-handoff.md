[WI HEADER]
WI ID: WI-20260714-ATS-009
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260714-ATS-003, WI-20260714-ATS-012
Blocks: WI-20260714-ATS-019, WI-20260714-ATS-022, WI-20260714-ATS-024

[WI SUMMARY]
Why: Prevent subscriber playlist thumbnails from publishing submitted active/non-image bytes on the SPA origin.
Scope: JPEG/PNG signature and bounded decode validation, JDK ImageIO canonical JPEG re-encode, opaque generated key, safe public response headers, playlist integration, and tests.
Out: New image library, GIF/WebP/SVG support, general media redesign, and legacy file migration.
DoD: Only server-generated bounded JPEG bytes are persisted/served for new playlist thumbnails; malformed/active/polyglot inputs fail before public storage.
Constraints: Use WI-012 storage coordinator, no direct storage bypass. Preserve playlist UX/API except validation errors. No upscaling; no submitted filename/metadata in stored key.

[ACCEPTANCE CRITERIA]
- [ ] Valid JPEG/PNG becomes a fresh RGB JPEG with size/dimension/pixel bounds.
- [ ] SVG/HTML/GIF/WebP, MIME mismatch, truncation, animation/APNG, excessive dimensions, and trailing active payload cases fail or canonicalize safely as designed.
- [ ] Public thumbnail response is fixed `image/jpeg` with `nosniff` and restrictive content policy.
- [ ] Focused playlist security tests, backend tests, compile, and diff check pass.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/development-standards.md
Tier 1: docs/policies/security-policy.md
Context: deliverables/user/REQ-20260714-ATS-001.md; docs/design/p1-security-acceptance-hardening-design.md; WI-012 evidence
Files: PlaylistService/Controller; WebConfig/static resource boundary; storage coordinator; ValidationConstants; focused tests

[OUTPUT CONTRACT]
User summary: deliverables/user/WI-20260714-ATS-009-summary.md (Korean)
Evidence Pack: deliverables/agent/WI-20260714-ATS-009-evidence-pack.md
Implementation ownership: canonical image service, playlist thumbnail integration, safe serving boundary, focused tests.

[TRACEABILITY REQUIREMENTS]
Include byte/signature test evidence and rollback; no claim about historical files.

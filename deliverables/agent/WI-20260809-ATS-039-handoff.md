[WI HEADER]
WI ID: WI-20260809-ATS-039
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-031
Blocks: WI-20260809-ATS-048, WI-20260809-ATS-050, WI-20260809-ATS-055, WI-20260809-ATS-066, WI-20260809-ATS-071

[WI SUMMARY]
Why: `CR-031-066` proves that ADMIN-uploaded Album thumbnails and Notice attachments can retain submitted active-content bytes under the publicly mapped storage root. Album thumbnails lack authoritative canonicalization; Notice files can bypass the download controller through `/uploads/**`.
Scope (in/out):
- In: Reuse `CanonicalImageService.canonicalizeThumbnail` before every Album thumbnail store/replace so only generated JPEG bytes and `.jpg` keys enter the public root.
- In: Apply the existing fixed safe-thumbnail response headers to Album thumbnail static paths while preserving the Playlist contract.
- In: Store new Notice attachments under `StorageRoot.PRIVATE`, load/delete them from PRIVATE, and deliver only through the public Notice attachment controller as forced octet-stream attachment with `no-store, private`, `nosniff`, sandboxed CSP, and same-origin resource policy.
- In: Preserve Notice attachment public availability through the controlled API endpoint and preserve current file-type/count/size behavior; exact attachment limits/types remain held by `CR-031-104` / WI-066.
- In: Focused security tests for HTML/SVG/polyglot/MIME mismatch/oversize Album input via canonicalization boundary, public static bypass containment, safe Notice download headers, and storage-root ownership.
- In: Synchronize directly affected security, storage, Album, Notice, and API current-state docs.
- Out: New scanner/parser dependency, Notice attachment type/count/size policy, existing-file migration, schema/data changes, production file moves/deletes, UI redesign, and unrelated upload flows.
DoD:
- Album create/update never pass submitted thumbnail bytes directly to public storage.
- Album and Playlist canonical public thumbnails receive fixed JPEG/nosniff/sandbox/same-origin headers.
- New Notice attachments are never written to the public root and static `/uploads/**` cannot resolve PRIVATE objects.
- Notice API download remains public but always forces an attachment with the full safe header set.
- Failed validation/storage leaves no Album/Notice DB mutation or orphaned file under the tested transaction paths.
- Focused and adjacent backend tests, full backend tests/coverage/build, docs validation, and diff check pass.
- Independent PG review confirms containment without inventing a new attachment policy.
Constraints/Forbidden:
- Do not inspect protected output artifacts or ignored secrets.
- Do not access or mutate a live/local DB or production filesystem; tests use mocks/temp files only.
- Do not move or delete retained Notice files. V1 fresh-baseline operation means no migration is performed in this WI.
- Do not add a dependency, scanner, parser, schema column, or attachment allowlist/limit.
- Do not change Notice visibility or require login for public Notice attachment downloads.
- Do not broaden beyond Album thumbnails and Notice attachments except the shared safe-header predicate needed to retain Playlist behavior.

[ACCEPTANCE CRITERIA]
Functional:
- [x] Album create/update canonicalizes JPEG/PNG input before public storage and stores only generated JPEG content/key.
- [x] Album canonicalization rejection causes no storage write or Album mutation.
- [x] Album and Playlist thumbnail paths receive fixed JPEG, `nosniff`, sandbox CSP, and same-origin resource headers; unrelated uploads do not.
- [x] Notice create/update stores attachments only under PRIVATE and delete/download use the same PRIVATE root.
- [x] Notice API download returns forced octet-stream attachment plus `no-store, private`, `nosniff`, sandbox CSP, and same-origin resource policy.
- [x] Direct public static paths cannot resolve Notice PRIVATE attachments by design and focused resource-boundary proof.
- [x] Existing Notice public read/download contract and current accepted file behavior remain unchanged.
Performance:
- [x] Album canonicalization reuses current bounded image dimensions/bytes and performs one decode/re-encode per supplied thumbnail.
- [x] Notice download streams/returns one Resource without copying the full file into application memory.
Quality:
- [x] Focused Album/Notice/canonical-image/header/security tests pass.
- [x] Adjacent Playlist/Track/Company Certification storage tests pass.
- [x] Full backend tests, JaCoCo thresholds, and assemble pass.
- [x] Documentation validation and `git diff --check` pass.
- [x] Independent PG review passes.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2 (Current contracts):
- docs/design/p1-security-acceptance-hardening-design.md:55
- docs/design/api-spec.md
- docs/design/usecase/sound-album.md
- docs/design/usecase/user-notice.md

REQ/Context Docs:
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-025-evidence-pack.md:111
- deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:630
- deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:968

Files:
- src/main/java/com/atstudio/atstudio/service/AlbumService.java
- src/main/java/com/atstudio/atstudio/service/NoticeService.java
- src/main/java/com/atstudio/atstudio/controller/NoticeController.java
- src/main/java/com/atstudio/atstudio/config/PublicThumbnailHeaderFilter.java
- src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java
- src/main/java/com/atstudio/atstudio/service/storage/StorageMutationCoordinator.java
- src/main/java/com/atstudio/atstudio/service/storage/StorageRoot.java
- src/test/java/com/atstudio/atstudio/service/AlbumServiceTest.java
- src/test/java/com/atstudio/atstudio/service/NoticeServiceTest.java
- src/test/java/com/atstudio/atstudio/controller/NoticeControllerTest.java
- src/test/java/com/atstudio/atstudio/config/PublicThumbnailHeaderFilterTest.java
- src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java

Repro/Logs:
- Use `MockMultipartFile`, mocked storage/coordinator, MockMvc, and temp storage only.
- No actual retained file or DB operation is permitted.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-039-summary.md:
- Korean summary, security boundary, unchanged policy, validation, risk, and follow-up.
Agent-facing -> deliverables/agent/WI-20260809-ATS-039-evidence-pack.md:
- Evidence pointers, red/green proof, tests, static-bypass argument, PG review status, rollback, and next chain.
Handoff Packet -> deliverables/agent/WI-20260809-ATS-039-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Record focused, adjacent, and full backend commands with counts/results.
Rollback: Revert canonicalization/private-root/header changes and tests/docs as one WI patch. No data rollback is executed; retained-file migration is explicitly outside scope.

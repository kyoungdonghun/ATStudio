---
version: 1.0
last_updated: 2026-10-26
project: ATS
owner: PG
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261026-ATS-004-handoff.md
    reason: Approved candidate review scope
  - path: ../user/REQ-20261026-ATS-001.md
    reason: User-approved local preservation and commits
---
# Evidence Pack: WI-20261026-ATS-004

## Summary
Classified 696 exact visible candidate files: 633 commit candidates and 63 retained exclusions. Preserved original active documents before two minimal path generalizations. No Git index, commit, server, private input or provider action.

## Scope / DoD
- [x] Every file in the fixed visible candidate snapshot has a disposition and raw SHA-256.
- [x] Pattern findings emit category/line only, never matched values.
- [x] Flagged benign source/default/test/version values separately reviewed.
- [x] Four client PDFs and one unfilled operator DOCX checked through local text/metadata extraction and prior exact-hash comparison.
- [x] Original frozen reports and all excluded files are preserved; no blanket historical redaction.
- [x] Active document source bytes preserved before narrow personal-path generalization.
- [x] Exact candidate manifest delivered; later new files require delta review and final staged-byte gate.

## Injected References
Tier 0: core-principles (evidence-led scoped work), development-standards (minimal reproducible changes), documentation-standards (metadata/pointers), glossary (canonical terms). Tier 1: AGENTS stage-two single approval, security-policy, quality-gates, versioning-policy. Tier 2: approved REQ, latest Current Decision Entry, prior WI20261021-001/002 classification, WI20261025-009/011/012/013 client artifact evidence and WI20261026-006 source checks. Rule source: .claude/config/context-injection-rules.json, role PG. Handoff existed and was read before applying create-wi-evidence-pack.

## Evidence
- [Exact classification](WI-20261026-ATS-004-classification.json): 696 paths, 633 approved candidates, 63 excluded.
- [Reusable bounded scanner](../../scripts/reconstruction/scan_commit_candidates_wi004.py): read-only candidate bodies; no automatic staging; never writes over an existing receipt.
- Initial/expanded/final scans: ignored build/WI-20261026-ATS-004/. Scans were refined for version/hash false positives and IP punctuation; earlier results remain distinct.
- [Active-document preservation](WI-20261026-ATS-004-active-doc-preservation.json): raw before/after hashes and retained ignored originals.
- Client artifact receipt: build/WI-20261026-ATS-004/client-artifact-scan.json. PDF SHA values match WI011/012/013 and WI009 outputs; DOCX matches WI009 outputs. Extracted 21 PDF pages with no images/active features and all DOCX XML parts with no media/macros; no flagged text or metadata. Existing visual reviews reused, not repeated. Word real rendering remains unverified.

## Classification Criteria
Visible text was scanned for private-key bodies, JWT/provider key literals, sensitive assignments, non-example addresses, email/phone/identity-number patterns and machine-specific paths. All matches were examined in value-masked context. Java/library versions, SHA digit substrings, fixed key-header checks, public noreply defaults and synthetic rejection fixtures are not disclosures. The exact per-file reasons accompany allowances.

The 48 text exclusions contain machine/host-specific historical references or unresolved sensitive operational context; they are not declared unsafe solely because a pattern matched. They remain outside this commit allowance. Fifteen binary/generated exclusions are old guides/screenshots or JARs not fully reviewed in this scope. Exclusion means preserve unstaged, never delete. No API keys/passwords/private account files were requested.

## Minimal Active-Document Changes
MA additionally delegated two path-only edits. docs/payment/index.md now uses a repository-root label and local-application-data reference for historical machine locations. scripts/deployment/README.md resolves installed Python through Get-Command rather than a personal absolute runtime path. Their byte-identical originals remain in ignored build/WI-20261026-ATS-004/private-originals/. Dates, claims, original release hashes and commands otherwise remain unchanged. No frozen historical EP was rewritten.

## Validation and Limits
Final delta review covers WI005's 94 archive/report files and four WI004 reports. Five fixture/error-mapping warnings and the two-rule archive .gitattributes are approved at their exact bytes. The retained archive separates 73 exact source copies from 13 redacted reference-only files; target bindings are removed from references, and references are not reconstruction inputs. Existing source originals were not edited. A supplemental long credential-label scan found synthetic test values, validator field-name symbols, preservation-policy prose and status constants, not actual credential bodies. Final combined exact allowances are in WI-20261026-ATS-004-allowlist.json; that manifest itself requires the final independent self-file check.

Commands: git status --porcelain=v1 -z --untracked-files=all; bounded scanner; locally bundled pypdf; zip/XML structural DOCX inspection; exact SHA checks. No network, Git writes, executable operational tooling, app test or runtime action. Candidate clear is a bounded scan result, not a universal secret-free assertion or complete semantic re-review of all historic outcomes. No scan of unrelated committed history or ignored private trees.

Raw SHA values must still match at staging. Git line-ending transformations require explicit index comparison. New reports, archive files or integration edits need their own delta allowance. Referenced ignored/private evidence does not become available in a fresh clone merely because a Markdown pointer is committed.

## Risks / Rollback / Follow-up
Do not force-add excluded originals or restore previous server state. The two active edits can be compared to their preserved originals; no deletion/reset is needed. WI007 must independently verify selected groups and final staged bytes, then MA may perform the authorized local commits. No push is authorized.

# Evidence Pack: WI-20260715-ATS-021

## Summary (one-liner)

- Aligned the authorized current Track documentation with public full-track listening, protected official download, and explicit historical supersession of bounded-preview policy.

## Scope / DoD Check

- [x] API and SOUND-010 describe complete active-Track listening and full-resource Range behavior.
- [x] Public listening is separated from original-key/static-path access and official download/License entitlement.
- [x] SOUND-011 and the API contract preserve active-subscription and plan-quota checks for first download and the existing License re-download rule.
- [x] The P0 remediation design and closure report label bounded-preview decisions as a historical 2026-07-13 snapshot superseded by REQ-20260715-ATS-001.
- [x] Canonical terminology uses Public Listening and Official Download instead of truncated-preview language.
- [x] The Upload glossary definition contains no preview-generation implementation/debt note.
- [x] No client PDF or product code was scanned, regenerated, or edited.
- [x] Repository-wide active wording is clean: remaining bounded-preview or preview-generation matches are explicitly historical/superseded or state that no current workflow depends on them.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution and approved execution authority |
| 0 | `docs/standards/documentation-standards.md` | Documentation structure and language rules |
| 0 | `docs/standards/development-standards.md` | Tier 0 safety and traceability requirements |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/security-policy.md` | Existing protected-media boundary and claim-scan baseline |
| 1 | `docs/policies/quality-gates.md` | Validation and evidence requirements |
| 2 | `deliverables/user/REQ-20260715-ATS-001.md` | Approved full-track listening policy |
| 2 | `deliverables/agent/WI-20260715-ATS-018-evidence-pack.md` | Backend full-resource stream and download-regression evidence |
| 2 | `deliverables/agent/WI-20260715-ATS-019-evidence-pack.md` | Frontend playback-state evidence |
| 2 | `docs/design/api-spec.md` | Current Track API contract |
| 2 | `docs/design/usecase/sound-track.md` | Current Track use cases |
| 2 | `docs/design/db-schema.md` | Current Track storage-column semantics and historical schema rows |
| 2 | `docs/design/usecase/index.md` | Current Track use-case index and historical change rows |
| 2 | `docs/design/p0-release-blocker-remediation-design.md` | Historical bounded-preview design |
| 2 | `docs/audit/p0-release-blocker-closure-20260713.md` | Historical P0 closure evidence |
| 2 | `docs/audit/full-system-audit-20260713.md` | Original audit context |

**Injection rules applied:** Handoff `deliverables/agent/WI-20260715-ATS-021-handoff.md`; assignee `docops`; task type `documentation`; read order Tier 0 -> Tier 1 -> Tier 2. Client PDFs remained explicitly excluded and were not scanned or regenerated.

## Evidence Pointers

- `docs/design/api-spec.md:9` - current addendum supersedes earlier dedicated-preview, bounded-prefix, ratio, and generator wording for listening behavior.
- `docs/design/api-spec.md:412` - public stream serves the complete active resource with full-length Range semantics and no download/License side effect.
- `docs/design/usecase/sound-track.md:114` - SOUND-010 records full-track controller playback, successful-play state, error handling, and static-path denial.
- `docs/design/usecase/sound-track.md:146` - SOUND-011 distinguishes first-download subscription/quota checks from licensed re-download.
- `docs/design/p0-release-blocker-remediation-design.md:23` - bounded-preview design is explicitly historical and superseded only for listening behavior.
- `docs/design/p0-release-blocker-remediation-design.md:57` - historical subsection and test criteria are prohibited as current implementation policy.
- `docs/audit/p0-release-blocker-closure-20260713.md:29` - closure report preserves historical evidence while pointing to REQ-20260715-ATS-001 and WI-018.
- `docs/audit/p0-release-blocker-closure-20260713.md:52` - protected-media bullets are labeled as the verified 2026-07-13 snapshot.
- `docs/standards/glossary.md:77` - canonical Public Listening and Official Download terms define the entitlement boundary; the adjacent Upload term contains no preview-generation debt statement.
- `docs/policies/security-policy.md:152` - protected-media policy requires complete controller-mediated Public Listening while preserving DTO/static denial and protected Official Download.
- `docs/design/db-schema.md:9` - v15 current contract is separated from the historical v14 and v3 preview decisions; no schema structure changed.
- `docs/design/db-schema.md:360` - current column notes treat `preview_file` as a legacy nullable compatibility column, not a listening dependency or product debt.
- `docs/design/usecase/index.md:10` - current Track media contract is explicit; historical v3 rows are marked superseded by v14.

## Commands & Outputs

- `python .agents/skills/validate-docs/scripts/validate_docs.py`
  - Exit `0`; all Tier 0 documents exist, no broken links, 382 supported traceability IDs matched, and all documents are indexed.
- `rg -n -i -g '*.md' ... docs deliverables`
  - Historical/immutable matches remain only in explicitly labeled dated design/closure sections, older REQ/WI deliverables, audit snapshots, and superseded change-history rows.
  - Current active Track policy/schema/use-case sections contain no bounded-prefix, ratio-limit, or required preview-generator prescription.
  - The removed Glossary upload note produced no remaining match.
- `git diff --check -- docs/policies/security-policy.md docs/design/db-schema.md docs/design/usecase/index.md docs/design/api-spec.md docs/design/usecase/sound-track.md docs/design/p0-release-blocker-remediation-design.md docs/audit/p0-release-blocker-closure-20260713.md docs/standards/glossary.md deliverables/user/WI-20260715-ATS-021-summary.md deliverables/agent/WI-20260715-ATS-021-evidence-pack.md`
  - Exit `0`; no whitespace errors. Git emitted only LF-to-CRLF working-copy notices.

## Risks / Rollback

- Risk: public full-track listening can be retained from network responses; this is the approved product policy and remains distinct from official download records and License entitlement.
- Rollback: reverse only WI-021 documentation hunks in the eight authorized documents and remove this WI's summary/evidence. Preserve historical bodies, product code, client PDFs, and concurrent worktree changes.

## Follow-ups

- WI-20260715-ATS-022 is unblocked from the WI-021 documentation-current-state perspective and may perform independent integration verification.

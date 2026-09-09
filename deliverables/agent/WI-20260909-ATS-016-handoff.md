# WI-20260909-ATS-016

[WI HEADER]
REQ: REQ-20260909-ATS-003 (approved)
Agent: docops
Depends On: WI-20260909-ATS-015
Blocks: WI-20260909-ATS-017

[WI SUMMARY]
Why: Remove local-only artifact dependencies without deleting necessary current evidence or changing product behavior.
Scope: Determine disposition for the approved 205-path manifest; update affected document references, current/history entry points, narrow output ignore rules, and a compact archive register. MA owns ZIP/restore/hash/exact deletions, Git and isolated verification.
DoD: Every original path classified; needed safe document dependencies retained selectively; archival original pointers resolve to an honest local-archive register rather than missing paths; raw logs/screenshots not uploaded. No stale runtime/production claims.
Forbidden: No source/test/config/media/DB/runtime changes, archive execution/deletion, Git staging/commit, external operations, new audit scope, or bulk promotion of all 205 artifacts.

[ACCEPTANCE CRITERIA]
- Inspect Markdown links and YAML dependencies from tracked docs to original untracked paths; retain only necessary safe document closure, or replace historical evidence links with precise archive register references.
- Preserve original historical claims/timestamps. If a linked result is not in Git, say local archive, not independently downloadable remote evidence.
- Confidential contents must not be emitted. Scan candidate retained docs for secrets/PII before proposing publication; no raw log promotion.
- Write disposition JSON using structured serialization under the external archive directory for MA, not a new workspace output directory.
- Add a concise public archive registry with archive ID, relative paths/dispositions/hashes where safe, total counts and recovery procedure. The private manifest/ZIP stays external.
- Current entry points distinguish operational docs from historical deliverables. Add narrow generated-output ignore patterns only after explicit dispositions, not blanket deliverables ignore.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/archive-policy.md
- docs/policies/security-policy.md
Tier 2:
- .agents/skills/create-wi-evidence-pack/SKILL.md
- .agents/skills/validate-docs/SKILL.md
- .agents/skills/validate-docs/scripts/validate_docs.py
Context:
- deliverables/user/REQ-20260909-ATS-003.md
- deliverables/agent/WI-20260909-ATS-015-evidence-pack.md
- docs/index.md
- docs/registry/index.md
- .gitignore
- C:/Users/jm991/AppData/Local/ATStudio/archives/v1-artifacts-20260909-084405/manifest.json

[OUTPUT CONTRACT]
- docs/registry/v1-artifact-retention-20260909.md (new compact public register, indexed)
- docs/index.md, docs/registry/index.md, docs/policies/archive-policy.md, .gitignore as needed
- Only tracked historical documents whose links/dependencies point to manifest entries; repair pointers minimally, not broad rewriting.
- Retained selected manifest documents: leave content unchanged unless approved privacy-safe necessary correction is reported first.
- External disposition.json with all 205 paths, RETAIN_GIT or ARCHIVE_LOCAL, reason; include affected tracked document list.
- deliverables/agent/WI-20260909-ATS-016-evidence-pack.md
- deliverables/user/WI-20260909-ATS-016-summary.md
- Notify MA with classification promptly; do not wait for heavy checks, which MA owns. Read-only source and reference scans only.

[TRACEABILITY REQUIREMENTS]
Follow create-wi-evidence-pack. Exact changed paths, reference repair rationale, archive ID, tests actually performed. Local archive is not remote backup. MA supplies validation/deletion/publication receipts later. WI016 completion immediately unblocks WI017.

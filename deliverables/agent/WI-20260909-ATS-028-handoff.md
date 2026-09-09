---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: MA
category: reference
status: draft
dependencies:
  - path: ../user/REQ-20260909-ATS-006.md
    reason: Approved actual multi-upload verification
  - path: WI-20260909-ATS-027-evidence-pack.md
    reason: Completed fixture and probe preparation
---

# WI-20260909-ATS-028: Actual Upload Evidence Cross-check

[WI HEADER]
REQ: REQ-20260909-ATS-006
Agent: qa-integ
Depends On: WI-20260909-ATS-027 (complete)
Blocks: none

[WI SUMMARY]
Independently verify original/derivative and retained-data evidence while MA performs actual CUA browser scenarios. Distinguish supplied browser observations from independently read filesystem/DB evidence. Close documents only for performed checks.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md.
Tier 1: docs/policies/security-policy.md.
Tier 2: docs/design/runtime-storage-operations.md; REQ006; WI027 evidence; scripts/validation/wav-browser/README.md; scripts/database/manual-wi023-audio-processing.sql.

[SCOPE / OWNERSHIP]
Own WI028 summary/evidence and REQ006 closure. Append a clearly dated actual deployment verification addendum to docs/design/runtime-storage-operations.md only after results are supplied. Do not erase prior scoped evidence. Read-only query of exact localhost:3306/atstudio is approved, secrets never printed; no writes, browser control, process changes, new DB or media mutation. MA owns UI and runtime tests. All private evidence lives in LOCALAPPDATA/ATStudio/validation/wav-browser-20260909; do not modify private-backup/.

[ACCEPTANCE CRITERIA]
- Cross-check actual DB state, matching original SHA256, 128000bps MP3 and full decoded duration for 20 synthetic uploads from MA-provided records.
- Independently confirm baseline 78 files remain identical and original 13 Track metadata/paths unchanged (except no new expected fields in pre-migration backup).
- Review SQL additivity and actual validate startup; retained ALTER column ordering may differ from fresh schema.sql. Do not fill the global disposable manifest using a mismatched retained hash or claim fresh install verified.
- Report exact coverage for UI batch, invalid size/input, retry, playback/seek/download and restart. Mark incomplete until MA supplies results. No generic all-green production approval.
- Record newly observed unrelated UI/log issues separately, without scope-expanding fixes.

[OUTPUT CONTRACT]
Use create-wi-evidence-pack. Create deliverables/user/WI-20260909-ATS-028-summary.md and matching evidence. Run validate-docs and git diff --check on owned paths. Provide findings with evidence, exact changed paths and remaining limits. Do not commit/push. Return promptly for missing MA observations without pretending completion; continue after supplied evidence.

[ROLLBACK]
Keep new test artifacts and additive schema. No destructive rollback, old-binary redeployment, existing media cleanup or credential changes.

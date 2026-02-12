---
version: 1.1
last_updated: 2026-01-29
project: system
owner: DocOps
category: standard
audience: both
keywords: ["evidence-pack", "traceability", "repro", "reproduction", "WI"]
dependencies:
  - path: core-principles.md
    reason: Transparency/traceability principles (evidence pointers, explainability)
  - path: documentation-standards.md
    reason: Document metadata/structure standards
  - path: glossary.md
    reason: Term (WI/REQ etc.) consistency
  - path: ../registry/workboard.md
    reason: Summary index where Evidence Pack pointers connect
status: stable
---

# Evidence Pack Standard — `deliverables/agent/<WI-ID>-evidence-pack.md`

> Purpose: Standardize Evidence Pack location/required fields/pointer specifications to ensure WI results are not just "completed in words" but **reproducible/verifiable evidence packs**.

---

## 1) Location/Naming (Fixed)

- **Standard path (fixed)**: `deliverables/agent/<WI-ID>-evidence-pack.md`
  - Example: `deliverables/agent/WI-20260127-001-evidence-pack.md`
- **Auxiliary artifacts (optional)**: Place in same directory as `<WI-ID>-<slug>.<ext>`.
  - Example: `deliverables/agent/WI-20260127-010-test-output.txt`

> Principle: To create new rules with folder branching (by project/by date), **propose/approve via separate WI** before changing standard (prevent drift).

---

## 2) Metadata (Required)

YAML frontmatter at top of Evidence Pack includes the following.

```yaml
---
version: 1.0
last_updated: YYYY-MM-DD
project: system | PRJ-...
owner: MA | SubagentName | Role
category: evidence-pack
status: draft | stable
related_wi: WI-YYYYMMDD-###
---
```

---

## 3) Required Sections (Minimum Checklist)

Evidence Pack body includes the following items at minimum.

- [ ] **Change Summary**: What/why changed (1-5 lines)
- [ ] **Pointers (Evidence/Tracking)**:
  - List of changed files (paths)
  - Key points (line ranges or function/section names if possible)
  - Consulted document/template/policy paths (if needed)
- [ ] **Reproduction/Verification Command**
  - Execution command (when possible)
  - **If impossible, write `N/A` + reason + alternative verification**.
- [ ] **Results**
  - Pass/fail summary
  - If failed: cause/response/follow-up WI (if any)
- [ ] **Risk/Rollback (if applicable)**

Recommended (good to have):

- [ ] **Scope/Out-of-scope** (In/Out)
- [ ] **Decision points** (decision needed/assumptions)
- [ ] **Follow-up WI proposals**

---

## 4) WI/Workboard Connection Rules (Prevent drift)

### 4.1 Must place Evidence Pack pointer in WI document

- Specify standard path in "Evidence Pack" section of WI document.
- If Evidence Pack file not yet created, place `TBD` and replace with path upon completion.

### 4.2 Workboard's Evidence Pack column is "link hub"

- Write Evidence Pack path in WI table of `docs/registry/workboard.md`.
- Since Workboard is summary index, send to Evidence Pack for detailed evidence/reproduction.

---

## 5) Minimum Template (Copy-paste ready)

```markdown
---
version: 1.0
last_updated: YYYY-MM-DD
project: system
owner: MA
category: evidence-pack
status: draft
related_wi: WI-YYYYMMDD-###
---

# Evidence Pack — WI-YYYYMMDD-###

## Change Summary

- ...

## Pointers (Evidence/Tracking)

- Changed files:
  - `path/to/file`
- Key points:
  - `path/to/file:line-range` (or function/section name)
- Related documents:
  - `docs/...`

## Reproduction/Verification

```bash
# ...
```

## Results

- ...

## Risk/Rollback (if applicable)

- ...
```

---

## Related Documents

### Reference Documents

- [Agent-facing Documentation Guide](../guides/agent-facing-docs.md): Overall structure including Evidence Pack
- [Workboard (Work Dashboard)](../registry/workboard.md): Location where Evidence Pack pointers connect

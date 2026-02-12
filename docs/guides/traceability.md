---
version: 1.1
last_updated: 2026-01-06
project: system
owner: EO
category: guide
status: stable
related_work_item: WI-20251230-001
related_decision: ADR-20251230-001-reuse-first-registry-traceability
dependencies:
  - path: ../standards/glossary.md
    reason: Standard terminology usage criteria
  - path: operation-process.md
    reason: Operation process reference
---

# Traceability Guide (WI ↔ ADR ↔ Asset)

> Goal: Make "what changes were there / why did it that way / where is impact / what was reused" traceable with **1~2 searches**.

## 0) Additional Principle: Provenance (Who Made It?) Recorded as Metadata

As documents/logs become voluminous, recording "traces" redundantly everywhere actually increases confusion.
Therefore follow below principles:

- **Essential Trace (Minimal Meta)**: Each deliverable (WI/ADR/Request/Approval/Execution) has at minimum "who made it" as metadata.
- **Minimize Duplication**: Maintain body focused on summary + links (chain). (Detailed logs handled by system logs)
- **Single Chain**: Sufficient to maintain only `Request → WI → (ADR) → Approval → Execution → Verification` links.

> **Work Management Process**: Refer to [operation-process.md](operation-process.md) for WI breakdown, parallel processing, trigger rules

## 1) Minimum Unit of Traceability: Work Item (WI)

All meaningful changes have **WI**.

- File location (recommended):
  - **System repo work**: `docs/work-items/WI-YYYYMMDD-###.md`
  - **Individual project work**: Under `docs/` of that project repo (match project standard)
- WI includes at minimum:
  - Goal/scope
  - Reuse candidates (Asset ID)
  - Impact (Consumers/Contracts/tests/rollback)
  - (MEDIUM/HIGH) Connected ADR links
  - (Recommended) Related WI links (similar/dependent/follow-up) + tags/keywords
  - (Recommended) **Related Request**: `REQ-...` (if started based on request)
  - (Recommended) **Evidence Pack (trace pointer bundle)**: Change justification/command/test/log pointers

Recommended rules:

- Use **Key (standard term key)** from `docs/standards/glossary.md` for tags/keywords (prevent synonym proliferation).

Evidence Pack recommended location (operational standard):
- `deliverables/agent/<WI-ID>-evidence-pack.md` (Agent-facing)
- `deliverables/user/<WI-ID>-summary.md` (User-facing)

## 2) Decision Justification: ADR

MEDIUM/HIGH tasks must leave "decisions".

- File location: `docs/adr/ADR-YYYYMMDD-###-<slug>.md`
- ADR must write **Related Work Item**.
- (Recommended) **Provenance**: created_by / created_at / source_ref

## 3) Reuse Index: Asset Registry

Assets must not just "exist", but **be found/know application scope** to be reused.

Minimum fields to include in registry:

- **Asset ID**
- **Scope**: System (Global) / Project (Local)
- **Owner (A)**
- **Location**
- **Consumers**
- **Status**

## 4) Actual Tracking Examples

### A. "Why did this change enter?"

1. Find ID like `WI-20251230-001` in commit/WI/conversation
2. Open WI document like `docs/work-items/WI-20251230-002-template-examples.md` to see goal/impact/reuse decision
3. If MEDIUM/HIGH, open connected ADR to verify "alternatives/risks/rollback"

### B. "Where is this asset (reuse code/policy) used?"

1. Find `AST-...` Asset ID
2. See Consumers in `docs/registry/asset-registry.md`
3. Search for `AST-...` or name with ripgrep if needed to find actual reference locations

### C. "Want to find similar work/reuse hints"

1. See current WI's `Tags/Keywords` and find WI with same tags
2. Follow connected WI through current WI's `Related Work Items`
3. Check Asset/ADR referenced by related WI to expand "reuse candidates"

## 5) Operational Tips (Discipline That Improves When Enforced)

- Blocking "merge without WI link" during PR/review drastically reduces omissions
- Proceed with Asset promotion when satisfying "checklist + Consumers update"
- Goal of WI↔WI connections is to create connectivity traceable with **minimal links (1~5)**

---

## Related Documents

### Required References

- [Operation Process](operation-process.md): Reuse-first and change management process
- [Registry](../registry/asset-registry.md): Asset and Capability list

### Reference Documents

- [Development Workflow](development-workflow.md): Overall process flow overview
- [System Design](../architecture/system-design.md): Overall design and operational structure
- [Quality Gates](../policies/quality-gates.md): Operation checklist by criticality

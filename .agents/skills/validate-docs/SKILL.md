---
name: validate-docs
description: This skill should be used when validating documentation consistency and integrity. It checks internal links, Tier 0 document references, and traceability ID validity.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob, Bash
model: sonnet
---

# Validate Docs

## Purpose

Validate documentation consistency and integrity by checking links, required documents, and traceability IDs.

## When to Use

- After modifying documentation files
- Before commits or pull requests that include doc changes
- When checking for broken internal links
- When verifying traceability ID compliance
- When ensuring Tier 0 documents are properly referenced

## How to Use

### Quick Start

Run the bundled validation script:

```bash
python3 .agents/skills/validate-docs/scripts/validate_docs.py
```

### What Gets Checked

1. **Internal Links**
   - Validate file path references in markdown files
   - Check relative and absolute path accuracy
   - Detect broken links to non-existent files

2. **Tier 0 Document References**
   - Verify existence of required core documents:
     - `docs/standards/core-principles.md`
     - `docs/standards/documentation-standards.md`
     - `docs/standards/development-standards.md`
     - `docs/standards/glossary.md`

3. **Traceability IDs**
   - Validate ID format compliance:
     - `REQ-YYYYMMDD-<PRJ>-###` (Requirements, e.g., `REQ-20260211-ATS-001`)
     - `WI-YYYYMMDD-<PRJ>-###` (Work Items, e.g., `WI-20260211-ATS-001`)
     - `STD-###` (Standards)
   - For REQ/WI, repeated references across summary/handoff/evidence documents are expected; validation focuses on supported ID format matching

4. **Document Index**
   - Verify `docs/index.md` and category index link validity
   - Detect orphaned documents that are not covered by a root or category index
   - Exclude snapshot/archive-only locations such as `.claude/worktrees/` and future `docs/archive/`

### Handling Results

**Error Types:**
- **Error**: Broken links, missing Tier 0 docs
- **Warning**: Orphaned documents, non-standard formatting

**Example Output:**

```
[VALIDATION RESULTS]

✓ Tier 0 Documents: All required files exist

✗ Internal Links: 2 broken links found
  - docs/templates/impact-analysis-template.md:34 → docs/missing.md (file not found)
  - README.md:23 → docs/old-path.md (file not found)

✓ Traceability IDs: Supported formats matched

⚠ Document Index: 1 orphan document
  - docs/experimental/draft.md (not listed in docs/index.md)

[SUMMARY]
Status: FAILED (2 errors, 1 warning)
Action Required: Fix broken links
```

## Exit Codes

- `0`: All validations passed
- `1`: Errors found (broken links, missing docs)
- `2`: Warnings only (orphaned documents)

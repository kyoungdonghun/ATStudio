---
version: 1.0
last_updated: 2026-01-06
project: system
owner: EO
category: policy
status: stable
dependencies:
  - path: ../registry/asset-registry.md
    reason: Template asset registration reference
  - path: ../standards/glossary.md
    reason: Standard terminology usage baseline
---
# Template Governance

## Goal

Prevent templates from multiplying and causing confusion, and operate toward **reusing/improving existing templates**.

## Principles

- Templates are the **interface of operational discipline**.
- Rather than creating new templates, default to **extending/improving existing templates (versioning)**.

## Rules

- **New template creation is the exception**.
  - Creation conditions (examples):
    - Existing templates cannot express the goal (fields are completely different)
    - Fields/procedures repeated across multiple WIs have solidified into a new category
- **Changes prioritize backward compatibility**:
  - Field additions are OK
  - Field deletions/semantic changes require ADR or explicit guidance
- **Registry registration required**: Register as Asset in Templates section of `docs/registry/asset-registry.md`

## Practical Tips

- Templates aim for "minimum 1 page, maximum 1~2 minutes to fill". If templates become heavy, nobody uses them.



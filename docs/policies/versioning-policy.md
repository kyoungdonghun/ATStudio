---
version: 1.0
last_updated: 2026-01-06
project: system
owner: EO
category: policy
status: stable
dependencies:
  - path: quality-gates.md
    reason: Quality gates reference
  - path: ../standards/glossary.md
    reason: Standard terminology usage baseline
---
# Versioning/Release/Deprecation Operating Rules

> Purpose: As reusable assets (policies/templates/tools/code) grow, "when/who/how" versions increase and get deprecated must be clear to avoid operational breakage.

## 1) Target Scope

- **Document/policy assets**: `docs/*.md`
- **Templates**: `docs/templates/*`
- **Scripts/tools**: `.claude/scripts/*` (and root wrapper: `setup_workspace.py`)
- (Future) **Packages/libraries/Platform Tools**
- **External dependencies**: npm, python packages and other external libraries

## 2) Status Definitions

- **Draft**: Experimental/frequently changing. Consumers use with limitations.
- **Stable**: Interface/behavior stabilized. Backward compatibility principle applies.
- **Deprecated**: Replacement path provided. Deprecation schedule/migration plan required.
- **Debt**: Technical debt identified. Future refactoring target.

## 3) Responsibilities (Roles)

- **EO (A)**: Final responsibility for global asset versioning/deprecation policy
- **MA (R)**: Identify versioning/deprecation needs in change work and record in WI/ADR
- **SA/RE/PG (C)**: Consult from contract/regression/security perspectives
- **MA (R)**: (For code/repo work) Physical execution of tags/releases/commits

## 4) Versioning Rules (Documents/Templates/Policies)

### 4.1 Basic Principle

- Default is to **fix the name** and **evolve the content** (prevent template proliferation).
- If "breaking change" is necessary:
  - (1) First convert to **Deprecated**
  - (2) Provide replacement path
  - (3) Remove after schedule

### 4.2 Version Notation (Recommended)

Add the following to the top or bottom of documents/templates (optional but increasingly recommended as it approaches Stable).

- **Version**: v0.1 / v1.0 …
- **Last Updated**: YYYY-MM-DD

## 5) Breaking Change Criteria

Consider **Breaking** if any of the following:

- Field deletion/semantic change (template/schema)
- Decision gate/quality gate criteria strengthened so existing work cannot pass
- Automation script output format change (affects downstream pipelines)

## 6) Deprecated Procedure (Required)

When converting to Deprecated, must record the following.

- **Replacement target**: What to migrate to (file/path/new policy)
- **Migration method**: 3~5 lines is sufficient
- **Deprecation schedule**: "Maintain until when, remove when"
- **Consumer impact**: Affected targets (update registry Consumers)

## 7) Example (Template Change)

Example: Need to change field semantics in a template.

1) Add "Deprecated" notice to existing template
2) Guide new approach via **field addition** in same template (backward compatible)
3) After sufficient period (e.g., 30 days), remove old usage approach

## 8) External Dependencies and Security Maintenance

For system stability, periodically manage external libraries and security vulnerabilities.

### 8.1 Dependency Checks and Patches
- **Check cycle**: Minimum quarterly or before major releases.
- **Patch principles**:
  - **Security Patch**: Apply immediately (treat as Criticality HIGH).
  - **Minor/Patch Update**: Apply in batch during regular checks, then perform regression testing (`Golden Set`).
  - **Major Update**: Issue WI, analyze impact, then gradually transition (follow Deprecated procedure).

### 8.2 Vulnerability Management
- Vulnerabilities identified through external tools (GitHub Dependabot, etc.) or manual checks are immediately registered as **WI (Incident)**.
- Resolve according to HIGH/MEDIUM procedures in `quality-gates.md` based on vulnerability severity.



---
version: 1.0
last_updated: 2026-01-29
project: system
owner: SA
category: template
status: stable
related_work_item: WI-20251230-001
dependencies:
  - path: ../guides/operation-process.md
    reason: Operation process reference
  - path: ../guides/traceability.md
    reason: Traceability system reference
---
# ADR-20251230-001: Reuse-first + Registry + ADR for Operational Traceability

- **Date**: 2025-12-30
- **Status**: Accepted
- **Related Work Item**: WI-20251230-001

## Context

As agents/projects grow, while adding features becomes easier, the following problems accumulate during operations:

- Failure to reuse sources leads to duplication/drift
- Impact analysis (consumer/contract/regression) is not identified during changes
- Loss of rationale for "why it was done this way" leads to rapidly increasing refactoring/extension costs

## Decision

Adopt the following three components as the minimum system for operational capability:

1. **Reuse-first process**: Force search/reuse/promotion review before creating new
2. **Registry (directory)**: Find Capability/Asset in one place for fast reuse decisions
3. **ADR (decision record)**: Record decision/alternatives/risks/rollback for MEDIUM/HIGH changes to ensure traceability

## Options Considered

- **Option A: Operate "rules in head" without documentation**
  - Pros: Initial speed
  - Cons: Unoperational (knowledge evaporation/duplication explosion/onboarding impossible)
- **Option B: Start with full automation in code**
  - Pros: Ideal
  - Cons: Excessive initial investment, MVP delay
- **Option C: Fix discipline first with minimal docs/templates/registry (chosen)**
  - Pros: Secure operational foundation at low cost, can expand to automation later
  - Cons: Requires "writing/updating" discipline initially

## Consequences

- **Positive**:
  - Faster reuse candidate search and reduced duplication
  - Standardized change impact/rollback/validation
  - Decision rationale preserved, reducing long-term maintenance/extension costs
- **Negative / Risks**:
  - System can collapse if document/registry updates are missed
- **Mitigations**:
  - Prevent "update omissions" with PR templates/checklists (can be automated later)

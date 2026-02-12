---
version: 1.0
last_updated: 2026-01-29
project: system
owner: EO
category: template
status: stable
dependencies:
  - path: ../registry/asset-registry.md
    reason: Asset registry reference
  - path: ../work-items/examples/asset-promotion-example.md
    reason: Example reference
  - path: ../standards/glossary.md
    reason: Standard term usage criteria
---
# Asset Promotion Checklist

> Purpose: Promote to common assets considering **full impact (Consumers/contracts/tests/operations)**.
>
> **Example:** See `docs/work-items/examples/asset-promotion-example.md`

## Metadata

- **Work Item ID**: WI-YYYYMMDD-###
- **Asset Candidate**: (name/location)
- **Current Layer**: Local | Module | Package | Platform Tool
- **Target Layer**: Package | Platform Tool | Global Asset
- **Owner(A)**:

## 1) Reuse Evidence

- [ ] Same pattern appears 2+ times (duplication elimination value)
- [ ] Repeatedly called/referenced from multiple Capabilities
- [ ] Common need from operations perspective (observability/policy/security)

## 2) Contract & Boundary

- [ ] **Public Interface** is clear (input/output/options/errors)
- [ ] Responsibility boundary is clear (includes what this asset "does not do")
- [ ] Failure modes/retry/Idempotency defined (required for tools)

## 3) Compatibility

- [ ] Backward compatibility principle defined
- [ ] Version policy (major bump) or migration plan exists for breaking changes

## 4) Validation

- [ ] Minimum regression tests/scenarios exist
- [ ] Observability points (log/metric) exist

## 5) Registry Update

- [ ] Register Asset in `docs/registry/asset-registry.md`
- [ ] Update Consumers list
- [ ] Specify Status (Draft/Stable/Deprecated)

## 6) Approval (Governance)

- [ ] EO approval (required)
- [ ] If HIGH impact, record PG/RE review

---
version: 1.0
last_updated: 2026-07-24
project: ATS
owner: se
category: wi-summary
status: confirmed
related_wi: WI-20260724-ATS-023
---

# WI-20260724-ATS-023 Summary

## Verdict

**PASS**

`GET /api/company-certifications/me` now checks the current member's BUSINESS
eligibility before looking up any certification record. A non-BUSINESS member
receives the existing `RESOURCE_NOT_ACCESS` (`403 Forbidden`) response, so an
account changed from BUSINESS to INDIVIDUAL cannot read historical
certification data.

## Changes

- Added the existing `UserType.BUSINESS` guard to
  `CompanyCertificationService.getMyStatus`.
- Added direct service tests for INDIVIDUAL and ADMIN rejection before any
  certification repository interaction.
- Added an independent security regression for the changed-account historical
  record scenario.
- Added a controller regression proving the existing forbidden HTTP contract.
- Preserved BUSINESS record (`200`) and BUSINESS no-record (`404`) behavior.

No schema, frontend, route, response DTO, or API shape changed.

## Verification

- TDD RED: 69 focused tests, 3 expected failures before the service fix.
- Focused regression: 69 tests passed, 0 failed.
- Related certification/payment/storage slice: 118 tests passed, 0 failed.
- Java main and test compilation: passed.
- Documentation validation: passed with 0 broken links and 471 valid traceability references.
- `git diff --check`: passed.

Commit and push status can be verified from Git history.

## Evidence

See
`deliverables/agent/WI-20260724-ATS-023-evidence-pack.md`.

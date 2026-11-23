# Proxy contracts and independent acceptance

WI-20261123-ATS-015 · qa-integ · REQ-20261123-ATS-003 (approved)

## Scope and acceptance

Own deploy/ and related scripts/tests, plus a new Spring integration acceptance test if appropriate. Coordinate exact contracts with 013 and 014. Run two isolated local users through first visit, like/unlike, playlist create/modify, re-login, isolation, publication removal, and regressions using local test facilities. Verify the actual application and edge contracts and report any gaps. No production work.

## Input pointers

Tier 0: docs/standards/core-principles.md, docs/standards/development-standards.md, docs/standards/documentation-standards.md, docs/standards/glossary.md. Tier 1: docs/policies/security-policy.md. Tier 2: docs/ui/screen-flow.md, docs/design/api-spec.md, deliverables/user/WI-20261123-ATS-003-summary.md and WI-20261123-ATS-004-summary.md. Use the current source as implementation baseline.

## Output contract

Record changed paths, reproducible tests, outcomes, limitations and rollback in the matching evidence pack and user summary. Final integrated verification depends on all three workstreams. Keep unrelated working changes intact.

# WI Remediation Handoff: WI-20260809-ATS-051

[WI HEADER]

- WI ID: `WI-20260809-ATS-051-REMEDIATION`
- REQ: `REQ-20260809-ATS-001` (`approved`)
- Agent: `se`
- Depends On: `WI-20260809-ATS-051-QA-INTEG-REVIEW`
- Blocks: WI-051 conclusive review and finalization

[REMEDIATION SCOPE]

Resolve every P2 and bounded P3 item in `deliverables/agent/WI-20260809-ATS-051-qa-integ-review-result.md`:

1. Fence post-review company-certification detail refresh with the same selected ID, modal ownership, and generation retirement as normal detail loads. Prove late success/failure after close or a newer target cannot commit.
2. Make frontend YouTube URL acceptance and payload canonicalization match backend behavior. Reject malformed shorthand/backslash syntax or send the exact validated canonical form; prove missing `//`, backslash, user-info, port, host, whitespace, and 255/256 boundaries. Preserve visible/user-entered semantics where safe.
3. Correct `CR-031-077`: apply the existing 500-character note bound, guidance/counter, local 501 rejection, and exact payload tests to `Admin WhitelistChannelManagePage`, not Company Certification. Remove or leave only clearly justified non-churning certification tests; do not credit them to CR-077.
4. Reduce all three documentation files to semantic WI-051 changes only. Remove unrelated table/list/payment formatting churn and correct note traceability to ADMIN Whitelist.
5. Extend Whitelist all-status tests to prove exact positive and zero API call counts for every visible/ineligible edit, request, primary, delete/removal action.

[INPUT POINTERS]

## Tier 0

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`

## Tier 1

- `docs/policies/quality-gates.md`
- `docs/policies/access-control-policy.md`

## Tier 2

- `docs/standards/frontend-standards.md`
- `docs/design/api-spec.md`
- `docs/design/usecase/whitelist.md`
- `docs/design/usecase/company-certification.md`

## Review and Work Context

- `deliverables/agent/WI-20260809-ATS-051-handoff.md`
- `deliverables/agent/WI-20260809-ATS-051-qa-integ-review-result.md`
- `deliverables/agent/WI-20260809-ATS-026-findings.md:109-119`
- Current uncommitted diff

[ACCEPTANCE]

- All P2 findings are closed with focused regression tests.
- Documentation diff contains no unrelated formatting-only hunks.
- CR-031-077 traceability names ADMIN Whitelist note handling.
- Focused frontend/backend tests, typecheck, ESLint, changed-file Prettier, build, docs validation, and diff check pass.

[FORBIDDEN]

- No CR-031-074 policy decision, schema/dependency change, real export/download/upload/provider/mail/payment/DB effect, commit/push, or protected-output access.
- Do not inspect ignored secrets or touch `output/client-demo-screenshots-20260716-140514.zip` / `output/ui-ux-audit/`.

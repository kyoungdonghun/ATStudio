---
version: 1.0
last_updated: 2026-08-16
project: ATS
owner: PG
category: agent
status: complete
dependencies:
  - path: deliverables/agent/WI-20260809-ATS-060-handoff.md
    reason: Approved decision-only WI boundary
  - path: deliverables/agent/WI-20260809-ATS-060-decision-register.md
    reason: Remediated decision record
  - path: deliverables/agent/WI-20260809-ATS-060-qa-integ-review.md
    reason: Source-pointer formatting correction
---

# WI-060 Decision-Record Remediation Result

## Corrections

- Clarified that unauthenticated-signup consent records attach to the newly created persistent user ID in the same transaction. The approved "authenticated user identity" is the durable account identity, never a pre-login `SecurityContext`, token, or `/users/me` identity.
- Clarified that consent category and immutable policy/document version identify the exact presented policy, and that the agreed timestamp is server-generated at persistence. No timestamp precision, time-zone, schema, or retention choice was added.
- Limited terminal `401` logout success to a logout-contract-confirmed already-invalid or revoked session. Bare or unclassified `401` responses and every other settled failure now require local sign-out plus the unconfirmed-revocation warning. The observed-current-state and future-follow-up statements use the same boundary.
- Reformatted the QA review source pointers to use `path` plus `(lines A-B)` and removed all `path:line` notation.

## Validation

- Documentation validation: `python .agents/skills/validate-docs/scripts/validate_docs.py` passed.
- Whitespace validation: `git diff --check` passed.
- The validator-safe source-pointer scan found no `path:line` notation in the remediated decision register or QA review.

## External Effects And Rollback

- No product source, tests, schema, runtime configuration, or other documentation was modified.
- No protected output or secrets were inspected. No browser, network, authentication, mail, provider, database, Git stage, commit, push, or external action was executed.
- Rollback is documentation-only: restore the prior versions of the two modified agent deliverables and remove this result file. No runtime or external-state rollback exists.

# Participant player actions

WI-20261123-ATS-016 · qa-fe · REQ-20261123-ATS-003 (approved). Coordinate with WI014. Blocks final WI015 review.

## Scope and acceptance

Own frontend/src/layouts/PlayerBar.tsx and associated player drawer and tests. Reuse the existing UI for the eligible pilot user to like, save and access personal collections while playing; reuse the existing pilot-safe download, verify delayed responses across account changes, preserve guest listening, playback and sales behavior. Use the pilot capacity contract for eligibility and avoid exposing closed selling actions. Desktop and mobile tests must cover at least the normal flow and an identity transition.

## Input pointers and outputs

Tier 0: docs/standards/core-principles.md, docs/standards/development-standards.md, docs/standards/documentation-standards.md, docs/standards/glossary.md. Tier 1: docs/policies/security-policy.md. Tier 2: docs/ui/screen-flow.md and WI-20261123-ATS-003-summary.md. Record the exact files and focused regression outcomes in the matching evidence pack and user summary. Production and Git publication are outside this WI.

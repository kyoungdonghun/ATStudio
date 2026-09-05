# WI Handoff Packet: WI-20260823-ATS-003

[WI HEADER]
WI ID: WI-20260823-ATS-003
REQ: REQ-20260823-ATS-001
Agent: docops
Depends On: WI-20260823-ATS-001, WI-20260823-ATS-002, WI-20260823-ATS-004
Blocks: WI-20260823-ATS-004

[WI SUMMARY]
Why: Align current-state documentation with the verified client-feedback remediation without changing historical SR/REQ records or overstating unverified runtime behavior.

Scope (in):
- After WI-002 passes or identifies required corrections, update current product documentation for:
  - BUSINESS existing `companyName` as the single `Company name or industry` descriptor and individual-only `job`.
  - nickname internal spaces with leading/trailing trim normalization.
  - public catalog repeated mood selection visibility.
  - Playlist `Play all` versus non-starting `Add all to queue`.
  - PlayerBar direct Likes action and Question-list responsive FAB.
  - safe local billing-key example/current shape only if a current document gives obsolete local bootstrap guidance.
- Preserve SR-55 and SR-72 as historical policy records; do not change plan limits or default-playlist timing.
- Update indexes only if new documentation files are created (not expected).

Scope (out):
- No source code, schema/data/storage, secret, client worktree, or historical-record rewrite.
- Do not claim the known development media/storage mismatch is fixed.

DoD:
- Documentation states only observed/verified current behavior and uses canonical terms.
- No new industry DB field or changed repeat/default-playlist/plan policy is implied.
- `validate-docs` and `git diff --check` results are recorded.

[ACCEPTANCE CRITERIA]
- [ ] User-info and API documentation distinguish individual job from business companyName descriptor.
- [ ] Nickname normalization contract covers UI/API/server consistency.
- [ ] UI/use-case docs cover multi-mood selection, Play all, direct Likes, and FAB behavior accurately.
- [ ] Links/index integrity is verified.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1:
- docs/policies/quality-gates.md

Tier 2:
- docs/design/api-spec.md
- docs/design/usecase/user-info.md
- docs/design/usecase/sound-playlist.md
- docs/ui/atstudio-front-list.md
- docs/ui/screen-flow.md
- docs/SR/SR-55.md
- docs/SR/SR-72.md

REQ/Context Docs:
- deliverables/user/REQ-20260823-ATS-001.md
- deliverables/agent/WI-20260823-ATS-001-evidence-pack.md
- deliverables/agent/WI-20260823-ATS-002-evidence-pack.md (read after its completion)

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260823-ATS-003-summary.md
Agent-facing -> deliverables/agent/WI-20260823-ATS-003-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260823-ATS-003-handoff.md

[TRACEABILITY REQUIREMENTS]
- Cite exact changed current documents and source pointers.
- Treat SR/REQ as historical evidence, not a mutable current-state source.
- Do not write new product policy outside the approved REQ.

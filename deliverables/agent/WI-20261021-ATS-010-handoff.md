---
version: 1.0
last_updated: 2026-10-21
project: ATS
owner: MA
category: reference
status: active
---
# WI010 Local Audio Trial Preparation

[WI HEADER]
REQ: REQ-20261011-ATS-007, approved / active; explicit current user approval is local audio tool preparation and independent review only.
Agent: SE. Depends On: WI009 completed authentication evidence. Blocks: WI011 independent PG review, then WI012 documentation.
Created using create-wi-handoff-packet.

[WI SUMMARY]
Prepare usable local audio-trial tooling around the retained successful v8r1 tools and WI068 JAR. Preserve every historical tool/freeze/receipt. One previously approved synthetic WAV only. No remote operation is authorized. Develop and locally test concrete request/data/worker/cleanup contracts and reusable tools; do not merely write a plan or claim native readiness from mocks. Identify any integration still required precisely.

[ACCEPTANCE CRITERIA]
- Verify retained 643 source inputs, JAR and selected WAV identity and reused tool members; no product source/JAR/SPA rebuild or edit.
- Reuse existing tools and successful native-format handling, collation fix and cleanup_steps shape. Do not replay consumed Release/Start approvals or reuse a review for changed bytes.
- Prepare bounded upload, conversion READY, stream/Range/seek, authorized original download hash and negative access checks. Public stream permission is intentional; direct original and unauthorized official download must be tested distinctly.
- Inspect actual API DTO/response/security and audio-only conditions. Account for multipart WAV size, async worker/poll timing, file journals, worker child process restrictions, network isolation, response/wire limits, and final data changes. No blanket auth-only 404 or audio-worker prohibition reused.
- Account/data proposal lists existing-user ADMIN promotion or alternative, USER entitlement prerequisite, track activation, all expected DB/file side effects and explicit preservation/cleanup choices. No real principal identifiers/private inputs, granting entitlement or changing accounts/data.
- Default local entrypoint cannot connect, launch app, mutate DB, mail, purchase, install dependencies or enable services. Synthetic fixtures/mocks only for tests, finite bounds, failure/replay/tamper tests; final exact hashes and honest integration/native limitations.
- Own only new build/WI010-audio-trial/ and own WI010 evidence/user summary; source tools are read-only. Notify MA before expanding scope. No deletes, commits, push, secret/config reading, SSH/cloud/browser access or service changes.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md; docs/standards/documentation-standards.md; docs/standards/development-standards.md; docs/standards/glossary.md.
Tier1: docs/policies/security-policy.md; docs/policies/quality-gates.md; .claude/agents/se.md.
Tier2: docs/index.md Current Decision Entry; deliverables/user/REQ-20261011-ATS-007.md; WI-20261021-ATS-009/006/007 evidence; WI-20261011-ATS-063/064/065/068 evidence.
Assets: build/WI067-auth-trial-v8r1/; build/WI067-pc-delivery-v8r1/; build/WI067-private-producer-v8r1/; build/WI068-release-run-02/source-before.json; build/WI068-release-run-02/build/libs/ATStudio-0.0.1-SNAPSHOT.jar; build/WI063-fixtures-final/WI063-tone-a.wav; scripts/validation/feature-trial/.
Code: TrackController, DownloadService, TrackService, audio worker/conditions, Storage journal, DTOs, frontend audio/upload callers as needed read-only.

[OUTPUT CONTRACT]
User-facing: deliverables/user/WI-20261021-ATS-010-summary.md.
Agent-facing: deliverables/agent/WI-20261021-ATS-010-evidence-pack.md using create-wi-evidence-pack.
Tools and synthetic test evidence: build/WI010-audio-trial/.
Report exact commands, outcomes, owned files, hashes, limitations, approval points and rollback (preserve originals; no automatic deletion). On completion immediately notify MA for WI011.

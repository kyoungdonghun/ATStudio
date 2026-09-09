
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A060: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a060). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

[WI HEADER]
WI ID: WI-20260817-ATS-031
REQ: REQ-20260817-ATS-010
Agent: qa-integ
Depends On: WI-20260817-ATS-030
Blocks: external-effect rehearsal, backup/restore rehearsal, independent release evidence review

[WI SUMMARY]
Why: Correct WI-030's incomplete live catalogue evidence without copying unrelated development data or private files. The current rehearsal clone has a valid schema but lacks playable tracks.
Scope (in/out):
- In: create one new guarded local rehearsal-only database; transfer only the already-approved `AT.M Demo` catalogue fixture set from the development environment into it (one verified demo admin, 20 dedicated tags, exactly 10 active demo tracks, one active 10-track album, and public audio assets needed by those tracks); use distinct rehearsal storage and ports; boot with `ddl-auto=validate`; leave the owned rehearsal frontend/backend running for a subsequent parent-led browser check.
- Out: all unrelated development data, all private storage (company documents, private uploads), production/stage/remote systems, client acceptance runtime/database, source/configuration edits, Cloudflare, SMTP, Toss/provider/payment/refund/renewal/settlement calls, DB or file deletion.
DoD:
- A new loopback-only rehearsal target contains only the approved demo fixture entities plus baseline seed data required by FK/runtime contracts; it must not be a full development DB clone.
- Every demo track exposes actual media and waveform data in the rehearsal runtime; the album contains the exact ten tracks.
- Spring starts with Hibernate `ddl-auto=validate` against the target and uses isolated public storage containing only the required public audio assets.
- Provider/email operations are fail-closed and no external request is made.
- Owned runtime remains running only on new ports and provides a secret-safe manifest/stop instruction for the parent browser verification; do not stop it before that check unless startup fails.
Constraints/Forbidden:
- Work only from $USERPROFILE\Desktop\project\ATStudio on codex/v1-release-rehearsal-fixes.
- The user authorized creation/mutation of the newly generated local rehearsal DB and isolated public storage only. Never modify or query unrelated development entities beyond the exact approved `AT.M Demo` fixture selection.
- Validate every selected source entity against the unique demo prefix/category/name rules before copying. If the set is ambiguous, stop and report rather than broadening selection.
- Do not copy private storage paths, user documents, billing keys, payment records, refresh tokens, or non-demo user data.
- Do not reveal credentials, database names/URLs, raw file paths with personal identifiers, or raw provider/mail configuration.
- Existing client ports `5173`/`8080` are read-only boundaries. Use only available rehearsal ports.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Exact demo fixture selection is proved from the development source without reading/copying unrelated rows into the target.
- [ ] Target has baseline plans plus exactly the scoped demo fixture composition needed for public catalogue/track/album behavior.
- [ ] All ten tracks return playable media and non-empty waveform/duration metadata; album membership is exactly ten unique demo tracks.
- [ ] Isolated Spring runtime reaches ready with `ddl-auto=validate`; public catalogue/detail/tag/capabilities endpoints respond through its own frontend/API route.
- [ ] Payment, mail, and all external provider boundaries are verified fail-closed; no external-effect request occurs.
- [ ] Owned runtime remains available for follow-up browser interaction and can be stopped only by its manifest/owned process IDs.
Quality:
- [ ] No source/tracked configuration changes and `git diff --check` passes.
- [ ] Evidence separates source selection, target persistence, storage/media, runtime/API, and non-executed external operations.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-010.md
- deliverables/user/REQ-20260817-ATS-011.md
- deliverables/user/WI-20260817-ATS-030-summary.md
- deliverables/agent/WI-20260817-ATS-030-evidence-pack.md
- deliverables/user/WI-20260817-ATS-027-summary.md
- deliverables/agent/WI-20260817-ATS-027-evidence-pack.md
- scripts/acceptance/README.md

Files:
- src/main/resources/schema.sql
- src/main/resources/seed.sql
- src/main/resources/application.yml
- frontend/vite.config.ts
- src/main/java/com/atstudio/atstudio/service/storage/
- src/main/java/com/atstudio/atstudio/service/TrackService.java
- src/main/java/com/atstudio/atstudio/service/AlbumService.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-031-summary.md:
- Korean concise outcome, exact fixture scope, live endpoint evidence, running runtime boundary, and next browser-validation step.
Agent-facing -> deliverables/agent/WI-20260817-ATS-031-evidence-pack.md:
- Secret-safe source/target selection proof, media/waveform/album checks, process ownership and stop instruction, API/runtime evidence, and non-executed external actions.
Handoff Packet -> deliverables/agent/WI-20260817-ATS-031-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Include exact count and endpoint/status evidence without publicizing credentials or DB identities.
Rollback: No DB/storage deletion is authorized. Leave the new rehearsal DB/storage retained. The runtime remains active for parent browser verification and must be stopped later only through the recorded owned-process instruction.

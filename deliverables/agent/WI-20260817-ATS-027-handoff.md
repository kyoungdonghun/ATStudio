
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A053: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a053). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

[WI HEADER]
WI ID: WI-20260817-ATS-027
REQ: REQ-20260817-ATS-011
Agent: qa-integ
Depends On: REQ-20260817-ATS-011 approved
Blocks: WI-20260817-ATS-028

[WI SUMMARY]
Why: Seed one identical, safely inspectable acceptance catalog into exactly the development MySQL database and the existing client acceptance disposable database.
Scope (in): Secret-safe preflight for exactly two local targets; provision one verified ADMIN account with a shared high-entropy password; create dedicated USAGE, GENRE, MOOD, and INSTRUMENT dummy tags; create ten playable sine-wave dummy tracks through supported application upload, analysis, storage, tag, and activation APIs; create one active album containing precisely those ten tracks; collect environment-specific and parity evidence.
Scope (out): Any other database, existing unrelated data, production/stage/remote services, payment/refund actions, email delivery, public URL/configuration changes, source functional changes, and secret disclosure.
DoD: Both targets authenticate the same ADMIN; each contains the approved catalog and album; all ten tracks have media retrieval and waveform data; strict API/storage/persisted-state parity proof passes without credentials, JDBC values, or raw secrets in outputs.
Constraints/Forbidden: Do not create data through direct SQL except the single ADMIN account when no supported no-email provisioning route exists. Never print/store plaintext credentials, connection information, provider values, or secrets. Do not delete or alter existing unrelated data. Do not create playlists, more than ten scoped tracks, or more than one scoped album. All audio/tag/album relations must use application APIs/services.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Exactly two local targets pass secret-safe allow-list preflight.
- [ ] Both targets accept one same verified ADMIN credential without triggering external email.
- [ ] Both targets contain dedicated tags in USAGE, GENRE, MOOD, and INSTRUMENT categories.
- [ ] Both targets contain exactly ten scoped active tracks with media, duration, waveform, and tag relations.
- [ ] Both targets contain exactly one scoped active album with the ten scoped tracks exactly once each.
- [ ] API, storage retrieval, and persisted-state parity evidence is collected for both targets.
Quality:
- [ ] No tracked source/config behavior change is made unless a supported workflow gap requires a separately reported blocker.
- [ ] Evidence is secret-safe and records exact scoped counts plus reproducible safe commands.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-011.md
- docs/design/api-spec.md
- src/main/resources/schema.sql

Files:
- scripts/demo/seed-client-demo.mjs
- scripts/demo/test-seed-client-demo.ps1
- src/main/java/com/atstudio/atstudio/controller/TrackController.java
- src/main/java/com/atstudio/atstudio/controller/AlbumController.java
- src/main/java/com/atstudio/atstudio/controller/TagController.java
- src/main/java/com/atstudio/atstudio/service/TrackService.java
- src/main/java/com/atstudio/atstudio/service/AlbumService.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-027-summary.md:
- Scope, safe outcome/counts, user credential redaction assertion, risks, and non-actions.
Agent-facing -> deliverables/agent/WI-20260817-ATS-027-evidence-pack.md:
- Safe commands/results, target isolation proof, API/storage/persistence evidence, parity proof, and rollback boundary.
Handoff Packet -> deliverables/agent/WI-20260817-ATS-027-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required. Keep secrets and target identity values out of all artifacts.
Tests: Record API authentication, tag/track/album behavior, HTTP media retrieval, waveform presence, exact scoped count, and parity checks.
Rollback: No automatic cleanup. Delete only the scoped prefix data after separate approval.

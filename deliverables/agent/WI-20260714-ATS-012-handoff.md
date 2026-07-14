[WI HEADER]
WI ID: WI-20260714-ATS-012
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260714-ATS-003, WI-20260714-ATS-004
Blocks: WI-20260714-ATS-009, WI-20260714-ATS-010, WI-20260714-ATS-019, WI-20260714-ATS-021, WI-20260714-ATS-024, WI-20260714-ATS-025

[WI SUMMARY]
Why: Make file creation, replacement, and deletion converge with DB commit/rollback and leave durable retry evidence when cleanup fails.
Scope (in/out):
- In: Typed public/private storage roots, staged/generated-key writes, strict normalized reads, transaction-aware rollback/after-commit cleanup, approved `storage_mutations` journal, bounded single-server recovery, and migration of Track/Playlist/Album/Certification/Notice/Question file mutations.
- In: Fresh DDL and a separate ordered manual patch for the journal; no application to a DB.
- Out: Playlist image authenticity/canonicalization (WI-009), certification format authenticity/quarantine policy (WI-010), physical source-audio relocation, malware scanning, multi-server scheduler locks, and data migration.
DoD:
- New files are removed on rollback, old files are deleted only after commit, and cleanup failure is durable/retryable.
- Deletes never escape declared roots and never silently claim success on failure.
- Existing domain behavior remains compatible while all file mutations use the coordinator.
- Recovery is idempotent, bounded, single-server, and contains no PII/original names/raw exceptions in its journal.
Constraints/Forbidden:
- User approved the `storage_mutations` schema design, but this WI must not apply DDL or create/delete a DB.
- No new library; use Spring transactions, JPA, and Java NIO.
- Do not inspect uploaded document bodies or migrate legacy paths/data.
- Preserve concurrent WI-004 schema changes and do not revert other workers.
- Existing runtime logs and PID files are untouched and never staged.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Storage API distinguishes public/private roots and generated relative keys.
- [ ] Create/replace/delete paths cover rollback, after-commit, partial multi-file failure, shared-reference protection, retry, and stale journal recovery.
- [ ] Path traversal, absolute paths, backslashes/colon/NUL, symlinks, and non-regular files fail closed.
- [ ] Journal transitions and claim/retry scheduling are deterministic and idempotent.
- [ ] Fresh schema/manual patch define indexes and states without auto-deleting legacy data.
Performance:
- [ ] Recovery claims a bounded batch and uses bounded retries/backoff.
- [ ] File bytes are not duplicated unnecessarily beyond staging/final promotion requirements.
Quality:
- [ ] Focused unit/integration tests cover every affected domain mutation matrix.
- [ ] Full backend tests, compile, and `git diff --check` pass for the integrated change.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/audit/p1-remediation-trace-matrix-20260714.md
- docs/design/p1-security-acceptance-hardening-design.md
- src/main/java/com/atstudio/atstudio/service/StorageService.java
- src/main/java/com/atstudio/atstudio/service/LocalStorageService.java
- src/main/java/com/atstudio/atstudio/service/TrackService.java
- src/main/java/com/atstudio/atstudio/service/PlaylistService.java
- src/main/java/com/atstudio/atstudio/service/AlbumService.java
- src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java
- src/main/java/com/atstudio/atstudio/service/NoticeService.java
- src/main/java/com/atstudio/atstudio/service/QuestionService.java
- src/main/resources/schema.sql
- src/main/resources/db/manual/
- relevant service/storage tests

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-012-summary.md (Korean)
Agent-facing -> deliverables/agent/WI-20260714-ATS-012-evidence-pack.md
Handoff -> deliverables/agent/WI-20260714-ATS-012-handoff.md
Implementation ownership -> storage abstraction/coordinator/journal/recovery, affected domain lifecycle adaptations, schema/manual patch, focused tests.

[TRACEABILITY REQUIREMENTS]
Evidence pointers and exact test commands: Required
Rollback: application-first; preserve pending journal rows and never blind-delete files
DB proof/application: deferred to WI-021 and later local-DB approval

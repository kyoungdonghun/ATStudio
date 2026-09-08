---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: MA
category: reference
status: active
---

# WI-20260909-ATS-003: Retained History and Restartable Storage Mutations

[WI HEADER]
WI ID: WI-20260909-ATS-003
REQ: REQ-20260909-ATS-001
Agent: se
Depends On: none
Blocks: 012, 006, 013, 014

[WI SUMMARY]
Scope: DATA-01/02/03 and STORAGE-04. Preserve licenses/download history/quota on Track soft deletion. Reconcile thumbnail delete/audit semantics for both Playlist and Album including shared/no-thumbnail. Prove and fix stale Track media writes (including likes/download counters) using existing transaction/lock patterns without DDL. Clean/close partial stage streams, own failed-stage recovery and test first/later partial writes and cleanup failure.
Write ownership: TrackService.java, PlaylistService.java, AlbumService.java, LikeService.java, DownloadService.java, TrackRepository.java; entity/Track.java, Playlist.java, Album.java only necessary non-schema methods; service/storage/LocalStorageService.java, StorageMutationCoordinator.java, StorageCleanupService.java, StorageReferenceChecker.java, StorageIntegrityService.java and corresponding/new domain/storage tests. Do not edit auth/payment/build/schema/private runtime settings.
Constraints: Do not weaken strict production storage audit. Do not repair or delete existing MySQL/media/history. Synthetic temp storage and H2 only. Favor established pessimistic locks across material Track writers; check lock order so no user<->track deadlock introduced. No new @Version column/DDL unless separately approved. Preserve historical audit documents; report exact semantics and schema unchanged.
Forbidden: touching unrelated changes, DDL/real DB/media deletion or repair, credential inspection, external payments/refunds/mail, runtime restart, Git writes, subdelegation. Product edits via apply_patch only; existing fake-provider/H2/temp-file tests only. MA serializes Gradle/npm runs; send requested command/test list, do not start heavy runners independently.

[ACCEPTANCE CRITERIA]
- [ ] Concrete regression scenarios fail for the original defect (when executed) and pass after the minimal fix; distinguish unrun from passed.
- [ ] Existing policy/authorization/idempotency/strict integrity guards retained; no schema or broad abstraction churn.
- [ ] Shared-file boundaries respected; list exact changed paths and test candidates/risks.
- [ ] Evidence pack and user summary created; hand back to downstream WI rather than declare production GO.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/documentation-standards.md; docs/standards/development-standards.md; docs/standards/glossary.md.
Tier 1: docs/policies/security-policy.md; docs/policies/quality-gates.md.
Tier 2: deliverables/user/REQ-20260909-ATS-001.md; deliverables/agent/WI-20260908-ATS-018-findings.md; WI-20260908-ATS-015/016/017-evidence-pack.md (only relevant scope); docs/design/api-spec.md; current related usecase/design docs. Frontend work: .agents/skills/react-best-practices/SKILL.md.
Snapshot: main 8161f0a; prior SR-93/audit documents already dirty/untracked and protected. No secrets/data reads needed.

[OUTPUT CONTRACT]
Implement directly in the shared workspace, only owned paths. User-facing: deliverables/user/WI-20260909-ATS-003-summary.md.
Agent-facing: deliverables/agent/WI-20260909-ATS-003-evidence-pack.md using create-wi-evidence-pack.
Report exact files, root cause, tests run/not run, deployment impact and remaining dependency. Do not edit this handoff or other agents' reports.

[TRACEABILITY REQUIREMENTS]
MA scope clarification (2026-09-09): StorageMutationJournalService and a minimal same-process active-operation holder plus tests are authorized to prevent PREPARED recovery from deleting live uploads beyond the existing 300-second grace. Exclusion must occur before claim/attempt increment and ownership must release on failure/transaction completion. This preserves the single-runtime contract, requires no DDL and does not replace restart recovery. AlbumLikeService/AlbumRepository and focused tests are also authorized only for the same stale-thumbnail/deactivation writer race; no unrelated album refactor.

Every relevant code/test change maps to a finding ID. Source pointers and reproducible safe tests required. No test weakening, no historical-data cleanup. English technical reports; Korean concise completion message.
